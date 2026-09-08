package destiny.penumbra_phantasm.client.render.fountain;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.math.Axis;
import destiny.penumbra_phantasm.client.render.ModShaders;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.client.render.model.*;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static destiny.penumbra_phantasm.server.fountain.DarkFountain.*;


public class FountainRenderUtil {
	public static float fountainHueAlpha = 0F;

	public static final float OPENING_SHADOW_FADE_START = 70f;
	public static final float OPENING_SHADOW_FADE_DURATION = 20f;
	public static final float OPENING_SHADOW_DURATION_FULL = OPENING_SHADOW_FADE_START + OPENING_SHADOW_FADE_DURATION;
	public static final float OPENING_POSTERIZE_SHADOW_FADE_TAIL = 4f;
	public static final float OPENING_PULSE_FREQ = 2f;

	public static final int OPENING_POSTERIZE_STRENGTH_FADE_IN = 10;
	public static final int OPENING_POSTERIZE_TICK_END = 130;
	public static final float OPENING_POSTERIZE_FADE_START = 16f;
	public static final float OPENING_POSTERIZE_DISTANCE_FADE_END = 24f;

	public static final float OPENING_POSTERIZE_STRENGTH_MAX = 1f;
	public static final float OPENING_POSTERIZE_LUMA_THRESHOLD = 0.05f;
	public static final float OPENING_POSTERIZE_LUMA_THRESHOLD_BLOCK_DARK_MUL = 0.48f;
	public static final float OPENING_POSTERIZE_LUMA_THRESHOLD_BLOCK_BRIGHT_MUL = 11f;

	public static final float FOUNTAIN_SCREEN_TINT_FADE_START = 24f;
	public static final float FOUNTAIN_SCREEN_TINT_FADE_END = 16f;

	public static final float COS45 = 0.70710678f;

	public static final ResourceLocation DEPTHS_OPENING = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/depths/fountain_opening.png");
	public static final ResourceLocation DEPTHS_SWIRL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/depths/fountain_swirl_part.png");
	public static final ResourceLocation DEPTHS_SWIRL_SMALL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/depths/fountain_swirl_small_part.png");
	public static final ResourceLocation DEPTHS_VORTEX = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/depths/fountain_vortex.png");
	public static final ResourceLocation DEPTHS_VORTEX_LARGER = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/depths/fountain_vortex_larger.png");
	public static final float DEPTHS_OPENING_PIXELS = 128f;
	public static final float DEPTHS_SWIRL_PIXELS = 256f;
	public static final float DEPTHS_VORTEX_PIXELS = 512f;
	public static final float DEPTHS_LARGER_VORTEX_PIXELS = DEPTHS_VORTEX_PIXELS * 3;

	public static final float DEPTHS_SWIRL_SIZE = 2f;

	public static final float DEPTHS_ALPHA_MIN = 0.25f;
	public static final float DEPTHS_DIM_DISTANCE = 256;

	public static final float DEPTHS_SWIRL_CULL_DISTANCE = 128f;
	public static final float DEPTHS_SWIRL_FADE_START = 96f;

	public static final float DEPTHS_VORTEX_Y = 4f;
	public static final float DEPTHS_VORTEX_DEG_PER_TICK = 3f;
	public static final float DEPTHS_LARGER_VORTEX_DEG_PER_TICK = 1f;

	public static final float DEPTHS_BEAM_TOP_WIDTH = 2f;
	public static final float DEPTHS_BEAM_BOTTOM_WIDTH = 5f;
	public static final float DEPTHS_BEAM_LENGTH = 48f;

	public static final int DEPTHS_FADE_OUT_DURATION = SEAL_DURATION;
	public static final int DEPTHS_FADE_IN_START = SEAL_DURATION + SEAL_FLASH_DELAY;
	public static final int DEPTHS_FADE_IN_END = DEPTHS_FADE_IN_START + SEAL_FLASH_DURATION - (SEAL_FLASH_DURATION / 3);
	public static final int DEPTHS_FADE_OUT_FINAL_END = DEPTHS_FADE_IN_START + SEAL_FLASH_DURATION;

	public static float openingPosterizeLumaThresholdForCameraBlockLight(Level level, Vec3 cameraPos, float baseThreshold) {
		int blockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(cameraPos));
		float t = Mth.clamp(blockLight / 15f, 0f, 1f);
		float mul = Mth.lerp(OPENING_POSTERIZE_LUMA_THRESHOLD_BLOCK_DARK_MUL, OPENING_POSTERIZE_LUMA_THRESHOLD_BLOCK_BRIGHT_MUL, t);
		return Mth.clamp(baseThreshold * mul, 0.03f, 0.55f);
	}

	private static DarkFountainGroundCrackModel cachedCrackModel;
	private static DarkFountainOpeningModel cachedOpeningModel;
	private static DarkFountainMiddleModel cachedMiddleModel;
	private static DarkFountainVortexModel cachedVortexModel;
	private static final Map<Long, Float> sealedPulseMotionByFountain = new HashMap<>();
	private static final Map<Long, Float> sealedShaderTimeByFountain = new HashMap<>();

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

	private static DarkFountainVortexModel getVortexModel() {
		if (cachedVortexModel == null)
			cachedVortexModel = new DarkFountainVortexModel(Minecraft.getInstance().getEntityModels().bakeLayer(DarkFountainVortexModel.LAYER_LOCATION));
		return cachedVortexModel;
	}

	private static void renderFountainCross(PoseStack poseStack, VertexConsumer consumer,
											float r, float g, float b, float a,
											float halfWidthPixels, float tileHeightPixels, int tileCount, float deformation) {
		renderFountainCross(poseStack, consumer, r, g, b, a, halfWidthPixels, tileHeightPixels, tileCount, deformation, 0f);
	}

	private static void renderFountainCross(PoseStack poseStack, VertexConsumer consumer,
											float r, float g, float b, float a,
											float halfWidthPixels, float tileHeightPixels, int tileCount, float deformation, float vOffset) {
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
				float vTop = tile + vOffset;
				float vBot = tile + 1f + vOffset;

				consumer.vertex(matrix, x1 + nx, yTop, z1 + nz).color(r, g, b, a).uv(0f, vTop).endVertex();
				consumer.vertex(matrix, x2 + nx, yTop, z2 + nz).color(r, g, b, a).uv(0.5f, vTop).endVertex();
				consumer.vertex(matrix, x2 + nx, yBot, z2 + nz).color(r, g, b, a).uv(0.5f, vBot).endVertex();
				consumer.vertex(matrix, x1 + nx, yBot, z1 + nz).color(r, g, b, a).uv(0f, vBot).endVertex();

				consumer.vertex(matrix, x2 - nx, yTop, z2 - nz).color(r, g, b, a).uv(0f, vTop).endVertex();
				consumer.vertex(matrix, x1 - nx, yTop, z1 - nz).color(r, g, b, a).uv(0.5f, vTop).endVertex();
				consumer.vertex(matrix, x1 - nx, yBot, z1 - nz).color(r, g, b, a).uv(0.5f, vBot).endVertex();
				consumer.vertex(matrix, x2 - nx, yBot, z2 - nz).color(r, g, b, a).uv(0f, vBot).endVertex();
			}
		}
	}

	private static float getMiddleUvOffset(DarkFountain fountain, float partialTick) {
		if (fountain.sealingTick >= 0 && fountain.sealingFrameTick >= 0) {
			return Mth.frac((fountain.sealingFrameTick + fountain.sealingFrameTickProgress) / (27f * 3f));
		}
		return Mth.frac((fountain.getFrameTick() + partialTick) / (27f * 3f));
	}

	private static void renderFountainCrossSorted(PoseStack poseStack, VertexConsumer consumer,
												  float r, float g, float b, float a,
												  float halfWidthPixels, float tileHeightPixels, int tileCount, float deformation,
												  Vec3 cameraPos, BlockPos fountainPos) {
		Matrix4f matrix = poseStack.last().pose();
		float hw = (halfWidthPixels + deformation) / 16f;
		float tileHeight = tileHeightPixels / 16f;
		float off = deformation / 16f;

		float[][] faces = {
				{ hw * COS45, -hw * COS45,  COS45, COS45,  1f },
				{ hw * COS45, -hw * COS45,  COS45, COS45, -1f },
				{ hw * COS45,  hw * COS45, -COS45, COS45,  1f },
				{ hw * COS45,  hw * COS45, -COS45, COS45, -1f }
		};

		float fountainCenterX = fountainPos.getX() + 0.5f;
		float fountainCenterZ = fountainPos.getZ() + 0.5f;
		float vx = (float) (cameraPos.x - fountainCenterX);
		float vz = (float) (cameraPos.z - fountainCenterZ);
		float vLen = Mth.sqrt(vx * vx + vz * vz);
		if (vLen < 1.0e-5f) {
			vx = 0f;
			vz = 1f;
		} else {
			vx /= vLen;
			vz /= vLen;
		}

		float[] faceDepth = new float[4];
		int[] order = {0, 1, 2, 3};
		for (int i = 0; i < 4; i++) {
			float nx = faces[i][2] * off;
			float nz = faces[i][3] * off;
			float sign = faces[i][4];
			float cx = sign * nx;
			float cz = sign * nz;
			faceDepth[i] = cx * vx + cz * vz;
		}
		for (int i = 0; i < order.length - 1; i++) {
			for (int j = i + 1; j < order.length; j++) {
				if (faceDepth[order[i]] > faceDepth[order[j]]) {
					int tmp = order[i];
					order[i] = order[j];
					order[j] = tmp;
				}
			}
		}

		for (int idx = 0; idx < order.length; idx++) {
			float[] face = faces[order[idx]];
			float x1 = face[0], z1 = face[1];
			float x2 = -face[0], z2 = -face[1];
			float nx = face[2] * off;
			float nz = face[3] * off;
			float sign = face[4];
			float sx = sign * nx;
			float sz = sign * nz;
			float rankBias = (idx - 1.5f) * 0.0025f;
			float bx = vx * rankBias;
			float bz = vz * rankBias;

			for (int tile = 0; tile < tileCount; tile++) {
				float yBot = tile * tileHeight;
				float yTop = (tile + 1) * tileHeight;

				if (sign > 0f) {
					consumer.vertex(matrix, x1 + sx + bx, yTop, z1 + sz + bz).color(r, g, b, a).uv(0f, 0f).endVertex();
					consumer.vertex(matrix, x2 + sx + bx, yTop, z2 + sz + bz).color(r, g, b, a).uv(0.5f, 0f).endVertex();
					consumer.vertex(matrix, x2 + sx + bx, yBot, z2 + sz + bz).color(r, g, b, a).uv(0.5f, 1f).endVertex();
					consumer.vertex(matrix, x1 + sx + bx, yBot, z1 + sz + bz).color(r, g, b, a).uv(0f, 1f).endVertex();
				} else {
					consumer.vertex(matrix, x2 + sx + bx, yTop, z2 + sz + bz).color(r, g, b, a).uv(0f, 0f).endVertex();
					consumer.vertex(matrix, x1 + sx + bx, yTop, z1 + sz + bz).color(r, g, b, a).uv(0.5f, 0f).endVertex();
					consumer.vertex(matrix, x1 + sx + bx, yBot, z1 + sz + bz).color(r, g, b, a).uv(0.5f, 1f).endVertex();
					consumer.vertex(matrix, x2 + sx + bx, yBot, z2 + sz + bz).color(r, g, b, a).uv(0f, 1f).endVertex();
				}
			}
		}
	}

	public static void renderOpeningFountain(float openingTick, int length, ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		ResourceLocation textureFountainOpeningBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom.png");
		ResourceLocation textureFountainOpeningMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle.png");
		ResourceLocation textureFountainOpeningBottomShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom_shadow.png");
		ResourceLocation textureFountainOpeningMiddleShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle_shadow.png");

		float expandTime = 5f;
		float pulsateTime = 120f;
		float shrinkTime = 5;
		float pulseAmp = 0.04f;

		float baseScale = 1f;
		boolean applyPulse = false;

		if (openingTick < expandTime) {
			baseScale = openingTick / expandTime;
		} else if (openingTick < expandTime + pulsateTime) {
			baseScale = 1f;
			applyPulse = true;
		} else {
			float shrinkProg = (openingTick - (expandTime + pulsateTime)) / shrinkTime;
			baseScale = 1f - shrinkProg;
		}

		float pulse = applyPulse ? (1f + pulseAmp * (float) Math.sin(openingTick * OPENING_PULSE_FREQ)) : 1f;
		float scaleXZ = baseScale * pulse;

		float alphaDark = 0f;
		if (openingTick >= OPENING_SHADOW_FADE_START) {
			if (openingTick < OPENING_SHADOW_FADE_START + OPENING_SHADOW_FADE_DURATION) {
				float prog = (openingTick - OPENING_SHADOW_FADE_START) / OPENING_SHADOW_FADE_DURATION;
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

		for (int i = 0; i < shockwaveTickers.size(); i++) {
			float ticker = shockwaveTickers.get(i) + partialTick;
			float shockwaveDelta = Mth.clamp(ticker / 5, 0f, 1f);
			float alpha = Mth.lerp(shockwaveDelta, 1f, 0f);
			float size = Mth.lerp(shockwaveDelta, 0f, 2f);
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

	public static void renderOpenFountain(DarkFountain fountain, Level level, int length, ResourceLocation textureCrack, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int overlay, float alpha) {
		int frame = fountain.getFrame();
		float middleUvOffset = getMiddleUvOffset(fountain, partialTick);

		ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_0.png");
		ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/fountain_middle_0.png");
		ResourceLocation textureMiddleEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle_edges/fountain_middle_edges_0.png");
		ResourceLocation textureBottomEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_edges.png");
		ResourceLocation imageDepth = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth.png");
		float pixel = 1f / 16f;
		float time = (level.getGameTime() + partialTick) * 0.1f;
		float pulse = (1.0f + 0.08f * (float) Math.sin(time * 1.2)) / 0.92f;
		float pulse_front = (1.0f - 0.08f * (float) Math.sin(time * 1.2)) / 1.08f;
		float brightness_front = 0.9f - 0.1f * (float) Math.sin(time * 1.2);
		float brightness_middle = 0.8f - 0.2f * (float) Math.sin(time * 1.2);
		float brightness_back = 0.7f - 0.3f * (float) Math.sin(time * 1.2);
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
			float fadeRange = FOUNTAIN_SCREEN_TINT_FADE_START - FOUNTAIN_SCREEN_TINT_FADE_END;
			float tintDelta = (FOUNTAIN_SCREEN_TINT_FADE_START - distanceInBlocks) / fadeRange;
			tintDelta = Math.max(0f, Math.min(1f, tintDelta));

			float middleRed = middleColor.getRed() / 255f;
			float middleGreen = middleColor.getGreen() / 255f;
			float middleBlue = middleColor.getBlue() / 255f;

			float tintRed = 1f + (middleRed - 1f) * tintDelta;
			float tintGreen = 1f + (middleGreen - 1f) * tintDelta;
			float tintBlue = 1f + (middleBlue - 1f) * tintDelta;

			if (shaderInstance != null) {
				shaderInstance.safeGetUniform("TintColor").set(
						tintRed,
						tintGreen,
						tintBlue,
						alpha
				);
			}


		}

		boolean depthWrite = alpha >= 0.85f;

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		getCrackModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, 7 - (4 * pixel), 0f);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		VertexConsumer bottomVertexConsumer = buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureBottom, imageDepth, depthWrite));
		getMiddleModel().renderToBuffer(poseStack, bottomVertexConsumer,
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha);
		getMiddleModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomEdges)),
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.74f, 0.5f);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
				middleEdgesColor.getRed() / 255f, middleEdgesColor.getGreen() / 255f, middleEdgesColor.getBlue() / 255f, alpha,
				48f, 140f, length, 0.1f, middleUvOffset);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
				middleEdgesColor.getRed() / 255f, middleEdgesColor.getGreen() / 255f, middleEdgesColor.getBlue() / 255f, alpha,
				48f, 140f, length, 0.1f, middleUvOffset);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.74f, 0.5f);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		poseStack.scale(pulse, 1.0f, pulse);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
				backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getBlue() / 255f, alpha,
				48f, 140f, length, 0.0f, middleUvOffset);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
				backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getBlue() / 255f, alpha,
				48f, 140f, length, 0.0f, middleUvOffset);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.74f, 0.5f);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		poseStack.scale(pulse_front, 1.0f, pulse_front);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
				frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha,
				48f, 140f, length, 0.2f, middleUvOffset);
		renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
				frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha,
				48f, 140f, length, 0.2f, middleUvOffset);
		poseStack.popPose();
	}

	public static void renderSealingFountain(DarkFountain fountain, Level level, int length, ResourceLocation textureCrack, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int overlay, float alpha) {
		int frame = fountain.getFrame();
		float sealDelta = Mth.clamp((fountain.sealingTick + partialTick) / SEAL_DURATION, 0f, 1f);
		float middleUvOffset = getMiddleUvOffset(fountain, partialTick);

		ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_0.png");
		ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/fountain_middle_0.png");
		ResourceLocation textureMiddleEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle_edges/fountain_middle_edges_0.png");
		ResourceLocation textureBottomEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_edges.png");
		ResourceLocation imageDepth = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth.png");
		float pixel = 1f / 16f;
		float smoothGameTime = level.getGameTime() + partialTick;
		float time = smoothGameTime * 0.1f;
		float sealingTicks = fountain.sealingTick + partialTick;
		float clampedSealingTicks = Mth.clamp(sealingTicks, 0f, SEAL_DURATION);
		float basePulseSpeed = 1.2f;
		float phaseAtSealStart = (smoothGameTime - fountain.sealingTick) * 0.1f * basePulseSpeed;
		float decelPhase = basePulseSpeed * 0.1f * (clampedSealingTicks - (clampedSealingTicks * clampedSealingTicks) / (2f * SEAL_DURATION));
		float pulseMotion = (float) Math.sin(phaseAtSealStart + decelPhase);
		long fountainKey = fountain.getFountainPos().asLong();
		if (sealDelta >= 1f) {
			float currentPulseMotion = pulseMotion;
			pulseMotion = sealedPulseMotionByFountain.computeIfAbsent(fountainKey, k -> currentPulseMotion);
		} else {
			sealedPulseMotionByFountain.remove(fountainKey);
		}
		float pulse = (1.0f + 0.08f * pulseMotion) / 0.92f;
		float pulseFront = (1.0f - 0.08f * pulseMotion) / 1.08f;
		float brightnessFront = 0.9f - 0.1f * pulseMotion;
		float brightnessMiddle = 0.8f - 0.2f * pulseMotion;
		float brightnessBack = 0.7f - 0.3f * pulseMotion;
		float fountainHue = time * 0.03f % 1f;
		float fountainSat = 0.8f * (1f - sealDelta);

		Player player = Minecraft.getInstance().player;

		Color frontColor = Color.getHSBColor(fountainHue, fountainSat, brightnessFront);
		Color middleColor = Color.getHSBColor(fountainHue, fountainSat, 1f);
		Color middleEdgesColor = Color.getHSBColor(fountainHue, fountainSat, brightnessMiddle);
		Color backColor = Color.getHSBColor(fountainHue, fountainSat, brightnessBack);
		ShaderInstance shaderInstance = ModShaders.FOUNTAIN_MASKED;
		if (shaderInstance != null) {
			float baseShaderSpeed = 0.05f;
			float shadertime = (smoothGameTime - fountain.sealingTick) * baseShaderSpeed - baseShaderSpeed * (clampedSealingTicks * clampedSealingTicks) / (2f * SEAL_DURATION);
			if (sealDelta >= 1f) {
				float currentShaderTime = shadertime;
				shadertime = sealedShaderTimeByFountain.computeIfAbsent(fountainKey, k -> currentShaderTime);
			} else {
				sealedShaderTimeByFountain.remove(fountainKey);
			}
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
			float fadeRange = FOUNTAIN_SCREEN_TINT_FADE_START - FOUNTAIN_SCREEN_TINT_FADE_END;
			float tintDelta = (FOUNTAIN_SCREEN_TINT_FADE_START - distanceInBlocks) / fadeRange;
			tintDelta = Math.max(0f, Math.min(1f, tintDelta));

			float middleRed = middleColor.getRed() / 255f;
			float middleGreen = middleColor.getGreen() / 255f;
			float middleBlue = middleColor.getBlue() / 255f;

			float tintRed = 1f + (middleRed - 1f) * tintDelta;
			float tintGreen = 1f + (middleGreen - 1f) * tintDelta;
			float tintBlue = 1f + (middleBlue - 1f) * tintDelta;

			float screenTintRed = Mth.lerp(sealDelta, tintRed, 1f);
			float screenTintGreen = Mth.lerp(sealDelta, tintGreen, 1f);
			float screenTintBlue = Mth.lerp(sealDelta, tintBlue, 1f);

			float fountainTintRed = Mth.lerp(sealDelta, tintRed, 0f);
			float fountainTintGreen = Mth.lerp(sealDelta, tintGreen, 0f);
			float fountainTintBlue = Mth.lerp(sealDelta, tintBlue, 0f);

			RenderSystem.setShaderColor(screenTintRed, screenTintGreen, screenTintBlue, 1F);

			if (shaderInstance != null) {
				shaderInstance.safeGetUniform("TintColor").set(
						fountainTintRed,
						fountainTintGreen,
						fountainTintBlue,
						alpha
				);
			}


		}

		boolean depthWrite = alpha >= 0.85f;

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		getCrackModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha);
		poseStack.popPose();

		if (fountain.sealingTick < SEAL_DURATION + DarkFountain.SEAL_FLASH_DELAY) {
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, 7 - (4 * pixel), 0f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			VertexConsumer bottomVertexConsumer = buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureBottom, imageDepth, depthWrite));
			getMiddleModel().renderToBuffer(poseStack, bottomVertexConsumer,
					LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha);
			getMiddleModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomEdges)),
					LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha);
			poseStack.popPose();
		}

		if (fountain.sealingTick >= SEAL_DURATION + DarkFountain.SEAL_FLASH_DELAY) {
			ResourceLocation textureMiddleSealing = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/sealing/fountain_middle_0.png");
			ResourceLocation textureBottomSealing = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_sealing.png");
			float sealingFlashTick = fountain.sealingTick - (SEAL_DURATION + DarkFountain.SEAL_FLASH_DELAY);
			float flashDelta = Mth.clamp((sealingFlashTick + partialTick) / DarkFountain.SEAL_FLASH_DURATION, 0f, 1f);
			float edgePulseDelta = Mth.clamp((sealingFlashTick + partialTick) / (DarkFountain.SEAL_FLASH_DURATION / 2f), 0f, 1f);
			float edgePulse = Mth.lerp(edgePulseDelta, pulse, 1f);
			float edgePulseFront = Mth.lerp(edgePulseDelta, pulseFront, 1f);

			//Fountain bottom edges
			poseStack.pushPose();
			poseStack.translate(0.5f, 0f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomEdges)),
					1f, 1f, 1f, 1f,
					48f, 140f, 1, 0.01f);
			poseStack.popPose();

			//Black masks for transparency on top
			poseStack.pushPose();
			poseStack.translate(0.5f, 0f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomSealing)),
					0f, 0f, 0f, 1f,
					48f, 140f, 1, 0.0f);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.74f, 0.5f);
			poseStack.scale(edgePulse, 1.0f, edgePulse);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleSealing)),
					0f, 0f, 0f, 1f,
					48f, 140f, length, 0.0f, middleUvOffset);
			poseStack.popPose();

			//Flash transparent overlays
			poseStack.pushPose();
			poseStack.translate(0.5f, 0f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomSealing)),
					1f, 1f, 1f, Mth.clamp(flashDelta, 0f, 1f),
					48f, 140f, 1, 0.01f);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5f, 0f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomSealing)),
					1f, 1f, 1f, Mth.clamp(flashDelta, 0f, 1f),
					48f, 140f, 1, 0.01f);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5f, 0f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottomSealing)),
					1f, 1f, 1f, Mth.clamp(flashDelta, 0f, 1f),
					48f, 140f, 1, 0.01f);
			poseStack.popPose();

			poseStack.pushPose();
			poseStack.translate(0.5f, 0.74f, 0.5f);
			poseStack.scale(edgePulse, 1.0f, edgePulse);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleSealing)),
					1f, 1f, 1f, flashDelta,
					48f, 140f, length, 0.01f, middleUvOffset);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.74f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleSealing)),
					1f, 1f, 1f, flashDelta,
					48f, 140f, length, 0.02f, middleUvOffset);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.74f, 0.5f);
			poseStack.scale(edgePulseFront, 1.0f, edgePulseFront);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleSealing)),
					1f, 1f, 1f, flashDelta,
					48f, 140f, length, 0.03f, middleUvOffset);
			poseStack.popPose();

			//Edges
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.74f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
					1f, 1f, 1f, 1f,
					48f, 140f, length, 0.1f, middleUvOffset);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.74f, 0.5f);
			poseStack.scale(edgePulse, 1.0f, edgePulse);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
					1f, 1f, 1f, 1f,
					48f, 140f, length, 0.0f, middleUvOffset);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.74f, 0.5f);
			poseStack.scale(1.0f, 1.0f, 1.0f);
			poseStack.scale(edgePulseFront, 1.0f, edgePulseFront);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
					1f, 1f, 1f, 1f,
					48f, 140f, length, 0.2f, middleUvOffset);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.74f, 0.5f);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		if (fountain.sealingTick < SEAL_DURATION + DarkFountain.SEAL_FLASH_DELAY) {
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
					middleEdgesColor.getRed() / 255f, middleEdgesColor.getGreen() / 255f, middleEdgesColor.getBlue() / 255f, alpha,
					48f, 140f, length, 0.1f, middleUvOffset);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
					middleEdgesColor.getRed() / 255f, middleEdgesColor.getGreen() / 255f, middleEdgesColor.getBlue() / 255f, alpha,
					48f, 140f, length, 0.1f, middleUvOffset);
		}
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.74f, 0.5f);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		poseStack.scale(pulse, 1.0f, pulse);
		if (fountain.sealingTick < SEAL_DURATION + DarkFountain.SEAL_FLASH_DELAY) {
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
					backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getBlue() / 255f, alpha,
					48f, 140f, length, 0.0f, middleUvOffset);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
					backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getBlue() / 255f, alpha,
					48f, 140f, length, 0.0f, middleUvOffset);
		}
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.74f, 0.5f);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		poseStack.scale(pulseFront, 1.0f, pulseFront);
		if (fountain.sealingTick < SEAL_DURATION + DarkFountain.SEAL_FLASH_DELAY) {
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountainMaskedPortal(textureMiddle, imageDepth, depthWrite)),
					frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha,
					48f, 140f, length, 0.2f, middleUvOffset);
			renderFountainCross(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
					frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getBlue() / 255f, alpha,
					48f, 140f, length, 0.2f, middleUvOffset);
		}
		poseStack.popPose();
	}

	public static void renderOpenFountainOptimized(DarkFountain fountain, int length, PoseStack poseStack, MultiBufferSource buffer, int overlay, float alpha, Vec3 cameraPos) {
		int frameOptimized = fountain.frameOptimized;
		ResourceLocation textureMiddleOptimized = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/optimized/fountain_middle_" + frameOptimized + ".png");
		ResourceLocation textureVortexLower = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/optimized/vortex/fountain_vortex_lower.png");
		ResourceLocation textureVortexTop = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/optimized/vortex/fountain_vortex_top.png");

		int pixelLength = (240 / 16) * length;
		float optimizedCycleProgress = fountain.getFrameTick() / (27f * 3f);
		float lowerRotation = -360f * optimizedCycleProgress* 0.5f;
		float topRotation = -360f * optimizedCycleProgress * 0.25f;

		poseStack.pushPose();
		poseStack.translate(0.5f, pixelLength + 8f, 0.5f);
		poseStack.mulPose(Axis.YN.rotationDegrees(topRotation));
		poseStack.scale(1.5f, 1, 1.5f);
		getVortexModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainNoCull(textureVortexTop)),
				LightTexture.FULL_BRIGHT, overlay, 1f, 1f, 1f, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, pixelLength, 0.5f);
		poseStack.mulPose(Axis.YN.rotationDegrees(lowerRotation));
		poseStack.scale(2, 1, 2);
		getVortexModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainNoCull(textureVortexLower)),
				LightTexture.FULL_BRIGHT, overlay, 1f, 1f, 1f, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.75f, 0.5f);
		renderFountainCrossSorted(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleOptimized)),
				1F, 1F, 1F, alpha, 48f, 240f, length, 0.1f, cameraPos, fountain.getFountainPos());
		poseStack.popPose();
	}

	public static void renderLightWorldOpenFountain(ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		getCrackModel().renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, 1f, 1f, 1f, 1f);
		poseStack.popPose();
	}

	public static void renderDepthsFountain(DarkFountain fountain, PoseStack poseStack, MultiBufferSource buffer, Camera camera, double distance2d, float partialTick, float lodFade) {
		float dimming = (float) Mth.clamp(DEPTHS_DIM_DISTANCE / Math.max(distance2d, DEPTHS_DIM_DISTANCE), DEPTHS_ALPHA_MIN, 1.0);
		int openingTick = fountain.openingTick;
		int sealingTick = fountain.sealingTick;
		float openingSize = DEPTHS_OPENING_PIXELS;

		if (openingTick >= 0 && openingTick < 5) {
			openingSize = Mth.lerp((float) openingTick / 5, 0, DEPTHS_OPENING_PIXELS);
		}

		if (sealingTick >= DEPTHS_FADE_IN_END && sealingTick < DEPTHS_FADE_OUT_FINAL_END) {
			dimming = Mth.lerp((float) (sealingTick - DEPTHS_FADE_IN_END) / (DEPTHS_FADE_OUT_FINAL_END - DEPTHS_FADE_IN_END), dimming, 0);
			openingSize = Mth.lerp((float) (sealingTick - DEPTHS_FADE_IN_END) / (DEPTHS_FADE_OUT_FINAL_END - DEPTHS_FADE_IN_END), DEPTHS_OPENING_PIXELS, 0);
		}
		if (sealingTick >= DEPTHS_FADE_OUT_FINAL_END) {
			dimming = 0;
			openingSize = 0;
		}

		poseStack.pushPose();

		poseStack.translate(0.5f, 0.5f, 0.5f);

		renderDepthsHorizontalPlane(poseStack, buffer.getBuffer(RenderTypes.fountainNoCull(DEPTHS_OPENING)),
				openingSize / 16f, 0f, 1f, 1f, 1f, dimming);

		if (distance2d < DEPTHS_SWIRL_CULL_DISTANCE) {
			float swirlDim = (float) Mth.clamp(1.0 - (distance2d - DEPTHS_SWIRL_FADE_START) / (DEPTHS_SWIRL_CULL_DISTANCE - DEPTHS_SWIRL_FADE_START), 0, 1);

			if (openingTick >= 0 && openingTick < OPENING_SHADOW_DURATION_FULL) {
				swirlDim = 0;
			}
			if (openingTick >= OPENING_SHADOW_DURATION_FULL && openingTick < OPENING_FINISH) {
				swirlDim = Mth.lerp((openingTick - OPENING_SHADOW_DURATION_FULL) / (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL), 0, swirlDim);
			}

			if (sealingTick >= 0 && sealingTick < DEPTHS_FADE_OUT_DURATION) {
				swirlDim = Mth.lerp((float) sealingTick / DEPTHS_FADE_OUT_DURATION, swirlDim, 0);
			}
			if (sealingTick >= DEPTHS_FADE_OUT_DURATION) {
				swirlDim = 0;
			}

			if (swirlDim > 0f) {
				for (DepthsFountainSwirls.Swirl swirl : DepthsFountainSwirls.swirlsAt(fountain.getFountainPos())) {
					float age = swirl.age + partialTick;
					float lifeTime = Mth.clamp(age / swirl.lifetime, 0f, 1f);
					float fadeIn = Mth.clamp(lifeTime / 0.15f, 0f, 1f);
					float fadeOut = Mth.clamp((1f - lifeTime) / 0.3f, 0f, 1f);
					float alpha = fadeIn * fadeOut * swirlDim;

					if (alpha <= 0.01f) {
						continue;
					}

					float baseSize = DEPTHS_SWIRL_SIZE;

					if (openingTick >= 0 && openingTick < OPENING_SHADOW_DURATION_FULL) {
						baseSize = 0;
					}
					if (openingTick >= OPENING_SHADOW_DURATION_FULL && openingTick < OPENING_FINISH) {
						baseSize = Mth.lerp((openingTick - OPENING_SHADOW_DURATION_FULL) / (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL), 6, DEPTHS_SWIRL_SIZE);
					}

					if (sealingTick >= 0) {
						baseSize = Mth.lerp((float) sealingTick / DEPTHS_FADE_OUT_DURATION, DEPTHS_SWIRL_SIZE, 6);
					}

					float size = (1f - lifeTime) * (DEPTHS_SWIRL_PIXELS / 16f) * baseSize;
					if (size <= 0.01f) {
						continue;
					}

					float y = -Mth.lerp(lifeTime, swirl.startYOffset, 0f);
					float yaw;

					if (openingTick >= OPENING_SHADOW_DURATION_FULL && openingTick < OPENING_FINISH) {
						float openDelta = (openingTick + partialTick - OPENING_SHADOW_DURATION_FULL) / (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL);

						yaw = swirl.yaw + swirl.spinSpeedDeg * openDelta * partialTick;
					} else if (sealingTick > 0) {
						float sealDelta = (sealingTick + partialTick) / DEPTHS_FADE_OUT_DURATION;

						yaw = swirl.yaw + swirl.spinSpeedDeg * (1 - sealDelta) * partialTick;
					} else {
						yaw = swirl.yaw + swirl.spinSpeedDeg * partialTick;
					}

					poseStack.pushPose();

					poseStack.translate(0f, y, 0f);
					poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
					ResourceLocation swirlTex = swirl.small ? DEPTHS_SWIRL_SMALL : DEPTHS_SWIRL;

					renderDepthsHorizontalPlane(poseStack, buffer.getBuffer(RenderTypes.fountainNoCull(swirlTex)),
							size, 0f, 1f, 1f, 1f, alpha * dimming);

					poseStack.popPose();
				}

				Level level = Minecraft.getInstance().level;

				if (level != null) {
					float vortexYaw = (level.getGameTime() + partialTick) * DEPTHS_VORTEX_DEG_PER_TICK;
					float vortexSize = DEPTHS_VORTEX_PIXELS;

					if (openingTick >= OPENING_SHADOW_DURATION_FULL && openingTick < OPENING_FINISH) {
						float openingTicks = (fountain.openingTick - OPENING_SHADOW_DURATION_FULL) + partialTick;

						vortexYaw = DEPTHS_VORTEX_DEG_PER_TICK * ((openingTicks * openingTicks) / (2f * (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL)));
						vortexSize = Mth.lerp((openingTick - OPENING_SHADOW_DURATION_FULL) / (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL), DEPTHS_VORTEX_PIXELS * 3, DEPTHS_VORTEX_PIXELS);
					}

					if (sealingTick >= 0) {
						float sealingTicks = fountain.sealingTick + partialTick;
						float sealingStartTime = level.getGameTime() - sealingTick;
						float vortexYawSealStart = sealingStartTime * DEPTHS_VORTEX_DEG_PER_TICK;

						vortexYaw = DEPTHS_VORTEX_DEG_PER_TICK * (vortexYawSealStart + sealingTicks - (sealingTicks * sealingTicks) / (2f * DEPTHS_FADE_OUT_DURATION));
						vortexSize = Mth.lerp(sealingTicks / DEPTHS_FADE_OUT_DURATION, DEPTHS_VORTEX_PIXELS, DEPTHS_VORTEX_PIXELS * 3);
					}

					poseStack.pushPose();

					poseStack.translate(0f, DEPTHS_VORTEX_Y, 0f);
					poseStack.mulPose(Axis.YP.rotationDegrees(vortexYaw));

					renderDepthsHorizontalPlane(poseStack, buffer.getBuffer(RenderTypes.fountainNoCull(DEPTHS_VORTEX)),
							vortexSize / 16f, 0f, 1f, 1f, 1f, swirlDim);

					poseStack.popPose();

					float largerVortexYaw = (level.getGameTime() + partialTick) * DEPTHS_LARGER_VORTEX_DEG_PER_TICK;
					float largerVortexSize = DEPTHS_LARGER_VORTEX_PIXELS;

					if (openingTick >= OPENING_SHADOW_DURATION_FULL && openingTick < OPENING_FINISH) {
						float openingTicks = (fountain.openingTick - OPENING_SHADOW_DURATION_FULL) + partialTick;

						largerVortexYaw = DEPTHS_LARGER_VORTEX_DEG_PER_TICK * ((openingTicks * openingTicks) / (2f * (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL)));
						largerVortexSize = Mth.lerp((openingTick - OPENING_SHADOW_DURATION_FULL) / (OPENING_FINISH - OPENING_SHADOW_DURATION_FULL), DEPTHS_LARGER_VORTEX_PIXELS * 3, DEPTHS_VORTEX_PIXELS);
					}

					if (sealingTick >= 0) {
						float sealingTicks = fountain.sealingTick + partialTick;
						float sealingStartTime = level.getGameTime() - sealingTick;
						float vortexYawSealStart = sealingStartTime * DEPTHS_LARGER_VORTEX_DEG_PER_TICK;

						largerVortexYaw = DEPTHS_LARGER_VORTEX_DEG_PER_TICK * (vortexYawSealStart + sealingTicks - (sealingTicks * sealingTicks) / (2f * DEPTHS_FADE_OUT_DURATION));
						largerVortexSize = Mth.lerp(sealingTicks / DEPTHS_FADE_OUT_DURATION, DEPTHS_LARGER_VORTEX_PIXELS, DEPTHS_LARGER_VORTEX_PIXELS * 3);
					}

					poseStack.pushPose();

					poseStack.translate(0f, DEPTHS_VORTEX_Y * 2, 0f);
					poseStack.mulPose(Axis.YP.rotationDegrees(largerVortexYaw));

					renderDepthsHorizontalPlane(poseStack, buffer.getBuffer(RenderTypes.fountainNoCull(DEPTHS_VORTEX_LARGER)),
							largerVortexSize / 16f, 0f, 1f, 1f, 1f, swirlDim);

					poseStack.popPose();
				}
			}
		}

		poseStack.popPose();
	}

	public static void renderDepthsFountainBeam(DarkFountain fountain, PoseStack poseStack, MultiBufferSource buffer, Camera camera, double distance2d) {
		float dimming = (float) Mth.clamp(DEPTHS_DIM_DISTANCE / Math.max(distance2d, DEPTHS_DIM_DISTANCE), DEPTHS_ALPHA_MIN, 1);
		int sealingTick = fountain.sealingTick;
		int openingTick = fountain.openingTick;
		float bottomWidth = DEPTHS_BEAM_BOTTOM_WIDTH;
		float topWidth = DEPTHS_BEAM_TOP_WIDTH;

		if (openingTick >= 0 && openingTick < OPENING_SHADOW_FADE_START + OPENING_SHADOW_FADE_DURATION) {
			float pulseScale = 0;
			float beamBottomWidth = DEPTHS_BEAM_BOTTOM_WIDTH;

			if (openingTick < 5) {
				pulseScale = Mth.lerp((float) openingTick / 5, 0, 1);
				beamBottomWidth = Mth.lerp((float) openingTick / 5, 0, DEPTHS_BEAM_BOTTOM_WIDTH * 2);
				topWidth = Mth.lerp((float) openingTick / 5, 0, DEPTHS_BEAM_TOP_WIDTH);
				dimming = Mth.lerp((float) openingTick / 5, 0, dimming);
			}
			if (openingTick >= 5 && openingTick < OPENING_SHADOW_FADE_START) {
				pulseScale = 1;
				beamBottomWidth = DEPTHS_BEAM_BOTTOM_WIDTH * 2;
			}
			if (openingTick >= OPENING_SHADOW_FADE_START && openingTick < OPENING_SHADOW_FADE_DURATION + OPENING_SHADOW_FADE_START) {
				pulseScale = Mth.lerp((openingTick - OPENING_SHADOW_FADE_START) / (OPENING_SHADOW_FADE_DURATION),
						1, 0);
				beamBottomWidth = Mth.lerp((openingTick - OPENING_SHADOW_FADE_START) / (OPENING_SHADOW_FADE_DURATION),
						DEPTHS_BEAM_BOTTOM_WIDTH * 2, DEPTHS_BEAM_BOTTOM_WIDTH);
			}

			float pulseAmp = 0.1f;
			float pulse = 1f + pulseAmp * (float) Math.sin(openingTick * OPENING_PULSE_FREQ);

			bottomWidth = beamBottomWidth + beamBottomWidth * (pulse * pulseScale);
		}

		if (sealingTick >= 0 && sealingTick < DEPTHS_FADE_OUT_DURATION) {
			dimming = Mth.lerp((float) sealingTick / DEPTHS_FADE_OUT_DURATION, dimming, 0);
		}
		if (sealingTick >= DEPTHS_FADE_OUT_DURATION && sealingTick < DEPTHS_FADE_IN_START) {
			dimming = 0;
		}
		if (sealingTick >= DEPTHS_FADE_IN_START && sealingTick < DEPTHS_FADE_IN_END) {
			dimming = Mth.lerp((float) (sealingTick - DEPTHS_FADE_IN_START) / (DEPTHS_FADE_IN_END - DEPTHS_FADE_IN_START), 0, dimming);
			bottomWidth = Mth.lerp((float) (sealingTick - DEPTHS_FADE_IN_START) / (DEPTHS_FADE_IN_END - DEPTHS_FADE_IN_START), DEPTHS_BEAM_BOTTOM_WIDTH, 10);
		}
		if (sealingTick >= DEPTHS_FADE_IN_END && sealingTick < DEPTHS_FADE_OUT_FINAL_END) {
			dimming = Mth.lerp((float) (sealingTick - DEPTHS_FADE_IN_END) / (DEPTHS_FADE_OUT_FINAL_END - DEPTHS_FADE_IN_END), dimming, 0);
			bottomWidth = Mth.lerp((float) (sealingTick - DEPTHS_FADE_IN_END) / (DEPTHS_FADE_OUT_FINAL_END - DEPTHS_FADE_IN_END), 10, 0);
			topWidth = Mth.lerp((float) (sealingTick - DEPTHS_FADE_IN_END) / (DEPTHS_FADE_OUT_FINAL_END - DEPTHS_FADE_IN_END), DEPTHS_BEAM_TOP_WIDTH, 0);
		}
		if (sealingTick >= DEPTHS_FADE_OUT_FINAL_END) {
			dimming = 0;
			bottomWidth = 0;
			topWidth = 0;
		}

		poseStack.pushPose();

		poseStack.translate(0.5f, 0.5f, 0.5f);
		renderDepthsBeam(poseStack, buffer.getBuffer(RenderTypes.fountainBeam()), camera, dimming, topWidth, bottomWidth);

		poseStack.popPose();
	}

	private static void renderDepthsHorizontalPlane(PoseStack poseStack, VertexConsumer consumer, float size, float y, float r, float g, float b, float a) {
		Matrix4f matrix = poseStack.last().pose();
		float h = size * 0.5f;

		consumer.vertex(matrix, -h, y, -h).color(r, g, b, a).uv(0f, 0f).endVertex();
		consumer.vertex(matrix, -h, y, h).color(r, g, b, a).uv(0f, 1f).endVertex();
		consumer.vertex(matrix, h, y, h).color(r, g, b, a).uv(1f, 1f).endVertex();
		consumer.vertex(matrix, h, y, -h).color(r, g, b, a).uv(1f, 0f).endVertex();
	}

	private static void renderDepthsBeam(PoseStack poseStack, VertexConsumer consumer, Camera camera, float alpha, float topWidth, float bottomWidth) {
		poseStack.pushPose();

		poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
		Matrix4f matrix = poseStack.last().pose();

		float beamLength = DEPTHS_BEAM_LENGTH;

		consumer.vertex(matrix, -topWidth, 0f, 0f).color(1f, 1f, 1f, alpha).endVertex();
		consumer.vertex(matrix, topWidth, 0f, 0f).color(1f, 1f, 1f, alpha).endVertex();
		consumer.vertex(matrix, bottomWidth, -beamLength, 0f).color(1f, 1f, 1f, 0f).endVertex();
		consumer.vertex(matrix, -bottomWidth, -beamLength, 0f).color(1f, 1f, 1f, 0f).endVertex();

		poseStack.popPose();
	}
}