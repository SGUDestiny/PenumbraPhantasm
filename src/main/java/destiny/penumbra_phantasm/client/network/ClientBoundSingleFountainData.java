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

		buffer.writeInt(fountain.getOpeningTick());
		buffer.writeInt(fountain.getFrameTick());
		buffer.writeInt(fountain.getFrame());
		buffer.writeInt(fountain.getFrameOptimized());

		buffer.writeCollection(fountain.teleportedEntities, FriendlyByteBuf::writeUUID);

		buffer.writeCollection(fountain.shockwaveTickers, FriendlyByteBuf::writeInt);

		buffer.writeInt(fountain.sealingTick);
		buffer.writeInt(fountain.sealingFrameTick);
		buffer.writeFloat(fountain.sealingFrameTickProgress);
	}

	public static ClientBoundSingleFountainData decode(FriendlyByteBuf buffer)
	{
		BlockPos fountainPos = buffer.readBlockPos();
		ResourceKey<Level> fountainDim = buffer.readResourceKey(Registries.DIMENSION);

		BlockPos targetPos = buffer.readBlockPos();
		ResourceKey<Level> targetDim = buffer.readResourceKey(Registries.DIMENSION);

		int openingTick = buffer.readInt();
		int frameTick = buffer.readInt();
		int frame = buffer.readInt();
		int frameOptimized = buffer.readInt();

		HashSet<UUID> teleportedEntities = buffer.readCollection(ii -> new HashSet<>(), FriendlyByteBuf::readUUID);

		List<Integer> shockwaveTickers = buffer.readCollection(ii -> new ArrayList<>(), FriendlyByteBuf::readInt);

		int sealingTick = buffer.readInt();
		int sealingFrameTick = buffer.readInt();
		float sealingFrameTickProgress = buffer.readFloat();

		DarkFountain fountain = new DarkFountain(fountainPos, fountainDim, targetPos, targetDim, openingTick,
				frameTick, frame, frameOptimized, teleportedEntities, shockwaveTickers, sealingTick, sealingFrameTick, sealingFrameTickProgress);

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
