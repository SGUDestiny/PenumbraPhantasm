package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.client.network.ClientBoundSingleGreatDoorPacket;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GreatDoor {
    public static final String GREAT_DOOR_POS = "greatDoorPos";
    public static final String DIRECTION = "direction";
    public static final String IS_OPEN = "isOpen";
    public static final String VOLUME_POSITIONS = "volumePositions";
    public static final String LIGHT_DOOR_POS = "lightDoorPos";
    public static final String LIGHT_DOOR_DIMENSION = "lightDoorDimension";
    public static final String DESTINATION_GREAT_DOOR_POS = "destinationGreatDoorPos";
    public static final String DESTINATION_GREAT_DOOR_DIMENSION = "destinationGreatDoorDimension";

    public BlockPos greatDoorPos;
    public Direction direction;
    public boolean isOpen;
    public List<BlockPos> volumePositions;
    public BlockPos lightDoorPos;
    public ResourceKey<Level> lightDoorDimension;
    public BlockPos destinationGreatDoorPos;
    public ResourceKey<Level> destinationGreatDoorDimension;

    public GreatDoor(BlockPos greatDoorPos, Direction direction, boolean isOpen, List<BlockPos> volumePositions, BlockPos lightDoorPos, ResourceKey<Level> lightDoorDimension, BlockPos destinationGreatDoorPos, ResourceKey<Level> destinationGreatDoorDimension) {
        this.greatDoorPos = greatDoorPos;
        this.direction = direction;
        this.isOpen = isOpen;
        this.volumePositions = volumePositions;
        this.lightDoorPos = lightDoorPos;
        this.lightDoorDimension = lightDoorDimension;
        this.destinationGreatDoorPos = destinationGreatDoorPos;
        this.destinationGreatDoorDimension = destinationGreatDoorDimension;
    }

    public void tick(Level level) {
        if (level.isClientSide() || !DarkWorldUtil.isDarkWorld(level)) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            ServerLevel destinationLevel;
            if (destinationGreatDoorDimension != null) {
                destinationLevel = serverLevel.getServer().getLevel(destinationGreatDoorDimension);
            } else {
                destinationLevel = serverLevel.getServer().getLevel(lightDoorDimension);
            }

            if (destinationLevel == null) {
                isOpen = false;
                return;
            }

            if (DarkWorldUtil.isDarkWorld(destinationLevel)) {
                if (!isDestinationValidDarkWorld(serverLevel)) {
                    isOpen = false;
                } else {

                }
            } else {

            }

            if (isOpen) {
                tickVolumeTeleportation(serverLevel, destinationLevel);
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

        if (DarkWorldUtil.isDarkWorld(destinationLevel)) {
            Vec3 destinationVec = lightDoorPos.relative(direction).getCenter();

            for (Entity player : greatDoorLevel.getEntitiesOfClass(Player.class, volumeBox)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    //float yaw = (float) Math.toDegrees(Math.atan2(-((destinationVec.x + 0.5) - destinationVec.x), (destinationVec.y + 0.5) - destinationVec.z));

                    greatDoorLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);

                    serverPlayer.teleportTo(destinationLevel, destinationVec.x, destinationVec.y, destinationVec.z, 0, 0f);
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
                } else {
                    ModUtil.teleportEntity(player, destinationLevel, destinationVec);
                }
            }
        } else {
            Vec3 destinationVec = lightDoorPos.getCenter();

            for (Entity player : greatDoorLevel.getEntitiesOfClass(Player.class, volumeBox)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    //float yaw = (float) Math.toDegrees(Math.atan2(-((destinationVec.x + 0.5) - destinationVec.x), (destinationVec.y + 0.5) - destinationVec.z));

                    greatDoorLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);

                    serverPlayer.teleportTo(destinationLevel, destinationVec.x, destinationVec.y, destinationVec.z, 0, 0f);
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
                } else {
                    ModUtil.teleportEntity(player, destinationLevel, destinationVec);
                }
            }
        }
    }

    public boolean isDestinationValidDarkWorld(ServerLevel destinationLevel) {
        //Get fountain capability in own dark world
        DarkFountainCapability originFountainCapability = null;
        LazyOptional<DarkFountainCapability> originFountainLazyCapability = destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(originFountainLazyCapability.isPresent() && originFountainLazyCapability.resolve().isPresent())
            originFountainCapability = originFountainLazyCapability.resolve().get();

        if (originFountainCapability == null) {
            return false;
        }

        ServerLevel lightDoorLevel = destinationLevel.getServer().getLevel(lightDoorDimension);
        if (lightDoorLevel == null) {
            return false;
        }

        //Get origin fountain
        DarkFountain originFountain = null;
        for (Map.Entry<BlockPos, DarkFountain> entry : originFountainCapability.darkFountains.entrySet()) {
            originFountain = entry.getValue();
        }

        //If origin fountain is present
        if (originFountain != null) {
            return false;
        }





        //Get fountain capability in target dark world
        DarkFountainCapability destinationFountainCapability = null;
        LazyOptional<DarkFountainCapability> fountainLazyCapability = destinationLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(fountainLazyCapability.isPresent() && fountainLazyCapability.resolve().isPresent())
            destinationFountainCapability = fountainLazyCapability.resolve().get();

        if (destinationFountainCapability == null) {
            return false;
        }

        //Get destination fountain
        DarkFountain destinationFountain = null;
        for (Map.Entry<BlockPos, DarkFountain> entry : destinationFountainCapability.darkFountains.entrySet()) {
            destinationFountain = entry.getValue();
        }

        //If dark world's fountain is present
        if (destinationFountain != null) {
            return false;
        }

        GreatDoorCapability destinationDoorCapability = null;
        LazyOptional<GreatDoorCapability> doorLazyCapability = destinationLevel.getCapability(CapabilityRegistry.GREAT_DOOR);
        if(doorLazyCapability.isPresent() && doorLazyCapability.resolve().isPresent())
            destinationDoorCapability = doorLazyCapability.resolve().get();

        if (destinationDoorCapability == null) {
            return false;
        }

        GreatDoor destinationGreatDoor = destinationDoorCapability.greatDoors.get(lightDoorPos);
        return destinationGreatDoor != null;
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
        tag.put(DESTINATION_GREAT_DOOR_POS, NbtUtils.writeBlockPos(destinationGreatDoorPos));
        tag.putString(DESTINATION_GREAT_DOOR_DIMENSION, destinationGreatDoorDimension.location().toString());

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
        BlockPos destinationGreatDoorPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_GREAT_DOOR_POS));
        ResourceKey<Level> destinationGreatDoorDimension = ModUtil.stringToDimension(tag.getString(DESTINATION_GREAT_DOOR_DIMENSION));

        return new GreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos, lightDoorDimension, destinationGreatDoorPos, destinationGreatDoorDimension);
    }

    public void sync(GreatDoor greatDoor) {
        this.greatDoorPos = greatDoor.greatDoorPos;
        this.direction = greatDoor.direction;
        this.isOpen = greatDoor.isOpen;
        this.volumePositions = greatDoor.volumePositions;
        this.lightDoorPos = greatDoor.lightDoorPos;
        this.lightDoorDimension = greatDoor.lightDoorDimension;
        this.destinationGreatDoorPos = greatDoor.destinationGreatDoorPos;
        this.destinationGreatDoorDimension = greatDoor.destinationGreatDoorDimension;
    }
}
