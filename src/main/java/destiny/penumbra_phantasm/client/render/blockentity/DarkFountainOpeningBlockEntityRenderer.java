package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.client.render.model.DarkFountainGroundCrackModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainOpeningModel;
import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainOpeningBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DarkFountainOpeningBlockEntityRenderer implements  BlockEntityRenderer<DarkFountainOpeningBlockEntity> {
    private DarkFountainOpeningModel fountainModel;
    private DarkFountainGroundCrackModel fountainCrackModel;

    public  DarkFountainOpeningBlockEntityRenderer(DarkFountainOpeningModel fountainModel, DarkFountainGroundCrackModel fountainCrackModel)
    {
        this.fountainModel = fountainModel;
        this.fountainCrackModel = fountainCrackModel;
    }

    @Override
    public void render(DarkFountainOpeningBlockEntity darkFountainOpeningBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Level level = darkFountainOpeningBlockEntity.getLevel();
        if (level == null) return; // Safety check

        int length = level.getHeight() / 2;
        ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom.png");
        ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle.png");
        ResourceLocation textureDarkBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom_shadow.png");
        ResourceLocation textureDarkMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle_shadow.png");
        ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

        float animTime = darkFountainOpeningBlockEntity.getAnimationTimer() + partialTick;
        float expandTime = 5f; // 0.5 seconds at 20 ticks/second
        float pulsateTime = 120f; // 5 seconds
        float shrinkTime = 5; // 0.5 seconds (assuming symmetric to expand)
        float totalTime = expandTime + pulsateTime + shrinkTime;
        float pulseAmp = 0.025f;
        float pulseFreq = 2f;

        if (animTime >= totalTime) {
            return; // Animation complete, don't render
        }

        float baseScale = 1f;
        boolean applyPulse = false;

        if (animTime < expandTime) {
            baseScale = animTime / expandTime;
        } else if (animTime < expandTime + pulsateTime) {
            baseScale = 1f;
            applyPulse = true;
        } else {
            float shrinkProg = (animTime - (expandTime + pulsateTime)) / shrinkTime;
            baseScale = 1f - shrinkProg;
        }

        float pulse = applyPulse ? (1f + pulseAmp * (float) Math.sin(animTime * pulseFreq)) : 1f;
        float scaleXZ = baseScale * pulse;

        // Compute alpha for dark parts
        float fadeStart = 70; // 2 seconds * 20 ticks/second
        float fadeDuration = 20f; // 2 seconds * 20 ticks/second
        float alphaDark = 0f;
        if (animTime >= fadeStart) {
            if (animTime < fadeStart + fadeDuration) {
                float prog = (animTime - fadeStart) / fadeDuration;
                alphaDark = prog;
            } else {
                alphaDark = 1f;
            }
        }

        // Render cracks
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.translate(0f, -1.95f, 0f);
        this.fountainCrackModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureCrack)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
        poseStack.popPose();

        // Render bottom
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.translate(0f, 3f, 0f);
        poseStack.scale(scaleXZ, 1.0f, scaleXZ);
        this.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottom)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
        this.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureDarkBottom)),
                LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alphaDark);
        poseStack.popPose();

        // Render middle segments
        for (int segment = 0; segment < length; segment++) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(0f, 3 + 5 + (5 * segment), 0f);
            poseStack.scale(scaleXZ, 1.0f, scaleXZ);
            this.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddle)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            this.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountainDark(textureDarkMiddle)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, alphaDark);
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(DarkFountainOpeningBlockEntity entity, Vec3 vec) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen(DarkFountainOpeningBlockEntity p_112306_) {
        return true;
    }
}
