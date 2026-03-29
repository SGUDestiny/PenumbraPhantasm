package destiny.penumbra_phantasm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.overlay.FountainDarknessOverlay;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void renderDarknessOverlays(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.isPaused())
			return;

		float landAlpha = 0f;
		float fountainAlpha = 0f;
		int sealShineTick = -1;

		if (minecraft.player != null) {
			int darknessLandTicker = minecraft.player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).resolve().map(c -> c.darknessLandTicker).orElse(-1);
			if (darknessLandTicker >= 0 && darknessLandTicker < 40) {
				landAlpha = darknessLandTicker < 20 ? 1f : Mth.lerp(darknessLandTicker / 40f, 1f, 0f);
			}

			int darknessOverlayTicker = minecraft.player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).resolve().map(c -> c.darknessOverlayTicker).orElse(0);
			if (darknessOverlayTicker > 0) {
				fountainAlpha = Math.min(Mth.lerp(darknessOverlayTicker / 100f, 0f, 3f), 2.5f);
			}

			sealShineTick = minecraft.player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).resolve().map(c -> c.sealShineTicker).orElse(-1);
		}

		if (landAlpha == 0f && fountainAlpha == 0f) return;

		int width = minecraft.getWindow().getGuiScaledWidth();
		int height = minecraft.getWindow().getGuiScaledHeight();

		Matrix4f ortho = new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 21000.0F);
		RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
		PoseStack pose = RenderSystem.getModelViewStack();
		pose.pushPose();
		pose.setIdentity();
		pose.translate(0.0, 0.0, -11000.0);
		RenderSystem.applyModelViewMatrix();

		GuiGraphics graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		renderLandScreenFadeOut(graphics, width, height, landAlpha);
		renderTransitionFadeOut(graphics, width, height, fountainAlpha);
		if (sealShineTick >= 0) {
			renderSealShine(pose, width, height, sealShineTick);
		}

		graphics.flush();

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();

		pose.popPose();
		RenderSystem.applyModelViewMatrix();
	}

	@Unique
	private void renderLandScreenFadeOut(GuiGraphics graphics, int width, int height, float landAlpha) {
		if (landAlpha > 0f) {
			graphics.fill(0, 0, width, height, (int)(landAlpha * 255) << 24);
		}
	}

	@Unique
	private void renderTransitionFadeOut(GuiGraphics graphics, int width, int height, float fountainAlpha) {
		if (fountainAlpha > 0f) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1f, 1f, 1f, fountainAlpha);
			graphics.blit(FountainDarknessOverlay.DARKNESS, 0, 0, 0, 0.0F, 0.0F, width, height, width, height);
		}
	}

	@Unique
	private void renderSealShine(PoseStack pose, int width, int height, int tick) {
		ResourceLocation WHITE_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/white_screen.png");

		float endingSizeX1;
		float endingSizeX2;
		float endingSizeX3;

		float endingAlpha1;
		float endingAlpha2;
		float endingAlpha3;
		float endingStart = 0;
		float endingDuration = 60;
		float endingDelta = (tick - endingStart) / endingDuration;
		if (tick < endingStart + endingDuration) {
			endingSizeX1 = Mth.lerp(endingDelta, 0, 1);
			endingSizeX2 = Mth.lerp(endingDelta, 0, 2);
			endingSizeX3 = Mth.lerp(endingDelta, 0, 3);

			endingAlpha1 = Mth.lerp(endingDelta, 0.075f, 1);
			endingAlpha2 = Mth.lerp(endingDelta, 0.05f, 1);
			endingAlpha3 = Mth.lerp(endingDelta, 0.025f, 1);
		} else {
			endingSizeX1 = 1;
			endingSizeX2 = 2;
			endingSizeX3 = 3;

			endingAlpha1 = 1f;
			endingAlpha2 = 1f;
			endingAlpha3 = 1f;
		}

		pose.pushPose();
		pose.translate(width / 2f, height / 2f, 0);
		pose.scale(endingSizeX3, 1, 1);
		pose.translate(-width / 2f, -height / 2f, 0);
		RenderBlitUtil.blit(WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha3, 0.0F, 0.0F, width, height, width, height);
		pose.popPose();

		pose.pushPose();
		pose.translate(width / 2f, height / 2f, 0);
		pose.scale(endingSizeX2, 1, 1);
		pose.translate(-width / 2f, -height / 2f, 0);
		RenderBlitUtil.blit(WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha2, 0.0F, 0.0F, width, height, width, height);
		pose.popPose();

		pose.pushPose();
		pose.translate(width / 2f, height / 2f, 0);
		pose.scale(endingSizeX1, 1, 1);
		pose.translate(-width / 2f, -height / 2f, 0);
		RenderBlitUtil.blit(WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha1, 0.0F, 0.0F, width, height, width, height);
		pose.popPose();
	}
}
