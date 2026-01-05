package destiny.penumbra_phantasm.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.model.*;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class FountainRenderUtil
{
	public static float fountainHueAlpha = 0F;

	public static void renderOpeningFoutain(float partialTick, float initialAnimationTime, int length, ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		DarkFountainGroundCrackModel crackModel = new DarkFountainGroundCrackModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_crack_model"), "main")));
		DarkFountainOpeningModel openingModel = new DarkFountainOpeningModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_opening_model"), "main")));

		ResourceLocation textureFountainOpeningBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom.png");
		ResourceLocation textureFountainOpeningMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle.png");
		ResourceLocation textureFountainOpeningBottomShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom_shadow.png");
		ResourceLocation textureFountainOpeningMiddleShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle_shadow.png");

		float animationTime = initialAnimationTime + partialTick;
		float expandTime = 5f; // 0.5 seconds at 20 ticks/second
		float pulsateTime = 120f; // 5 seconds
		float shrinkTime = 5; // 0.5 seconds (assuming symmetric to expand)
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

		// Compute alpha for dark parts
		float fadeStart = 70; // 2 seconds * 20 ticks/second
		float fadeDuration = 20f; // 2 seconds * 20 ticks/second
		float alphaDark = 0f;
		if (animationTime >= fadeStart) {
			if (animationTime < fadeStart + fadeDuration) {
				float prog = (animationTime - fadeStart) / fadeDuration;
				alphaDark = prog;
			} else {
				alphaDark = 1f;
			}
		}

		// Render cracks
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		crackModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		poseStack.popPose();

		// Render bottom
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, 3f, 0f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		openingModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningBottom)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		openingModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureFountainOpeningBottomShadow)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alphaDark);
		poseStack.popPose();

		// Render middle segments
		for (int segment = 0; segment < length; segment++) {
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, 3 + 5 + (5 * segment), 0f);
			poseStack.scale(scaleXZ, 1.0f, scaleXZ);
			openingModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningMiddle)),
					LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
			openingModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureFountainOpeningMiddleShadow)),
					LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alphaDark);
			poseStack.popPose();
		}
	}

	public static void renderOpenFountain(DarkFountain fountain, Level level, float initialAnimationTime, int length, ResourceLocation textureCrack, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		DarkFountainGroundCrackModel crackModel = new DarkFountainGroundCrackModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_crack_model"), "main")));
		DarkFountainBackModel backModel = new DarkFountainBackModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_back_model"), "main")));
		DarkFountainMiddleModel middleModel = new DarkFountainMiddleModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_middle_model"), "main")));
		DarkFountainFrontModel frontModel = new DarkFountainFrontModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_front_model"), "main")));

		int frame = fountain.getFrame();

		ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_" + frame + ".png");
		ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/fountain_middle_" + frame + ".png");
		ResourceLocation textureMiddleEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle_edges/fountain_middle_edges_" + frame + ".png");

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

		if(player != null)
		{
			double playerX = player.getX();
			double playerZ = player.getZ();

			double fountainX = fountain.getDestinationPos().getX();
			double fountainZ = fountain.getDestinationPos().getZ();

			Vec2 flatPlayerPos = new Vec2((float)playerX, (float) playerZ);
			Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);
			if(flatPlayerPos.distanceToSqr(flatFountainPos) < Math.pow(16, 2)) {
				RenderSystem.setShaderColor(middleColor.getRed() / 255f, middleColor.getGreen() / 255f, middleColor.getBlue() / 255f, 1F);
			}
		}

		if (initialAnimationTime != -1) {
			scaleXZ = (animationTime - 140) / 5f;
		}

		// Render cracks
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		crackModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, 1F);
		poseStack.popPose();

		// Render bottom
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, 7 - (4 * pixel), 0f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		middleModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottom)),
				LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, 1F);
		poseStack.popPose();

		// Render middle segments
		for (int segment = 0; segment < length; segment++) {
			float spriteHeight = 140 * pixel;
			float offset = spriteHeight + (spriteHeight * segment);

			// Render middle
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, -20f * pixel, 0f);
			poseStack.translate(0f, offset, 0f);
			poseStack.scale(scaleXZ, 1.0f, scaleXZ);
			middleModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddle)),
					LightTexture.FULL_BRIGHT, overlay, middleColor.getRed() / 255f, middleColor.getGreen() / 255f, middleColor.getGreen() / 255f, 1F);
			poseStack.popPose();
			// Render middle edges
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, -20f * pixel, 0f);
			poseStack.translate(0f, offset, 0f);
			poseStack.scale(scaleXZ, 1.0f, scaleXZ);
			middleModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureMiddleEdges)),
					LightTexture.FULL_BRIGHT, overlay, middleEdgesColor.getRed() / 255f, middleEdgesColor.getGreen() / 255f, middleEdgesColor.getGreen() / 255f, 1F);
			poseStack.popPose();

			// Render back
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, -20f * pixel, 0f);
			poseStack.translate(0f, offset, 0f);
			poseStack.scale(scaleXZ, 1.0f, scaleXZ);
			poseStack.scale(pulse, 1.0f, pulse);
			backModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddle)),
					LightTexture.FULL_BRIGHT, overlay, backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getGreen() / 255f, 1F);
			poseStack.popPose();
			// Render back edges
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, -20f * pixel, 0f);
			poseStack.translate(0f, offset, 0f);
			poseStack.scale(scaleXZ, 1.0f, scaleXZ);
			poseStack.scale(pulse, 1.0f, pulse);
			backModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureMiddleEdges)),
					LightTexture.FULL_BRIGHT, overlay, backColor.getRed() / 255f, backColor.getGreen() / 255f, backColor.getGreen() / 255f, 1F);
			poseStack.popPose();

			// Render front edges
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, -20f * pixel, 0f);
			poseStack.translate(0f, offset, 0f);
			poseStack.scale(scaleXZ, 1.0f, scaleXZ);
			poseStack.scale(pulse_front, 1.0f, pulse_front);
			frontModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureMiddleEdges)),
					LightTexture.FULL_BRIGHT, overlay, frontColor.getRed() / 255f, frontColor.getGreen() / 255f, frontColor.getGreen() / 255f, 1F);
			poseStack.popPose();
		}
	}

	public static void renderOpenFountainOptimized(DarkFountain fountain, float partialTick, float initialAnimationTime, int length, ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		DarkFountainGroundCrackModel crackModel = new DarkFountainGroundCrackModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_crack_model"), "main")));
		DarkFountainBackModel backModel = new DarkFountainBackModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_back_model"), "main")));
		DarkFountainMiddleModel middleModel = new DarkFountainMiddleModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_middle_model"), "main")));

		ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom/fountain_bottom_0.png");
		ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/fountain_middle_0.png");

		float pixel = 1f / 16f;
		float scaleXZ = 1.0f;
		float animationTime = fountain.animationTimer + partialTick;

		if (initialAnimationTime != -1) {
			scaleXZ = (animationTime - 140) / 5f;
		}

		// Render cracks
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		crackModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		poseStack.popPose();

		// Render bottom
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, 7 - (4 * pixel), 0f);
		poseStack.scale(scaleXZ, 1.0f, scaleXZ);
		middleModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottom)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		poseStack.popPose();

		// Render middle segments
		for (int segment = 0; segment < length; segment++) {
			float spriteHeight = 140 * pixel;
			float offset = spriteHeight + (spriteHeight * segment);

			// Render middle
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.translate(0f, -20f * pixel, 0f);
			poseStack.translate(0f, offset, 0f);
			poseStack.scale(scaleXZ, 1.0f, scaleXZ);
			backModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddle)),
					LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
			poseStack.popPose();
		}
	}

	public static void renderLightWorldOpenFountain(ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
		DarkFountainGroundCrackModel crackModel = new DarkFountainGroundCrackModel(Minecraft.getInstance().getEntityModels().bakeLayer(new ModelLayerLocation(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_crack_model"), "main")));

		// Render cracks
		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.translate(0f, -1.95f, 0f);
		crackModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
				LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
		poseStack.popPose();
	}
}
