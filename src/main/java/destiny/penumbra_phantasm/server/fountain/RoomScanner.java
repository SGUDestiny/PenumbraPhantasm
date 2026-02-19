package destiny.penumbra_phantasm.server.fountain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import destiny.penumbra_phantasm.server.block.DarknessBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
public class RoomScanner {

    public static RoomScanResult scan(Level level, BlockPos seedPos, int maxVolume, boolean includeDarkness) {
        return scan(level, seedPos, maxVolume, includeDarkness, false);
    }

    public static RoomScanResult scan(Level level, BlockPos seedPos, int maxVolume, boolean includeDarkness, boolean openDoorsAsWalls) {
        List<BlockPos> positions = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> doorPositions = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(seedPos);
        visited.add(seedPos);

        while (!queue.isEmpty()) {
            if (positions.size() > maxVolume) {
                return RoomScanResult.failure("Room is too large or not sealed (found " + positions.size() + "/" + maxVolume + " blocks)");
            }

            BlockPos current = queue.poll();
            positions.add(current);

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor)) continue;

                BlockState state = level.getBlockState(neighbor);

                if (state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                } else if (includeDarkness && state.getBlock() instanceof DarknessBlock) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                } else if (state.getBlock() instanceof DoorBlock) {
                    boolean open = state.getValue(DoorBlock.OPEN);
                    if (open && !openDoorsAsWalls) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    } else {
                        doorPositions.add(neighbor);
                    }
                }
            }
        }

        if (positions.size() > maxVolume) {
            return RoomScanResult.failure("Room is too large or not sealed (found " + positions.size() + "/" + maxVolume + " blocks)");
        }

        positions.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed());

        return RoomScanResult.success(positions, doorPositions);
    }

    public static boolean hasBreach(Level level, Set<BlockPos> roomPositions, Set<BlockPos> allRoomPositions) {
        for (BlockPos pos : roomPositions) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (allRoomPositions.contains(neighbor)) continue;

                BlockState state = level.getBlockState(neighbor);
                if (state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class RoomScanResult {
        private final List<BlockPos> positions;
        private final Set<BlockPos> doorPositions;
        private final boolean valid;
        private final String failReason;

        private RoomScanResult(List<BlockPos> positions, Set<BlockPos> doorPositions, boolean valid, String failReason) {
            this.positions = positions;
            this.doorPositions = doorPositions;
            this.valid = valid;
            this.failReason = failReason;
        }

        public static RoomScanResult success(List<BlockPos> positions, Set<BlockPos> doorPositions) {
            return new RoomScanResult(positions, doorPositions, true, null);
        }

        public static RoomScanResult failure(String reason) {
            return new RoomScanResult(Collections.emptyList(), Collections.emptySet(), false, reason);
        }

        public List<BlockPos> getPositions() { return positions; }
        public Set<BlockPos> getDoorPositions() { return doorPositions; }
        public boolean isValid() { return valid; }
        public String getFailReason() { return failReason; }
    }
}
