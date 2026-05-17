package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import static destiny.penumbra_phantasm.server.block.GenericHorizontalOrientableBlock.HORIZONTAL_FACING;

public class CliffrockEyeFeature extends Feature<NoneFeatureConfiguration> {
    public CliffrockEyeFeature(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = level.getRandom();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        int attempts = 64;

        for (int i = 0; i < attempts; i++) {
            int y = minY + random.nextInt(maxY - minY);
            BlockPos currentPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = level.getBlockState(currentPos);

            if (!state.is(BlockRegistry.CLIFFROCK.get())) continue;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = currentPos.relative(dir);
                if (!level.getBlockState(adjacent).isSolidRender(level, adjacent)) {
                    if (random.nextFloat() < 0.2F) {
                        level.setBlock(currentPos, BlockRegistry.CLIFFROCK_EYE.get().defaultBlockState().setValue(HORIZONTAL_FACING, dir), 2);
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }
}
