package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.server.capability.SoulType;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;

public class SoulDeathScreen
{
	public static boolean hadSoulHearth = false;
	public static SoulType soulType = SoulType.DETERMINATION;

	public static void render(GuiGraphics graphics, DeathScreen screen, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		int width = screen.width;
		int height = screen.height;

		if(Minecraft.getInstance().player == null)
			return;
		//Mid-death, right after finishing the death animation the player capability stops existing
		//I had to store the data in static fields because of this, preferably do not use that for animations.
		Minecraft.getInstance().player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap ->
			{
				hadSoulHearth = cap.diedWithSoulHearth;
				soulType = SoulType.byId(cap.soulType);
			});

		poseStack.pushPose();
		poseStack.translate(width/2f, height/2f, 0);

		if(hadSoulHearth)
		{
			graphics.drawString(Minecraft.getInstance().font, "TESTING", 0, 0, -1);
		}

		poseStack.popPose();
	}
}
