package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RoaringEyeBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0F, 0.0F, 5.0F, 11.0F, 10.0F, 11.0F);

    public RoaringEyeBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (!isOnBlock(pLevel, pPos)) {
            pLevel.destroyBlock(pPos, true);
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();

        return mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
    }

    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return isOnBlock(pLevel, pPos);
    }

    public boolean isOnBlock(LevelAccessor pLevel, BlockPos pPos) {
        BlockState parentBlockState = pLevel.getBlockState(pPos.below());

        return parentBlockState.isCollisionShapeFullBlock(pLevel, pPos);
    }

    public boolean isOnBlock(BlockGetter pLevel, BlockPos pPos) {
        BlockState parentBlockState = pLevel.getBlockState(pPos.below());

        return parentBlockState.isCollisionShapeFullBlock(pLevel, pPos);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.ROARING_EYE_BLOCK_ENTITY.get().create(blockPos, blockState);
    }
}
