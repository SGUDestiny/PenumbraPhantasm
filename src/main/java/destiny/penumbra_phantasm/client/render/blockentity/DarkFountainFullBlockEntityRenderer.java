package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.client.render.model.DarkFountainEdgesModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainGroundCrackModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainOpeningModel;
import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainFullBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DarkFountainFullBlockEntityRenderer implements BlockEntityRenderer<DarkFountainFullBlockEntity> {
    private DarkFountainOpeningModel fountainOpeningModel;
    private DarkFountainModel fountainOpenModel;
    private DarkFountainEdgesModel fountainOpenEdgesModel;
    private DarkFountainGroundCrackModel fountainCrackModel;

    public DarkFountainFullBlockEntityRenderer(DarkFountainOpeningModel fountainOpeningModel, DarkFountainModel fountainOpenModel, DarkFountainEdgesModel fountainOpenEdgesModel, DarkFountainGroundCrackModel fountainCrackModel) {
        this.fountainOpeningModel = fountainOpeningModel;
        this.fountainOpenModel = fountainOpenModel;
        this.fountainOpenEdgesModel = fountainOpenEdgesModel;
        this.fountainCrackModel = fountainCrackModel;
    }

    @Override
    public void render(DarkFountainFullBlockEntity darkFountainFullBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Level level = darkFountainFullBlockEntity.getLevel();
        if (level == null) return; // Safety check

        int length = level.getHeight() / 6;
        ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

        float animTimeInitial = darkFountainFullBlockEntity.animationTimer;
        float animationTime = animTimeInitial + partialTick;

        if (animTimeInitial < 140 && animTimeInitial >= 0) {
            renderOpeningFoutain(animationTime, length, textureCrack, poseStack, buffer, overlay);
        } else {
            renderOpenFountain(darkFountainFullBlockEntity, level, animationTime, length, textureCrack, partialTick, poseStack, buffer, overlay);
        }
    }

    public static void renderOpeningFoutain(float animationTime, int length, ResourceLocation textureCrack, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
        ResourceLocation textureFountainOpeningBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom.png");
        ResourceLocation textureFountainOpeningMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle.png");
        ResourceLocation textureFountainOpeningBottomShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom_shadow.png");
        ResourceLocation textureFountainOpeningMiddleShadow = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle_shadow.png");

        float expandTime = 5f; // 0.5 seconds at 20 ticks/second
        float pulsateTime = 130f; // 5 seconds
        float shrinkTime = 5; // 0.5 seconds (assuming symmetric to expand)
        float pulseAmp = 0.025f;
        float pulseFreq = 2.5f;

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
        PenumbraPhantasm.ClientModEvents.fountainGroundCrackModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
        poseStack.popPose();

        // Render bottom
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.translate(0f, 3f, 0f);
        poseStack.scale(scaleXZ, 1.0f, scaleXZ);
        PenumbraPhantasm.ClientModEvents.fountainOpeningModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningBottom)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
        PenumbraPhantasm.ClientModEvents.fountainOpeningModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureFountainOpeningBottomShadow)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alphaDark);
        poseStack.popPose();

        // Render middle segments
        for (int segment = 0; segment < length; segment++) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(0f, 3 + 5 + (5 * segment), 0f);
            poseStack.scale(scaleXZ, 1.0f, scaleXZ);
            PenumbraPhantasm.ClientModEvents.fountainOpeningModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureFountainOpeningMiddle)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            PenumbraPhantasm.ClientModEvents.fountainOpeningModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureFountainOpeningMiddleShadow)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alphaDark);
            poseStack.popPose();
        }
    }

    public static void renderOpenFountain(DarkFountainFullBlockEntity darkFountainFullBlockEntity, Level level, float animationTime, int length, ResourceLocation textureCrack, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
        float pixel = 1f / 16f;
        float time = (level.getGameTime() + partialTick) * 0.1f;
        float pulse = 1.0f + 0.1f * (float) Math.sin(time);
        float pulse_opposite = 1.0f - 0.1f * (float) Math.sin(time);
        float scaleXZ = 1.0f;
        float animTimeInitial = darkFountainFullBlockEntity.animationTimer;
        int frame = darkFountainFullBlockEntity.getFrame();

        ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom.png");
        ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/fountain_middle_" + frame + ".png");
        ResourceLocation textureMiddleEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle_edges/fountain_middle_edges_" + frame + ".png");

        if (animTimeInitial != -1) {
            scaleXZ = (animationTime - 140) / 5f;
        }

        // Render cracks
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.translate(0f, -1.95f, 0f);
        PenumbraPhantasm.ClientModEvents.fountainGroundCrackModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
        poseStack.popPose();

        // Render bottom
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.translate(0f, 7 - (4 * pixel), 0f);
        poseStack.scale(scaleXZ, 1.0f, scaleXZ);
        PenumbraPhantasm.ClientModEvents.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottom)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 0F, 0F, 1F);
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
            PenumbraPhantasm.ClientModEvents.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddle)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();

            // Render edges
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(0f, -20f * pixel, 0f);
            poseStack.translate(0f, offset, 0f);
            poseStack.scale(scaleXZ, 1.0f, scaleXZ);
            poseStack.scale(pulse, 1.0f, pulse);
            PenumbraPhantasm.ClientModEvents.fountainEdgesModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();

            // Render opposite edges
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(0f, -20f * pixel, 0f);
            poseStack.translate(0f, offset, 0f);
            poseStack.scale(scaleXZ, 1.0f, scaleXZ);
            poseStack.scale(pulse_opposite, 1.0f, pulse_opposite);
            PenumbraPhantasm.ClientModEvents.fountainEdgesModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(DarkFountainFullBlockEntity entity, Vec3 vec) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen(DarkFountainFullBlockEntity p_112306_) {
        return true;
    }
}