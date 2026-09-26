package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.registry.DamageTypeRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
/*	@Shadow
	protected abstract boolean isAlwaysExperienceDropper();

	@Shadow
	@Nullable
	private DamageSource lastDamageSource;

	@Shadow
	@Nullable
	protected Player lastHurtByPlayer;

	@Shadow
	protected int lastHurtByPlayerTime;*/

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

/*	@Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
	private void dropExperience(CallbackInfo ci) {
		Level level = ((LivingEntity)(Object) this).level();
		Vec3 position = ((LivingEntity)(Object) this).position();
		boolean shouldDropExperience = ((LivingEntity)(Object) this).shouldDropExperience();
		boolean wasExperienceConsumed = ((LivingEntity)(Object) this).wasExperienceConsumed();

		if (this.lastDamageSource != null && (this.lastDamageSource.type() == DamageTypeRegistry.getSource(level, DamageTypeRegistry.REAL_KNIFE).type()
				|| this.lastDamageSource.type() == DamageTypeRegistry.getSource(level, DamageTypeRegistry.SWOON).type())) {
			if (level instanceof ServerLevel && wasExperienceConsumed && (this.isAlwaysExperienceDropper() || lastHurtByPlayerTime > 0 && shouldDropExperience && level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
				int reward = ForgeEventFactory.getExperienceDrop((LivingEntity)(Object) this, this.lastHurtByPlayer, ((LivingEntity)(Object) this).getExperienceReward());
				ExperienceOrb.award((ServerLevel)level, position, reward);

				ci.cancel();
			}
		}
	}*/
}
