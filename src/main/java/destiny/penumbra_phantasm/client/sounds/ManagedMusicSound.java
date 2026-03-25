package destiny.penumbra_phantasm.client.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ManagedMusicSound extends AbstractTickableSoundInstance {
    private float targetVolume = 0.0F;

    public ManagedMusicSound(SoundEvent soundEvent, boolean looping) {
        super(soundEvent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.looping = looping;
        this.volume = 0.0F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (this.volume < targetVolume) {
            this.volume = Math.min(this.volume + 0.01F, targetVolume);
        } else if (this.volume > targetVolume) {
            this.volume = Math.max(this.volume - 0.01F, targetVolume);
        }

        if (this.volume <= 0.0F && targetVolume <= 0.0F && !this.looping) {
            this.stop();
        }
    }

    @Override
    public float getVolume() {
        if (this.sound == null) return 0.0F;
        return super.getVolume();
    }

    @Override
    public float getPitch() {
        if (this.sound == null) return 1.0F;
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

    public boolean isFadedOut() {
        return this.volume <= 0.0F && this.targetVolume <= 0.0F;
    }

    public void forceVolume(float vol) {
        this.volume = vol;
        this.targetVolume = vol;
    }

    public void stopSound() {
        this.stop();
    }
}
