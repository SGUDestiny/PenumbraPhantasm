package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.ServerConfig;
import destiny.penumbra_phantasm.client.network.*;
import destiny.penumbra_phantasm.client.sound.SoundWrapper;
import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.block.entity.DarknessBlockEntity;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.*;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class DarkFountain {
    public static final String FOUNTAIN_POS = "fountainPos";
    public static final String FOUNTAIN_DIMENSION = "fountainDimension";
    public static final String DESTINATION_POS = "destinationPos";
    public static final String DESTINATION_DIMENSION = "destinationDimension";
    public static final String OPENING_TICK = "animationTimer";
    public static final String FRAME_TICK = "frameTick";
    public static final String FRAME = "frame";
    public static final String FRAME_OPTIMIZED = "frameOptimized";
    public static final String TELEPORTED_ENTITIES = "teleportedEntities";
    public static final String ROOMS = "rooms";
    public static final String SHOCKWAVE_TICKERS = "shockwaveTickers";
    public static final String SEALING_TICK = "sealingTick";
    public static final String SEALING_FRAME_TICK = "sealingFrameTick";
    public static final String SEALING_FRAME_TICK_PROGRESS = "sealingFrameTickProgress";
    public static final String DEPTHS_POS = "depthsPos";

    public static final int OPENING_FINISH = 144;
    public static final int FILL_DELAY = 60;
    public static final int FILL_START_TICK = OPENING_FINISH + FILL_DELAY;
    public static final int TRANSPORT_TICKER_DURATION = 5 * 20;
    public static final int SEAL_DURATION = 3 * 20;
    public static final int SEAL_FLASH_DELAY = 20;
    public static final int SEAL_FLASH_DURATION = 30;

    public static final int DEPTHS_XZ_SCALE = 10;
    public static final int DEPTHS_FOUNTAIN_Y_OFFSET = 64;

    public static final double DEPTHS_PIERCE_XZ = 1;
    public static final double DEPTHS_PIERCE_PUSH_STRENGTH = 0.07;

    public static final double DEPTHS_SUCTION_XZ = 8.0;
    public static final double DEPTHS_SUCTION_Y = 64.0;

    public static final double DEPTHS_CONTACT_XZ = 3;
    public static final double DEPTHS_CONTACT_Y = 3;
    public static final double DEPTHS_EJECT_OFFSET = -0.45;

    public BlockPos fountainPos;
    public ResourceKey<Level> fountainDimension;
    public BlockPos destinationPos;
    public ResourceKey<Level> destinationDimension;
    public int openingTick;
    public int frameTick;
    public int frame;
    public int frameOptimized;
    public HashSet<UUID> teleportedEntities;
    public HashSet<UUID> depthsTransit = new HashSet<>();
    public List<DarkRoom> rooms = new ArrayList<>();
    public int rescanTimer = 0;
    public List<Integer> shockwaveTickers;
    public int sealingTick;
    public int sealingFrameTick;
    public float sealingFrameTickProgress;
    @Nullable
    public BlockPos depthsPos;

    public int openingTickTarget;
    public float openingTickClientO;
    public float openingTickClient;
    public boolean openingTickClientInitialized;

    @Nullable
    public SoundWrapper windSound = null;
    @Nullable
    public SoundWrapper darknessSound = null;

    public DarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension, BlockPos destinationPos, ResourceKey<Level> destinationDimension, int openingTick, int frameTick, int frame, int frameOptimized, HashSet<UUID> teleportedEntities, List<Integer> shockwaveTickers, int sealingTick, int sealingFrameTick, float sealingFrameTickProgress) {
        this.fountainPos = fountainPos;
        this.fountainDimension = fountainDimension;
        this.destinationPos = destinationPos;
        this.destinationDimension = destinationDimension;
        this.openingTick = openingTick;
        this.frameTick = frameTick;
        this.frame = frame;
        this.frameOptimized = frameOptimized;
        this.teleportedEntities = teleportedEntities;
        this.shockwaveTickers = shockwaveTickers;
        this.openingTickTarget = openingTick;
        this.sealingTick = sealingTick;
        this.sealingFrameTick = sealingFrameTick;
        this.sealingFrameTickProgress = sealingFrameTickProgress;
    }

    public void tick(Level level) {
        if (!level.isClientSide()) {
            if (!DarkWorldUtil.isDarkWorld(level)) {
                if (level instanceof ServerLevel serverLevel) {
                    tickRoomDarknessFill(serverLevel);
                    tickOutsideDoorToGreatDoor(serverLevel);
                    tickDarkWorldTransportTickers(serverLevel);
                    tickRoomDissipation(serverLevel);
                    tickDarkWorldTeleportContact(serverLevel);

                    rescanTimer++;
                    if (rescanTimer >= ServerConfig.rescanInterval && (this.openingTick == -1)) {
                        rescanTimer = 0;
                        tickRoomManagement(serverLevel);
                    }
                }

                if (this.openingTick > 125 || this.openingTick == -1) {
                    if (level.getGameTime() % 2 == 0) {
                        double x = fountainPos.getX() + 0.5;
                        double y = fountainPos.getY();
                        double z = fountainPos.getZ() + 0.5;
                        double vx = ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f);
                        double vy = ModUtil.getBoundRandomFloatStatic(level, 0f, 0.1f);
                        double vz = ModUtil.getBoundRandomFloatStatic(level, -0.03f, 0.03f);

                        level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), x, y, z, vx, vy, vz);
                        level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), x, y, z, vx, vy, vz);
                        level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), x, y, z, vx, vy, vz);

                        PacketHandlerRegistry.INSTANCE.send(
                                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 32.0, level.dimension())),
                                new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get()), x, y, z, vx, vy, vz, 1)
                        );
                        PacketHandlerRegistry.INSTANCE.send(
                                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 32.0, level.dimension())),
                                new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get()), x, y, z, vx, vy, vz, 1)
                        );
                        PacketHandlerRegistry.INSTANCE.send(
                                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 32.0, level.dimension())),
                                new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get()), x, y, z, vx, vy, vz, 1)
                        );
                    }
                }
            } else if (DarkWorldUtil.isDepths(level)) {
                if (level instanceof ServerLevel serverLevel) {
                    if (sealingTick < 0 && openingTick < 0) {
                        tickDepthsFountain(serverLevel);
                    }

                    if (openingTick == 1) {
                        serverLevel.players().forEach(player -> {
                            level.playSound(null, player.getOnPos().above(), SoundRegistry.FOUNTAIN_MAKE_DEPTHS.get(), SoundSource.AMBIENT, 0.75f, 1f);
                        });
                    }

                    if (sealingTick >= 0) {
                        if (sealingTick < SEAL_DURATION + SEAL_FLASH_DELAY + SEAL_FLASH_DURATION + 40) {
                            sealingTick++;
                        }
                    }
                }
            } else {
                if (level instanceof ServerLevel serverLevel) {
                    tickDarkWorldFountainPushing(serverLevel);
                }

                if (this.sealingTick == 0 && level instanceof ServerLevel sealingServerLevel) {
                    BlockPos sealingFountainPos = this.getFountainPos();
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.DIMENSION.with(sealingServerLevel::dimension),
                            new ClientBoundSoundPackets.FountainWind(sealingFountainPos, true)
                    );
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.DIMENSION.with(sealingServerLevel::dimension),
                            new ClientBoundSoundPackets.FountainMusic(sealingFountainPos, true)
                    );
                }

                if (this.sealingTick >= 0) {
                    if (this.sealingFrameTick >= 0) {
                        float delta = Mth.clamp((float) this.sealingTick / (float) SEAL_DURATION, 0, 1);
                        float frameSpeed = Mth.lerp(delta, 1, 0);
                        frameSpeed *= frameSpeed;

                        this.sealingFrameTickProgress += frameSpeed;

                        while (this.sealingFrameTickProgress >= 1) {
                            this.sealingFrameTickProgress -= 1;
                            if (this.sealingFrameTick >= 27 * 3) {
                                this.sealingFrameTick = 0;
                            } else {
                                this.sealingFrameTick++;
                            }

                            if (this.sealingFrameTick % 3 == 0) {
                                if (this.frame >= 27) {
                                    this.frame = 0;
                                } else {
                                    this.frame++;
                                }
                            }
                            if (this.sealingFrameTick % 6 == 0) {
                                if (this.frameOptimized >= 5) {
                                    this.frameOptimized = 0;
                                } else {
                                    this.frameOptimized++;
                                }
                            }
                        }
                    }
                } else {
                    if (this.frameTick % 3 == 0) {
                        if (this.frame >= 27) {
                            this.frame = 0;
                        } else {
                            this.frame++;
                        }
                    }
                    if (this.frameTick % 6 == 0) {
                        if (this.frameOptimized >= 5) {
                            this.frameOptimized = 0;
                        } else {
                            this.frameOptimized++;
                        }
                    }

                    if (this.frameTick >= 27 * 3) {
                        this.frameTick = 0;
                    } else {
                        this.frameTick++;
                    }
                }

                if (this.sealingTick >= 0) {
                    tickFountainSealing(level);
                    if (sealingTick < SEAL_DURATION + SEAL_FLASH_DELAY + SEAL_FLASH_DURATION + 40) {
                        sealingTick++;
                    }
                }
            }

            if (!DarkWorldUtil.isDepths(level) && this.openingTick == 1) {
                level.playSound(null, fountainPos, SoundRegistry.FOUNTAIN_MAKE.get(), SoundSource.AMBIENT, 0.5f, 1f);
            }

            if (this.openingTick == 0) {
                this.shockwaveTickers.add(0);
            }
            if (this.openingTick % 3 == 0 && this.openingTick < OPENING_FINISH - 10) {
                this.shockwaveTickers.add(0);
            }

            for (int i = shockwaveTickers.size() - 1; i >= 0; i--) {
                int ticker = shockwaveTickers.get(i);
                if (ticker < 5) {
                    shockwaveTickers.set(i, ticker + 1);
                } else {
                    shockwaveTickers.remove(i);
                }
            }

            if (this.openingTick >= FILL_START_TICK) {
                this.openingTick = -1;
            }
            if (this.openingTick >= 0) {
                this.openingTick++;
            }

            if ((this.openingTick > 125 || this.openingTick == -1) && this.sealingTick < 0) {
                tickSoundPackets(level);
            }
        }

        boolean stillPresent = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN)
                .map(cap -> cap.darkFountains.get(this.fountainPos) == this)
                .orElse(false);
        if (stillPresent) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientBoundSingleFountainData(this));
        }
    }

    public void clientTickOpening() {
        this.openingTickClientO = this.openingTickClient;

        if (!this.openingTickClientInitialized) {
            this.openingTickClientInitialized = true;
            this.openingTickClientO = this.openingTickTarget;
            this.openingTickClient = this.openingTickTarget;
            return;
        }

        if (this.openingTickTarget < 0f) {
            this.openingTickClientO = this.openingTickTarget;
            this.openingTickClient = this.openingTickTarget;
            return;
        }

        float diff = this.openingTickTarget - this.openingTickClient;
        if (diff > 0f) {
            this.openingTickClient += Math.min(diff, 1f);
        } else if (diff < 0f) {
            this.openingTickClient -= Math.min(-diff, 1f);
        }
    }

    public float getOpeningTick(float partialTick) {
        if (!this.openingTickClientInitialized) {
            return this.openingTickTarget + partialTick;
        }
        return Mth.lerp(partialTick, this.openingTickClientO, this.openingTickClient);
    }

    private void tickRoomDarknessFill(ServerLevel level) {
        for (DarkRoom room : rooms) {
            if (!room.isFilling()) continue;

            boolean isInitialRoom = room.getSeedPos().equals(fountainPos);
            int fillRate;

            if (isInitialRoom && this.openingTick >= 0) {
                if (this.openingTick < FILL_START_TICK) continue;
            }
            fillRate = Math.max(1, room.getPositions().size() / TRANSPORT_TICKER_DURATION);

            int placed = 0;
            while (room.fillIndex < room.getPositions().size() && placed < fillRate) {
                BlockPos pos = room.getPositions().get(room.fillIndex);
                BlockState current = level.getBlockState(pos);
                if (current.isAir()) {
                    level.setBlock(pos, BlockRegistry.DARKNESS.get().defaultBlockState(), 3);
                    if (level.getBlockEntity(pos) instanceof DarknessBlockEntity darkness) {
                        darkness.fountainPos = this.fountainPos;
                    }
                    placed++;
                }
                else if (current.getBlock() instanceof DarknessBlock) {
                    if (level.getBlockEntity(pos) instanceof DarknessBlockEntity darkness) {
                        darkness.fountainPos = this.fountainPos;
                        darkness.setChanged();
                    }
                }
                room.fillIndex++;
            }

            room.checkActivation();
        }
    }

    public void tickFountainSealing(Level level) {
        if (this.sealingTick >= SEAL_DURATION + SEAL_FLASH_DELAY + SEAL_FLASH_DURATION + 40) {
            if (!(level instanceof ServerLevel soulLevel)) {
                return;
            }

            if (!DarkWorldUtil.isDarkWorld(level)) {
                return;
            }

            DarkFountainCapability darkFountainCapability = null;
            LazyOptional<DarkFountainCapability> darkLazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
            if(darkLazyCapability.isPresent() && darkLazyCapability.resolve().isPresent())
                darkFountainCapability = darkLazyCapability.resolve().get();

            if (darkFountainCapability == null){
                return;
            }

            DarkFountain darkFountain = darkFountainCapability.darkFountains.get(fountainPos);

            if (darkFountain == null){
                return;
            }

            ServerLevel lightLevel = soulLevel.getServer().getLevel(destinationDimension);

            if (lightLevel == null) {
                return;
            }

            //Teleport all players to light fountain
            for (Player player : new ArrayList<>(soulLevel.players())) {
                if (player instanceof ServerPlayer serverPlayer) {

                    Vec3 lightPos = destinationPos.getCenter();

                    serverPlayer.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
                        cap.sealShineTicker = -1;
                        cap.syncToClient(serverPlayer);
                    });

                    serverPlayer.teleportTo(lightLevel, lightPos.x, lightPos.y,
                            lightPos.z, player.getYHeadRot(), player.getXRot());
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
                }
            }

            //Get light fountain capability
            DarkFountainCapability lightFountainCapability = null;
            LazyOptional<DarkFountainCapability> lightLazyCapability = lightLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
            if(lightLazyCapability.isPresent() && lightLazyCapability.resolve().isPresent())
                lightFountainCapability = lightLazyCapability.resolve().get();

            if (lightFountainCapability == null){
                return;
            }

            //Get light fountain from destination pos
            DarkFountain lightFountain = lightFountainCapability.darkFountains.get(destinationPos);

            if (lightFountain != null) {
                for (DarkRoom room : lightFountain.rooms) {
                    for (BlockPos pos : room.getPositions()) {
                        if (lightLevel.getBlockState(pos).getBlock() instanceof DarknessBlock) {
                            lightLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
                lightFountainCapability.removeDarkFountain(lightLevel, lightFountain.fountainPos);
            }

            darkFountainCapability.removeDarkFountain(level, fountainPos);
            removeDepthsTwin(soulLevel);

            if (level instanceof ServerLevel serverLevel) {
                ChunkPos soulChunk = new ChunkPos(this.fountainPos);
                serverLevel.setChunkForced(soulChunk.x, soulChunk.z, false);
            }
        } else {
            if (this.sealingTick == 0) {
                if (level instanceof ServerLevel serverLevel) {
                    ChunkPos soulChunk = new ChunkPos(this.fountainPos);
                    serverLevel.setChunkForced(soulChunk.x, soulChunk.z, true);
                }
            }
        }
    }

    private void tickDarkWorldTransportTickers(ServerLevel level) {
        if (this.openingTick >= 0 && this.openingTick < FILL_START_TICK) return;

        ServerLevel destinationLevel = level.getServer().getLevel(this.destinationDimension);
        if (destinationLevel == null) return;

        Vec3 lightAnchor = Vec3.atBottomCenterOf(this.fountainPos);

        for (DarkRoom room : rooms) {
            if (room.isDissipating()) continue;

            Set<BlockPos> posSet = new HashSet<>(room.getPositions());
            List<UUID> completedThisTick = new ArrayList<>();
            List<Map.Entry<UUID, Integer>> snapshot = new ArrayList<>(room.getTransportTickers().entrySet());

            for (Map.Entry<UUID, Integer> snapshotEntry : snapshot) {
                UUID entityId = snapshotEntry.getKey();
                Integer boxedTicker = room.getTransportTickers().get(entityId);
                if (boxedTicker == null) {
                    continue;
                }
                int ticker = boxedTicker;

                Entity entity = level.getEntity(entityId);
                if (entity == null) {
                    room.getTransportTickers().remove(entityId);
                    continue;
                }
                if (!(entity instanceof ServerPlayer serverPlayer)) {
                    room.getTransportTickers().remove(entityId);
                    continue;
                }

                boolean inRoom = posSet.contains(entity.blockPosition()) || posSet.contains(entity.blockPosition().above());

                if (inRoom) {
                    ticker = Math.min(ticker + 1, TRANSPORT_TICKER_DURATION);
                    room.getTransportTickers().put(entityId, ticker);

                    int finalTicker = ticker;
                    serverPlayer.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> cap.darknessOverlayTicker = finalTicker);

                    if (ticker == TRANSPORT_TICKER_DURATION) {
                        completedThisTick.add(entityId);
                    }
                } else {
                    ticker = Math.max(ticker - 1, 0);
                    int finalTicker = ticker;
                    serverPlayer.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> cap.darknessOverlayTicker = finalTicker);
                    if (ticker == 0) {
                        room.getTransportTickers().remove(entityId);
                    } else {
                        room.getTransportTickers().put(entityId, ticker);
                    }
                }
            }

            if (!completedThisTick.isEmpty()) {
                DarkFountain destinationFountain = destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).map(cap ->
                                cap.darkFountains.get(destinationPos)).orElse(null);

                if (destinationFountain != null) {
                    Vec3 base = getRandomTeleportTarget(destinationLevel, ServerConfig.fountainContactTeleportMinRadius, ServerConfig.fountainContactTeleportMaxRadius);

                    for (UUID completedId : completedThisTick) {
                        ServerPlayer player = level.getServer().getPlayerList().getPlayer(completedId);

                        if (player == null || !player.level().dimension().equals(level.dimension())) {
                            room.getTransportTickers().remove(completedId);
                            continue;
                        }

                        double tx = base.x + (player.getX() - lightAnchor.x);
                        double tz = base.z + (player.getZ() - lightAnchor.z);
                        double ty = ModUtil.worldSurfaceYAtXZ(destinationLevel, tx, tz);
                        Vec3 target = new Vec3(tx, ty, tz);

                        teleportPlayer(player, destinationLevel, target, player.getYRot(), player.getXRot());

                        room.getTransportTickers().remove(completedId);
                    }
                } else {
                    for (UUID completedId : completedThisTick) {
                        room.getTransportTickers().remove(completedId);
                    }
                }
            }

            for (Map.Entry<UUID, Integer> entry : room.getTransportTickers().entrySet()) {
                Entity entity = level.getEntity(entry.getKey());

                if (!(entity instanceof ServerPlayer player)) {
                    continue;
                }

                float progress = (float) entry.getValue() / TRANSPORT_TICKER_DURATION;
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(progress));
            }

            room.checkActivation();
        }
    }

    private void tickRoomDissipation(ServerLevel level) {
        Iterator<DarkRoom> roomIt = rooms.iterator();
        while (roomIt.hasNext()) {
            DarkRoom room = roomIt.next();
            if (!room.isDissipating()) continue;

            for (int i = 0; i < ServerConfig.dissipationRate && !room.dissipationQueue.isEmpty(); i++) {
                BlockPos pos = room.dissipationQueue.remove(0);
                if (level.getBlockState(pos).getBlock() instanceof DarknessBlock) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                room.getPositions().remove(pos);
            }

            for (Map.Entry<UUID, Integer> entry : room.getTransportTickers().entrySet()) {
                Entity entity = level.getEntity(entry.getKey());
                if (entity instanceof ServerPlayer player) {
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(0f));
                }
            }
            room.getTransportTickers().clear();

            if (room.dissipationQueue.isEmpty()) {
                roomIt.remove();
            }
        }
    }

    public static @Nullable Set<BlockPos> otherFountainAnchors(ServerLevel level, @Nullable BlockPos excludeFountainAnchor) {
        HashSet<BlockPos> anchors = new HashSet<>();
        level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
            for (Map.Entry<BlockPos, DarkFountain> e : cap.darkFountains.entrySet()) {
                if (e.getKey().equals(excludeFountainAnchor)) continue;
                anchors.add(e.getValue().getFountainPos());
            }
        });
        return anchors.isEmpty() ? null : anchors;
    }

    public static Map<BlockPos, ResourceKey<Level>> otherFountainRoomCellsToDarkWorld(ServerLevel level, @Nullable BlockPos excludeFountainAnchor) {
        Map<BlockPos, ResourceKey<Level>> map = new HashMap<>();

        level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
            for (Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet()) {
                if (entry.getKey().equals(excludeFountainAnchor)) continue;

                ResourceKey<Level> destDim = entry.getValue().getDestinationDimension();
                for (DarkRoom room : entry.getValue().rooms) {
                    if (!room.isDissipating()) {
                        for (BlockPos roomBlock : room.getPositions()) {
                            map.put(roomBlock, destDim);
                        }
                        for (BlockPos doorBlock : room.getDoorPositions()) {
                            map.put(doorBlock, destDim);
                        }
                    }
                }
            }
        });

        return map;
    }

    @Nullable
    private Set<BlockPos> collectOtherFountainAnchors(ServerLevel level) {
        return otherFountainAnchors(level, this.fountainPos);
    }

    private void tickRoomManagement(ServerLevel level) {
        Set<BlockPos> otherFountains = collectOtherFountainAnchors(level);
        Map<BlockPos, ResourceKey<Level>> otherRoomBlocks = otherFountainRoomCellsToDarkWorld(level, this.fountainPos);

        if (rooms.isEmpty()) {
            RoomScanner.RoomScanResult result = RoomScanner.scan(level, fountainPos, ServerConfig.maxRoomVolume, false, false,
                    otherFountains, otherRoomBlocks);
            if (result.isValid()) {
                DarkRoom newRoom = new DarkRoom(fountainPos, result.getPositions(), result.getDoorPositions(), result.getOutsideDoors(), result.getSharedDoors());

                addEntitiesInRoomToTickers(level, newRoom);
                rooms.add(newRoom);
            }
            return;
        }

        for (DarkRoom room : rooms) {
            if (room.isDissipating()) continue;

            RoomScanner.RoomScanResult result = RoomScanner.scan(level, room.getSeedPos(), ServerConfig.maxRoomVolume, true, true,
                    otherFountains, otherRoomBlocks);
            if (result.isValid()) {
                room.positions = result.getPositions();
                room.doorPositions = result.getDoorPositions();
                room.outsideDoors = new HashMap<>(result.getOutsideDoors());
                room.sharedDoors = new HashMap<>(result.getSharedDoors());
                room.fillIndex = 0;

                int totalDarkness = DarkRoom.getTotalDarknessCount(rooms);
                int remainingVolume = ServerConfig.maxRoomVolume - totalDarkness;

                RoomScanner.reclassifyDoorsWithRemainingBudget(level, room, Math.max(0, remainingVolume), otherFountains, otherRoomBlocks);
            }
        }

        tickConnectivityViaDoors(level);
        tickExpansionThroughDoors(level, otherFountains, otherRoomBlocks);
    }

    private void tickConnectivityViaDoors(ServerLevel level) {
        if (rooms.isEmpty()) return;

        DarkRoom fountainRoom = null;
        for (DarkRoom room : rooms) {
            if (room.getSeedPos().equals(fountainPos) && !room.isDissipating()) {
                fountainRoom = room;
                break;
            }
        }

        if (fountainRoom == null) {
            for (DarkRoom room : rooms) {
                if (!room.isDissipating()) room.beginDissipation();
            }
            return;
        }

        Set<DarkRoom> reachableViaOpenDoors = new HashSet<>();
        Queue<DarkRoom> queue = new LinkedList<>();
        reachableViaOpenDoors.add(fountainRoom);
        queue.add(fountainRoom);

        while (!queue.isEmpty()) {
            DarkRoom current = queue.poll();

            for (DarkRoom other : rooms) {
                if (reachableViaOpenDoors.contains(other) || other.isDissipating()) continue;

                if (DarkRoom.sharesAnOpenDoor(level, current, other)) {
                    reachableViaOpenDoors.add(other);
                    queue.add(other);
                }
            }
        }

        for (DarkRoom room : rooms) {
            if (!room.isDissipating() && !reachableViaOpenDoors.contains(room)) {
                room.beginDissipation();
            }
        }
    }

    private void tickExpansionThroughDoors(ServerLevel level, @Nullable Set<BlockPos> otherFountainAnchors, Map<BlockPos, ResourceKey<Level>> otherRoomCells) {
        //Subtract total used volume from max volume, if zero or below, don't expand
        int remainingVolume = ServerConfig.maxRoomVolume - DarkRoom.getTotalDarknessCount(rooms);
        if (remainingVolume <= 0) return;

        Set<BlockPos> allPositions = new HashSet<>();
        for (DarkRoom room : rooms) {
            allPositions.addAll(room.getPositions());
        }

        List<DarkRoom> newRooms = new ArrayList<>();
        for (DarkRoom room : rooms) {
            if (!room.isActive() && !room.isFillComplete()) continue;
            if (room.isDissipating()) continue;

            for (BlockPos doorPos : room.getDoorPositions()) {
                BlockState doorState = level.getBlockState(doorPos);

                if (!(doorState.getBlock() instanceof DoorBlock) || !doorState.getValue(DoorBlock.OPEN)) continue;

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos adjacent = doorPos.relative(dir);

                    if (allPositions.contains(adjacent)) continue;

                    if (otherFountainAnchors != null && otherFountainAnchors.contains(adjacent)) continue;

                    BlockState adjState = level.getBlockState(adjacent);

                    if (!adjState.isAir()) continue;

                    RoomScanner.RoomScanResult result = RoomScanner.scan(level, adjacent, remainingVolume, false, false,
                            otherFountainAnchors, otherRoomCells);

                    if (result.isValid()) {
                        DarkRoom newRoom = new DarkRoom(adjacent, result.getPositions(), result.getDoorPositions(), result.getOutsideDoors(),
                                result.getSharedDoors());
                        addEntitiesInRoomToTickers(level, newRoom);
                        newRooms.add(newRoom);
                        allPositions.addAll(result.getPositions());
                        remainingVolume -= result.getPositions().size();

                        if (remainingVolume <= 0) break;
                    }
                }

                if (remainingVolume <= 0) break;
            }

            if (remainingVolume <= 0) break;
        }

        rooms.addAll(newRooms);
    }

    private void addEntitiesInRoomToTickers(ServerLevel level, DarkRoom room) {
        if (room.getPositions().isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : room.getPositions()) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        AABB roomBox = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        Set<BlockPos> posSet = new HashSet<>(room.getPositions());

        for (ServerPlayer entity : level.getEntitiesOfClass(ServerPlayer.class, roomBox)) {
            if (posSet.contains(entity.blockPosition()) || posSet.contains(entity.blockPosition().above())) {
                room.getTransportTickers().put(entity.getUUID(), 0);
            }
        }
    }

    private void tickDarkWorldTeleportContact(ServerLevel level) {
        ServerLevel destinationLevel = level.getServer().getLevel(this.destinationDimension);
        if (destinationLevel == null) return;

        for (DarkRoom room : rooms) {
            if (!room.isActive()) continue;

            Set<BlockPos> posSet = new HashSet<>(room.getPositions());
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (BlockPos pos : room.getPositions()) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }
            AABB roomBox = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, roomBox)) {
                if (this.teleportedEntities.contains(player.getUUID())) continue;
                if (!posSet.contains(player.blockPosition()) && !posSet.contains(player.blockPosition().above())) continue;

                destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                    DarkFountain destinationFountain = cap.darkFountains.get(destinationPos);

                    if (destinationFountain != null) {
                        Vec3 target = getRandomTeleportTarget(destinationLevel, ServerConfig.fountainContactTeleportMinRadius,
                                ServerConfig.fountainContactTeleportMaxRadius);

                        float yaw = (float) Math.toDegrees(Math.atan2(-((destinationPos.getX() + 0.5) - target.x), (destinationPos.getZ() + 0.5) - target.z));

                        level.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);

                        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(0f));
                        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundDarknessFallPacket(destinationPos,
                                target.x, target.y, target.z, yaw, destinationDimension, false, BlockPos.ZERO));
                    }
                });
                this.teleportedEntities.add(player.getUUID());
            }
        }
        clearLightFountainTeleportedEntities(level);
    }

    private static AABB lightDoorLowerColumnBox(BlockPos lowerFoot) {
        return new AABB(lowerFoot.getX(), lowerFoot.getY(), lowerFoot.getZ(), lowerFoot.getX() + 1, lowerFoot.getY() + 2, lowerFoot.getZ() + 1);
    }

    private void tickOutsideDoorToGreatDoor(ServerLevel level) {
        if (this.openingTick >= 0 && this.openingTick < FILL_START_TICK) return;

        ServerLevel destinationLevel = level.getServer().getLevel(this.destinationDimension);
        if (destinationLevel == null) return;

        for (DarkRoom room : rooms) {
            if (!room.isActive()) continue;

            for (Map.Entry<BlockPos, DarkRoom.OutsideDoorExit> entry : room.getOutsideDoors().entrySet()) {
                BlockPos doorLowerPos = entry.getKey();
                BlockPos doorPos = DarkWorldUtil.getLowerDoor(level, doorLowerPos);

                if (!doorPos.equals(doorLowerPos)) continue;

                DarkRoom.OutsideDoorExit outsideExit = entry.getValue();
                Direction directionFromInside = outsideExit.exitFromInterior();
                BlockState doorState = level.getBlockState(doorPos);

                if (!(doorState.getBlock() instanceof DoorBlock)) continue;

                Direction directionFromOutside = directionFromInside.getOpposite();
                boolean isVisuallyOpen = DarknessBlock.isDoorVisuallyOpenFromSide(level, doorPos, doorState, directionFromOutside);
                BlockPos doubleLowerHalf = outsideExit.doubleLowerHalf();

                if (doubleLowerHalf != null) {
                    BlockPos doubleLowerPos = doubleLowerHalf;
                    BlockState doubleLowerState = level.getBlockState(doubleLowerPos);

                    if (doubleLowerState.getBlock() instanceof DoorBlock && doubleLowerState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                        doubleLowerPos = doubleLowerHalf.below();
                        doubleLowerState = level.getBlockState(doubleLowerPos);
                    }

                    if (doubleLowerState.getBlock() instanceof DoorBlock) {
                        isVisuallyOpen = isVisuallyOpen || DarknessBlock.isDoorVisuallyOpenFromSide(level, doubleLowerPos, doubleLowerState, directionFromOutside);
                    }
                }

                if (!isVisuallyOpen) continue;

                AABB doorBoundingBox = lightDoorLowerColumnBox(doorPos);

                if (doubleLowerHalf != null) {
                    doorBoundingBox = doorBoundingBox.minmax(lightDoorLowerColumnBox(doubleLowerHalf));
                }

                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, doorBoundingBox)) {
                    if (!player.level().dimension().equals(level.dimension())) continue;
                    if (player.isSpectator()) continue;

                    BlockPos feetPos = player.blockPosition();
                    BlockState feetState = level.getBlockState(feetPos);

                    if (!(feetState.getBlock() instanceof DoorBlock)) continue;

                    BlockPos feetLower = feetState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? feetPos.below() : feetPos;
                    BlockPos feetDoor = DarkWorldUtil.getLowerDoor(level, feetLower);

                    if (!feetDoor.equals(doorPos)) continue;
                    if (this.teleportedEntities.contains(player.getUUID())) continue;

                    GreatDoor greatDoor = DarkWorldUtil.ensureGreatDoorForOutsideDoor(destinationLevel, this.destinationPos, doorPos, level.dimension(),
                            directionFromInside, doubleLowerHalf);

                    if (greatDoor == null) continue;

                    Vec3 spawn = GreatDoor.spawnCenterInFrontOfGreatDoor(destinationLevel, greatDoor.greatDoorPos, greatDoor.direction);
                    float yaw = greatDoor.direction.toYRot();
                    this.teleportedEntities.add(player.getUUID());
                    player.invulnerableTime = 60;

                    level.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(0f));
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundDarknessFallPacket(this.destinationPos, spawn.x, spawn.y, spawn.z, yaw, this.destinationDimension, true, greatDoor.greatDoorPos));
                }
            }
        }
    }

    private void clearLightFountainTeleportedEntities(ServerLevel level) {
        HashSet<UUID> next = new HashSet<>();
        Set<BlockPos> outsideDoorLower = new HashSet<>();
        for (DarkRoom room : rooms) {
            if (room.isDissipating()) continue;

            for (BlockPos doorPos : room.getOutsideDoors().keySet()) {
                DarkWorldUtil.addDoorStandingLower(level, doorPos, outsideDoorLower);
            }
        }

        for (UUID id : this.teleportedEntities) {
            Entity entity = level.getEntity(id);

            if (entity == null) continue;

            BlockPos feet = entity.blockPosition();

            if (outsideDoorLower.contains(feet)) {
                next.add(id);
                continue;
            }

            boolean isDarknessBlock = false;
            for (DarkRoom room : rooms) {
                if (room.isDissipating()) continue;
                for (BlockPos roomBlock : room.getPositions()) {
                    if (!roomBlock.equals(feet) && !roomBlock.equals(feet.above())) continue;

                    if (level.getBlockState(roomBlock).getBlock() instanceof DarknessBlock) {
                        isDarknessBlock = true;
                        break;
                    }
                }

                if (isDarknessBlock) break;
            }

            if (isDarknessBlock) {
                next.add(id);
            }
        }

        this.teleportedEntities = next;
    }

    private void tickDarkWorldFountainPushing(ServerLevel level) {
        AABB pushBox = new AABB(fountainPos.offset(0, 5, 0)).inflate(5).setMaxY(level.dimensionType().height());
        Vec3 center = fountainPos.getCenter();

        for (Entity entity : level.getEntitiesOfClass(Entity.class, pushBox)) {
            Vec3 entityPos = entity.position();
            double dx = entityPos.x - center.x;
            double dz = entityPos.z - center.z;
            double xz = Math.sqrt(dx * dx + dz * dz);

            double pushStrength = 3;
            boolean showPushMessage = true;

            if (entity instanceof ServerPlayer serverPlayer) {
                if (xz >= 4) {
                    this.depthsTransit.remove(serverPlayer.getUUID());
                }

                if (this.sealingTick > 0) {
                    pushStrength = DEPTHS_PIERCE_PUSH_STRENGTH;
                    showPushMessage = false;
                } else if (canPierceFountain(serverPlayer) && !this.depthsTransit.contains(serverPlayer.getUUID())) {
                    if (xz < DEPTHS_PIERCE_XZ && entityPos.y > fountainPos.getY() - 1) {
                        enterDepths(serverPlayer, level);
                        continue;
                    }

                    pushStrength = DEPTHS_PIERCE_PUSH_STRENGTH;
                    showPushMessage = false;
                }
            }

            Vec3 awayVec;
            double distance;

            if (entityPos.y < center.y) {
                awayVec = entityPos.subtract(center);
            } else {
                awayVec = new Vec3(dx, 0, dz);
            }

            distance = awayVec.length();
            if (distance >= 4 || distance < 0.0001) continue;

            Vec3 directionVec = awayVec.scale(1 / distance);
            double falloff = 1 - distance / 4;
            Vec3 pushAwayVec = directionVec.scale(pushStrength * falloff);

            entity.push(pushAwayVec.x, pushAwayVec.y, pushAwayVec.z);

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));

                if (showPushMessage) {
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.DARK_FOUNTAIN_PUSH_AWAY));
                }
            }
        }
    }

    private static boolean canPierceFountain(ServerPlayer player) {
        if (player.isCreative()) return true;

        SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);

        return soulCap.determination >= 100;
    }

    private void enterDepths(ServerPlayer player, ServerLevel darkLevel) {
        if (this.depthsPos == null) return;

        ServerLevel depths = DarkWorldUtil.getDepths(darkLevel.getServer());
        if (depths == null) return;

        DarkFountain depthsFountain = depths.getCapability(CapabilityRegistry.DARK_FOUNTAIN).map(cap -> cap.darkFountains.get(this.depthsPos))
                .orElse(null);

        if (depthsFountain == null) return;

        player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
            cap.depthsEntryTicker = 0;
            cap.darknessOverlayTicker = TRANSPORT_TICKER_DURATION;
            cap.syncToClient(player);
        });

        Vec3 spawn = Vec3.atCenterOf(this.depthsPos).add(DEPTHS_EJECT_OFFSET, -1, 0);

        player.fallDistance = 0f;
        player.teleportTo(depths, spawn.x, spawn.y, spawn.z, player.getYRot(), player.getXRot());
        player.fallDistance = 0f;
        player.setDeltaMovement(0, -10, 0);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        depthsFountain.depthsTransit.add(player.getUUID());
        depthsFountain.teleportedEntities.add(player.getUUID());
    }

    private void tickDepthsFountain(ServerLevel level) {
        Vec3 opening = this.fountainPos.getCenter();
        double suctionY = DEPTHS_SUCTION_Y;
        AABB suctionBox = new AABB(opening, opening).inflate(DEPTHS_SUCTION_XZ, 0, DEPTHS_SUCTION_XZ).expandTowards(0, -suctionY, 0)
                .expandTowards(0, 2, 0);
        AABB contactBox = new AABB(opening, opening).inflate(DEPTHS_CONTACT_XZ, DEPTHS_CONTACT_Y, DEPTHS_CONTACT_XZ);

        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, suctionBox)) {
            double dx = player.getX() - opening.x;
            double dz = player.getZ() - opening.z;
            double xz = Math.sqrt(dx * dx + dz * dz);

            if (xz > DEPTHS_SUCTION_XZ) {
                this.teleportedEntities.remove(player.getUUID());
                this.depthsTransit.remove(player.getUUID());

                continue;
            }

            if (this.teleportedEntities.contains(player.getUUID()) || this.depthsTransit.contains(player.getUUID())) continue;

            if (player.getY() > opening.y + 0.5) continue;

            double dy = opening.y - player.getY();
            if (dy < -0.5 || dy > suctionY) continue;


            if (player.getBoundingBox().intersects(contactBox) || player.getEyeY() >= opening.y - 0.35) {
                ejectFromDepths(player, level);
                continue;
            }

            Vec3 toOpening = opening.subtract(player.position());
            double dist = Math.max(toOpening.length(), 0.0001);
            Vec3 pull = toOpening.scale((0.45 + 0.55 * (1 - dist / suctionY)) / dist);

            player.push(pull.x, pull.y, pull.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

    private void ejectFromDepths(ServerPlayer player, ServerLevel depthsLevel) {
        ServerLevel darkLevel = depthsLevel.getServer().getLevel(this.destinationDimension);

        if (darkLevel == null) return;

        DarkFountain darkFountain = darkLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).map(cap ->
                cap.darkFountains.get(this.destinationPos)).orElse(null);

        Vec3 dest = Vec3.atCenterOf(this.destinationPos).add(DEPTHS_EJECT_OFFSET, 0, 0);

        player.teleportTo(darkLevel, dest.x, dest.y, dest.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(0.45, 0.15, 0);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        if (darkFountain != null) {
            darkFountain.depthsTransit.add(player.getUUID());
        }
    }

    private void removeDepthsTwin(ServerLevel darkLevel) {
        if (this.depthsPos == null) return;

        ServerLevel depths = DarkWorldUtil.getDepths(darkLevel.getServer());

        if (depths == null) return;

        depths.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> cap.removeDarkFountain(depths, this.depthsPos));
        this.depthsPos = null;
    }

    public static int scaleDepthsAxis(int originAxis) {
        return -1 * originAxis / DEPTHS_XZ_SCALE;
    }

    public static boolean isDepthsOccupied(MinecraftServer server, Vec2 depthsPos) {
        ServerLevel depths = DarkWorldUtil.getDepths(server);

        if (depths == null) return false;

        return isDepthsOccupied(depths, depthsPos);
    }

    public static boolean isDepthsOccupied(ServerLevel depths, Vec2 depthsPos) {
        return depths.getCapability(CapabilityRegistry.DARK_FOUNTAIN).map(cap -> {
            for (DarkFountain fountain : cap.darkFountains.values()) {
                Vec2 originPos = new Vec2(depthsPos.x, depthsPos.y);
                Vec2 fountainPos = new Vec2(fountain.fountainPos.getX(), fountain.fountainPos.getZ());

                return originPos.distanceToSqr(fountainPos) < Mth.square(16);
            }

            return false;
        }).orElse(false);
    }

    public static Vec2 getBumpedDepthsXZ(MinecraftServer server, Vec2 originPos) {
        ServerLevel depths = DarkWorldUtil.getDepths(server);

        if (depths == null) return originPos;

        return getBumpedDepthsXZ(depths, originPos);
    }

    public static Vec2 getBumpedDepthsXZ(ServerLevel depths, Vec2 originPos) {
        DarkFountainCapability capability;
        LazyOptional<DarkFountainCapability> darkLazyCapability = depths.getCapability(CapabilityRegistry.DARK_FOUNTAIN);

        if(darkLazyCapability.isPresent() && darkLazyCapability.resolve().isPresent())
            capability = darkLazyCapability.resolve().get();
        else {
            return new Vec2(originPos.x, originPos.y);
        }

        List<Vec2> fountainsTooClose = new ArrayList<>();

        for (DarkFountain fountain : capability.darkFountains.values()) {
            Vec2 fountainPos = new Vec2(fountain.fountainPos.getX(), fountain.fountainPos.getZ());
            float distance = originPos.distanceToSqr(fountainPos);

            if (distance < Mth.square(32)) {
                fountainsTooClose.add(fountainPos);
            }
        }

        if (fountainsTooClose.isEmpty()) {
            return new Vec2(originPos.x, originPos.y);
        }

        Vec2 combinedAwayVec = Vec2.ZERO;
        for (Vec2 conflictPos : fountainsTooClose) {
            Vec2 awayVec = new Vec2(originPos.x - conflictPos.x, originPos.y - conflictPos.y);

            awayVec = awayVec.normalized();
            combinedAwayVec = combinedAwayVec.add(awayVec);
        }

        return originPos.add(combinedAwayVec.normalized().scale(32));
    }

    public static BlockPos getHeightmappedDepthsPos(ServerLevel depths, Vec2 originXZ) {
        BlockPos originPos = new BlockPos((int) originXZ.x, depths.getMinBuildHeight(), (int) originXZ.y);
        ChunkPos chunkPos = new ChunkPos(originPos);

        depths.setChunkForced(chunkPos.x, chunkPos.z, true);
        BlockPos heightmap = depths.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, originPos);
        BlockPos pos = new BlockPos(originPos.getX(), heightmap.getY() + DEPTHS_FOUNTAIN_Y_OFFSET, originPos.getZ());
        depths.setChunkForced(chunkPos.x, chunkPos.z, false);

        return pos;
    }

    private void tickSoundPackets(Level level) {
        if (DarkWorldUtil.isDepths(level)) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                    new ClientBoundSoundPackets.FountainWindDepths(this.fountainPos, false)
            );
        } else if (DarkWorldUtil.isDarkWorld(level)) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                    new ClientBoundSoundPackets.FountainWind(this.fountainPos, false));
        } else {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                    new ClientBoundSoundPackets.FountainDarkness(this.fountainPos, false));
        }
    }

    private Vec3 getRandomTeleportTarget(ServerLevel destinationLevel, int teleportMinRadius, int teleportMaxRadius) {
        double angle = destinationLevel.getRandom().nextDouble() * 2 * Math.PI;
        double distance = teleportMinRadius + destinationLevel.getRandom().nextDouble() * (teleportMaxRadius - teleportMinRadius);
        double x = destinationPos.getX() + 0.5 + Math.cos(angle) * distance;
        double z = destinationPos.getZ() + 0.5 + Math.sin(angle) * distance;
        double y = ModUtil.worldSurfaceYAtXZ(destinationLevel, x, z);

        return new Vec3(x, y, z);
    }

    public void teleportPlayer(ServerPlayer player, ServerLevel destinationLevel, Vec3 targetPos, float yRot, float xRot) {
        player.teleportTo(destinationLevel, targetPos.x, targetPos.y, targetPos.z, yRot, xRot);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(0f));
    }

    public void playWind() {
        if (!this.windSound.isPlaying()) {
            this.windSound.stopSound();
            this.windSound.playSound();
        }
    }

    public void stopWind() { this.windSound.stopSound(); }

    public void playDarkness() {
        if (!this.darknessSound.isPlaying()) {
            this.darknessSound.stopSound();
            this.darknessSound.playSound();
        }
    }

    public void stopDarkness() { this.darknessSound.stopSound(); }

    public void addRoom(DarkRoom room) {
        rooms.add(room);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.put(FOUNTAIN_POS, NbtUtils.writeBlockPos(fountainPos));
        tag.putString(FOUNTAIN_DIMENSION, fountainDimension.location().toString());
        tag.put(DESTINATION_POS, NbtUtils.writeBlockPos(destinationPos));
        tag.putString(DESTINATION_DIMENSION, destinationDimension.location().toString());
        tag.putInt(OPENING_TICK, openingTick);
        tag.putInt(FRAME_TICK, frameTick);
        tag.putInt(FRAME, frame);
        tag.putInt(FRAME_OPTIMIZED, frameOptimized);

        ListTag teleportedEntitiesList = new ListTag();
        for (UUID uuid : teleportedEntities) {
            teleportedEntitiesList.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put(TELEPORTED_ENTITIES, teleportedEntitiesList);

        ListTag roomsTag = new ListTag();
        for (DarkRoom room : rooms) {
            roomsTag.add(room.save());
        }
        tag.put(ROOMS, roomsTag);

        ListTag shockwaveTickersTag = new ListTag();
        for (int ticker : shockwaveTickers) {
            shockwaveTickersTag.add(IntTag.valueOf(ticker));
        }
        tag.put(SHOCKWAVE_TICKERS, shockwaveTickersTag);

        tag.putInt(SEALING_TICK, sealingTick);
        tag.putInt(SEALING_FRAME_TICK, sealingFrameTick);
        tag.putFloat(SEALING_FRAME_TICK_PROGRESS, sealingFrameTickProgress);
        if (depthsPos != null) {
            tag.put(DEPTHS_POS, NbtUtils.writeBlockPos(depthsPos));
        }

        return tag;
    }

    public static DarkFountain load(CompoundTag tag) {
        BlockPos fountainPos = NbtUtils.readBlockPos(tag.getCompound(FOUNTAIN_POS));
        ResourceKey<Level> fountainDimension = ModUtil.stringToDimension(tag.getString(FOUNTAIN_DIMENSION));
        BlockPos destinationPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_POS));
        ResourceKey<Level> destinationDimension = ModUtil.stringToDimension(tag.getString(DESTINATION_DIMENSION));
        int openingTick = tag.getInt(OPENING_TICK);
        int frameTick = tag.getInt(FRAME_TICK);
        int frame = tag.getInt(FRAME);
        int frameOptimized = tag.getInt(FRAME_OPTIMIZED);
        HashSet<UUID> teleportedEntities = new HashSet<>();

        ListTag teleportedEntitiesTag = tag.getList(TELEPORTED_ENTITIES, Tag.TAG_STRING);
        for (Tag tg : teleportedEntitiesTag) {
            teleportedEntities.add(UUID.fromString(tg.getAsString()));
        }

        List<Integer> shockwaveTickers = new ArrayList<>();
        if (tag.contains(SHOCKWAVE_TICKERS)) {
            ListTag shockwaveTickersTag = tag.getList(SHOCKWAVE_TICKERS, Tag.TAG_INT);
            for (Tag ticker : shockwaveTickersTag) {
                shockwaveTickers.add(((IntTag) ticker).getAsInt());
            }
        }

        int sealingTick = tag.getInt(SEALING_TICK);
        int sealingFrameTick = tag.getInt(SEALING_FRAME_TICK);
        float sealingFrameTickProgress = tag.getFloat(SEALING_FRAME_TICK_PROGRESS);

        DarkFountain fountain = new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, openingTick, frameTick, frame, frameOptimized, teleportedEntities, shockwaveTickers, sealingTick, sealingFrameTick, sealingFrameTickProgress);
        if (tag.contains(DEPTHS_POS)) {
            fountain.depthsPos = NbtUtils.readBlockPos(tag.getCompound(DEPTHS_POS));
        }

        if (tag.contains(ROOMS)) {
            ListTag roomsTag = tag.getList(ROOMS, Tag.TAG_COMPOUND);
            for (Tag rt : roomsTag) {
                fountain.rooms.add(DarkRoom.load((CompoundTag) rt));
            }
        }

        return fountain;
    }

    public void sync(DarkFountain fountain) {
        this.fountainPos = fountain.fountainPos;
        this.destinationPos = fountain.destinationPos;
        this.fountainDimension = fountain.fountainDimension;
        this.destinationDimension = fountain.destinationDimension;

        this.frame = fountain.frame;
        this.openingTick = fountain.openingTick;
        this.frameTick = fountain.frameTick;
        this.frameOptimized = fountain.frameOptimized;

        this.teleportedEntities = fountain.teleportedEntities;

        this.shockwaveTickers = fountain.shockwaveTickers;

        this.openingTickTarget = fountain.openingTick;
        if (!this.openingTickClientInitialized) {
            this.openingTickClientInitialized = true;
            this.openingTickClientO = this.openingTickTarget;
            this.openingTickClient = this.openingTickTarget;
        }

        this.sealingTick = fountain.sealingTick;
        this.sealingFrameTick = fountain.sealingFrameTick;
        this.sealingFrameTickProgress = fountain.sealingFrameTickProgress;
        this.depthsPos = fountain.depthsPos;
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

    public int getOpeningTick() {
        return openingTick;
    }

    public int getFrameTick() {
        return frameTick;
    }

    public int getFrame() {
        return frame;
    }

    public int getFrameOptimized() {
        return frameOptimized;
    }

    public static class DarkFountainTeleporter implements ITeleporter {
        private final Vec3 pos;
        private final Vec3 momentum;
        private final float newYRot;
        private final float newXRot;

        public DarkFountainTeleporter(Vec3 pos, Vec3 momentum, float newYRot, float newXRot) {
            this.pos = pos;
            this.momentum = momentum;
            this.newYRot = newYRot;
            this.newXRot = newXRot;
        }

        @Override
        public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            return new PortalInfo(pos, momentum, newYRot, newXRot);
        }

        @Override
        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
            return false;
        }
    }
}