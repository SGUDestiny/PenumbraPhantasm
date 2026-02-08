package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ServerBoundSoulPacket;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.TypewriterText;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class IntroScreen extends Screen {

    Minecraft minecraft = Minecraft.getInstance();
    public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
    public static final ResourceLocation WHITE_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/white_screen.png");
    public static final ResourceLocation IMAGE_DEPTH = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/image_depth_blue_alt.png");
    public final Runnable onFinished;
    public boolean shouldClose = false;
    public final int droneLength = (int) (8.727 * 20);
    public final int depthsMusicLength = 48 * 20;
    public int tick = 0;
    public int outlineTick = 0;
    public int tickDepthsMusic = -1;
    public boolean stopDepthsMusic = false;
    //Depths lifetime - 8 seconds
    public final float depthsLifetime = 8 * 20;
    public float depthsTick1 = 0f;
    public float depthsTick2 = depthsLifetime / 5f;
    public float depthsTick3 = depthsLifetime * 2f / 5f;
    public float depthsTick4 = depthsLifetime * 3f / 5f;
    public float depthsTick5 = depthsLifetime * 4f / 5f;
    public final int depthsStart = droneLength * 5 + 3 * 20;

    public int tickText = 0;
    public boolean isChoosing = false;
    public final int choiceStart = 57 * 20;
    public int currentChoice = 1;
    public float oldChoiceSoulY = 0f;
    private int choiceLerpStartTick = -1;

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
            1, 50 * 20, 30);

    public TypewriterText line12 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.12").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 53 * 20, 30);
    public TypewriterText line13 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.13").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 55 * 20, 30).syncTransparency(line12);

    public TypewriterText choice1 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.choice.1").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, choiceStart, 30).syncTransparency(line12);
    public TypewriterText choice2 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.choice.2").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, choiceStart, 30).syncTransparency(line12);
    public TypewriterText choice3 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.choice.3").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, choiceStart, 30).syncTransparency(line12);
    public TypewriterText choice4 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.choice.4").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, choiceStart, 30).syncTransparency(line12);
    public TypewriterText choice5 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.choice.5").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, choiceStart, 30).syncTransparency(line12);
    public TypewriterText choice6 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.choice.6").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, choiceStart, 30).syncTransparency(line12);
    public TypewriterText choice7 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.choice.7").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, choiceStart, 30).syncTransparency(line12);



    public TypewriterText line14 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.14").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 59 * 20, 30);
    public TypewriterText line15 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.15.1").append(getSpacedNickname(minecraft.player.getName())).append(Component.translatable("screen.penumbra_phantasm.intro.line.15.2")).withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 63 * 20, 20);

    public TypewriterText line16 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.16").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 69 * 20, 30);
    public TypewriterText line17 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.17").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 71 * 20, 30).syncTransparency(line16);
    public TypewriterText line18 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.18").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 72 * 20, 30).syncTransparency(line16);

    public TypewriterText line19 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.19").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 76 * 20, 30);
    public TypewriterText line20 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.20").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 78 * 20, 30).syncTransparency(line19);
    public TypewriterText line21 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.21").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 79 * 20, 30).syncTransparency(line19);

    public TypewriterText line22 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.22").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 76 * 20, 30).syncTransparency(line19);;
    public TypewriterText line23 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.23").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 78 * 20, 30).syncTransparency(line19);
    public TypewriterText line24 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.24").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 79 * 20, 20).syncTransparency(line19);

    public TypewriterText line25 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.25").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 81 * 20 + 10, 30);

    public TypewriterText line26 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.26").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 84 * 20, 30);
    public TypewriterText line27 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.27").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 86 * 20, 30).syncTransparency(line26);

    public TypewriterText line28 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.28").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 89 * 20, 0);
    public TypewriterText line29 = new TypewriterText(Component.translatable("screen.penumbra_phantasm.intro.line.29").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
            1, 90 * 20, 0).syncTransparency(line28);

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
        if (shouldClose) {
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
                if (tickText == 88 * 20) {
                    minecraft.getSoundManager().stop();
                    stopDepthsMusic = true;
                    minecraft.player.playSound(SoundRegistry.GREAT_SHINE.get());
                }
                if (tickText == 92 * 20 + 5) {
                    minecraft.player.playSound(SoundRegistry.FOUNTAIN_SEAL.get());
                }
                if ((tickDepthsMusic % depthsMusicLength == 0 || tickDepthsMusic == 0) && !stopDepthsMusic) {
                    minecraft.player.playSound(SoundRegistry.INTRO_ANOTHER_HIM_LOOP.get());
                }
                if (tick > depthsStart + (100.567 * 20) && !stopDepthsMusic) {
                    tickDepthsMusic++;
                }

                depthsTick1 = (depthsTick1 + 1f) % depthsLifetime;
                depthsTick2 = (depthsTick2 + 1f) % depthsLifetime;
                depthsTick3 = (depthsTick3 + 1f) % depthsLifetime;
                depthsTick4 = (depthsTick4 + 1f) % depthsLifetime;
                depthsTick5 = (depthsTick5 + 1f) % depthsLifetime;
            }

            if (tick == choiceStart) {
                isChoosing = true;
            }
            if (!isChoosing) {
                tickText++;
            }

            if (outlineTick >= 60) {
                outlineTick = 0;
            } else {
                outlineTick++;
            }

            if (tickText >= 96 * 20) {
                shouldClose = true;
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

        float outlineAlphaDelta = outlineTick / 60f;
        float outlineAlpha = outlineAlphaDelta <= 0.5f
                ? Mth.lerp(outlineAlphaDelta * 2.0f, 0.2f, 0.5f)
                : Mth.lerp((outlineAlphaDelta - 0.5f) * 2.0f, 0.5f, 0.2f);

        Component lineString1 = line1.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString1,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2,
                this.height / 2 - 20, 0xFFFFFF, line1.getAlpha(tickText), outlineAlpha);
        Component lineString2 = line2.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString2,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2,
                this.height / 2 - 5, 0xFFFFFF, line1.getAlpha(tickText), outlineAlpha);

        Component lineString3 = line3.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString3,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2,
                this.height / 2 - 20, 0xFFFFFF, line3.getAlpha(tickText), outlineAlpha);
        Component lineString4 = line4.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString4,
                (this.width - Minecraft.getInstance().font.width(line1.text)) / 2 - 10,
                this.height / 2 - 5, 0xFFFFFF, line3.getAlpha(tickText), outlineAlpha);

        Component lineString5 = line5.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString5,
                (this.width - Minecraft.getInstance().font.width(line5.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line5.getAlpha(tickText), outlineAlpha);

        Component lineString6 = line6.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString6,
                (this.width - Minecraft.getInstance().font.width(line6.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line6.getAlpha(tickText), outlineAlpha);
        Component lineString7 = line7.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString7,
                (this.width - Minecraft.getInstance().font.width(line7.text)) / 2,
                this.height / 2 - 35, 0xFFFFFF, line6.getAlpha(tickText), outlineAlpha);

        Component lineString8 = line8.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString8,
                (this.width - Minecraft.getInstance().font.width(line8.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line8.getAlpha(tickText), outlineAlpha);

        Component lineString9 = line9.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString9,
                (this.width - Minecraft.getInstance().font.width(line9.text)) / 2,
                this.height / 2 - 50, 0xFFFFFF, line9.getAlpha(tickText), outlineAlpha);
        Component lineString10 = line10.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString10,
                (this.width - Minecraft.getInstance().font.width(line9.text)) / 2,
                this.height / 2 - 35, 0xFFFFFF, line9.getAlpha(tickText), outlineAlpha);



        Component lineString11 = line11.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString11,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 50, 0xFFFFFF, line11.getAlpha(tickText), outlineAlpha);

        Component lineString12 = line12.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString12,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 50, 0xFFFFFF, line12.getAlpha(tickText), outlineAlpha);
        Component lineString13 = line13.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString13,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 35, 0xFFFFFF, line12.getAlpha(tickText), outlineAlpha);



        float appearStart = choiceStart - 10;
        float appearDuration = 10f;
        float alpha = 1f;
        float appearDelta = (tickText - appearStart) / appearDuration;
        if (tickText >= appearStart && tickText < choiceStart) {
            alpha = Mth.lerp(appearDelta, 1f, 0f);
        } else if (tickText >= choiceStart) {
            alpha = 0f;
        }

        drawString(graphics, Component.translatable("screen.penumbra_phantasm.intro.choice.1").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 10, currentChoice == 1 ? 0xFFFF40 : 0xFFFFFF, line12.getAlpha(tickText) - alpha);
        drawString(graphics, Component.translatable("screen.penumbra_phantasm.intro.choice.2").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2, currentChoice == 2 ? 0xFFFF40 : 0xFFFFFF, line12.getAlpha(tickText) - alpha);
        drawString(graphics, Component.translatable("screen.penumbra_phantasm.intro.choice.3").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 + 10, currentChoice == 3 ? 0xFFFF40 : 0xFFFFFF, line12.getAlpha(tickText) - alpha);
        drawString(graphics, Component.translatable("screen.penumbra_phantasm.intro.choice.4").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 + 20, currentChoice == 4 ? 0xFFFF40 : 0xFFFFFF, line12.getAlpha(tickText) - alpha);
        drawString(graphics, Component.translatable("screen.penumbra_phantasm.intro.choice.5").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 + 30, currentChoice == 5 ? 0xFFFF40 : 0xFFFFFF, line12.getAlpha(tickText) - alpha);
        drawString(graphics, Component.translatable("screen.penumbra_phantasm.intro.choice.6").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 + 40, currentChoice == 6 ? 0xFFFF40 : 0xFFFFFF, line12.getAlpha(tickText) - alpha);
        drawString(graphics, Component.translatable("screen.penumbra_phantasm.intro.choice.7").withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))),
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 + 50, currentChoice == 7 ? 0xFFFF40 : 0xFFFFFF, line12.getAlpha(tickText) - alpha);



        Component lineString14 = line14.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString14,
                (this.width - Minecraft.getInstance().font.width(line14.text)) / 2,
                this.height / 2 - 30, 0xFFFFFF, line14.getAlpha(tickText), outlineAlpha);

        Component lineString15 = line15.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString15,
                (this.width - Minecraft.getInstance().font.width(line15.text)) / 2,
                this.height / 2 - 30, 0xFFFFFF, line15.getAlpha(tickText), outlineAlpha);

        Component lineString16 = line16.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString16,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 30, 0xFFFFFF, line16.getAlpha(tickText), outlineAlpha);
        Component lineString17 = line17.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString17,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 20, 0xFFFFFF, line16.getAlpha(tickText), outlineAlpha);
        Component lineString18 = line18.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString18,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 10, 0xFFFFFF, line16.getAlpha(tickText), outlineAlpha);

        Component lineString19 = line19.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString19,
                (this.width - Minecraft.getInstance().font.width(line19.text)) / 2 - 60,
                this.height / 2 - 30, 0xFFFFFF, line19.getAlpha(tickText), outlineAlpha);
        Component lineString20 = line20.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString20,
                (this.width - Minecraft.getInstance().font.width(line19.text)) / 2 - 60,
                this.height / 2 - 20, 0xFFFFFF, line19.getAlpha(tickText), outlineAlpha);
        Component lineString21 = line21.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString21,
                (this.width - Minecraft.getInstance().font.width(line19.text)) / 2 - 60,
                this.height / 2 - 10, 0xFFFFFF, line19.getAlpha(tickText), outlineAlpha);

        Component lineString22 = line22.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString22,
                (this.width - Minecraft.getInstance().font.width(line19.text)) / 2 + 60,
                this.height / 2 - 30, 0xFFFFFF, line19.getAlpha(tickText), outlineAlpha);
        Component lineString23 = line23.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString23,
                (this.width - Minecraft.getInstance().font.width(line19.text)) / 2 + 60,
                this.height / 2 - 20, 0xFFFFFF, line19.getAlpha(tickText), outlineAlpha);
        Component lineString24 = line24.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString24,
                (this.width - Minecraft.getInstance().font.width(line19.text)) / 2 + 60,
                this.height / 2 - 10, 0xFFFFFF, line19.getAlpha(tickText), outlineAlpha);

        Component lineString25 = line25.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString25,
                (this.width - Minecraft.getInstance().font.width(line25.text)) / 2,
                this.height / 2 - 30, 0xFFFFFF, line25.getAlpha(tickText), outlineAlpha);

        Component lineString26 = line26.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString26,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 40, 0xFFFFFF, line26.getAlpha(tickText), outlineAlpha);
        Component lineString27 = line27.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString27,
                (this.width - Minecraft.getInstance().font.width(line11.text)) / 2 - 40,
                this.height / 2 - 30, 0xFFFFFF, line26.getAlpha(tickText), outlineAlpha);

        Component lineString28 = line28.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString28,
                (this.width - Minecraft.getInstance().font.width(line28.text)) / 2,
                this.height / 2 - 40, 0xFFFFFF, line28.getAlpha(tickText), outlineAlpha);
        Component lineString29 = line29.getVisibleText(tickText);
        drawStringOutlined(graphics, lineString29,
                (this.width - Minecraft.getInstance().font.width(line28.text)) / 2 - 20,
                this.height / 2 - 30, 0xFFFFFF, line28.getAlpha(tickText), outlineAlpha);
    }

    public void renderBackground(GuiGraphics graphics, PoseStack pose)
    {
        ResourceLocation BLURRY_SOUL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/blurry_soul_" + currentChoice + ".png");

        pose.popPose();

        //SOUL sequence
        float appearStart = 16 * 20;
        float appearDuration = 20f;
        float soulY = 1f;
        float soulX = 1f;
        float appearDelta = (tick - appearStart) / appearDuration;
        if (tick >= appearStart && tick < appearStart + appearDuration) {
            soulY = Mth.lerp(appearDelta, 150f, 1.25f);
            soulX = Mth.lerp(appearDelta, 0f, 1.25f);
        }

        float disappearStart = 43 * 20;
        float disappearDelta = (tick - disappearStart) / appearDuration;
        if (tick >= disappearStart && tick < disappearStart + appearDuration) {
            soulY = Mth.lerp(disappearDelta, 1.25f, 150f);
            soulX = Mth.lerp(disappearDelta, 1.25f, 0f);
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
        float depthsColor = 0.5f;
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


            //Choice SOUL
            pose.pushPose();
            if (tickText >= choiceStart - 10 && tickText < choiceStart + 25) {
                //SOUL sequence
                float choiceSoulX = 0f;
                float choiceSoulAlpha = 1f;

                float choiceSoulAppearStart = choiceStart - 10;
                float choiceSoulAppearDuration = 10f;
                float choiceSoulAppearDelta = (tickText - choiceSoulAppearStart) / choiceSoulAppearDuration;
                if (tickText >= choiceStart - 10 && tickText < choiceStart) {
                    choiceSoulX = Mth.lerp(choiceSoulAppearDelta, -10f, 0f);
                    choiceSoulAlpha = Mth.lerp(choiceSoulAppearDelta, 0f, 1f);
                }

                float choiceSoulDisappearStart = choiceStart + 15;
                float choiceSoulDisappearDuration = 10f;
                float choiceSoulDisappearDelta = (tickText - choiceSoulDisappearStart) / choiceSoulDisappearDuration;
                if (tickText >= choiceStart + 15 && tickText < choiceStart + 25) {
                    choiceSoulAlpha = Mth.lerp(choiceSoulDisappearDelta, 1f, 0f);
                }

                float yPos;
                float targetY = -45f + (20 * currentChoice);
                if (choiceLerpStartTick == -1) {
                    yPos = targetY;
                } else {
                    float delta = Mth.clamp((tick - choiceLerpStartTick) / 3f, 0f, 1f);
                    yPos = Mth.lerp(delta, oldChoiceSoulY, targetY);
                    if (delta >= 1f) {
                        choiceLerpStartTick = -1;  // Reset once lerp completes
                    }
                }

                pose.translate(this.width / 2f, this.height / 2f, 0);
                pose.scale(1.25f, 1.25f, 1);
                pose.translate(-10f - 135f + choiceSoulX, yPos, 0f);
                RenderBlitUtil.blit(BLURRY_SOUL, pose, 0, 0, 1, 1, 1, choiceSoulAlpha, 0, 0.0F, 0.0F, 20, 20, 20, 20);
            }

            //Ending SOUL
            if (tickText >= 88 * 20) {
                float endingSoulAlpha = 0f;
                float endingSoulSize = 1f;
                float endingSoulSecondarySize = 0f;

                float endingSoulAppearStart = 88 * 20;
                float endingSoulAppearDuration = 10f;
                float endingSoulAppearDelta = (tickText - endingSoulAppearStart) / endingSoulAppearDuration;
                if (tickText < endingSoulAppearStart + endingSoulAppearDuration) {
                    endingSoulSize = Mth.lerp(endingSoulAppearDelta, 0f, 1f);
                }
                float endingSoulSecondaryAppearDuration = 20f;
                float endingSoulSecondaryAppearDelta = (tickText - endingSoulAppearStart) / endingSoulSecondaryAppearDuration;
                if (tickText < endingSoulAppearStart + endingSoulSecondaryAppearDuration) {
                    endingSoulAlpha = Mth.lerp(endingSoulSecondaryAppearDelta, 1f, 0f);
                    endingSoulSecondarySize = Mth.lerp(endingSoulSecondaryAppearDelta, 0f, 5f);
                }

                float endingSoulShineStart = 92 * 20;
                float endingSoulShineDuration = 20;
                float endingSoulSecondaryShineDelta = (tickText - endingSoulShineStart) / endingSoulShineDuration;
                if (tickText >= endingSoulShineStart && tickText < endingSoulShineStart + endingSoulShineDuration) {
                    endingSoulAlpha = Mth.lerp(endingSoulSecondaryShineDelta, 1f, 0f);
                    endingSoulSecondarySize = Mth.lerp(endingSoulSecondaryShineDelta, 0f, 5f);
                }

                pose.pushPose();
                pose.translate(this.width / 2f, this.height / 2f, 0);
                pose.scale(1.25f * endingSoulSize, 1.25f * endingSoulSize, 1);
                pose.translate(-10f, -10, 0f);
                RenderBlitUtil.blit(BLURRY_SOUL, pose, 0, 0, 1, 1, 1, 1, 0, 0.0F, 0.0F, 20, 20, 20, 20);
                pose.popPose();

                pose.pushPose();
                pose.translate(this.width / 2f, this.height / 2f, 0);
                pose.scale(endingSoulSecondarySize, endingSoulSecondarySize, 1);
                pose.translate(-10f, -10, 0f);
                RenderBlitUtil.blit(BLURRY_SOUL, pose, 0, 0, 1, 1, 1, endingSoulAlpha, 0, 0.0F, 0.0F, 20, 20, 20, 20);
                pose.popPose();
            }

            //Ending
            if (tickText >= 92 * 20 + 5) {
                float endingSizeX1 = 1;
                float endingSizeX2 = 2;
                float endingSizeX3 = 3;

                float endingAlpha1 = 1f;
                float endingAlpha2 = 1f;
                float endingAlpha3 = 1f;
                float endingStart = 92 * 20 + 5;
                float endingDuration = 60;
                float endingDelta = (tickText - endingStart) / endingDuration;
                if (tickText < endingStart + endingDuration) {
                    endingSizeX1 = Mth.lerp(endingDelta, 0, 1);
                    endingSizeX2 = Mth.lerp(endingDelta, 0, 2);
                    endingSizeX3 = Mth.lerp(endingDelta, 0, 3);

                    endingAlpha1 = Mth.lerp(endingDelta, 0.075f, 1);
                    endingAlpha2 = Mth.lerp(endingDelta, 0.05f, 1);
                    endingAlpha3 = Mth.lerp(endingDelta, 0.025f, 1);
                } else {
                    endingSizeX1 = 1;
                    endingSizeX2 = 2;
                    endingSizeX3 = 3;

                    endingAlpha1 = 1f;
                    endingAlpha2 = 1f;
                    endingAlpha3 = 1f;
                }

                pose.pushPose();
                pose.translate(this.width / 2f, this.height / 2f, 0);
                pose.scale(endingSizeX3, 1, 1);
                pose.translate(-this.width / 2f, -this.height / 2f, 0);
                RenderBlitUtil.blit(WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha3, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
                pose.popPose();

                pose.pushPose();
                pose.translate(this.width / 2f, this.height / 2f, 0);
                pose.scale(endingSizeX2, 1, 1);
                pose.translate(-this.width / 2f, -this.height / 2f, 0);
                RenderBlitUtil.blit(WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha2, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
                pose.popPose();

                pose.pushPose();
                pose.translate(this.width / 2f, this.height / 2f, 0);
                pose.scale(endingSizeX1, 1, 1);
                pose.translate(-this.width / 2f, -this.height / 2f, 0);
                RenderBlitUtil.blit(WHITE_SCREEN, pose, 0, 0, 1, 1, 1, endingAlpha1, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
                pose.popPose();
            }
            pose.popPose();
        }
    }

    public void drawString(GuiGraphics graphics, Component lineString, int x, int y, int color, float alpha)
    {
        RenderSystem.setShaderColor(1f ,1f, 1f, alpha);
        graphics.drawString(Minecraft.getInstance().font, lineString, x, y, color, false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public void drawStringOutlined(GuiGraphics graphics, Component lineString, int x, int y, int color, float alpha, float alphaOutline)
    {
        Font font = Minecraft.getInstance().font;
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        renderTextLayer(graphics, font, lineString, x + 1, y, color, alphaOutline);
        renderTextLayer(graphics, font, lineString, x - 1, y, color, alphaOutline);
        renderTextLayer(graphics, font, lineString, x, y + 1, color, alphaOutline);
        renderTextLayer(graphics, font, lineString, x, y - 1, color, alphaOutline);

        renderTextLayer(graphics, font, lineString, x, y, color, alpha);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void renderTextLayer(GuiGraphics graphics, Font font, Component text, int x, int y, int color, float alpha) {
        if (alpha <= 0.05f) return; // Optimization: don't render if invisible

        // Extract RGB and apply our custom alpha
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255);
        int finalColor = (a << 24) | (r << 16) | (g << 8) | b;

        graphics.drawString(font, text, x, y, finalColor, false);
    }

    public void incrementChoice(int increment) {
        if (increment != 0) {
            // Record old Y before changing choice
            float oldY = -45f + (20 * currentChoice);
            int oldChoice = currentChoice;
            currentChoice = Mth.clamp(currentChoice + increment, 1, 7);
            if (currentChoice != oldChoice) {
                oldChoiceSoulY = oldY;
                choiceLerpStartTick = tick;
            }
        }
    }

    public void pickChoice()
    {
        isChoosing = false;
        // Choose what is currently selected(Pressing ENTER)
    }

    public Component getSpacedNickname(Component nickname) {
        String spacedNickname = String.join(" ", nickname.getString().split(""));
        return Component.literal(spacedNickname);
    }

    @Override
    public void onClose() {
        if (minecraft.player.getAbilities().instabuild) {
            this.onFinished.run();
            minecraft.getSoundManager().stop();
            PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundSoulPacket(currentChoice));
        }
    }

    public void closeScreen() {
        this.onFinished.run();
        minecraft.getSoundManager().stop();
        PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundSoulPacket(currentChoice));
    }
}