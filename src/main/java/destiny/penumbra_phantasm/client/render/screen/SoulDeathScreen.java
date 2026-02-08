package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.server.capability.SoulType;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.resources.ResourceLocation;

public class SoulDeathScreen {
	public static boolean hadSoulHearth = false;
	public static int soulType = 1;
	public static long tickStart = 0;
	public static int fragment = 1;

	public static void render(GuiGraphics graphics, DeathScreen screen, float partialTick) {
		PoseStack pose = graphics.pose();
		int width = screen.width;
		int height = screen.height;

		if(Minecraft.getInstance().player == null || Minecraft.getInstance().level == null)
			return;

		Minecraft minecraft = Minecraft.getInstance();
		long currentTimeTicks = System.currentTimeMillis() / 50;

		//Mid-death, right after finishing the death animation the player capability stops existing
		//I had to store the data in static fields because of this, preferably do not use that for animations.
		minecraft.player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> {
			hadSoulHearth = cap.diedWithSoulHearth;
			soulType = cap.soulType;
			tickStart = System.currentTimeMillis() / 50;
		});

		if (!hadSoulHearth) return;

		long tick = currentTimeTicks - tickStart;
		if (tick % 10 == 0) {
			if (fragment < 4) {
				fragment++;
			} else {
				fragment = 1;
			}
		}

		System.out.println(currentTimeTicks);

		ResourceLocation SOUL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");
		ResourceLocation SOUL_BROKEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");
		ResourceLocation SOUL_FRAGMENT = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + "_fragment_" + fragment + ".png");

		pose.pushPose();
		pose.translate(width / 2f, height / 2f, 0);
		pose.scale(1.5f, 1.5f, 1f);

		if (tick < 20) {
			pose.translate(-15f / 2f, -15f / 2f, 0);
			RenderBlitUtil.blit(SOUL, pose, 0, 0, 1, 1, 1, 1, 15, 15, 15, 15);
		} else if (tick < 60) {
			if (tick == 20) {
				minecraft.player.playSound(SoundRegistry.SOUL_BREAK.get(), 0.5f, 1f);
			}

			pose.translate(-20f / 2f, -16f / 2f, 0);
			RenderBlitUtil.blit(SOUL_BROKEN, pose, 0, 0, 1, 1, 1, 1, 20, 16, 20, 16);
		} else if (tick < 120) {
			if (tick == 60) {
				minecraft.player.playSound(SoundRegistry.SOUL_SHATTER.get(), 0.5f, 1f);
			}
		}

		pose.popPose();
	}
}
