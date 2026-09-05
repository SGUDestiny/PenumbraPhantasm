package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

import java.util.*;

public class DarkRoom {
    public record OutsideDoorExit(Direction exitFromInterior, @Nullable BlockPos doubleLowerHalf) {
    }

    public record SharedDoorLink(ResourceKey<Level> otherDarkWorld, @Nullable BlockPos doubleLowerHalf) {
    }

    public static final String SEED_POS = "seedPos";
    public static final String POSITIONS = "positions";
    public static final String DOOR_POSITIONS = "doorPositions";
    public static final String OUTSIDE_DOORS = "outsideDoors";
    public static final String SHARED_DOORS = "sharedDoors";
    public static final String ENTRY_POS = "pos";
    public static final String ENTRY_DOUBLE_LOWER = "doubleLower";
    public static final String ENTRY_DIR = "dir";
    public static final String ENTRY_DIM = "dim";
    public static final String ACTIVE = "active";
    public static final String FILL_INDEX = "fillIndex";

    BlockPos seedPos;
    List<BlockPos> positions;
    Set<BlockPos> doorPositions;
    Map<BlockPos, OutsideDoorExit> outsideDoors;
    Map<BlockPos, SharedDoorLink> sharedDoors;
    Map<UUID, Integer> transportTickers;
    int fillIndex;
    boolean active;
    List<BlockPos> dissipationQueue;

    public DarkRoom(BlockPos seedPos, List<BlockPos> positions, Set<BlockPos> doorPositions, Map<BlockPos, OutsideDoorExit> outsideDoors, Map<BlockPos,
            SharedDoorLink> sharedDoors) {
        this.seedPos = seedPos;
        this.positions = positions;
        this.doorPositions = doorPositions;
        this.outsideDoors = new HashMap<>(outsideDoors);
        this.sharedDoors = new HashMap<>(sharedDoors);
        this.transportTickers = new HashMap<>();
        this.fillIndex = 0;
        this.active = false;
        this.dissipationQueue = new ArrayList<>();
    }

    public boolean isFilling() {
        return fillIndex < positions.size() && dissipationQueue.isEmpty();
    }

    public boolean isFillComplete() {
        return fillIndex >= positions.size();
    }

    public boolean isDissipating() {
        return !dissipationQueue.isEmpty();
    }

    public boolean isActive() {
        return active;
    }

    public void checkActivation() {
        if (isFillComplete() && transportTickers.isEmpty() && !isDissipating()) {
            active = true;
        }
    }

    public void beginDissipation() {
        active = false;
        dissipationQueue = new ArrayList<>(positions);
        Collections.shuffle(dissipationQueue);
        transportTickers.clear();
    }

    public static boolean sharesAnOpenDoor(ServerLevel level, DarkRoom firstDarkRoom, DarkRoom secondDarkRoom) {
        Set<BlockPos> firstRoomPositions = new HashSet<>(firstDarkRoom.getPositions());
        for (BlockPos doorPos : firstDarkRoom.getDoorPositions()) {
            if (!secondDarkRoom.getDoorPositions().contains(doorPos)) continue;

            BlockState state = level.getBlockState(doorPos);

            for (Direction direction : Direction.values()) {
                if (!firstRoomPositions.contains(doorPos.relative(direction))) continue;

                if (DarknessBlock.isDoorVisuallyOpenFromSide(level, doorPos, state, direction)) return true;

                break;
            }
        }

        return false;
    }

    public static int getTotalDarknessCount(List<DarkRoom> rooms) {
        int total = 0;
        for (DarkRoom room : rooms) {
            if (!room.isDissipating()) {
                total += room.getPositions().size();
            }
        }

        return total;
    }

    public boolean containsPosition(BlockPos pos) {
        return positions.contains(pos);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(SEED_POS, NbtUtils.writeBlockPos(seedPos));

        ListTag positionsTag = new ListTag();
        for (BlockPos pos : positions) {
            positionsTag.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put(POSITIONS, positionsTag);

        ListTag doorsTag = new ListTag();
        for (BlockPos pos : doorPositions) {
            doorsTag.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put(DOOR_POSITIONS, doorsTag);

        ListTag outsideList = new ListTag();
        for (Map.Entry<BlockPos, OutsideDoorExit> entry : outsideDoors.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            entryTag.put(ENTRY_POS, NbtUtils.writeBlockPos(entry.getKey()));
            entryTag.putString(ENTRY_DIR, entry.getValue().exitFromInterior().getName());

            if (entry.getValue().doubleLowerHalf() != null) {
                entryTag.put(ENTRY_DOUBLE_LOWER, NbtUtils.writeBlockPos(entry.getValue().doubleLowerHalf()));
            }

            outsideList.add(entryTag);
        }
        tag.put(OUTSIDE_DOORS, outsideList);

        ListTag sharedList = new ListTag();
        for (Map.Entry<BlockPos, SharedDoorLink> entry : sharedDoors.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            entryTag.put(ENTRY_POS, NbtUtils.writeBlockPos(entry.getKey()));
            entryTag.putString(ENTRY_DIM, entry.getValue().otherDarkWorld().location().toString());

            if (entry.getValue().doubleLowerHalf() != null) {
                entryTag.put(ENTRY_DOUBLE_LOWER, NbtUtils.writeBlockPos(entry.getValue().doubleLowerHalf()));
            }

            sharedList.add(entryTag);
        }
        tag.put(SHARED_DOORS, sharedList);

        tag.putBoolean(ACTIVE, active);
        tag.putInt(FILL_INDEX, fillIndex);

        return tag;
    }

    public static DarkRoom load(CompoundTag compoundTag) {
        BlockPos seedPos = NbtUtils.readBlockPos(compoundTag.getCompound(SEED_POS));

        List<BlockPos> positions = new ArrayList<>();
        ListTag positionsTag = compoundTag.getList(POSITIONS, Tag.TAG_COMPOUND);
        for (Tag tag : positionsTag) {
            positions.add(NbtUtils.readBlockPos((CompoundTag) tag));
        }

        Set<BlockPos> doorPositions = new HashSet<>();
        ListTag doorsTag = compoundTag.getList(DOOR_POSITIONS, Tag.TAG_COMPOUND);
        for (Tag tag : doorsTag) {
            doorPositions.add(NbtUtils.readBlockPos((CompoundTag) tag));
        }

        Map<BlockPos, OutsideDoorExit> outsideDoors = new HashMap<>();
        if (compoundTag.contains(OUTSIDE_DOORS)) {
            ListTag outsideList = compoundTag.getList(OUTSIDE_DOORS, Tag.TAG_COMPOUND);

            for (Tag tag : outsideList) {
                CompoundTag entryTag = (CompoundTag) tag;

                BlockPos blockPos = NbtUtils.readBlockPos(entryTag.getCompound(ENTRY_POS));
                Direction direction = Direction.byName(entryTag.getString(ENTRY_DIR));
                BlockPos secondLower = entryTag.contains(ENTRY_DOUBLE_LOWER, Tag.TAG_COMPOUND) ? NbtUtils.readBlockPos(entryTag.getCompound(ENTRY_DOUBLE_LOWER)) : null;

                if (direction != null) {
                    outsideDoors.put(blockPos, new OutsideDoorExit(direction, secondLower));
                }
            }
        }

        Map<BlockPos, SharedDoorLink> sharedDoors = new HashMap<>();
        if (compoundTag.contains(SHARED_DOORS)) {
            ListTag sharedList = compoundTag.getList(SHARED_DOORS, Tag.TAG_COMPOUND);
            for (Tag tag : sharedList) {
                CompoundTag entryTag = (CompoundTag) tag;

                BlockPos blockPos = NbtUtils.readBlockPos(entryTag.getCompound(ENTRY_POS));
                ResourceKey<Level> dimensionKey = ModUtil.stringToDimension(entryTag.getString(ENTRY_DIM));
                BlockPos doubleLower = entryTag.contains(ENTRY_DOUBLE_LOWER, Tag.TAG_COMPOUND) ? NbtUtils.readBlockPos(entryTag.getCompound(ENTRY_DOUBLE_LOWER)) : null;

                if (dimensionKey != null) {
                    sharedDoors.put(blockPos, new SharedDoorLink(dimensionKey, doubleLower));
                }
            }
        }

        DarkRoom room = new DarkRoom(seedPos, positions, doorPositions, outsideDoors, sharedDoors);
        room.active = compoundTag.getBoolean(ACTIVE);
        room.fillIndex = compoundTag.getInt(FILL_INDEX);

        return room;
    }

    public BlockPos getSeedPos() {
        return seedPos;
    }

    public List<BlockPos> getPositions() {
        return positions;
    }

    public Set<BlockPos> getDoorPositions() {
        return doorPositions;
    }

    public Map<BlockPos, OutsideDoorExit> getOutsideDoors() {
        return Collections.unmodifiableMap(outsideDoors);
    }

    public Map<BlockPos, SharedDoorLink> getSharedDoors() {
        return Collections.unmodifiableMap(sharedDoors);
    }

    public Optional<Direction> insideHorizontalDirectionTowardDoor(BlockPos doorLowerFoot) {
        for (BlockPos blockPos : positions) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (blockPos.relative(direction).equals(doorLowerFoot)) {
                    return Optional.of(direction);
                }
            }
        }
        return Optional.empty();
    }

    public Map<UUID, Integer> getTransportTickers() {
        return transportTickers;
    }

    public int getFillIndex() {
        return fillIndex;
    }
}