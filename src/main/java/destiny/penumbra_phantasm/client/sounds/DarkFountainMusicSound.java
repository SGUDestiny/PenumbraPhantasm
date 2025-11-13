package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainBlockEntity;
import net.minecraft.sounds.SoundEvent;

public class DarkFountainMusicSound extends DarkFountainSound<DarkFountainBlockEntity>{
    private static final float VOLUME_MIN = 0.0F;
    private static final float VOLUME_MAX = 0.5F;

    public DarkFountainMusicSound(DarkFountainBlockEntity fountain, SoundEvent soundEvent) {
        super(fountain, soundEvent);
        this.looping = true;
        this.volume = VOLUME_MIN;
    }


    @Override
    public void tick()
    {
        if(getDistanceFromSource() <= 16)
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
            this.volume += 0.05F;
    }

    private void fadeOut()
    {
        if(this.volume > VOLUME_MIN)
            this.volume -= 0.05F;
    }
}
