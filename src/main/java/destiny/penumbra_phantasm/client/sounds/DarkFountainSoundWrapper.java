package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainBlockEntity;
import net.minecraft.client.Minecraft;

public abstract class DarkFountainSoundWrapper <T extends DarkFountainBlockEntity> extends SoundWrapper
{
    protected static Minecraft minecraft = Minecraft.getInstance();

    protected T stargate;
    protected DarkFountainSound<?> sound;
    protected boolean playingSound = false;

    protected DarkFountainSoundWrapper(T stargate, DarkFountainSound sound)
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

    public static class DarkFountainMusic extends DarkFountainSoundWrapper<DarkFountainBlockEntity>
    {
        public DarkFountainMusic(DarkFountainBlockEntity fountain)
        {
            super(fountain, new DarkFountainMusicSound(fountain, SoundAccess.getFountainMusic(fountain)));
        }
    }

    public static class DarkFountainWind extends DarkFountainSoundWrapper<DarkFountainBlockEntity>
    {
        public DarkFountainWind(DarkFountainBlockEntity fountain)
        {
            super(fountain, new DarkFountainWindSound(fountain, SoundAccess.getFountainWind(fountain)));
        }
    }
}
