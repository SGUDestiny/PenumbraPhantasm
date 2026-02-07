package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ClientboundPacketHandler
{
	public static void openIntroScreen(BlockPos pos, ResourceKey<Level> dim)
	{
		Minecraft minecraft = Minecraft.getInstance();

		minecraft.setScreen(new IntroScreen(() -> {
			minecraft.setScreen(null);
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundIntroPacket(pos, dim));
		}));
	}
}
