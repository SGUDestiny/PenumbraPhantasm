package destiny.penumbra_phantasm.client.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundTextBoxPacket {
	//Knife uses
	public static final String MAKING_FOUNTAIN_INSIDE_DARK_WORLD = "making_fountain_inside_dark_world";
	public static final String MAKING_FOUNTAIN_INSIDE_DEPTHS = "making_fountain_inside_depths";

	//Soul hearth uses
	public static final String SOUL_HEARTH_REJECT = "soul_hearth_reject";
	public static final String SOUL_HEARTH_SEALING_FOUNTAIN_DEPTHS = "soul_hearth_sealing_fountain_depths";
	public static final String SOUL_HEARTH_SEALING_FOUNTAIN_NOT_ENOUGH_DETERMINATION = "soul_hearth_sealing_fountain_not_enough_determination";
	public static final String SOUL_HEARTH_SEALING_FOUNTAIN_ALREADY_BEING_SEALED = "soul_hearth_sealing_fountain_already_being_sealed";

	public static final String SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE = "soul_hearth_sealing_fountain_choice";
	public static final String SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE_DENY = "soul_hearth_sealing_fountain_choice_deny";
	public static final String SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE_CONFIRM = "soul_hearth_sealing_fountain_choice_confirm";

	//Dark fountain stuff
	public static final String DARK_FOUNTAIN_PUSH_AWAY = "dark_fountain_push_away";

	//Great door stuff
	public static final String GREAT_DOOR_NO_LIGHT_DOOR = "great_door_no_light_door";
	public static final String GREAT_DOOR_NO_FOUNTAIN = "great_door_no_fountain";
	public static final String GREAT_DOOR_NO_FOUNTAIN_DESTINATION = "great_door_no_fountain_destination";
	public static final String GREAT_DOOR_NOT_FOUNTAIN_DOOR = "great_door_not_fountain_door";

	//Great compass stuff
	public static final String GREAT_COMPASS_BOUND = "great_compass_bound";
	public static final String GREAT_COMPASS_UNBOUND = "great_compass_unbound";

	//Fire door stuff
	public static final String FIRE_DOOR_LINK = "fire_door_link";
	public static final String FIRE_DOOR_UNLINK = "fire_door_unlink";
	public static final String FIRE_DOOR_LIMIT_REACHED = "fire_door_limit_reached";
	public static final String FIRE_DOOR_NOT_ENOUGH_DOORS = "fire_door_not_enough_doors";
	public static final String FIRE_DOOR_DEPTHS = "fire_door_depths";

	//Egg rooms
	public static final String CARD_KINGDOM_EGG_ROOM_TREE_FRONT = "card_kingdom_egg_room_tree_front";
	public static final String CARD_KINGDOM_EGG_ROOM_TREE_FRONT_GONE = "card_kingdom_egg_room_tree_front_gone";
	public static final String CARD_KINGDOM_EGG_ROOM_TREE_BEHIND = "card_kingdom_egg_room_tree_behind";
	public static final String CARD_KINGDOM_EGG_ROOM_TREE_BEHIND_GONE = "card_kingdom_egg_room_tree_behind_gone";
	public static final String CARD_KINGDOM_EGG_ROOM_CHOICE_DENY = "card_kingdom_egg_room_choice_deny";
	public static final String EGG_ROOM_RECEIVED_EGG = "egg_room_received_egg";

	//Item uses
	public static final String USE_EGG = "use_egg";

	public final String scriptId;

	public ClientBoundTextBoxPacket(String scriptId) {
		this.scriptId = scriptId;
	}

	public ClientBoundTextBoxPacket(FriendlyByteBuf buf) {
		this.scriptId = buf.readUtf();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(scriptId);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientBoundPacketHandler.openTextBox(scriptId));
		return true;
	}
}
