package destiny.penumbra_phantasm.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.server.block.entity.DustBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.ModelData;

import static destiny.penumbra_phantasm.server.block.DustBlock.ANIMATION_OFFSET;

public class DustBlockEntityRenderer implements BlockEntityRenderer<DustBlockEntity> {
    public DustBlockEntityRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
    }

    @Override
    public void render(DustBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockState state = blockEntity.getBlockState();
        RandomSource localRandom = RandomSource.create(blockEntity.getBlockPos().asLong());

        if (state.hasProperty(ANIMATION_OFFSET) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            int offset = state.getValue(ANIMATION_OFFSET);
            if (offset > 0) {
                float speed = 0.1F;
                float radius = 0.1F;

                float phase = offset * 0.3333F * (float) (2 * Math.PI);
                double angle = (level.getGameTime() + partialTick) * speed + phase;

                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                float fx = facing.getStepX();
                float fz = facing.getStepZ();

                float diagX = fx + fz;
                float diagZ = fz - fx;
                float length = (float) Math.sqrt(diagX * diagX + diagZ * diagZ);
                if (length != 0) {
                    diagX /= length;
                    diagZ /= length;
                }

                float horizDisp = radius * (float) Math.cos(angle);
                float vertDisp = radius * (float) Math.sin(angle);

                poseStack.pushPose();
                poseStack.translate(diagX * horizDisp, vertDisp, diagZ * horizDisp);

                state = BlockRegistry.DUST_BLOCK_GHOST.get().defaultBlockState();

                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
                for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY)) {
                    Minecraft.getInstance().getBlockRenderer().renderBatched(state, blockEntity.getBlockPos(), level, poseStack,
                            bufferSource.getBuffer(rt), true, localRandom, ModelData.EMPTY, rt);
                }

                poseStack.popPose();
                return;
            }
        }

        state = BlockRegistry.DUST_BLOCK_GHOST.get().defaultBlockState();

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY)) {
            Minecraft.getInstance().getBlockRenderer().renderBatched(state, blockEntity.getBlockPos(), level, poseStack,
                    bufferSource.getBuffer(rt), true, localRandom, ModelData.EMPTY, rt);
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
