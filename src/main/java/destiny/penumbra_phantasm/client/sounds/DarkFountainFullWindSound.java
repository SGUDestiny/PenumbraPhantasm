package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainFullBlockEntity;
import net.minecraft.sounds.SoundEvent;

public class DarkFountainFullWindSound extends DarkFountainFullSound<DarkFountainFullBlockEntity>{
    private static final float VOLUME_MIN = 0.0F;
    private static final float VOLUME_MAX = 0.2F;

    public DarkFountainFullWindSound(DarkFountainFullBlockEntity fountain, SoundEvent soundEvent) {
        super(fountain, soundEvent, 23, 32);
        this.looping = true;
        this.volume = VOLUME_MIN;
    }

    @Override
    public void tick()
    {
        if(getDistanceFromSource() <= 32)
            fadeIn();
        else
            fadeOut();

        super.tick();
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    private void fadeIn()
    {
        if(this.volume < VOLUME_MAX)
            this.volume += 0.01F;
    }

    private void fadeOut()
    {
        if(this.volume > VOLUME_MIN)
            this.volume -= 0.01F;
    }
}
