package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.block.StartailBlock;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class StartailFeature extends Feature<NoneFeatureConfiguration> {
    public StartailFeature(Codec<NoneFeatureConfiguration> p_66768_) {
        super(p_66768_);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        boolean flag = false;

        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();

        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());
        BlockPos finalPos = new BlockPos(pos.getX(), y, pos.getZ());

        if (level.getBlockState(finalPos).is(BlockRegistry.LUMINESCENT_WATER.get())
                && level.getBlockState(finalPos.above()).isAir()) {
            BlockState startailLower = BlockRegistry.STARTAIL.get().defaultBlockState().setValue(StartailBlock.WATERLOGGED, true);
            if (startailLower.canSurvive(level, finalPos)) {
                BlockState startailUpper = BlockRegistry.STARTAIL.get().defaultBlockState().setValue(StartailBlock.HALF, DoubleBlockHalf.UPPER);
                level.setBlock(finalPos.above(), startailUpper, 2);
                level.setBlock(finalPos, startailLower, 2);
                flag = true;
            }
        }

        return flag;
    }
}