package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PenumbraPhantasm.MODID);

    public static RegistryObject<SoundEvent> FIELD_OF_HOPES_AND_DREAMS = registerSoundEvent("field_of_hopes_and_dreams");
    public static RegistryObject<SoundEvent> EVERLASTING_AUTUMN = registerSoundEvent("everlasting_autumn");

    public static RegistryObject<SoundEvent> FOUNTAIN_MAKE = registerSoundEvent("fountain_make");
    public static RegistryObject<SoundEvent> FOUNTAIN_TARGET = registerSoundEvent("fountain_target");
    public static RegistryObject<SoundEvent> FOUNTAIN_WIND = registerSoundEvent("fountain_wind");
    public static RegistryObject<SoundEvent> FOUNTAIN_MUSIC = registerSoundEvent("fountain_music");
    public static RegistryObject<SoundEvent> FOUNTAIN_MUSIC_DISC = registerSoundEvent("fountain_music_disc");
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
    public static RegistryObject<SoundEvent> SOUL_BREAK = registerSoundEvent("soul_break");
    public static RegistryObject<SoundEvent> SOUL_SHATTER = registerSoundEvent("soul_shatter");
    public static RegistryObject<SoundEvent> DARK_WORLD_FALL = registerSoundEvent("dark_world_fall");
    public static RegistryObject<SoundEvent> DARK_WORLD_LAND = registerSoundEvent("dark_world_land");
    public static RegistryObject<SoundEvent> HIM_QUICK = registerSoundEvent("him_quick");

    public static RegistryObject<SoundEvent> CLIFF_FALL = registerSoundEvent("cliff_fall");
    public static RegistryObject<SoundEvent> CLIFF_STEP = registerSoundEvent("cliff_step");
    public static RegistryObject<SoundEvent> CLIFF_BREAK = registerSoundEvent("cliff_break");
    public static RegistryObject<SoundEvent> CLIFFROCK_PRESS = registerSoundEvent("cliffrock_press");
    public static RegistryObject<SoundEvent> CLIFFROCK_UNPRESS = registerSoundEvent("cliffrock_unpress");

    public static RegistryObject<SoundEvent> GREAT_DOOR = registerSoundEvent("great_door");

    public static RegistryObject<SoundEvent> AMBIENCE_WIND = registerSoundEvent("ambience_wind");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound)
    {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PenumbraPhantasm.MODID, sound)));
    }
}
