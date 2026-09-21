package destiny.penumbra_phantasm.client.render.textbox;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
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

import java.util.List;

public final class DarkWorldTextBox {
	private DarkWorldTextBox() {}

	public static void render(Minecraft minecraft, GuiGraphics graphics, TextBoxWriter writer, int screenWidth, int screenHeight) {
		int boxWidth = TextBoxConstants.BOX_WIDTH * TextBoxConstants.BOX_SIZE;
		int boxHeight = TextBoxConstants.BOX_HEIGHT * TextBoxConstants.BOX_SIZE;
		int originX = (screenWidth - boxWidth) / 2;
		int originY = screenHeight - boxHeight;

		PoseStack pose = graphics.pose();

		pose.pushPose();
		pose.translate(originX, originY, 0);
		pose.scale(TextBoxConstants.BOX_SIZE, TextBoxConstants.BOX_SIZE, 1);

		RenderSystem.enableBlend();

		graphics.blit(TextBoxConstants.TEXTURE, 0, 0, 0, 0, TextBoxConstants.BOX_WIDTH, TextBoxConstants.BOX_HEIGHT,
				TextBoxConstants.TEXTURE_SIZE, TextBoxConstants.TEXTURE_SIZE);

		float glowDelta = (float) (Util.getMillis() % TextBoxConstants.GLOW_PERIOD_MS) / TextBoxConstants.GLOW_PERIOD_MS;
		float jewelAlpha = 1f - Mth.sin(glowDelta * Mth.PI);

		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null && DarkWorldUtil.isDepths(player.level())) {
			jewelAlpha = 0;
		}

		graphics.setColor(1f, 1f, 1f, jewelAlpha);

		blitJewel(graphics, 0, 0, false, false);
		blitJewel(graphics, TextBoxConstants.BOX_WIDTH, 0, true, false);
		blitJewel(graphics, 0, TextBoxConstants.BOX_HEIGHT, false, true);
		blitJewel(graphics, TextBoxConstants.BOX_WIDTH, TextBoxConstants.BOX_HEIGHT, true, true);

		graphics.setColor(1f, 1f, 1f, 1f);

		Font font = minecraft.font;
		List<String> lines = writer.visibleLines();
		for (int i = 0; i < lines.size(); i++) {
			drawGridLine(graphics, font, lines.get(i), TextBoxConstants.TEXT_ORIGIN_X, TextBoxConstants.TEXT_ORIGIN_Y + i * TextBoxConstants.VERTICAL_SPACE);
		}

		if (writer.isChoosing()) {
			drawChoices(graphics, font, writer);
		}

		RenderSystem.disableBlend();

		pose.popPose();
	}

	private static void blitJewel(GuiGraphics graphics, int x, int y, boolean flipX, boolean flipY) {
		int size = TextBoxConstants.JEWEL_SIZE;
		int destX = flipX ? x - size : x;
		int destY = flipY ? y - size : y;
		float uOffset = flipX ? size : 0;
		float vOffset = flipY ? TextBoxConstants.JEWEL_V + size : TextBoxConstants.JEWEL_V;
		int uWidth = flipX ? -size : size;
		int vHeight = flipY ? -size : size;

		graphics.blit(TextBoxConstants.TEXTURE, destX, destY, size, size, uOffset, vOffset, uWidth, vHeight,
				TextBoxConstants.TEXTURE_SIZE, TextBoxConstants.TEXTURE_SIZE);
	}

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

			cursor += TextBoxConstants.HORIZONTAL_SPACE;
		}
	}

	private static void drawChoices(GuiGraphics graphics, Font font, TextBoxWriter writer) {
		int yesW = writer.yesLabel().getString().length() * TextBoxConstants.HORIZONTAL_SPACE;
		int noW = writer.noLabel().getString().length() * TextBoxConstants.HORIZONTAL_SPACE;
		int yesX = TextBoxConstants.BOX_WIDTH / 4 - yesW / 2;
		int noX = TextBoxConstants.BOX_WIDTH * 3 / 4 - noW / 2;
		int y = (TextBoxConstants.BOX_HEIGHT - 8) / 2;
		int choice = writer.choiceIndex();
		int yesColor = choice == 0 ? TextBoxConstants.CHOICE_SELECTED_COLOR : 0xFFFFFFFF;
		int noColor = choice == 1 ? TextBoxConstants.CHOICE_SELECTED_COLOR : 0xFFFFFFFF;

		drawGridLine(graphics, font, writer.yesLabel().getString(), yesX, y, yesColor);
		drawGridLine(graphics, font, writer.noLabel().getString(), noX, y, noColor);

		int soulSize = TextBoxConstants.SOUL_SIZE;
		int soulX;
		int soulY = (TextBoxConstants.BOX_HEIGHT - soulSize) / 2;

		if (choice < 0) {
			soulX = TextBoxConstants.BOX_WIDTH / 2 - soulSize / 2;
		} else {
			int labelX = choice == 0 ? yesX : noX;
			soulX = labelX - soulSize - TextBoxConstants.CHOICE_SOUL_GAP;
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
