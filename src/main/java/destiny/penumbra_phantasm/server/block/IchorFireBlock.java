package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public class IchorFireBlock extends FireBlock {
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_53467_) -> p_53467_.getKey() != Direction.DOWN).collect(Util.toMap());

    public IchorFireBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState getStateForPlacement(BlockGetter pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (!this.canCatchFire(pLevel, pPos, Direction.UP) && !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
            BlockState blockstate1 = this.defaultBlockState();

            for(Direction direction : Direction.values()) {
                BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(direction);
                if (booleanproperty != null) {
                    blockstate1 = blockstate1.setValue(booleanproperty, this.canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite()));
                }
            }

            return blockstate1;
        } else {
            return this.defaultBlockState();
        }
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        pLevel.scheduleTick(pPos, this, getFireTickDelay(pLevel.random));
        if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!pState.canSurvive(pLevel, pPos)) {
            }

            BlockState blockstate = pLevel.getBlockState(pPos.below());
            boolean flag = blockstate.isFireSource(pLevel, pPos, Direction.UP);
            int i = pState.getValue(AGE);
            if (!flag && pLevel.isRaining() && this.isNearRain(pLevel, pPos) && pRandom.nextFloat() < 0.2F + (float)i * 0.03F) {
                pLevel.removeBlock(pPos, false);
                pLevel.removeBlock(pPos, false);
            } else {
                int j = Math.min(15, i + pRandom.nextInt(3) / 2);
                if (i != j) {
                    pState = pState.setValue(AGE, j);
                    pLevel.setBlock(pPos, pState, 4);
                }

                if (!flag) {
                    if (!this.isValidFireLocation(pLevel, pPos)) {
                        BlockPos blockpos = pPos.below();
                        if (!pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, Direction.UP) || i > 3) {
                            pLevel.removeBlock(pPos, false);
                        }

                        return;
                    }

                    if (i == 15 && pRandom.nextInt(4) == 0 && !this.canCatchFire(pLevel, pPos.below(), Direction.UP)) {
                        pLevel.removeBlock(pPos, false);
                        return;
                    }
                }

                boolean flag1 = pLevel.getBiome(pPos).is(BiomeTags.INCREASED_FIRE_BURNOUT);
                int k = flag1 ? -50 : 0;
                this.tryCatchFire(pLevel, pPos.east(), 300 + k, pRandom, i, Direction.WEST);
                this.tryCatchFire(pLevel, pPos.west(), 300 + k, pRandom, i, Direction.EAST);
                this.tryCatchFire(pLevel, pPos.below(), 250 + k, pRandom, i, Direction.UP);
                this.tryCatchFire(pLevel, pPos.above(), 250 + k, pRandom, i, Direction.DOWN);
                this.tryCatchFire(pLevel, pPos.north(), 300 + k, pRandom, i, Direction.SOUTH);
                this.tryCatchFire(pLevel, pPos.south(), 300 + k, pRandom, i, Direction.NORTH);
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                for(int l = -1; l <= 1; ++l) {
                    for(int i1 = -1; i1 <= 1; ++i1) {
                        for(int j1 = -1; j1 <= 4; ++j1) {
                            if (l != 0 || j1 != 0 || i1 != 0) {
                                int k1 = 100;
                                if (j1 > 1) {
                                    k1 += (j1 - 1) * 100;
                                }

                                blockpos$mutableblockpos.setWithOffset(pPos, l, j1, i1);
                                int l1 = this.getIgniteOdds(pLevel, blockpos$mutableblockpos);
                                if (l1 > 0) {
                                    int i2 = (l1 + 40 + pLevel.getDifficulty().getId() * 7) / (i + 30);
                                    if (flag1) {
                                        i2 /= 2;
                                    }

                                    if (i2 > 0 && pRandom.nextInt(k1) <= i2 && (!pLevel.isRaining() || !this.isNearRain(pLevel, blockpos$mutableblockpos))) {
                                        int j2 = Math.min(15, i + pRandom.nextInt(5) / 4);
                                        pLevel.setBlock(blockpos$mutableblockpos, this.getStateWithAge(pLevel, blockpos$mutableblockpos, j2), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private static int getFireTickDelay(RandomSource pRandom) {
        return 30 + pRandom.nextInt(10);
    }

    private int getIgniteOdds(LevelReader pLevel, BlockPos pPos) {
        if (!pLevel.isEmptyBlock(pPos)) {
            return 0;
        } else {
            int i = 0;

            for(Direction direction : Direction.values()) {
                BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));
                i = Math.max(blockstate.getFireSpreadSpeed(pLevel, pPos.relative(direction), direction.getOpposite()), i);
            }

            return i;
        }
    }

    private boolean isValidFireLocation(BlockGetter pLevel, BlockPos pPos) {
        for(Direction direction : Direction.values()) {
            if (this.canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    private void tryCatchFire(Level p_53432_, BlockPos p_53433_, int p_53434_, RandomSource p_53435_, int p_53436_, Direction face) {
        int i = p_53432_.getBlockState(p_53433_).getFlammability(p_53432_, p_53433_, face);
        if (p_53435_.nextInt(p_53434_) < i) {
            BlockState blockstate = p_53432_.getBlockState(p_53433_);
            blockstate.onCaughtFire(p_53432_, p_53433_, face, (LivingEntity)null);
            if (p_53435_.nextInt(p_53434_ + 10) < 5 && !p_53432_.isRainingAt(p_53433_)) {
                int j = Math.min(p_53434_ + p_53435_.nextInt(5) / 4, 15);
                p_53432_.setBlock(p_53433_, this.getStateWithAge(p_53432_, p_53433_, j), 3);
            } else {
                p_53432_.removeBlock(p_53433_, false);
            }
        }

    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return this.canSurvive(pState, pLevel, pCurrentPos) ? this.getStateWithAge(pLevel, pCurrentPos, pState.getValue(AGE)) : Blocks.AIR.defaultBlockState();
    }

    private BlockState getStateWithAge(LevelAccessor pLevel, BlockPos pPos, int pAge) {
        BlockState blockstate = getState(pLevel, pPos);
        return blockstate.is(BlockRegistry.ICHOR_FIRE.get()) ? blockstate.setValue(AGE, pAge) : blockstate;
    }

    public static BlockState getState(BlockGetter pReader, BlockPos pPos) {
        return ((IchorFireBlock)BlockRegistry.ICHOR_FIRE.get()).getStateForPlacement(pReader, pPos);
    }

    @Override
    protected boolean canBurn(BlockState pState) {
        return super.canBurn(pState);
    }
}
