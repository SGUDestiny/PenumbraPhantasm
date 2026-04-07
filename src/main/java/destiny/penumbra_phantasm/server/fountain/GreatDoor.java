package destiny.penumbra_phantasm.server.fountain;

import destiny.penumbra_phantasm.client.network.ClientBoundSingleFountainData;
import destiny.penumbra_phantasm.client.network.ClientBoundSingleGreatDoorPacket;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

public class GreatDoor {
    public static final String GREAT_DOOR_POS = "greatDoorPos";
    public static final String DIRECTION = "direction";
    public static final String IS_OPEN = "isOpen";
    public static final String DESTINATION_DOOR_POS = "destinationDoorPos";
    public static final String DESTINATION_FOUNTAIN_DIMENSION = "destinationFountainDimension";

    public BlockPos greatDoorPos;
    public Direction direction;
    public boolean isOpen;
    public BlockPos destinationDoorPos;
    public ResourceKey<Level> destinationFountainDimension;

    public GreatDoor(BlockPos greatDoorPos, Direction direction, boolean isOpen, BlockPos destinationDoorPos, ResourceKey<Level> destinationFountainDimension) {
        this.greatDoorPos = greatDoorPos;
        this.direction = direction;
        this.isOpen = isOpen;
        this.destinationDoorPos = destinationDoorPos;
        this.destinationFountainDimension = destinationFountainDimension;
    }

    public void tick(Level level) {
        if (level.isClientSide() || !DarkWorldUtil.isDarkWorld(level)) {
            return;
        }

        //Get fountain capability in great door's dark world
        DarkFountainCapability darkFountainCapability = null;
        LazyOptional<DarkFountainCapability> lightLazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if(lightLazyCapability.isPresent() && lightLazyCapability.resolve().isPresent())
            darkFountainCapability = lightLazyCapability.resolve().get();

        if (darkFountainCapability == null) {
            return;
        }

        DarkFountain darkFountain = null;
        for (Map.Entry<BlockPos, DarkFountain> entry : darkFountainCapability.darkFountains.entrySet()) {
            darkFountain = entry.getValue();
        }

        //If dark world's fountain is present
        if (darkFountain != null) {

        }

        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(greatDoorPos)), new ClientBoundSingleGreatDoorPacket(this));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.put(GREAT_DOOR_POS, NbtUtils.writeBlockPos(greatDoorPos));
        tag.putString(DIRECTION, direction.getName());
        tag.putBoolean(IS_OPEN, isOpen);
        tag.put(DESTINATION_DOOR_POS, NbtUtils.writeBlockPos(destinationDoorPos));
        tag.putString(DESTINATION_FOUNTAIN_DIMENSION, destinationFountainDimension.location().toString());

        return tag;
    }

    public static GreatDoor load(CompoundTag tag) {
        BlockPos greatDoorPos = NbtUtils.readBlockPos(tag.getCompound(GREAT_DOOR_POS));
        Direction direction = Direction.byName(tag.getString(DIRECTION));
        boolean isOpen = tag.getBoolean(IS_OPEN);
        BlockPos destinationDoorPos = NbtUtils.readBlockPos(tag.getCompound(DESTINATION_DOOR_POS));
        ResourceKey<Level> destinationFountainDimension = ModUtil.stringToDimension(tag.getString(DESTINATION_FOUNTAIN_DIMENSION));

        return new GreatDoor(greatDoorPos, direction, isOpen, destinationDoorPos, destinationFountainDimension);
    }

    public void sync(GreatDoor greatDoor) {
        this.greatDoorPos = greatDoor.greatDoorPos;
        this.direction = greatDoor.direction;
        this.isOpen = greatDoor.isOpen;
        this.destinationDoorPos = greatDoor.destinationDoorPos;
        this.destinationFountainDimension = greatDoor.destinationFountainDimension;
    }
}
