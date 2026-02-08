package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
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

	public static void syncSoulBreak(boolean diedWithSoulHearth, int soulType)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if(player != null)
			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap ->
				{
					cap.diedWithSoulHearth = diedWithSoulHearth;
					cap.soulType = soulType;
				});
	}
}
