package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.block.WeepingEyeBlock;
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

public class WeepingEyeFeature extends Feature<NoneFeatureConfiguration> {
    public WeepingEyeFeature(Codec<NoneFeatureConfiguration> pCodec) {
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

            if (!state.is(BlockRegistry.CLIFFROCK.get()) && !state.is(BlockRegistry.COBBLED_CLIFFROCK.get())) continue;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = currentPos.relative(dir);
                if (!level.getBlockState(adjacent).isSolidRender(level, adjacent)) {
                    if (random.nextFloat() >= 0.2F) break;

                    int columnLength = random.nextInt(WeepingEyeBlock.MAX_TOTAL_LENGTH + 1);

                    int actualLength = 0;
                    if (columnLength > 0) {
                        for (int d = 1; d <= columnLength; d++) {
                            BlockPos ichorPos = currentPos.below(d);
                            if (!level.isOutsideBuildHeight(ichorPos) && (level.getBlockState(ichorPos).is(BlockRegistry.CLIFFROCK.get())
                                    || level.getBlockState(ichorPos).is(BlockRegistry.COBBLED_CLIFFROCK.get())) && !level.getBlockState(ichorPos.relative(dir)).isSolidRender(level, ichorPos.relative(dir))) {
                                actualLength++;
                            } else {
                                break;
                            }
                        }
                    }

                    // Place the eye
                    int leaking = actualLength > 0 ? 2 : 0;
                    level.setBlock(currentPos, BlockRegistry.WEEPING_EYE.get().defaultBlockState().setValue(HORIZONTAL_FACING, dir)
                                    .setValue(WeepingEyeBlock.LEAKING, leaking), 2);

                    for (int d = 1; d <= actualLength; d++) {
                        BlockPos ichorPos = currentPos.below(d);
                        level.setBlock(ichorPos, BlockRegistry.LEAKING_ICHOR.get().defaultBlockState().setValue(HORIZONTAL_FACING, dir), 2);
                    }

                    if (actualLength > 0 && random.nextBoolean()) {
                        BlockPos bottomOfColumn = currentPos.below(actualLength);
                        BlockPos puddlePos = bottomOfColumn.relative(dir);
                        if (level.getBlockState(puddlePos.below()).isSolidRender(level, puddlePos.below())
                                && !level.getBlockState(puddlePos).is(BlockRegistry.ICHOR_PUDDLE.get())
                                && level.getBlockState(puddlePos).canBeReplaced()) {
                            level.setBlock(puddlePos, BlockRegistry.ICHOR_PUDDLE.get().defaultBlockState(), 2);
                        }
                    }

                    return true;
                }
            }
        }
        return false;
    }
}
