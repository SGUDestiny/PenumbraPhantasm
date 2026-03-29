package destiny.penumbra_phantasm.client.sound;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Minecraft;

public abstract class DarkFountainSoundWrapper<T extends DarkFountain> extends SoundWrapper {
    protected static Minecraft minecraft = Minecraft.getInstance();

    protected T fountain;
    protected DarkFountainSound<T> sound;
    protected boolean playingSound = false;

    protected DarkFountainSoundWrapper(T fountain, DarkFountainSound<T> sound) {
        this.fountain = fountain;
        this.sound = sound;
    }

    protected abstract DarkFountainSound<T> newSoundInstance();

    @Override
    public boolean isPlaying() {
        return playingSound && sound != null && minecraft.getSoundManager().isActive(sound);
    }

    @Override
    public boolean hasSound() {
        return sound != null && !sound.isStopped();
    }

    @Override
    public void playSound() {
        if (sound != null && playingSound && !minecraft.getSoundManager().isActive(sound)) {
            if (!sound.isStopped()) {
                sound.stopSound();
            }
            sound = newSoundInstance();
            playingSound = false;
        }
        if (!this.playingSound) {
            if (sound == null || sound.isStopped()) {
                sound = newSoundInstance();
            }
            minecraft.getSoundManager().queueTickingSound(sound);
            this.playingSound = true;
        }
    }

    @Override
    public void stopSound() {
        if (sound != null && !sound.isStopped()) {
            sound.stopSound();
        }
        this.playingSound = false;
    }

    public void recoverAfterResourceReload() {
        if (playingSound) {
            playSound();
        }
    }

    public static class DarkFountainWind extends DarkFountainSoundWrapper<DarkFountain> {
        public DarkFountainWind(DarkFountain fountain) {
            super(fountain, new DarkFountainWindSound(fountain, SoundAccess.getFountainWind()));
        }

        @Override
        protected DarkFountainSound<DarkFountain> newSoundInstance() {
            return new DarkFountainWindSound(fountain, SoundAccess.getFountainWind());
        }
    }

    public static class DarkFountainDarkness extends DarkFountainSoundWrapper<DarkFountain> {
        public DarkFountainDarkness(DarkFountain fountain) {
            super(fountain, new DarkFountainDarknessSound(fountain, SoundAccess.getFountainDarkness()));
        }

        @Override
        protected DarkFountainSound<DarkFountain> newSoundInstance() {
            return new DarkFountainDarknessSound(fountain, SoundAccess.getFountainDarkness());
        }
    }
}
