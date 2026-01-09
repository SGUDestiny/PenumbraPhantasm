package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Minecraft;

public class DarkFountainSoundWrapper<T extends DarkFountain> extends SoundWrapper
{
    protected static Minecraft minecraft = Minecraft.getInstance();

    protected T fountain;
    protected DarkFountainSound<?> sound;
    protected boolean playingSound = false;

    protected DarkFountainSoundWrapper(T fountain, DarkFountainSound sound)
    {
        this.fountain = fountain;
        this.sound = sound;
    }

    @Override
    public boolean isPlaying()
    {
        return this.playingSound;
    }

    @Override
    public boolean hasSound()
    {
        return this.sound != null && !this.sound.isStopped();
    }

    @Override
    public void playSound()
    {
        if(!this.playingSound)
        {
            minecraft.getSoundManager().queueTickingSound(sound);
            this.playingSound = true;
        }
    }

    @Override
    public void stopSound()
    {
        if(this.playingSound)
        {
            this.sound.stopSound();
            this.playingSound = false;
        }
    }

    public static class DarkFountainMusic extends DarkFountainSoundWrapper<DarkFountain>
    {
        public DarkFountainMusic(DarkFountain fountain)
        {
            super(fountain, new DarkFountainMusicSound(fountain, SoundAccess.getFountainMusic()));
        }
    }

    public static class DarkFountainWind extends DarkFountainSoundWrapper<DarkFountain>
    {
        public DarkFountainWind(DarkFountain fountain)
        {
            super(fountain, new DarkFountainWindSound(fountain, SoundAccess.getFountainWind()));
        }
    }

    public static class DarkFountainDarkness extends DarkFountainSoundWrapper<DarkFountain>
    {
        public DarkFountainDarkness(DarkFountain fountain)
        {
            super(fountain, new DarkFountainDarknessSound(fountain, SoundAccess.getFountainDarkness()));
        }
    }
}
