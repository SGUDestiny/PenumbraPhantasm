package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.client.network.ClientBoundSingleGreatDoorPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundTransportTickerPacket;
import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GreatDoor {
    public static final String GREAT_DOOR_POS = "greatDoorPos";
    public static final String DIRECTION = "direction";
    public static final String IS_OPEN = "isOpen";
    public static final String VOLUME_POSITIONS = "volumePositions";
    public static final String LIGHT_DOOR_POS = "lightDoorPos";
    public static final String LIGHT_DOOR_DIMENSION = "lightDoorDimension";
    public static final String DESTINATION_GREAT_DOOR_POS = "destinationGreatDoorPos";
    public static final String DESTINATION_GREAT_DOOR_DIMENSION = "destinationGreatDoorDimension";
    public static final String IS_DESTINATION_DARK_WORLD = "isDestinationDarkWorld";
    public static final String LIGHT_DOOR_EXIT_DIRECTION = "lightDoorExitDirection";

    public BlockPos greatDoorPos;
    public Direction direction;
    public boolean isOpen;
    public List<BlockPos> volumePositions;
    public BlockPos lightDoorPos;
    public ResourceKey<Level> lightDoorDimension;
    public Direction lightDoorExitDirection;
    public boolean isDestinationDarkWorld;
    @Nullable
    public BlockPos destinationGreatDoorPos;
    @Nullable
    public ResourceKey<Level> destinationGreatDoorDimension;

    public GreatDoor(BlockPos greatDoorPos, Direction direction, boolean isOpen, List<BlockPos> volumePositions,
                     BlockPos lightDoorPos, ResourceKey<Level> lightDoorDimension, Direction lightDoorExitDirection,
                     boolean isDestinationDarkWorld, @Nullable BlockPos destinationGreatDoorPos,
                     @Nullable ResourceKey<Level> destinationGreatDoorDimension) {
        this.greatDoorPos = greatDoorPos;
        this.direction = direction;
        this.isOpen = isOpen;
        this.volumePositions = volumePositions;
        this.lightDoorPos = lightDoorPos;
        this.lightDoorDimension = lightDoorDimension;
        this.lightDoorExitDirection = lightDoorExitDirection;
        this.isDestinationDarkWorld = isDestinationDarkWorld;
        this.destinationGreatDoorPos = destinationGreatDoorPos;
        this.destinationGreatDoorDimension = destinationGreatDoorDimension;
    }

    public static float yawFacingAwayFromDoor(Direction doorFacing) {
        return doorFacing.toYRot();
    }

    public static Vec3 spawnCenterInFrontOfGreatDoor(BlockPos greatDoorPos, Direction doorFacing) {
        return Vec3.atBottomCenterOf(greatDoorPos.relative(doorFacing, 1));
    }

    public void refreshOpenFromLinkedLightDoor(ServerLevel darkLevel) {
        if (lightDoorPos == null || lightDoorDimension == null) {
            isOpen = false;
            return;
        }
        ServerLevel lightLevel = darkLevel.getServer().getLevel(lightDoorDimension);
        if (lightLevel == null) {
            isOpen = false;
            return;
        }
        BlockPos lower = lightDoorPos;
        BlockState doorState = lightLevel.getBlockState(lower);
        if (doorState.getBlock() instanceof DoorBlock && doorState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            lower = lightDoorPos.below();
            doorState = lightLevel.getBlockState(lower);
        }
        if (!(doorState.getBlock() instanceof DoorBlock)) {
            isOpen = false;
            return;
        }
        Direction fromDoorToRoom = lightDoorExitDirection.getOpposite();
        isOpen = DarknessBlock.isDoorVisuallyOpenFromSide(lightLevel, lower, doorState, fromDoorToRoom);
    }

    public static boolean toggleLinkedLightDoor(ServerLevel darkLevel, GreatDoor door, @Nullable Entity causedBy) {
        if (door.lightDoorPos == null || door.lightDoorDimension == null) {
            return false;
        }
        ServerLevel lightLevel = darkLevel.getServer().getLevel(door.lightDoorDimension);
        if (lightLevel == null) {
            return false;
        }
        BlockPos interactPos = door.lightDoorPos;
        BlockState doorState = lightLevel.getBlockState(interactPos);
        if (doorState.getBlock() instanceof DoorBlock && doorState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            interactPos = door.lightDoorPos.below();
            doorState = lightLevel.getBlockState(interactPos);
        }
        if (!(doorState.getBlock() instanceof DoorBlock doorBlock)) {
            return false;
        }
        boolean currentOpen = DarknessBlock.getDoorOpenState(lightLevel, interactPos, doorState);
        doorBlock.setOpen(causedBy, lightLevel, doorState, interactPos, !currentOpen);
        door.refreshOpenFromLinkedLightDoor(darkLevel);
        return true;
    }

    public void broadcastSync(ServerLevel darkLevel) {
        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> darkLevel.getChunkAt(greatDoorPos)), new ClientBoundSingleGreatDoorPacket(this));
    }

    public void tick(Level level) {
        if (level.isClientSide() || !DarkWorldUtil.isDarkWorld(level)) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            boolean openBefore = isOpen;
            refreshOpenFromLinkedLightDoor(serverLevel);
            if (openBefore != isOpen) {
                serverLevel.playSound(null, greatDoorPos.getX() + 0.5, greatDoorPos.getY() + 0.5, greatDoorPos.getZ() + 0.5,
                        SoundRegistry.GREAT_DOOR.get(), SoundSource.BLOCKS, 1f, 1f);
            }

            ServerLevel teleportDestination = null;
            if (isDestinationDarkWorld) {
                if (destinationGreatDoorDimension != null) {
                    teleportDestination = serverLevel.getServer().getLevel(destinationGreatDoorDimension);
                }
            } else {
                teleportDestination = serverLevel.getServer().getLevel(lightDoorDimension);
            }

            if (isOpen && teleportDestination != null) {
                tickVolumeTeleportation(serverLevel, teleportDestination);
            }
        }

        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(greatDoorPos)), new ClientBoundSingleGreatDoorPacket(this));
    }

    public void tickVolumeTeleportation(ServerLevel greatDoorLevel, ServerLevel destinationLevel) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : volumePositions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        AABB volumeBox = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

        if (isDestinationDarkWorld) {
            GreatDoor peer = resolvePeerGreatDoor(greatDoorLevel, destinationLevel);
            if (peer == null) {
                DarkWorldUtil.ensurePeerGreatDoor(this, greatDoorLevel, destinationLevel);
                peer = resolvePeerGreatDoor(greatDoorLevel, destinationLevel);
            }
            if (peer == null) {
                return;
            }
            float yaw = yawFacingAwayFromDoor(peer.direction);
            Vec3 destVec = spawnCenterInFrontOfGreatDoor(peer.greatDoorPos, peer.direction);
            BlockPos destDarkAnchor = findDarkFountainAnchor(destinationLevel);
            for (Entity entity : greatDoorLevel.getEntitiesOfClass(Player.class, volumeBox)) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.isSpectator()) {
                        continue;
                    }
                    greatDoorLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
                    serverPlayer.teleportTo(destinationLevel, destVec.x, destVec.y, destVec.z, yaw, 0f);
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundTransportTickerPacket(0f));
                    if (destDarkAnchor != null) {
                        addToDarkFountainTeleported(destinationLevel, destDarkAnchor, serverPlayer.getUUID());
                    }
                } else {
                    ModUtil.teleportEntity(entity, destinationLevel, destVec);
                }
            }
        } else {
            Vec3 destVec = lightDoorPos.getCenter();
            float yaw = lightDoorExitDirection.toYRot();
            BlockPos darkAnchor = findDarkFountainAnchor(greatDoorLevel);
            for (Entity entity : greatDoorLevel.getEntitiesOfClass(Player.class, volumeBox)) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.isSpectator()) {
                        continue;
                    }
                    greatDoorLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
                    serverPlayer.teleportTo(destinationLevel, destVec.x, destVec.y, destVec.z, yaw, 0f);
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                    PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundTransportTickerPacket(0f));
                    if (darkAnchor != null) {
                        addPlayerToPairedLightFountainTeleported(destinationLevel, greatDoorLevel.dimension(), darkAnchor, serverPlayer.getUUID());
                    }
                } else {
                    ModUtil.teleportEntity(entity, destinationLevel, destVec);
                }
            }
        }
    }

    @Nullable
    private GreatDoor resolvePeerGreatDoor(ServerLevel sourceWorld, ServerLevel destWorld) {
        if (destinationGreatDoorDimension == null || !destinationGreatDoorDimension.equals(destWorld.dimension())) {
            return null;
        }
        if (destinationGreatDoorPos == null) {
            return null;
        }
        return destWorld.getCapability(CapabilityRegistry.GREAT_DOOR)
                .resolve()
                .map(cap -> cap.greatDoors.get(destinationGreatDoorPos))
                .orElse(null);
    }

    @Nullable
    private static BlockPos findDarkFountainAnchor(ServerLevel darkLevel) {
        return darkLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN)
                .resolve()
                .map(cap -> cap.darkFountains.isEmpty() ? null : cap.darkFountains.keySet().iterator().next())
                .orElse(null);
    }

    private static void addToDarkFountainTeleported(ServerLevel darkLevel, BlockPos fountainAnchor, UUID id) {
        darkLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
            DarkFountain f = cap.darkFountains.get(fountainAnchor);
            if (f != null) {
                f.teleportedEntities.add(id);
            }
        });
    }

    private static void addPlayerToPairedLightFountainTeleported(ServerLevel lightLevel, ResourceKey<Level> darkDim, BlockPos darkFountainAnchor, UUID playerId) {
        lightLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
            for (DarkFountain lf : cap.darkFountains.values()) {
                if (darkDim.equals(lf.destinationDimension) && darkFountainAnchor.equals(lf.destinationPos)) {
                    lf.teleportedEntities.add(playerId);
                    return;
                }
            }
        });
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.put(GREAT_DOOR_POS, NbtUtils.writeBlockPos(greatDoorPos));
        tag.putString(DIRECTION, direction.getName());
        tag.putBoolean(IS_OPEN, isOpen);
        ListTag volumePositionsTag = new ListTag();
        for (BlockPos pos : volumePositions) {
            volumePositionsTag.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put(VOLUME_POSITIONS, volumePositionsTag);
        tag.put(LIGHT_DOOR_POS, NbtUtils.writeBlockPos(lightDoorPos));
        tag.putString(LIGHT_DOOR_DIMENSION, lightDoorDimension.location().toString());
        tag.putString(LIGHT_DOOR_EXIT_DIRECTION, lightDoorExitDirection.getName());
        tag.putBoolean(IS_DESTINATION_DARK_WORLD, isDestinationDarkWorld);
        if (isDestinationDarkWorld && destinationGreatDoorPos != null && destinationGreatDoorDimension != null) {
            tag.put(DESTINATION_GREAT_DOOR_POS, NbtUtils.writeBlockPos(destinationGreatDoorPos));
            tag.putString(DESTINATION_GREAT_DOOR_DIMENSION, destinationGreatDoorDimension.location().toString());
        }

        return tag;
    }

    public static GreatDoor load(CompoundTag tag) {
        BlockPos greatDoorPos = NbtUtils.readBlockPos(tag.getCompound(GREAT_DOOR_POS));
        Direction direction = Direction.byName(tag.getString(DIRECTION));
        boolean isOpen = tag.getBoolean(IS_OPEN);
        List<BlockPos> volumePositions = new ArrayList<>();
        ListTag volumePositionsTag = tag.getList(VOLUME_POSITIONS, Tag.TAG_COMPOUND);
        for (Tag t : volumePositionsTag) {
            volumePositions.add(NbtUtils.readBlockPos((CompoundTag) t));
        }
        BlockPos lightDoorPos = NbtUtils.readBlockPos(tag.getCompound(LIGHT_DOOR_POS));
        ResourceKey<Level> lightDoorDimension = ModUtil.stringToDimension(tag.getString(LIGHT_DOOR_DIMENSION));
        Direction lightDoorExitDirection = tag.contains(LIGHT_DOOR_EXIT_DIRECTION, Tag.TAG_STRING)
                ? Direction.byName(tag.getString(LIGHT_DOOR_EXIT_DIRECTION))
                : null;
        if (lightDoorExitDirection == null) {
            lightDoorExitDirection = Direction.NORTH;
        }

        boolean isDestinationDarkWorld;
        BlockPos destinationGreatDoorPos = null;
        ResourceKey<Level> destinationGreatDoorDimension = null;

        if (tag.contains(IS_DESTINATION_DARK_WORLD)) {
            isDestinationDarkWorld = tag.getBoolean(IS_DESTINATION_DARK_WORLD);
            if (isDestinationDarkWorld && tag.contains(DESTINATION_GREAT_DOOR_POS, Tag.TAG_COMPOUND)) {
                destinationGreatDoorPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_GREAT_DOOR_POS));
                destinationGreatDoorDimension = ModUtil.stringToDimension(tag.getString(DESTINATION_GREAT_DOOR_DIMENSION));
            }
        } else {
            if (tag.contains(DESTINATION_GREAT_DOOR_POS, Tag.TAG_COMPOUND)) {
                destinationGreatDoorPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_GREAT_DOOR_POS));
            }
            destinationGreatDoorDimension = ModUtil.stringToDimension(tag.getString(DESTINATION_GREAT_DOOR_DIMENSION));
            isDestinationDarkWorld = destinationGreatDoorDimension != null && DarkWorldUtil.isDarkWorldKey(destinationGreatDoorDimension);
            if (!isDestinationDarkWorld) {
                destinationGreatDoorPos = null;
                destinationGreatDoorDimension = null;
            }
        }

        return new GreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos, lightDoorDimension,
                lightDoorExitDirection, isDestinationDarkWorld, destinationGreatDoorPos, destinationGreatDoorDimension);
    }

    public void sync(GreatDoor greatDoor) {
        this.greatDoorPos = greatDoor.greatDoorPos;
        this.direction = greatDoor.direction;
        this.isOpen = greatDoor.isOpen;
        this.volumePositions = greatDoor.volumePositions;
        this.lightDoorPos = greatDoor.lightDoorPos;
        this.lightDoorDimension = greatDoor.lightDoorDimension;
        this.lightDoorExitDirection = greatDoor.lightDoorExitDirection;
        this.isDestinationDarkWorld = greatDoor.isDestinationDarkWorld;
        this.destinationGreatDoorPos = greatDoor.destinationGreatDoorPos;
        this.destinationGreatDoorDimension = greatDoor.destinationGreatDoorDimension;
    }
}
