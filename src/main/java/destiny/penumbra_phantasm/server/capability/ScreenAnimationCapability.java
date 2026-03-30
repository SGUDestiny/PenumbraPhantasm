package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.network.ClientBoundAnimationPacket;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static destiny.penumbra_phantasm.client.sound.MusicManager.FOUNTAIN_MUSIC_RANGE;

public class ScreenAnimationCapability implements INBTSerializable<CompoundTag> {
    public static final String DARKNESS_LAND_TICKER = "darknessLandTicker";
    public static final String DARKNESS_OVERLAY_TICKER = "darknessOverlayTicker";

    public static final String PREVIOUS_LOCATION = "previousLocation";
    public static final String CURRENT_LOCATION = "currentLocation";
    public static final String TITLE_ALPHA_TICKER = "titleAlphaTicker";

    public static final String SEAL_SHINE_TICKER = "sealShineTicker";

    public int darknessLandTicker = -1;
    public int darknessOverlayTicker = -1;

    public String previousLocation = "";
    public String currentLocation = "";
    public int titleAlphaTicker = -1;
    public List<String> visitedLocations = new ArrayList<>();

    public int sealShineTicker = -1;

    public void tick(Level level, Player player) {
        if (darknessLandTicker >= 40) {
            darknessLandTicker = -1;
        }
        if (darknessLandTicker >= 0) {
            darknessLandTicker++;
        }

        if (darknessOverlayTicker > 0) {
            darknessOverlayTicker--;
        }
        if (darknessOverlayTicker <= 0) {
            darknessOverlayTicker = -1;
        }

        if (sealShineTicker >= 7 * 20) {
            sealShineTicker = -1;
        }
        if (sealShineTicker >= 0) {
            sealShineTicker++;
        }

        //Location title stuff below this point
        currentLocation = Util.makeDescriptionId("biome", level.getBiome(player.getOnPos()).unwrapKey().get().location());

        LazyOptional<DarkFountainCapability> lazyCap = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
        if (lazyCap.isPresent()) {
            DarkFountainCapability cap = lazyCap.resolve().orElse(null);

            for (Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet()) {
                DarkFountain fountain = entry.getValue();
                if (fountain.animationTimer != -1) continue;

                double distance = fountain.getFountainPos().getCenter().subtract(player.position()).horizontalDistance();
                if (distance <= FOUNTAIN_MUSIC_RANGE) {
                    currentLocation = Util.makeDescriptionId("location", new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain"));
                }
            }
        }

        if (!previousLocation.equals(currentLocation)) {
            boolean darkWorldLandFinished = darknessLandTicker < 0 || darknessLandTicker >= 20;
            boolean fountainTransitionFinished = darknessOverlayTicker <= 0;
            boolean notVisited = true;

            for (String visitedLocation : visitedLocations) {
                if (visitedLocation.equals(currentLocation)) {
                    notVisited = false;
                    break;
                }
            }

            if (darkWorldLandFinished && fountainTransitionFinished && notVisited) {
                previousLocation = currentLocation;
                visitedLocations.add(currentLocation);
                titleAlphaTicker = 0;
            }
        }
        if (titleAlphaTicker >= 80) {
            titleAlphaTicker = -1;
        }
        if (titleAlphaTicker >= 0) {
            titleAlphaTicker++;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    public void syncToClient(ServerPlayer serverPlayer) {
        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundAnimationPacket(darknessLandTicker, darknessOverlayTicker, previousLocation, currentLocation, titleAlphaTicker, sealShineTicker));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
    }

    public void sync(@NotNull ScreenAnimationCapability cap) {
        this.darknessLandTicker = cap.darknessLandTicker;
        this.darknessOverlayTicker = cap.darknessOverlayTicker;
        this.previousLocation = cap.previousLocation;
        this.currentLocation = cap.currentLocation;
        this.titleAlphaTicker = cap.titleAlphaTicker;
        this.sealShineTicker = cap.sealShineTicker;
    }
}
