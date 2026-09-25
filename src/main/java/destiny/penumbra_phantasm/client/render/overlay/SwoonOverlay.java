package destiny.penumbra_phantasm.client.render.overlay;

import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class SwoonOverlay {
    public static int lastTick = -1;

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        ScreenAnimationCapability screenCap = player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).resolve().orElse(null);

        if (screenCap == null) return;

        int ticker = screenCap.swoonAnimationTicker;
        if (ticker < 0) {
            lastTick = -1;
            return;
        }
        if (ticker >= 55) return;

        if(lastTick == -1 || ticker == 0)
            lastTick = 0;

        if (ticker >= 2 && lastTick < 2) {
            player.playSound(SoundRegistry.KNIGHT_SWOON.get(), 1f, 1f);
        }

        lastTick = ticker;
    });
}
