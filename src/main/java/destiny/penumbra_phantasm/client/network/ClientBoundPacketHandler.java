package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.render.screen.DEVICE_INTRO_SCREEN;
import destiny.penumbra_phantasm.client.render.textbox.DarkWorldDialogue;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxConstants;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxScript;
import destiny.penumbra_phantasm.client.render.screen.DarknessFallScreen;
import destiny.penumbra_phantasm.client.render.screen.EggRoomCoverScreen;
import destiny.penumbra_phantasm.client.render.screen.FireDoorScreen;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.FireDoor;
import destiny.penumbra_phantasm.server.network.ServerBoundFireDoorPacket;
import destiny.penumbra_phantasm.server.network.ServerBoundIntroPacket;
import destiny.penumbra_phantasm.server.network.ServerBoundSealingSoulPacket;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

public class ClientBoundPacketHandler
{
	public static void openIntroScreen(BlockPos pos, ResourceKey<Level> dim)
	{
		Minecraft minecraft = Minecraft.getInstance();

		minecraft.setScreen(new DEVICE_INTRO_SCREEN(() -> {
			minecraft.setScreen(null);
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundIntroPacket(pos, dim));
		}));
	}

	public static void openFireDoorScreen(List<FireDoor> fireDoors, ResourceKey<Level> originDarkWorld, BlockPos originPos) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new FireDoorScreen(fireDoors, originDarkWorld, originPos, chosenDoor -> {
			minecraft.setScreen(null);
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundFireDoorPacket(chosenDoor.darkWorld(), chosenDoor.doorPos(), chosenDoor.facingAngle(),
					originDarkWorld, originPos));
		}));
	}

	public static void openDarknessFallScreen(BlockPos destinationPos, double spawnX, double spawnY, double spawnZ, float spawnYaw, ResourceKey<Level> dim,
			boolean narrowGreatDoorPrepare, BlockPos arrivalGreatDoorAnchor) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new DarknessFallScreen(() -> minecraft.setScreen(null), destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dim,
				narrowGreatDoorPrepare, arrivalGreatDoorAnchor));
	}

	public static void openEggRoomCover(ResourceKey<Level> dim, int chunkX, int chunkZ) {
		EggRoomCoverScreen.open(dim, chunkX, chunkZ);
	}

	public static void syncSoulBreak(boolean diedWithSoulHearth, int soulType)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if(player != null)
			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap ->
				{
					cap.diedWithSoulHearth = diedWithSoulHearth;
					cap.soulType = soulType;
				});
	}

	public static void syncSoulStuff(boolean seenIntro, boolean diedWithSoulHearth, int soulType, int determination, int connectionLevel, int eggRoomManGone, int eggObtained) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if(player != null) {
			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> {
				cap.seenIntro = seenIntro;
				cap.diedWithSoulHearth = diedWithSoulHearth;
				cap.soulType = soulType;
				cap.determination = determination;
				cap.connectionLevel = connectionLevel;
				cap.eggRoomManGone = eggRoomManGone;
				cap.eggObtained = eggObtained;
			});
		}
	}

	public static void openTextBox(String scriptId) {
		if (DarkWorldDialogue.isActive()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null || !DarkWorldUtil.isDarkWorld(minecraft.player.level())) {
			return;
		}

		DarkWorldDialogue.start(createTextBoxScript(scriptId));
	}

	private static TextBoxScript createTextBoxScript(String id) {
		TextBoxScript textBox = new TextBoxScript().id(id);

		switch (id) {
			//Egg rooms
			case ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_FRONT ->
					textBox.line(Component.translatable("textbox.penumbra_phantasm.card_kingdom_egg_room.tree_front"));
			case ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_FRONT_GONE ->
					textBox.line(Component.translatable("textbox.penumbra_phantasm.card_kingdom_egg_room.tree_front.gone"));
			case ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_BEHIND -> textBox
					.speed(TextBoxConstants.CHARS_PER_TICK_FAST)
					.line(Component.translatable("textbox.penumbra_phantasm.card_kingdom_egg_room.tree_behind.1"))
					.waitAfter(',', 12)
					.line(Component.translatable("textbox.penumbra_phantasm.card_kingdom_egg_room.tree_behind.2"))
					.choices();
			case ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_BEHIND_GONE -> textBox
					.speed(TextBoxConstants.CHARS_PER_TICK_FAST)
					.line(Component.translatable("textbox.penumbra_phantasm.card_kingdom_egg_room.tree_behind.gone"))
					.waitAfter(',', 12);
			case ClientBoundTextBoxPacket.EGG_ROOM_RECEIVED_EGG ->
					textBox.line(Component.translatable("textbox.penumbra_phantasm.egg_room.received_egg"), ClientBoundPacketHandler::playEggAcquire, null);
			case ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_CHOICE_DENY ->
					textBox.line(Component.translatable("textbox.penumbra_phantasm.card_kingdom_egg_room.choice_deny"));
			case ClientBoundTextBoxPacket.USE_EGG ->
					textBox.line(Component.translatable("textbox.penumbra_phantasm.use.egg"), ClientBoundPacketHandler::playEggAcquire, null);

			//Knife uses
			case ClientBoundTextBoxPacket.MAKING_FOUNTAIN_INSIDE_DARK_WORLD ->
					textBox.line(Component.translatable("textbox.penumbra_phantasm.making_fountain.inside_dark_world"));
			case ClientBoundTextBoxPacket.MAKING_FOUNTAIN_INSIDE_DEPTHS ->
					textBox.line(Component.translatable("textbox.penumbra_phantasm.making_fountain.inside_depths"));

			//Soul hearth
			case ClientBoundTextBoxPacket.SOUL_HEARTH_REJECT -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.reject"));
			case ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_DEPTHS -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.sealing_fountain.depths"));
			case ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_NOT_ENOUGH_DETERMINATION -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.sealing_fountain.not_enough_determination"));
			case ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_ALREADY_BEING_SEALED -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.sealing_fountain.already_being_sealed"));
			case ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.sealing_fountain"))
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.sealing_fountain.choice"))
					.choices();
			case ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE_DENY -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.sealing_fountain.choice.deny"))
					.waitAfter(',', 20);
			case ClientBoundTextBoxPacket.SOUL_HEARTH_SEALING_FOUNTAIN_CHOICE_CONFIRM -> textBox
					.speed(0.5f)
					.line(Component.translatable("textbox.penumbra_phantasm.soul_hearth.sealing_fountain.choice.confirm"))
					.onTextBoxClose(ClientBoundPacketHandler::spawnSealingSoulEntity);

			//Dark fountains
			case ClientBoundTextBoxPacket.DARK_FOUNTAIN_PUSH_AWAY -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.dark_fountain.push_away"));

			//Great doors
			case ClientBoundTextBoxPacket.GREAT_DOOR_NO_LIGHT_DOOR -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.great_door.no_light_door"));
			case ClientBoundTextBoxPacket.GREAT_DOOR_NO_FOUNTAIN -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.great_door.no_fountain"));
			case ClientBoundTextBoxPacket.GREAT_DOOR_NO_FOUNTAIN_DESTINATION -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.great_door.no_fountain_destination"));
			case ClientBoundTextBoxPacket.GREAT_DOOR_NOT_FOUNTAIN_DOOR -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.great_door.not_fountain_door"));

			//Great compass
			case ClientBoundTextBoxPacket.GREAT_COMPASS_BOUND -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.great_compass.bound"));
			case ClientBoundTextBoxPacket.GREAT_COMPASS_UNBOUND -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.great_compass.unbound"));

			//Fire doors
			case ClientBoundTextBoxPacket.FIRE_DOOR_LINK -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.fire_door.link"));
			case ClientBoundTextBoxPacket.FIRE_DOOR_UNLINK -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.fire_door.unlink"));
			case ClientBoundTextBoxPacket.FIRE_DOOR_LIMIT_REACHED -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.fire_door.limit_reached"));
			case ClientBoundTextBoxPacket.FIRE_DOOR_NOT_ENOUGH_DOORS -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.fire_door.not_enough_doors"));
			case ClientBoundTextBoxPacket.FIRE_DOOR_DEPTHS -> textBox
					.line(Component.translatable("textbox.penumbra_phantasm.fire_door.depths"));

			default -> textBox.line(Component.literal(id));
		}

		return textBox;
	}

	private static void playEggAcquire() {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null) {
			player.level().playSound(player, player.blockPosition(), SoundRegistry.EGG_ACQUIRE.get(), SoundSource.PLAYERS, 1f, 1f);
		}
	}

	private static void spawnSealingSoulEntity() {
		LocalPlayer player = Minecraft.getInstance().player;
		Level level = player.level();

		SoulCapability soulCapability = null;
		LazyOptional<SoulCapability> soulCapabilityLazy = player.getCapability(CapabilityRegistry.SOUL);
		if(soulCapabilityLazy.isPresent() && soulCapabilityLazy.resolve().isPresent())
			soulCapability = soulCapabilityLazy.resolve().get();

		if (soulCapability == null){
			return;
		}

		DarkFountainCapability darkFountainCapability = null;
		LazyOptional<DarkFountainCapability> darkLazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
		if(darkLazyCapability.isPresent() && darkLazyCapability.resolve().isPresent())
			darkFountainCapability = darkLazyCapability.resolve().get();

		if (darkFountainCapability == null){
			return;
		}

		DarkFountain darkFountain = null;
		for(Map.Entry<BlockPos, DarkFountain> entry : darkFountainCapability.darkFountains.entrySet()) {
			DarkFountain entryFountain = entry.getValue();

			if(entryFountain.openingTick > 125 || entryFountain.openingTick == -1) {
				BlockPos fountainPos = entry.getValue().getFountainPos();
				Vec3 fountainPos2d = new Vec3(fountainPos.getX(), 0, fountainPos.getZ());
				Vec3 playerPos2d = new Vec3(player.getX(), 0, player.getZ());

				if (fountainPos2d.distanceTo(playerPos2d) < 16) {
					darkFountain = entry.getValue();
					break;
				}
			}
		}

		if (darkFountain == null){
			return;
		}

		int soulType = soulCapability.soulType;
		float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
		Vec3 playerPos = player.position();
		double forwardX = -Mth.sin(yawRad);
		double forwardZ = Mth.cos(yawRad);
		double x = playerPos.x + forwardX;
		double y = playerPos.y + 1;
		double z = playerPos.z + (forwardZ * 2);

        PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundSealingSoulPacket(level.dimension(), soulType, x, y, z));
    }

	public static void sendParticle(ResourceLocation particleId, double x, double y, double z, double vx, double vy, double vz, int count) {
		Level level = Minecraft.getInstance().level;

		if (level == null) return;

		ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(particleId);

		if (!(type instanceof SimpleParticleType simpleType)) return;

		for (int i = 0; i < count; i++) {
			level.addParticle(simpleType, x, y, z, vx, vy, vz);
		}
	}

	@SuppressWarnings("unchecked")
	public static void playPlayerAnimation(int entityId, ResourceLocation animationId) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;

		Entity entity = level.getEntity(entityId);
		if (!(entity instanceof AbstractClientPlayer player)) return;

		ModifierLayer<IAnimation> animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess
				.getPlayerAssociatedData(player)
				.get(animationId);

		if (animation != null) {
			animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(animationId)));
		}
	}

	@SuppressWarnings("unchecked")
	public static void cancelPlayerAnimation(int entityId, ResourceLocation animationId) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;

		Entity entity = level.getEntity(entityId);
		if (!(entity instanceof AbstractClientPlayer player)) return;

		ModifierLayer<IAnimation> animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess
				.getPlayerAssociatedData(player)
				.get(animationId);

		if (animation != null) {
			animation.setAnimation(null);
		}
	}
}
