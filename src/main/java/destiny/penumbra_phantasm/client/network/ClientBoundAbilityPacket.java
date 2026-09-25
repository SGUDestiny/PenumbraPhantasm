package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record ClientBoundAbilityPacket(UUID targetEntityUUID, int realKnifeDelayTicker, int swoonDelayTicker) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(targetEntityUUID);
        buffer.writeInt(realKnifeDelayTicker);
        buffer.writeInt(swoonDelayTicker);
    }

    public static ClientBoundAbilityPacket decode(FriendlyByteBuf buffer) {
        UUID targetEntityUUID = buffer.readUUID();
        int realKnifeDelayTicker = buffer.readInt();
        int swoonDelayTicker = buffer.readInt();

        return new ClientBoundAbilityPacket(targetEntityUUID, realKnifeDelayTicker, swoonDelayTicker);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                player.getCapability(CapabilityRegistry.ABILITY).ifPresent(cap -> {
  /*                  cap.targetEntityUUID = targetEntityUUID;
                    cap.realKnifeDelayTicker = realKnifeDelayTicker;
                    cap.swoonDelayTicker = swoonDelayTicker;*/
                });
            }
        });
        return true;
    }
}
