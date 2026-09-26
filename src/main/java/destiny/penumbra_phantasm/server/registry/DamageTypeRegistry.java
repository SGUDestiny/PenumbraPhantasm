package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DamageTypeRegistry {
    public static final ResourceKey<DamageType> INJECTION_PRICK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "injection_prick"));
    public static final ResourceKey<DamageType> INJECTION_DRAIN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "injection_drain"));
    public static final ResourceKey<DamageType> INJECTION_OVERDOSE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "injection_overdose"));
    public static final ResourceKey<DamageType> SOUL_DAMAGE_1 = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "soul_damage_1"));
    public static final ResourceKey<DamageType> SOUL_DAMAGE_2 = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "soul_damage_2"));
    public static final ResourceKey<DamageType> PETRIFICATION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "petrification"));
    public static final ResourceKey<DamageType> SWOON = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "swoon"));
    public static final ResourceKey<DamageType> REAL_KNIFE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "real_knife"));

    public static DamageSource getSource(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
    }

    public static DamageSource getSourceUnattributed(Level level, ResourceKey<DamageType> type, @Nullable Entity entity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), entity);
    }

    public static DamageSource getSourceCaused(Level level, ResourceKey<DamageType> type, @Nullable Entity target, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), target, attacker);
    }

    public static class ExperienceDroppingDamageSource extends DamageSource {
        public ExperienceDroppingDamageSource(Holder<DamageType> type) {
            super(type);
        }

        public ExperienceDroppingDamageSource(Holder<DamageType> pType, @Nullable Entity pDirectEntity, @Nullable Entity pCausingEntity) {
            super(pType, pDirectEntity, pCausingEntity, null);
        }

        public static DamageSource getExperienceDroppingSource(LevelReader level, ResourceKey<DamageType> type) {
            Holder.Reference<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);

            return new ExperienceDroppingDamageSource(holder);
        }

        public static DamageSource getExperienceDroppingSourceCaused(LevelReader level, ResourceKey<DamageType> type, @Nullable Entity target, @Nullable Entity attacker) {
            Holder.Reference<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);

            return new ExperienceDroppingDamageSource(holder, target, attacker);
        }
    }
}
