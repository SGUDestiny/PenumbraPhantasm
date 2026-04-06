package destiny.penumbra_phantasm.server.fountain;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

public class GreatDoor {
    public static final String GREAT_DOOR_POS = "greatDoorPos";
    public static final String PARENT_FOUNTAIN = "parentFountain";
    public static final String IS_OPEN = "isOpen";
    public static final String DESTINATION_DOOR_POS = "destinationDoorPos";
    public static final String DESTINATION_FOUNTAIN = "destinationFountain";

    public BlockPos greatDoorPos;
    public DarkFountain parentFountain;
    public boolean isOpen;
    public BlockPos destinationDoorPos;
    public DarkFountain destinationFountain;

    public GreatDoor(BlockPos greatDoorPos, DarkFountain parentFountain, boolean isOpen, BlockPos destinationDoorPos, DarkFountain destinationFountain) {
        this.greatDoorPos = greatDoorPos;
        this.parentFountain = parentFountain;
        this.isOpen = isOpen;
        this.destinationDoorPos = destinationDoorPos;
        this.destinationFountain = destinationFountain;
    }

    public void tick() {

    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.put(GREAT_DOOR_POS, NbtUtils.writeBlockPos(greatDoorPos));

        return tag;
    }

    public static GreatDoor load(CompoundTag tag) {
        BlockPos greatDoorPos = NbtUtils.readBlockPos(tag.getCompound(GREAT_DOOR_POS));
        DarkFountain parentFountain = DarkFountain.load(tag.getCompound(PARENT_FOUNTAIN));
        boolean isOpen = tag.getBoolean(IS_OPEN);
        BlockPos destinationDoorPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_DOOR_POS));
        DarkFountain destinationFountain = DarkFountain.load(tag.getCompound(DESTINATION_FOUNTAIN));

        return new GreatDoor(greatDoorPos, parentFountain, isOpen, destinationDoorPos, destinationFountain);
    }

    public void sync(GreatDoor greatDoor) {
        this.greatDoorPos = greatDoor.greatDoorPos;
        this.parentFountain = greatDoor.parentFountain;
        this.isOpen = greatDoor.isOpen;
        this.destinationDoorPos = greatDoor.destinationDoorPos;
        this.destinationFountain = greatDoor.destinationFountain;
    }
}
