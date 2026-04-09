package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.client.network.ClientBoundSoulBreakPacket;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerBoundSoulPacket
{
	public int soulType;

	public ServerBoundSoulPacket(int soulType)
	{
		this.soulType = soulType;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeInt(this.soulType);
	}

	public static ServerBoundSoulPacket decode(FriendlyByteBuf buffer)
	{
		int soulType = buffer.readInt();
		return new ServerBoundSoulPacket(soulType);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if(player == null)
				return;

			int clamped = Mth.clamp(this.soulType, 1, 7);
			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> cap.soulType = clamped);
			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundSoulBreakPacket(false, clamped));
		});
		return true;
	}
}
