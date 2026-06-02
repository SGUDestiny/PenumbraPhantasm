package destiny.penumbra_phantasm.client.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;


public record ClientBoundSoulSyncPacket(int soulType) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.soulType);
    }

    public static ClientBoundSoulSyncPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundSoulSyncPacket(buffer.readInt());
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientBoundPacketHandler.syncSoulType(soulType));
        return true;
    }
}