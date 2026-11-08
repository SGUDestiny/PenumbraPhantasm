package destiny.penumbra_phantasm.server.entity;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class DEVICE_FRIEND extends Entity {
    public static final EntityDataAccessor<Integer> ANIMATION_TICKER_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> ANIMATION_HALFTIME_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DISAPPEAR_SILENT_TICKER_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DISAPPEAR_LAUGH_TICKER_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> FUN_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DELAY_TICKER_ENTITY_DATA = SynchedEntityData.defineId(DEVICE_FRIEND.class, EntityDataSerializers.INT);

    public static final String ANIMATION_TICKER = "animationTicker";
    public static final String ANIMATION_HALFTIME = "animationHalftime";
    public static final String DISAPPEAR_SILENT_TICKER = "disappearSilentTicker";
    public static final String DISAPPEAR_LAUGH_TICKER = "disappearLaughTicker";
    public static final String FUN = "fun";
    public static final String DELAY_TICKER = "delayTicker";

    public int animationTicker;
    public int animationHalftime;
    public int disappearSilentTicker;
    public int disappearLaughTicker;
    public int fun;
    public int delayTicker;

    public DEVICE_FRIEND(EntityType<DEVICE_FRIEND> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
        this.setInvulnerable(true);

        this.setAnimationTicker(0);
        this.setAnimationHalftime(5);
        this.setDisappearSilentTicker(-1);
        this.setDisappearLaughTicker(-1);
        this.setFun(1);
        this.setDelayTicker(-1);
    }

    @Override
    public void tick() {
        Level level = this.level();

        if (level.isClientSide()) return;

        fun(level);
        tickers(level);
    }

    public void fun(Level level) {
        ServerPlayer serverPlayer = (ServerPlayer) level.getNearestPlayer(this, 128);

        if (serverPlayer == null) return;

        if (fun == 1) {
            assignFun(level);
        } else {
            switch (fun) {
                case 4: {
                    if (disappearLaughTicker == -1 && isInViewOfAnyPlayer((ServerLevel) level, this.blockPosition(), 5f)
                            && isUnobstructedByBlocksToAnyPlayer((ServerLevel) level, this.blockPosition())) {
                        setDelayTicker(-1);
                        setDisappearLaughTicker(0);
                    }
                }
                case 5: {
                    if (disappearLaughTicker == -1 && isInViewOfAnyPlayer((ServerLevel) level, this.blockPosition(), 5f)
                            && isUnobstructedByBlocksToAnyPlayer((ServerLevel) level, this.blockPosition())) {
                        setDisappearLaughTicker(0);
                    }
                }
            }
        }
    }

    public void assignFun(Level level) {
        if (isInViewOfAnyPlayer((ServerLevel) level, this.blockPosition(), 90)) {
            int funRoll = 2;
            for (int i = 0; i < 3; i++) {
                if (level.getRandom().nextFloat() > 0.5f) break;

                funRoll++;
            }

            switch (funRoll) {
                case 2: {
                    setDisappearSilentTicker(0);
                }
                case 3: {
                    setDelayTicker(level.random.nextInt(20, 40));
                }
                case 4: {
                    setDelayTicker(level.random.nextInt(40, 70));
                }
            }

            setFun(funRoll);
        }
    }

    public void tickers(Level level) {
        if (fun > 1) {
            if (delayTicker > 0) {
                delayTicker--;
            } else if (delayTicker == 0) {
                delayTicker = -1;

                switch (fun) {
                    case 3: {
                        setDisappearSilentTicker(0);
                    }
                    case 4: {
                        setDisappearSilentTicker(0);
                    }
                }
            }
        }

        if (animationTicker >= animationHalftime * 2) {
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
            ServerPlayer serverPlayer = (ServerPlayer) level.getNearestPlayer(this, 128);

            if (serverPlayer == null) return;

            this.setAnimationHalftime(2);
            level.playSound(null, this.blockPosition(), SoundRegistry.DEVICE_FRIEND_LAUGH.get(), SoundSource.HOSTILE, 100f, 1f);
        }
        if (disappearLaughTicker >= 40) {
            this.discard();
        } else if (disappearLaughTicker > -1) {
            disappearLaughTicker++;
        }

        setDelayTicker(delayTicker);
        setAnimationTicker(animationTicker);
        setAnimationHalftime(animationHalftime);
        setDisappearSilentTicker(disappearSilentTicker);
        setDisappearLaughTicker(disappearLaughTicker);
    }

    public boolean isInViewOfAnyPlayer(ServerLevel serverLevel, BlockPos pos, float fovDegrees) {
        AABB box = new AABB(pos).inflate(128);

        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (CustomEntitySpawning.isInPlayerView(player, pos, fovDegrees)) {
                return true;
            }
        }

        return false;
    }

    public boolean isUnobstructedByBlocksToAnyPlayer(ServerLevel serverLevel, BlockPos pos) {
        AABB box = new AABB(pos).inflate(128);

        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (CustomEntitySpawning.isUnobstructedByBlocks(serverLevel, player, pos)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        this.setDisappearLaughTicker(0);

        return super.interact(pPlayer, pHand);
    }

    public void setAnimationTicker(int animationTicker) {
        this.animationTicker = animationTicker;
        this.entityData.set(ANIMATION_TICKER_ENTITY_DATA, animationTicker);
    }

    public void setAnimationHalftime(int animationHalftime) {
        this.animationHalftime = animationHalftime;
        this.entityData.set(ANIMATION_HALFTIME_ENTITY_DATA, animationHalftime);
    }

    public void setDisappearSilentTicker(int disappearSilentTicker) {
        this.disappearSilentTicker = disappearSilentTicker;
        this.entityData.set(DISAPPEAR_SILENT_TICKER_ENTITY_DATA, disappearSilentTicker);
    }

    public void setDisappearLaughTicker(int disappearLaughTicker) {
        this.disappearLaughTicker = disappearLaughTicker;
        this.entityData.set(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA, disappearLaughTicker);
    }

    public void setFun(int fun) {
        this.fun = fun;
        this.entityData.set(FUN_ENTITY_DATA, fun);
    }

    public void setDelayTicker(int delayTicker) {
        this.delayTicker = delayTicker;
        this.entityData.set(DELAY_TICKER_ENTITY_DATA, delayTicker);
    }

    public int getAnimationTicker() {
        return this.entityData.get(ANIMATION_TICKER_ENTITY_DATA);
    }

    public int getAnimationHalftime() {
        return this.entityData.get(ANIMATION_HALFTIME_ENTITY_DATA);
    }

    public int getDisappearSilentTicker() {
        return this.entityData.get(DISAPPEAR_SILENT_TICKER_ENTITY_DATA);
    }

    public int getDisappearLaughTicker() {
        return this.entityData.get(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA);
    }

    public int getFun() {
        return this.entityData.get(FUN_ENTITY_DATA);
    }

    public int getDelayTicker() {
        return this.entityData.get(DELAY_TICKER_ENTITY_DATA);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ANIMATION_TICKER_ENTITY_DATA, 0);
        this.entityData.define(ANIMATION_HALFTIME_ENTITY_DATA, 5);
        this.entityData.define(DISAPPEAR_SILENT_TICKER_ENTITY_DATA, -1);
        this.entityData.define(DISAPPEAR_LAUGH_TICKER_ENTITY_DATA, -1);
        this.entityData.define(FUN_ENTITY_DATA, 1);
        this.entityData.define(DELAY_TICKER_ENTITY_DATA, -1);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        if (tag.contains(ANIMATION_TICKER)) {
            this.animationTicker = tag.getInt(ANIMATION_TICKER);
        }
        if (tag.contains(ANIMATION_HALFTIME)) {
            this.animationHalftime = tag.getInt(ANIMATION_HALFTIME);
        }
        if (tag.contains(DISAPPEAR_SILENT_TICKER)) {
            this.disappearSilentTicker = tag.getInt(DISAPPEAR_SILENT_TICKER);
        }
        if (tag.contains(DISAPPEAR_LAUGH_TICKER)) {
            this.disappearLaughTicker = tag.getInt(DISAPPEAR_LAUGH_TICKER);
        }
        if (tag.contains(FUN)) {
            this.fun = tag.getInt(FUN);
        }
        if (tag.contains(DELAY_TICKER)) {
            this.delayTicker = tag.getInt(DELAY_TICKER);
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt(ANIMATION_TICKER, animationTicker);
        tag.putInt(ANIMATION_HALFTIME, animationHalftime);
        tag.putInt(DISAPPEAR_SILENT_TICKER, disappearSilentTicker);
        tag.putInt(DISAPPEAR_LAUGH_TICKER, disappearLaughTicker);
        tag.putInt(FUN, fun);
        tag.putInt(DELAY_TICKER, delayTicker);
    }

    @Override
    public boolean isPickable() {
        return true;
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
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    //Without play
    //          the knife
    //                        grows
    //                                         dull.
}