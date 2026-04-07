package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;

public class GreatDoorCapability implements INBTSerializable<CompoundTag> {
    private static final String GREAT_DOORS = "great_doors";

    public HashMap<BlockPos, GreatDoor> greatDoors = new HashMap<>();

    private CompoundTag serializeDarkFountains() {
        CompoundTag objectsTag = new CompoundTag();
        ListTag greatDoorTag = new ListTag();

        this.greatDoors.forEach((pos, greatDoor) -> {
            greatDoorTag.add(greatDoor.save());
        });
        objectsTag.put(GREAT_DOORS, greatDoorTag);

        return objectsTag;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.put(GREAT_DOORS, serializeDarkFountains());

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
        deserializeGreatDoors(compoundTag);
    }
}