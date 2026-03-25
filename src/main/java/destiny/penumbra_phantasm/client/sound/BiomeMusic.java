package destiny.penumbra_phantasm.client.sound;

import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public record BiomeMusic(Supplier<SoundEvent> soundSupplier, boolean looping, int minDelay, int maxDelay) {
    public BiomeMusic(Supplier<SoundEvent> soundSupplier) {
        this(soundSupplier, true, 0, 0);
    }

    public SoundEvent sound() {
        return soundSupplier.get();
    }
}
