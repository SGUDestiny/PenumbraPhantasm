package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundSingleFountainData;
import destiny.penumbra_phantasm.client.network.ClientBoundSoundPackets;
import destiny.penumbra_phantasm.client.sounds.SoundWrapper;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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
    @Nullable
    public SoundWrapper darknessSound = null;

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

    public CompoundTag save() {
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

    public void sync(DarkFountain fountain) {
        this.fountainPos = fountain.fountainPos;
        this.destinationPos = fountain.destinationPos;
        this.fountainDimension = fountain.fountainDimension;
        this.destinationDimension = fountain.destinationDimension;

        this.frame = fountain.frame;
        this.animationTimer = fountain.animationTimer;
        this.frameTimer = fountain.frameTimer;

        this.teleportedEntities = fountain.teleportedEntities;
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

    public static ResourceKey<Level> stringToDimension(String dimensionString) {
        String[] split = dimensionString.split(":");

        if(split.length > 1)
            return ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(split[0], split[1]));

        return null;
    }

    public void tick(Level level) {
        if (!level.isClientSide()) {
            if (level instanceof ServerLevel serverLevel) {
                ChunkPos fountainChunk = serverLevel.getChunk(destinationPos).getPos();
                serverLevel.setChunkForced(fountainChunk.x, fountainChunk.z, true);
            }

            if (this.animationTimer == 1) {
                level.players().forEach(player -> {
                    if (player.level().dimension() == fountainDimension) {
                        level.playSound(null, player.getOnPos().above(), SoundRegistry.FOUNTAIN_MAKE.get(), SoundSource.AMBIENT, 0.75f, 1f);
                    }
                });
            }

            if (this.frameTimer % 3 == 0) {
                if (this.frame >= 27) {
                    this.frame = 0;
                } else {
                    this.frame++;
                }
            }

            if (this.frameTimer >= 27 * 3) {
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

            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(fountainPos)), new ClientBoundSingleFountainData(this));

            if (this.animationTimer > 125 || this.animationTimer == -1) {
                AABB teleportBox;
                if (!isDarkWorld(fountainDimension)) {
                    teleportBox = new AABB(fountainPos.above()).inflate(1);
                } else {
                    teleportBox = new AABB(fountainPos.above()).inflate(1).setMaxY(128);
                }
                HashSet<UUID> teleportBoxEntities = new HashSet<>();
                ServerLevel fountainLevel = level.getServer().getLevel(this.fountainDimension);
                ServerLevel destinationLevel = level.getServer().getLevel(this.destinationDimension);

                if(fountainLevel == null || destinationLevel == null) {
                    return;
                }

                destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                    DarkFountain destinationFountain = cap.darkFountains.get(this.destinationPos);

                    for(Entity entity : level.getEntitiesOfClass(Entity.class, teleportBox)) {
                        if(!this.teleportedEntities.contains(entity.getUUID())) {
                            if(destinationFountain != null) {
                                if(entity instanceof ServerPlayer player) {
                                    destinationFountain.teleportedEntities.add(
                                            teleportPlayer(player, destinationLevel).getUUID());
                                } else {
                                    destinationFountain.teleportedEntities.add(
                                            teleportEntity(entity, destinationLevel).getUUID());
                                }
                            }
                        }
                        teleportBoxEntities.add(entity.getUUID());
                    }
                });


                //If entity outside teleport box, remove from teleported entities list
                HashSet<UUID> newTeleportedEntities = new HashSet<>();
                for(UUID entity : teleportedEntities) {
                    if(teleportBoxEntities.contains(entity)) {
                        newTeleportedEntities.add(entity);
                    }
                }
                this.teleportedEntities = newTeleportedEntities;

                //Sound packet stuff
                if(isDarkWorld(level.dimension())) {
                    if(Config.darkFountainMusic) {
                        PacketHandlerRegistry.INSTANCE.send(
                                PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                                new ClientBoundSoundPackets.FountainMusic(this.fountainPos, false));
                    }

                    PacketHandlerRegistry.INSTANCE.send(
                            PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                            new ClientBoundSoundPackets.FountainWind(this.fountainPos, false));
                }

                PacketHandlerRegistry.INSTANCE.send(
                        PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                        new ClientBoundSoundPackets.FountainDarkness(this.fountainPos, false));
            }

        }

        if (!isDarkWorld(level.dimension()) && (this.animationTimer > 125 || this.animationTimer == -1)) {
            if (level.getGameTime() % 2 == 0) {
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), fountainPos.getX() + 0.5f, fountainPos.getY(), fountainPos.getZ() + 0.5f, ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f), ModUtil.getBoundRandomFloatStatic(level, 0f, 0.1f), ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f));
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), fountainPos.getX() + 0.5f, fountainPos.getY(), fountainPos.getZ() + 0.5f, ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f), ModUtil.getBoundRandomFloatStatic(level, 0f, 0.1f), ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f));
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), fountainPos.getX() + 0.5f, fountainPos.getY(), fountainPos.getZ() + 0.5f, ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f), ModUtil.getBoundRandomFloatStatic(level, 0f, 0.1f), ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f));
            }
        }
    }

    public boolean isDarkWorld(ResourceKey<Level> levelKey) {
        return levelKey == ResourceKey.create(Registries.DIMENSION, new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths"));
    }

    public static boolean isDarkWorldStatic(ResourceKey<Level> levelKey) {
        return levelKey == ResourceKey.create(Registries.DIMENSION, new ResourceLocation(PenumbraPhantasm.MODID, "dark_depths"));
    }

/*    public Entity teleportPlayer(ServerPlayer player, ServerLevel destinationLevel) {
        player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap ->
            {
                if(true)
                {
                    cap.seenIntro = true;
                    destinationLevel.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundIntroPacket(destinationPos, destinationLevel.dimension()));
                }
            });

        return player;
    }*/

    public Entity teleportPlayer(ServerPlayer player, ServerLevel destinationLevel) {
        player.teleportTo(destinationLevel, destinationPos.getX() + 0.5, destinationPos.getY(), destinationPos.getZ() + 0.5, (float)Math.toDegrees(Math.atan2((float) player.getLookAngle().x(), (float) player.getLookAngle().z()) + 270), player.getXRot());
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        return player;
    }

    public Entity teleportEntity(Entity entity, ServerLevel destinationLevel) {
        return entity.changeDimension(destinationLevel, new DarkFountainTeleporter(destinationPos.getCenter(), entity.getDeltaMovement(),
                entity.getYRot(), entity.getXRot()));
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

    public void playDarkness()
    {
        if(!this.darknessSound.isPlaying())
        {
            this.darknessSound.stopSound();
            this.darknessSound.playSound();
        }
    }

    public void stopDarkness(){
        this.darknessSound.stopSound();
    }

    public int getFrameTimer() {
        return frameTimer;
    }


    public int getFrame() {
        return frame;
    }

    public static class DarkFountainTeleporter implements ITeleporter
    {
        private Vec3 pos;
        private Vec3 momentum;
        private float newYRot;
        private float newXRot;

        public DarkFountainTeleporter(Vec3 pos, Vec3 momentum, float newYRot, float newXRot)
        {
            this.pos = pos;
            this.momentum = momentum;
            this.newYRot = newYRot;
            this.newXRot = newXRot;
        }

        @Override
        public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo)
        {
            return new PortalInfo(pos, momentum, newYRot, newXRot);
        }

        @Override
        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld)
        {
            return false;
        }
    }
}