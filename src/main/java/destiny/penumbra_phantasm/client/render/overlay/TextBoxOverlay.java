package destiny.penumbra_phantasm.client.render.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.textbox.DarkWorldDialogue;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxWriter;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

public class TextBoxOverlay {
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/gui/dark_world/text_box.png");
    public static final ResourceLocation TEXTURE_GLOW = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/gui/dark_world/text_box_glow.png");

    public static final int TEXTURE_SIZE = 512;
    public static final int BOX_WIDTH = 296;
    public static final int BOX_HEIGHT = 83;
    public static final int BOX_SIZE = 1;
    public static final int GLOW_PERIOD_MS = 5000;

    public static final int SOUL_SIZE = 8;
    public static final int CHOICE_SELECTED_COLOR = 0xFFFF00;

    public static final int HORIZONTAL_SPACE = 6;
    public static final int VERTICAL_SPACE = 18;
    public static final int TEXT_ORIGIN_X = 17;
    public static final int TEXT_ORIGIN_Y = 14;
    public static final int CHOICE_SOUL_GAP = 4;

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        Level level = player.level();

        if (!DarkWorldUtil.isDarkWorld(level)) return;

        Minecraft minecraft = Minecraft.getInstance();

        if (DarkWorldDialogue.isActive() && DarkWorldDialogue.writer() != null) {
            int boxWidth = BOX_WIDTH * BOX_SIZE;
            int boxHeight = BOX_HEIGHT * BOX_SIZE;
            int originX = (width - boxWidth) / 2;
            int originY = height - boxHeight;

            PoseStack pose = guiGraphics.pose();
            TextBoxWriter writer = DarkWorldDialogue.writer();

            pose.pushPose();
            pose.translate(originX, originY, 0);
            pose.scale(BOX_SIZE, BOX_SIZE, 1);

            guiGraphics.blit(TEXTURE, 0, 0, 0, 0, BOX_WIDTH, BOX_HEIGHT,
                    TEXTURE_SIZE, TEXTURE_SIZE);

            float glowDelta = (float) (Util.getMillis() % GLOW_PERIOD_MS) / GLOW_PERIOD_MS;
            float jewelAlpha = 1f - Mth.sin(glowDelta * Mth.PI);

            if (DarkWorldUtil.isDepths(player.level())) {
                jewelAlpha = 0;
            }

            RenderBlitUtil.blit(TEXTURE_GLOW, pose, 0, 0, 1f, 1f, 1f, jewelAlpha, 0, 0, BOX_WIDTH, BOX_HEIGHT,
                    TEXTURE_SIZE, TEXTURE_SIZE);

            Font font = minecraft.font;
            List<String> lines = writer.visibleLines();
            for (int i = 0; i < lines.size(); i++) {
                drawGridLine(guiGraphics, font, lines.get(i), TEXT_ORIGIN_X, TEXT_ORIGIN_Y + i * VERTICAL_SPACE);
            }

            if (writer.isChoosing()) {
                drawChoices(guiGraphics, font, writer);
            }

            pose.popPose();
        }
    });

    private static void drawGridLine(GuiGraphics graphics, Font font, String text, int x, int y) {
        drawGridLine(graphics, font, text, x, y, 0xFFFFFFFF);
    }

    private static void drawGridLine(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        Style style = TextBoxWriter.FONT_STYLE;
        int cursor = x;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            Component component = Component.literal(ch).withStyle(style);

            graphics.drawString(font, component, cursor + 1, y + 1, 0xFF111133, false);
            graphics.drawString(font, component, cursor, y, color, false);

            cursor += HORIZONTAL_SPACE;
        }
    }

    private static void drawChoices(GuiGraphics graphics, Font font, TextBoxWriter writer) {
        int yesW = writer.yesLabel().getString().length() * HORIZONTAL_SPACE;
        int noW = writer.noLabel().getString().length() * HORIZONTAL_SPACE;
        int yesX = BOX_WIDTH / 4 - yesW / 2;
        int noX = BOX_WIDTH * 3 / 4 - noW / 2;
        int y = (BOX_HEIGHT - 8) / 2;
        int choice = writer.choiceIndex();
        int yesColor = choice == 0 ? CHOICE_SELECTED_COLOR : 0xFFFFFFFF;
        int noColor = choice == 1 ? CHOICE_SELECTED_COLOR : 0xFFFFFFFF;

        drawGridLine(graphics, font, writer.yesLabel().getString(), yesX, y, yesColor);
        drawGridLine(graphics, font, writer.noLabel().getString(), noX, y, noColor);

        int soulSize = SOUL_SIZE;
        int soulX;
        int soulY = (BOX_HEIGHT - soulSize) / 2;

        if (choice < 0) {
            soulX = BOX_WIDTH / 2 - soulSize / 2;
        } else {
            int labelX = choice == 0 ? yesX : noX;
            soulX = labelX - soulSize - CHOICE_SOUL_GAP;
        }

        int soulType = 1;
        if (Minecraft.getInstance().player != null) {
            soulType = Minecraft.getInstance().player.getCapability(CapabilityRegistry.SOUL)
                    .map(cap -> Mth.clamp(cap.soulType, 1, 7)).orElse(1);
        }

        ResourceLocation soul = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");
        graphics.blit(soul, soulX, soulY, soulSize, soulSize, 0, 0, 15, 15, 15, 15);
    }
}
