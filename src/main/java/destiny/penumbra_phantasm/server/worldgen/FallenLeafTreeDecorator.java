package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.server.registry.FeatureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static destiny.penumbra_phantasm.server.block.FallenLeafBlock.LEAVES;

public class FallenLeafTreeDecorator extends TreeDecorator {
    public static final Codec<FallenLeafTreeDecorator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("leaf_block").forGetter(d -> d.fallenLeafBlock),
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(d -> d.probability)
    ).apply(instance, FallenLeafTreeDecorator::new));

    private final BlockState fallenLeafBlock;
    private final float probability;

    public FallenLeafTreeDecorator(BlockState fallenLeafBlock, float probability) {
        this.fallenLeafBlock = fallenLeafBlock;
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return FeatureRegistry.FALLEN_LEAF_TREE_DECORATOR.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        List<BlockPos> leaves = context.leaves();
        Set<BlockPos> placedGroundPositions = new HashSet<>();

        for (BlockPos leafPos : leaves) {
            if (random.nextFloat() >= probability) continue;

            BlockPos groundPos = findGroundUnderLeaf(context, leafPos);
            if (groundPos == null) continue;

            if (!placedGroundPositions.add(groundPos)) continue;

            BlockPos placePos = groundPos.above();
            if (!context.isAir(placePos)) continue;

            context.setBlock(placePos, fallenLeafBlock.setValue(LEAVES, random.nextInt(1, 4)));
        }
    }


    public BlockPos findGroundUnderLeaf(Context context, BlockPos leafPos) {
        BlockPos.MutableBlockPos mutablePos = leafPos.mutable();
        LevelSimulatedReader level = context.level();

        for (int i = 0; i < 10; i++) {
            mutablePos.move(Direction.DOWN);
            BlockPos currentPos = mutablePos.immutable();

            if (level.isStateAtPosition(currentPos, BlockBehaviour.BlockStateBase::isSolid)
                    && !level.isStateAtPosition(currentPos, BlockBehaviour.BlockStateBase::canBeReplaced)
                    && !context.isAir(currentPos)) {
                return currentPos;
            }
        }
        return null;
    }
}