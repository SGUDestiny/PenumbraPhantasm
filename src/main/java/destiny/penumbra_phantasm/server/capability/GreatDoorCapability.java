package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GreatDoorCapability implements INBTSerializable<CompoundTag> {
    private static final String GREAT_DOORS = "great_doors";
    private static final String RANDOM_GREAT_DOOR_CHUNKS = "random_great_door_chunks";

    public HashMap<BlockPos, GreatDoor> greatDoors = new HashMap<>();
    private final HashSet<Long> randomGreatDoorChunksProcessed = new HashSet<>();

    public void addGreatDoor(BlockPos greatDoorPos, Direction direction, boolean isOpen, List<BlockPos> volumePositions,
                             BlockPos lightDoorPos, ResourceKey<Level> lightDoorDimension, Direction lightDoorExitDirection,
                             boolean isDestinationDarkWorld, @Nullable BlockPos destinationGreatDoorPos,
                             @Nullable ResourceKey<Level> destinationGreatDoorDimension) {
        this.greatDoors.put(greatDoorPos, new GreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos,
                lightDoorDimension, lightDoorExitDirection, isDestinationDarkWorld, destinationGreatDoorPos, destinationGreatDoorDimension));
    }

    @Nullable
    public GreatDoor findByLightDoor(BlockPos lightDoorPos, ResourceKey<Level> lightDimension) {
        for (GreatDoor door : this.greatDoors.values()) {
            if (lightDoorPos.equals(door.lightDoorPos) && lightDimension.equals(door.lightDoorDimension)) {
                return door;
            }
        }
        return null;
    }

    public void removeGreatDoor(Level level, BlockPos greatDoorPos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getCapability(CapabilityRegistry.GREAT_DOOR).ifPresent(cap ->
                    cap.greatDoors.remove(greatDoorPos));
        }
    }

    public boolean isRandomGreatDoorChunkProcessed(long chunkPacked) {
        return randomGreatDoorChunksProcessed.contains(chunkPacked);
    }

    public void markRandomGreatDoorChunkProcessed(long chunkPacked) {
        randomGreatDoorChunksProcessed.add(chunkPacked);
    }

    public static boolean isLightDoorClaimedGlobally(MinecraftServer server, BlockPos doorLower, ResourceKey<Level> doorDimension) {
        for (ServerLevel sl : server.getAllLevels()) {
            GreatDoor g = sl.getCapability(CapabilityRegistry.GREAT_DOOR)
                    .resolve()
                    .map(c -> c.findByLightDoor(doorLower, doorDimension))
                    .orElse(null);
            if (g != null) {
                return true;
            }
        }
        return false;
    }

    private CompoundTag serializeGreatDoors() {
        CompoundTag objectsTag = new CompoundTag();
        ListTag greatDoorTag = new ListTag();

        for (GreatDoor greatDoor : new ArrayList<>(this.greatDoors.values())) {
            greatDoorTag.add(greatDoor.save());
        }
        objectsTag.put(GREAT_DOORS, greatDoorTag);

        return objectsTag;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.put(GREAT_DOORS, serializeGreatDoors());
        long[] chunkLongs = randomGreatDoorChunksProcessed.stream().mapToLong(Long::longValue).toArray();
        tag.putLongArray(RANDOM_GREAT_DOOR_CHUNKS, chunkLongs);

        return tag;
    }

    private void deserializeGreatDoors(CompoundTag tag) {
        ListTag greatDoorsTag = tag.getList(GREAT_DOORS, ListTag.TAG_COMPOUND);
        for (Tag nbt : greatDoorsTag) {
            CompoundTag greatDoorTag = ((CompoundTag) nbt);
            GreatDoor greatDoor = GreatDoor.load(greatDoorTag);

            this.greatDoors.put(greatDoor.greatDoorPos, greatDoor);
        }
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        randomGreatDoorChunksProcessed.clear();
        if (compoundTag.contains(RANDOM_GREAT_DOOR_CHUNKS, Tag.TAG_LONG_ARRAY)) {
            for (long l : compoundTag.getLongArray(RANDOM_GREAT_DOOR_CHUNKS)) {
                randomGreatDoorChunksProcessed.add(l);
            }
        }
        deserializeGreatDoors(compoundTag.getCompound(GREAT_DOORS));
    }
}