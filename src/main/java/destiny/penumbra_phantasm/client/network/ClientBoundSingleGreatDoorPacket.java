package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientBoundSingleGreatDoorPacket {
    public GreatDoor greatDoor;

    public ClientBoundSingleGreatDoorPacket(GreatDoor greatDoor) {
        this.greatDoor = greatDoor;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(greatDoor.greatDoorPos);
        buffer.writeUtf(greatDoor.direction.getName());
        buffer.writeBoolean(greatDoor.isOpen);
        buffer.writeCollection(greatDoor.volumePositions, FriendlyByteBuf::writeBlockPos);
        boolean hasLight = greatDoor.lightDoorPos != null && greatDoor.lightDoorDimension != null;
        buffer.writeBoolean(hasLight);
        if (hasLight) {
            buffer.writeBlockPos(greatDoor.lightDoorPos);
            buffer.writeBoolean(greatDoor.lightDoorSecondLower != null);
            if (greatDoor.lightDoorSecondLower != null) {
                buffer.writeBlockPos(greatDoor.lightDoorSecondLower);
            }
            buffer.writeResourceKey(greatDoor.lightDoorDimension);
            Direction exit = greatDoor.lightDoorExitDirection != null ? greatDoor.lightDoorExitDirection : Direction.NORTH;
            buffer.writeUtf(exit.getName());
        }
        buffer.writeBoolean(greatDoor.isDestinationDarkWorld);
        if (greatDoor.isDestinationDarkWorld && greatDoor.destinationGreatDoorPos != null && greatDoor.destinationGreatDoorDimension != null) {
            buffer.writeBoolean(true);
            buffer.writeBlockPos(greatDoor.destinationGreatDoorPos);
            buffer.writeResourceKey(greatDoor.destinationGreatDoorDimension);
        } else {
            buffer.writeBoolean(false);
        }
    }

    public static ClientBoundSingleGreatDoorPacket decode(FriendlyByteBuf buffer) {
        BlockPos greatDoorPos = buffer.readBlockPos();
        Direction direction = Direction.byName(buffer.readUtf());
        boolean isOpen = buffer.readBoolean();
        List<BlockPos> volumePositions = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readBlockPos);
        boolean hasLight = buffer.readBoolean();
        @Nullable BlockPos lightDoorPos = null;
        @Nullable BlockPos lightDoorSecondLower = null;
        @Nullable ResourceKey<Level> lightDoorDimension = null;
        @Nullable Direction lightDoorExitDirection = null;
        if (hasLight) {
            lightDoorPos = buffer.readBlockPos();
            if (buffer.readBoolean()) {
                lightDoorSecondLower = buffer.readBlockPos();
            }
            lightDoorDimension = buffer.readResourceKey(Registries.DIMENSION);
            lightDoorExitDirection = Direction.byName(buffer.readUtf());
            if (lightDoorExitDirection == null) {
                lightDoorExitDirection = Direction.NORTH;
            }
        }
        boolean isDestinationDarkWorld = buffer.readBoolean();
        BlockPos destinationGreatDoorPos = null;
        ResourceKey<Level> destinationGreatDoorDimension = null;
        if (buffer.readBoolean()) {
            destinationGreatDoorPos = buffer.readBlockPos();
            destinationGreatDoorDimension = buffer.readResourceKey(Registries.DIMENSION);
        }

        GreatDoor greatDoor = new GreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos, lightDoorSecondLower, lightDoorDimension,
                lightDoorExitDirection, isDestinationDarkWorld, destinationGreatDoorPos, destinationGreatDoorDimension);

        return new ClientBoundSingleGreatDoorPacket(greatDoor);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if(level != null)
            {
                level.getCapability(CapabilityRegistry.GREAT_DOOR).ifPresent(cap -> {
                    GreatDoor realGreatDoor = cap.greatDoors.get(greatDoor.greatDoorPos);
                    if(realGreatDoor == null)
                        realGreatDoor = greatDoor;
                    realGreatDoor.sync(this.greatDoor);
                    cap.greatDoors.put(realGreatDoor.greatDoorPos, realGreatDoor);
                });
            }
        });
        return true;
    }
}
