package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.registry.DamageTypeRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void onGetHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        if (source.is(DamageTypeRegistry.SOUL_DAMAGE_1)) {
            ((Entity)(Object)this).playSound(SoundRegistry.SOUL_HURT.get(), 1F, 1F);
            cir.cancel();
        }

        if (source.is(DamageTypeRegistry.SOUL_DAMAGE_2)) {
            ((Entity)(Object)this).playSound(SoundRegistry.SOUL_SHATTER.get(), 1F, 1F);
            cir.cancel();
        }
    }

    @Redirect(method = "disableShield(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    private void disableShieldBreakSound(Level instance, Entity pEntity, byte pState) {
        if (!(pEntity instanceof LivingEntity livingEntity)) {
            instance.broadcastEntityEvent(pEntity, pState);
            return;
        }

        ItemStack shieldItem = livingEntity.getUseItem();

        if (shieldItem.getItem() == ItemRegistry.DELTA_SHIELD.get()) {
            instance.broadcastEntityEvent(pEntity, (byte) 1);
        } else {
            instance.broadcastEntityEvent(pEntity, pState);
        }
    }
}
