package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class ClientBoundFountainData
{
	public HashMap<BlockPos, DarkFountain> fountains;

	public ClientBoundFountainData(HashMap<BlockPos, DarkFountain> fountains)
	{
		this.fountains = fountains;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeCollection(this.fountains.entrySet(), (writer, entry) -> {
			DarkFountain fountain = entry.getValue();

			writer.writeBlockPos(fountain.getFountainPos());
			writer.writeResourceKey(fountain.getFountainDimension());

			writer.writeBlockPos(fountain.getDestinationPos());
			writer.writeResourceKey(fountain.getDestinationDimension());

			writer.writeInt(fountain.getAnimationTimer());
			writer.writeInt(fountain.getFrameTimer());
			writer.writeInt(fountain.getFrame());

			writer.writeCollection(fountain.teleportedEntities, FriendlyByteBuf::writeUUID);
		});
	}

	public static ClientBoundFountainData decode(FriendlyByteBuf buffer)
	{
		List<Map.Entry<BlockPos, DarkFountain>> darkFountains = buffer.readCollection(i -> new ArrayList<>(), reader -> {
			BlockPos fountainPos = reader.readBlockPos();
			ResourceKey<Level> fountainDim = reader.readResourceKey(Registries.DIMENSION);

			BlockPos targetPos = reader.readBlockPos();
			ResourceKey<Level> targetDim = reader.readResourceKey(Registries.DIMENSION);

			int animationTimer = reader.readInt();
			int frameTimer = reader.readInt();
			int frame = reader.readInt();

			HashSet<UUID> teleportedEntities = reader.readCollection(ii -> new HashSet<>(), FriendlyByteBuf::readUUID);

			DarkFountain fountain = new DarkFountain(fountainPos, fountainDim, targetPos, targetDim, animationTimer, frameTimer, frame, teleportedEntities);

			return Map.entry(fountainPos, fountain);
		});
		HashMap<BlockPos, DarkFountain> fountainMap = new HashMap<>();
		darkFountains.forEach(entry -> fountainMap.put(entry.getKey(), entry.getValue()));

		return new ClientBoundFountainData(fountainMap);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			ClientLevel level = Minecraft.getInstance().level;
			if(level != null)
			{
				level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
					cap.darkFountains = this.fountains;
				});
			}
		});
		return true;
	}
}
