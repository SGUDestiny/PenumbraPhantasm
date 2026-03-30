package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientBoundAnimationPacket(int darknessLandTicker, int darknessOverlayTicker, String previousLocation,
                                         String currentLocation, int titleAlphaTicker, int sealShineTicker) {

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(darknessLandTicker);
        buffer.writeInt(darknessOverlayTicker);
        buffer.writeUtf(previousLocation);
        buffer.writeUtf(currentLocation);
        buffer.writeInt(titleAlphaTicker);
        buffer.writeInt(sealShineTicker);
    }

    public static ClientBoundAnimationPacket decode(FriendlyByteBuf buffer) {
        int darknessLandTicker = buffer.readInt();
        int darknessOverlayTicker = buffer.readInt();
        String previousLocation = buffer.readUtf();
        String currentLocation = buffer.readUtf();
        int titleAlphaTicker = buffer.readInt();
        int sealShineTicker = buffer.readInt();

        return new ClientBoundAnimationPacket(darknessLandTicker, darknessOverlayTicker, previousLocation, currentLocation, titleAlphaTicker, sealShineTicker);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
                    cap.darknessLandTicker = darknessLandTicker;
                    cap.darknessOverlayTicker = darknessOverlayTicker;
                    cap.previousLocation = previousLocation;
                    cap.currentLocation = currentLocation;
                    cap.titleAlphaTicker = titleAlphaTicker;
                    cap.sealShineTicker = sealShineTicker;
                });
            }
        });
        return true;
    }
}
