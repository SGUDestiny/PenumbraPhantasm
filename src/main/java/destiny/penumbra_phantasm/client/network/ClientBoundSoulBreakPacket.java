package destiny.penumbra_phantasm.client.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSoulBreakPacket
{
	public boolean diedWithSoulHearth;
	public int soulType;
	public ClientBoundSoulBreakPacket(boolean diedWithSoulHearth, int soulType)
	{
		this.diedWithSoulHearth = diedWithSoulHearth;
		this.soulType = soulType;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(this.diedWithSoulHearth);
		buffer.writeInt(this.soulType);
	}

	public static ClientBoundSoulBreakPacket decode(FriendlyByteBuf buffer)
	{
		boolean diedWithSoulHearth = buffer.readBoolean();
		int soulType = buffer.readInt();
		return new ClientBoundSoulBreakPacket(diedWithSoulHearth, soulType);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			ClientboundPacketHandler.syncSoulBreak(diedWithSoulHearth, soulType);
		});
		return true;
	}
}
