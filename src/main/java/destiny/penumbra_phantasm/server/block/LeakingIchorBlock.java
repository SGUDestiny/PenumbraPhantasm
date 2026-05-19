package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import static destiny.penumbra_phantasm.server.block.WeepingEyeBlock.LEAKING;

public class LeakingIchorBlock extends GenericHorizontalOrientableBlock {
    public LeakingIchorBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        Direction interactionDirection = pHit.getDirection();

        if (interactionDirection == pState.getValue(HORIZONTAL_FACING)) {
           BlockPos dropPos = pPos.relative(interactionDirection);

           pLevel.addFreshEntity(new ItemEntity(pLevel, dropPos.getX(), dropPos.getY(), dropPos.getZ(), ItemRegistry.ICHOR.get().getDefaultInstance()));
           pLevel.playSound(null, pPos, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 1f, 1f);

           pLevel.setBlockAndUpdate(pPos, BlockRegistry.CLIFFROCK.get().defaultBlockState());

           if (pLevel.getBlockState(pPos.above()).is(BlockRegistry.WEEPING_EYE.get())) {
               if (pLevel.getBlockState(pPos.above()).getValue(LEAKING) > 0) {
                   pLevel.addFreshEntity(new ItemEntity(pLevel, dropPos.getX(), dropPos.above().getY(), dropPos.getZ(), ItemRegistry.ICHOR.get().getDefaultInstance()));

                   Direction eyeFacing = pLevel.getBlockState(pPos.above()).getValue(HORIZONTAL_FACING);

                   pLevel.setBlockAndUpdate(pPos.above(), BlockRegistry.WEEPING_EYE.get().defaultBlockState().setValue(LEAKING, 0).setValue(HORIZONTAL_FACING, eyeFacing));
               }
           }

           return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
