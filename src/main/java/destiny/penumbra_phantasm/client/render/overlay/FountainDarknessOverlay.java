package destiny.penumbra_phantasm.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

public class FountainDarknessOverlay {
    public static final ResourceLocation DARKNESS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/fountain_darkness_old.png");
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

        int ticker = cap.darknessOverlayTicker;
        if (ticker <= 0)
            return;

        if(lastTick == -1)
            lastTick = 0;

        //Rendering shenanigans
        float tickerDelta = ticker / 100f;
        float alpha = Mth.lerp(tickerDelta, 0f, 3.5f);

        PoseStack pose = guiGraphics.pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        RenderSystem.setShaderTexture(0, DARKNESS);

        pose.pushPose();
        pose.scale(1f, 1f, 1f);
        guiGraphics.blit(DARKNESS, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        pose.popPose();

        lastTick = ticker;
    });
}
