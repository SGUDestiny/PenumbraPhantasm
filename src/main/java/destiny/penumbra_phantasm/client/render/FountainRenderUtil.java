package destiny.penumbra_phantasm.client.render;

import java.awt.Color;
import java.util.List;

import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.ModShaders;
import destiny.penumbra_phantasm.client.render.model.DarkFountainGroundCrackModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainMiddleModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainOpeningModel;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


public class FountainRenderUtil
{
	public static float fountainHueAlpha = 0F;

	private static final float COS45 = 0.70710678f;

	private static DarkFountainGroundCrackModel cachedCrackModel;
	private static DarkFountainOpeningModel cachedOpeningModel;
	private static DarkFountainMiddleModel cachedMiddleModel;

	private static DarkFountainGroundCrackModel getCrackModel() {
		if (cachedCrackModel == null)
			cachedCrackModel = new DarkFountainGroundCrackModel(Minecraft.getInstance().getEntityModels().bakeLayer(DarkFountainGroundCrackModel.LAYER_LOCATION));
		return cachedCrackModel;
	}

	private static DarkFountainOpeningModel getOpeningModel() {
		if (cachedOpeningModel == null)
			cachedOpeningModel = new DarkFountainOpeningModel(Minecraft.getInstance().getEntityModels().bakeLayer(DarkFountainOpeningModel.LAYER_LOCATION));
		return cachedOpeningModel;
	}

	private static DarkFountainMiddleModel getMiddleModel() {
		if (cachedMiddleModel == null)
			cachedMiddleModel = new DarkFountainMiddleModel(Minecraft.getInstance().getEntityModels().bakeLayer(DarkFountainMiddleModel.LAYER_LOCATION));
		return cachedMiddleModel;
	}

	private static void renderFountainCross(PoseStack poseStack, VertexConsumer consumer,
											float r, float g, float b, float a,
											float halfWidthPixels, float tileHeightPixels, int tileCount, float deformation) {
		Matrix4f matrix = poseStack.last().pose();
		float hw = (halfWidthPixels + deformation) / 16f;
		float tileHeight = tileHeightPixels / 16f;
		float off = deformation / 16f;

		float[][] planes = {
				{ hw * COS45, -hw * COS45,  COS45, COS45 },
				{ hw * COS45,  hw * COS45, -COS45, COS45 }
		};

		for (float[] plane : planes) {
			float x1 = plane[0], z1 = plane[1];
			float x2 = -plane[0], z2 = -plane[1];
			float nx = plane[2] * off, nz = plane[3] * off;

			for (int tile = 0; tile < tileCount; tile++) {
				float yBot = tile * tileHeight;
				float yTop = (tile + 1) * tileHeight;

				consumer.vertex(matrix, x1 + nx, yTop, z1 + nz).color(r, g, b, a).uv(0f, 0f).endVertex();
				consumer.vertex(matrix, x2 + nx, yTop, z2 + nz).color(r, g, b, a).uv(0.5f, 0f).endVertex();
				consumer.vertex(matrix, x2 + nx, yBot, z2 + nz).color(r, g, b, a).uv(0.5f, 1f).endVertex();
				consumer.vertex(matrix, x1 + nx, yBot, z1 + nz).color(r, g, b, a).uv(0f, 1f).endVertex();

				consumer.vertex(matrix, x2 - nx, yTop, z2 - nz).color(r, g, b, a).uv(0f, 0f).endVertex();
				consumer.vertex(matrix, x1 - nx, yTop, z1 - nz).color(r, g, b, a).uv(0.5f, 0f).endVertex();
				consumer.vertex(matrix, x1 - nx, yBot, z1 - nz).color(r, g, b, a).uv(0.5f, 1f).endVertex();
				consumer.vertex(matrix, x2 - nx, yBot, z2 - nz).color(r, g, b, a).uv(0f, 1f).endVertex();
			}
		}
	}

	public static void renderOpeningFoutain(float animationTime, int length, ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		ResourceLocation textureFountainOpeningBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom.png");
		ResourceLocation textureFountainOpeningMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle.png");
		ResourceLocation textureFountainOpeningBottomShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom_shadow.png");
		ResourceLocation textureFountainOpeningMiddleShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle_shadow.png");

		float expandTime = 5f;
		float pulsateTime = 120f;
		float shrinkTime = 5;
		float pulseAmp = 0.04f;
		float pulseFreq = 2.0f;

		float baseScale = 1f;
		boolean applyPulse = false;

		if (animationTime < expandTime) {
			baseScale = animationTime / expandTime;
		} else if (animationTime < expandTime + pulsateTime) {
			baseScale = 1f;
			applyPulse = true;
		} else {
			float shrinkProg = (animationTime - (expandTime + pulsateTime)) / shrinkTime;
			baseScale = 1f - shrinkProg;
		}

		float pulse = applyPulse ? (1f + pulseAmp * (float) Math.sin(animationTime * pulseFreq)) : 1f;
		float scaleXZ = baseScale * pulse;

		float fadeStart = 70;
		float fadeDuration = 20f;
		float alphaDark = 0f;
		if (animationTime >= fadeStart) {
			if (animationTime < fadeStart + fadeDuration) {
				float prog = (animationTime - fadeStart) / fadeDuration;
				alphaDark = prog;
			} else {
				alphaDark = 1f;
			}
		}

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		getCrackModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, 3f, 0f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		getOpeningModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningBottom)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		getOpeningModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningBottomShadow)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alphaDark);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 5.0f, 0.5f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningMiddle)),
				1F, 1F, 1F, 1F, 32f, 80f, length, 0f);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningMiddleShadow)),
				1F, 1F, 1F, alphaDark, 32f, 80f, length, 0f);
		poseStack.popPose();
	}

	public static void renderShockwaves(DarkFountain fountain, PoseStack pose, MultiBufferSource buffer, int overlay, float partialTick) {
		ResourceLocation textureFountainTarget = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_shockwave.png");
		List<Integer> shockwaveTickers = fountain.shockwaveTickers;

		for (float ticker : shockwaveTickers) {
			ticker = ticker + partialTick;
			float shockwaveDelta = Mth.clamp(ticker / 5, 0f, 1f);
			float alpha = Mth.lerp(shockwaveDelta, 1f, 0f);
			float size = Mth.lerp(shockwaveDelta, 0f, 3f);
			float y = Mth.lerp(shockwaveDelta, -1.90f, -1.94f);

			pose.pushPose();
			pose.translate(0.5f, 0.5f, 0.5f);
			pose.translate(0f, y, 0f);
			pose.scale(size, 1f, size);
			getCrackModel().renderToBuffer(pose, buffer.getBuffer(RenderTypes.fountainShockwave(textureFountainTarget)),
					LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alpha);
			pose.popPose();
		}
	}

	public static void renderOpenFountain(DarkFountain fountain, Level level, float initialAnimationTime, int length, ResourceLocation textureCrack, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int overlay, float alpha) {
		int frame = fountain.getFrame();

		ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_" + frame + ".png");
		ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/fountain_middle_" + frame + ".png");
		ResourceLocation textureMiddleEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle_edges/fountain_middle_edges_" + frame + ".png");
		ResourceLocation textureBottomEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_edges.png");
		ResourceLocation imageDepth = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth.png");
		float pixel = 1f / 16f;
		float time = (level.getGameTime() + partialTick) * 0.1f;
		float pulse = (1.0f + 0.08f * (float) Math.sin(time * 1.2)) / 0.92f;
		float pulse_front = (1.0f - 0.08f * (float) Math.sin(time * 1.2)) / 1.08f;
		float brightness_front = 0.9f - 0.1f * (float) Math.sin(time * 1.2);
		float brightness_middle = 0.8f - 0.2f * (float) Math.sin(time * 1.2);
		float brightness_back = 0.7f - 0.3f * (float) Math.sin(time * 1.2);
		float scaleXZ = 1.0f;
		float animationTime = fountain.animationTimer + partialTick;
		float fountainHue = time * 0.03f % 1f;

		Player player = Minecraft.getInstance().player;

		Color frontColor = Color.getHSBColor(fountainHue, 0.8f, brightness_front);
		Color middleColor = Color.getHSBColor(fountainHue, 0.8f, 1f);
		Color middleEdgesColor = Color.getHSBColor(fountainHue, 0.8f, brightness_middle);
		Color backColor = Color.getHSBColor(fountainHue, 0.8f, brightness_back);
		ShaderInstance shaderInstance = ModShaders.FOUNTAIN_MASKED;
		if (shaderInstance != null) {
			float shadertime = (level.getGameTime() + partialTick) * 0.05f;
			shaderInstance.safeGetUniform("Time").set(shadertime);
			Minecraft mc = Minecraft.getInstance();
			float aspect = (float) mc.getWindow().getWidth() /
					(float) mc.getWindow().getHeight();

			shaderInstance.safeGetUniform("AspectRatio").set(aspect);

		}
		if(player != null)
		{
			double playerX = player.getX();
			double playerY = player.getY();
			double playerZ = player.getZ();

			double fountainX = fountain.getFountainPos().getX();
			double fountainY = fountain.getFountainPos().getY();
			double fountainZ = fountain.getFountainPos().getZ();

			Vec2 flatPlayerPos = new Vec2((float)playerX, (float) playerZ);
			Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);

			float distanceSquared = flatPlayerPos.distanceToSqr(flatFountainPos);
			if(playerY < fountainY)
				distanceSquared = (float) player.position().distanceToSqr(Vec3.atLowerCornerOf(fountain.getFountainPos()));

			float distanceInBlocks = (float) Math.sqrt(distanceSquared);
			float fadeStartDistance = 24f;
			float fadeEndDistance = 16f;
			float fadeRange = fadeStartDistance - fadeEndDistance;
			float tintDelta = (fadeStartDistance - distanceInBlocks) / fadeRange;
			tintDelta = Math.max(0f, Math.min(1f, tintDelta));

			float middleRed = middleColor.getRed() / 255f;
			float middleGreen = middleColor.getGreen() / 255f;
			float middleBlue = middleColor.getBlue() / 255f;

			float tintRed = 1f + (middleRed - 1f) * tintDelta;
			float tintGreen = 1f + (middleGreen - 1f) * tintDelta;
			float tintBlue = 1f + (middleBlue - 1f) * tintDelta;

			RenderSystem.setShaderColor(tintRed, tintGreen, tintBlue, 1F);
			if (shaderInstance != null) {
				shaderInstance.safeGetUniform("TintColor").set(
						tintRed,
						tintGreen,
						tintBlue,
						alpha
				);
			}


		}

		if (initialAnimationTime != -1) {
			scaleXZ = (animationTime - 140) / 5f;
		}

		boolean depthWrite = alpha >= 0.85f;

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		getCrackModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, 7 - (4 * pixel), 0f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		VertexConsumer bottomVertexConsumer = buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureBottom, imageDepth, depthWrite));
		getMiddleModel().renderToBuffer(poseStack, bottomVertexConsumer,
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, alpha);
		getMiddleModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomEdges)),
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.75f, 0.5f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
				middleEdgesColor.getRed() / 255f, middleEdgesColor.getGreen() / 255f, middleEdgesColor.getGreen() / 255f, alpha,
				48f, 140f, length, 0.1f);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
				middleEdgesColor.getRed() / 255f, middleEdgesColor.getGreen() / 255f, middleEdgesColor.getGreen() / 255f, alpha,
				48f, 140f, length, 0.1f);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.75f, 0.5f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		poseStack.scale(pulse, 1.0f, pulse);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
				backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getGreen() / 255f, alpha,
				48f, 140f, length, 0.0f);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
				backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getGreen() / 255f, alpha,
				48f, 140f, length, 0.0f);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.75f, 0.5f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		poseStack.scale(pulse_front, 1.0f, pulse_front);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
				frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, alpha,
				48f, 140f, length, 0.2f);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
				frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, alpha,
				48f, 140f, length, 0.2f);
		poseStack.popPose();
	}

	public static void renderOpenFountainOptimized(DarkFountain fountain, int length, PoseStack poseStack, MultiBufferSource buffer, int overlay, float alpha) {
		int frameOptimized = fountain.frameOptimized;
		ResourceLocation textureMiddleOptimized = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/optimized/fountain_middle_" + frameOptimized + ".png");

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.75f, 0.5f);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleOptimized)),
				1F, 1F, 1F, alpha, 48f, 240f, length, 0.1f);
		poseStack.popPose();
	}

	public static void renderLightWorldOpenFountain(ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		getCrackModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		poseStack.popPose();
	}
}