package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.ServerConfig;
import destiny.penumbra_phantasm.client.network.ClientBoundParticlePacket;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class DelayTicker {
    public static final int REAL_KNIFE_DELAY = 10;
    public static final int SWOON_DELAY = 55;

    UUID targetEntity;
    ItemStack weaponStack;
    int delayGoal;
    int delayTicker;

    public DelayTicker(UUID targetEntity, ItemStack weaponStack, int delayGoal, int delayTicker) {
        this.targetEntity = targetEntity;
        this.weaponStack = weaponStack;
        this.delayGoal = delayGoal;
        this.delayTicker = delayTicker;
    }

    public void tick(Level level, Player attacker) {
        Entity target = null;

        if (level instanceof ServerLevel serverLevel) {
            target = serverLevel.getEntity(targetEntity);
        }

        if (target == null) {
            delayTicker = -1;
            return;
        }

        if (weaponStack.getItem() == ItemRegistry.BLACK_KNIFE.get()) {
            target.setDeltaMovement(0, 0, 0);
        }

        if (delayTicker >= delayGoal) {
            if (weaponStack.getItem() == ItemRegistry.REAL_KNIFE.get()) {
                finishAttackWithRealKnife(level, attacker, target);
            } else if (weaponStack.getItem() == ItemRegistry.BLACK_KNIFE.get()) {
                finishAttackWithBlackKnife(level, target);
            }

            delayTicker = -1;
            return;
        }

        delayTicker++;
    }

    public void finishAttackWithRealKnife(Level level, Player attacker, Entity target) {
        level.playSound(null, attacker.getOnPos().above(), SoundRegistry.REAL_KNIFE_HIT.get(), SoundSource.PLAYERS, 0.5f, 1f);

        int addition = ServerConfig.realKnifeOP ? 5 : 0;
        int particleCount = level.random.nextInt(3, 6) + addition;

        for (int i = 0; i < particleCount; i++) {
            PacketHandlerRegistry.INSTANCE.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(),
                            32, target.level().dimension())),
                    new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.REAL_KNIFE_HIT.get()),
                            target.getX(), target.getY() + 1, target.getZ(),
                            -0.15 + level.random.nextDouble() * 0.3, 0.3, -0.15 + level.random.nextDouble() * 0.3, 1)
            );
        }

        int damage = ServerConfig.realKnifeOP ? Integer.MAX_VALUE : weaponStack.getMaxDamage();

        target.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.REAL_KNIFE), damage);
    }

    public void finishAttackWithBlackKnife(Level level, Entity target) {
        PacketHandlerRegistry.INSTANCE.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(),
                        32, target.level().dimension())),
                new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.SWOON_PARTICLE.get()),
                        target.getX(), target.getY() + 1, target.getZ(),
                        -0.15 + level.random.nextDouble() * 0.3, 0.3, -0.15 + level.random.nextDouble() * 0.3, 1)
        );

        target.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.SWOON), Integer.MAX_VALUE);
    }
}
