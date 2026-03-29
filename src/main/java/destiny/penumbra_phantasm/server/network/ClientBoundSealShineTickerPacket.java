package destiny.penumbra_phantasm.server.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSealShineTickerPacket {
    private final int sealShineTicker;

    public ClientBoundSealShineTickerPacket(int sealShineTicker) {
        this.sealShineTicker = sealShineTicker;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(sealShineTicker);
    }

    public static ClientBoundTransportTickerPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundTransportTickerPacket(buffer.readInt());
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        return true;
    }
}
