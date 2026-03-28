package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.client.sound.SoundWrapper;
import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.network.*;
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
    public static final String ANIMATION_TIMER = "animationTimer";
    public static final String FRAME_TIMER = "frameTimer";
    public static final String FRAME = "frame";
    public static final String FRAME_OPTIMIZED = "frameOptimized";
    public static final String TELEPORTED_ENTITIES = "teleportedEntities";
    public static final String FADE_IN_TICKERS = "fadeInTickers";
    public static final String ROOMS = "rooms";

    public static final int FILL_START_TICK = 126;
    public static final int TRANSPORT_TICKER_DURATION = 100;
    public static final int FILL_DURATION_TICKS = TRANSPORT_TICKER_DURATION + 20;

    public BlockPos fountainPos;
    public ResourceKey<Level> fountainDimension;
    public BlockPos destinationPos;
    public ResourceKey<Level> destinationDimension;
    public int animationTimer;
    public int frameTimer;
    public int frame;
    public int frameOptimized;
    public HashSet<UUID> teleportedEntities;
    public List<DarkRoom> rooms = new ArrayList<>();
    public int rescanTimer = 0;

    @Nullable
    public SoundWrapper windSound = null;
    @Nullable
    public SoundWrapper darknessSound = null;

    public DarkFountain(BlockPos fountainPos, ResourceKey<Level> fountainDimension, BlockPos destinationPos, ResourceKey<Level> destinationDimension, int animationTimer, int frameTimer, int frame, int frameOptimized, HashSet<UUID> teleportedEntities) {
        this.fountainPos = fountainPos;
        this.fountainDimension = fountainDimension;
        this.destinationPos = destinationPos;
        this.destinationDimension = destinationDimension;
        this.animationTimer = animationTimer;
        this.frameTimer = frameTimer;
        this.frame = frame;
        this.frameOptimized = frameOptimized;
        this.teleportedEntities = teleportedEntities;
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
                    if (rescanTimer >= Config.rescanInterval && (this.animationTimer == -1)) {
                        rescanTimer = 0;
                        tickRoomManagement(serverLevel);
                    }
                }

                if (this.animationTimer > 125 || this.animationTimer == -1) {
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
            if (this.frameTimer % 6 == 0) {
                if (this.frameOptimized >= 5) {
                    this.frameOptimized = 0;
                } else {
                    this.frameOptimized++;
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

            if (this.animationTimer > 125 || this.animationTimer == -1) {
                tickSoundPackets(level);
            }
        }

        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientBoundSingleFountainData(this));
    }

    private void tickRoomDarknessFill(ServerLevel level) {
        for (DarkRoom room : rooms) {
            if (!room.isFilling()) continue;

            boolean isInitialRoom = room.getSeedPos().equals(fountainPos);
            int fillRate;

            if (isInitialRoom && this.animationTimer >= 0) {
                if (this.animationTimer < FILL_START_TICK) continue;
            }
            fillRate = Math.max(1, room.getPositions().size() / FILL_DURATION_TICKS);

            for (int i = 0; i < fillRate && room.fillIndex < room.getPositions().size(); i++) {
                BlockPos pos = room.getPositions().get(room.fillIndex);
                BlockState current = level.getBlockState(pos);
                if (current.is(Blocks.AIR) || current.is(Blocks.CAVE_AIR) || current.is(Blocks.VOID_AIR)) {
                    level.setBlock(pos, BlockRegistry.DARKNESS.get().defaultBlockState(), 3);
                }
                room.fillIndex++;
            }

            room.checkActivation();
        }
    }

    private void tickDarkWorldTransportTickers(ServerLevel level) {
        if (this.animationTimer >= 0 && this.animationTimer < FILL_START_TICK) return;

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
                                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTransportTickerPacket(1f));
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

            for (int i = 0; i < Config.dissipationRate && !room.dissipationQueue.isEmpty(); i++) {
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

    private void tickRoomManagement(ServerLevel level) {
        if (rooms.isEmpty()) {
            RoomScanner.RoomScanResult result = RoomScanner.scan(level, fountainPos, Config.maxRoomVolume, false);
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

            RoomScanner.RoomScanResult result = RoomScanner.scan(level, room.getSeedPos(), Config.maxRoomVolume, true, true);
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
        tickExpansionThroughDoors(level);
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

    private void tickExpansionThroughDoors(ServerLevel level) {
        //Subtract total used volume from max volume, if zero or below, don't expand
        int remainingVolume = Config.maxRoomVolume - DarkRoom.getTotalDarknessCount(rooms);
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

                    BlockState adjState = level.getBlockState(adjacent);
                    if (!adjState.is(Blocks.AIR) && !adjState.is(Blocks.CAVE_AIR) && !adjState.is(Blocks.VOID_AIR))
                        continue;

                    RoomScanner.RoomScanResult result = RoomScanner.scan(level, adjacent, remainingVolume, false);
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
        tag.putInt(ANIMATION_TIMER, animationTimer);
        tag.putInt(FRAME_TIMER, frameTimer);
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
        int frameOptimized = tag.getInt(FRAME_OPTIMIZED);
        HashSet<UUID> teleportedEntities = new HashSet<>();
        ListTag teleportedEntitiesTag = tag.getList(TELEPORTED_ENTITIES, Tag.TAG_STRING);
        for (Tag tg : teleportedEntitiesTag) {
            teleportedEntities.add(UUID.fromString(tg.getAsString()));
        }

        DarkFountain fountain = new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, animationTimer, frameTimer, frame, frameOptimized, teleportedEntities);

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
        this.animationTimer = fountain.animationTimer;
        this.frameTimer = fountain.frameTimer;
        this.frameOptimized = fountain.frameOptimized;

        this.teleportedEntities = fountain.teleportedEntities;
    }

    public BlockPos getFountainPos() { return fountainPos; }
    public ResourceKey<Level> getFountainDimension() { return fountainDimension; }
    public BlockPos getDestinationPos() { return destinationPos; }
    public ResourceKey<Level> getDestinationDimension() { return destinationDimension; }
    public int getAnimationTimer() { return animationTimer; }
    public int getFrameTimer() { return frameTimer; }
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
