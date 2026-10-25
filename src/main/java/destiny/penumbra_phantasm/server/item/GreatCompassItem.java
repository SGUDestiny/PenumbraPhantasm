package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

public class GreatCompassItem extends Item implements Vanishable {
    public static final String TAG_GREAT_DOOR_POS = "GreatDoorPos";
    public static final String TAG_GREAT_DOOR_DIMENSION = "GreatDoorDimension";

    public GreatCompassItem(Properties pProperties) {
        super(pProperties);
    }

    private static Optional<ResourceKey<Level>> getGreatDoorDimension(CompoundTag pCompoundTag) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, pCompoundTag.get(TAG_GREAT_DOOR_DIMENSION)).result();
    }

    @Nullable
    public static GlobalPos getGreatDoorPosition(CompoundTag pTag) {
        boolean isPosPresent = pTag.contains(TAG_GREAT_DOOR_POS);
        boolean isDimensionPresent = pTag.contains(TAG_GREAT_DOOR_DIMENSION);

        if (isPosPresent && isDimensionPresent) {
            Optional<ResourceKey<Level>> dimension = getGreatDoorDimension(pTag);

            if (dimension.isPresent()) {
                BlockPos greatDoorPos = NbtUtils.readBlockPos(pTag.getCompound(TAG_GREAT_DOOR_POS));

                return GlobalPos.of(dimension.get(), greatDoorPos);
            }
        }

        return null;
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
        if (!pLevel.isClientSide) {
            if (pLevel.getGameTime() % 20 == 0) {
                CompoundTag tag = pStack.getOrCreateTag();

                if (!DarkWorldUtil.isDarkWorld(pLevel)) {
                    tag.remove(TAG_GREAT_DOOR_POS);
                    tag.remove(TAG_GREAT_DOOR_DIMENSION);

                    return;
                }

                GreatDoorCapability greatDoorCapability = pLevel.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().orElse(null);

                if (greatDoorCapability == null) {
                    return;
                }

                List<BlockPos> greatDoorsByDistance = new ArrayList<>();
                for (Map.Entry<BlockPos, GreatDoor> entry : greatDoorCapability.greatDoors.entrySet()) {
                    greatDoorsByDistance.add(entry.getKey());
                }

                greatDoorsByDistance.sort((Comparator.comparingDouble(pos -> pos.getCenter().distanceTo(pEntity.position()))));
                BlockPos closestGreatDoorPos = greatDoorsByDistance.get(0);

                GreatDoor closestGreatDoor = greatDoorCapability.greatDoors.get(closestGreatDoorPos);
                ServerLevel serverLevel = (ServerLevel) pLevel;

                if (DarkWorldUtil.isLightDoorValidForFountain(serverLevel, closestGreatDoor.lightDoorPos, closestGreatDoor.lightDoorDimension)) {
                    ResourceKey<Level> greatDoorDimension = pEntity.level().dimension();

                    tag.put(TAG_GREAT_DOOR_POS, NbtUtils.writeBlockPos(closestGreatDoorPos));
                    tag.putString(TAG_GREAT_DOOR_DIMENSION, greatDoorDimension.location().toString());
                } else {
                    tag.remove(TAG_GREAT_DOOR_POS);
                    tag.remove(TAG_GREAT_DOOR_DIMENSION);
                }
            }
        }
    }
}
