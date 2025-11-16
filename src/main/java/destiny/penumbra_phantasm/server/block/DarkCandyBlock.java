package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecated")
public class DarkCandyBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 1, 6);

    public static final VoxelShape SHAPE_NORTH = ModUtil.buildShape(
            Block.box(0, 0, 15, 16, 16, 16)
    );
    public static final VoxelShape SHAPE_SOUTH = ModUtil.buildShape(
            Block.box(0, 0, 0, 16, 16, 1)
    );
    public static final VoxelShape SHAPE_WEST = ModUtil.buildShape(
            Block.box(15, 0, 0, 16, 16, 16)
    );
    public static final VoxelShape SHAPE_EAST = ModUtil.buildShape(
            Block.box(0, 0, 0, 1, 16, 16)
    );
    public static final VoxelShape SHAPE_UP = ModUtil.buildShape(
            Block.box(0, 0, 0, 16, 1, 16)
    );
    public static final VoxelShape SHAPE_DOWN = ModUtil.buildShape(
            Block.box(0, 15, 0, 16, 16, 16)
    );

    public DarkCandyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP).setValue(AGE, 6));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (state.getValue(AGE) != 6) {
            return new ArrayList<>();
        }
        return super.getDrops(state, builder);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch (pState.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            case UP:
                return SHAPE_UP;
            case DOWN:
                return SHAPE_DOWN;
            default:
                return SHAPE_NORTH;
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(FACING, AGE);
        super.createBlockStateDefinition(blockStateBuilder);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (!isValidBlock(pLevel, pPos, pState)) {
            pLevel.destroyBlock(pPos, true);
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    public boolean isValidBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        Direction direction = pState.getValue(FACING);
        BlockState placedBlockState = pLevel.getBlockState(pPos.relative(direction.getOpposite()));
        return !placedBlockState.isAir();
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getClickedFace()).setValue(AGE, 1);
    }
}
