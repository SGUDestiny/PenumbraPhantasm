package destiny.penumbra_phantasm.server.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class BlockSetTypeRegistry {
    public static final BlockSetType CLIFFROCK_BLOCKSET = new BlockSetType("cliffrock",
            true,
            SoundTypeRegistry.CLIFF,
            SoundEvents.WOODEN_DOOR_CLOSE,
            SoundEvents.WOODEN_DOOR_OPEN,
            SoundEvents.WOODEN_TRAPDOOR_CLOSE,
            SoundEvents.WOODEN_TRAPDOOR_OPEN,
            SoundRegistry.CLIFFROCK_UNPRESS.get(),
            SoundRegistry.CLIFFROCK_PRESS.get(),
            SoundRegistry.CLIFFROCK_UNPRESS.get(),
            SoundRegistry.CLIFFROCK_PRESS.get()
    );
}
