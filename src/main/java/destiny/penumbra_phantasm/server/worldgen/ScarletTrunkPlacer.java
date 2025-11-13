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

public class ScarletTrunkPlacer extends TrunkPlacer {
    public static final Codec<ScarletTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("base_height").forGetter(tree -> tree.baseHeight),
            Codec.INT.fieldOf("height_rand_a").forGetter(tree -> tree.heightRandA),
            Codec.INT.fieldOf("height_rand_b").forGetter(tree -> tree.heightRandB)
    ).apply(instance, ScarletTrunkPlacer::new));

    public ScarletTrunkPlacer(int pBaseHeight, int pHeightRandA, int pHeightRandB)
    {
        super(pBaseHeight, pHeightRandA, pHeightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type()
    {
        return FeatureRegistry.SCARLET_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        List<FoliagePlacer.FoliageAttachment> foliageAttachment = Lists.newArrayList();
        Direction direction = Direction.from3DDataValue(randomSource.nextInt(2, 6));

        BlockPos trackPos = blockPos;
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
        trackPos = trackPos.above();
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
        trackPos = trackPos.relative(direction);
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()));
        trackPos = trackPos.above();
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
        trackPos = trackPos.above();
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
        trackPos = trackPos.above();
        this.placeLog(level, biConsumer, randomSource, trackPos, treeConfiguration);
        trackPos = trackPos.above();
        foliageAttachment.add(new FoliagePlacer.FoliageAttachment(trackPos, 0, false));

        BlockPos branch = trackPos;
        branch = branch.below();
        branch = branch.below();
        branch = branch.relative(direction);
        this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()));
        branch = branch.relative(direction);
        this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration, (state) -> state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()));
        branch = branch.above();
        this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration);
        branch = branch.above();
        this.placeLog(level, biConsumer, randomSource, branch, treeConfiguration);

        BlockPos bushLower = trackPos;
        bushLower = bushLower.above();
        bushLower = bushLower.relative(direction);
        bushLower = bushLower.relative(direction);
        foliageAttachment.add(new FoliagePlacer.FoliageAttachment(bushLower, 0, false));

        Direction directionOpposite = direction.getOpposite();

        BlockPos bushUpper = trackPos;
        bushUpper = bushUpper.below();
        bushUpper = bushUpper.relative(directionOpposite);
        bushUpper = bushUpper.relative(directionOpposite);
        bushUpper = bushUpper.relative(directionOpposite.getClockWise());
        foliageAttachment.add(new FoliagePlacer.FoliageAttachment(bushUpper, 0, false));

        return foliageAttachment;
    }
}
