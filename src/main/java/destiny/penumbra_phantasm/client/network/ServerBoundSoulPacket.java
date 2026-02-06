package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.capability.SoulType;
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

			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap ->
				{
					cap.soulType = SoulType.byId(this.soulType);
				});
		});
		return true;
	}
}
