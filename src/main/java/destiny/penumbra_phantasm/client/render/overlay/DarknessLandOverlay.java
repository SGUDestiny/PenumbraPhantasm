package destiny.penumbra_phantasm.client.render.overlay;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.client.registry.ScreenAnimationCapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

public class DarknessLandOverlay {
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
    public static int lastTick = -1;

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        ScreenAnimationCapability cap;
        LazyOptional<ScreenAnimationCapability> lazyCapability = player.getCapability(ScreenAnimationCapabilityRegistry.SCREEN_ANIMATION);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else return;

        int ticker = cap.darknessLandTicker;
        if (ticker < 0) {
            lastTick = -1;
            return;
        }
        if (ticker >= 40) return;

        if(lastTick == -1 || ticker == 0)
            lastTick = 0;

        if (ticker >= 2 && lastTick < 2) {
            player.playSound(SoundRegistry.DARK_WORLD_LAND.get(), 0.5f, 1f);
        }
        if (ticker >= 20 && lastTick < 20) {
            player.playSound(SoundRegistry.HIM_QUICK.get(), 0.5f, 1f);
        }

        lastTick = ticker;
    });
}
