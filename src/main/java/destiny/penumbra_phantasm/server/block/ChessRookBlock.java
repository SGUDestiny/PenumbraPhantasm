package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChessRookBlock extends ChessBlock {
    public static final VoxelShape SHAPE = ModUtil.buildShape(
            Block.box(5, 4, 5, 11, 14, 11),
            Block.box(3, 0, 3, 13, 4, 13),
            Block.box(4, 14, 4, 12, 20, 4),
            Block.box(4, 14, 12, 12, 20, 12),
            Block.box(12, 14, 4, 12, 20, 12),
            Block.box(4, 14, 4, 4, 20, 12),
            Block.box(4, 14, 4, 12, 14, 12),
            Block.box(4, 20, 4, 12, 20, 12),
            Block.box(4, 17, 4, 12, 17, 12),
            Block.box(4, 11, 4, 12, 13, 12)
    );

    public ChessRookBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}
