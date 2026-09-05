package destiny.penumbra_phantasm.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ManagedMusicSound extends AbstractTickableSoundInstance {
    private float targetVolume = 0;

    public ManagedMusicSound(SoundEvent soundEvent, boolean looping) {
        super(soundEvent, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.looping = looping;
        this.volume = 0;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (this.volume < targetVolume) {
            this.volume = Math.min(this.volume + 0.005f, targetVolume);
        } else if (this.volume > targetVolume) {
            this.volume = Math.max(this.volume - 0.005f, targetVolume);
        }

        if (this.volume <= 0 && targetVolume <= 0 && !this.looping) {
            this.stop();
        }
    }

    @Override
    public float getVolume() {
        if (this.sound == null) return 0;
        return super.getVolume();
    }

    @Override
    public float getPitch() {
        if (this.sound == null) return 1;
        return super.getPitch();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void setTargetVolume(float target) {
        this.targetVolume = target;
    }

    public float getTargetVolume() {
        return this.targetVolume;
    }

    public float getLinearVolume() {
        return this.volume;
    }

    public boolean isFadedOut() {
        return this.volume <= 0 && this.targetVolume <= 0;
    }

    public void forceVolume(float vol) {
        this.volume = vol;
        this.targetVolume = vol;
    }

    public void stopSound() {
        this.stop();
    }
}
