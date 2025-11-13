package destiny.penumbra_phantasm.client.sounds;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainBlockEntity;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;

public class SoundAccess {
    public static final String EMPTY = PenumbraPhantasm.EMPTY;

    protected static Minecraft minecraft = Minecraft.getInstance();

    public static void playFountainMusic(BlockPos pos, boolean stop)
    {
        if(minecraft.level.getBlockEntity(pos) instanceof DarkFountainBlockEntity fountain)
        {
            if(fountain.musicSound == null)
            {
                fountain.musicSound = new DarkFountainSoundWrapper.DarkFountainMusic(fountain);
            }

            if(stop)
                fountain.stopRotationSound();
            else
                fountain.playRotationSound();
        }
    }

    public static SoundEvent getFountainMusic(DarkFountainBlockEntity fountain) {
        return SoundRegistry.FOUNTAIN_MUSIC.get();
    }

    public static SoundEvent getFountainWind(DarkFountainBlockEntity fountain) {
        return SoundRegistry.FOUNTAIN_WIND.get();
    }
}
