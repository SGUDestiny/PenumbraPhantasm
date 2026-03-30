package destiny.penumbra_phantasm.server.entity;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SealingSoulEntity extends Entity {
    public static final EntityDataAccessor<Integer> SOUL_TYPE_ENTITY_DATA = SynchedEntityData.defineId(SealingSoulEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> TICK_ENTITY_DATA = SynchedEntityData.defineId(SealingSoulEntity.class, EntityDataSerializers.INT);
    public static final String SOUL_TYPE = "soulType";
    public static final String TICK = "tick";

    public int soulType = 1;
    public int tick = 0;

    public SealingSoulEntity(EntityType<SealingSoulEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    public void tick() {
        Level level = this.level();

        if (tick >= 5 * 20) {
            tick = 0;

            //Send packets here
            this.discard();
        } else {
            if (tick == 0) {
                level.playSound(null, this.blockPosition(), SoundRegistry.GREAT_SHINE.get(), SoundSource.AMBIENT, 1, 1);
            }
            if (tick == 4 * 20 && !level.isClientSide) {
                level.players().forEach(player -> {
                    if (player.level().dimension() == level.dimension()) {
                        player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
                            cap.sealShineTicker = 0;
                            if (player instanceof ServerPlayer serverPlayer) {
                                cap.syncToClient(serverPlayer);
                            }
                        });
                        level.playSound(null, player.getOnPos().above(), SoundRegistry.FOUNTAIN_SEAL.get(), SoundSource.AMBIENT, 1f, 1f);
                    }
                });
            }

            tick++;
            this.entityData.set(TICK_ENTITY_DATA, tick);
        }
    }

    public void setSoulType(int soulType) {
        this.soulType = soulType;
        this.entityData.set(SOUL_TYPE_ENTITY_DATA, soulType);
    }

    public int getTick() {
        return this.entityData.get(TICK_ENTITY_DATA);
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SOUL_TYPE_ENTITY_DATA, soulType);
        this.entityData.define(TICK_ENTITY_DATA, tick);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.soulType = tag.getInt(SOUL_TYPE);
        this.tick = tag.getInt(TICK);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt(SOUL_TYPE, soulType);
        tag.putInt(TICK, tick);
    }
}