package destiny.penumbra_phantasm.server.block.blockentity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DarkFountainOpeningBlockEntity extends BlockEntity {
    private int animationTimer = 0;

    public DarkFountainOpeningBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.DARK_FOUNTAIN_OPENING.get(), p_155229_, p_155230_);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DarkFountainOpeningBlockEntity entity) {
        if (entity.animationTimer == 0) {
            level.playSound(null, pos, SoundRegistry.FOUNTAIN_MAKE.get(), SoundSource.AMBIENT, 1, 1);
        }
        entity.animationTimer++;
    }

    public int getAnimationTimer() {
        return animationTimer;
    }
}
