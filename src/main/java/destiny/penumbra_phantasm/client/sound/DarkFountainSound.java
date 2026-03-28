package destiny.penumbra_phantasm.client.sound;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec2;
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

        if (!DarkWorldUtil.isDarkWorld(minecraft.level)) {
            return fountainPos.getCenter().distanceTo(playerPos);
        } else {
            if (playerPos.y < fountainPos.getY()) {
                return fountainPos.getCenter().distanceTo(playerPos);
            } else {
                Vec3 playerPos2d = new Vec3(playerPos.x, 0f, playerPos.z);
                Vec3 fountainPos2d = new Vec3(fountainPos.getX(), 0, fountainPos.getZ());

                return  fountainPos2d.distanceTo(playerPos2d);
            }
        }
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
