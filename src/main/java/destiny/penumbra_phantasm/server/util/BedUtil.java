package destiny.penumbra_phantasm.server.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BedUtil {
    private BedUtil() {}


    public static void releaseSleepingPlayer(ServerPlayer player) {
        if (!player.isSleeping() && player.getSleepingPos().isEmpty()) {
            return;
        }

        BlockPos bedPos = player.getSleepingPos().orElse(null);
        Level sleepLevel = player.level();


        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }


        if (bedPos != null) {
            clearBedOccupied(sleepLevel, bedPos);
        }
    }

    public static void clearBedOccupied(Level level, BlockPos anyBedPart) {
        BlockState state = level.getBlockState(anyBedPart);
        if (!(state.getBlock() instanceof BedBlock)) {
            return;
        }


        if (state.hasProperty(BlockStateProperties.OCCUPIED)
                && state.getValue(BlockStateProperties.OCCUPIED)) {
            level.setBlock(anyBedPart, state.setValue(BlockStateProperties.OCCUPIED, false), 3);
        }


        BedPart part = state.getValue(BlockStateProperties.BED_PART);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos other = part == BedPart.FOOT
                ? anyBedPart.relative(facing)
                : anyBedPart.relative(facing.getOpposite());

        BlockState otherState = level.getBlockState(other);
        if (otherState.getBlock() instanceof BedBlock
                && otherState.getValue(BlockStateProperties.OCCUPIED)) {
            level.setBlock(other, otherState.setValue(BlockStateProperties.OCCUPIED, false), 3);
        }
    }
}
