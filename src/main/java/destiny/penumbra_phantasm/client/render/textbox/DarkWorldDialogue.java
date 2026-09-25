package destiny.penumbra_phantasm.client.render.textbox;

import destiny.penumbra_phantasm.client.KeyBindings;
import destiny.penumbra_phantasm.server.network.ServerBoundTextBoxChoicePacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import org.lwjgl.glfw.GLFW;

public final class DarkWorldDialogue {
	private static TextBoxWriter writer;

	private DarkWorldDialogue() {
	}

	public static boolean isActive() {
		return writer != null && !writer.isClosed();
	}

	public static boolean isChoosing() {
		return writer != null && writer.isChoosing();
	}

	public static TextBoxWriter writer() {
		return writer;
	}

	public static void start(TextBoxScript script) {
		writer = new TextBoxWriter(script);
	}

	public static void stop() {
		writer = null;
	}

	public static void tick() {
		if (!isActive()) {
			return;
		}

		writer.tick();

		if (writer.isClosed()) {
			writer = null;
		}
	}

	public static void onConfirm() {
		if (!isActive()) {
			return;
		}

		if (writer.selectChoice()) {
			boolean yes = writer.choiceIndex() == 0;
			String id = writer.scriptId();
			writer.close();
			writer = null;
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundTextBoxChoicePacket(id, yes));
			return;
		}

		writer.confirm();

		if (writer.isClosed()) {
			writer.runOnTextBoxClose();
			writer = null;
		}
	}

	public static void onCancel() {
		if (!isActive()) {
			return;
		}

		writer.cancel();
	}

	public static void applyChoiceMovement(Input input) {
		if (!isActive() || writer == null || !writer.isChoosing()) {
			return;
		}

		if (input.left) {
			writer.cycleChoice(-1);
		}

		if (input.right) {
			writer.cycleChoice(1);
		}

		input.left = false;
		input.right = false;
		input.leftImpulse = 0f;
	}

	public static boolean handleKey(int key, int action) {
		if (!isActive()) {
			return false;
		}

		if (key == GLFW.GLFW_KEY_ESCAPE) {
			if (action == GLFW.GLFW_PRESS) {
				stop();
			}
			return false;
		}

		if (writer != null && writer.isChoosing() && isChoiceMoveKey(key)) {
			if (action == GLFW.GLFW_PRESS) {
				if (isChoiceLeftKey(key)) {
					writer.cycleChoice(-1);
				} else {
					writer.cycleChoice(1);
				}
			}
			return true;
		}

		if (action != GLFW.GLFW_PRESS) {
			return KeyBindings.isDialogueKey(key);
		}

		if (KeyBindings.isConfirmKey(key)) {
			onConfirm();
			return true;
		}

		if (KeyBindings.isCancelKey(key)) {
			onCancel();
			return true;
		}

		return KeyBindings.isDialogueKey(key);
	}

	private static boolean isChoiceMoveKey(int key) {
		return isChoiceLeftKey(key) || isChoiceRightKey(key);
	}

	private static boolean isChoiceLeftKey(int key) {
		if (key == GLFW.GLFW_KEY_LEFT) {
			return true;
		}

		return Minecraft.getInstance().options.keyLeft.getKey().getValue() == key;
	}

	private static boolean isChoiceRightKey(int key) {
		if (key == GLFW.GLFW_KEY_RIGHT) {
			return true;
		}

		return Minecraft.getInstance().options.keyRight.getKey().getValue() == key;
	}

	public static boolean shouldBlockSneak() {
		if (!isActive()) {
			return false;
		}

		return KeyBindings.isCancelKey(Minecraft.getInstance().options.keyShift.getKey().getValue())
				|| (KeyBindings.CANCEL_ALT != null && KeyBindings.CANCEL_ALT.getKey().getValue() == Minecraft.getInstance().options.keyShift.getKey().getValue())
				|| (KeyBindings.CANCEL != null && KeyBindings.CANCEL.getKey().getValue() == Minecraft.getInstance().options.keyShift.getKey().getValue());
	}
}
