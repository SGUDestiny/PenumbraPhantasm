package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.client.render.model.DarkFountainEdgesModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainGroundCrackModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainModel;
import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DarkFountainBlockEntityRenderer implements BlockEntityRenderer<DarkFountainBlockEntity> {
    private DarkFountainModel fountainModel;
    private DarkFountainEdgesModel fountainEdgesModel;
    private DarkFountainGroundCrackModel fountainCrackModel;

    public DarkFountainBlockEntityRenderer(DarkFountainModel fountainModel, DarkFountainEdgesModel fountainEdgesModel, DarkFountainGroundCrackModel fountainCrackModel) {
        this.fountainModel = fountainModel;
        this.fountainEdgesModel = fountainEdgesModel;
        this.fountainCrackModel = fountainCrackModel;
    }

    @Override
    public void render(DarkFountainBlockEntity darkFountainBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Level level = darkFountainBlockEntity.getLevel();
        if (level == null) return;

        float pixel = 1f / 16f;
        float time = (level.getGameTime() + partialTick) * 0.1f;
        float pulse = 1.0f + 0.1f * (float) Math.sin(time);
        float pulse_opposite = 1.0f - 0.1f * (float) Math.sin(time);
        float scaleXZ = 1.0f;
        float animTimeInitial = darkFountainBlockEntity.getAnimationTimer();
        float animationTime = animTimeInitial + partialTick;
        int length = level.getHeight() / 8;
        int frame = darkFountainBlockEntity.getFrame();

        ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_bottom.png");
        ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle/fountain_middle_" + frame + ".png");
        ResourceLocation textureMiddleEdges = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_middle_edges/fountain_middle_edges_" + frame + ".png");
        ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

        if (animTimeInitial != -1) {
            scaleXZ = animationTime / 5f;
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
        poseStack.translate(0f, 7 - (4 * pixel), 0f);
        poseStack.scale(scaleXZ, 1.0f, scaleXZ);
        this.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureBottom)),
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
            this.fountainModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddle)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();

            // Render edges
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(0f, -20f * pixel, 0f);
            poseStack.translate(0f, offset, 0f);
            poseStack.scale(scaleXZ, 1.0f, scaleXZ);
            poseStack.scale(pulse, 1.0f, pulse);
            this.fountainEdgesModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();

            // Render opposite edges
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(0f, -20f * pixel, 0f);
            poseStack.translate(0f, offset, 0f);
            poseStack.scale(scaleXZ, 1.0f, scaleXZ);
            poseStack.scale(pulse_opposite, 1.0f, pulse_opposite);
            this.fountainEdgesModel.renderToBuffer(poseStack, buffer.getBuffer(RenderTypes.fountain(textureMiddleEdges)),
                    LightTexture.FULL_BRIGHT, overlay, 1F, 1F, 1F, 1F);
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(DarkFountainBlockEntity entity, Vec3 vec) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen(DarkFountainBlockEntity p_112306_) {
        return true;
    }
}
