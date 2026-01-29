package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public class IntroScreen extends Screen {
    Minecraft minecraft = Minecraft.getInstance();
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
    public static final ResourceLocation IMAGE_DEPTH = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth_blue.png");
    public static final ResourceLocation BLURRY_SOUL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/blurry_soul.png");
    public final Runnable onFinished;
    public int screenLength = 40 * 20;
    public int droneLength = (int) (8.727 * 20);
    public int tick = droneLength;

    public IntroScreen(Runnable runnable) {
        super(GameNarrator.NO_TITLE);
        this.onFinished = runnable;
    }

    @Override
    public void tick() {
        if (tick > screenLength) {
            this.closeScreen();
        } else {
            if (tick < droneLength * 3) {
                if (tick % droneLength == 0) {
                    minecraft.player.playSound(SoundRegistry.INTRO_DRONE.get());
                }
            } else if (tick == droneLength * 3) {
                minecraft.player.playSound(SoundRegistry.INTRO_ANOTHER_HIM.get());
            }
            tick++;
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        graphics.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        if (this.tick > droneLength * 3) {
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        } else {
            graphics.blit(BLACK_SCREEN, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        }
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.pose().popPose();
        super.renderBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float p_282465_) {
        super.render(graphics, p_281550_, p_282878_, p_282465_);
    }

    @Override
    public void onClose()
    {

    }
    public void closeScreen() {
        this.onFinished.run();
    }
}
