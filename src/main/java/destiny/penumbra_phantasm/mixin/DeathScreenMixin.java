package destiny.penumbra_phantasm.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.server.capability.SoulType;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin
{
	@Unique
	private static boolean diedWithSoulHearth = false;

	@Unique
	private static int soulType = 1;

	@Inject(method = "render", at = @At("TAIL"))
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci)
	{
		PoseStack pose = graphics.pose();
		DeathScreen screen = (DeathScreen) (Object) this;

		int width = screen.width;
		int height = screen.height;
		ResourceLocation SOUL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");

		Minecraft.getInstance().player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> {
			diedWithSoulHearth = cap.diedWithSoulHearth;
			soulType = cap.soulType;
		});

		if(!diedWithSoulHearth)
			return;
		pose.pushPose();
		pose.translate(width/2f, height/2f, 0);

		graphics.drawString(Minecraft.getInstance().font, "TESTING + " + soulType, 0, 0, -1);

		RenderBlitUtil.blit(SOUL, pose, 0, -30, 1, 1, 1, 1, 0, 0, 15, 15, 15, 15);

		pose.popPose();
	}

	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo ci)
	{

	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void tick(CallbackInfo ci)
	{

	}
}
