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
        BlockPos origin = pContext.origin();
        Direction extension = Direction.Plane.HORIZONTAL.getRandomDirection(pContext.random());

        BlockState baseState = BlockRegistry.SCARLET_BUSH.get().defaultBlockState();
        BlockState tallState = baseState.setValue(ScarletBushBlock.TALL, true);

        BlockPos[] footprint = new BlockPos[6];
        footprint[0] = origin;
        footprint[1] = origin.offset(1, 0, 0);
        footprint[2] = origin.offset(0, 0, 1);
        footprint[3] = origin.offset(1, 0, 1);

        if (extension.getAxis() == Direction.Axis.X) {
            int x = extension == Direction.EAST ? 2 : -1;
            footprint[4] = origin.offset(x, 0, 0);
            footprint[5] = origin.offset(x, 0, 1);
        } else {
            int z = extension == Direction.SOUTH ? 2 : -1;
            footprint[4] = origin.offset(0, 0, z);
            footprint[5] = origin.offset(1, 0, z);
        }

        for (BlockPos pos : footprint) {
            if (level.isStateAtPosition(pos.below(), s -> s.isAir() || !s.getFluidState().isEmpty())) {
                return false;
            }
        }

        boolean placed = false;
        for (int y = 0; y < 2; y++) {
            BlockState state = y == 1 ? tallState : baseState;
            for (BlockPos pos : footprint) {
                BlockPos target = pos.above(y);
                if (level.isStateAtPosition(target, s -> s.isAir() || s.canBeReplaced())) {
                    level.setBlock(target, state, 2);
                    placed = true;
                }
            }
        }

        return placed;
    }
}
