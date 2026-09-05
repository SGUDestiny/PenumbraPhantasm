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
            Codec.floatRange(0, 1).fieldOf("chance").forGetter(decorator -> decorator.chance)
    ).apply(instance, DarkCandyOnLeavesTreeDecorator::new));

    private final float chance;

    public DarkCandyOnLeavesTreeDecorator(float chance) {
        this.chance = chance;
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
            if (random.nextFloat() >= chance) continue;

            Direction outward = pickOutwardDirection(context, leafPos, random);
            if (outward == null) continue;

            BlockPos candyPos = leafPos.relative(outward);
            if (!context.isAir(candyPos)) continue;

            int age = 1 + random.nextInt(6);
            context.setBlock(candyPos, BlockRegistry.DARK_CANDY_BLOCK.get().defaultBlockState().setValue(DarkCandyBlock.FACING, outward)
                    .setValue(DarkCandyBlock.AGE, age));
        }
    }

    private static Direction pickOutwardDirection(Context context, BlockPos leafPos, RandomSource random) {
        Direction[] directions = Direction.values().clone();
        for (int i = directions.length - 1; i > 0; i--) {
            int randomDirection = random.nextInt(i + 1);
            Direction relativeDirections = directions[i];

            directions[i] = directions[randomDirection];
            directions[randomDirection] = relativeDirections;
        }

        for (Direction direction : directions) {
            if (context.isAir(leafPos.relative(direction))) {
                return direction;
            }
        }

        return null;
    }
}