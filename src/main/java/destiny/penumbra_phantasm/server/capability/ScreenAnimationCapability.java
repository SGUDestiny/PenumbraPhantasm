package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.network.ClientBoundAnimationPacket;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static destiny.penumbra_phantasm.client.sound.MusicManager.FOUNTAIN_MUSIC_RANGE;

public class ScreenAnimationCapability implements INBTSerializable<CompoundTag> {
    public static final String DARKNESS_LAND_TICKER = "darknessLandTicker";
    public static final String DARKNESS_OVERLAY_TICKER = "darknessOverlayTicker";

    public static final String PREVIOUS_LOCATION = "previousLocation";
    public static final String CURRENT_LOCATION = "currentLocation";
    public static final String TITLE_ALPHA_TICKER = "titleAlphaTicker";

    public int darknessLandTicker = -1;
    public int darknessOverlayTicker = -1;

    public String previousLocation = "";
    public String currentLocation = "";
    public int titleAlphaTicker = -1;

    public void tick(Level level, Player player) {
        if (darknessLandTicker >= 40) {
            darknessLandTicker = -1;
        }
        if (darknessLandTicker >= 0) {
            darknessLandTicker++;
        }

        if (darknessOverlayTicker <= 0) {
            darknessOverlayTicker = -1;
        }

        currentLocation = Util.makeDescriptionId("biome", level.getBiome(player.getOnPos()).unwrapKey().get().location());

        LazyOptional<DarkFountainCapability> lazyCap = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if (lazyCap.isPresent()) {
            DarkFountainCapability cap = lazyCap.resolve().orElse(null);

            for (Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet()) {
                DarkFountain fountain = entry.getValue();
                if (fountain.animationTimer != -1) continue;

                double distance = fountain.getFountainPos().getCenter().distanceTo(player.position());
                if (distance <= FOUNTAIN_MUSIC_RANGE) {
                    currentLocation = Util.makeDescriptionId("location", new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain"));
                }
            }
        }

        if (!previousLocation.equals(currentLocation)) {
            previousLocation = currentLocation;
            titleAlphaTicker = 0;
        }
        if (titleAlphaTicker >= 80) {
            titleAlphaTicker = -1;
        }
        if (titleAlphaTicker >= 0) {
            titleAlphaTicker++;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundAnimationPacket(darknessLandTicker, darknessOverlayTicker, previousLocation, currentLocation, titleAlphaTicker));
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(DARKNESS_LAND_TICKER, darknessLandTicker);
        tag.putInt(DARKNESS_OVERLAY_TICKER, darknessOverlayTicker);
        tag.putString(PREVIOUS_LOCATION, previousLocation);
        tag.putString(CURRENT_LOCATION, currentLocation);
        tag.putInt(TITLE_ALPHA_TICKER, titleAlphaTicker);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        darknessLandTicker = tag.getInt(DARKNESS_LAND_TICKER);
        darknessOverlayTicker = tag.getInt(DARKNESS_OVERLAY_TICKER);
        previousLocation = tag.getString(PREVIOUS_LOCATION);
        currentLocation = tag.getString(CURRENT_LOCATION);
        titleAlphaTicker = tag.getInt(TITLE_ALPHA_TICKER);
    }

    public void sync(@NotNull ScreenAnimationCapability cap) {
        this.darknessLandTicker = cap.darknessLandTicker;
        this.darknessOverlayTicker = cap.darknessOverlayTicker;
        this.previousLocation = cap.previousLocation;
        this.currentLocation = cap.currentLocation;
        this.titleAlphaTicker = cap.titleAlphaTicker;
    }
}
