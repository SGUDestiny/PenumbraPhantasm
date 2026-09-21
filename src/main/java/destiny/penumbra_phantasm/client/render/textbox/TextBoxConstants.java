package destiny.penumbra_phantasm.client.render.textbox;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.resources.ResourceLocation;

public final class TextBoxConstants {
	public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "textures/gui/dark_world/text_box.png");
	public static final ResourceLocation FONT = ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "8_bit_operator");

	public static final int TEXTURE_SIZE = 512;
	public static final int BOX_WIDTH = 296;
	public static final int BOX_HEIGHT = 83;
	public static final int BOX_SIZE = 2;
	public static final int JEWEL_V = 83;
	public static final int JEWEL_SIZE = 16;
	public static final int GLOW_PERIOD_MS = 5000;

	public static final int CHARLINE = 44;
	public static final int MAX_LINES = 3;
	public static final int HORIZONTAL_SPACE = 6;
	public static final int VERTICAL_SPACE = 18;
	public static final int TEXT_ORIGIN_X = 17;
	public static final int TEXT_ORIGIN_Y = 14;
	public static final int SOUL_SIZE = 8;
	public static final int CHOICE_SOUL_GAP = 4;
	public static final int CHOICE_SELECTED_COLOR = 0xFFFF00;

	public static final float CHARS_PER_TICK = 1.5f;
	public static final float CHARS_PER_TICK_FAST = 2.5f;
	public static final int CHOICE_BLIPS = 3;

	private TextBoxConstants() {}
}