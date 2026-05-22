package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.model.CheshireChestModel;
import destiny.penumbra_phantasm.server.block.entity.CheshireChestBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CheshireChestBlockEntityRenderer implements BlockEntityRenderer<CheshireChestBlockEntity> {
    private final CheshireChestModel model;
    private final float pivotX = 8.0F / 16.0F;
    private final float pivotY = 5.5F / 16.0F;
    private final float pivotZ = 14.5F / 16.0F;

    public CheshireChestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new CheshireChestModel(ctx.getModelSet().bakeLayer(CheshireChestModel.LAYER_LOCATION));
    }

    @Override
    public void render(CheshireChestBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       int packedOverlay) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(new ResourceLocation(PenumbraPhantasm.MODID, "textures/block/cheshire_chest.png")));
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        float yRot = switch (facing) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f;
        };

        float lidAngle = blockEntity.getLidAngle(partialTick);
        float openRadians = lidAngle * (float) Math.PI / 2.0F;

        poseStack.pushPose();
        applyBlockTransform(poseStack, yRot);
        this.model.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        applyBlockTransform(poseStack, yRot);

        poseStack.translate(pivotX, pivotY, pivotZ);
        poseStack.mulPose(Axis.XN.rotation(openRadians));
        poseStack.translate(-pivotX, -pivotY, -pivotZ);

        this.model.lid.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void applyBlockTransform(PoseStack poseStack, float yRot) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(-1, -1, 0);
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.translate(-0.5, -0.5, -0.5);
    }
}