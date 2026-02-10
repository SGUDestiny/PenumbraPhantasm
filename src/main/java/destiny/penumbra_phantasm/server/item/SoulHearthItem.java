package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.SoulType;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

//TODO:
// - Transition soul hearth stuff to the capability

public class SoulHearthItem extends Item {
    public static final String OWNER_UUID = "owner_uuid";
    public static final String SOUL_TYPE = "soul_type";
    public static final String DETERMINATION = "determination";
    public static final String TRUST_LEVEL = "trust_level";

    public UUID ownerUuid = null;
    public int soulType = 1;
    public float determination = 0;
    public int trustLevel = 0;

    public SoulHearthItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private static final HumanoidModel.ArmPose POSE = HumanoidModel.ArmPose.create("POSE", false, (model, entity, arm) -> {
                if (arm == HumanoidArm.RIGHT) {
                    model.rightArm.xRot = 4.8f + entity.getXRot() / 90;
                    model.rightArm.yRot = Mth.clamp(ModUtil.wrapRad(0F + model.head.yRot), -0.5f, 1);
                } else {
                    model.leftArm.xRot = 4.8f + entity.getXRot() / 90;
                    model.leftArm.yRot = Mth.clamp(ModUtil.wrapRad(0F + model.head.yRot), -0.5f, 1);
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;

        if (entity instanceof Player player) {
            if (!player.getCapability(CapabilityRegistry.SOUL).isPresent()) return;

            if (stack.getTag() == null || stack.getTag().get(OWNER_UUID) == null) {
                stack.getOrCreateTag().putUUID(OWNER_UUID, player.getUUID());

                player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> soulType = cap.soulType);
                stack.getOrCreateTag().putInt(SOUL_TYPE, soulType);
                stack.getOrCreateTag().putFloat(DETERMINATION, determination);
                stack.getOrCreateTag().putInt(TRUST_LEVEL, trustLevel);
            }

            UUID ownerUuid = stack.getTag().getUUID(OWNER_UUID);
            if (player.getUUID().equals(ownerUuid)) {
                float determination = stack.getTag().getFloat(DETERMINATION);

                if (determination < 1) {
                    if (level.getGameTime() % (5 * 20) == 0) {
                        determination = determination + 0.01f;
                    }
                }

                stack.getOrCreateTag().putFloat(DETERMINATION, determination);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isAdvanced) {
        if (stack.getTag() == null) return;

        UUID ownerUuid = stack.getTag().getUUID(OWNER_UUID);
        if (!Minecraft.getInstance().player.getUUID().equals(ownerUuid)) {
            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.not_owner")
                            .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
        } else {
            int soulType = stack.getTag().getInt(SOUL_TYPE);
            int determination = (int) (100 * stack.getTag().getFloat(DETERMINATION));
            int trustLevel = stack.getTag().getInt(TRUST_LEVEL);

            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.soul_type")
                    .append(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.soul_type." + soulType))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
            );
            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.determination")
                    .append(Component.literal(determination + "%"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
            );
            components.add(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.trust_level")
                    .append(Component.literal(trustLevel + ""))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
            );
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
