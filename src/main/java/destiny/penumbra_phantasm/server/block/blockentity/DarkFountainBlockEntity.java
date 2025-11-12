package destiny.penumbra_phantasm.server.block.blockentity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DarkFountainBlockEntity extends BlockEntity {
    private int animationTimer = 0;
    private int frame = 0;
    private int windTicker = 102;
    private int musicTicker = 1066;

    public DarkFountainBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.DARK_FOUNTAIN.get(), p_155229_, p_155230_);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DarkFountainBlockEntity entity) {
        if (entity.animationTimer % 3 == 0) {
            if (entity.frame >= 13) {
                entity.frame = 0;
            } else {
                entity.frame++;
            }
        }
        entity.animationTimer++;

        if (entity.windTicker == 102) {
            level.playSound(null, pos, SoundRegistry.FOUNTAIN_WIND.get(), SoundSource.AMBIENT, 0.2f, 1f);
            entity.windTicker = 0;
        } else {
            entity.windTicker++;
        }

        if (entity.musicTicker == 1066) {
            level.playSound(null, pos, SoundRegistry.FOUNTAIN_MUSIC.get(), SoundSource.AMBIENT, 0.5f, 1f);
            entity.musicTicker = 0;
        } else {
            entity.musicTicker++;
        }
    }

    public int getAnimationTimer() {
        return animationTimer;
    }

    public int getFrame() {
        return frame;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
