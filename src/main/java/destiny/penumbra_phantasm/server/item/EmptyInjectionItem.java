package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.DamageTypeRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EmptyInjectionItem extends Item {
    public EmptyInjectionItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        Level level = pPlayer.level();
        if (level.isClientSide()) return InteractionResult.FAIL;

        if (pUsedHand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        if (!(pInteractionTarget instanceof Player targetPlayer)) return InteractionResult.FAIL;

        if(pStack.getItem() == ItemRegistry.EMPTY_INJECTION.get()) {
            SoulCapability soulCap = targetPlayer.getCapability(CapabilityRegistry.SOUL).orElse(null);

            if (soulCap.determination < 100) return InteractionResult.FAIL;

            targetPlayer.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.INJECTION_DRAIN), targetPlayer.getMaxHealth() / 2);
            soulCap.determination = 0;

            level.playSound(null, targetPlayer.getOnPos(), SoundEvents.PLAYER_BIG_FALL, SoundSource.PLAYERS, 1, 1);

            if (pStack.getCount() > 1) {
                pStack.shrink(1);
                pPlayer.addItem(new ItemStack(ItemRegistry.DETERMINATION_INJECTION.get()));
            } else {
                pPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.DETERMINATION_INJECTION.get()));
            }

            pPlayer.getCooldowns().addCooldown(pStack.getItem(), 20);

            TriggerCriterions.DETERMINATION_INJECTION_STEAL.trigger((ServerPlayer) pPlayer);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.fail(stack);

        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.fail(stack);

        SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
        if (soulCap.determination < 100) return InteractionResultHolder.fail(stack);

        if (player.getHealth() < player.getMaxHealth() / 2) {
            TriggerCriterions.DETERMINATION_INJECTION_DEATH.trigger((ServerPlayer) player);
        }

        player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.INJECTION_DRAIN), player.getMaxHealth() / 2);
        soulCap.determination = 0;

        level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_BIG_FALL, SoundSource.PLAYERS, 1, 1);

        if (stack.getCount() > 1) {
            stack.shrink(1);
            player.addItem(new ItemStack(ItemRegistry.DETERMINATION_INJECTION.get()));
        } else {
            player.setItemInHand(hand, new ItemStack(ItemRegistry.DETERMINATION_INJECTION.get()));
        }

        player.getCooldowns().addCooldown(stack.getItem(), 20);

        return InteractionResultHolder.success(stack);
    }
}
