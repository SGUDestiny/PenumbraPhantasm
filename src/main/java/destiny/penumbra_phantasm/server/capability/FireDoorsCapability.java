package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.fountain.FireDoor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FireDoorsCapability implements INBTSerializable<CompoundTag> {
    public static final String PLAYER_FIRE_DOORS = "player_fire_doors";

    public List<FireDoor> playerFireDoors = new ArrayList<>();

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag fireDoorTag = new ListTag();

        for (FireDoor fireDoor : playerFireDoors) {
            fireDoorTag.add(fireDoor.saveDoor());
        }

        tag.put(PLAYER_FIRE_DOORS, fireDoorTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ListTag fireDoorTag = tag.getList(PLAYER_FIRE_DOORS, Tag.TAG_COMPOUND);
        List<FireDoor> playerFireDoors = new ArrayList<>();

        for (Tag listTag : fireDoorTag) {
            FireDoor fireDoor = FireDoor.loadDoor((CompoundTag) listTag);

            playerFireDoors.add(fireDoor);
        }

        this.playerFireDoors = playerFireDoors;
    }

    public void addFireDoor(ResourceKey<Level> darkWorld, BlockPos doorPos, float facingAngle, String name) {
        this.playerFireDoors.add(new FireDoor(darkWorld, doorPos, facingAngle, name));
    }

    public void removeFireDoor(ResourceKey<Level> darkWorld, BlockPos doorPos) {
        int fireDoorToRemove = findDoorIndexInList(darkWorld, doorPos);

        if (fireDoorToRemove > -1) {
            this.playerFireDoors.remove(fireDoorToRemove);
        }
    }

    public int findDoorIndexInList(ResourceKey<Level> darkWorld, BlockPos doorPos) {
        for (int i = 0; i < this.playerFireDoors.size(); i++) {
            FireDoor fireDoor = this.playerFireDoors.get(i);

            if (fireDoor.darkWorld().equals(darkWorld) && fireDoor.doorPos().equals(doorPos)) {
                return i;
            }
        }
        return -1;
    }

    public FireDoor getDoorFromIndex(int doorIndex) {
        return this.playerFireDoors.get(doorIndex);
    }

    public void sync(@NotNull FireDoorsCapability cap) {
        this.playerFireDoors = cap.playerFireDoors;
    }
}
