package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.ServerConfig;
import destiny.penumbra_phantasm.client.network.*;
import destiny.penumbra_phantasm.client.sound.SoundWrapper;
import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.block.entity.DarknessBlockEntity;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.*;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
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

    public static final int OPENING_FINISH = 144;
    public static final int FILL_DELAY = 60;
    public static final int FILL_START_TICK = OPENING_FINISH + FILL_DELAY;
    public static final int TRANSPORT_TICKER_DURATION = 5 * 20;
    public static final int SEAL_DURATION = 3 * 20;
    public static final int SEAL_FLASH_DELAY = 20;
    public static final int SEAL_FLASH_DURATION = 30;

    public BlockPos fountainPos;
    public ResourceKey<Level> fountainDimension;
    public BlockPos destinationPos;
    public ResourceKey<Level> destinationDimension;
    public int openingTick;
    public int frameTick;
    public int frame;
    public int frameOptimized;
    public HashSet<UUID> teleportedEntities;
    public List<DarkRoom> rooms = new ArrayList<>();
    public int rescanTimer = 0;
    public List<Integer> shockwaveTickers;
    public int sealingTick;
    public int sealingFrameTick;
    public float sealingFrameTickProgress;

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
            } else {
                if (level instanceof ServerLevel serverLevel) {
                    tickDarkWorldFountainPushing(serverLevel);
                }

                if (this.sealingTick == 0 && level instanceof ServerLevel sealingServerLevel) {
                    BlockPos sealingFountainPos = this.getFountainPos();
                    PacketHandlerRegistry.INSTANCE.send(
                            PacketDistributor.DIMENSION.with(() -> sealingServerLevel.dimension()),
                            new ClientBoundSoundPackets.FountainWind(sealingFountainPos, true)
                    );
                    PacketHandlerRegistry.INSTANCE.send(
                            PacketDistributor.DIMENSION.with(() -> sealingServerLevel.dimension()),
                            new ClientBoundSoundPackets.FountainMusic(sealingFountainPos, true)
                    );
                }

                if (this.sealingTick >= 0) {
                    if (this.sealingFrameTick >= 0) {
                        float delta = Mth.clamp((float) this.sealingTick / (float) SEAL_DURATION, 0.0F, 1.0F);
                        float frameSpeed = Mth.lerp(delta, 1.0F, 0.0F);
                        frameSpeed *= frameSpeed;

                        this.sealingFrameTickProgress += frameSpeed;

                        while (this.sealingFrameTickProgress >= 1.0F) {
                            this.sealingFrameTickProgress -= 1.0F;
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
                    if (sealingTick < SEAL_DURATION + SEAL_FLASH_DELAY + SEAL_FLASH_DURATION + 20) {
                        sealingTick++;
                    }
                }
            }

            if (this.openingTick == 1) {
                level.playSound(null, fountainPos, SoundRegistry.FOUNTAIN_MAKE.get(), SoundSource.AMBIENT, 0.5f, 1f);
            }

            if (this.openingTick == 0) {
                this.shockwaveTickers.add(0);
            }
            if (this.openingTick % 3 == 0 && this.openingTick < OPENING_FINISH - 10) {
                this.shockwaveTickers.add(0);
            }

            List<Integer> toRemove = new ArrayList<>();
            for (int i = 0; i < this.shockwaveTickers.size(); i++) {
                int ticker = this.shockwaveTickers.get(i);

                if (ticker < 5) {
                    this.shockwaveTickers.set(i, ticker + 1);
                } else {
                    toRemove.add(i);
                }
            }
            for (int ticker : toRemove) {
                this.shockwaveTickers.remove(ticker);
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

        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientBoundSingleFountainData(this));
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

            for (int i = 0; i < fillRate && room.fillIndex < room.getPositions().size(); i++) {
                BlockPos pos = room.getPositions().get(room.fillIndex);
                BlockState current = level.getBlockState(pos);
                if (current.is(Blocks.AIR) || current.is(Blocks.CAVE_AIR) || current.is(Blocks.VOID_AIR)) {
                    level.setBlock(pos, BlockRegistry.DARKNESS.get().defaultBlockState(), 3);
                    if (level.getBlockEntity(pos) instanceof DarknessBlockEntity darkness) {
                        darkness.fountainPos = this.fountainPos;
                    }
                }
                room.fillIndex++;
            }

            room.checkActivation();
        }
    }

    public void tickFountainSealing(Level level) {
        if (this.sealingTick >= SEAL_DURATION + SEAL_FLASH_DELAY + SEAL_FLASH_DURATION + 20) {
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
            }

            darkFountainCapability.removeDarkFountain(level, fountainPos);
            lightFountainCapability.removeDarkFountain(lightLevel, destinationPos);

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

        for (DarkRoom room : rooms) {
            if (room.isDissipating()) continue;

            Set<BlockPos> posSet = new HashSet<>(room.getPositions());
            Iterator<Map.Entry<UUID, Integer>> iterator = room.getTransportTickers().entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                UUID entityId = entry.getKey();
                int ticker = entry.getValue();

                Entity entity = level.getEntity(entityId);
                if (entity == null) {
                    iterator.remove();
                    continue;
                }

                boolean inRoom = posSet.contains(entity.blockPosition()) || posSet.contains(entity.blockPosition().above());

                if (inRoom) {
                    ticker = Math.min(ticker + 1, TRANSPORT_TICKER_DURATION);
                    entry.setValue(ticker);

                    if (entity instanceof ServerPlayer serverPlayer) {
                        int finalTicker = ticker;

                        serverPlayer.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> cap.darknessOverlayTicker = finalTicker);
                    }

                    if (ticker == TRANSPORT_TICKER_DURATION) {
                        destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                            DarkFountain destinationFountain = cap.darkFountains.get(destinationPos);

                            if (destinationFountain != null) {
                                Vec3 target = getRandomTeleportTarget(destinationLevel, 8, 16);

                                if (entity instanceof ServerPlayer player) {
                                    destinationFountain.teleportedEntities.add(teleportPlayer(player, destinationLevel, target).getUUID());
                                } else {
                                    Entity teleported = teleportEntity(entity, destinationLevel, target);
                                    if (teleported != null) {
                                        destinationFountain.teleportedEntities.add(teleported.getUUID());
                                    }
                                }
                            }
                        });
                        iterator.remove();
                        continue;
                    }
                } else {
                    ticker = Math.max(ticker - 1, 0);
                    entry.setValue(ticker);

                    if (entity instanceof ServerPlayer serverPlayer) {
                        int finalTicker = ticker;

                        serverPlayer.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> cap.darknessOverlayTicker = finalTicker);
                    }

                    if (ticker == 0) {
                        iterator.remove();
                        continue;
                    }
                }

                if (entity instanceof ServerPlayer player) {
                    float progress = (float) ticker / TRANSPORT_TICKER_DURATION;
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(progress));
                }
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

    @Nullable
    private Set<BlockPos> collectOtherFountainAnchors(ServerLevel level) {
        HashSet<BlockPos> anchors = new HashSet<>();
        level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
            for (Map.Entry<BlockPos, DarkFountain> e : cap.darkFountains.entrySet()) {
                if (!e.getKey().equals(this.fountainPos)) {
                    anchors.add(e.getValue().getFountainPos());
                }
            }
        });
        return anchors.isEmpty() ? null : anchors;
    }

    private void tickRoomManagement(ServerLevel level) {
        Set<BlockPos> otherAnchors = collectOtherFountainAnchors(level);

        if (rooms.isEmpty()) {
            RoomScanner.RoomScanResult result = RoomScanner.scan(level, fountainPos, ServerConfig.maxRoomVolume, false, false, otherAnchors);
            if (result.isValid()) {
                DarkRoom newRoom = new DarkRoom(fountainPos, result.getPositions(), result.getDoorPositions());
                addEntitiesInRoomToTickers(level, newRoom);
                rooms.add(newRoom);
            }
            return;
        }

        Set<BlockPos> allPositions = new HashSet<>();
        for (DarkRoom room : rooms) {
            if (!room.isDissipating()) allPositions.addAll(room.getPositions());
        }

        for (DarkRoom room : rooms) {
            if (room.isDissipating()) continue;

            RoomScanner.RoomScanResult result = RoomScanner.scan(level, room.getSeedPos(), ServerConfig.maxRoomVolume, true, true, otherAnchors);
            if (result.isValid()) {
                room.positions = result.getPositions();
                room.doorPositions = result.getDoorPositions();
                int alreadyFilled = 0;
                for (BlockPos pos : room.positions) {
                    if (level.getBlockState(pos).getBlock() instanceof DarknessBlock) alreadyFilled++;
                }
                room.fillIndex = alreadyFilled;
            }
        }

        tickConnectivityViaDoors(level);
        tickExpansionThroughDoors(level, otherAnchors);
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

    private void tickExpansionThroughDoors(ServerLevel level, @Nullable Set<BlockPos> otherFountainAnchors) {
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
                    if (!adjState.is(Blocks.AIR) && !adjState.is(Blocks.CAVE_AIR) && !adjState.is(Blocks.VOID_AIR))
                        continue;

                    RoomScanner.RoomScanResult result = RoomScanner.scan(level, adjacent, remainingVolume, false, false, otherFountainAnchors);
                    if (result.isValid()) {
                        DarkRoom newRoom = new DarkRoom(adjacent, result.getPositions(), result.getDoorPositions());
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
        for (Entity entity : level.getEntitiesOfClass(Entity.class, roomBox)) {
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

            for (Entity entity : level.getEntitiesOfClass(Entity.class, roomBox)) {
                if (this.teleportedEntities.contains(entity.getUUID())) continue;
                if (!posSet.contains(entity.blockPosition()) && !posSet.contains(entity.blockPosition().above())) continue;

                destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                    DarkFountain destinationFountain = cap.darkFountains.get(destinationPos);

                    if (destinationFountain != null) {
                        Vec3 target = getRandomTeleportTarget(destinationLevel, 256, 512);

                        if (entity instanceof ServerPlayer player) {
                            float yaw = (float) Math.toDegrees(Math.atan2(-((destinationPos.getX() + 0.5) - target.x), (destinationPos.getZ() + 0.5) - target.z));

                            level.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);

                            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(0f));
                            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundDarknessFallPacket(destinationPos, target.x, target.y, target.z, yaw, destinationDimension));
                        } else {
                            Entity teleported = teleportEntity(entity, destinationLevel, target);
                            if (teleported != null) destinationFountain.teleportedEntities.add(teleported.getUUID());
                        }
                    }
                });
                this.teleportedEntities.add(entity.getUUID());
            }

            HashSet<UUID> stillInRoom = new HashSet<>();
            for (UUID id : this.teleportedEntities) {
                Entity entity = level.getEntity(id);
                if (entity != null && (posSet.contains(entity.blockPosition()) || posSet.contains(entity.blockPosition().above()))) {
                    stillInRoom.add(id);
                }
            }
            this.teleportedEntities = stillInRoom;
        }
    }

    private void tickDarkWorldFountainPushing(ServerLevel level) {
        AABB pushBox = new AABB(fountainPos.offset(0, 5, 0)).inflate(5).setMaxY(level.dimensionType().height());

        for (Entity entity : level.getEntitiesOfClass(Entity.class, pushBox)) {
            Vec3 entityPos = entity.position();
            Vec3 awayVec;
            double distance;

            if (entityPos.y < fountainPos.getCenter().y) {
                awayVec = entityPos.subtract(fountainPos.getCenter());
            } else {
                double dx = entityPos.x - fountainPos.getCenter().x;
                double dz = entityPos.z - fountainPos.getCenter().z;

                awayVec = new Vec3(dx, 0.0, dz);
            }

            distance = awayVec.length();
            if (distance >= 4) {
                return;
            }

            Vec3 directionVec = awayVec.scale(1.0 / distance);
            double falloff = 1.0 - distance / 4;
            Vec3 pushAwayVec = directionVec.scale(3 * falloff);

            entity.push(pushAwayVec.x, pushAwayVec.y, pushAwayVec.z);

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }

            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.penumbra_phantasm.pushed_away_by_fountain"), true);
            }
        }
    }

    private void tickLightWorldTeleport(ServerLevel level) {
        AABB teleportBox = new AABB(fountainPos.above()).inflate(1).setMaxY(level.dimensionType().height());
        HashSet<UUID> teleportBoxEntities = new HashSet<>();
        ServerLevel destinationLevel = level.getServer().getLevel(this.destinationDimension);

        if (destinationLevel == null) return;

        destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
            DarkFountain destinationFountain = cap.darkFountains.get(this.destinationPos);

            Vec3 fountainCenter = destinationPos.getCenter();
            for (Entity entity : level.getEntitiesOfClass(Entity.class, teleportBox)) {
                if (!this.teleportedEntities.contains(entity.getUUID())) {
                    if (destinationFountain != null) {
                        if (entity instanceof ServerPlayer player) {
                            destinationFountain.teleportedEntities.add(teleportPlayer(player, destinationLevel, fountainCenter).getUUID());
                        } else {
                            Entity teleported = teleportEntity(entity, destinationLevel, fountainCenter);
                            if (teleported != null) destinationFountain.teleportedEntities.add(teleported.getUUID());
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
    }

    private void tickSoundPackets(Level level) {
        if (DarkWorldUtil.isDarkWorld(level)) {
            PacketHandlerRegistry.INSTANCE.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                    new ClientBoundSoundPackets.FountainWind(this.fountainPos, false));
        } else {
            PacketHandlerRegistry.INSTANCE.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.getFountainPos())),
                    new ClientBoundSoundPackets.FountainDarkness(this.fountainPos, false));
        }
    }

    private Vec3 getRandomTeleportTarget(ServerLevel destinationLevel, int teleportMinRadius, int teleportMaxRadius) {
        double angle = destinationLevel.getRandom().nextDouble() * 2 * Math.PI;
        double distance = teleportMinRadius + destinationLevel.getRandom().nextDouble() * (teleportMaxRadius - teleportMinRadius);
        double x = destinationPos.getX() + 0.5 + Math.cos(angle) * distance;
        double z = destinationPos.getZ() + 0.5 + Math.sin(angle) * distance;

        ChunkPos chunk = new ChunkPos(BlockPos.containing(x, destinationPos.getY(), z));

        destinationLevel.setChunkForced(chunk.x, chunk.z, true);
        BlockPos heightmapPos = destinationLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, destinationLevel.getMaxBuildHeight(), z));
        destinationLevel.setChunkForced(chunk.x, chunk.z, false);

        return new Vec3(x, heightmapPos.getY() + 1, z);
    }

    public Entity teleportPlayer(ServerPlayer player, ServerLevel destinationLevel, Vec3 targetPos) {
        double dx = (destinationPos.getX() + 0.5) - targetPos.x;
        double dz = (destinationPos.getZ() + 0.5) - targetPos.z;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        player.teleportTo(destinationLevel, targetPos.x, targetPos.y, targetPos.z, yaw, 0f);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(0f));
        return player;
    }

    public Entity teleportEntity(Entity entity, ServerLevel destinationLevel, Vec3 targetPos) {
        return entity.changeDimension(destinationLevel, new DarkFountainTeleporter(targetPos, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()));
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

        return tag;
    }

    public static DarkFountain load(CompoundTag tag) {
        BlockPos fountainPos = NbtUtils.readBlockPos(tag.getCompound(FOUNTAIN_POS));
        ResourceKey<Level> fountainDimension = stringToDimension(tag.getString(FOUNTAIN_DIMENSION));
        BlockPos destinationPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_POS));
        ResourceKey<Level> destinationDimension = stringToDimension(tag.getString(DESTINATION_DIMENSION));
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
    }

    public BlockPos getFountainPos() { return fountainPos; }
    public ResourceKey<Level> getFountainDimension() { return fountainDimension; }
    public BlockPos getDestinationPos() { return destinationPos; }
    public ResourceKey<Level> getDestinationDimension() { return destinationDimension; }
    public int getOpeningTick() { return openingTick; }
    public int getFrameTick() { return frameTick; }
    public int getFrame() { return frame; }
    public int getFrameOptimized() { return frameOptimized; }

    public static ResourceKey<Level> stringToDimension(String dimensionString) {
        String[] split = dimensionString.split(":");
        if (split.length > 1)
            return ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(split[0], split[1]));
        return null;
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