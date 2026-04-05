package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.registry.ScreenAnimationCapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientBoundSealShinePacket(int sealShineTicker) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(sealShineTicker);
    }

    public static ClientBoundSealShinePacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundSealShinePacket(buffer.readInt());
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(ScreenAnimationCapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> cap.sealShineTicker = sealShineTicker);
            }
        });
        return true;
    }
}
