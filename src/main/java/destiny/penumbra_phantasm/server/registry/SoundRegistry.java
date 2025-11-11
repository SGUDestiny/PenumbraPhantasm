package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PenumbraPhantasm.MODID);

    public static RegistryObject<SoundEvent> FOUNTAIN_MAKE = registerSoundEvent("fountain_make");
    public static RegistryObject<SoundEvent> FOUNTAIN_TARGET = registerSoundEvent("fountain_target");
    public static RegistryObject<SoundEvent> FOUNTAIN_WIND = registerSoundEvent("fountain_wind");
    public static RegistryObject<SoundEvent> FOUNTAIN_MUSIC = registerSoundEvent("fountain_music");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound)
    {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PenumbraPhantasm.MODID, sound)));
    }
}
