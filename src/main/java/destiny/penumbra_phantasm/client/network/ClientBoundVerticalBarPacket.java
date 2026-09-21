package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientBoundVerticalBarPacket(int oldDetermination, ItemStack determinationSelectedItem, int determinationDifference,
                                           int determinationTargetTicker, int determinationDifferenceTicker, int determinationAppearTicker) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(oldDetermination);
        buffer.writeItem(determinationSelectedItem);
        buffer.writeInt(determinationDifference);
        buffer.writeInt(determinationTargetTicker);
        buffer.writeInt(determinationDifferenceTicker);
        buffer.writeInt(determinationAppearTicker);
    }

    public static ClientBoundVerticalBarPacket decode(FriendlyByteBuf buffer) {
        int oldDetermination = buffer.readInt();
        ItemStack determinationSelectedItem = buffer.readItem();
        int determinationDifference = buffer.readInt();
        int determinationTargetTicker = buffer.readInt();
        int determinationDifferenceTicker = buffer.readInt();
        int determinationAppearTicker = buffer.readInt();

        return new ClientBoundVerticalBarPacket(oldDetermination, determinationSelectedItem, determinationDifference, determinationTargetTicker, determinationDifferenceTicker, determinationAppearTicker);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(CapabilityRegistry.VERTICAL_BAR).ifPresent(cap -> {
                    cap.oldDetermination = oldDetermination;
                    cap.determinationSelectedItem = determinationSelectedItem;
                    cap.determinationDifference = determinationDifference;
                    cap.determinationTargetTicker = determinationTargetTicker;
                    cap.determinationDifferenceTicker = determinationDifferenceTicker;
                    cap.determinationAppearTicker = determinationAppearTicker;
                });
            }
        });
        return true;
    }
}
