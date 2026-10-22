package destiny.penumbra_phantasm.client.render.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.textbox.DarkWorldDialogue;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxWriter;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
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
    public static final int BOX_SIZE = 2;
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
                //drawGridLine(guiGraphics, font, lines.get(i), TEXT_ORIGIN_X, TEXT_ORIGIN_Y + i * VERTICAL_SPACE);
            }

            if (writer.isChoosing()) {
                //drawChoices(guiGraphics, font, writer);
            }

            pose.popPose();
        }
    });
}
