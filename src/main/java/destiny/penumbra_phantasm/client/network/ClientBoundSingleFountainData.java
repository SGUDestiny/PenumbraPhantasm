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

public class ClientBoundSingleFountainData
{
	public DarkFountain fountain;

	public ClientBoundSingleFountainData(DarkFountain fountain)
	{
		this.fountain = fountain;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeBlockPos(fountain.getFountainPos());
		buffer.writeResourceKey(fountain.getFountainDimension());

		buffer.writeBlockPos(fountain.getDestinationPos());
		buffer.writeResourceKey(fountain.getDestinationDimension());

		buffer.writeInt(fountain.getAnimationTimer());
		buffer.writeInt(fountain.getFrameTimer());
		buffer.writeInt(fountain.getFrame());

		buffer.writeCollection(fountain.teleportedEntities, FriendlyByteBuf::writeUUID);
	}

	public static ClientBoundSingleFountainData decode(FriendlyByteBuf buffer)
	{
		BlockPos fountainPos = buffer.readBlockPos();
		ResourceKey<Level> fountainDim = buffer.readResourceKey(Registries.DIMENSION);

		BlockPos targetPos = buffer.readBlockPos();
		ResourceKey<Level> targetDim = buffer.readResourceKey(Registries.DIMENSION);

		int animationTimer = buffer.readInt();
		int frameTimer = buffer.readInt();
		int frame = buffer.readInt();

		HashSet<UUID> teleportedEntities = buffer.readCollection(ii -> new HashSet<>(), FriendlyByteBuf::readUUID);

		DarkFountain fountain = new DarkFountain(fountainPos, fountainDim, targetPos, targetDim, animationTimer,
				frameTimer, frame, teleportedEntities);

		return new ClientBoundSingleFountainData(fountain);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			ClientLevel level = Minecraft.getInstance().level;
			if(level != null)
			{
				level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
					DarkFountain realFountain = cap.darkFountains.get(fountain.getFountainPos());
					if(realFountain == null)
						realFountain = fountain;
					realFountain.sync(this.fountain);
					cap.darkFountains.put(realFountain.getFountainPos(), realFountain);
				});
			}
		});
		return true;
	}
}
