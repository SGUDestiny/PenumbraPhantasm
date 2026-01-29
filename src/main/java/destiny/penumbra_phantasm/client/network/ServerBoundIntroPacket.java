package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundIntroPacket
{
	public BlockPos destinationPos;
	public ResourceKey<Level> destinationLevel;

	public ServerBoundIntroPacket(BlockPos pos, ResourceKey<Level> level)
	{
		this.destinationPos = pos;
		this.destinationLevel = level;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeBlockPos(destinationPos);
		buffer.writeResourceKey(destinationLevel);
	}

	public static ServerBoundIntroPacket decode(FriendlyByteBuf buffer)
	{
		BlockPos pos = buffer.readBlockPos();
		ResourceKey<Level> key = buffer.readResourceKey(Registries.DIMENSION);
		return new ServerBoundIntroPacket(pos, key);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if(player == null)
				return;

			ServerLevel level = player.getServer().getLevel(destinationLevel);
			if(level == null)
				return;

			player.teleportTo(level, destinationPos.getX(), destinationPos.getY(), destinationPos.getZ(), (float)Math.toDegrees(Math.atan2((float) player.getLookAngle().x(), (float) player.getLookAngle().z()) + 270), player.getXRot());
			player.connection.send(new ClientboundSetEntityMotionPacket(player));

			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap ->
				{
					DarkFountain fountain = cap.darkFountains.get(destinationPos);
					fountain.teleportedEntities.add(player.getUUID());
				});
		});
		return true;
	}
}
