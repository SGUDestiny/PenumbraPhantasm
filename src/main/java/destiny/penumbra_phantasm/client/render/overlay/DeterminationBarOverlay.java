package destiny.penumbra_phantasm.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.capability.VerticalBarCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

public class DeterminationBarOverlay {
    private static final ResourceLocation VERTICAL_BAR_OUTSIDE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/vertical_bar/vertical_bar_outside.png");
    private static final ResourceLocation VERTICAL_BAR_INSIDE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/vertical_bar/vertical_bar_inside.png");
    private static final ResourceLocation VERTICAL_BAR_DT_LABEL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/vertical_bar/vertical_bar_dt_label.png");

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;
        PoseStack pose = guiGraphics.pose();

        if (player == null) return;

        Level level = player.level();

        if (!DarkWorldUtil.isDarkWorld(level)) return;

        //Getting capability
        VerticalBarCapability verticalBarCap;
        LazyOptional<VerticalBarCapability> lazyVerticalBarCap = player.getCapability(CapabilityRegistry.VERTICAL_BAR);
        if(lazyVerticalBarCap.isPresent() && lazyVerticalBarCap.resolve().isPresent())
            verticalBarCap = lazyVerticalBarCap.resolve().get();
        else return; // If capability isn't present

        int determinationAppearTicker = verticalBarCap.determinationAppearTicker;

        if (determinationAppearTicker < 0) return;

        int difference = verticalBarCap.determinationDifference;
        int oldDetermination = verticalBarCap.oldDetermination;
        int targetTicker = verticalBarCap.determinationTargetTicker;
        int differenceTicker = verticalBarCap.determinationDifferenceTicker;

        //Getting capability
        SoulCapability soulCap;
        LazyOptional<SoulCapability> lazySoulCap = player.getCapability(CapabilityRegistry.SOUL);
        if(lazySoulCap.isPresent() && lazySoulCap.resolve().isPresent())
            soulCap = lazySoulCap.resolve().get();
        else return; // If capability isn't present

        int determination = soulCap.determination;

        int xTarget = width - 60;
        float xPos;
        float totalAlpha;

        if (determinationAppearTicker < 10) {
            float appearDelta = determinationAppearTicker / 10f;
            xPos = Mth.lerp(appearDelta, width, xTarget);
            totalAlpha = Mth.lerp(appearDelta, 0, 1);
        } else {
            xPos = xTarget;
            totalAlpha = 1;
        }

        pose.pushPose();
        pose.translate(xPos, height / 2f, 0);

        float targetBarFillDelta;
        float barFillDelta;
        if (targetTicker > -1 && targetTicker < 6) {
            if (oldDetermination > determination) {
                //Target bar fill
                if (targetTicker >= 3) {
                    float targetTickerDelta = Mth.clamp((targetTicker - 3f) / 3f, 0, 1);
                    float targetProgress = Mth.lerp(targetTickerDelta, oldDetermination, determination);
                    targetBarFillDelta = targetProgress / 100f;
                } else {
                    targetBarFillDelta = oldDetermination / 100f;
                }

                //Bar fill
                float targetTickerDelta = Mth.clamp(targetTicker / 6f, 0, 1);
                float targetProgress = Mth.lerp(targetTickerDelta, oldDetermination, determination);
                barFillDelta = targetProgress / 100f;
            } else {
                //Bar fill
                if (targetTicker >= 3) {
                    float targetTickerDelta = Mth.clamp((targetTicker - 3f) / 3f, 0, 1);
                    float targetProgress = Mth.lerp(targetTickerDelta, oldDetermination, determination);
                    barFillDelta = targetProgress / 100f;
                } else {
                    barFillDelta = oldDetermination / 100f;
                }

                //Target bar fill
                float targetTickerDelta = Mth.clamp(targetTicker / 6f, 0, 1);
                float targetProgress = Mth.lerp(targetTickerDelta, oldDetermination, determination);
                targetBarFillDelta = targetProgress / 100f;
            }
        } else {
            targetBarFillDelta = determination / 100f;
            barFillDelta = determination / 100f;
        }

        float targetBarFillHeight = Mth.lerp(targetBarFillDelta, 4, 196 - 3);
        float barFillHeight = Mth.lerp(barFillDelta, 4, 196 - 3);

        float differenceBarFillDelta;
        if (differenceTicker > -1 && differenceTicker < 6) {
            float differenceTickerDelta = differenceTicker / 6f;

            float differenceProgress;
            if (difference < 0) {
                differenceProgress = Mth.lerp(differenceTickerDelta, 0, Math.min(-difference, determination));
            } else {
                differenceProgress = Mth.lerp(differenceTickerDelta, determination, Math.min(determination + difference, 100));
            }

            differenceBarFillDelta = differenceProgress / 100f;
        } else {
            if (difference < 0) {
                differenceBarFillDelta = Math.min(-difference, determination) / 100f;
            } else {
                differenceBarFillDelta = Math.min(determination + difference, 100) / 100f;
            }
        }

        float differenceBarFillHeight = Mth.lerp(differenceBarFillDelta, 4, 196 - 3);
        float differenceAlpha = 0.7f - 0.3f * (float) Math.sin(level.getGameTime() * 0.25);

        //Outside frame
        RenderBlitUtil.blit(VERTICAL_BAR_OUTSIDE, pose, 0, (float) -196 / 2, 0, 0, 0, totalAlpha, 25, 0,
                25, 196, -25, 196);
        //Red background
        RenderBlitUtil.blit(VERTICAL_BAR_INSIDE, pose, 0, (float) -196 / 2, (float) 128 / 256, 0, 0, totalAlpha, 25, 0,
                25, 196, -25, 196);

        if (targetTicker > -1 && targetTicker < 6) {
            //Target bar fill
            RenderBlitUtil.blit(VERTICAL_BAR_INSIDE, pose, 0, (float) -196 / 2 + (196 - targetBarFillHeight), 1, 1, 1, totalAlpha,
                    25, 196 - targetBarFillHeight, 25, targetBarFillHeight, -25, 196);
        }

        if (difference > 0) {
            //Difference bar fill
            RenderBlitUtil.blit(VERTICAL_BAR_INSIDE, pose, 0, (float) -196 / 2 + (196 - differenceBarFillHeight), 1, 1, 1,
                    Mth.clamp(differenceAlpha * totalAlpha, 0, 1), 25, 196 - differenceBarFillHeight, 25, differenceBarFillHeight,
                    -25, 196);
        }

        //Bar fill
        RenderBlitUtil.blit(VERTICAL_BAR_INSIDE, pose, 0, (float) -196 / 2 + (196 - barFillHeight), 1, 0, 0, totalAlpha,
                25, 196 - barFillHeight, 25, barFillHeight, -25, 196);
        //Bar marker
        RenderBlitUtil.blit(VERTICAL_BAR_INSIDE, pose, 0, (float) -196 / 2 + (196 - barFillHeight), 1, 1, 1, totalAlpha,
                25, 196 - barFillHeight, 25, 2, -25, 196);

        if (difference < 0) {
            //Negative difference bar fill
            RenderBlitUtil.blit(VERTICAL_BAR_INSIDE, pose, 0, (float) -196 / 2 + (196 - barFillHeight), 1, 1, 1,
                    Mth.clamp(differenceAlpha * totalAlpha, 0, 1), 25, 196 - barFillHeight, 25,
                    differenceBarFillHeight, -25, 196);
        }

        //DT label
        RenderBlitUtil.blit(VERTICAL_BAR_DT_LABEL, pose, 27, -65, 1, 1, 1, totalAlpha, 0, 0,
                22, 36, 22, 36);

        pose.pushPose();

        pose.scale(2f, 2f, 1f);

        int determinationDisplay;
        if (targetTicker > -1 && targetTicker < 6) {
            float displayDelta = Mth.clamp(targetTicker / 6f, 0, 1);
            determinationDisplay = (int) Mth.lerp(displayDelta, oldDetermination, determination);
        } else {
            determinationDisplay = determination;
        }

        //Determination number
        drawStringOutlined(guiGraphics, Component.literal(determinationDisplay + "").withStyle(Style.EMPTY.withFont(
                ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "8_bit_operator"))), 14, -10, 0xFFFFFF, 0x000000, 1f);

        //Percentage symbol
        drawStringOutlined(guiGraphics, Component.literal("%").withStyle(Style.EMPTY.withFont(
                ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "8_bit_operator"))), 18, 0, 0xFFFFFF, 0x000000, 1f);

        pose.popPose();

        pose.popPose();
    });

    public static void drawString(GuiGraphics graphics, Component lineString, int x, int y, int color, float alpha) {
        RenderSystem.setShaderColor(1f ,1f, 1f, alpha);

        graphics.drawString(Minecraft.getInstance().font, lineString, x, y, color, false);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawStringOutlined(GuiGraphics graphics, Component text, int x, int y, int fillHex, int outlineHex, float alpha) {
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence charSequence = text.getVisualOrderText();

        int finalAlpha = Mth.clamp((int) (alpha * 255f), 0, 255);
        int fillColor = (finalAlpha << 24) | (fillHex & 0xFFFFFF);
        int outlineColor = (finalAlpha << 24) | (outlineHex & 0xFFFFFF);

        font.drawInBatch8xOutline(charSequence, x, y, fillColor, outlineColor, graphics.pose().last().pose(), graphics.bufferSource(), 15728880);
        graphics.flush();
    }
}
