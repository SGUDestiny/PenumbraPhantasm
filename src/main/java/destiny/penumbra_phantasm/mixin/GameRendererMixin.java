package destiny.penumbra_phantasm.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.ClientConfig;
import destiny.penumbra_phantasm.client.render.ModShaders;
import destiny.penumbra_phantasm.client.render.fluid.NegativePhotonsRenderUtil;
import destiny.penumbra_phantasm.client.render.fountain.FountainHueShiftRenderer;
import destiny.penumbra_phantasm.client.render.fountain.FountainOpeningPosterizeRenderer;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.overlay.FountainDarknessOverlay;
import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.client.render.textbox.DarkWorldDialogue;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxWriter;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.FluidTypeRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static destiny.penumbra_phantasm.client.render.overlay.TextBoxOverlay.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Unique
	private static final ResourceLocation SWOON = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/misc/swoon.png");

	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void penumbraPhantasm$hideEggRoomHands(PoseStack poseStack, Camera camera, float partialTick, CallbackInfo ci) {
		if (Minecraft.getInstance().level != null && CardKingdomEggRoomUtil.isEggRoom(Minecraft.getInstance().level)) {
			ci.cancel();
		}
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void penumbraPhantasm$skipEggRoomBobView(PoseStack poseStack, float partialTick, CallbackInfo ci) {
		if (Minecraft.getInstance().level != null && CardKingdomEggRoomUtil.isEggRoom(Minecraft.getInstance().level)) {
			ci.cancel();
		}
	}

	@Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
	private void penumbraPhantasm$skipEggRoomBobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
		if (Minecraft.getInstance().level != null && CardKingdomEggRoomUtil.isEggRoom(Minecraft.getInstance().level)) {
			ci.cancel();
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void renderDarknessOverlays(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.isPaused()) return;

		ClientLevel level = minecraft.level;

		if (level == null) return;

		LocalPlayer player = minecraft.player;

		if (player == null) return;

		if (ClientConfig.fountainMakingPosterization) {
			FountainOpeningPosterizeRenderer.render(minecraft, (GameRenderer) (Object) this, partialTick);
		}

		if (ClientConfig.fountainProximityRainbow) {
			FountainHueShiftRenderer.render(minecraft, (GameRenderer) (Object) this, partialTick);
		}

		ScreenAnimationCapability screenCap = minecraft.player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).resolve().orElse(null);
		if (screenCap == null) return;

		int darknessLandTicker = screenCap.darknessLandTicker;

		float landAlpha = 0f;
		if (darknessLandTicker >= 0 && darknessLandTicker < 40) {
			landAlpha = darknessLandTicker < 20 ? 1f : Mth.lerp(darknessLandTicker / 40f, 1f, 0f);
		}

		int darknessOverlayTicker = screenCap.darknessOverlayTicker;

		float transitionAlpha = 0f;
		if (darknessOverlayTicker > 0) {
			transitionAlpha = Math.min(Mth.lerp(darknessOverlayTicker / 100f, 0f, 3f), 2.5f);
		}

		SoulCapability soulCap = minecraft.player.getCapability(CapabilityRegistry.SOUL).resolve().orElse(null);
		if (soulCap == null) return;

		int determination = soulCap.determination;

		float petrificationAlpha = 0f;
		if (DarkWorldUtil.isDepths(minecraft.level) && determination <= 25) {
			float erosionDelta = (25 - determination) / 25f;

			petrificationAlpha = Math.min(Mth.lerp(erosionDelta, 0f, 1f), 2.5f);
		}

		transitionAlpha = Math.max(transitionAlpha, petrificationAlpha);

		int sealShineTick = screenCap.sealShineTicker;
		int swoonTicker = screenCap.swoonAnimationTicker;



		minecraft.getMainRenderTarget().bindWrite(false);
		RenderSystem.disableDepthTest();

		Window window = minecraft.getWindow();
		float guiFarPlane = ForgeHooksClient.getGuiFarPlane();

		Matrix4f guiProjection = new Matrix4f().setOrtho(0, (float) ((double) window.getWidth() / window.getGuiScale()), (float) ((double) window.getHeight() / window.getGuiScale()), 0f, 1000f, guiFarPlane);
		RenderSystem.setProjectionMatrix(guiProjection, VertexSorting.ORTHOGRAPHIC_Z);

		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushPose();
		modelViewStack.setIdentity();
		modelViewStack.translate(0, 0, 1000 - guiFarPlane);
		RenderSystem.applyModelViewMatrix();

		GuiGraphics graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

		int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = minecraft.getWindow().getGuiScaledHeight();

		renderNegativePhotonsOverlay(minecraft, level, player, scaledWidth, scaledHeight);

		renderTextBox(graphics, minecraft, level, player, scaledWidth, scaledHeight);

		renderLandScreenFadeOut(graphics, scaledWidth, scaledHeight, landAlpha);
		renderTransitionFadeOut(graphics, scaledWidth, scaledHeight, transitionAlpha);
		renderSealShine(graphics, scaledWidth, scaledHeight, sealShineTick);
		renderSwoon(graphics, scaledWidth, scaledHeight, swoonTicker);

		graphics.flush();

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
		modelViewStack.popPose();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.enableDepthTest();
	}

	@Unique
	private void renderLandScreenFadeOut(GuiGraphics graphics, int width, int height, float landAlpha) {
		if (landAlpha > 0f) {
			graphics.fill(0, 0, width, height, (int)(landAlpha * 255) << 24);
		}
	}

	@Unique
	private void renderSwoon(GuiGraphics graphics, int width, int height, int swoonTicker) {
		if (swoonTicker == -1) return;

		graphics.fill(0, 0, width, height, FastColor.ARGB32.color(255, 0, 0, 0));

		PoseStack pose = graphics.pose();
		int spriteWidth = 262 * 2;
		int spriteHeight = 18 * 2;

		pose.pushPose();

		pose.translate((width - spriteWidth) / 2f, (height - spriteHeight) / 2f, 0);

		graphics.blit(SWOON, 0, 0, 0, 0, 0, spriteWidth, spriteHeight, spriteWidth, spriteHeight);

		pose.popPose();
	}

	@Unique
	private void renderTransitionFadeOut(GuiGraphics graphics, int width, int height, float fountainAlpha) {
		if (fountainAlpha > 0f) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1f, 1f, 1f, fountainAlpha);

			graphics.blit(FountainDarknessOverlay.DARKNESS, 0, 0, 0, 0, 0, width, height, width, height);
		}
	}

	@Unique
	private void renderSealShine(GuiGraphics graphics, int width, int height, int tick) {
		PoseStack pose = graphics.pose();

		if (tick < 0) {
			return;
		}

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

		RenderBlitUtil.blit(IntroScreen.WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha3, 0, 0, width, height, width, height);

		pose.popPose();

		pose.pushPose();

		pose.translate(width / 2f, height / 2f, 0);
		pose.scale(endingSizeX2, 1, 1);

		pose.translate(-width / 2f, -height / 2f, 0);
		RenderBlitUtil.blit(IntroScreen.WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha2, 0, 0, width, height, width, height);

		pose.popPose();

		pose.pushPose();

		pose.translate(width / 2f, height / 2f, 0);
		pose.scale(endingSizeX1, 1, 1);
		pose.translate(-width / 2f, -height / 2f, 0);

		RenderBlitUtil.blit(IntroScreen.WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha1, 0, 0, width, height, width, height);

		pose.popPose();
	}

	@Unique
	private void renderNegativePhotonsOverlay(Minecraft minecraft, ClientLevel level, LocalPlayer player, int width, int height) {
		if (player.getEyeInFluidType() != FluidTypeRegistry.NEGATIVE_PHOTONS.get()) return;

		ShaderInstance shaderInstance = ModShaders.FOUNTAIN_MASKED;

		if (shaderInstance == null) return;

		float shaderTime = (level.getGameTime()) * 0.01f;
		shaderInstance.safeGetUniform("Time").set(shaderTime);

		float aspectRatio = (float) minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight();

		shaderInstance.safeGetUniform("AspectRatio").set(aspectRatio);
		shaderInstance.safeGetUniform("TintColor").set(1f, 1f, 1f, 1f);

		RenderSystem.setShader(() -> ModShaders.FOUNTAIN_MASKED);
		RenderSystem.setShaderTexture(0, NegativePhotonsRenderUtil.WHITE_SCREEN);
		RenderSystem.setShaderTexture(1, NegativePhotonsRenderUtil.IMAGE_DEPTH);

		PoseStack pose = new PoseStack();
		Matrix4f matrix = pose.last().pose();

		BufferBuilder builder = Tesselator.getInstance().getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

		builder.vertex(matrix, 0f, 0f, 0f).color(0.1f, 0.1f, 0.1f, 0.5f).uv(0f, 0f).endVertex();
		builder.vertex(matrix, 0f, (float) height, 0f).color(0.1f, 0.1f, 0.1f, 0.5f).uv(0f, 1f).endVertex();
		builder.vertex(matrix, (float) width, (float) height, 0f).color(0.1f, 0.1f, 0.1f, 0.5f).uv(1f, 1f).endVertex();
		builder.vertex(matrix, (float) width, 0f, 0f).color(0.1f, 0.1f, 0.1f, 0.5f).uv(1f, 0f).endVertex();

		BufferUploader.drawWithShader(builder.end());
	}

	@Unique
	private void renderTextBox(GuiGraphics guiGraphics, Minecraft minecraft, ClientLevel level, LocalPlayer player, int width, int height) {
		if (!DarkWorldUtil.isDarkWorld(level)) return;

		if (!DarkWorldDialogue.isActive() || DarkWorldDialogue.writer() == null) return;

		PoseStack pose = guiGraphics.pose();
		TextBoxWriter writer = DarkWorldDialogue.writer();

		pose.pushPose();

		pose.translate((width - BOX_WIDTH) / 2f, height - BOX_HEIGHT, 0f);

		guiGraphics.blit(TEXTURE, 0, 0, 0, 0, BOX_WIDTH, BOX_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

		float glowDelta = (float) (Util.getMillis() % GLOW_PERIOD_MS) / GLOW_PERIOD_MS;
		float jewelAlpha = 1f - Mth.sin(glowDelta * Mth.PI);

		if (DarkWorldUtil.isDepths(player.level())) {
			jewelAlpha = 0;
		}

		RenderBlitUtil.blit(TEXTURE_GLOW, pose, 0, 0, 1f, 1f, 1f, jewelAlpha, 0, 0, BOX_WIDTH, BOX_HEIGHT,
				TEXTURE_SIZE, TEXTURE_SIZE);

		Font font = minecraft.font;
		List<String> lines = writer.visibleLines();
		for (int i = 0; i < lines.size(); i++) {
			drawGridLine(guiGraphics, font, lines.get(i), TEXT_ORIGIN_X, TEXT_ORIGIN_Y + i * VERTICAL_SPACE);
		}

		if (writer.isChoosing()) {
			drawChoices(guiGraphics, font, writer);
		}

		pose.popPose();
	}
}