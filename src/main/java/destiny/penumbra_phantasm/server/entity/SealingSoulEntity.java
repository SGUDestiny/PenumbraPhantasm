package destiny.penumbra_phantasm.server.entity;

import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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

        if (!DarkWorldUtil.isDarkWorld(level)) {
            this.discard();
            return;
        }

        DarkFountainCapability darkFountainCapability = null;
        LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            darkFountainCapability = lazyCapability.resolve().get();

        if (darkFountainCapability == null){
            this.discard();
            return;
        }

        DarkFountain darkFountain = null;
        for(Map.Entry<BlockPos, DarkFountain> entry : darkFountainCapability.darkFountains.entrySet()) {
            DarkFountain entryFountain = entry.getValue();

            if(entryFountain.animationTimer > 125 || entryFountain.animationTimer == -1) {
                BlockPos fountainPos = entry.getValue().getFountainPos();
                Vec3 fountainPos2d = new Vec3(fountainPos.getX(), 0, fountainPos.getZ());
                Vec3 soulPos2d = new Vec3(this.position().x, 0, this.position().z);

                if (fountainPos2d.distanceTo(soulPos2d) < 16) {
                    darkFountain = entry.getValue();
                    break;
                }
            }
        }

        if (darkFountain == null){
            this.discard();
            return;
        }

        if (tick >= 7 * 20) {
            tick = 0;

            for (Player player : level.players()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ServerLevel destinationLevel = level.getServer().getLevel(darkFountain.destinationDimension);

                    if (destinationLevel == null) break;

                    Vec3 destinationPos = darkFountain.destinationPos.getCenter();

                    serverPlayer.teleportTo(destinationLevel, destinationPos.x, destinationPos.y,
                            destinationPos.z, player.getYHeadRot(), player.getXRot());
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
                }
            }

            //Send packets here
            this.discard();
        } else {
            if (tick == 0) {
                level.playSound(null, this.blockPosition(), SoundRegistry.GREAT_SHINE.get(), SoundSource.AMBIENT, 1, 1);
            }
            if (tick == 4 * 20 && !level.isClientSide) {
                level.players().forEach(player -> {
                    player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
                        cap.sealShineTicker = 0;
                        if (player instanceof ServerPlayer serverPlayer) {
                            cap.syncToClient(serverPlayer);
                        }
                    });
                    level.playSound(null, player.getOnPos().above(), SoundRegistry.FOUNTAIN_SEAL.get(), SoundSource.AMBIENT, 1f, 1f);
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