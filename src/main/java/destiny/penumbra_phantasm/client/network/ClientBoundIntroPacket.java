package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundIntroPacket
{
	public BlockPos pos;
	public ResourceKey<Level> level;
	public ClientBoundIntroPacket(BlockPos pos, ResourceKey<Level> level)
	{
		this.pos = pos;
		this.level = level;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeBlockPos(this.pos);
		buffer.writeResourceKey(this.level);
	}

	public static ClientBoundIntroPacket decode(FriendlyByteBuf buffer)
	{
		BlockPos pos = buffer.readBlockPos();
		ResourceKey<Level> level = buffer.readResourceKey(Registries.DIMENSION);

		return new ClientBoundIntroPacket(pos, level);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			ClientboundPacketHandler.openIntroScreen(pos, level);
		});
		return true;
	}
}
