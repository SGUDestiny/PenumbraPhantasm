package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.block.entity.FireDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundFireDoorScreenPacket {
    private final ResourceKey<Level> dimension;
    private final BlockPos doorPos;

    public ServerBoundFireDoorScreenPacket(ResourceKey<Level> dimension, BlockPos doorPos) {
        this.dimension = dimension;
        this.doorPos = doorPos;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(dimension);
        buffer.writeBlockPos(doorPos);
    }

    public static ServerBoundFireDoorScreenPacket decode(FriendlyByteBuf buffer) {
        ResourceKey<Level> dim = buffer.readResourceKey(Registries.DIMENSION);
        BlockPos pos = buffer.readBlockPos();
        return new ServerBoundFireDoorScreenPacket(dim, pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.getServer().getLevel(dimension);
            if (level != null && level.isLoaded(doorPos)) {
                if (level.getBlockEntity(doorPos) instanceof FireDoorBlockEntity be) {
                    be.setDoorState(level, doorPos, false);
                    be.decrementOpenCount();
                }
            }
        });
        return true;
    }
}
