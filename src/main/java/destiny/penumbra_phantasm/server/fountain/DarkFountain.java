package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.sounds.SoundWrapper;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class DarkFountain {
    public static final String FOUNTAIN_POS = "fountainPos";
    public static final String FOUNTAIN_DIMENSION = "fountainDimension";
    public static final String DESTINATION_POS = "destinationPos";
    public static final String DESTINATION_DIMENSION = "destinationDimension";
    public static final String ANIMATION_TIMER = "animationTimer";
    public static final String FRAME_TIMER = "frameTimer";
    public static final String FRAME = "frame";
    public static final String TELEPORTED_ENTITIES = "teleportedEntities";

    BlockPos fountainPos;
    ResourceKey<Level> fountainDimension;
    BlockPos destinationPos;
    ResourceKey<Level> destinationDimension;

    public int animationTimer;
    public int frameTimer;
    public int frame;

    public HashSet<UUID> teleportedEntities;

    @Nullable
    public SoundWrapper musicSound = null;
    @Nullable
    public SoundWrapper windSound = null;

    public DarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension, BlockPos destinationPos, ResourceKey<Level> destinationDimension, int animationTimer, int frameTimer, int frame, HashSet<UUID> teleportedEntities) {
        this.fountainPos = fountainPos;
        this.fountainDimension = fountainDimension;
        this.destinationPos = destinationPos;
        this.destinationDimension = destinationDimension;
        this.animationTimer = animationTimer;
        this.frameTimer = frameTimer;
        this.frame = frame;
        this.teleportedEntities = teleportedEntities;
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
        ListTag teleportedEntitiesList = new ListTag();
        for (UUID uuid : teleportedEntities) {
            teleportedEntitiesList.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put(TELEPORTED_ENTITIES, teleportedEntitiesList);

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
        HashSet<UUID> teleportedEntities = new HashSet<>();
        ListTag teleportedEntitiesTag = tag.getList(TELEPORTED_ENTITIES, Tag.TAG_STRING);
        for (Tag tg : teleportedEntitiesTag) {
            UUID uuid = UUID.fromString(tg.getAsString());
            teleportedEntities.add(uuid);
        }

        return new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, animationTimer, frameTimer, frame, teleportedEntities);
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

            if (this.animationTimer > 125 || this.animationTimer == -1) {
                AABB teleportBox = new AABB(fountainPos).inflate(1);
                HashSet<UUID> teleportBoxEntities = new HashSet<>();

                ServerLevel destinationLevel = level.getServer().getLevel(this.destinationDimension);
                if (destinationLevel == null) {
                    return;
                }

                destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                    DarkFountain destinationFountain = cap.darkFountains.get(this.destinationPos);

                    for (Entity entity : level.getEntitiesOfClass(Entity.class, teleportBox)) {
                        if (!this.teleportedEntities.contains(entity.getUUID())) {
                            if (destinationFountain != null) {
                                if (entity instanceof ServerPlayer player) {
                                    destinationFountain.teleportedEntities.add(teleportPlayer(player, destinationLevel).getUUID());
                                } else {
                                    destinationFountain.teleportedEntities.add(teleportEntity(entity, destinationLevel).getUUID());
                                }
                            }
                        }
                        teleportBoxEntities.add(entity.getUUID());
                    }
                });

                HashSet<UUID> newTeleportedEntities = new HashSet<>();

                for (UUID entity : teleportedEntities) {
                    if (teleportBoxEntities.contains(entity)) {
                        newTeleportedEntities.add(entity);
                    }
                }

                this.teleportedEntities = newTeleportedEntities;

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

    public Entity teleportPlayer(ServerPlayer player, ServerLevel destinationLevel) {
        player.teleportTo(destinationLevel, destinationPos.getX(), destinationPos.getY(), destinationPos.getZ(), (float)Math.toDegrees(Math.atan2((float) player.getLookAngle().x(), (float) player.getLookAngle().z()) + 270), player.getXRot());
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        return player;
    }

    public Entity teleportEntity(Entity entity, ServerLevel destinationLevel) {
        return entity.changeDimension(destinationLevel);
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