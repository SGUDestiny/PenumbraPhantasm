package destiny.penumbra_phantasm.server.block.blockentity;

import destiny.penumbra_phantasm.client.sounds.SoundWrapper;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class DarkFountainFullBlockEntity extends BlockEntity {
    public int animationTimer;
    private int frameTimer;
    private int frame;
    @Nullable
    public SoundWrapper musicSound = null;
    @Nullable
    public SoundWrapper windSound = null;

    public DarkFountainFullBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.DARK_FOUNTAIN_FULL.get(), p_155229_, p_155230_);
        this.animationTimer = 0;
        this.frameTimer = 0;
        this.frame = 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DarkFountainFullBlockEntity entity) {
        if (entity.animationTimer == 0) {
            level.playSound(null, pos, SoundRegistry.FOUNTAIN_MAKE.get(), SoundSource.AMBIENT, 1, 1);
        }

        if (entity.frameTimer % 3 == 0) {
            if (entity.frame >= 13) {
                entity.frame = 0;
            } else {
                entity.frame++;
            }
        }

        if (entity.frameTimer >= 13 * 3) {
            entity.frameTimer = 0;
        } else {
            entity.frameTimer++;
        }

        if (entity.animationTimer >= 144) {
            entity.animationTimer = -1;
        }
        if (entity.animationTimer >= 0) {
            entity.animationTimer++;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        frame = tag.getInt("frame");
        frameTimer = tag.getInt("frameTimer");
        animationTimer = tag.getInt("animationTimer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("frame", frame);
        tag.putInt("frameTimer", frameTimer);
        tag.putInt("animationTimer", animationTimer);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    private void markUpdated() {
        super.setChanged();
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void playMusic()
    {
        if(!this.musicSound.isPlaying())
        {
            this.musicSound.stopSound();
            this.musicSound.playSound();
        }
    }

    public void stopMusic(){
        this.musicSound.stopSound();
    }

    public void playWind()
    {
        if(!this.windSound.isPlaying())
        {
            this.windSound.stopSound();
            this.windSound.playSound();
        }
    }

    public void stopWind(){
        this.windSound.stopSound();
    }

    public int getFrame() {
        return frame;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
