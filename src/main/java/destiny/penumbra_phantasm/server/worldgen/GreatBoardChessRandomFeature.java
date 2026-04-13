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
        BlockPos origin = context.origin();
        BlockPos floorPos = findMarbleFloorColumn(level, origin);
        if (floorPos == null) {
            return false;
        }
        BlockState floor = level.getBlockState(floorPos);
        BlockPos placePos = firstAirAbove(level, floorPos);
        if (placePos == null) {
            return false;
        }
        RandomSource random = context.random();
        Block piece;
        if (floor.is(BlockRegistry.POLISHED_DARK_MARBLE.get()) || floor.is(BlockRegistry.DARK_MARBLE.get())) {
            piece = DARK_PIECES[random.nextInt(DARK_PIECES.length)];
        } else if (floor.is(BlockRegistry.POLISHED_SCARLET_MARBLE.get()) || floor.is(BlockRegistry.SCARLET_MARBLE.get())) {
            piece = SCARLET_PIECES[random.nextInt(SCARLET_PIECES.length)];
        } else {
            return false;
        }
        long facingSeed = level.getSeed() ^ Mth.getSeed(placePos) ^ Mth.getSeed(floorPos);
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(facingSeed));
        BlockState placed = piece.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
        return level.setBlock(placePos, placed, 3);
    }

    private static BlockPos findMarbleFloorColumn(LevelReader level, BlockPos start) {
        BlockPos.MutableBlockPos scan = start.mutable();
        int minY = level.getMinBuildHeight();
        for (int i = 0; i < 48; i++) {
            BlockState st = level.getBlockState(scan);
            if (st.is(BlockRegistry.POLISHED_DARK_MARBLE.get()) || st.is(BlockRegistry.DARK_MARBLE.get())
                    || st.is(BlockRegistry.POLISHED_SCARLET_MARBLE.get()) || st.is(BlockRegistry.SCARLET_MARBLE.get())) {
                return scan.immutable();
            }
            if (!st.isAir() && !st.canBeReplaced()) {
                return null;
            }
            if (scan.getY() <= minY) {
                return null;
            }
            scan.move(Direction.DOWN);
        }
        return null;
    }

    private static BlockPos firstAirAbove(LevelReader level, BlockPos floor) {
        BlockPos.MutableBlockPos m = floor.mutable().move(Direction.UP);
        int maxY = level.getMaxBuildHeight() - 1;
        for (int i = 0; i < 24; i++) {
            BlockState st = level.getBlockState(m);
            if (st.isAir()) {
                return m.immutable();
            }
            if (m.getY() >= maxY) {
                return null;
            }
            m.move(Direction.UP);
        }
        return null;
    }
}
