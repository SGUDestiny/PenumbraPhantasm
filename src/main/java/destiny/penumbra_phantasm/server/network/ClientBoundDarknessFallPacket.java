package destiny.penumbra_phantasm.server.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundDarknessFallPacket {
    public final BlockPos destinationPos;
    public final double spawnX;
    public final double spawnY;
    public final double spawnZ;
    public final float spawnYaw;
    public final ResourceKey<Level> dimension;

    public ClientBoundDarknessFallPacket(BlockPos destinationPos, double spawnX, double spawnY, double spawnZ, float spawnYaw, ResourceKey<Level> dimension) {
        this.destinationPos = destinationPos;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.spawnYaw = spawnYaw;
        this.dimension = dimension;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(destinationPos);
        buffer.writeDouble(spawnX);
        buffer.writeDouble(spawnY);
        buffer.writeDouble(spawnZ);
        buffer.writeFloat(spawnYaw);
        buffer.writeResourceKey(dimension);
    }

    public static ClientBoundDarknessFallPacket decode(FriendlyByteBuf buffer) {
        BlockPos destinationPos = buffer.readBlockPos();
        double spawnX = buffer.readDouble();
        double spawnY = buffer.readDouble();
        double spawnZ = buffer.readDouble();
        float spawnYaw = buffer.readFloat();
        ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);
        return new ClientBoundDarknessFallPacket(destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dimension);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientboundPacketHandler.openDarknessFallScreen(destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dimension));
        return true;
    }
}
