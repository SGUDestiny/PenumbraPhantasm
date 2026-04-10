package destiny.penumbra_phantasm.client.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientBoundDarknessFallPacket(BlockPos destinationPos, double spawnX, double spawnY, double spawnZ,
                                            float spawnYaw, ResourceKey<Level> dimension, boolean narrowGreatDoorPrepare,
                                            BlockPos arrivalGreatDoorAnchor) {

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(destinationPos);
        buffer.writeDouble(spawnX);
        buffer.writeDouble(spawnY);
        buffer.writeDouble(spawnZ);
        buffer.writeFloat(spawnYaw);
        buffer.writeResourceKey(dimension);
        buffer.writeBoolean(narrowGreatDoorPrepare);
        if (narrowGreatDoorPrepare) {
            buffer.writeBlockPos(arrivalGreatDoorAnchor);
        }
    }

    public static ClientBoundDarknessFallPacket decode(FriendlyByteBuf buffer) {
        BlockPos destinationPos = buffer.readBlockPos();
        double spawnX = buffer.readDouble();
        double spawnY = buffer.readDouble();
        double spawnZ = buffer.readDouble();
        float spawnYaw = buffer.readFloat();
        ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);
        boolean narrow = buffer.readBoolean();
        BlockPos arrivalGreatDoor = narrow ? buffer.readBlockPos() : BlockPos.ZERO;
        return new ClientBoundDarknessFallPacket(destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dimension, narrow, arrivalGreatDoor);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBoundPacketHandler.openDarknessFallScreen(destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dimension,
                narrowGreatDoorPrepare, arrivalGreatDoorAnchor));
        return true;
    }
}
