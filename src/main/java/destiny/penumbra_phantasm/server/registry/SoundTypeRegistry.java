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
            () -> SoundEvents.WOOL_STEP,
            () -> SoundEvents.WOOL_PLACE,
            () -> SoundEvents.WOOL_HIT,
            () -> SoundEvents.WOOL_FALL
    );
}
