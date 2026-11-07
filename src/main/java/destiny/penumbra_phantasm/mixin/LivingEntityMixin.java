package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
	private void penumbraPhantasm$blockEggRoomSprint(boolean sprinting, CallbackInfo ci) {
		if (!sprinting) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player && CardKingdomEggRoomUtil.isEggRoom(self.level())) {
			ci.cancel();
		}
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void penumbraPhantasm$depthsWaterlikeTravel(Vec3 input, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof Player player)) return;
		if (!DarkWorldUtil.isDepths(player.level())) return;
		if (!player.isControlledByLocalInstance()) return;
		if (player.getAbilities().flying || player.isSpectator() || player.isPassenger()) return;
		if (player.isFallFlying()) return;
		if (player.isInWater() || player.isInLava() || player.isInFluidType()) return;

		double gravity = player.isCrouching() ? 0.75 : 0.35;
		if (player.getDeltaMovement().y <= 0 && player.hasEffect(MobEffects.SLOW_FALLING)) {
			gravity = 0.01;
			player.resetFallDistance();
		}

		float friction = 0.8F;
		float speed = 0.05F;
		player.moveRelative(speed, input);
		player.move(MoverType.SELF, player.getDeltaMovement());

		Vec3 delta = player.getDeltaMovement();
		if (player.horizontalCollision && player.onClimbable()) {
			delta = new Vec3(delta.x, 0.2, delta.z);
		}
		delta = delta.multiply(friction, 0.8, friction);
		if (!player.isNoGravity()) {
			double y = delta.y - gravity / 16;
			delta = new Vec3(delta.x, y, delta.z);
		}
		player.setDeltaMovement(delta);

		player.calculateEntityAnimation(false);
		ci.cancel();
	}

	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
	private void test(Level instance, Entity pEntity, byte pState) {
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

	@Redirect(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isBlocking()Z"))
	private boolean setShieldUseDelay(LivingEntity instance) {
		ItemStack shieldItem = instance.getUseItem();

		if (shieldItem.getItem() == ItemRegistry.DELTA_SHIELD.get()) {
            return instance.isUsingItem() && !shieldItem.isEmpty();
		} else {
			if (instance.isUsingItem() && !shieldItem.isEmpty()) {
				if (!shieldItem.canPerformAction(ToolActions.SHIELD_BLOCK)) {
					return false;
				} else {
					return shieldItem.getUseDuration() - instance.useItemRemaining >= 5;
				}
			} else {
				return false;
			}
		}
	}
}
