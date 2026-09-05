package destiny.penumbra_phantasm.server.egg_room;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundEggRoomCoverPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundTextBoxPacket;
import destiny.penumbra_phantasm.server.block.ScarletLogMysteriousDoorBlock;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CardKingdomEggRoomManager {
	private static final ResourceLocation STRUCTURE_ID = new ResourceLocation(PenumbraPhantasm.MODID, "egg_room_card_kingdom");
	private static final List<PendingDoor> PENDING_DOORS = new ArrayList<>();
	private static final Map<UUID, Long> DOOR_LOCK_UNTIL = new HashMap<>();
	private static final Map<UUID, Long> INTERACT_TICK = new HashMap<>();
	private static final Map<UUID, Long> TRANSIT_UNTIL = new HashMap<>();
	private static final Map<UUID, TransitDest> TRANSIT_DEST = new HashMap<>();
	private static final Set<UUID> TRANSIT = new HashSet<>();
	private static final Set<UUID> RESYNC_SCHEDULED = new HashSet<>();
	private static final Set<UUID> LEFT_ENTRANCE = new HashSet<>();
	private static final Set<UUID> LEAVING = new HashSet<>();
	private static final long ARRIVAL_GRACE_MS = 8000;

	public static void ensurePlaced(ServerLevel eggLevel) {
		Data data = Data.get(eggLevel);

		if (data.placed) {
			return;
		}

		Optional<StructureTemplate> templateOpt = eggLevel.getStructureManager().get(STRUCTURE_ID);
		if (templateOpt.isEmpty()) {
			PenumbraPhantasm.LOGGER.error("Missing structure {}", STRUCTURE_ID);
			return;
		}

		StructureTemplate template = templateOpt.get();
		Vec3i size = template.getSize();
		BlockPos origin = new BlockPos(-size.getX() / 2, CardKingdomEggRoomUtil.PLACE_Y, -size.getZ() / 2);
		StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(true);
		forceChunks(eggLevel, origin.getX(), origin.getZ(), origin.getX() + size.getX() - 1, origin.getZ() + size.getZ() - 1);
		template.placeInWorld(eggLevel, origin, origin, settings, eggLevel.random, 2);

		BlockPos tree = null;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = origin.getX(); x < origin.getX() + size.getX(); x++) {
			for (int y = origin.getY(); y < origin.getY() + size.getY(); y++) {
				for (int z = origin.getZ(); z < origin.getZ() + size.getZ(); z++) {
					cursor.set(x, y, z);
					BlockState state = eggLevel.getBlockState(cursor);

					if (state.is(Blocks.BLACK_CONCRETE)) {
						eggLevel.setBlock(cursor, BlockRegistry.UNBREAKABLE_DARKNESS.get().defaultBlockState(), 2);
					} else if (state.is(BlockRegistry.SCARLET_LOG.get()) && state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y
							&& (tree == null || y < tree.getY() || (y == tree.getY() && (x < tree.getX() || (x == tree.getX() && z < tree.getZ()))))) {
						tree = cursor.immutable();
					}
				}
			}
		}

		data.placed = true;
		data.minX = origin.getX();
		data.minY = origin.getY();
		data.minZ = origin.getZ();
		data.maxX = origin.getX() + size.getX() - 1;
		data.maxY = origin.getY() + size.getY() - 1;
		data.maxZ = origin.getZ() + size.getZ() - 1;

		if (tree != null) {
			data.treeX = tree.getX();
			data.treeY = tree.getY();
			data.treeZ = tree.getZ();
		}

		data.setDirty();
		forceRoomChunks(eggLevel, data);
	}

	public static void enterFromDoor(Player player, Level originLevel, BlockPos doorLower) {
		if (!(player instanceof ServerPlayer serverPlayer) || originLevel.isClientSide) return;
		if (CardKingdomEggRoomUtil.isEggRoom(serverPlayer.level())) return;
		if (isDoorLocked(serverPlayer.getUUID()) || isPendingDoor(originLevel.dimension(), doorLower)) return;

		MinecraftServer server = serverPlayer.getServer();
		if (server == null) return;

		ServerLevel eggLevel = server.getLevel(CardKingdomEggRoomUtil.CARD_KINGDOM_EGG_ROOM);
		if (eggLevel == null) return;

		lockDoor(serverPlayer.getUUID(), 8000);
		ensurePlaced(eggLevel);
		forceRoomChunks(eggLevel, Data.get(eggLevel));
		ScarletLogMysteriousDoorBlock.setOpen(originLevel, doorLower, originLevel.getBlockState(doorLower), false);
		originLevel.playSound(null, doorLower, SoundEvents.CHERRY_WOOD_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1f);

		serverPlayer.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> {
			cap.eggReturnDim = originLevel.dimension().location().toString();
			cap.eggReturnX = serverPlayer.getX();
			cap.eggReturnY = serverPlayer.getY();
			cap.eggReturnZ = serverPlayer.getZ();
			cap.eggReturnYaw = serverPlayer.getYRot();
			cap.eggDoorX = doorLower.getX();
			cap.eggDoorY = doorLower.getY();
			cap.eggDoorZ = doorLower.getZ();
			cap.eggLeftEntrance = false;
		});

		Vec3 spawn = CardKingdomEggRoomUtil.spawnPos();
		eggLevel.getChunk(BlockPos.containing(spawn.x, spawn.y, spawn.z));

		beginTransit(serverPlayer, eggLevel.dimension(), true, spawn.x, spawn.y, spawn.z, CardKingdomEggRoomUtil.SPAWN_YAW);

		serverPlayer.teleportTo(eggLevel, spawn.x, spawn.y, spawn.z, CardKingdomEggRoomUtil.SPAWN_YAW, 0f);

		onChangedToEggRoom(serverPlayer);
	}

	public static void onChangedToEggRoom(ServerPlayer player) {
		if (!CardKingdomEggRoomUtil.isEggRoom(player.level())) return;

		prepareArrival(player);
	}

	public static void onClientReady(ServerPlayer player) {
		if (!TRANSIT.contains(player.getUUID())) return;

		endTransit(player);
	}

	public static void onLoggedOut(UUID id) {
		DOOR_LOCK_UNTIL.remove(id);
		INTERACT_TICK.remove(id);
		TRANSIT_UNTIL.remove(id);
		TRANSIT_DEST.remove(id);
		TRANSIT.remove(id);
		RESYNC_SCHEDULED.remove(id);
		LEFT_ENTRANCE.remove(id);
		LEAVING.remove(id);
	}

	private static void prepareArrival(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel eggLevel) || !CardKingdomEggRoomUtil.isEggRoom(eggLevel)) return;

		ensurePlaced(eggLevel);

		Data data = Data.get(eggLevel);
		forceRoomChunks(eggLevel, data);

		LEFT_ENTRANCE.remove(player.getUUID());
		player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> cap.eggLeftEntrance = false);

		Vec3 spawn = CardKingdomEggRoomUtil.spawnPos();

		beginTransit(player, eggLevel.dimension(), true, spawn.x, spawn.y, spawn.z, CardKingdomEggRoomUtil.SPAWN_YAW);
		scheduleResync(player, true, spawn.x, spawn.y, spawn.z, CardKingdomEggRoomUtil.SPAWN_YAW);
	}

	public static void leaveToOrigin(ServerPlayer player) {
		if (!LEAVING.add(player.getUUID())) return;

		leaveToOriginInner(player);
		LEAVING.remove(player.getUUID());
	}

	private static void leaveToOriginInner(ServerPlayer player) {
		MinecraftServer server = player.getServer();

		if (server == null) {
			return;
		}

		SoulCapability cap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
		ServerLevel dest = null;
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();
		float yaw = player.getYRot();
		BlockPos door = BlockPos.ZERO;

		if (cap.eggReturnDim != null && !cap.eggReturnDim.isEmpty()) {
			ResourceLocation loc = ResourceLocation.tryParse(cap.eggReturnDim);

			if (loc != null) {
				dest = server.getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, loc));
			}

			x = cap.eggReturnX;
			y = cap.eggReturnY;
			z = cap.eggReturnZ;
			yaw = cap.eggReturnYaw;
			door = new BlockPos(cap.eggDoorX, cap.eggDoorY, cap.eggDoorZ);
		}

		if (dest == null) {
			ResourceKey<Level> respawn = player.getRespawnDimension();
			dest = server.getLevel(respawn);
			BlockPos bed = player.getRespawnPosition();

			if (dest != null && bed != null) {
				x = bed.getX() + 0.5;
				y = bed.getY();
				z = bed.getZ() + 0.5;
			} else {
				dest = server.overworld();
				BlockPos spawn = dest.getSharedSpawnPos();
				x = spawn.getX() + 0.5;
				y = spawn.getY();
				z = spawn.getZ() + 0.5;
			}
		}

        lockDoor(player.getUUID(), 2500);

		if (door != BlockPos.ZERO && dest.getBlockState(door).getBlock() instanceof ScarletLogMysteriousDoorBlock) {
			BlockState doorState = dest.getBlockState(door);
			Direction facing = doorState.getValue(ScarletLogMysteriousDoorBlock.FACING);
			x = door.getX() + 0.5 + facing.getStepX() * 1.6;
			y = door.getY();
			z = door.getZ() + 0.5 + facing.getStepZ() * 1.6;
			yaw = facing.toYRot();

			ScarletLogMysteriousDoorBlock.setOpen(dest, door, doorState, true);
			dest.playSound(null, door, SoundEvents.CHERRY_WOOD_DOOR_OPEN, SoundSource.BLOCKS, 1f, 1f);
			PENDING_DOORS.add(new PendingDoor(dest.dimension(), door.immutable(), dest.getGameTime() + 20));
		}

		LEFT_ENTRANCE.remove(player.getUUID());

        cap.eggLeftEntrance = false;
        cap.eggReturnDim = "";

        dest.getChunk(BlockPos.containing(x, y, z));
		beginTransit(player, dest.dimension(), false, x, y, z, yaw);
		player.teleportTo(dest, x, y, z, yaw, 0f);
		scheduleResync(player, false, x, y, z, yaw);

		LEFT_ENTRANCE.remove(player.getUUID());
	}

	public static void tickPendingDoors(ServerLevel level) {
		Iterator<PendingDoor> iterator = PENDING_DOORS.iterator();

		while (iterator.hasNext()) {
			PendingDoor pending = iterator.next();

			if (!pending.dimension.equals(level.dimension())) {
				continue;
			}

			if (level.getGameTime() < pending.when) {
				continue;
			}

			iterator.remove();
			BlockState state = level.getBlockState(pending.door);
			if (state.getBlock() instanceof ScarletLogMysteriousDoorBlock) {
				ScarletLogMysteriousDoorBlock.setOpen(level, pending.door, state, false);

				level.playSound(null, pending.door, SoundEvents.CHERRY_WOOD_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1f);

				BlockPos lower = ScarletLogMysteriousDoorBlock.lowerPos(pending.door, state);
				BlockState log = BlockRegistry.SCARLET_LOG.get().defaultBlockState();

				level.setBlock(lower, log, 3);
				level.setBlock(lower.above(), log, 3);
			}
		}
	}

	public static void tickPlayer(ServerPlayer player) {
		tickTransit(player);

		if (!CardKingdomEggRoomUtil.isEggRoom(player.level())) {
			return;
		}

		if (!(player.level() instanceof ServerLevel eggLevel)) {
			return;
		}

		ensurePlaced(eggLevel);
		SoulCapability cap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);

        if (LEAVING.contains(player.getUUID()) || isInTransit(player.getUUID())) {
			return;
		}

		player.setNoGravity(false);
		player.setSprinting(false);
		player.fallDistance = 0f;
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();
		boolean fallen = y < CardKingdomEggRoomUtil.FLOOR_Y;
		if (fallen) {
			settleAtSpawn(player);
			return;
		}

		if (CardKingdomEggRoomUtil.northOfRoom(z) && LEFT_ENTRANCE.contains(player.getUUID())) {
			leaveToOrigin(player);
			return;
		}

		boolean inEntrance = CardKingdomEggRoomUtil.inEntranceZone(x, z);
		if (!inEntrance && z > CardKingdomEggRoomUtil.LEFT_ENTRANCE_Z) {
			LEFT_ENTRANCE.add(player.getUUID());
			cap.eggLeftEntrance = true;
		}

		if (LEFT_ENTRANCE.contains(player.getUUID()) && inEntrance) {
			leaveToOrigin(player);
		}
	}

	public static void tryInteract(ServerPlayer player) {
		if (!CardKingdomEggRoomUtil.isEggRoom(player.level()) || !(player.level() instanceof ServerLevel eggLevel) || isInTransit(player.getUUID())) {
			return;
		}

		long tick = eggLevel.getGameTime();
		Long last = INTERACT_TICK.put(player.getUUID(), tick);
		if (last != null && last == tick) {
			return;
		}

		ensurePlaced(eggLevel);

		Data data = Data.get(eggLevel);
		double x = player.getX();
		double z = player.getZ();
		SoulCapability cap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
        boolean gone = cap.hasEggRoomManGone(CardKingdomEggRoomUtil.CARD_KINGDOM_BIT);
		String interactionMessage = null;

		if (CardKingdomEggRoomUtil.inTreeFront(x, z, data.treeX, data.treeZ)) {
			interactionMessage = gone ? ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_FRONT_GONE : ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_FRONT;
		} else if (CardKingdomEggRoomUtil.inTreeBehind(x, z, data.treeX, data.treeZ)) {
			interactionMessage = gone ? ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_BEHIND_GONE : ClientBoundTextBoxPacket.CARD_KINGDOM_EGG_ROOM_TREE_BEHIND;
		}

		if (interactionMessage != null) {
			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundTextBoxPacket(interactionMessage));
		}
	}

	private static void settleAtSpawn(ServerPlayer player) {
		if (!CardKingdomEggRoomUtil.isEggRoom(player.level()) || isInTransit(player.getUUID())) {
			return;
		}

		Vec3 spawn = CardKingdomEggRoomUtil.spawnPos();
		player.setNoGravity(false);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0f;
		player.teleportTo(spawn.x, spawn.y, spawn.z);
		applySpawnFacing(player);
	}

	private static void applySpawnFacing(ServerPlayer player) {
		player.setYRot(CardKingdomEggRoomUtil.SPAWN_YAW);
		player.setXRot(0f);
		player.setYHeadRot(CardKingdomEggRoomUtil.SPAWN_YAW);
		player.setYBodyRot(CardKingdomEggRoomUtil.SPAWN_YAW);
	}

	private static boolean isInTransit(UUID id) {
		return TRANSIT.contains(id);
	}

	private static void beginTransit(ServerPlayer player, ResourceKey<Level> destDim, boolean eggRoom, double x, double y, double z, float yaw) {
		UUID id = player.getUUID();
		boolean first = TRANSIT.add(id);

		TRANSIT_UNTIL.put(id, System.currentTimeMillis() + ARRIVAL_GRACE_MS);
		TRANSIT_DEST.put(id, new TransitDest(eggRoom, x, y, z, yaw));

		player.setNoGravity(true);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0f;

		if (first) {
			ChunkPos chunk = new ChunkPos(BlockPos.containing(x, y, z));
			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new ClientBoundEggRoomCoverPacket(destDim, chunk.x, chunk.z));
		}
	}

	private static void scheduleResync(ServerPlayer player, boolean eggRoom, double x, double y, double z, float yaw) {
		if (!RESYNC_SCHEDULED.add(player.getUUID())) {
			return;
		}

		MinecraftServer server = player.getServer();
		if (server == null) {
			RESYNC_SCHEDULED.remove(player.getUUID());
			return;
		}

		server.execute(() -> {
			RESYNC_SCHEDULED.remove(player.getUUID());
			if (player.hasDisconnected()) {
				return;
			}

			resyncNow(player, eggRoom, x, y, z, yaw);
		});
	}

	private static void resyncNow(ServerPlayer player, boolean eggRoom, double x, double y, double z, float yaw) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}

		player.connection.teleport(x, y, z, yaw, 0f);

		if (eggRoom && CardKingdomEggRoomUtil.isEggRoom(level)) {
			applySpawnFacing(player);
			sendRoomToPlayer(player, level, Data.get(level));
		} else {
			sendAreaToPlayer(player, level, x, z);
		}
	}

	private static void tickTransit(ServerPlayer player) {
		UUID id = player.getUUID();

		if (!TRANSIT.contains(id)) {
			return;
		}

		player.setNoGravity(true);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0f;
		player.setSprinting(false);
		Long until = TRANSIT_UNTIL.get(id);
		if (until != null && System.currentTimeMillis() >= until) {
			TransitDest dest = TRANSIT_DEST.get(id);

			if (dest != null) {
				resyncNow(player, dest.eggRoom, dest.x, dest.y, dest.z, dest.yaw);
			}

			endTransit(player);
		}
	}

	private static void endTransit(ServerPlayer player) {
		UUID id = player.getUUID();

		TRANSIT.remove(id);
		TRANSIT_UNTIL.remove(id);
		TRANSIT_DEST.remove(id);
		RESYNC_SCHEDULED.remove(id);

		player.setNoGravity(false);
	}

	private static void forceRoomChunks(ServerLevel level, Data data) {
		forceChunks(level, data.minX, data.minZ, data.maxX, data.maxZ);
	}

	private static void forceChunks(ServerLevel level, int minX, int minZ, int maxX, int maxZ) {
		int minCx = (minX >> 4) - 1;
		int maxCx = (maxX >> 4) + 1;
		int minCz = (minZ >> 4) - 1;
		int maxCz = (maxZ >> 4) + 1;

		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				level.setChunkForced(cx, cz, true);
				level.getChunk(cx, cz);
			}
		}
	}

	private static void sendRoomToPlayer(ServerPlayer player, ServerLevel level, Data data) {
		forceRoomChunks(level, data);
		int minCx = (data.minX >> 4) - 1;
		int maxCx = (data.maxX >> 4) + 1;
		int minCz = (data.minZ >> 4) - 1;
		int maxCz = (data.maxZ >> 4) + 1;
		ChunkPos center = new ChunkPos(BlockPos.containing(CardKingdomEggRoomUtil.SPAWN_X, CardKingdomEggRoomUtil.SPAWN_Y, CardKingdomEggRoomUtil.SPAWN_Z));
		level.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, center, 3, player.getId());
		player.connection.send(new ClientboundSetChunkCacheCenterPacket(center.x, center.z));

		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				LevelChunk chunk = level.getChunk(cx, cz);
				player.connection.send(new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null));
			}
		}
	}

	private static void sendAreaToPlayer(ServerPlayer player, ServerLevel level, double x, double z) {
		ChunkPos center = new ChunkPos(BlockPos.containing(x, 0, z));
		level.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, center, 3, player.getId());
		player.connection.send(new ClientboundSetChunkCacheCenterPacket(center.x, center.z));
		int radius = 2;

		for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
			for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
				LevelChunk chunk = level.getChunk(cx, cz);
				player.connection.send(new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null));
			}
		}
	}

	private static boolean isPendingDoor(ResourceKey<Level> dimension, BlockPos door) {
		BlockPos lower = door;
		for (PendingDoor pending : PENDING_DOORS) {
			if (pending.dimension.equals(dimension) && (pending.door.equals(lower) || pending.door.equals(lower.above()) || pending.door.equals(lower.below()))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDoorLocked(UUID id) {
		Long until = DOOR_LOCK_UNTIL.get(id);
		return until != null && System.currentTimeMillis() < until;
	}

	private static void lockDoor(UUID id, long ms) {
		DOOR_LOCK_UNTIL.put(id, System.currentTimeMillis() + ms);
	}

	private record PendingDoor(ResourceKey<Level> dimension, BlockPos door, long when) {
	}

	private record TransitDest(boolean eggRoom, double x, double y, double z, float yaw) {
	}

	public static class Data extends SavedData {
		public static final String ID = PenumbraPhantasm.MODID + "_egg_room";
		public boolean placed;
		public int minX = CardKingdomEggRoomUtil.MIN_X;
		public int minY = CardKingdomEggRoomUtil.PLACE_Y;
		public int minZ = CardKingdomEggRoomUtil.MIN_Z;
		public int maxX = CardKingdomEggRoomUtil.MAX_X;
		public int maxY = CardKingdomEggRoomUtil.PLACE_Y + 9;
		public int maxZ = CardKingdomEggRoomUtil.MAX_Z;
		public int treeX;
		public int treeY = CardKingdomEggRoomUtil.PLACE_Y + 1;
		public int treeZ;

		public static Data get(ServerLevel level) {
			return level.getDataStorage().computeIfAbsent(Data::load, Data::new, ID);
		}

		public static Data load(CompoundTag tag) {
			Data data = new Data();
			data.placed = tag.getBoolean("placed");
			data.minX = tag.getInt("minX");
			data.minY = tag.getInt("minY");
			data.minZ = tag.getInt("minZ");
			data.maxX = tag.getInt("maxX");
			data.maxY = tag.getInt("maxY");
			data.maxZ = tag.getInt("maxZ");
			data.treeX = tag.getInt("treeX");
			data.treeY = tag.getInt("treeY");
			data.treeZ = tag.getInt("treeZ");

			return data;
		}

		@Override
		public CompoundTag save(CompoundTag tag) {
			tag.putBoolean("placed", placed);
			tag.putInt("minX", minX);
			tag.putInt("minY", minY);
			tag.putInt("minZ", minZ);
			tag.putInt("maxX", maxX);
			tag.putInt("maxY", maxY);
			tag.putInt("maxZ", maxZ);
			tag.putInt("treeX", treeX);
			tag.putInt("treeY", treeY);
			tag.putInt("treeZ", treeZ);

			return tag;
		}

		public boolean contains(double x, double y, double z) {
			return x >= minX && x < maxX + 1 && z >= minZ && z < maxZ + 1 && y >= minY - 1 && y < maxY + 2;
		}
	}
}
