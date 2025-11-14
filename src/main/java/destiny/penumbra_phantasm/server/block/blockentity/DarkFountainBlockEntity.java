package destiny.penumbra_phantasm.server.block.blockentity;

import destiny.penumbra_phantasm.client.network.ClientBoundSoundPackets;
import destiny.penumbra_phantasm.client.sounds.SoundWrapper;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class DarkFountainBlockEntity extends BlockEntity {
    private int animationTimer = 0;
    private int frame = 0;
    private int windTicker = 102;
    private int musicTicker = 1066;
    @Nullable
    public SoundWrapper musicSound = null;
    @Nullable
    public SoundWrapper windSound = null;

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
            //level.playSound(null, pos, SoundRegistry.FOUNTAIN_WIND.get(), SoundSource.AMBIENT, 0.2f, 1f);
            entity.windTicker = 0;
        } else {
            entity.windTicker++;
        }

        if (entity.musicTicker == 1066) {
            //level.playSound(null, pos, SoundRegistry.FOUNTAIN_MUSIC.get(), SoundSource.AMBIENT, 0.5f, 1f);
            entity.musicTicker = 0;
        } else {
            entity.musicTicker++;
        }

        if (!level.isClientSide()) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(entity.worldPosition)), new ClientBoundSoundPackets.FountainMusic(entity.worldPosition, false));
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(entity.worldPosition)), new ClientBoundSoundPackets.FountainWind(entity.worldPosition, false));
        }
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
