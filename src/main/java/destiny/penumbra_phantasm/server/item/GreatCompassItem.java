package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.client.network.ClientBoundTextBoxPacket;
import destiny.penumbra_phantasm.server.block.entity.GreatDoorShapeBlockEntity;
import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

public class GreatCompassItem extends Item implements Vanishable {
    public static final String TAG_GREAT_DOOR_POS = "GreatDoorPos";
    public static final String TAG_GREAT_DOOR_DIMENSION = "GreatDoorDimension";
    public static final String TAG_IS_BOUND = "IsBound";

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

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.get(TAG_IS_BOUND) == null || !tag.getBoolean(TAG_IS_BOUND) || !pPlayer.isCrouching()) {
           return super.use(pLevel, pPlayer, pUsedHand);
        } else {
            tag.putBoolean(TAG_IS_BOUND, false);

            if (!pLevel.isClientSide()) {
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.GREAT_COMPASS_UNBOUND));
                }
            }

            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();

        if (!level.getBlockState(clickedPos).is(BlockRegistry.GREAT_DOOR_SHAPE.get())) {
            return super.useOn(context);
        } else {
            Player player = context.getPlayer();
            ItemStack handItem = context.getItemInHand();

            addGreatDoorTags(level, player, clickedPos, handItem);

            level.playSound(null, clickedPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1, 1);

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    public void addGreatDoorTags(Level level, Player player, BlockPos clickedPos, ItemStack handItem) {
        if (!level.isClientSide()) {
            CompoundTag tag = handItem.getOrCreateTag();

            tag.putBoolean(TAG_IS_BOUND, true);

            BlockPos greatDoorPos = null;
            if (level.getBlockEntity(clickedPos) instanceof GreatDoorShapeBlockEntity shape) {
                greatDoorPos = shape.greatDoorPos;
            }

            GreatDoorCapability greatDoorCapability = level.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().orElse(null);

            if (greatDoorPos != null && greatDoorCapability != null) {
                if (greatDoorCapability.greatDoors.get(greatDoorPos) != null) {
                    tag.put(TAG_GREAT_DOOR_POS, NbtUtils.writeBlockPos(greatDoorPos));
                    tag.putString(TAG_GREAT_DOOR_DIMENSION, level.dimension().location().toString());
                }
            }

            if (player instanceof ServerPlayer serverPlayer) {
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.GREAT_COMPASS_BOUND));
            }
        }
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
        if (!pLevel.isClientSide) {
            if (pLevel.getGameTime() % 20 == 0) {
                CompoundTag tag = pStack.getOrCreateTag();
                ServerLevel serverLevel = (ServerLevel) pLevel;
                GreatDoorCapability greatDoorCapability = pLevel.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().orElse(null);

                if (greatDoorCapability == null || greatDoorCapability.greatDoors.isEmpty()) return;

                if (tag.get(TAG_GREAT_DOOR_POS) == null && tag.get(TAG_GREAT_DOOR_DIMENSION) == null) {
                    List<BlockPos> greatDoorsByDistance = new ArrayList<>();

                    for (Map.Entry<BlockPos, GreatDoor> entry : greatDoorCapability.greatDoors.entrySet()) {
                        GreatDoor greatDoor = entry.getValue();

                        if (DarkWorldUtil.isLightDoorValidForFountain(serverLevel, greatDoor.lightDoorPos, greatDoor.lightDoorDimension)) {
                            greatDoorsByDistance.add(entry.getKey());
                        }
                    }

                    greatDoorsByDistance.sort((Comparator.comparingDouble(pos -> pos.getCenter().distanceTo(pEntity.position()))));
                    BlockPos closestGreatDoorPos = greatDoorsByDistance.get(0);

                    ResourceKey<Level> greatDoorDimension = pEntity.level().dimension();

                    tag.put(TAG_GREAT_DOOR_POS, NbtUtils.writeBlockPos(closestGreatDoorPos));
                    tag.putString(TAG_GREAT_DOOR_DIMENSION, greatDoorDimension.location().toString());
                } else {
                    ResourceKey<Level> doorLevelKey = ModUtil.stringToDimension(tag.getString(TAG_GREAT_DOOR_DIMENSION));
                    boolean isBound = tag.getBoolean(TAG_IS_BOUND);

                    if (!isBound && (doorLevelKey == null || !DarkWorldUtil.isDarkWorldKey(doorLevelKey) || pEntity.level().dimension() != doorLevelKey)) {
                        tag.remove(TAG_GREAT_DOOR_POS);
                        tag.remove(TAG_GREAT_DOOR_DIMENSION);

                        return;
                    }

                    BlockPos doorPos = NbtUtils.readBlockPos(tag.getCompound(TAG_GREAT_DOOR_POS));
                    GreatDoor savedDoor = greatDoorCapability.greatDoors.get(doorPos);

                    if (!DarkWorldUtil.isLightDoorValidForFountain(serverLevel, savedDoor.lightDoorPos, savedDoor.lightDoorDimension)) {
                        tag.remove(TAG_GREAT_DOOR_POS);
                        tag.remove(TAG_GREAT_DOOR_DIMENSION);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
