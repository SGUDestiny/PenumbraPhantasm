package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundAnimationPacket {
    public final int darknessLandTicker;

    public ClientBoundAnimationPacket(int darknessLandTicker) {
        this.darknessLandTicker = darknessLandTicker;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(darknessLandTicker);
    }

    public static ClientBoundAnimationPacket decode(FriendlyByteBuf buffer) {
        int darknessLandTicker = buffer.readInt();
        return new ClientBoundAnimationPacket(darknessLandTicker);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if(player != null) {
                player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
                    cap.darknessLandTicker = darknessLandTicker;
                });
            }
        });
        return true;
    }
}
