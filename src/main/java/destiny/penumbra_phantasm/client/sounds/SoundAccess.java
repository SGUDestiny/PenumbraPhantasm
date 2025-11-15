package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class SoundAccess {
    public static final String EMPTY = PenumbraPhantasm.EMPTY;

    protected static Minecraft minecraft = Minecraft.getInstance();

    public static void playFountainMusic(UUID fountainUuid, boolean stop)
    {
        DarkFountain fountain = getFountain(fountainUuid);

        if(fountain != null)
        {
            if(fountain.musicSound == null)
            {
                fountain.musicSound = new DarkFountainSoundWrapper.DarkFountainMusic(fountain);
            }

            if(stop)
                fountain.stopMusic();
            else
                fountain.playMusic();
        }
    }

    public static SoundEvent getFountainMusic(DarkFountain fountain) {
        return SoundRegistry.FOUNTAIN_MUSIC.get();
    }

    public static void playFountainWind(UUID fountainUuid, boolean stop)
    {
        DarkFountain fountain = getFountain(fountainUuid);

        if(fountain != null)
        {
            if(fountain.windSound == null)
            {
                fountain.windSound = new DarkFountainSoundWrapper.DarkFountainWind(fountain);
            }

            if(stop)
                fountain.stopWind();
            else
                fountain.playWind();
        }
    }

    public static SoundEvent getFountainWind(DarkFountain fountain) {
        return SoundRegistry.FOUNTAIN_WIND.get();
    }

    public static DarkFountain getFountain(UUID fountainUuid) {
        DarkFountain fountain = null;
        AtomicReference<DarkFountainCapability> atomicCapability = null;
        minecraft.level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> atomicCapability.set(cap));

        DarkFountainCapability capability = atomicCapability.get();

        if (capability != null) {
            fountain = capability.darkFountains.get(fountainUuid);
        }

        return fountain;
    }
}
