package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class IntroScreen extends Screen {
    Minecraft minecraft = Minecraft.getInstance();
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
    public static final ResourceLocation IMAGE_DEPTH = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth_blue_alt.png");
    public static final ResourceLocation BLURRY_SOUL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/blurry_soul.png");
    public final Runnable onFinished;
    public int screenLength = 120 * 20;
    public int droneLength = (int) (8.727 * 20);
    public int tick = droneLength;
    //Depths lifetime - 8 seconds
    public float depthsLifetime = 8 * 20;
    public float depthsTick1 = 0;
    public float depthsTick2 = depthsLifetime / 5;
    public float depthsTick3 = (depthsLifetime / 5) * 2;
    public float depthsTick4 = (depthsLifetime / 5) * 3;
    public float depthsTick5 = (depthsLifetime / 5) * 4;

    public int depthsStart = droneLength * 5;

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
            if (tick == 16 * 20 || tick == 40 * 20) {
                minecraft.player.playSound(SoundRegistry.INTRO_APPEARANCE.get());
            }

            if (tick < depthsStart) {
                if (tick % droneLength == 0 || tick == 0) {
                    minecraft.player.playSound(SoundRegistry.INTRO_DRONE.get());
                }
            }
            if (tick == depthsStart) {
                minecraft.player.playSound(SoundRegistry.INTRO_ANOTHER_HIM.get());
            }
            if (tick > depthsStart) {
                if (depthsTick1 >= depthsLifetime) {
                    depthsTick1 = 0;
                } else {
                    depthsTick1++;
                }
                if (depthsTick2 >= depthsLifetime) {
                    depthsTick2 = 0;
                } else {
                    depthsTick2++;
                }
                if (depthsTick3 >= depthsLifetime) {
                    depthsTick3 = 0;
                } else {
                    depthsTick3++;
                }
                if (depthsTick4 >= depthsLifetime) {
                    depthsTick4 = 0;
                } else {
                    depthsTick4++;
                }
                if (depthsTick5 >= depthsLifetime) {
                    depthsTick5 = 0;
                } else {
                    depthsTick5++;
                }
            }
            tick++;

            System.out.println("---");
            System.out.println("depthsTick1: "+ depthsTick1);
            System.out.println("depthsTick2: "+ depthsTick2);
            System.out.println("depthsTick3: "+ depthsTick3);
            System.out.println("depthsTick4: "+ depthsTick4);
            System.out.println("depthsTick5: "+ depthsTick5);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        graphics.blit(BLACK_SCREEN, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        pose.popPose();

        //SOUL sequence
        float appearStart = 16 * 20;
        float appearDuration = 20f;
        float soulY = 1f;
        float soulX = 1f;
        float appearDelta = (tick - appearStart) / appearDuration;
        if (tick >= appearStart && tick < appearStart + appearDuration) {
            soulY = Mth.lerp(appearDelta, 150f, 1f);
            soulX = Mth.lerp(appearDelta, 0f, 1f);
        }

        float disappearStart = 40 * 20;
        float disappearDelta = (tick - disappearStart) / appearDuration;
        if (tick >= disappearStart && tick < disappearStart + appearDuration) {
            soulY = Mth.lerp(disappearDelta, 1f, 150f);
            soulX = Mth.lerp(disappearDelta, 1f, 0f);
        }

        pose.pushPose();
        pose.translate(this.width / 2f, this.height / 2f, 0);
        if (tick >= appearStart && tick < disappearStart + appearDuration) {
            pose.scale(soulX, soulY, 1f);
            pose.translate(-10f, -10f + (3 * Math.sin(tick * 0.1f)), 0f);
            graphics.blit(BLURRY_SOUL, 0, 0, 0, 0.0F, 0.0F, 20, 20, 20, 20);
        }
        pose.popPose();



        float depthsAppearDuration = 5 * 20f;
        float depthsAppearDelta = (tick - depthsStart) / depthsAppearDuration;
        float depthsAlpha = 1f;
        if (tick >= depthsStart && tick < depthsStart + depthsAppearDuration) {
            depthsAlpha = Mth.lerp(depthsAppearDelta, 0f, 1f);
        }

        if (this.tick > depthsStart) {
            //BG Depths
            pose.pushPose();
            RenderSystem.setShaderColor(depthsAlpha, depthsAlpha, depthsAlpha, 1);
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            pose.popPose();

            //Depths layer 1
            float depthsAlphaDelta1 = depthsTick1 / depthsLifetime;
            float depthsAlpha1 = Mth.lerp(depthsAlphaDelta1, 1f, 0f);
            float depthsSize1 = Mth.lerp(depthsAlphaDelta1, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize1, 1 + depthsSize1, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderSystem.setShaderColor(depthsAlpha, depthsAlpha, depthsAlpha, depthsAlpha1);
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            pose.popPose();

            //Depths layer 2
            float depthsAlphaDelta2 = depthsTick2 / depthsLifetime;
            float depthsAlpha2 = Mth.lerp(depthsAlphaDelta2, 1f, 0f);
            float depthsSize2 = Mth.lerp(depthsAlphaDelta2, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize2, 1 + depthsSize2, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderSystem.setShaderColor(depthsAlpha, depthsAlpha, depthsAlpha, depthsAlpha2);
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            pose.popPose();

            //Depths layer 3
            float depthsAlphaDelta3 = depthsTick3 / depthsLifetime;
            float depthsAlpha3 = Mth.lerp(depthsAlphaDelta3, 1f, 0f);
            float depthsSize3 = Mth.lerp(depthsAlphaDelta3, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize3, 1 + depthsSize3, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderSystem.setShaderColor(depthsAlpha, depthsAlpha, depthsAlpha, depthsAlpha3);
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            pose.popPose();

            //Depths layer 4
            float depthsAlphaDelta4 = depthsTick4 / depthsLifetime;
            float depthsAlpha4 = Mth.lerp(depthsAlphaDelta4, 1f, 0f);
            float depthsSize4 = Mth.lerp(depthsAlphaDelta4, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize4, 1 + depthsSize4, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderSystem.setShaderColor(depthsAlpha, depthsAlpha, depthsAlpha, depthsAlpha4);
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            pose.popPose();

            //Depths layer 5
            float depthsAlphaDelta5 = depthsTick5 / depthsLifetime;
            float depthsAlpha5 = Mth.lerp(depthsAlphaDelta5, 1f, 0f);
            float depthsSize5 = Mth.lerp(depthsAlphaDelta5, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize5, 1 + depthsSize5, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderSystem.setShaderColor(depthsAlpha, depthsAlpha, depthsAlpha, depthsAlpha5);
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            graphics.blit(IMAGE_DEPTH, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            pose.popPose();

            //Black bars
            pose.pushPose();
            graphics.blit(BLACK_SCREEN, 0, 0, 0, 0.0F, 0.0F, this.width / 8, this.height, this.width, this.height);
            pose.popPose();

            pose.pushPose();
            graphics.blit(BLACK_SCREEN, this.width - this.width / 8, 0, 0, 0.0F, 0.0F, this.width / 8, this.height, this.width, this.height);
            pose.popPose();

            pose.pushPose();
            pose.scale(2f, 2f, 2f);
            //graphics.drawString(this.font, Component.literal("H O W   V E R Y").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))), this.width / 6, this.height / 6, 16777215);
            //graphics.drawString(this.font, Component.literal("V E R Y").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))), this.width / 6, this.height / 6 + 14, 16777215);
            //graphics.drawString(this.font, Component.literal("I N T E R E S T I N G .").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))), this.width / 6, this.height / 6 + 28, 16777215);
            pose.popPose();
        }
    }

    @Override
    public void onClose()
    {
        this.onFinished.run();
    }
    public void closeScreen() {
        this.onFinished.run();
    }
}
