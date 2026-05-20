package destiny.penumbra_phantasm.server.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class IchorCandleBlockItem extends BlockItem {
    public IchorCandleBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
        return isOnBlock(pContext.getLevel(), pContext.getClickedPos(), pContext.getClickedFace().getOpposite()) && pContext.getClickedFace() != Direction.DOWN;
    }

    public boolean isOnBlock(Level pLevel, BlockPos pPos, Direction blockDirection) {
        Block parentBlock = pLevel.getBlockState(pPos.relative(blockDirection)).getBlock();
        return !parentBlock.equals(Blocks.AIR);
    }
}
