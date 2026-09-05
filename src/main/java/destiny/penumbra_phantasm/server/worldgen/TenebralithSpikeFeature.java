package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Consumer;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class TenebralithSpikeFeature extends Feature<PointedDripstoneConfiguration> {
    public TenebralithSpikeFeature(Codec<PointedDripstoneConfiguration> pCodec) {
        super(pCodec);
    }

    public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> pContext) {
        LevelAccessor levelAccessor = pContext.level();
        BlockPos originPos = pContext.origin();
        RandomSource random = pContext.random();
        PointedDripstoneConfiguration configuration = pContext.config();
        Optional<Direction> tipDirection = getTipDirection(levelAccessor, originPos, random);

        if (tipDirection.isEmpty()) {
            return false;
        } else {
            int height = random.nextFloat() < configuration.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(levelAccessor.getBlockState(originPos.relative(tipDirection.get()))) ? 2 : 1;
            growTenebralithSpike(levelAccessor, originPos, tipDirection.get(), height, false);
            return true;
        }
    }

    private static Optional<Direction> getTipDirection(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
        boolean baseAbove = isTenebralithBase(pLevel.getBlockState(pPos.above()));
        boolean baseBelow = isTenebralithBase(pLevel.getBlockState(pPos.below()));

        if (baseAbove && baseBelow) {
            return Optional.of(pRandom.nextBoolean() ? Direction.DOWN : Direction.UP);
        } else if (baseAbove) {
            return Optional.of(Direction.DOWN);
        } else {
            return baseBelow ? Optional.of(Direction.UP) : Optional.empty();
        }
    }

    public static void growTenebralithSpike(LevelAccessor pLevel, BlockPos pPos, Direction pDirection, int pHeight, boolean pMergeTip) {
        if (isTenebralithBase(pLevel.getBlockState(pPos.relative(pDirection.getOpposite())))) {
            BlockPos.MutableBlockPos mutablePos = pPos.mutable();

            buildBaseToTipColumn(pDirection, pHeight, pMergeTip, (state) -> {
                if (state.is(BlockRegistry.TENEBRALITH_SPIKE.get())) {
                    state = state.setValue(PointedDripstoneBlock.WATERLOGGED, pLevel.isWaterAt(mutablePos));
                }

                pLevel.setBlock(mutablePos, state, 2);
                mutablePos.move(pDirection);
            });
        }
    }

    public static void buildBaseToTipColumn(Direction pDirection, int pHeight, boolean pMergeTip, Consumer<BlockState> pBlockSetter) {
        if (pHeight >= 3) {
            pBlockSetter.accept(createTenebralithSpike(pDirection, DripstoneThickness.BASE));

            for(int i = 0; i < pHeight - 3; ++i) {
                pBlockSetter.accept(createTenebralithSpike(pDirection, DripstoneThickness.MIDDLE));
            }
        }

        if (pHeight >= 2) {
            pBlockSetter.accept(createTenebralithSpike(pDirection, DripstoneThickness.FRUSTUM));
        }

        if (pHeight >= 1) {
            pBlockSetter.accept(createTenebralithSpike(pDirection, pMergeTip ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
        }
    }

    private static BlockState createTenebralithSpike(Direction pDirection, DripstoneThickness pDripstoneThickness) {
        return BlockRegistry.TENEBRALITH_SPIKE.get().defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, pDirection).setValue(PointedDripstoneBlock.THICKNESS, pDripstoneThickness);
    }

    public static boolean isTenebralithBase(BlockState pState) {
        return pState.is(BlockRegistry.TENEBRALITH.get()) || pState.is(BlockRegistry.TENEBRALITH_PATH.get());
    }
}
