package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record FireDoor(ResourceKey<Level> darkWorld, BlockPos doorPos, float facingAngle, String name) {
    public static final String DARk_WORLD = "dark_world";
    public static final String DOOR_POS = "door_pos";
    public static final String FACING_ANGLE = "facing_angle";
    public static final String NAME = "name";

    public CompoundTag saveDoor() {
        CompoundTag tag = new CompoundTag();

        tag.putString(DARk_WORLD, darkWorld.location().toString());
        tag.put(DOOR_POS, NbtUtils.writeBlockPos(doorPos));
        tag.putFloat(FACING_ANGLE, facingAngle);
        tag.putString(NAME, name);

        return tag;
    }

    public static FireDoor loadDoor(CompoundTag tag) {
        ResourceKey<Level> darkWorld = ModUtil.stringToDimension(tag.getString(DARk_WORLD));
        BlockPos doorPos = NbtUtils.readBlockPos(tag.getCompound(DOOR_POS));
        float facingAngle = tag.getFloat(FACING_ANGLE);
        String name = tag.getString(NAME);

        return new FireDoor(darkWorld, doorPos, facingAngle, name);
    }
}