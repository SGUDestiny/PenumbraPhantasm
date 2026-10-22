package destiny.penumbra_phantasm.server.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;

public class SoundTypeRegistry {
    public static final SoundType CLIFF = new ForgeSoundType(1.0F, 1.0F,
            () -> SoundRegistry.CLIFF_BREAK.get(),
            () -> SoundRegistry.CLIFF_STEP.get(),
            () -> SoundRegistry.CLIFF_BREAK.get(),
            () -> SoundRegistry.CLIFF_BREAK.get(),
            () -> SoundRegistry.CLIFF_FALL.get()
    );

    public static final SoundType DUST = new ForgeSoundType(1.0F, 1.0F,
            () -> SoundRegistry.DUST_BREAK.get(),
            () -> SoundEvents.SOUL_SAND_STEP,
            () -> SoundEvents.SOUL_SAND_PLACE,
            () -> SoundEvents.SOUL_SAND_HIT,
            () -> SoundEvents.SOUL_SAND_FALL
    );

    public static final SoundType LUCIRAMIC = new ForgeSoundType(1.0F, 1.0F,
            () -> SoundRegistry.LUCIRAMIC_BREAK.get(),
            () -> SoundRegistry.LUCIRAMIC_STEP.get(),
            () -> SoundRegistry.LUCIRAMIC_BREAK.get(),
            () -> SoundRegistry.LUCIRAMIC_STEP.get(),
            () -> SoundRegistry.LUCIRAMIC_STEP.get()
    );
}
