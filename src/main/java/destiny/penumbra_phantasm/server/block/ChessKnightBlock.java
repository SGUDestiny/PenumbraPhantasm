package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChessKnightBlock extends ChessBlock {
    public static final VoxelShape SHAPE = ModUtil.buildShape(
            Block.box(5, 4, 5, 11, 14, 11),
            Block.box(4, 12, 4, 12, 18, 12),
            Block.box(3, 0, 3, 13, 4, 13),
            Block.box(4, 9, 4, 12, 11, 12)
    );

    public ChessKnightBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}
