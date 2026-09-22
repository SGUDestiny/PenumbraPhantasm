package destiny.penumbra_phantasm.client.render.textbox;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class TextBoxWriter {
	public static final Style FONT_STYLE = Style.EMPTY.withFont(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "8_bit_operator"));

	private final List<Page> pages = new ArrayList<>();
	private int pageIndex;
	private int revealed;
	private float typeAccum;
	private float waitRemaining;
	private int choiceBlipsLeft;
	private boolean skippable = true;
	private boolean choosing;
	private int choiceIndex = -1;
	private boolean closed;
	private final boolean hasChoices;
	private final String scriptId;
	private final Component yesLabel;
	private final Component noLabel;
	private final float charsPerTick;
	private final Runnable onTextBoxClose;

	public TextBoxWriter(TextBoxScript script) {
		this.hasChoices = script.hasChoices;
		this.scriptId = script.id;
		this.yesLabel = script.yesLabel.copy().withStyle(FONT_STYLE);
		this.noLabel = script.noLabel.copy().withStyle(FONT_STYLE);
		this.charsPerTick = script.charsPerTick;
		this.onTextBoxClose = script.onTextBoxClose;

		paginate(script);
	}

	public String scriptId() {
		return scriptId;
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isChoosing() {
		return choosing && !closed;
	}

	public int choiceIndex() {
		return choiceIndex;
	}

	public Component yesLabel() {
		return yesLabel;
	}

	public Component noLabel() {
		return noLabel;
	}

	public List<String> visibleLines() {
		if (choosing || pages.isEmpty() || closed) {
			return List.of();
		}

		Page page = pages.get(pageIndex);
		int remaining = revealed;
		List<String> out = new ArrayList<>();
		for (DisplayLine line : page.lines) {
			if (remaining <= 0) {
				break;
			}

			int take = Math.min(remaining, line.text.length());
			out.add(line.text.substring(0, take));
			remaining -= take;
		}

		return out;
	}

	public void tick() {
		if (closed) {
			return;
		}

		if (choosing) {
			tickChoiceBlips();
			return;
		}

		if (pages.isEmpty()) {
			return;
		}

		Page page = pages.get(pageIndex);
		if (waitRemaining > 0f) {
			waitRemaining = Math.max(0f, waitRemaining - charsPerTick);
			return;
		}

		if (revealed >= page.length) {
			return;
		}

		typeAccum += charsPerTick;

		while (waitRemaining <= 0f && typeAccum >= 1f && revealed < page.length) {
			typeAccum -= 1f;
			revealOne(page, true);
		}
	}

	public void confirm() {
		if (closed) {
			return;
		}

		if (choosing) {
			return;
		}

		if (waitRemaining > 0f) {
			return;
		}

		if (pages.isEmpty()) {
			closed = true;
			return;
		}

		Page page = pages.get(pageIndex);
		if (revealed < page.length) {
			if (skippable) {
				while (revealed < page.length) {
					revealOne(page, false);
				}

				waitRemaining = 0f;
				typeAccum = 0f;
			}

			return;
		}

		if (isLastPage()) {
			if (hasChoices) {
				beginChoosing();
			} else {
				closed = true;
			}

			return;
		}

		pageIndex++;
		revealed = 0;
		typeAccum = 0f;
		waitRemaining = 0f;
		tick();

		fireLineStarts(pages.get(pageIndex), 0);
	}

	public void cancel() {
		if (closed || choosing || pages.isEmpty()) {
			return;
		}

		Page page = pages.get(pageIndex);
		if ((revealed < page.length || waitRemaining > 0f) && skippable) {
			while (revealed < page.length) {
				revealOne(page, false);
			}

			waitRemaining = 0f;
			typeAccum = 0f;
		}
	}

	public boolean selectChoice() {
		return choosing && !closed && choiceIndex >= 0;
	}

	public void cycleChoice(int delta) {
		if (!choosing || delta == 0) {
			return;
		}

		if (choiceIndex < 0) {
			choiceIndex = delta < 0 ? 0 : 1;
			return;
		}

		choiceIndex = Mth.clamp(choiceIndex + delta, 0, 1);
	}

	public void close() {
		closed = true;
		choosing = false;
	}

	public void runOnTextBoxClose() {
		if (onTextBoxClose != null) {
			onTextBoxClose.run();
		}
	}


	private void beginChoosing() {
		choosing = true;
		choiceIndex = -1;
		choiceBlipsLeft = TextBoxConstants.CHOICE_BLIPS;
		typeAccum = 0f;
		waitRemaining = 0f;
	}

	private void tickChoiceBlips() {
		if (choiceBlipsLeft <= 0) {
			return;
		}

		typeAccum += charsPerTick;

		while (typeAccum >= 1f && choiceBlipsLeft > 0) {
			typeAccum -= 1f;
			playBlip();
			choiceBlipsLeft--;
		}
	}

	private boolean isLastPage() {
		return pageIndex >= pages.size() - 1;
	}

	private void revealOne(Page page, boolean sound) {
		int before = revealed;
		char ch = page.charAt(revealed);
		revealed++;

		if (sound && !Character.isWhitespace(ch)) {
			playBlip();
		}

		waitRemaining = page.waitAfter[before];
		fireLineStarts(page, before);
	}

	private static void playBlip() {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null) {
			player.level().playSound(player, player.blockPosition(), SoundRegistry.TEXTBOX_GENERIC.get(), SoundSource.PLAYERS, 1f, 1f);
		}
	}

	private void fireLineStarts(Page page, int previousRevealed) {
		int cursor = 0;
		for (DisplayLine line : page.lines) {
			if (previousRevealed <= cursor && revealed > cursor && line.onBegin != null) {
				line.onBegin.run();
			}

			cursor += line.text.length();
		}
	}

	private void paginate(TextBoxScript script) {
		for (TextBoxLine scripted : script.lines) {
			List<String> wrapped = wrap(scripted.text.getString(), true);
			List<DisplayLine> current = new ArrayList<>();
			boolean first = true;

			for (String row : wrapped) {
				if (current.size() >= TextBoxConstants.MAX_LINES) {
					pages.add(new Page(List.copyOf(current), '\0', 0f));
					current.clear();
				}

				current.add(new DisplayLine(row, first ? scripted.onBegin : null));
				first = false;
			}

			if (!current.isEmpty()) {
				pages.add(new Page(List.copyOf(current), scripted.waitAfterChar, scripted.waitTicks));
			}
		}

		if (pages.isEmpty()) {
			pages.add(new Page(List.of(), '\0', 0f));
		}

        fireLineStarts(pages.get(0), -1);
    }

	static List<String> wrap(String raw, boolean autoAster) {
		String text = raw == null ? "" : raw;

		if (autoAster && !text.startsWith("*")) {
			text = "* " + text;
		}

		StringBuilder mystring = new StringBuilder(text);
		int charpos = 0;
		int remspace = -1;
		boolean aster = false;

		for (int i = 0; i < mystring.length(); i++) {
			char c = mystring.charAt(i);

			if (c == '\n') {
				charpos = 0;
				remspace = -1;

				if (aster && autoAster && i + 1 < mystring.length() && mystring.charAt(i + 1) != '*') {
					mystring.insert(i + 1, "  ");
					i += 2;
					charpos = 2;
				}

				continue;
			}

			if (c == ' ') {
				remspace = i;
			}

			if (c == '*') {
				aster = true;
			}

			if (charpos >= TextBoxConstants.CHARLINE) {
				if (remspace > 2) {
					mystring.setCharAt(remspace, '\n');

					i = remspace;
					remspace = -1;
					charpos = 0;

					if (aster && autoAster && i + 1 < mystring.length() && mystring.charAt(i + 1) != '*') {
						mystring.insert(i + 1, "  ");
						i += 2;
						charpos = 2;
					}

				} else {
					mystring.insert(i, '\n');
					remspace = -1;
					charpos = 0;
				}

			} else {
				charpos++;
			}
		}

		List<String> rows = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < mystring.length(); i++) {
			if (mystring.charAt(i) == '\n') {
				rows.add(mystring.substring(start, i));
				start = i + 1;
			}
		}

		rows.add(mystring.substring(start));
		return rows;
	}

	private record DisplayLine(String text, Runnable onBegin) {
	}

	private static final class Page {
		final List<DisplayLine> lines;
		final int length;
		final float[] waitAfter;

		Page(List<DisplayLine> lines, char waitAfterChar, float waitUnits) {
			this.lines = lines;
			int total = 0;

			for (DisplayLine line : lines) {
				total += line.text.length();
			}

			this.length = total;
			this.waitAfter = new float[Math.max(total, 1)];

			if (waitAfterChar != '\0' && waitUnits > 0f) {
				int index = 0;
				for (DisplayLine line : lines) {
					int at = line.text.indexOf(waitAfterChar);

					if (at >= 0) {
						waitAfter[index + at] = waitUnits;
						break;
					}

					index += line.text.length();
				}
			}
		}

		char charAt(int index) {
			int cursor = 0;
			for (DisplayLine line : lines) {
				if (index < cursor + line.text.length()) {
					return line.text.charAt(index - cursor);
				}

				cursor += line.text.length();
			}

			return ' ';
		}
	}
}
