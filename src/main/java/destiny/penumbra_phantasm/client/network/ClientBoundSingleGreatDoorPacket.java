package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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
        buffer.writeBlockPos(greatDoor.destinationDoorPos);
        buffer.writeResourceKey(greatDoor.destinationDoorDimension);
    }

    public static ClientBoundSingleGreatDoorPacket decode(FriendlyByteBuf buffer) {
        BlockPos greatDoorPos = buffer.readBlockPos();
        Direction direction = Direction.byName(buffer.readUtf());
        boolean isOpen = buffer.readBoolean();
        BlockPos destinationDoorPos = buffer.readBlockPos();
        ResourceKey<Level> destinationFountainDimension = buffer.readResourceKey(Registries.DIMENSION);

        GreatDoor greatDoor = new GreatDoor(greatDoorPos, direction, isOpen, destinationDoorPos, destinationFountainDimension);

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
