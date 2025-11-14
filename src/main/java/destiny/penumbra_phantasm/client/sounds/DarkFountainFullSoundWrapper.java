package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainFullBlockEntity;
import net.minecraft.client.Minecraft;

public class DarkFountainFullSoundWrapper<T extends DarkFountainFullBlockEntity> extends SoundWrapper
{
    protected static Minecraft minecraft = Minecraft.getInstance();

    protected T stargate;
    protected DarkFountainFullSound<?> sound;
    protected boolean playingSound = false;

    protected DarkFountainFullSoundWrapper(T stargate, DarkFountainFullSound sound)
    {
        this.stargate = stargate;
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

    public static class DarkFountainMusic extends DarkFountainFullSoundWrapper<DarkFountainFullBlockEntity>
    {
        public DarkFountainMusic(DarkFountainFullBlockEntity fountain)
        {
            super(fountain, new DarkFountainFullMusicSound(fountain, SoundAccess.getFountainFullMusic(fountain)));
        }
    }

    public static class DarkFountainWind extends DarkFountainFullSoundWrapper<DarkFountainFullBlockEntity>
    {
        public DarkFountainWind(DarkFountainFullBlockEntity fountain)
        {
            super(fountain, new DarkFountainFullWindSound(fountain, SoundAccess.getFountainFullWind(fountain)));
        }
    }
}
