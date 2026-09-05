package destiny.penumbra_phantasm.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
	public static final String CATEGORY = "key.categories.penumbra_phantasm";

	public static KeyMapping CONFIRM;
	public static KeyMapping CONFIRM_ALT;
	public static KeyMapping CANCEL;
	public static KeyMapping CANCEL_ALT;

	private KeyBindings() {}

	public static void register(RegisterKeyMappingsEvent event) {
		CONFIRM = new KeyMapping("key.penumbra_phantasm.confirm", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);
		CONFIRM_ALT = new KeyMapping("key.penumbra_phantasm.confirm_alt", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_ENTER, CATEGORY);
		CANCEL = new KeyMapping("key.penumbra_phantasm.cancel", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);
		CANCEL_ALT = new KeyMapping("key.penumbra_phantasm.cancel_alt", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, CATEGORY);
		event.register(CONFIRM);
		event.register(CONFIRM_ALT);
		event.register(CANCEL);
		event.register(CANCEL_ALT);
	}

	public static boolean isDialogueKey(int keyCode) {
		return matches(CONFIRM, keyCode) || matches(CONFIRM_ALT, keyCode) || matches(CANCEL, keyCode) || matches(CANCEL_ALT, keyCode);
	}

	public static boolean isConfirmKey(int keyCode) {
		return matches(CONFIRM, keyCode) || matches(CONFIRM_ALT, keyCode);
	}

	public static boolean isCancelKey(int keyCode) {
		return matches(CANCEL, keyCode) || matches(CANCEL_ALT, keyCode);
	}

	private static boolean matches(KeyMapping mapping, int keyCode) {
		return mapping != null && mapping.getKey().getValue() == keyCode;
	}
}
