package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleTypeRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PenumbraPhantasm.MODID);

    public static final RegistryObject<SimpleParticleType> SCARLET_LEAF = PARTICLE_TYPES.register("scarlet_leaves", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FOUNTAIN_TARGET = PARTICLE_TYPES.register("fountain_target", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> REAL_KNIFE_SLASH = PARTICLE_TYPES.register("real_knife_slash", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> REAL_KNIFE_HIT = PARTICLE_TYPES.register("real_knife_hit", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FOUNTAIN_DARKNESS = PARTICLE_TYPES.register("fountain_darkness", () -> new SimpleParticleType(true));
}
