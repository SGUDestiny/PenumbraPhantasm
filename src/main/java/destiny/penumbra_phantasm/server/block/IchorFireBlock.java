package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public class IchorFireBlock extends FireBlock {
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_53467_) -> p_53467_.getKey() != Direction.DOWN).collect(Util.toMap());

    public IchorFireBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState getStateForPlacement(BlockGetter pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (!this.canCatchFire(pLevel, pPos, Direction.UP) && !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
            BlockState blockstate1 = this.defaultBlockState();

            for(Direction direction : Direction.values()) {
                BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(direction);
                if (booleanproperty != null) {
                    blockstate1 = blockstate1.setValue(booleanproperty, this.canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite()));
                }
            }

            return blockstate1;
        } else {
            return this.defaultBlockState();
        }
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return this.canSurvive(pState, pLevel, pCurrentPos) ? this.getStateWithAge(pLevel, pCurrentPos, pState.getValue(AGE)) : Blocks.AIR.defaultBlockState();
    }

    private BlockState getStateWithAge(LevelAccessor pLevel, BlockPos pPos, int pAge) {
        BlockState blockstate = getState(pLevel, pPos);
        return blockstate.is(BlockRegistry.ICHOR_FIRE.get()) ? blockstate.setValue(AGE, pAge) : blockstate;
    }

    public static BlockState getState(BlockGetter pReader, BlockPos pPos) {
        return ((IchorFireBlock)BlockRegistry.ICHOR_FIRE.get()).getStateForPlacement(pReader, pPos);
    }
}
