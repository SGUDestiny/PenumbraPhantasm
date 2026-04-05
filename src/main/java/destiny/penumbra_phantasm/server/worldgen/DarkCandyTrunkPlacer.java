package destiny.penumbra_phantasm.server.worldgen;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.server.registry.FeatureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class DarkCandyTrunkPlacer extends TrunkPlacer {
    public static final Codec<DarkCandyTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("base_height").forGetter(tree -> tree.baseHeight),
            Codec.INT.fieldOf("height_rand_a").forGetter(tree -> tree.heightRandA),
            Codec.INT.fieldOf("height_rand_b").forGetter(tree -> tree.heightRandB)
    ).apply(instance, DarkCandyTrunkPlacer::new));

    public DarkCandyTrunkPlacer(int pBaseHeight, int pHeightRandA, int pHeightRandB)
    {
        super(pBaseHeight, pHeightRandA, pHeightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type()
    {
        return FeatureRegistry.DARK_CANDY_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        List<FoliagePlacer.FoliageAttachment> foliageAttachment = Lists.newArrayList();

        BlockPos trackPos = blockPos;
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);

        this.placeLog(level, biConsumer, randomSource, trackPos.relative(Direction.NORTH), treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, Direction.NORTH.getAxis()));
        this.placeLog(level, biConsumer, randomSource, trackPos.relative(Direction.SOUTH), treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, Direction.SOUTH.getAxis()));
        this.placeLog(level, biConsumer, randomSource, trackPos.relative(Direction.WEST), treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, Direction.WEST.getAxis()));
        this.placeLog(level, biConsumer, randomSource, trackPos.relative(Direction.EAST), treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, Direction.EAST.getAxis()));

        trackPos = trackPos.above();
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
        trackPos = trackPos.above();
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
        trackPos = trackPos.above();
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);

        foliageAttachment.add(new FoliagePlacer.FoliageAttachment(trackPos, 0, false));

        return foliageAttachment;
    }
}