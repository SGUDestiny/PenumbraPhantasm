package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static destiny.penumbra_phantasm.server.block.DarkCandyBlock.AGE;
import static destiny.penumbra_phantasm.server.block.DarkCandyBlock.FACING;

public class DarkCandyLeaves extends LeavesBlock {
    public DarkCandyLeaves(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source) {
        if (level.getRandom().nextFloat() > 0.7f) {
            List<Direction> directions = new ArrayList<>();

            if (level.getBlockState(pos.relative(Direction.NORTH)).isAir() ||
                    (level.getBlockState(pos.relative(Direction.NORTH)).getBlock() == BlockRegistry.DARK_CANDY_BLOCK.get() && level.getBlockState(pos.relative(Direction.NORTH)).getValue(AGE) < 6)) {
                directions.add(Direction.NORTH);
            }
            if (level.getBlockState(pos.relative(Direction.SOUTH)).isAir() ||
                    (level.getBlockState(pos.relative(Direction.SOUTH)).getBlock() == BlockRegistry.DARK_CANDY_BLOCK.get() && level.getBlockState(pos.relative(Direction.SOUTH)).getValue(AGE) < 6)) {
                directions.add(Direction.SOUTH);
            }
            if (level.getBlockState(pos.relative(Direction.WEST)).isAir() ||
                    (level.getBlockState(pos.relative(Direction.WEST)).getBlock() == BlockRegistry.DARK_CANDY_BLOCK.get() && level.getBlockState(pos.relative(Direction.WEST)).getValue(AGE) < 6)) {
                directions.add(Direction.WEST);
            }
            if (level.getBlockState(pos.relative(Direction.EAST)).isAir() ||
                    (level.getBlockState(pos.relative(Direction.EAST)).getBlock() == BlockRegistry.DARK_CANDY_BLOCK.get() && level.getBlockState(pos.relative(Direction.EAST)).getValue(AGE) < 6)) {
                directions.add(Direction.EAST);
            }
            if (level.getBlockState(pos.relative(Direction.UP)).isAir() ||
                    (level.getBlockState(pos.relative(Direction.UP)).getBlock() == BlockRegistry.DARK_CANDY_BLOCK.get() && level.getBlockState(pos.relative(Direction.UP)).getValue(AGE) < 6)) {
                directions.add(Direction.UP);
            }
            if (level.getBlockState(pos.relative(Direction.DOWN)).isAir() ||
                    (level.getBlockState(pos.relative(Direction.DOWN)).getBlock() == BlockRegistry.DARK_CANDY_BLOCK.get() && level.getBlockState(pos.relative(Direction.DOWN)).getValue(AGE) < 6)) {
                directions.add(Direction.DOWN);
            }

            if (!directions.isEmpty()) {
                Direction direction = directions.get(level.getRandom().nextInt(0, directions.size()));

                level.playSound(null, pos, SoundEvents.AZALEA_PLACE, SoundSource.BLOCKS, 1f, 1f);
                if (level.getBlockState(pos.relative(direction)).getBlock() != BlockRegistry.DARK_CANDY_BLOCK.get()) {
                    level.setBlockAndUpdate(pos.relative(direction), BlockRegistry.DARK_CANDY_BLOCK.get().defaultBlockState().setValue(FACING, direction).setValue(AGE, 1));
                } else if (level.getBlockState(pos.relative(direction)).getValue(FACING) == direction) {
                    level.setBlockAndUpdate(pos.relative(direction), BlockRegistry.DARK_CANDY_BLOCK.get().defaultBlockState().setValue(FACING, direction).setValue(AGE, level.getBlockState(pos.relative(direction)).getValue(AGE) + 1));
                }
            }
        }

        super.randomTick(state, level, pos, source);
    }

    @Override
    public boolean isRandomlyTicking(BlockState p_54449_) {
        return true;
    }
}
