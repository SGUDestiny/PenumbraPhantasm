package destiny.penumbra_phantasm.server.entity;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SealingSoulEntity extends Entity {
    public static final EntityDataAccessor<Integer> SOUL_TYPE_ENTITY_DATA = SynchedEntityData.defineId(SealingSoulEntity.class, EntityDataSerializers.INT);
    public static final String SOUL_TYPE = "soulType";
    public static final String TICK = "tick";

    public int soulType;
    public int tick = 0;

    public SealingSoulEntity(EntityType<SealingSoulEntity> type, Level level, int soulType) {
        super(type, level);
        this.soulType = soulType;
    }

    @Override
    public void tick() {
        Level level = this.level();

        if (tick >= 100) {
            tick = 0;

            //Send packets here
            this.discard();
        } else {
            if (tick == 0) {
                level.playSound(null, this.blockPosition(), SoundRegistry.GREAT_SHINE.get(), SoundSource.AMBIENT, 1, 1);
            }

            tick++;
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SOUL_TYPE_ENTITY_DATA, soulType);
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
