package destiny.penumbra_phantasm.server.entity;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DEVICE_FRIEND extends Entity {
    public static final EntityDataAccessor<Integer> ANIMATION_TICKER_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DISAPPEAR_SILENT_TICKER_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DISAPPEAR_LAUGH_TICKER_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);

    public static final String ANIMATION_TICKER = "animationTicker";
    public static final String DISAPPEAR_SILENT_TICKER = "disappearSilentTicker";
    public static final String DISAPPEAR_LAUGH_TICKER = "disappearLaughTicker";

    public int animationTicker;
    public int disappearSilentTicker;
    public int disappearLaughTicker;

    public DEVICE_FRIEND(EntityType<DEVICE_FRIEND> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
        this.setInvulnerable(true);
        this.setDisappearSilentTicker(-1);
        this.setDisappearLaughTicker(-1);
    }

    @Override
    public void tick() {
        Level level = this.level();

        if (level.isClientSide()) return;

        if (animationTicker >= 10) {
            animationTicker = 0;
        } else {
            animationTicker++;
        }

        if (disappearSilentTicker >= 20) {
            this.discard();
        } else if (disappearSilentTicker > -1) {
            disappearSilentTicker++;
        }

        if (disappearLaughTicker == 0) {
            level.playSound(null, this.blockPosition(), SoundRegistry.DEVICE_FRIEND_LAUGH.get(), SoundSource.HOSTILE, 0.5f, 1f);
        }
        if (disappearLaughTicker >= 40) {
            this.discard();
        } else if (disappearLaughTicker > -1) {
            disappearLaughTicker++;
        }

        System.out.println("---");
        System.out.println(disappearSilentTicker);
        System.out.println(disappearLaughTicker);

        this.entityData.set(ANIMATION_TICKER_ENTITY_DATA, animationTicker);
        this.entityData.set(DISAPPEAR_SILENT_TICKER_ENTITY_DATA, disappearSilentTicker);
        this.entityData.set(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA, disappearLaughTicker);
    }

/*    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        Level level = pPlayer.level();

        if (level.isClientSide()) return super.interact(pPlayer, pHand);

        this.entityData.set(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA, 0);

        return super.interact(pPlayer, pHand);
    }*/

    public void setDisappearSilentTicker(int disappearSilentTicker) {
        this.disappearSilentTicker = disappearSilentTicker;
        this.entityData.set(DISAPPEAR_SILENT_TICKER_ENTITY_DATA, disappearSilentTicker);
    }

    public void setDisappearLaughTicker(int disappearLaughTicker) {
        this.disappearLaughTicker = disappearLaughTicker;
        this.entityData.set(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA, disappearLaughTicker);
    }

    public int getAnimationTicker() {
        return this.entityData.get(ANIMATION_TICKER_ENTITY_DATA);
    }

    public int getDisappearSilentTicker() {
        return this.entityData.get(DISAPPEAR_SILENT_TICKER_ENTITY_DATA);
    }

    public int getDisappearLaughTicker() {
        return this.entityData.get(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ANIMATION_TICKER_ENTITY_DATA, 0);
        this.entityData.define(DISAPPEAR_SILENT_TICKER_ENTITY_DATA, -10);
        this.entityData.define(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA, -10);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.animationTicker = tag.getInt(ANIMATION_TICKER);
        this.disappearSilentTicker = tag.getInt(DISAPPEAR_SILENT_TICKER);
        this.disappearLaughTicker = tag.getInt(DISAPPEAR_LAUGH_TICKER);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt(ANIMATION_TICKER, animationTicker);
        tag.putInt(DISAPPEAR_SILENT_TICKER, disappearSilentTicker);
        tag.putInt(DISAPPEAR_LAUGH_TICKER, disappearLaughTicker);
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

    //Without play
    //          the knife
    //                        grows
    //                                         dull.
}