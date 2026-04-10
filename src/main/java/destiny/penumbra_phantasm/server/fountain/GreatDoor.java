package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.client.network.ClientBoundSingleGreatDoorPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundTransportTickerPacket;
import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.block.GreatDoorShapeBlock;
import destiny.penumbra_phantasm.server.block.entity.GreatDoorShapeBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
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

import static destiny.penumbra_phantasm.server.block.GreatDoorShapeBlock.IS_DOOR_OPEN;

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
    @Nullable
    public BlockPos lightDoorPos;
    @Nullable
    public ResourceKey<Level> lightDoorDimension;
    @Nullable
    public Direction lightDoorExitDirection;
    public boolean isDestinationDarkWorld;
    @Nullable
    public BlockPos destinationGreatDoorPos;
    @Nullable
    public ResourceKey<Level> destinationGreatDoorDimension;
    public boolean lightDoorForceClosedWithoutFountain;

    public GreatDoor(BlockPos greatDoorPos, Direction direction, boolean isOpen, List<BlockPos> volumePositions, @Nullable BlockPos lightDoorPos,
                     @Nullable ResourceKey<Level> lightDoorDimension, @Nullable Direction lightDoorExitDirection, boolean isDestinationDarkWorld,
                     @Nullable BlockPos destinationGreatDoorPos, @Nullable ResourceKey<Level> destinationGreatDoorDimension) {
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

    public void tick(Level level) {
        if (level.isClientSide() || !DarkWorldUtil.isDarkWorld(level)) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            boolean localFountain = DarkWorldUtil.levelHasDarkFountain(serverLevel);

            if (localFountain) {
                lightDoorForceClosedWithoutFountain = false;
            } else if (!isDestinationDarkWorld
                    && lightDoorPos != null && lightDoorDimension != null && lightDoorExitDirection != null) {
                if (!lightDoorForceClosedWithoutFountain) {
                    forceCloseLinkedLightDoor(serverLevel, this);
                    lightDoorForceClosedWithoutFountain = true;
                }
            }

            maintainDoorShape(serverLevel);

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
                if (lightDoorDimension != null) {
                    teleportDestination = serverLevel.getServer().getLevel(lightDoorDimension);
                }
            }

            boolean canTeleport = localFountain;
            if (canTeleport && isDestinationDarkWorld && teleportDestination != null) {
                canTeleport = DarkWorldUtil.levelHasDarkFountain(teleportDestination);
            }
            if (isOpen && teleportDestination != null && canTeleport) {
                tickVolumeTeleportation(serverLevel, teleportDestination);
            }
        }

        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(greatDoorPos)), new ClientBoundSingleGreatDoorPacket(this));
    }

    public void maintainDoorShape(ServerLevel darkLevel) {
        for (BlockPos pos : volumePositions) {
            if (!(darkLevel.getBlockState(pos).getBlock() instanceof GreatDoorShapeBlock)) {
                if (isOpen) {
                    darkLevel.setBlock(pos, BlockRegistry.GREAT_DOOR_SHAPE.get().defaultBlockState().setValue(IS_DOOR_OPEN, true), 3);
                } else {
                    darkLevel.setBlock(pos, BlockRegistry.GREAT_DOOR_SHAPE.get().defaultBlockState().setValue(IS_DOOR_OPEN, false), 3);
                }
            } else {
                if (darkLevel.getBlockState(pos).getValue(IS_DOOR_OPEN) != isOpen) {
                    darkLevel.setBlock(pos, BlockRegistry.GREAT_DOOR_SHAPE.get().defaultBlockState().setValue(IS_DOOR_OPEN, isOpen), 3);
                }

                if (darkLevel.getBlockEntity(pos) instanceof GreatDoorShapeBlockEntity shapeBlockEntity) {
                    if (shapeBlockEntity.greatDoorPos != greatDoorPos) {
                        shapeBlockEntity.greatDoorPos = greatDoorPos;
                    }
                }
            }
        }
    }

    public void refreshOpenFromLinkedLightDoor(ServerLevel darkLevel) {
        if (!DarkWorldUtil.levelHasDarkFountain(darkLevel) && !isDestinationDarkWorld && lightDoorPos != null && lightDoorDimension != null
                && lightDoorExitDirection != null) {
            isOpen = false;

            return;
        }

        if (lightDoorPos == null || lightDoorDimension == null || lightDoorExitDirection == null) {
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
        if (door.lightDoorPos == null || door.lightDoorDimension == null || door.lightDoorExitDirection == null) {
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

        Direction fromDoorToRoom = door.lightDoorExitDirection.getOpposite();
        boolean currentVisual = DarknessBlock.isDoorVisuallyOpenFromSide(lightLevel, interactPos, doorState, fromDoorToRoom);
        boolean targetVisual = !currentVisual;
        Direction facing = doorState.getValue(DoorBlock.FACING);
        boolean parallel = facing.getAxis() == fromDoorToRoom.getAxis();
        boolean targetPhysical = parallel ? targetVisual : !targetVisual;
        doorBlock.setOpen(causedBy, lightLevel, doorState, interactPos, targetPhysical);

        door.refreshOpenFromLinkedLightDoor(darkLevel);

        return true;
    }

    public static void forceCloseLinkedLightDoor(ServerLevel darkLevel, GreatDoor door) {
        if (door.isDestinationDarkWorld || door.lightDoorPos == null || door.lightDoorDimension == null) {
            return;
        }

        ServerLevel lightLevel = darkLevel.getServer().getLevel(door.lightDoorDimension);

        if (lightLevel == null) {
            return;
        }

        BlockPos interactPos = door.lightDoorPos;
        BlockState doorState = lightLevel.getBlockState(interactPos);

        if (doorState.getBlock() instanceof DoorBlock && doorState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            interactPos = door.lightDoorPos.below();
            doorState = lightLevel.getBlockState(interactPos);
        }

        if (!(doorState.getBlock() instanceof DoorBlock doorBlock)) {
            return;
        }

        boolean openBefore = doorState.getValue(DoorBlock.OPEN);

        if (!openBefore) {
            door.refreshOpenFromLinkedLightDoor(darkLevel);

            return;
        }

        doorBlock.setOpen(null, lightLevel, doorState, interactPos, false);

        door.refreshOpenFromLinkedLightDoor(darkLevel);
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
            GreatDoor peer = resolvePeerGreatDoor(destinationLevel);
            if (peer == null) {
                DarkWorldUtil.ensurePeerGreatDoor(this, greatDoorLevel, destinationLevel);
                peer = resolvePeerGreatDoor(destinationLevel);
            }

            if (peer == null) {
                return;
            }

            float yaw = peer.direction.toYRot();
            Vec3 destVec = spawnCenterInFrontOfGreatDoor(peer.greatDoorPos, peer.direction);

            for (ServerPlayer serverPlayer : greatDoorLevel.getEntitiesOfClass(ServerPlayer.class, volumeBox)) {
                if (serverPlayer.isSpectator()) {
                    continue;
                }

                greatDoorLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);

                serverPlayer.teleportTo(destinationLevel, destVec.x, destVec.y, destVec.z, yaw, 0f);
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));

                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundTransportTickerPacket(0f));
            }
        } else {
            if (lightDoorPos == null || lightDoorExitDirection == null) {
                return;
            }

            Vec3 destVec = lightDoorPos.getCenter();
            float yaw = lightDoorExitDirection.toYRot();
            BlockPos darkAnchor = DarkWorldUtil.findDarkFountainAnchor(greatDoorLevel);

            for (ServerPlayer serverPlayer : greatDoorLevel.getEntitiesOfClass(ServerPlayer.class, volumeBox)) {
                if (serverPlayer.isSpectator()) {
                    continue;
                }

                greatDoorLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);

                serverPlayer.teleportTo(destinationLevel, destVec.x, destVec.y, destVec.z, yaw, 0f);
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));

                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundTransportTickerPacket(0f));

                if (darkAnchor != null) {
                    addPlayerToLightFountain(destinationLevel, greatDoorLevel.dimension(), darkAnchor, serverPlayer.getUUID());
                }
            }
        }
    }

    @Nullable
    private GreatDoor resolvePeerGreatDoor(ServerLevel destWorld) {
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

    public boolean isUnlinkedForAutoBinding() {
        return !isDestinationDarkWorld && lightDoorPos == null && lightDoorDimension == null && destinationGreatDoorPos == null
                && destinationGreatDoorDimension == null;
    }

    private static void addPlayerToLightFountain(ServerLevel lightLevel, ResourceKey<Level> darkDim, BlockPos darkFountainAnchor, UUID playerId) {
        lightLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
            for (DarkFountain lf : cap.darkFountains.values()) {
                if (darkDim.equals(lf.destinationDimension) && darkFountainAnchor.equals(lf.destinationPos)) {
                    lf.teleportedEntities.add(playerId);
                    return;
                }
            }
        });
    }

    public static Vec3 spawnCenterInFrontOfGreatDoor(BlockPos greatDoorPos, Direction doorFacing) {
        return Vec3.atBottomCenterOf(greatDoorPos.relative(doorFacing, 1))
                .add(Vec3.atLowerCornerOf(BlockPos.ZERO.relative(doorFacing.getClockWise(), 1)).scale(2.5));
    }

    public void broadcastSync(ServerLevel darkLevel) {
        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> darkLevel.getChunkAt(greatDoorPos)), new ClientBoundSingleGreatDoorPacket(this));
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
        boolean hasLightLink = lightDoorPos != null && lightDoorDimension != null && lightDoorExitDirection != null;
        tag.putBoolean("has_light_link", hasLightLink);
        if (hasLightLink) {
            tag.put(LIGHT_DOOR_POS, NbtUtils.writeBlockPos(lightDoorPos));
            tag.putString(LIGHT_DOOR_DIMENSION, lightDoorDimension.location().toString());
            tag.putString(LIGHT_DOOR_EXIT_DIRECTION, lightDoorExitDirection.getName());
        }
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

        BlockPos lightDoorPos = null;
        ResourceKey<Level> lightDoorDimension = null;
        Direction lightDoorExitDirection = null;

        if (tag.contains("has_light_link", Tag.TAG_BYTE)) {
            if (tag.getBoolean("has_light_link") && tag.contains(LIGHT_DOOR_POS, Tag.TAG_COMPOUND)) {
                lightDoorPos = NbtUtils.readBlockPos(tag.getCompound(LIGHT_DOOR_POS));
                lightDoorDimension = ModUtil.stringToDimension(tag.getString(LIGHT_DOOR_DIMENSION));
                lightDoorExitDirection = tag.contains(LIGHT_DOOR_EXIT_DIRECTION, Tag.TAG_STRING) ? Direction.byName(tag.getString(LIGHT_DOOR_EXIT_DIRECTION)) : Direction.NORTH;

                if (lightDoorExitDirection == null) {
                    lightDoorExitDirection = Direction.NORTH;
                }
            }
        } else if (tag.contains(LIGHT_DOOR_POS, Tag.TAG_COMPOUND)) {
            lightDoorPos = NbtUtils.readBlockPos(tag.getCompound(LIGHT_DOOR_POS));
            lightDoorDimension = ModUtil.stringToDimension(tag.getString(LIGHT_DOOR_DIMENSION));
            lightDoorExitDirection = tag.contains(LIGHT_DOOR_EXIT_DIRECTION, Tag.TAG_STRING) ? Direction.byName(tag.getString(LIGHT_DOOR_EXIT_DIRECTION)) : null;

            if (lightDoorExitDirection == null) {
                lightDoorExitDirection = Direction.NORTH;
            }
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
