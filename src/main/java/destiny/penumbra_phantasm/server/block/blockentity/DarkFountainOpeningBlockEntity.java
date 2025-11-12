package destiny.penumbra_phantasm.server.block.blockentity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DarkFountainOpeningBlockEntity extends BlockEntity {
    public DarkFountainOpeningBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.DARK_FOUNTAIN_OPENING.get(), p_155229_, p_155230_);
    }
}
