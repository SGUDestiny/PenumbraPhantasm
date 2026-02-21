package destiny.penumbra_phantasm.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

public class DarknessLandOverlay {
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
    public static int lastTick = -1;

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        //Getting capability
        ScreenAnimationCapability cap;
        LazyOptional<ScreenAnimationCapability> lazyCapability = player.getCapability(CapabilityRegistry.SCREEN_ANIMATION);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else return; // If capability isn't present

        int ticker = cap.darknessLandTicker;
        if (ticker < 0)
            return;

        if(lastTick == -1 || ticker == 0)
            lastTick = 0;

        if (ticker == 2 && lastTick != 2) {
            player.playSound(SoundRegistry.DARK_WORLD_LAND.get(), 0.5f, 1f);
        }
        if (ticker == 20 && lastTick != 20) {
            player.playSound(SoundRegistry.HIM_QUICK.get(), 0.5f, 1f);
        }

        float alpha = 1f;
        if (ticker >= 20) {
            float alphaDelta = ticker / 40f;
            alpha = Mth.lerp(alphaDelta, 1f, 0f);
        }

        PoseStack pose = guiGraphics.pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        RenderSystem.setShaderTexture(0, BLACK_SCREEN);

        pose.pushPose();
        pose.scale(1f, 1f, 1f);
        guiGraphics.blit(BLACK_SCREEN, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        pose.popPose();

        lastTick = ticker;
    });
}
