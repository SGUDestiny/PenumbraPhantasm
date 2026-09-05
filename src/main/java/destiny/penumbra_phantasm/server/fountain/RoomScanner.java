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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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

        if (otherFountainRoomToDarkWorld != null && otherFountainRoomToDarkWorld.containsKey(fountainPos)) {
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

                if (otherFountainRoomToDarkWorld != null && otherFountainRoomToDarkWorld.containsKey(neighborPos)) continue;

                BlockState state = level.getBlockState(neighborPos);

                if (state.isAir()) {
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
                    for (Map.Entry<ResourceKey<DarkWorldType>, DarkWorldType> entry : darkWorldTypeRegistry.entrySet()) {
                        if (state.is(DarkWorldUtil.getBlockTag(entry.getValue().blockTag()))) {
                            visitedPositions.add(neighborPos);
                            keyBlockPositions.add(neighborPos);
                            break;
                        }
                    }
                }
            }
        }

        positions.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed());

        Map<BlockPos, DarkRoom.OutsideDoorExit> outsideDoors = new HashMap<>();
        Map<BlockPos, DarkRoom.SharedDoorLink> sharedDoors = new HashMap<>();
        processOutsideDoors(level, positions, doorPositions, otherFountainRoomToDarkWorld, outsideDoors, sharedDoors, maxVolume, blockingPositions);

        return RoomScanResult.success(positions, keyBlockPositions, doorPositions, outsideDoors, sharedDoors);
    }

    private static BlockPos doorLowerHalf(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DoorBlock)) {
            return pos;
        }

        return state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    private static boolean isOpenAir(BlockState state) {
        return state.isAir() || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR);
    }

    private static BlockPos getPosBeyondDoor(Level level, BlockPos firstBeyond, Direction outwardDir) {
        BlockPos pos = firstBeyond;
        for (int i = 0; i < 3; i++) {
            BlockState state = level.getBlockState(pos);

            if (!(state.getBlock() instanceof DoorBlock)) {
                return pos;
            }

            pos = pos.relative(outwardDir);
        }
        return pos;
    }

    private static boolean outsideExceedsRoomBudget(Level level, BlockPos footBeyond, BlockState b0, Set<BlockPos> posSet, int maxVolume,
                                                    @Nullable Set<BlockPos> scanBlockingAnchors,
                                                    @Nullable Map<BlockPos, ResourceKey<Level>> otherFountainRoomToDarkWorld) {
        BlockPos seed = isOpenAir(b0) ? footBeyond : footBeyond.above();
        Set<BlockPos> blocking = new HashSet<>(posSet);

        if (scanBlockingAnchors != null) {
            blocking.addAll(scanBlockingAnchors);
        }

        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        queue.add(seed);
        visited.add(seed);

        int count = 0;
        while (!queue.isEmpty()) {
            if (count > maxVolume) return true;

            BlockPos p = queue.poll();
            count++;

            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);

                if (visited.contains(n) || blocking.contains(n)) continue;
                if (otherFountainRoomToDarkWorld != null && otherFountainRoomToDarkWorld.containsKey(n)) continue;

                BlockState state = level.getBlockState(n);

                if (state.isAir()) {
                    visited.add(n);
                    queue.add(n);
                }
            }
        }
        return false;
    }

    private static void processOutsideDoors(Level level, List<BlockPos> positions, Set<BlockPos> doorPositions, @Nullable Map<BlockPos, ResourceKey<Level>> otherFountainRoomToDarkWorld, Map<BlockPos, DarkRoom.OutsideDoorExit> outsideDoors, Map<BlockPos, DarkRoom.SharedDoorLink> sharedDoors, int maxVolume, @Nullable Set<BlockPos> scanBlockingAnchors) {
        Set<BlockPos> posSet = new HashSet<>(positions);
        Map<BlockPos, ResourceKey<Level>> ownerMap = otherFountainRoomToDarkWorld != null ? otherFountainRoomToDarkWorld : Collections.emptyMap();

        BiConsumer<BlockPos, Direction> consumer = (doorPartPos, directionFromInside) -> {
            BlockState doorState = level.getBlockState(doorPartPos);
            if (!(doorState.getBlock() instanceof DoorBlock)) return;

            BlockPos lower = doorLowerHalf(level, doorPartPos);
            BlockPos upper = DarkWorldUtil.getLowerDoor(level, lower);

            if (outsideDoors.containsKey(upper) || sharedDoors.containsKey(upper)) return;

            BlockPos beyondPos = getPosBeyondDoor(level, upper.relative(directionFromInside), directionFromInside);
            BlockPos beyondPosUp = beyondPos.above();
            ResourceKey<Level> sharedLevel = ownerMap.get(beyondPos);

            if (sharedLevel == null) {
                sharedLevel = ownerMap.get(beyondPosUp);
            }

            BlockPos doubleLower = DarkWorldUtil.getDoubleDoorLower(level, upper);
            if (sharedLevel != null) {
                sharedDoors.put(upper, new DarkRoom.SharedDoorLink(sharedLevel, doubleLower));
                return;
            }

            BlockState beyondState = level.getBlockState(beyondPos);
            BlockState beyondUpState = level.getBlockState(beyondPosUp);
            if (isOpenAir(beyondState) || isOpenAir(beyondUpState)) {
                if (outsideExceedsRoomBudget(level, beyondPos, beyondState, posSet, maxVolume, scanBlockingAnchors, otherFountainRoomToDarkWorld)) {
                    outsideDoors.put(upper, new DarkRoom.OutsideDoorExit(directionFromInside, doubleLower));
                }
            }
        };

        for (BlockPos blockPos : positions) {
            for (Direction direction : Direction.values()) {
                BlockPos relativePos = blockPos.relative(direction);

                if (posSet.contains(relativePos)) continue;

                if (level.getBlockState(relativePos).getBlock() instanceof DoorBlock) {
                    consumer.accept(relativePos, direction);
                }
            }
        }

        for (BlockPos doorPart : doorPositions) {
            BlockPos lowerPos = doorLowerHalf(level, doorPart);

            for (Direction direction : Direction.values()) {
                BlockPos insideLowerPos = lowerPos.relative(direction.getOpposite());
                BlockPos insideUpperPos = lowerPos.above().relative(direction.getOpposite());

                if (posSet.contains(insideLowerPos) || posSet.contains(insideUpperPos)) {
                    consumer.accept(doorPart, direction);
                    break;
                }
            }
        }
    }

    public static void reclassifyDoorsWithRemainingBudget(ServerLevel level, DarkRoom room, int remainingVolume, Set<BlockPos> otherFountains,
                                                          Map<BlockPos, ResourceKey<Level>> otherRoomBlocks) {
        if (remainingVolume <= 0) {
            for (BlockPos doorPos : room.getDoorPositions()) {
                BlockPos lower = doorLowerHalf(level, doorPos);
                BlockPos upper = DarkWorldUtil.getLowerDoor(level, lower);

                if (room.outsideDoors.containsKey(upper) || room.getSharedDoors().containsKey(upper))
                    continue;

                Direction directionFromInside = null;
                for (Direction direction : Direction.values()) {
                    BlockPos insidePos = lower.relative(direction.getOpposite());

                    if (room.getPositions().contains(insidePos) || room.getPositions().contains(insidePos.above())) {
                        directionFromInside = direction;
                        break;
                    }
                }

                if (directionFromInside == null) continue;

                BlockPos doubleLower = DarkWorldUtil.getDoubleDoorLower(level, upper);
                room.outsideDoors.put(upper, new DarkRoom.OutsideDoorExit(directionFromInside, doubleLower));
            }
            return;
        }

        Set<BlockPos> posSet = new HashSet<>(room.getPositions());
        Set<BlockPos> blocking = new HashSet<>(posSet);

        if (otherFountains != null) blocking.addAll(otherFountains);

        for (BlockPos doorPos : room.getDoorPositions()) {
            BlockPos lower = doorLowerHalf(level, doorPos);
            BlockPos upper = DarkWorldUtil.getLowerDoor(level, lower);

            if (room.outsideDoors.containsKey(upper) || room.getSharedDoors().containsKey(upper)) continue;

            Direction directionFromInside = null;
            for (Direction direction : Direction.values()) {
                BlockPos innerPos = lower.relative(direction.getOpposite());

                if (posSet.contains(innerPos) || posSet.contains(innerPos.above())) {
                    directionFromInside = direction;
                    break;
                }
            }

            if (directionFromInside == null) continue;

            BlockPos beyondPos = getPosBeyondDoor(level, upper.relative(directionFromInside), directionFromInside);
            BlockState beyondState = level.getBlockState(beyondPos);
            BlockPos seedPos = isOpenAir(beyondState) ? beyondPos : beyondPos.above();

            Set<BlockPos> visited = new HashSet<>();
            Deque<BlockPos> queue = new ArrayDeque<>();

            queue.add(seedPos);
            visited.add(seedPos);

            int count = 0;
            boolean exceedsBudget = false;
            while (!queue.isEmpty()) {
                if (count > remainingVolume) {
                    exceedsBudget = true;
                    break;
                }

                BlockPos pos = queue.poll();
                count++;

                for (Direction direction : Direction.values()) {
                    BlockPos relativePos = pos.relative(direction);

                    if (visited.contains(relativePos) || blocking.contains(relativePos)) continue;
                    if (otherRoomBlocks.containsKey(relativePos)) continue;

                    BlockState state = level.getBlockState(relativePos);

                    if (state.isAir()) {
                        visited.add(relativePos);
                        queue.add(relativePos);
                    }
                }
            }

            if (exceedsBudget) {
                BlockPos doubleLower = DarkWorldUtil.getDoubleDoorLower(level, upper);
                room.outsideDoors.put(upper, new DarkRoom.OutsideDoorExit(directionFromInside, doubleLower));
            }
        }
    }

    public static boolean hasBreach(Level level, Set<BlockPos> roomPositions, Set<BlockPos> allRoomPositions) {
        for (BlockPos blockPos : roomPositions) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = blockPos.relative(direction);

                if (allRoomPositions.contains(neighborPos)) continue;

                BlockState state = level.getBlockState(neighborPos);

                if (state.isAir()) return true;
            }
        }

        return false;
    }

    public static class RoomScanResult {
        private final List<BlockPos> positions;
        private final List<BlockPos> keyBlockPositions;
        private final Set<BlockPos> doorPositions;
        private final Map<BlockPos, DarkRoom.OutsideDoorExit> outsideDoors;
        private final Map<BlockPos, DarkRoom.SharedDoorLink> sharedDoors;
        private final boolean valid;

        private RoomScanResult(List<BlockPos> positions, List<BlockPos> keyBlockPositions, Set<BlockPos> doorPositions, Map<BlockPos, DarkRoom.OutsideDoorExit> outsideDoors, Map<BlockPos, DarkRoom.SharedDoorLink> sharedDoors, boolean valid) {
            this.positions = positions;
            this.keyBlockPositions = keyBlockPositions;
            this.doorPositions = doorPositions;
            this.outsideDoors = outsideDoors;
            this.sharedDoors = sharedDoors;
            this.valid = valid;
        }

        public static RoomScanResult success(List<BlockPos> positions, List<BlockPos> keyBlockPositions, Set<BlockPos> doorPositions, Map<BlockPos, DarkRoom.OutsideDoorExit> outsideDoors, Map<BlockPos, DarkRoom.SharedDoorLink> sharedDoors) {
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
        public Map<BlockPos, DarkRoom.OutsideDoorExit> getOutsideDoors() {
            return outsideDoors;
        }
        public Map<BlockPos, DarkRoom.SharedDoorLink> getSharedDoors() {
            return sharedDoors;
        }
        public boolean isValid() {
            return valid;
        }
    }
}