package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.network.ClientBoundAnimationPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class ScreenAnimationCapability implements INBTSerializable<CompoundTag> {
    public static final String DARKNESS_LAND_TICKER = "darknessLandTicker";
    public static final String DARKNESS_OVERLAY_TICKER = "darknessOverlayTicker";

    public int darknessLandTicker = -1;
    public int darknessOverlayTicker = -1;

    public void tick(Level level, Player player) {
        if (darknessLandTicker >= 40) {
            darknessLandTicker = -1;
        }
        if (darknessOverlayTicker <= 0) {
            darknessLandTicker = -1;
        }

        if (darknessLandTicker >= 0) {
            darknessLandTicker++;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundAnimationPacket(darknessLandTicker, darknessOverlayTicker));
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(DARKNESS_LAND_TICKER, darknessLandTicker);
        tag.putInt(DARKNESS_OVERLAY_TICKER, darknessOverlayTicker);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        darknessLandTicker = tag.getInt(DARKNESS_LAND_TICKER);
        darknessOverlayTicker = tag.getInt(DARKNESS_OVERLAY_TICKER);
    }

    public void sync(@NotNull ScreenAnimationCapability cap) {
        this.darknessLandTicker = cap.darknessLandTicker;
        this.darknessOverlayTicker = cap.darknessOverlayTicker;
    }
}
