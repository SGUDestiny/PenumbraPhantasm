package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundAnimationPacket {
    public final int darknessLandTicker;
    public final int darknessOverlayTicker;
    public final String previousLocation;
    public final String currentLocation;
    public final int titleAlphaTicker;

    public ClientBoundAnimationPacket(int darknessLandTicker, int darknessOverlayTicker, String previousLocation, String currentLocation, int titleAlphaTicker) {
        this.darknessLandTicker = darknessLandTicker;
        this.darknessOverlayTicker = darknessOverlayTicker;
        this.previousLocation = previousLocation;
        this.currentLocation = currentLocation;
        this.titleAlphaTicker = titleAlphaTicker;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(darknessLandTicker);
        buffer.writeInt(darknessOverlayTicker);
        buffer.writeUtf(previousLocation);
        buffer.writeUtf(currentLocation);
        buffer.writeInt(titleAlphaTicker);
    }

    public static ClientBoundAnimationPacket decode(FriendlyByteBuf buffer) {
        int darknessLandTicker = buffer.readInt();
        int darknessOverlayTicker = buffer.readInt();
        String previousLocation = buffer.readUtf();
        String currentLocation = buffer.readUtf();
        int titleAlphaTicker = buffer.readInt();

        return new ClientBoundAnimationPacket(darknessLandTicker, darknessOverlayTicker, previousLocation, currentLocation, titleAlphaTicker);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if(player != null) {
                player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
                    cap.darknessLandTicker = darknessLandTicker;
                    cap.darknessOverlayTicker = darknessOverlayTicker;
                    cap.previousLocation = previousLocation;
                    cap.currentLocation = currentLocation;
                    cap.titleAlphaTicker = titleAlphaTicker;
                });
            }
        });
        return true;
    }
}
