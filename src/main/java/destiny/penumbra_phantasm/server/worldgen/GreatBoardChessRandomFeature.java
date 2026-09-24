package destiny.penumbra_phantasm.server.worldgen;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GreatBoardChessRandomFeature extends Feature<NoneFeatureConfiguration> {
    private static final Block[] DARK_PIECES = new Block[]{
            BlockRegistry.DARK_MARBLE_PAWN.get(),
            BlockRegistry.DARK_MARBLE_ROOK.get(),
            BlockRegistry.DARK_MARBLE_KNIGHT.get(),
            BlockRegistry.DARK_MARBLE_BISHOP.get(),
            BlockRegistry.DARK_MARBLE_QUEEN.get(),
            BlockRegistry.DARK_MARBLE_KING.get()
    };
    private static final Block[] SCARLET_PIECES = new Block[]{
            BlockRegistry.SCARLET_MARBLE_PAWN.get(),
            BlockRegistry.SCARLET_MARBLE_ROOK.get(),
            BlockRegistry.SCARLET_MARBLE_KNIGHT.get(),
            BlockRegistry.SCARLET_MARBLE_BISHOP.get(),
            BlockRegistry.SCARLET_MARBLE_QUEEN.get(),
            BlockRegistry.SCARLET_MARBLE_KING.get()
    };

    public GreatBoardChessRandomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos originPos = context.origin();

        if (!level.getBlockState(originPos).isAir()) return false;

        BlockPos floorPos = originPos.below();

        if (!level.getBlockState(floorPos).isCollisionShapeFullBlock(level, floorPos)) return false;

        BlockState floorState = level.getBlockState(floorPos);
        RandomSource random = context.random();

        Block pieceBlock;
        if (floorState.is(BlockRegistry.POLISHED_DARK_MARBLE.get()) || floorState.is(BlockRegistry.DARK_MARBLE.get())) {
            pieceBlock = DARK_PIECES[random.nextInt(DARK_PIECES.length)];
        } else if (floorState.is(BlockRegistry.POLISHED_SCARLET_MARBLE.get()) || floorState.is(BlockRegistry.SCARLET_MARBLE.get())) {
            pieceBlock = SCARLET_PIECES[random.nextInt(SCARLET_PIECES.length)];
        } else {
            return false;
        }

        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockState finalState = pieceBlock.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);

        return level.setBlock(originPos, finalState, 3);
    }
}