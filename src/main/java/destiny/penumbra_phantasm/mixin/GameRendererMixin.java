package destiny.penumbra_phantasm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import destiny.penumbra_phantasm.client.render.overlay.FountainDarknessOverlay;
import destiny.penumbra_phantasm.server.network.ClientboundPacketHandler;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	@Inject(method = "render", at = @At("TAIL"))
	private void renderDarknessOverlays(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci)
	{
		Minecraft mc = Minecraft.getInstance();

		float landAlpha = 0f;
		float fountainAlpha = 0f;

		if (ClientboundPacketHandler.fountainTransitioning) {
			boolean isLoadingScreen = mc.screen instanceof ReceivingLevelScreen ||
									  mc.screen instanceof ProgressScreen ||
									  mc.screen instanceof GenericDirtMessageScreen;
			boolean tickerSynced = mc.player != null && mc.player.getCapability(CapabilityRegistry.SCREEN_ANIMATION)
					.resolve().map(c -> c.darknessLandTicker >= 0).orElse(false);
			if (isLoadingScreen || !tickerSynced) {
				landAlpha = 1f;
			}
		}

		if (landAlpha == 0f && mc.player != null) {
			int ticker = mc.player.getCapability(CapabilityRegistry.SCREEN_ANIMATION)
					.resolve().map(c -> c.darknessLandTicker).orElse(-1);
			if (ticker >= 0 && ticker < 40) {
				landAlpha = ticker < 20 ? 1f : Mth.lerp(ticker / 40f, 1f, 0f);
			}
		}

		if (mc.player != null) {
			int ticker = mc.player.getCapability(CapabilityRegistry.SCREEN_ANIMATION)
					.resolve().map(c -> c.darknessOverlayTicker).orElse(0);
			if (ticker > 0) {
				fountainAlpha = Math.min(Mth.lerp(ticker / 100f, 0f, 3f), 1f);
			}
		}

		if (landAlpha == 0f && fountainAlpha == 0f) return;

		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();

		Matrix4f ortho = new Matrix4f().setOrtho(0.0F, w, h, 0.0F, 1000.0F, 21000.0F);
		RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
		PoseStack modelView = RenderSystem.getModelViewStack();
		modelView.pushPose();
		modelView.setIdentity();
		modelView.translate(0.0, 0.0, -11000.0);
		RenderSystem.applyModelViewMatrix();

		GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		if (fountainAlpha > 0f) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1f, 1f, 1f, fountainAlpha);
			graphics.blit(FountainDarknessOverlay.DARKNESS, 0, 0, 0, 0.0F, 0.0F, w, h, w, h);
		}

		if (landAlpha > 0f) {
			graphics.fill(0, 0, w, h, (int)(landAlpha * 255) << 24);
		}

		graphics.flush();

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();

		modelView.popPose();
		RenderSystem.applyModelViewMatrix();
	}
}
