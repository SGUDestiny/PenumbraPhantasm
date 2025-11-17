package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.sounds.SoundWrapper;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class DarkFountain {
    public static final String FOUNTAIN_POS = "fountainPos";
    public static final String FOUNTAIN_DIMENSION = "fountainDimension";
    public static final String DESTINATION_POS = "destinationPos";
    public static final String DESTINATION_DIMENSION = "destinationDimension";
    public static final String ANIMATION_TIMER = "animationTimer";
    public static final String FRAME_TIMER = "frameTimer";
    public static final String FRAME = "frame";

    BlockPos fountainPos;
    ResourceKey<Level> fountainDimension;
    BlockPos destinationPos;
    ResourceKey<Level> destinationDimension;

    public int animationTimer;
    public int frameTimer;
    public int frame;

    @Nullable
    public SoundWrapper musicSound = null;
    @Nullable
    public SoundWrapper windSound = null;

    public DarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension, BlockPos destinationPos, ResourceKey<Level> destinationDimension, int animationTimer, int frameTimer, int frame) {
        this.fountainPos = fountainPos;
        this.fountainDimension = fountainDimension;
        this.destinationPos = destinationPos;
        this.destinationDimension = destinationDimension;
        this.animationTimer = animationTimer;
        this.frameTimer = frameTimer;
        this.frame = frame;
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();

        tag.put(FOUNTAIN_POS, NbtUtils.writeBlockPos(fountainPos));
        tag.putString(FOUNTAIN_DIMENSION, fountainDimension.location().toString());
        tag.put(DESTINATION_POS, NbtUtils.writeBlockPos(destinationPos));
        tag.putString(DESTINATION_DIMENSION, destinationDimension.location().toString());
        tag.putInt(ANIMATION_TIMER, animationTimer);
        tag.putInt(FRAME_TIMER, frameTimer);
        tag.putInt(FRAME, frame);

        return tag;
    }

    public static DarkFountain load(CompoundTag tag) {
        BlockPos fountainPos = NbtUtils.readBlockPos(tag.getCompound(FOUNTAIN_POS));
        ResourceKey<Level> fountainDimension = stringToDimension(tag.getString(FOUNTAIN_DIMENSION));
        BlockPos destinationPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_POS));
        ResourceKey<Level> destinationDimension = stringToDimension(tag.getString(DESTINATION_DIMENSION));
        int animationTimer = tag.getInt(ANIMATION_TIMER);
        int frameTimer = tag.getInt(FRAME_TIMER);
        int frame = tag.getInt(FRAME);

        return new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, animationTimer, frameTimer, frame);
    }

    public BlockPos getFountainPos() {
        return fountainPos;
    }

    public ResourceKey<Level> getFountainDimension() {
        return fountainDimension;
    }

    public BlockPos getDestinationPos() {
        return destinationPos;
    }

    public ResourceKey<Level> getDestinationDimension() {
        return destinationDimension;
    }

    public int getAnimationTimer() {
        return animationTimer;
    }

    public static ResourceKey<Level> stringToDimension(String dimensionString)
    {
        String[] split = dimensionString.split(":");

        if(split.length > 1)
            return ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(split[0], split[1]));

        return null;
    }

    public void tick(Level level)
    {
        if (!level.isClientSide()) {
            if (this.animationTimer == 0) {
                level.playSound(null, getFountainPos(), SoundRegistry.FOUNTAIN_MAKE.get(), SoundSource.AMBIENT, 1, 1);
            }

            if (this.frameTimer % 3 == 0) {
                if (this.frame >= 13) {
                    this.frame = 0;
                } else {
                    this.frame++;
                }
            }

            if (this.frameTimer >= 13 * 3) {
                this.frameTimer = 0;
            } else {
                this.frameTimer++;
            }

            if (this.animationTimer >= 144) {
                this.animationTimer = -1;
            }
            if (this.animationTimer >= 0) {
                this.animationTimer++;
            }

            if (this.animationTimer > 130 || this.animationTimer == -1) {
                if (Config.darkFountainMusic) {
                    //PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())), new ClientBoundSoundPackets.FountainMusic(this.fountainUuid, false));
                }
                //PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())), new ClientBoundSoundPackets.FountainWind(this.fountainUuid, false));
            }
        }

        if (level.dimension() != ResourceKey.create(Registries.DIMENSION, new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths"))
                    && (this.animationTimer > 125 || this.animationTimer == -1)) {
            if (level.getGameTime() % 2 == 0)
            {
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), fountainPos.getX() + 0.5f, fountainPos.getY(), fountainPos.getZ() + 0.5f, ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f), ModUtil.getBoundRandomFloatStatic(level, 0f, 0.1f), ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f));
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), fountainPos.getX() + 0.5f, fountainPos.getY(), fountainPos.getZ() + 0.5f, ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f), ModUtil.getBoundRandomFloatStatic(level, 0f, 0.1f), ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f));
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), fountainPos.getX() + 0.5f, fountainPos.getY(), fountainPos.getZ() + 0.5f, ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f), ModUtil.getBoundRandomFloatStatic(level, 0f, 0.1f), ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f));
            }
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

    public int getFrameTimer() {
        return frameTimer;
    }


    public int getFrame() {
        return frame;
    }
}