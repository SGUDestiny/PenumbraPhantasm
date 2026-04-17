package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.FluidRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class StartailBlock extends DoublePlantBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StartailBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        LevelAccessor levelaccessor = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        boolean canPlace = blockpos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(blockpos).getBlock() == BlockRegistry.LUMINESCENT_WATER.get()
                && level.getBlockState(blockpos.above()).isAir();

        return canPlace ? this.defaultBlockState().setValue(WATERLOGGED, isLuminescentWater(levelaccessor.getFluidState(blockpos))) : null;
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? FluidRegistry.SOURCE_LUMINESCENT_WATER.get().getSource(false) : super.getFluidState(pState);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
        return isLuminescentWater(pFluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
        if (!pState.getValue(BlockStateProperties.WATERLOGGED) && isLuminescentWater(pFluidState)) {
            if (!pLevel.isClientSide()) {
                pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.WATERLOGGED, true), 3);
                pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pPos, FluidRegistry.SOURCE_LUMINESCENT_WATER.get(), FluidRegistry.SOURCE_LUMINESCENT_WATER.get().getTickDelay(pLevel));
        }

        BlockState updatedState = super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
        return updatedState.isAir() && pState.getValue(WATERLOGGED) ? BlockRegistry.LUMINESCENT_WATER.get().defaultBlockState() : updatedState;
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        if (pState.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive(pState, pLevel, pPos);
        } else {
            BlockState blockstate = pLevel.getBlockState(pPos.below());
            if (pState.getBlock() != this) {
                return super.canSurvive(pState, pLevel, pPos);
            } else {
                return blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
            }
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock()) && pNewState.isAir() && pState.getValue(HALF) == DoubleBlockHalf.LOWER && pState.getValue(WATERLOGGED)) {
            pLevel.setBlock(pPos, BlockRegistry.LUMINESCENT_WATER.get().defaultBlockState(), 3);
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        if (pState.getValue(BlockStateProperties.WATERLOGGED)) {
            pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            if (!pState.canSurvive(pLevel, pPos)) {
                pLevel.destroyBlock(pPos, true);
            }

            return new ItemStack(ItemRegistry.LUMINESCENT_WATER_BUCKET.get());
        } else {
            return ItemStack.EMPTY;
        }
    }

    private static boolean isLuminescentWater(FluidState pFluidState) {
        return isLuminescentWater(pFluidState.getType());
    }

    private static boolean isLuminescentWater(Fluid pFluid) {
        return pFluid == FluidRegistry.SOURCE_LUMINESCENT_WATER.get() || pFluid == FluidRegistry.FLOWING_LUMINESCENT_WATER.get();
    }
}
