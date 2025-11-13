package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.server.registry.FeatureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class ScarletFoliagePlacer extends FoliagePlacer {
    public static final Codec<ScarletFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.CODEC.fieldOf("radius").forGetter(foliage -> foliage.radius),
            IntProvider.CODEC.fieldOf("offset").forGetter(foliage -> foliage.offset)
    ).apply(instance, ScarletFoliagePlacer::new));

    public ScarletFoliagePlacer(IntProvider pRadius, IntProvider pOffset)
    {
        super(pRadius, pOffset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FeatureRegistry.SCARLET_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int foliageHeight, int foliageRadius, int offset) {
        this.placeLeavesRow(level, foliageSetter, randomSource, treeConfiguration, foliageAttachment.pos(), 1, offset, false);
        this.placeLeavesRow(level, foliageSetter, randomSource, treeConfiguration, foliageAttachment.pos().below(), 1, offset, false);

        Direction direction = Direction.from3DDataValue(randomSource.nextInt(2, 6));

        BlockPos trackPos = foliageAttachment.pos();
        trackPos.relative(direction);
        trackPos.offset(direction.getNormal());

        this.placeLeavesRow(level, foliageSetter, randomSource, treeConfiguration, trackPos, 1, offset, false);
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int i1, int i2, int i3, boolean b) {
        return false;
    }
}
