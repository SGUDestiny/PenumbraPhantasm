package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DarkGeyserBlockEntity extends BlockEntity {
    public DarkGeyserBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.DARK_GEYSER_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DarkGeyserBlockEntity darkness) {
        if (level.isClientSide()) {
            BlockState aboveState = level.getBlockState(pos.above());

            if (aboveState.isAir()) {
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), false, pos.getX() + 0.5, pos.getY() + 1,
                        pos.getZ() + 0.5, ModUtil.getBoundRandomDoubleStatic(level, -0.02, 0.02),
                        ModUtil.getBoundRandomDoubleStatic(level, 0, 0.02),
                        ModUtil.getBoundRandomDoubleStatic(level, -0.02, 0.02));
            }
        }
    }
}
