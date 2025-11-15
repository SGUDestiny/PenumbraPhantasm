package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class ClientBoundFountainData
{
	public HashMap<UUID, DarkFountain> fountains;

	public ClientBoundFountainData(HashMap<UUID, DarkFountain> fountains)
	{
		this.fountains = fountains;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeCollection(this.fountains.entrySet(), (writer, entry) -> {
			DarkFountain fountain = entry.getValue();

			writer.writeUUID(entry.getKey());

			writer.writeBlockPos(fountain.getFountainPos());
			writer.writeResourceKey(fountain.getFountainDimension());

			writer.writeBlockPos(fountain.getDestinationPos());
			writer.writeResourceKey(fountain.getDestinationDimension());

			writer.writeInt(fountain.animationTimer);
			writer.writeInt(fountain.getAnimationTick());
			writer.writeInt(fountain.frameTimer);
			writer.writeInt(fountain.frame);
		});
	}

	public static ClientBoundFountainData decode(FriendlyByteBuf buffer)
	{
		List<Map.Entry<UUID, DarkFountain>> darkFountains = buffer.readCollection(i -> new ArrayList<>(), reader -> {
			UUID uuid = reader.readUUID();

			BlockPos fountainPos = reader.readBlockPos();
			ResourceKey<Level> fountainDim = reader.readResourceKey(Registries.DIMENSION);

			BlockPos targetPos = reader.readBlockPos();
			ResourceKey<Level> targetDim = reader.readResourceKey(Registries.DIMENSION);

			int animTimer = reader.readInt();
			int animTick = reader.readInt();
			int frameTimer = reader.readInt();
			int frame = reader.readInt();

			DarkFountain fountain = new DarkFountain(fountainPos, fountainDim, targetPos, targetDim, animTick);
			fountain.animationTimer = animTimer;
			fountain.frame = frame;
			fountain.frameTimer = frameTimer;

			return Map.entry(uuid, fountain);
		});
		HashMap<UUID, DarkFountain> fountainMap = new HashMap<>();
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
