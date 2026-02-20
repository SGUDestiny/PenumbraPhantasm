package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class DarknessLandOverlay {
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;

        if (player == null) return;

        //Get ticker from player capability here

        int ticker = 0;

        if (ticker == 0) {
            player.playSound(SoundRegistry.DARK_WORLD_FALL.get(), 0.5f, 1f);
        }
        if (ticker == 20) {
            player.playSound(SoundRegistry.HIM_QUICK.get(), 0.5f, 1f);
        }

        if (ticker >= 20) {
            float alphaDelta = ticker / 40f;
            float alpha = Mth.lerp(alphaDelta, 1f, 0f);

            PoseStack pose = guiGraphics.pose();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            RenderSystem.setShaderTexture(0, BLACK_SCREEN);

            pose.pushPose();
            pose.scale(1f, 1f, 1f);
            guiGraphics.blit(BLACK_SCREEN, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
            pose.popPose();
        }
    });
}
