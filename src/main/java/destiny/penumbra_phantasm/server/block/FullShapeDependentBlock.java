package destiny.penumbra_phantasm.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FullShapeDependentBlock extends Block {
    public FullShapeDependentBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (!isOnBlock(pLevel, pPos)) {
            pLevel.destroyBlock(pPos, false);
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return this.isOnBlock(pLevel, pPos);
    }

    public boolean isOnBlock(LevelAccessor pLevel, BlockPos pPos) {
        BlockState parentBlockState = pLevel.getBlockState(pPos.below());

        return parentBlockState.isCollisionShapeFullBlock(pLevel, pPos);
    }

    public boolean isOnBlock(BlockGetter pLevel, BlockPos pPos) {
        BlockState parentBlockState = pLevel.getBlockState(pPos.below());

        return parentBlockState.isCollisionShapeFullBlock(pLevel, pPos);
    }
}
