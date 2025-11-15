package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.client.network.ClientBoundSoundPackets;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class DarkFountain {
    public static final String FOUNTAIN_POS = "fountainPos";
    public static final String FOUNTAIN_DIMENSION = "fountainDimension";
    public static final String DESTINATION_POS = "destinationPos";
    public static final String DESTINATION_DIMENSION = "destinationDimension";
    public static final String ANIMATION_TICK = "animationTick";

    BlockPos fountainPos;
    ResourceKey<Level> fountainDimension;
    BlockPos destinationPos;
    ResourceKey<Level> destinationDimension;
    int animationTick;

    public int animationTimer;
    public int frameTimer;
    public int frame;

    public DarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension, BlockPos destinationPos, ResourceKey<Level> destinationDimension, int animationTick) {
        this.fountainPos = fountainPos;
        this.fountainDimension = fountainDimension;
        this.destinationPos = destinationPos;
        this.destinationDimension = destinationDimension;
        this.animationTick = animationTick;
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();

        tag.put(FOUNTAIN_POS, NbtUtils.writeBlockPos(fountainPos));
        tag.putString(FOUNTAIN_DIMENSION, fountainDimension.location().toString());
        tag.put(DESTINATION_POS, NbtUtils.writeBlockPos(destinationPos));
        tag.putString(DESTINATION_DIMENSION, destinationDimension.location().toString());
        tag.putInt(ANIMATION_TICK, animationTick);

        return tag;
    }

    public static DarkFountain load(CompoundTag tag) {
        BlockPos fountainPos = NbtUtils.readBlockPos(tag.getCompound(FOUNTAIN_POS));
        ResourceKey<Level> fountainDimension = stringToDimension(tag.getString(FOUNTAIN_DIMENSION));
        BlockPos destinationPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_POS));
        ResourceKey<Level> destinationDimension = stringToDimension(tag.getString(DESTINATION_DIMENSION));
        int animationTick = tag.getInt(ANIMATION_TICK);

        return new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, animationTick);
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

    public int getAnimationTick() {
        return animationTick;
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

        if (this.animationTimer > 140 || this.animationTimer == -1) {
            if (!level.isClientSide()) {
                if (Config.darkFountainMusic) {
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getDestinationPos())), new ClientBoundSoundPackets.FountainFullMusic(this.getFountainPos(), false));
                }
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getDestinationPos())), new ClientBoundSoundPackets.FountainFullWind(this.getFountainPos(), false));
            }
        }
    }

    public int getFrame() {
        return frame;
    }
}