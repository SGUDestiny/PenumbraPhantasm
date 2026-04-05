package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.server.block.DarkCandyBlock;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.FeatureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.List;

public class DarkCandyOnLeavesTreeDecorator extends TreeDecorator {
    public static final Codec<DarkCandyOnLeavesTreeDecorator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(d -> d.probability)
    ).apply(instance, DarkCandyOnLeavesTreeDecorator::new));

    private final float probability;

    public DarkCandyOnLeavesTreeDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return FeatureRegistry.DARK_CANDY_ON_LEAVES.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        List<BlockPos> leaves = context.leaves();
        for (BlockPos leafPos : leaves) {
            if (random.nextFloat() >= probability) {
                continue;
            }
            Direction outward = pickOutwardDirection(context, leafPos, random);
            if (outward == null) {
                continue;
            }
            BlockPos candyPos = leafPos.relative(outward);
            if (!context.isAir(candyPos)) {
                continue;
            }
            int age = 1 + random.nextInt(6);
            context.setBlock(candyPos, BlockRegistry.DARK_CANDY_BLOCK.get().defaultBlockState()
                    .setValue(DarkCandyBlock.FACING, outward)
                    .setValue(DarkCandyBlock.AGE, age));
        }
    }

    private static Direction pickOutwardDirection(Context context, BlockPos leafPos, RandomSource random) {
        Direction[] dirs = Direction.values().clone();
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }
        for (Direction dir : dirs) {
            if (context.isAir(leafPos.relative(dir))) {
                return dir;
            }
        }
        return null;
    }
}
