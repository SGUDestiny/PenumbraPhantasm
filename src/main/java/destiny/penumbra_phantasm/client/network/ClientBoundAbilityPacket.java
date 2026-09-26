package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientBoundAbilityPacket(int tension) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(tension);
    }

    public static ClientBoundAbilityPacket decode(FriendlyByteBuf buffer) {
        int tension = buffer.readInt();

        return new ClientBoundAbilityPacket(tension);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                player.getCapability(CapabilityRegistry.ABILITY).ifPresent(cap -> {
                    cap.tension = tension;
                });
            }
        });
        return true;
    }
}
