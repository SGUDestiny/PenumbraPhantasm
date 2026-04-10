package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerBoundDarknessFallPacket(BlockPos destinationPos, double spawnX, double spawnY, double spawnZ,
                                            float spawnYaw, ResourceKey<Level> dimension) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(destinationPos);
        buffer.writeDouble(spawnX);
        buffer.writeDouble(spawnY);
        buffer.writeDouble(spawnZ);
        buffer.writeFloat(spawnYaw);
        buffer.writeResourceKey(dimension);
    }

    public static ServerBoundDarknessFallPacket decode(FriendlyByteBuf buffer) {
        BlockPos destinationPos = buffer.readBlockPos();
        double spawnX = buffer.readDouble();
        double spawnY = buffer.readDouble();
        double spawnZ = buffer.readDouble();
        float spawnYaw = buffer.readFloat();
        ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);
        return new ServerBoundDarknessFallPacket(destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dimension);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.getServer().getLevel(dimension);
            if (level == null) return;

            if (DarkWorldUtil.isDarkWorld(level)) {
                GreatDoor.prepareDarkWorldGreatDoorsAfterPlayerTravel(level);
            }

            player.teleportTo(level, spawnX, spawnY, spawnZ, spawnYaw, 0f);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
                cap.darknessLandTicker = 0;
            });

            level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                DarkFountain fountain = cap.darkFountains.get(destinationPos);
                if (fountain != null) {
                    fountain.teleportedEntities.add(player.getUUID());
                }
            });
        });
        return true;
    }
}
