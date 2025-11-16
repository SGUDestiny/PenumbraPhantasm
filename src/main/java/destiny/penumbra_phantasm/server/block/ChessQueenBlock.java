package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChessQueenBlock extends ChessBlock {
    public static final VoxelShape SHAPE = ModUtil.buildShape(
            Block.box(5, 4, 5, 11, 18, 11),
            Block.box(4, 14, 4, 12, 17, 12),
            Block.box(4, 5, 4, 12, 7, 12),
            Block.box(4, 18, 4, 12, 23, 12),
            Block.box(6.5, 23, 6.5, 9.5, 26, 9.5),
            Block.box(3, 0, 3, 13, 4, 13)
    );

    public ChessQueenBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }
}
