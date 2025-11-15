package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class DarkFountainSound<T extends DarkFountain> extends AbstractTickableSoundInstance {
    protected T fountain;
    protected BlockPos fountainPos;
    protected Minecraft minecraft = Minecraft.getInstance();
    protected int fullDistance;
    protected int maxDistance;

    public DarkFountainSound(T fountain, SoundEvent soundEvent, int fullDistance, int maxDistance) {
        super(soundEvent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());

        this.fountain = fountain;
        this.fountainPos = fountain.getFountainPos();
        this.relative = true;
        this.fullDistance = fullDistance;
        this.maxDistance = maxDistance;
    }

    @Override
    public void tick()
    {
        if(fountain == null)
            this.stop();
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    public void stopSound()
    {
        this.stop();
    }

    public double getDistanceFromSource()
    {
        LocalPlayer player = minecraft.player;
        Vec3 playerPos = player.position();
        return fountainPos.getCenter().distanceTo(playerPos);
    }

    public float getVolume()
    {
        float localVolume = 0.0F;
        double distanceFromSource = getDistanceFromSource();

        float fullDistance = this.fullDistance;
        float maxDistance = this.maxDistance;

        if(fullDistance >= maxDistance)
            maxDistance = fullDistance + 1;

        if(fullDistance >= maxDistance)
            maxDistance = fullDistance + 1;

        if(distanceFromSource <= fullDistance)
            localVolume = getMaxVolume();
        else if(distanceFromSource <= maxDistance)
            localVolume = (float) (getMaxVolume() - (distanceFromSource - fullDistance) / (maxDistance - fullDistance));
        else
            localVolume = getMinVolume();

        return super.getVolume() * localVolume;
    }

    public float getMaxVolume()
    {
        return 1.0F;
    }

    public float getMinVolume()
    {
        return 0.0F;
    }
}
