package destiny.penumbra_phantasm.server.fountain;

import java.util.*;
import java.util.function.BiConsumer;

import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import javax.annotation.Nullable;

public class RoomScanner {

    public static RoomScanResult scan(Level level, BlockPos fountainPos, int maxVolume, boolean includeDarkness) {
        return scan(level, fountainPos, maxVolume, includeDarkness, false, null, null);
    }

    public static RoomScanResult scan(Level level, BlockPos fountainPos, int maxVolume, boolean includeDarkness, boolean openDoorsAsWalls) {
        return scan(level, fountainPos, maxVolume, includeDarkness, openDoorsAsWalls, null, null);
    }

    public static RoomScanResult scan(Level level, BlockPos fountainPos, int maxVolume, boolean includeDarkness, boolean openDoorsAsWalls, @Nullable Set<BlockPos> blockingPositions) {
        return scan(level, fountainPos, maxVolume, includeDarkness, openDoorsAsWalls, blockingPositions, null);
    }

    public static RoomScanResult scan(Level level, BlockPos fountainPos, int maxVolume, boolean includeDarkness, boolean openDoorsAsWalls, @Nullable Set<BlockPos> blockingPositions, @Nullable Map<BlockPos, ResourceKey<Level>> otherFountainRoomToDarkWorld) {
        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> keyBlockPositions = new ArrayList<>();
        Set<BlockPos> visitedPositions = new HashSet<>();
        Set<BlockPos> doorPositions = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Registry<DarkWorldType> darkWorldTypeRegistry = level.registryAccess().registryOrThrow(DarkWorldType.REGISTRY_KEY);

        if (blockingPositions != null && blockingPositions.contains(fountainPos)) {
            return RoomScanResult.failure();
        }

        queue.add(fountainPos);
        visitedPositions.add(fountainPos);

        while (!queue.isEmpty()) {
            if (positions.size() > maxVolume) {
                return RoomScanResult.failure();
            }

            BlockPos currentPos = queue.poll();
            positions.add(currentPos);

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);

                if (visitedPositions.contains(neighborPos)) continue;

                if (blockingPositions != null && blockingPositions.contains(neighborPos)) continue;

                BlockState state = level.getBlockState(neighborPos);

                if (state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR)) {
                    visitedPositions.add(neighborPos);
                    queue.add(neighborPos);
                } else if (includeDarkness && state.getBlock() instanceof DarknessBlock) {
                    visitedPositions.add(neighborPos);
                    queue.add(neighborPos);
                } else if (state.getBlock() instanceof DoorBlock) {
                    boolean open = state.getValue(DoorBlock.OPEN);

                    if (open && !openDoorsAsWalls) {
                        visitedPositions.add(neighborPos);
                        queue.add(neighborPos);
                    } else {
                        doorPositions.add(neighborPos);
                    }
                } else {
                    for (Map.Entry<ResourceKey<DarkWorldType>, DarkWorldType> darkWorldTypeEntry : darkWorldTypeRegistry.entrySet()) {
                        DarkWorldType darkWorldType = darkWorldTypeEntry.getValue();
                        TagKey<Block> currentTag = DarkWorldUtil.getBlockTag(darkWorldType.blockTag());

                        if (state.is(currentTag)) {
                            visitedPositions.add(neighborPos);
                            keyBlockPositions.add(neighborPos);
                            queue.add(neighborPos);
                        }
                    }
                }
            }
        }

        if (positions.size() > maxVolume) {
            return RoomScanResult.failure();
        }

        positions.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed());

        Map<BlockPos, Direction> outsideDoors = new HashMap<>();
        Map<BlockPos, ResourceKey<Level>> sharedDoors = new HashMap<>();
        classifyShellDoors(level, positions, doorPositions, otherFountainRoomToDarkWorld, outsideDoors, sharedDoors, maxVolume, blockingPositions);

        return RoomScanResult.success(positions, keyBlockPositions, doorPositions, outsideDoors, sharedDoors);
    }

    private static BlockPos doorLowerHalf(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (!(s.getBlock() instanceof DoorBlock)) {
            return pos;
        }
        return s.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    private static boolean isOpenAir(BlockState state) {
        return state.isAir() || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR);
    }

    private static BlockPos outwardPastAdjacentDoorLeaves(Level level, BlockPos firstBeyond, Direction outwardDir) {
        BlockPos p = firstBeyond;
        for (int i = 0; i < 3; i++) {
            BlockState st = level.getBlockState(p);
            if (!(st.getBlock() instanceof DoorBlock)) {
                return p;
            }
            p = p.relative(outwardDir);
        }
        return p;
    }

    private static boolean exteriorPocketExceedsRoomBudget(Level level, BlockPos footBeyond, BlockState b0, Set<BlockPos> posSet, int maxVolume, @Nullable Set<BlockPos> scanBlockingAnchors, @Nullable Map<BlockPos, ResourceKey<Level>> otherFountainRoomToDarkWorld) {
        BlockPos seed = isOpenAir(b0) ? footBeyond : footBeyond.above();
        HashSet<BlockPos> exteriorBlock = new HashSet<>(posSet);
        if (scanBlockingAnchors != null) {
            exteriorBlock.addAll(scanBlockingAnchors);
        }
        RoomScanResult side = scan(level, seed, maxVolume, false, false, exteriorBlock, otherFountainRoomToDarkWorld);
        return !side.isValid();
    }

    private static void classifyShellDoors(Level level, List<BlockPos> positions, Set<BlockPos> doorPositions, @Nullable Map<BlockPos, ResourceKey<Level>> otherFountainRoomToDarkWorld, Map<BlockPos, Direction> outsideDoors, Map<BlockPos, ResourceKey<Level>> sharedDoors, int maxVolume, @Nullable Set<BlockPos> scanBlockingAnchors) {
        Set<BlockPos> posSet = new HashSet<>(positions);
        Map<BlockPos, ResourceKey<Level>> ownerMap = otherFountainRoomToDarkWorld != null ? otherFountainRoomToDarkWorld : Collections.emptyMap();

        BiConsumer<BlockPos, Direction> tryClassify = (doorPartPos, dirFromInterior) -> {
            BlockState doorState = level.getBlockState(doorPartPos);
            if (!(doorState.getBlock() instanceof DoorBlock)) {
                return;
            }
            BlockPos lower = doorLowerHalf(level, doorPartPos);
            if (outsideDoors.containsKey(lower) || sharedDoors.containsKey(lower)) {
                return;
            }
            BlockPos footBeyond = outwardPastAdjacentDoorLeaves(level, lower.relative(dirFromInterior), dirFromInterior);
            BlockPos footBeyondUp = footBeyond.above();
            ResourceKey<Level> shr = ownerMap.get(footBeyond);
            if (shr == null) {
                shr = ownerMap.get(footBeyondUp);
            }
            if (shr != null) {
                sharedDoors.put(lower, shr);
                return;
            }
            BlockState b0 = level.getBlockState(footBeyond);
            BlockState b1 = level.getBlockState(footBeyondUp);
            if (isOpenAir(b0) || isOpenAir(b1)) {
                if (exteriorPocketExceedsRoomBudget(level, footBeyond, b0, posSet, maxVolume, scanBlockingAnchors, otherFountainRoomToDarkWorld)) {
                    outsideDoors.put(lower, dirFromInterior);
                }
            }
        };

        for (BlockPos pos : positions) {
            for (Direction dir : Direction.values()) {
                BlockPos n = pos.relative(dir);
                if (posSet.contains(n)) {
                    continue;
                }
                if (level.getBlockState(n).getBlock() instanceof DoorBlock) {
                    tryClassify.accept(n, dir);
                }
            }
        }

        for (BlockPos doorPart : doorPositions) {
            BlockPos lower = doorLowerHalf(level, doorPart);
            for (Direction dir : Direction.values()) {
                BlockPos in0 = lower.relative(dir.getOpposite());
                BlockPos in1 = lower.above().relative(dir.getOpposite());
                if (posSet.contains(in0) || posSet.contains(in1)) {
                    tryClassify.accept(doorPart, dir);
                    break;
                }
            }
        }
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
        private final List<BlockPos> keyBlockPositions;
        private final Set<BlockPos> doorPositions;
        private final Map<BlockPos, Direction> outsideDoors;
        private final Map<BlockPos, ResourceKey<Level>> sharedDoors;
        private final boolean valid;

        private RoomScanResult(List<BlockPos> positions, List<BlockPos> keyBlockPositions, Set<BlockPos> doorPositions, Map<BlockPos, Direction> outsideDoors, Map<BlockPos, ResourceKey<Level>> sharedDoors, boolean valid) {
            this.positions = positions;
            this.keyBlockPositions = keyBlockPositions;
            this.doorPositions = doorPositions;
            this.outsideDoors = outsideDoors;
            this.sharedDoors = sharedDoors;
            this.valid = valid;
        }

        public static RoomScanResult success(List<BlockPos> positions, List<BlockPos> keyBlockPositions, Set<BlockPos> doorPositions, Map<BlockPos, Direction> outsideDoors, Map<BlockPos, ResourceKey<Level>> sharedDoors) {
            return new RoomScanResult(positions, keyBlockPositions, doorPositions, outsideDoors, sharedDoors, true);
        }

        public static RoomScanResult failure() {
            return new RoomScanResult(Collections.emptyList(), Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap(), false);
        }

        public List<BlockPos> getPositions() {
            return positions;
        }
        public List<BlockPos> getKeyBlockPositions() {
            return keyBlockPositions;
        }
        public Set<BlockPos> getDoorPositions() {
            return doorPositions;
        }
        public Map<BlockPos, Direction> getOutsideDoors() {
            return outsideDoors;
        }
        public Map<BlockPos, ResourceKey<Level>> getSharedDoors() {
            return sharedDoors;
        }
        public boolean isValid() {
            return valid;
        }
    }
}
