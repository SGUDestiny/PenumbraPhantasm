package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class WeepingEyeBlock extends GenericHorizontalOrientableBlock {
    public static final int MAX_TOTAL_LENGTH = 4;

    public static final IntegerProperty LEAKING = IntegerProperty.create("leaking", 0, 2);

    public WeepingEyeBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(LEAKING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
        pBuilder.add(LEAKING);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        Direction facing = pState.getValue(HORIZONTAL_FACING);
        int leaking = pState.getValue(LEAKING);

        if (!pLevel.getBlockState(pPos.relative(facing)).isSolidRender(pLevel, pPos.relative(facing))) {
            if (leaking < 1) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(LEAKING, leaking + 1));
            } else if (leaking < 2) {
                BlockPos futurePos = pPos.below();

                if (!pLevel.getBlockState(futurePos.relative(facing)).isSolidRender(pLevel, futurePos.relative(facing)) && pLevel.getBlockState(futurePos).is(BlockRegistry.CLIFFROCK.get())) {
                    pLevel.setBlockAndUpdate(futurePos, BlockRegistry.LEAKING_ICHOR.get().defaultBlockState().setValue(HORIZONTAL_FACING, facing));
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(LEAKING, leaking + 1));
                }
            } else {
                int leakingBlocks = 0;
                BlockPos.MutableBlockPos checkPos = pPos.mutable().move(0, -1, 0);
                while (leakingBlocks < MAX_TOTAL_LENGTH && pLevel.getBlockState(checkPos).is(BlockRegistry.LEAKING_ICHOR.get())) {
                    leakingBlocks++;
                    checkPos.move(0, -1, 0);
                }

                BlockPos targetPos = pPos.offset(0, -(leakingBlocks + 1), 0);

                BlockPos bottomOfColumn = pPos.offset(0, -leakingBlocks, 0);
                BlockPos puddlePos = bottomOfColumn.relative(facing);
                if (leakingBlocks >= 1
                        && pLevel.getBlockState(puddlePos.below()).isSolidRender(pLevel, puddlePos.below())
                        && !pLevel.getBlockState(puddlePos).is(BlockRegistry.ICHOR_PUDDLE.get())) {
                    pLevel.setBlockAndUpdate(puddlePos, BlockRegistry.ICHOR_PUDDLE.get().defaultBlockState());
                    return;
                }

                if (leakingBlocks < MAX_TOTAL_LENGTH) {
                    if (!pLevel.getBlockState(targetPos.relative(facing)).isSolidRender(pLevel, targetPos.relative(facing))
                            && pLevel.getBlockState(targetPos).is(BlockRegistry.CLIFFROCK.get())) {
                        pLevel.setBlockAndUpdate(targetPos, BlockRegistry.LEAKING_ICHOR.get().defaultBlockState()
                                .setValue(HORIZONTAL_FACING, facing));
                    }
                }
            }
        }

        super.randomTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        Direction interactionDirection = pHit.getDirection();

        if (interactionDirection == pState.getValue(HORIZONTAL_FACING) && pState.getValue(LEAKING) > 0) {
            BlockPos dropPos = pPos.relative(interactionDirection);

            int leakingBlocks = 0;

            BlockPos.MutableBlockPos checkPos = pPos.mutable().move(0, -1, 0);
            while (leakingBlocks < MAX_TOTAL_LENGTH && pLevel.getBlockState(checkPos).is(BlockRegistry.LEAKING_ICHOR.get())) {
                leakingBlocks++;
                pLevel.setBlockAndUpdate(checkPos, BlockRegistry.CLIFFROCK.get().defaultBlockState());
                checkPos.move(0, -1, 0);
            }

            pLevel.addFreshEntity(new ItemEntity(pLevel, dropPos.getX(), dropPos.getY(), dropPos.getZ(), new ItemStack(ItemRegistry.ICHOR.get(), leakingBlocks)));
            pLevel.playSound(null, pPos, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 1f, 1f);

            pLevel.setBlockAndUpdate(pPos, BlockRegistry.WEEPING_EYE.get().defaultBlockState());

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}