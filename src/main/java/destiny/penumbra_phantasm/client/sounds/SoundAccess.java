package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.FountainSoundUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;
import java.util.UUID;

public class SoundAccess {
    public static final String EMPTY = PenumbraPhantasm.EMPTY;

    protected static Minecraft minecraft = Minecraft.getInstance();

    public static void playFountainMusic(BlockPos fountainPos, boolean stop)
    {
        DarkFountain fountain = getFountain(fountainPos);

        if(fountain != null)
        {
            DarkFountainSoundWrapper.DarkFountainMusic fountainMusic = FountainSoundUtil.getAmbientMusicSound(fountainPos);
            if(fountainMusic == null)
            {
                fountainMusic = new DarkFountainSoundWrapper.DarkFountainMusic(fountain);
                FountainSoundUtil.setAmbientSound(fountainMusic, fountainPos);
            }

            if(stop)
                fountainMusic.stopSound();
            else if(!fountainMusic.isPlaying())
                fountainMusic.playSound();
        }
    }

    public static SoundEvent getFountainMusic(DarkFountain fountain) {
        return Config.alternateDarkFountainMusic ? SoundRegistry.FOUNTAIN_MUSIC_ALTERNATE.get() : SoundRegistry.FOUNTAIN_MUSIC.get();
    }

    public static void playFountainDarkWind(BlockPos fountainPos, boolean stop)
    {
        DarkFountain fountain = getFountain(fountainPos);

        if(fountain != null)
        {
            DarkFountainSoundWrapper.DarkFountainWind darkFountainWind = FountainSoundUtil.getDarkWindSound(fountainPos);
            if(darkFountainWind == null)
            {
                darkFountainWind = new DarkFountainSoundWrapper.DarkFountainWind(fountain);
                FountainSoundUtil.setDarkWindSound(darkFountainWind, fountainPos);
            }

            if(stop)
                darkFountainWind.stopSound();
            else if(!darkFountainWind.isPlaying())
                darkFountainWind.playSound();
        }
    }

    public static void playFountainLightWind(BlockPos fountainPos, boolean stop)
    {
        DarkFountain fountain = getFountain(fountainPos);

        if(fountain != null)
        {
            DarkFountainSoundWrapper.DarkFountainWind lightFountainWind = FountainSoundUtil.getLightWindSound(fountainPos);
            if(lightFountainWind == null)
            {
                lightFountainWind = new DarkFountainSoundWrapper.DarkFountainWind(fountain);
                FountainSoundUtil.setLightWindSound(lightFountainWind, fountainPos);
            }

            if(stop)
                lightFountainWind.stopSound();
            else if(!lightFountainWind.isPlaying())
                lightFountainWind.playSound();
        }
    }

    public static SoundEvent getFountainWind(DarkFountain fountain) {
        return SoundRegistry.FOUNTAIN_WIND.get();
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
