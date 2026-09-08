package destiny.penumbra_phantasm.client.sound;

import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public record BiomeMusic(Supplier<SoundEvent> soundSupplier, boolean looping, int minDelay, int maxDelay) {
    public SoundEvent sound() {
        return soundSupplier.get();
    }
}
