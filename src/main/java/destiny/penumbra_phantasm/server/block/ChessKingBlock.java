package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChessKingBlock extends ChessBlock {
    public static final VoxelShape SHAPE_NORTH_SOUTH = ModUtil.buildShape(
            Block.box(4, 14, 4, 12, 17, 12),
            Block.box(5, 4, 5, 11, 18, 11),
            Block.box(4, 5, 4, 12, 7, 12),
            Block.box(4, 18, 4, 12, 24, 12),
            Block.box(6.5, 24, 6.5, 9.5, 25, 9.5),
            Block.box(6.5, 28, 6.5, 9.5, 30, 9.5),
            Block.box(4.5, 25, 6.5, 11.5, 28, 9.5),
            Block.box(3, 0, 3, 13, 4, 13)
    );
    public static final VoxelShape SHAPE_WEST_EAST = ModUtil.buildShape(
            Block.box(4, 14, 4, 12, 17, 12),
            Block.box(5, 4, 5, 11, 18, 11),
            Block.box(4, 5, 4, 12, 7, 12),
            Block.box(4, 18, 4, 12, 24, 12),
            Block.box(6.5, 24, 6.5, 9.5, 25, 9.5),
            Block.box(6.5, 28, 6.5, 9.5, 30, 9.5),
            Block.box(6.5, 25, 4.5, 9.5, 28, 11.5),
            Block.box(3, 0, 3, 13, 4, 13)
    );

    public ChessKingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH) {
            return SHAPE_NORTH_SOUTH;
        } else if (pState.getValue(FACING) == Direction.WEST || pState.getValue(FACING) == Direction.EAST) {
            return SHAPE_WEST_EAST;
        }
        return SHAPE_NORTH_SOUTH;
    }
}
