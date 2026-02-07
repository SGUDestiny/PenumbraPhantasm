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
    public static RegistryObject<SoundEvent> FOUNTAIN_MUSIC_ALTERNATE = registerSoundEvent("fountain_music_alternate");
    public static RegistryObject<SoundEvent> FOUNTAIN_DARKNESS = registerSoundEvent("fountain_darkness");
    public static RegistryObject<SoundEvent> FOUNTAIN_SEAL = registerSoundEvent("fountain_seal");
    public static RegistryObject<SoundEvent> REAL_KNIFE_SLASH = registerSoundEvent("real_knife_slash");
    public static RegistryObject<SoundEvent> REAL_KNIFE_HIT = registerSoundEvent("real_knife_hit");
    public static RegistryObject<SoundEvent> LEVEL_UP = registerSoundEvent("level_up");
    public static RegistryObject<SoundEvent> INTRO_ANOTHER_HIM = registerSoundEvent("intro_another_him");
    public static RegistryObject<SoundEvent> INTRO_ANOTHER_HIM_LOOP = registerSoundEvent("intro_another_him_loop");
    public static RegistryObject<SoundEvent> INTRO_APPEARANCE = registerSoundEvent("intro_appearance");
    public static RegistryObject<SoundEvent> INTRO_DRONE = registerSoundEvent("intro_drone");
    public static RegistryObject<SoundEvent> GREAT_SHINE = registerSoundEvent("great_shine");
    public static RegistryObject<SoundEvent> OCEAN = registerSoundEvent("ocean");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound)
    {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PenumbraPhantasm.MODID, sound)));
    }
}
