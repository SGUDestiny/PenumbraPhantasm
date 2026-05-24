package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.block.FireDoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundFireDoorSyncPacket {
    private final ResourceKey<Level> dimension;
    private final BlockPos upperPos;
    private final boolean open;

    public ClientBoundFireDoorSyncPacket(ResourceKey<Level> dimension, BlockPos upperPos, boolean open) {
        this.dimension = dimension;
        this.upperPos = upperPos;
        this.open = open;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceKey(dimension);
        buf.writeBlockPos(upperPos);
        buf.writeBoolean(open);
    }

    public static ClientBoundFireDoorSyncPacket decode(FriendlyByteBuf buf) {
        return new ClientBoundFireDoorSyncPacket(
                buf.readResourceKey(Registries.DIMENSION),
                buf.readBlockPos(),
                buf.readBoolean()
        );
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || !mc.level.dimension().equals(dimension)) return;

            BlockState upper = mc.level.getBlockState(upperPos);
            if (upper.getBlock() instanceof FireDoorBlock) {
                mc.level.setBlock(upperPos, upper.setValue(BlockStateProperties.OPEN, open), Block.UPDATE_NONE);
                mc.levelRenderer.setBlocksDirty(
                        upperPos.getX(), upperPos.getY(), upperPos.getZ(),
                        upperPos.getX(), upperPos.getY(), upperPos.getZ()
                );
            }
        });
        return true;
    }
}