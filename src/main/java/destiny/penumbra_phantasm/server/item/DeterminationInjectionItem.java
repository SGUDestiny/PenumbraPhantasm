package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundCancelPlayerAnimationPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundPlayPlayerAnimationPacket;
import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.DamageTypeRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class DeterminationInjectionItem extends Item {
    public static final int ANIMATION_STAB_FRAME = 20;
    public static final int ANIMATION_LENGTH = (int) (1.75 * 20);

    public static final String TICKER = "ticker";

    public DeterminationInjectionItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        Level level = pPlayer.level();
        if (level.isClientSide()) return InteractionResult.FAIL;

        if (pUsedHand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        if (!(pInteractionTarget instanceof Player targetPlayer)) return InteractionResult.FAIL;

        if(pStack.getItem() == ItemRegistry.DETERMINATION_INJECTION.get()) {
            SoulCapability soulCap = targetPlayer.getCapability(CapabilityRegistry.SOUL).orElse(null);

            if (soulCap.determination >= 100) {
                TriggerCriterions.DETERMINATION_INJECTION_CAUSE_OVERDOSE.trigger((ServerPlayer) pPlayer);

                targetPlayer.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.INJECTION_OVERDOSE), targetPlayer.getMaxHealth());
            } else {
                targetPlayer.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.INJECTION_PRICK), targetPlayer.getMaxHealth() / 2);
                soulCap.determination = 100;
            }

            level.playSound(null, targetPlayer.getOnPos(), SoundEvents.PLAYER_BIG_FALL, SoundSource.PLAYERS, 1, 1);

            if (!pPlayer.isCreative()) {
                pPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.EMPTY_INJECTION.get()));
            }

            pPlayer.getCooldowns().addCooldown(pStack.getItem(), 20);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.fail(stack);

        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.fail(stack);

        stack.getTag().putInt(TICKER, 0);
        player.getCooldowns().addCooldown(stack.getItem(), ANIMATION_LENGTH);

        String animationName = getAnimationNameForPlayer(player);

        PacketHandlerRegistry.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new ClientBoundPlayPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, animationName))
        );

        return InteractionResultHolder.success(stack);
    }

    public String getAnimationNameForPlayer(Player player) {
        if (player.getMainArm() == HumanoidArm.RIGHT) {
            return "injection_use";
        } else {
            return "injection_use_alt";
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;

        if (!(entity instanceof Player player)) return;

        if (stack.getTag() == null) {
            stack.getOrCreateTag().putInt(TICKER, -1);
        }

        int ticker = stack.getTag().getInt(TICKER);

        if (ticker > -1) {
            if (!isSelected) {
                stack.getTag().putInt(TICKER, -1);

                String animationName = getAnimationNameForPlayer(player);

                PacketHandlerRegistry.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new ClientBoundCancelPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, animationName))
                );
                return;
            }

            if (ticker < ANIMATION_STAB_FRAME) {
                stack.getTag().putInt(TICKER, ticker + 1);
            } else if (ticker == ANIMATION_STAB_FRAME) {
                stack.getTag().putInt(TICKER, -1);

                SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);

                if (soulCap.determination >= 100) {
                    TriggerCriterions.DETERMINATION_INJECTION_OVERDOSE.trigger((ServerPlayer) player);

                    player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.INJECTION_OVERDOSE), player.getMaxHealth());
                } else {
                    soulCap.determination = 100;

                    if (!player.isCreative()) {
                        if (player.getHealth() < player.getMaxHealth() / 2) {
                            TriggerCriterions.DETERMINATION_INJECTION_DEATH.trigger((ServerPlayer) player);
                        }

                        player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.INJECTION_PRICK), player.getMaxHealth() / 2);
                    }
                }

                TriggerCriterions.DETERMINATION_INJECTION.trigger((ServerPlayer) player);

                level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_BIG_FALL, SoundSource.PLAYERS, 1, 1);

                if (!player.isCreative()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.EMPTY_INJECTION.get()));
                }
            }
        }
    }
}
