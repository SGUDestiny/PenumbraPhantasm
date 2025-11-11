package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleTypeRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PenumbraPhantasm.MODID);

    public static final RegistryObject<SimpleParticleType> SCARLET_LEAF = PARTICLE_TYPES.register("scarlet_leaves", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FOUNTAIN_TARGET = PARTICLE_TYPES.register("fountain_target", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
