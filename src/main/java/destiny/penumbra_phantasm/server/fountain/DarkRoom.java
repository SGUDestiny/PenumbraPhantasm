package destiny.penumbra_phantasm.server.fountain;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.*;

public class DarkRoom {
    public static final String SEED_POS = "seedPos";
    public static final String POSITIONS = "positions";
    public static final String DOOR_POSITIONS = "doorPositions";
    public static final String ACTIVE = "active";
    public static final String FILL_INDEX = "fillIndex";

    BlockPos seedPos;
    List<BlockPos> positions;
    Set<BlockPos> doorPositions;
    Map<UUID, Integer> transportTickers;
    int fillIndex;
    boolean active;
    List<BlockPos> dissipationQueue;

    public DarkRoom(BlockPos seedPos, List<BlockPos> positions, Set<BlockPos> doorPositions) {
        this.seedPos = seedPos;
        this.positions = positions;
        this.doorPositions = doorPositions;
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

        DarkRoom room = new DarkRoom(seedPos, positions, doorPositions);
        room.active = tag.getBoolean(ACTIVE);
        room.fillIndex = tag.getInt(FILL_INDEX);
        return room;
    }

    public BlockPos getSeedPos() { return seedPos; }
    public List<BlockPos> getPositions() { return positions; }
    public Set<BlockPos> getDoorPositions() { return doorPositions; }
    public Map<UUID, Integer> getTransportTickers() { return transportTickers; }
    public int getFillIndex() { return fillIndex; }
}
