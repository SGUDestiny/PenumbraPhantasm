package destiny.penumbra_phantasm.server.block.blockentity;

import destiny.penumbra_phantasm.client.network.ClientBoundSoundPackets;
import destiny.penumbra_phantasm.client.sounds.SoundWrapper;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class DarkFountainBlockEntity extends BlockEntity {
    private int animationTimer = 0;
    private int frameTimer = 0;
    private int frame = 0;
    @Nullable
    public SoundWrapper musicSound = null;
    @Nullable
    public SoundWrapper windSound = null;

    public DarkFountainBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.DARK_FOUNTAIN.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DarkFountainBlockEntity entity) {
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

        if (entity.animationTimer >= 4) {
            entity.animationTimer = -1;
        }
        if (entity.animationTimer >= 0) {
            entity.animationTimer++;
        }

        if (!level.isClientSide()) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(entity.worldPosition)), new ClientBoundSoundPackets.FountainMusic(entity.worldPosition, false));
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(entity.worldPosition)), new ClientBoundSoundPackets.FountainWind(entity.worldPosition, false));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("frame", frame);
        tag.putInt("frameTimer", frameTimer);
        tag.putInt("animationTimer", animationTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        frame = tag.getInt("frame");
        frameTimer = tag.getInt("frameTimer");
        animationTimer = tag.getInt("animationTimer");
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
