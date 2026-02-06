package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.TypewriterText;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class IntroScreen extends Screen {

    Minecraft minecraft = Minecraft.getInstance();
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
    public static final ResourceLocation IMAGE_DEPTH = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth_blue_alt.png");
    public static final ResourceLocation BLURRY_SOUL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/blurry_soul.png");
    public final Runnable onFinished;
    public int screenLength = 120 * 20;
    public int droneLength = (int) (8.727 * 20);
    public int tick = droneLength * 5;
    //Depths lifetime - 8 seconds
    public float depthsLifetime = 8 * 20;
    public float depthsTick1 = 0f;
    public float depthsTick2 = depthsLifetime / 5f;
    public float depthsTick3 = depthsLifetime * 2f / 5f;
    public float depthsTick4 = depthsLifetime * 3f / 5f;
    public float depthsTick5 = depthsLifetime * 4f / 5f;

    public int depthsStart = droneLength * 5 + 3 * 20;

    public TypewriterText line1 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.1").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            2, 30, 0);
    public TypewriterText line2 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.2").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 80, 0).syncTransparency(line1);

    public TypewriterText line3 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.3").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 9 * 20, 0);
    public TypewriterText line4 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.4").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            2, 9 * 20 + 30, 0).syncTransparency(line3);

    public TypewriterText line5 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.5").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            2, 20 * 20, 0);

    public TypewriterText line6 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.6").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 26 * 20, 0);
    public TypewriterText line7 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.7").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            2, 27 * 20, 0).syncTransparency(line6);

    public TypewriterText line8 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.8").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 33 * 20, 0);

    public TypewriterText line9 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.9").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            2, 37 * 20, 0);
    public TypewriterText line10 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.10").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 39 * 20, 0).syncTransparency(line9);



    public TypewriterText line11 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.11").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 50 * 20, 20);

    public TypewriterText line12 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.12").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 53 * 20, 20);
    public TypewriterText line13 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.13").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 55 * 20, 20).syncTransparency(line12);

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
            if (tick == 16 * 20 || tick == 43 * 20) {
                minecraft.player.playSound(SoundRegistry.INTRO_APPEARANCE.get());
            }

            if (tick < depthsStart && tick < 36 * 20) {
                if (tick % droneLength == 0 || tick == 0) {
                    minecraft.player.playSound(SoundRegistry.INTRO_DRONE.get());
                }
            }
            if (tick == depthsStart) {
                minecraft.player.playSound(SoundRegistry.INTRO_ANOTHER_HIM.get());
            }
            if (tick > depthsStart) {
                depthsTick1 = (depthsTick1 + 1f) % depthsLifetime;
                depthsTick2 = (depthsTick2 + 1f) % depthsLifetime;
                depthsTick3 = (depthsTick3 + 1f) % depthsLifetime;
                depthsTick4 = (depthsTick4 + 1f) % depthsLifetime;
                depthsTick5 = (depthsTick5 + 1f) % depthsLifetime;
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


        renderBackground(graphics, pose);
        renderText(graphics, pose);
    }

    public void renderText(GuiGraphics graphics, PoseStack pose)
    {
        pose.pushPose();
        pose.translate(this.width / 2f, this.height / 2f, 0f);
        pose.scale(2.5f, 2.5f, 0);
        pose.translate(-this.width / 2f, -this.height / 2f, 0f);

        Component lineString1 = line1.getVisibleText(tick);
        drawString(graphics, lineString1,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2,
                this.height / 2 - 20, 0xFFFFFF, line1.getAlpha(tick));
        Component lineString2 = line2.getVisibleText(tick);
        drawString(graphics, lineString2,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2,
                this.height / 2 - 5, 0xFFFFFF, line2.getAlpha(tick));

        Component lineString3 = line3.getVisibleText(tick);
        drawString(graphics, lineString3,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2,
                this.height / 2 - 20, 0xFFFFFF, line3.getAlpha(tick));
        Component lineString4 = line4.getVisibleText(tick);
        drawString(graphics, lineString4,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2 - 10,
                this.height / 2 - 5, 0xFFFFFF, line4.getAlpha(tick));

        Component lineString5 = line5.getVisibleText(tick);
        drawString(graphics, lineString5,
                (this.width - Minecraft.getInstance().font.width(line5.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line5.getAlpha(tick));

        Component lineString6 = line6.getVisibleText(tick);
        drawString(graphics, lineString6,
                (this.width - Minecraft.getInstance().font.width(line6.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line6.getAlpha(tick));
        Component lineString7 = line7.getVisibleText(tick);
        drawString(graphics, lineString7,
                (this.width - Minecraft.getInstance().font.width(line7.text)) / 2,
                this.height / 2 - 35, 0xFFFFFF, line7.getAlpha(tick));

        Component lineString8 = line8.getVisibleText(tick);
        drawString(graphics, lineString8,
                (this.width - Minecraft.getInstance().font.width(line8.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line8.getAlpha(tick));

        Component lineString9 = line9.getVisibleText(tick);
        drawString(graphics, lineString9,
                (this.width - Minecraft.getInstance().font.width(line9.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line9.getAlpha(tick));
        Component lineString10 = line10.getVisibleText(tick);
        drawString(graphics, lineString10,
                (this.width - Minecraft.getInstance().font.width(line9.text)) / 2,
                this.height / 2 - 35, 0xFFFFFF, line10.getAlpha(tick));



        Component lineString11 = line11.getVisibleText(tick);
        drawString(graphics, lineString11,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 50, 0xFFFFFF, line11.getAlpha(tick));

        Component lineString12 = line12.getVisibleText(tick);
        drawString(graphics, lineString12,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 50, 0xFFFFFF, line12.getAlpha(tick));
        Component lineString13 = line13.getVisibleText(tick);
        drawString(graphics, lineString13,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 35, 0xFFFFFF, line13.getAlpha(tick));
    }

    public void renderBackground(GuiGraphics graphics, PoseStack pose)
    {
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

        float disappearStart = 43 * 20;
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
        float depthsColor = 1f;
        if (tick >= depthsStart && tick < depthsStart + depthsAppearDuration) {
            depthsColor = Mth.lerp(depthsAppearDelta, 0f, 0.5f);
        }

        if (this.tick > depthsStart) {
            //BG Depths
            pose.pushPose();
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            RenderBlitUtil.blit(IMAGE_DEPTH, pose, 0, 0, depthsColor, depthsColor, depthsColor, 1, 0, 0, this.width, this.height, this.width, this.height);
            pose.popPose();

            //Depths layer 5
            float depthsAlphaDelta5 = depthsTick5 / depthsLifetime;
            float depthsAlpha5 = depthsAlphaDelta5 <= 0.5f
                                         ? Mth.lerp(depthsAlphaDelta5 * 2.0f, 0.0f, 0.8f)
                                         : Mth.lerp((depthsAlphaDelta5 - 0.5f) * 2.0f, 0.8f, 0.0f);
            float depthsSize5 = Mth.lerp(depthsAlphaDelta5, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize5 / 2, 1 + depthsSize5 / 2, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderBlitUtil.blit(IMAGE_DEPTH, pose, 0, 0, depthsColor, depthsColor, depthsColor, depthsAlpha5, 0, 0, 0, this.width, this.height, this.width, this.height);
            pose.popPose();

            //Depths layer 4
            float depthsAlphaDelta4 = depthsTick4 / depthsLifetime;
            float depthsAlpha4 = depthsAlphaDelta4 <= 0.5f
                                         ? Mth.lerp(depthsAlphaDelta4 * 2.0f, 0.0f, 0.8f)
                                         : Mth.lerp((depthsAlphaDelta4 - 0.5f) * 2.0f, 0.8f, 0.0f);
            float depthsSize4 = Mth.lerp(depthsAlphaDelta4, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize4 / 2, 1 + depthsSize4 / 2, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderBlitUtil.blit(IMAGE_DEPTH, pose, 0, 0, depthsColor, depthsColor, depthsColor, depthsAlpha4, 0, 0, 0, this.width, this.height, this.width, this.height);
            pose.popPose();

            //Depths layer 3
            float depthsAlphaDelta3 = depthsTick3 / depthsLifetime;
            float depthsAlpha3 = depthsAlphaDelta3 <= 0.5f
                                         ? Mth.lerp(depthsAlphaDelta3 * 2.0f, 0.0f, 0.8f)
                                         : Mth.lerp((depthsAlphaDelta3 - 0.5f) * 2.0f, 0.8f, 0.0f);
            float depthsSize3 = Mth.lerp(depthsAlphaDelta3, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize3 / 2, 1 + depthsSize3 / 2, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderBlitUtil.blit(IMAGE_DEPTH, pose, 0, 0, depthsColor, depthsColor, depthsColor, depthsAlpha3, 0, 0, 0, this.width, this.height, this.width, this.height);
            pose.popPose();

            //Depths layer 2
            float depthsAlphaDelta2 = depthsTick2 / depthsLifetime;
            float depthsAlpha2 = depthsAlphaDelta2 <= 0.5f
                                         ? Mth.lerp(depthsAlphaDelta2 * 2.0f, 0.0f, 0.8f)
                                         : Mth.lerp((depthsAlphaDelta2 - 0.5f) * 2.0f, 0.8f, 0.0f);
            float depthsSize2 = Mth.lerp(depthsAlphaDelta2, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize2 / 2, 1 + depthsSize2 / 2, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            RenderBlitUtil.blit(IMAGE_DEPTH, pose, 0, 0, depthsColor, depthsColor, depthsColor, depthsAlpha2, 0, 0, 0, this.width, this.height, this.width, this.height);
            pose.popPose();

            //Depths layer 1
            float depthsAlphaDelta1 = depthsTick1 / depthsLifetime;
            float depthsAlpha1 = depthsAlphaDelta1 <= 0.5f
                                         ? Mth.lerp(depthsAlphaDelta1 * 2.0f, 0.0f, 0.8f)
                                         : Mth.lerp((depthsAlphaDelta1 - 0.5f) * 2.0f, 0.8f, 0.0f);
            float depthsSize1 = Mth.lerp(depthsAlphaDelta1, 0f, 1f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0);
            pose.scale(1 + depthsSize1 / 2, 1 + depthsSize1 / 2, 1);
            pose.translate(-this.width / 2f, -this.height / 2f, 0);
            //atlasLocation, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight
            RenderBlitUtil.blit(IMAGE_DEPTH, pose, 0, 0, depthsColor, depthsColor, depthsColor, depthsAlpha1, 0, 0, 0, this.width, this.height, this.width, this.height);
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

    public void drawString(GuiGraphics graphics, Component lineString, int x, int y, int color, float alpha)
    {
        RenderSystem.setShaderColor(1f ,1f, 1f, alpha);
        graphics.drawString(Minecraft.getInstance().font, lineString, x, y, color, false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
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
