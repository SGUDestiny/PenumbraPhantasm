package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChessBishopBlock extends ChessBlock {
    public static final VoxelShape SHAPE = ModUtil.buildShape(
            Block.box(5, 4, 5, 11, 15, 11),
            Block.box(4, 13.25, 4, 12, 21.25, 12),
            Block.box(6.5, 21.25, 6.5, 9.5, 24.25, 9.5),
            Block.box(3, 0, 3, 13, 4, 13),
            Block.box(4, 8, 4, 12, 10, 12)
    );

    public ChessBishopBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}
