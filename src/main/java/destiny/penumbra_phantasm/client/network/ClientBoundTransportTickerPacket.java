package destiny.penumbra_phantasm.client.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundTransportTickerPacket {

    private final float progress;

    public ClientBoundTransportTickerPacket(float progress) {
        this.progress = progress;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(progress);
    }

    public static ClientBoundTransportTickerPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundTransportTickerPacket(buffer.readFloat());
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientboundPacketHandler.updateTransportVeil(progress));
        return true;
    }
}
