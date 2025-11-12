package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.client.render.model.DarkFountainModel;
import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainOpeningBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DarkFountainOpeningBlockEntityRenderer implements  BlockEntityRenderer<DarkFountainOpeningBlockEntity> {
    private DarkFountainModel model;
    public  DarkFountainOpeningBlockEntityRenderer(DarkFountainModel model)
    {
        this.model = model;
    }

    @Override
    public void render(DarkFountainOpeningBlockEntity darkFountainOpeningBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        int length = 3;
        Level level = darkFountainOpeningBlockEntity.getLevel();

        ResourceLocation textureBottom = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_bottom.png");
        ResourceLocation textureMiddle = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_open_middle.png");

        float time = (level.getGameTime() + v) * 2f; // Reduced multiplier for smoother animation
        float pulse = 1.0f + 0.025f * (float) Math.sin(time); // Pulsates between 0.8 and 1.2

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.translate(0f, 3f, 0f);
        poseStack.scale(pulse, 1.0f, pulse); // Apply pulsating scale
        this.model.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderTypes.fountain(textureBottom)),
                LightTexture.FULL_BRIGHT, i1, 1F, 1F, 1F, 1F);
        poseStack.popPose();


        for (int segment = 0; segment < length; segment++) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.translate(0f, 3 + 5 + (5 * segment), 0f);
            poseStack.scale(pulse, 1.0f, pulse); // Apply pulsating scale
            this.model.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderTypes.fountain(textureMiddle)),
                    LightTexture.FULL_BRIGHT, i1, 1F, 1F, 1F, 1F);
            poseStack.popPose();
        }
    }
}
