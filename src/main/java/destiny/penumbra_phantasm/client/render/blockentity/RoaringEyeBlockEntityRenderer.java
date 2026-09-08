package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderTypes;
import destiny.penumbra_phantasm.server.block.entity.RoaringEyeBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RoaringEyeBlockEntityRenderer implements BlockEntityRenderer<RoaringEyeBlockEntity> {
    public RoaringEyeBlockEntityRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
    }

    @Override
    public void render(RoaringEyeBlockEntity blockEntity, float v, PoseStack poseStack, MultiBufferSource bufferSource, int i, int i1) {
        Level level = blockEntity.getLevel();

        if (level == null) return;

        RandomSource localRandom = RandomSource.create(blockEntity.getBlockPos().asLong());
        BlockState state = BlockRegistry.ROARING_EYE_GHOST.get().defaultBlockState();

        poseStack.pushPose();

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY)) {
            Minecraft.getInstance().getBlockRenderer().renderBatched(state, blockEntity.getBlockPos(), level, poseStack,
                    bufferSource.getBuffer(rt), true, localRandom, ModelData.EMPTY, rt);
        }

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.translate(0.3f, 0.7f, 0.6f);

        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        ResourceLocation textureLocation = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/block/roaring_eye_emissive.png");
        int overlay = OverlayTexture.NO_OVERLAY;
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.fountain(textureLocation));

        consumer.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(overlay).uv2(i).normal(normalMatrix, 0, 0, 1).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(overlay).uv2(i).normal(normalMatrix, 0, 0, 1).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(overlay).uv2(i).normal(normalMatrix, 0, 0, 1).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(overlay).uv2(i).normal(normalMatrix, 0, 0, 1).endVertex();

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
