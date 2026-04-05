package destiny.penumbra_phantasm.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PathableBlock extends Block {
    public final Block pathBlock;

    public PathableBlock(Properties pProperties, Block pathBlock) {
        super(pProperties);
        this.pathBlock = pathBlock;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pState.canHarvestBlock(pLevel, pPos, pPlayer)) {
            if (pHit.getDirection() == Direction.UP) {
                pLevel.setBlock(pPos, pathBlock.defaultBlockState(), 2);
                pLevel.playSound(null, pPos, pState.getSoundType().getPlaceSound(), SoundSource.PLAYERS, 1f, 1f);
                pPlayer.swing(pHand);

                if (!pPlayer.isCreative()) {
                    pPlayer.getItemInHand(pHand).hurtAndBreak(1, pPlayer, (user) -> user.broadcastBreakEvent(pHand));
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
