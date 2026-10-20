package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AnchorOfLightPartBlockEntity extends BlockEntity {
    public static final String ORIGIN_POS = "originPos";

    public BlockPos originPos = null;

    public AnchorOfLightPartBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.ANCHOR_OF_LIGHT_PART_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AnchorOfLightPartBlockEntity part) {
        if (level.isClientSide()) return;

        if (part.originPos == null || !level.getBlockState(part.originPos).is(BlockRegistry.ANCHOR_OF_LIGHT.get())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put(ORIGIN_POS, NbtUtils.writeBlockPos(originPos));
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.originPos = NbtUtils.readBlockPos(pTag.getCompound(ORIGIN_POS));
    }
}
