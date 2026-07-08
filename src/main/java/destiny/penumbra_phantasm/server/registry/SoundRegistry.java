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
    public static RegistryObject<SoundEvent> AN_OUTSTANDING_MOVE = registerSoundEvent("an_outstanding_move");

    public static RegistryObject<SoundEvent> FOUNTAIN_MAKE = registerSoundEvent("fountain_make");
    public static RegistryObject<SoundEvent> FOUNTAIN_TARGET = registerSoundEvent("fountain_target");
    public static RegistryObject<SoundEvent> FOUNTAIN_WIND = registerSoundEvent("fountain_wind");
    public static RegistryObject<SoundEvent> FOUNTAIN_MUSIC = registerSoundEvent("fountain_music");
    public static RegistryObject<SoundEvent> FOUNTAIN_MUSIC_DISC = registerSoundEvent("fountain_music_disc");
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

    public static RegistryObject<SoundEvent> DICE_THROW = registerSoundEvent("dice_throw");
    public static RegistryObject<SoundEvent> SLIDE_DOWN = registerSoundEvent("slide_down");
    public static RegistryObject<SoundEvent> DUST_BREAK = registerSoundEvent("dust_break");

    public static RegistryObject<SoundEvent> LIGHTER_TRY = registerSoundEvent("lighter_try");
    public static RegistryObject<SoundEvent> LIGHTER_LIGHT = registerSoundEvent("lighter_light");
    public static RegistryObject<SoundEvent> LIGHTER_CLOSE = registerSoundEvent("lighter_close");

    public static RegistryObject<SoundEvent> CHESHIRE_CHEST_LAUGH = registerSoundEvent("cheshire_chest_laugh");

    public static RegistryObject<SoundEvent> FIRE_DOOR_OPEN = registerSoundEvent("fire_door_open");
    public static RegistryObject<SoundEvent> FIRE_DOOR_CLOSE = registerSoundEvent("fire_door_close");

    public static RegistryObject<SoundEvent> HEAL = registerSoundEvent("heal");

    public static RegistryObject<SoundEvent> SOUL_GRAB = registerSoundEvent("soul_grab");
    public static RegistryObject<SoundEvent> SOUL_HURT = registerSoundEvent("soul_hurt");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound)
    {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PenumbraPhantasm.MODID, sound)));
    }
}
