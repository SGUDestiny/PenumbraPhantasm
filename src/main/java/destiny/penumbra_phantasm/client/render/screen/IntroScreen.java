package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class IntroScreen extends Screen {
    Minecraft minecraft = Minecraft.getInstance();
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
    public static final ResourceLocation IMAGE_DEPTH = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth_blue_alt.png");
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
    protected void init() {
        minecraft.getSoundManager().stop();
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
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        graphics.blit(BLACK_SCREEN, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        pose.popPose();

        if (this.tick > droneLength * 3) {
            pose.pushPose();
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            pose.popPose();

            pose.pushPose();
            graphics.blit(BLACK_SCREEN, 0, 0, 0, 0.0F, 0.0F, this.width / 8, this.height, this.width, this.height);
            pose.popPose();

            pose.pushPose();
            graphics.blit(BLACK_SCREEN, this.width - this.width / 8, 0, 0, 0.0F, 0.0F, this.width / 8, this.height, this.width, this.height);
            pose.popPose();

            pose.pushPose();
            pose.scale(2f, 2f, 2f);
            graphics.drawString(this.font, Component.literal("H O W   V E R Y").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))), this.width / 6, this.height / 6, 16777215);
            graphics.drawString(this.font, Component.literal("V E R Y").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))), this.width / 6, this.height / 6 + 14, 16777215);
            graphics.drawString(this.font, Component.literal("I N T E R E S T I N G .").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))), this.width / 6, this.height / 6 + 28, 16777215);
            pose.popPose();
        }
    }

    @Override
    public void onClose()
    {

    }
    public void closeScreen() {
        this.onFinished.run();
    }
}
