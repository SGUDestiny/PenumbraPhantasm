package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HearthBlock extends Block {
    public static final VoxelShape SHAPE = ModUtil.buildShape(
            Block.box(3.5, 0, 3.5, 12.5, 1, 12.5),
            Block.box(4.5, 1, 4.5, 11.5, 2, 11.5),
            Block.box(3.5, 2, 3.5, 12.5, 4, 12.5),
            Block.box(3.5, 11, 3.5, 12.5, 12, 12.5),
            Block.box(2.5, 12, 2.5, 13.5, 14, 13.5),
            Block.box(4.5, 14, 4.5, 11.5, 17, 11.5),
            Block.box(5.5, 17, 5.5, 10.5, 19, 10.5),
            Block.box(4.5, 4, 4.5, 11.5, 11, 11.5),
            Block.box(4.5, 1, 4.5, 11.5, 2, 11.5),
            Block.box(3.5, 0, 3.5, 12.5, 1, 12.5),
            Block.box(4.5, 14, 4.5, 11.5, 17, 11.5),
            Block.box(5.5, 17, 5.5, 10.5, 19, 10.5),
            Block.box(4.25, 3.75, 4.25, 11.75, 11.25, 11.75)
    );

    public HearthBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
}
