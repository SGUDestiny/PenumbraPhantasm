package destiny.penumbra_phantasm.client.sound;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;

public class SoundAccess {
    public static final String EMPTY = PenumbraPhantasm.EMPTY;

    protected static Minecraft minecraft = Minecraft.getInstance();

    public static SoundEvent getFountainMusic() {
        return Config.alternateDarkFountainMusic ? SoundRegistry.FOUNTAIN_MUSIC_ALTERNATE.get() : SoundRegistry.FOUNTAIN_MUSIC.get();
    }

    public static void playFountainWind(BlockPos fountainPos, boolean stop)
    {
        DarkFountain fountain = getFountain(fountainPos);

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

    public static SoundEvent getFountainWind() {
        return SoundRegistry.FOUNTAIN_WIND.get();
    }

    public static void playFountainDarkness(BlockPos fountainPos, boolean stop)
    {
        DarkFountain fountain = getFountain(fountainPos);

        if(fountain != null)
        {
            if(fountain.darknessSound == null)
            {
                fountain.darknessSound = new DarkFountainSoundWrapper.DarkFountainDarkness(fountain);
            }

            if(stop)
                fountain.stopDarkness();
            else
                fountain.playDarkness();
        }
    }

    public static SoundEvent getFountainDarkness() {
        return SoundRegistry.FOUNTAIN_DARKNESS.get();
    }

    public static DarkFountain getFountain(BlockPos fountainPos) {
        return Optional.ofNullable(minecraft.level)
                .flatMap(level ->
                        level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).resolve()
                ).map(cap ->
                        cap.darkFountains.get(fountainPos)
                ).orElse(null);
    }
}
