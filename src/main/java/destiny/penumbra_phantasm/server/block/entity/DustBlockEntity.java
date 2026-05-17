package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DustBlockEntity extends BlockEntity {
    public DustBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.DUST_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
}
