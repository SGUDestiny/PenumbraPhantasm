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

import java.util.*;

public class DarkRoom {
    public static final String SEED_POS = "seedPos";
    public static final String POSITIONS = "positions";
    public static final String DOOR_POSITIONS = "doorPositions";
    public static final String OUTSIDE_DOORS = "outsideDoors";
    public static final String SHARED_DOORS = "sharedDoors";
    public static final String ENTRY_POS = "pos";
    public static final String ENTRY_DIR = "dir";
    public static final String ENTRY_DIM = "dim";
    public static final String ACTIVE = "active";
    public static final String FILL_INDEX = "fillIndex";

    BlockPos seedPos;
    List<BlockPos> positions;
    Set<BlockPos> doorPositions;
    Map<BlockPos, Direction> outsideDoors;
    Map<BlockPos, ResourceKey<Level>> sharedDoors;
    Map<UUID, Integer> transportTickers;
    int fillIndex;
    boolean active;
    List<BlockPos> dissipationQueue;

    public DarkRoom(BlockPos seedPos, List<BlockPos> positions, Set<BlockPos> doorPositions, Map<BlockPos, Direction> outsideDoors, Map<BlockPos, ResourceKey<Level>> sharedDoors) {
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

    public static boolean sharesAnOpenDoor(ServerLevel level, DarkRoom a, DarkRoom b) {
        Set<BlockPos> aPositions = new HashSet<>(a.getPositions());
        for (BlockPos doorPos : a.getDoorPositions()) {
            if (!b.getDoorPositions().contains(doorPos)) continue;
            BlockState state = level.getBlockState(doorPos);
            for (Direction dir : Direction.values()) {
                if (!aPositions.contains(doorPos.relative(dir))) continue;
                if (DarknessBlock.isDoorVisuallyOpenFromSide(level, doorPos, state, dir)) {
                    return true;
                }
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
        for (Map.Entry<BlockPos, Direction> e : outsideDoors.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.put(ENTRY_POS, NbtUtils.writeBlockPos(e.getKey()));
            entry.putString(ENTRY_DIR, e.getValue().getName());
            outsideList.add(entry);
        }
        tag.put(OUTSIDE_DOORS, outsideList);

        ListTag sharedList = new ListTag();
        for (Map.Entry<BlockPos, ResourceKey<Level>> e : sharedDoors.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.put(ENTRY_POS, NbtUtils.writeBlockPos(e.getKey()));
            entry.putString(ENTRY_DIM, e.getValue().location().toString());
            sharedList.add(entry);
        }
        tag.put(SHARED_DOORS, sharedList);

        tag.putBoolean(ACTIVE, active);
        tag.putInt(FILL_INDEX, fillIndex);

        return tag;
    }

    public static DarkRoom load(CompoundTag tag) {
        BlockPos seedPos = NbtUtils.readBlockPos(tag.getCompound(SEED_POS));

        List<BlockPos> positions = new ArrayList<>();
        ListTag positionsTag = tag.getList(POSITIONS, Tag.TAG_COMPOUND);
        for (Tag t : positionsTag) {
            positions.add(NbtUtils.readBlockPos((CompoundTag) t));
        }

        Set<BlockPos> doorPositions = new HashSet<>();
        ListTag doorsTag = tag.getList(DOOR_POSITIONS, Tag.TAG_COMPOUND);
        for (Tag t : doorsTag) {
            doorPositions.add(NbtUtils.readBlockPos((CompoundTag) t));
        }

        Map<BlockPos, Direction> outsideDoors = new HashMap<>();
        if (tag.contains(OUTSIDE_DOORS)) {
            ListTag outsideList = tag.getList(OUTSIDE_DOORS, Tag.TAG_COMPOUND);
            for (Tag t : outsideList) {
                CompoundTag entry = (CompoundTag) t;
                BlockPos pos = NbtUtils.readBlockPos(entry.getCompound(ENTRY_POS));
                Direction dir = Direction.byName(entry.getString(ENTRY_DIR));
                if (dir != null) {
                    outsideDoors.put(pos, dir);
                }
            }
        }

        Map<BlockPos, ResourceKey<Level>> sharedDoors = new HashMap<>();
        if (tag.contains(SHARED_DOORS)) {
            ListTag sharedList = tag.getList(SHARED_DOORS, Tag.TAG_COMPOUND);
            for (Tag t : sharedList) {
                CompoundTag entry = (CompoundTag) t;
                BlockPos pos = NbtUtils.readBlockPos(entry.getCompound(ENTRY_POS));
                ResourceKey<Level> dim = ModUtil.stringToDimension(entry.getString(ENTRY_DIM));
                if (dim != null) {
                    sharedDoors.put(pos, dim);
                }
            }
        }

        DarkRoom room = new DarkRoom(seedPos, positions, doorPositions, outsideDoors, sharedDoors);
        room.active = tag.getBoolean(ACTIVE);
        room.fillIndex = tag.getInt(FILL_INDEX);
        return room;
    }

    public BlockPos getSeedPos() { return seedPos; }
    public List<BlockPos> getPositions() { return positions; }
    public Set<BlockPos> getDoorPositions() { return doorPositions; }
    public Map<BlockPos, Direction> getOutsideDoors() { return Collections.unmodifiableMap(outsideDoors); }
    public Map<BlockPos, ResourceKey<Level>> getSharedDoors() { return Collections.unmodifiableMap(sharedDoors); }
    public Map<UUID, Integer> getTransportTickers() { return transportTickers; }
    public int getFillIndex() { return fillIndex; }
}
