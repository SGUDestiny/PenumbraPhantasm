package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChessPawnBlock extends ChessBlock {
    public static final VoxelShape SHAPE = ModUtil.buildShape(
            Block.box(3, 0, 3, 13, 4, 13),
            Block.box(5, 4, 5, 11, 10, 11),
            Block.box(4, 7, 4, 12, 9, 12),
            Block.box(4, 10, 4, 12, 18, 12)
    );

    public ChessPawnBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}
