package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RoaringEyeBlockEntity extends BlockEntity {
    public RoaringEyeBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.ROARING_EYE_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
}
