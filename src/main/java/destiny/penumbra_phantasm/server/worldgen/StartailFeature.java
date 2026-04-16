package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class StartailFeature extends Feature<ProbabilityFeatureConfiguration> {
    public StartailFeature(Codec<ProbabilityFeatureConfiguration> p_66768_) {
        super(p_66768_);
    }

    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
        boolean flag = false;

        RandomSource random = context.random();
        ProbabilityFeatureConfiguration config = context.config();

        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();

        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());

        BlockPos finalPos = new BlockPos(pos.getX(), y, pos.getZ());

        if (level.getBlockState(finalPos).is(BlockRegistry.LUMINESCENT_WATER.get())) {
            BlockState startail = BlockRegistry.STARTAIL.get().defaultBlockState();

            level.setBlock(finalPos, startail, 2);
            flag = true;
        }

        return flag;
    }
}