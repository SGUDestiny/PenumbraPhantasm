package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.block.ScarletBushBlock;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class ScarletBushFeature extends Feature<ProbabilityFeatureConfiguration> {
    public ScarletBushFeature(Codec<ProbabilityFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> pContext) {
        WorldGenLevel level = pContext.level();
        BlockPos originPos = pContext.origin();
        Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(pContext.random());

        BlockState baseState = BlockRegistry.SCARLET_BUSH.get().defaultBlockState();
        BlockState tallState = baseState.setValue(ScarletBushBlock.TALL, true);

        BlockPos[] bushPositions = new BlockPos[6];
        bushPositions[0] = originPos;
        bushPositions[1] = originPos.offset(1, 0, 0);
        bushPositions[2] = originPos.offset(0, 0, 1);
        bushPositions[3] = originPos.offset(1, 0, 1);

        if (randomDirection.getAxis() == Direction.Axis.X) {
            int x = randomDirection == Direction.EAST ? 2 : -1;
            bushPositions[4] = originPos.offset(x, 0, 0);
            bushPositions[5] = originPos.offset(x, 0, 1);
        } else {
            int z = randomDirection == Direction.SOUTH ? 2 : -1;
            bushPositions[4] = originPos.offset(0, 0, z);
            bushPositions[5] = originPos.offset(1, 0, z);
        }

        for (BlockPos pos : bushPositions) {
            if (level.isStateAtPosition(pos.below(), state -> state.isAir() || !state.getFluidState().isEmpty())) {
                return false;
            }
        }

        boolean placed = false;
        for (int y = 0; y < 2; y++) {
            BlockState state = y == 1 ? tallState : baseState;

            for (BlockPos pos : bushPositions) {
                BlockPos targetPos = pos.above(y);

                if (level.isStateAtPosition(targetPos, state1 -> state1.isAir() || state1.canBeReplaced())) {
                    level.setBlock(targetPos, state, 2);
                    placed = true;
                }
            }
        }

        return placed;
    }
}
