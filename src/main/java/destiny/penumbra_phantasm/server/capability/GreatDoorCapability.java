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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GreatDoorCapability implements INBTSerializable<CompoundTag> {
    private static final String GREAT_DOORS = "great_doors";

    public HashMap<BlockPos, GreatDoor> greatDoors = new HashMap<>();

    public void addGreatDoor(BlockPos greatDoorPos, Direction direction, boolean isOpen, List<BlockPos> volumePositions,
                             @Nullable BlockPos lightDoorPos, @Nullable ResourceKey<Level> lightDoorDimension, @Nullable Direction lightDoorExitDirection,
                             boolean isDestinationDarkWorld, @Nullable BlockPos destinationGreatDoorPos,
                             @Nullable ResourceKey<Level> destinationGreatDoorDimension) {
        this.greatDoors.put(greatDoorPos, new GreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos,
                lightDoorDimension, lightDoorExitDirection, isDestinationDarkWorld, destinationGreatDoorPos, destinationGreatDoorDimension));
    }

    @Nullable
    public GreatDoor findByLightDoor(BlockPos lightDoorPos, ResourceKey<Level> lightDimension) {
        if (lightDoorPos == null || lightDimension == null) {
            return null;
        }
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
        deserializeGreatDoors(compoundTag.getCompound(GREAT_DOORS));
    }
}