package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class DamageTypeRegistry {
    public static final ResourceKey<DamageType> INJECTION_PRICK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "injection_prick"));
    public static final ResourceKey<DamageType> INJECTION_DRAIN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "injection_drain"));
    public static final ResourceKey<DamageType> INJECTION_OVERDOSE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "injection_overdose"));
    public static final ResourceKey<DamageType> SOUL_DAMAGE_1 = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "soul_damage_1"));
    public static final ResourceKey<DamageType> SOUL_DAMAGE_2 = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PenumbraPhantasm.MODID, "soul_damage_2"));

    public static DamageSource getSimpleDamageSource(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
    }
}
