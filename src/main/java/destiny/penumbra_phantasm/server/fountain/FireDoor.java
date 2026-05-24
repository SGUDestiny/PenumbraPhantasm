package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record FireDoor(ResourceKey<Level> darkWorld, BlockPos doorPos, float facingAngle, Component name) {
    public static final String DARk_WORLD = "dark_world";
    public static final String DOOR_POS = "door_pos";
    public static final String FACING_ANGLE = "facing_angle";
    public static final String NAME = "name";

    public CompoundTag saveDoor() {
        CompoundTag tag = new CompoundTag();

        tag.putString(DARk_WORLD, darkWorld.location().toString());
        tag.put(DOOR_POS, NbtUtils.writeBlockPos(doorPos));
        tag.putFloat(FACING_ANGLE, facingAngle);
        tag.putString(NAME, Component.Serializer.toJson(name));

        return tag;
    }

    public static FireDoor loadDoor(CompoundTag tag) {
        ResourceKey<Level> darkWorld = ModUtil.stringToDimension(tag.getString(DARk_WORLD));
        BlockPos doorPos = NbtUtils.readBlockPos(tag.getCompound(DOOR_POS));
        float facingAngle = tag.getFloat(FACING_ANGLE);

        Component name;
        try {
            name = Component.Serializer.fromJson(tag.getString(NAME));
        } catch (Exception e) {
            name = Component.translatable("block.penumbra_phantasm.fire_door");
        }

        return new FireDoor(darkWorld, doorPos, facingAngle, name);
    }
}