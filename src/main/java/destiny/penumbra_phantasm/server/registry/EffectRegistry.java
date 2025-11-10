package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> DEF_REG = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, PenumbraPhantasm.MODID);
}
