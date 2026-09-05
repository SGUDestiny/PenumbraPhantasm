package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundTextBoxPacket;
import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.entity.SealingSoulEntity;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.EntityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SoulHearthItem extends Item {
    public static final String OWNER_UUID = "owner_uuid";
    public static final String CURRENT_UUID = "current_uuid";
    public static final String SOUL_TYPE = "soul_type";

    public int soulType = 1;

    public SoulHearthItem(Properties pProperties) {
        super(pProperties);
    }

    public static boolean isOwnedBy(ItemStack stack, UUID playerUUID) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SoulHearthItem)) {
            return false;
        }

        CompoundTag tag = stack.getTag();

        return tag != null && tag.hasUUID(OWNER_UUID) && playerUUID.equals(tag.getUUID(OWNER_UUID));
    }

    public static boolean isHoldingOwn(Player player) {
        UUID playerUUID = player.getUUID();

        return isOwnedBy(player.getMainHandItem(), playerUUID) || isOwnedBy(player.getOffhandItem(), playerUUID);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private static final HumanoidModel.ArmPose POSE = HumanoidModel.ArmPose.create("POSE", false, (model, entity, arm) -> {
                if (arm == HumanoidArm.RIGHT) {
                    model.rightArm.xRot = 4.8f + entity.getXRot() / 90;
                    model.rightArm.yRot = Mth.clamp(ModUtil.wrapRad(0 + model.head.yRot), -0.5f, 1);
                } else {
                    model.leftArm.xRot = 4.8f + entity.getXRot() / 90;
                    model.leftArm.yRot = Mth.clamp(ModUtil.wrapRad(0 + model.head.yRot), -0.5f, 1);
                }
            });

            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (itemStack.getItem() instanceof SoulHearthItem) {
                    return POSE;
                }

                return IClientItemExtensions.super.getArmPose(entityLiving, hand, itemStack);
            }
        });
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getTag() == null) return InteractionResultHolder.fail(stack);
        if (!DarkWorldUtil.isDarkWorld(level)) return InteractionResultHolder.pass(stack);

        //Player rejection textbox
        UUID ownerUuid = stack.getTag().getUUID(OWNER_UUID);
        if (!player.getUUID().equals(ownerUuid)) {
            if (player instanceof ServerPlayer serverPlayer) {
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.SOUL_HEARTH_REJECT));
            }
            return InteractionResultHolder.fail(stack);
        }

        //Depths use textbox
        if (DarkWorldUtil.isDepths(level)) {
            if (player instanceof ServerPlayer serverPlayer) {
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_DEPTHS));
            }
            return InteractionResultHolder.pass(stack);
        }

        DarkFountainCapability darkFountainCapability = null;
        LazyOptional<DarkFountainCapability> darkLazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(darkLazyCapability.isPresent() && darkLazyCapability.resolve().isPresent())
            darkFountainCapability = darkLazyCapability.resolve().get();

        if (darkFountainCapability == null){
            return InteractionResultHolder.pass(stack);
        }

        DarkFountain darkFountain = null;
        for(Map.Entry<BlockPos, DarkFountain> entry : darkFountainCapability.darkFountains.entrySet()) {
            DarkFountain entryFountain = entry.getValue();

            if(entryFountain.openingTick > 125 || entryFountain.openingTick == -1) {
                BlockPos fountainPos = entry.getValue().getFountainPos();
                Vec3 fountainPos2d = new Vec3(fountainPos.getX(), 0, fountainPos.getZ());
                Vec3 playerPos2d = new Vec3(player.getX(), 0, player.getZ());

                if (fountainPos2d.distanceTo(playerPos2d) < 16) {
                    darkFountain = entry.getValue();
                    break;
                }
            }
        }

        if (darkFountain == null){
            return InteractionResultHolder.pass(stack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
            int determination = soulCap.determination;

            if (determination < 100) {
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_NOT_ENOUGH_DETERMINATION));
                return InteractionResultHolder.pass(stack);
            }

            if (darkFountain.sealingFrameTick >= 0) {
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_NOT_ENOUGH_DETERMINATION));
                return InteractionResultHolder.pass(stack);
            }

            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE));

            return InteractionResultHolder.pass(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;

        if (entity instanceof Player player) {
            SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
            int soulType = soulCap.soulType;

            if (stack.getTag() == null || stack.getTag().get(OWNER_UUID) == null) {
                stack.getOrCreateTag().putUUID(OWNER_UUID, player.getUUID());
                stack.getOrCreateTag().putInt(SOUL_TYPE, soulType);
            }

            if(!stack.getOrCreateTag().contains(CURRENT_UUID) || !stack.getOrCreateTag().getUUID(CURRENT_UUID).equals(player.getUUID()))
                stack.getOrCreateTag().putUUID(CURRENT_UUID, player.getUUID());

            if (stack.getTag().getInt(SOUL_TYPE) != soulType) {
                stack.getOrCreateTag().putInt(SOUL_TYPE, soulType);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag isAdvanced) {
        if (stack.getTag() == null || level == null) return;

        UUID ownerUuid = stack.getTag().getUUID(OWNER_UUID);
        UUID currentUUID = stack.getTag().getUUID(CURRENT_UUID);
        Player player = level.getPlayerByUUID(currentUUID);
        if(player == null)
            return;

        if (!player.getUUID().equals(ownerUuid)) {
            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.not_owner")
                            .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
        } else {
            SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);

            int soulType = soulCap.soulType;
            int determination = soulCap.determination;
            int connectionLevel = soulCap.connectionLevel;

            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.soul_type")
                    .append(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.soul_type." + soulType))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
            );
            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.determination")
                    .append(Component.literal(determination + "%"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
            );
            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.connection_level")
                    .append(Component.literal(connectionLevel + ""))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
            );
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
