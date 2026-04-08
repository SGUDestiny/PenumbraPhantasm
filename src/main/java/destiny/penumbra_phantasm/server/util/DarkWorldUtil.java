package destiny.penumbra_phantasm.server.util;

import commoble.infiniverse.api.InfiniverseAPI;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.ServerConfig;
import destiny.penumbra_phantasm.server.block.entity.GreatDoorShapeBlockEntity;
import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkRoom;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.worldgen.SeededNoiseBasedChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DarkWorldUtil
{
	private static final Object RANDOM_GREAT_DOOR_PLACEMENT_LOCK = new Object();

	public record GreatDoorStructureResult(BlockPos anchorPos, Direction facing) {}

	public record RandomGreatDoorCandidate(boolean shared, BlockPos canonLower, Direction lightDoorExitFromInterior, @Nullable ResourceKey<Level> peerDarkDimension) {}

	private static BlockPos doubleDoorPartnerLower(Level level, BlockPos lowerDoorFoot) {
		BlockState low = level.getBlockState(lowerDoorFoot);
		if (!(low.getBlock() instanceof DoorBlock) || low.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
			return null;
		}
		Direction facing = low.getValue(DoorBlock.FACING);
		boolean open = low.getValue(DoorBlock.OPEN);
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos o = lowerDoorFoot.relative(dir);
			BlockState os = level.getBlockState(o);
			if (!(os.getBlock() instanceof DoorBlock)) {
				continue;
			}
			BlockPos oLower = os.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? o.below() : o;
			os = level.getBlockState(oLower);
			if (oLower.equals(lowerDoorFoot)) {
				continue;
			}
			if (os.getBlock() instanceof DoorBlock
					&& os.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
					&& os.getValue(DoorBlock.FACING) == facing
					&& os.getValue(DoorBlock.OPEN) == open) {
				return oLower;
			}
		}
		return null;
	}

	public static BlockPos canonicalLowerDoorFoot(Level level, BlockPos anyDoorPart) {
		BlockState s = level.getBlockState(anyDoorPart);
		if (!(s.getBlock() instanceof DoorBlock)) {
			return anyDoorPart;
		}
		BlockPos lower = s.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? anyDoorPart.below() : anyDoorPart;
		BlockState low = level.getBlockState(lower);
		if (!(low.getBlock() instanceof DoorBlock) || low.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
			return lower;
		}
		BlockPos partner = doubleDoorPartnerLower(level, lower);
		if (partner == null) {
			return lower;
		}
		return lower.compareTo(partner) < 0 ? lower : partner;
	}

	public static void addDoorStandingFeet(Level level, BlockPos anyDoorPart, Set<BlockPos> out) {
		BlockPos c = canonicalLowerDoorFoot(level, anyDoorPart);
		out.add(c);
		out.add(c.above());
		BlockPos partner = doubleDoorPartnerLower(level, c);
		if (partner != null) {
			out.add(partner);
			out.add(partner.above());
		}
	}

	private static boolean isGreatDoorFootSurfaceClutter(BlockState state) {
		if (state.isAir()) {
			return false;
		}
		if (!state.getFluidState().isEmpty()) {
			return true;
		}
		if (state.is(BlockTags.LEAVES)) {
			return true;
		}
		if (state.is(BlockTags.LOGS) || state.is(BlockTags.LOGS_THAT_BURN) || state.is(BlockTags.CRIMSON_STEMS) || state.is(BlockTags.WARPED_STEMS)) {
			return true;
		}
		if (state.is(BlockTags.SAPLINGS)) {
			return true;
		}
		if (state.is(BlockTags.REPLACEABLE)) {
			return true;
		}
		if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.SMALL_FLOWERS)) {
			return true;
		}
		if (state.is(Blocks.BAMBOO) || state.is(Blocks.BAMBOO_SAPLING)) {
			return true;
		}
		if (state.is(Blocks.SWEET_BERRY_BUSH)) {
			return true;
		}
		if (state.is(Blocks.CACTUS)) {
			return true;
		}
		if (state.is(Blocks.VINE) || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) {
			return true;
		}
		if (state.is(Blocks.MOSS_CARPET) || state.is(Blocks.AZALEA) || state.is(Blocks.FLOWERING_AZALEA)) {
			return true;
		}
		if (state.is(Blocks.BIG_DRIPLEAF) || state.is(Blocks.BIG_DRIPLEAF_STEM) || state.is(Blocks.SMALL_DRIPLEAF)) {
			return true;
		}
		if (state.is(Blocks.SPORE_BLOSSOM)) {
			return true;
		}
		if (state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK)) {
			return true;
		}
		if (state.is(Blocks.CHORUS_PLANT) || state.is(Blocks.CHORUS_FLOWER)) {
			return true;
		}
		return false;
	}

	private static boolean isUnsuitableGreatDoorFooting(ServerLevel level, BlockPos groundTop) {
		BlockState solid = level.getBlockState(groundTop);
		if (solid.isAir()) {
			return true;
		}
		if (!solid.getFluidState().isEmpty()) {
			return true;
		}
		if (solid.is(Blocks.BEDROCK)) {
			return true;
		}
		if (isGreatDoorFootSurfaceClutter(solid)) {
			return true;
		}
		int minY = level.getMinBuildHeight();
		if (groundTop.getY() <= minY + 5) {
			return true;
		}
		return false;
	}

	private static int resolveGreatDoorFootY(ServerLevel level, BlockPos fountainAnchor, int bx, int bz) {
		BlockPos column = new BlockPos(bx, level.getMaxBuildHeight() - 1, bz);
		int y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column).getY();
		int minY = level.getMinBuildHeight() + 5;
		int steps = 64;
		while (steps-- > 0 && y > minY) {
			BlockPos test = new BlockPos(bx, y, bz);
			BlockState st = level.getBlockState(test);
			if (isGreatDoorFootSurfaceClutter(st)) {
				y--;
				continue;
			}
			if (!isUnsuitableGreatDoorFooting(level, test)) {
				return y;
			}
			y--;
		}
		y = fountainAnchor.getY();
		steps = 32;
		while (steps-- > 0 && y > minY) {
			BlockPos anchorTest = new BlockPos(bx, y, bz);
			BlockState st = level.getBlockState(anchorTest);
			if (isGreatDoorFootSurfaceClutter(st)) {
				y--;
				continue;
			}
			if (!isUnsuitableGreatDoorFooting(level, anchorTest)) {
				return y;
			}
			y--;
		}
		return Integer.MIN_VALUE;
	}

	public static void createGreatDoor(Level pLevel, BlockPos greatDoorPos, Direction direction, boolean isOpen, BlockPos lightDoorPos,
									   ResourceKey<Level> lightDoorLevel, Direction lightDoorExitDirection, boolean isDestinationDarkWorld,
									   @Nullable BlockPos destinationGreatDoorPos, @Nullable ResourceKey<Level> destinationGreatDoorLevel) {
		GreatDoorCapability greatDoorCapability = null;
		LazyOptional<GreatDoorCapability> lightLazyCapability = pLevel.getCapability(CapabilityRegistry.GREAT_DOOR);
		if(lightLazyCapability.isPresent() && lightLazyCapability.resolve().isPresent())
			greatDoorCapability = lightLazyCapability.resolve().get();

		if (greatDoorCapability == null) {
			return;
		}

		Direction widthDir = direction.getClockWise();
		Direction depthDir = direction.getOpposite();
		List<BlockPos> volumePositions = new ArrayList<>();
		for (int y = 0; y < 9; y++) {
			for (int x = 0; x < 6; x++) {
				for (int z = 0; z < 2; z++) {
					volumePositions.add(greatDoorPos.relative(widthDir, x).relative(depthDir, z).above(y));
				}
			}
		}

		greatDoorCapability.addGreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos, lightDoorLevel,
				lightDoorExitDirection, isDestinationDarkWorld, destinationGreatDoorPos, destinationGreatDoorLevel);

		BlockState block = BlockRegistry.GREAT_DOOR_SHAPE.get().defaultBlockState();
		for (BlockPos target : volumePositions) {
			pLevel.setBlock(target, block, 3);
			if (pLevel.getBlockEntity(target) instanceof GreatDoorShapeBlockEntity greatDoorShape) {
				greatDoorShape.greatDoorPos = greatDoorPos;
			}
		}
	}

	public static Optional<GreatDoorStructureResult> tryPlaceGreatDoorStructure(ServerLevel level, BlockPos fountainAnchor, RandomSource random) {
		ResourceLocation structureId = new ResourceLocation(PenumbraPhantasm.MODID, "great_door");
		Optional<StructureTemplate> templateOpt = level.getStructureManager().get(structureId);
		if (templateOpt.isEmpty()) {
			return Optional.empty();
		}
		StructureTemplate template = templateOpt.get();
		int minR = Math.min(ServerConfig.greatDoorPlaceMinRadius, ServerConfig.greatDoorPlaceMaxRadius);
		int maxR = Math.max(ServerConfig.greatDoorPlaceMinRadius, ServerConfig.greatDoorPlaceMaxRadius);
		for (int attempt = 0; attempt < 48; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double dist = minR + random.nextDouble() * (maxR - minR);
			int bx = fountainAnchor.getX() + Mth.floor(Mth.cos((float) angle) * dist);
			int bz = fountainAnchor.getZ() + Mth.floor(Mth.sin((float) angle) * dist);
			int footY = resolveGreatDoorFootY(level, fountainAnchor, bx, bz);
			if (footY == Integer.MIN_VALUE) {
				continue;
			}
			int originY = footY - 1;
			if (originY <= level.getMinBuildHeight()) {
				continue;
			}
			BlockPos origin = new BlockPos(bx, originY, bz);
			Rotation rot = switch (random.nextInt(4)) {
				case 0 -> Rotation.NONE;
				case 1 -> Rotation.CLOCKWISE_90;
				case 2 -> Rotation.CLOCKWISE_180;
				default -> Rotation.COUNTERCLOCKWISE_90;
			};
			StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rot).setMirror(Mirror.NONE).setIgnoreEntities(false);
			ChunkPos cp = new ChunkPos(origin);
			level.setChunkForced(cp.x, cp.z, true);
			boolean placedOk;
			try {
				placedOk = template.placeInWorld(level, origin, origin, settings, random, 2);
			} finally {
				level.setChunkForced(cp.x, cp.z, false);
			}
			if (!placedOk) {
				continue;
			}
			BoundingBox bb = template.getBoundingBox(settings, origin);
			BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
			BlockPos found = null;
			Direction facing = Direction.NORTH;
			for (int x = bb.minX(); x <= bb.maxX(); x++) {
				for (int y = bb.minY(); y <= bb.maxY(); y++) {
					for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
						m.set(x, y, z);
						BlockState st = level.getBlockState(m);
						if (st.is(BlockRegistry.GREAT_DOOR_SPAWNER.get())) {
							found = m.immutable();
							facing = st.getValue(HorizontalDirectionalBlock.FACING);
							break;
						}
					}
					if (found != null) {
						break;
					}
				}
				if (found != null) {
					break;
				}
			}
			if (found != null) {
				level.setBlock(found, Blocks.AIR.defaultBlockState(), 3);
				return Optional.of(new GreatDoorStructureResult(found, facing));
			}
		}
		return Optional.empty();
	}

	private static boolean horizontalBoundingBoxInsideChunk(ChunkPos cp, BoundingBox bb) {
		return bb.minX() >= cp.getMinBlockX() && bb.maxX() <= cp.getMaxBlockX()
				&& bb.minZ() >= cp.getMinBlockZ() && bb.maxZ() <= cp.getMaxBlockZ();
	}

	public static Optional<GreatDoorStructureResult> tryPlaceGreatDoorStructureInChunk(ServerLevel level, ChunkPos cp, RandomSource random) {
		ResourceLocation structureId = new ResourceLocation(PenumbraPhantasm.MODID, "great_door");
		Optional<StructureTemplate> templateOpt = level.getStructureManager().get(structureId);
		if (templateOpt.isEmpty()) {
			return Optional.empty();
		}
		StructureTemplate template = templateOpt.get();
		Vec3i tsz = template.getSize();
		int maxHoriz = Math.max(tsz.getX(), tsz.getZ()) + 2;
		int span = Math.min(12, Math.max(4, 16 - maxHoriz));
		if (span < 2) {
			return Optional.empty();
		}
		for (int attempt = 0; attempt < 40; attempt++) {
			int bx = cp.getMinBlockX() + 2 + random.nextInt(span);
			int bz = cp.getMinBlockZ() + 2 + random.nextInt(span);
			BlockPos heightProbe = new BlockPos(bx, level.getMaxBuildHeight() - 1, bz);
			int ay = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, heightProbe).getY();
			BlockPos localAnchor = new BlockPos(bx, ay, bz);
			int footY = resolveGreatDoorFootY(level, localAnchor, bx, bz);
			if (footY == Integer.MIN_VALUE) {
				continue;
			}
			int originY = footY - 1;
			if (originY <= level.getMinBuildHeight()) {
				continue;
			}
			BlockPos origin = new BlockPos(bx, originY, bz);
			Rotation rot = switch (random.nextInt(4)) {
				case 0 -> Rotation.NONE;
				case 1 -> Rotation.CLOCKWISE_90;
				case 2 -> Rotation.CLOCKWISE_180;
				default -> Rotation.COUNTERCLOCKWISE_90;
			};
			StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rot).setMirror(Mirror.NONE).setIgnoreEntities(false);
			BoundingBox bbPreview = template.getBoundingBox(settings, origin);
			if (!horizontalBoundingBoxInsideChunk(cp, bbPreview)) {
				continue;
			}
			ChunkPos forceCp = new ChunkPos(origin);
			level.setChunkForced(forceCp.x, forceCp.z, true);
			boolean placedOk;
			try {
				placedOk = template.placeInWorld(level, origin, origin, settings, random, 2);
			} finally {
				level.setChunkForced(forceCp.x, forceCp.z, false);
			}
			if (!placedOk) {
				continue;
			}
			BoundingBox bb = template.getBoundingBox(settings, origin);
			BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
			BlockPos found = null;
			Direction facing = Direction.NORTH;
			for (int x = bb.minX(); x <= bb.maxX(); x++) {
				for (int y = bb.minY(); y <= bb.maxY(); y++) {
					for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
						m.set(x, y, z);
						BlockState st = level.getBlockState(m);
						if (st.is(BlockRegistry.GREAT_DOOR_SPAWNER.get())) {
							found = m.immutable();
							facing = st.getValue(HorizontalDirectionalBlock.FACING);
							break;
						}
					}
					if (found != null) {
						break;
					}
				}
				if (found != null) {
					break;
				}
			}
			if (found != null) {
				level.setBlock(found, Blocks.AIR.defaultBlockState(), 3);
				return Optional.of(new GreatDoorStructureResult(found, facing));
			}
		}
		return Optional.empty();
	}

	@Nullable
	public static DarkFountain getFirstDarkFountain(ServerLevel level) {
		return level.getCapability(CapabilityRegistry.DARK_FOUNTAIN)
				.resolve()
				.map(cap -> cap.darkFountains.entrySet().stream()
						.min(Comparator.comparingLong(e -> e.getKey().asLong()))
						.map(Map.Entry::getValue)
						.orElse(null))
				.orElse(null);
	}

	private static Direction findDirFromInteriorForShellDoor(Set<BlockPos> roomPositions, BlockPos canonLower) {
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (roomPositions.contains(canonLower.relative(dir.getOpposite()))
					|| roomPositions.contains(canonLower.above().relative(dir.getOpposite()))) {
				return dir;
			}
		}
		return null;
	}

	public static List<RandomGreatDoorCandidate> collectUnclaimedShellDoorCandidates(ServerLevel darkLevel, MinecraftServer server, DarkFountain fountain) {
		List<RandomGreatDoorCandidate> out = new ArrayList<>();
		Set<BlockPos> outsideCanonSeen = new HashSet<>();
		Set<BlockPos> sharedCanonSeen = new HashSet<>();
		ResourceKey<Level> darkDim = darkLevel.dimension();
		for (DarkRoom room : fountain.rooms) {
			if (room.isDissipating()) {
				continue;
			}
			Set<BlockPos> posSet = new HashSet<>(room.getPositions());
			for (Map.Entry<BlockPos, Direction> e : room.getOutsideDoors().entrySet()) {
				BlockPos canon = canonicalLowerDoorFoot(darkLevel, e.getKey());
				if (!canon.equals(e.getKey())) {
					continue;
				}
				if (!outsideCanonSeen.add(canon)) {
					continue;
				}
				if (GreatDoorCapability.isLightDoorClaimedGlobally(server, canon, darkDim)) {
					continue;
				}
				out.add(new RandomGreatDoorCandidate(false, canon, e.getValue(), null));
			}
			for (Map.Entry<BlockPos, ResourceKey<Level>> e : room.getSharedDoors().entrySet()) {
				BlockPos canon = canonicalLowerDoorFoot(darkLevel, e.getKey());
				if (!canon.equals(e.getKey())) {
					continue;
				}
				if (!sharedCanonSeen.add(canon)) {
					continue;
				}
				Direction dir = findDirFromInteriorForShellDoor(posSet, canon);
				if (dir == null) {
					continue;
				}
				if (GreatDoorCapability.isLightDoorClaimedGlobally(server, canon, darkDim)) {
					continue;
				}
				out.add(new RandomGreatDoorCandidate(true, canon, dir, e.getValue()));
			}
		}
		return out;
	}

	private static void removeGreatDoorAndVolume(ServerLevel level, BlockPos greatDoorAnchor) {
		level.getCapability(CapabilityRegistry.GREAT_DOOR).ifPresent(cap -> {
			GreatDoor g = cap.greatDoors.remove(greatDoorAnchor);
			if (g != null) {
				for (BlockPos p : g.volumePositions) {
					level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
				}
			}
		});
	}

	public static void tryRandomGreatDoorForDarkChunk(ServerLevel level, ChunkPos cp) {
		if (!isDarkWorld(level)) {
			return;
		}
		MinecraftServer server = level.getServer();
		GreatDoorCapability cap = level.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().orElse(null);
		if (cap == null) {
			return;
		}
		if (cap.isRandomGreatDoorChunkProcessed(cp.toLong())) {
			return;
		}
		DarkFountain fountain = getFirstDarkFountain(level);
		if (fountain == null) {
			return;
		}
		List<RandomGreatDoorCandidate> unclaimed = collectUnclaimedShellDoorCandidates(level, server, fountain);
		if (unclaimed.isEmpty()) {
			return;
		}
		long rollSeed = level.getSeed() ^ cp.toLong() ^ 0xC0AFE11DCAFEBAB2L;
		RandomSource rollRng = RandomSource.create(rollSeed);
		int rarity = Math.max(1, ServerConfig.randomGreatDoorChunkRarity);
		if (rollRng.nextInt(rarity) != 0) {
			cap.markRandomGreatDoorChunkProcessed(cp.toLong());
			return;
		}
		synchronized (RANDOM_GREAT_DOOR_PLACEMENT_LOCK) {
			if (cap.isRandomGreatDoorChunkProcessed(cp.toLong())) {
				return;
			}
			List<RandomGreatDoorCandidate> unclaimed2 = collectUnclaimedShellDoorCandidates(level, server, fountain);
			if (unclaimed2.isEmpty()) {
				cap.markRandomGreatDoorChunkProcessed(cp.toLong());
				return;
			}
			RandomSource pickRng = RandomSource.create(rollSeed ^ 0x9E3779B97F4A7C15L);
			RandomGreatDoorCandidate choice = unclaimed2.get(pickRng.nextInt(unclaimed2.size()));
			if (GreatDoorCapability.isLightDoorClaimedGlobally(server, choice.canonLower(), level.dimension())) {
				cap.markRandomGreatDoorChunkProcessed(cp.toLong());
				return;
			}
			if (choice.shared()) {
				if (choice.peerDarkDimension() == null) {
					cap.markRandomGreatDoorChunkProcessed(cp.toLong());
					return;
				}
				if (server.getLevel(choice.peerDarkDimension()) == null) {
					cap.markRandomGreatDoorChunkProcessed(cp.toLong());
					return;
				}
			}
			RandomSource placeRng = RandomSource.create(rollSeed ^ 0xDEADBEEFCAFEBAB2L);
			Optional<GreatDoorStructureResult> placed = tryPlaceGreatDoorStructureInChunk(level, cp, placeRng);
			if (placed.isEmpty()) {
				cap.markRandomGreatDoorChunkProcessed(cp.toLong());
				return;
			}
			GreatDoorStructureResult r = placed.get();
			if (choice.shared()) {
				ServerLevel peerLevel = server.getLevel(choice.peerDarkDimension());
				createGreatDoor(level, r.anchorPos(), r.facing(), true, choice.canonLower(), level.dimension(),
						choice.lightDoorExitFromInterior(), true, null, choice.peerDarkDimension());
				GreatDoor created = level.getCapability(CapabilityRegistry.GREAT_DOOR)
						.resolve()
						.map(c -> c.greatDoors.get(r.anchorPos()))
						.orElse(null);
				if (created == null) {
					cap.markRandomGreatDoorChunkProcessed(cp.toLong());
					return;
				}
				if (!ensurePeerGreatDoor(created, level, peerLevel)) {
					removeGreatDoorAndVolume(level, r.anchorPos());
					cap.markRandomGreatDoorChunkProcessed(cp.toLong());
					return;
				}
			} else {
				createGreatDoor(level, r.anchorPos(), r.facing(), true, choice.canonLower(), level.dimension(),
						choice.lightDoorExitFromInterior(), false, null, null);
			}
			cap.markRandomGreatDoorChunkProcessed(cp.toLong());
		}
	}

	public static boolean ensurePeerGreatDoor(GreatDoor source, ServerLevel sourceLevel, ServerLevel destLevel) {
		if (!source.isDestinationDarkWorld || source.destinationGreatDoorDimension == null) {
			return false;
		}
		if (!source.destinationGreatDoorDimension.equals(destLevel.dimension())) {
			return false;
		}
		if (source.destinationGreatDoorPos != null) {
			GreatDoor existing = destLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
					.resolve()
					.map(c -> c.greatDoors.get(source.destinationGreatDoorPos))
					.orElse(null);
			if (existing != null) {
				return true;
			}
		}
		BlockPos anchor = findDarkFountainAnchor(destLevel);
		if (anchor == null) {
			return false;
		}
		Optional<GreatDoorStructureResult> placed = tryPlaceGreatDoorStructure(destLevel, anchor, destLevel.random);
		if (placed.isEmpty()) {
			return false;
		}
		GreatDoorStructureResult r = placed.get();
		createGreatDoor(destLevel, r.anchorPos(), r.facing(), true, source.lightDoorPos, source.lightDoorDimension,
				source.lightDoorExitDirection, true, source.greatDoorPos, sourceLevel.dimension());
		source.destinationGreatDoorPos = r.anchorPos();
		source.destinationGreatDoorDimension = destLevel.dimension();
		return true;
	}

	@Nullable
	public static GreatDoor ensureGreatDoorForOutsideDoor(ServerLevel darkLevel, BlockPos darkFountainAnchor, BlockPos lightDoorLower,
														  ResourceKey<Level> lightDimension, Direction lightDoorExitFromInterior) {
		GreatDoor existing = darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
				.resolve()
				.map(c -> c.findByLightDoor(lightDoorLower, lightDimension))
				.orElse(null);
		if (existing != null) {
			return existing;
		}
		Optional<GreatDoorStructureResult> placed = tryPlaceGreatDoorStructure(darkLevel, darkFountainAnchor, darkLevel.random);
		if (placed.isEmpty()) {
			return null;
		}
		GreatDoorStructureResult r = placed.get();
		createGreatDoor(darkLevel, r.anchorPos(), r.facing(), true, lightDoorLower, lightDimension, lightDoorExitFromInterior,
				false, null, null);
		return darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
				.resolve()
				.map(c -> c.greatDoors.get(r.anchorPos()))
				.orElse(null);
	}

	@Nullable
	private static BlockPos findDarkFountainAnchor(ServerLevel darkLevel) {
		return darkLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN)
				.resolve()
				.map(cap -> cap.darkFountains.isEmpty() ? null : cap.darkFountains.keySet().iterator().next())
				.orElse(null);
	}

	public static TagKey<Block> getBlockTag(ResourceLocation location)
	{
		return TagKey.create(Registries.BLOCK, location);
	}

	public static Holder<NoiseGeneratorSettings> getNoiseGenerator(MinecraftServer server, ResourceLocation location)
	{
		ResourceKey<NoiseGeneratorSettings> key = ResourceKey.create(Registries.NOISE_SETTINGS, location);
		return server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(key);
	}

	public static ResourceKey<NoiseGeneratorSettings> getNoiseGeneratorKey(ResourceLocation location)
	{
		return ResourceKey.create(Registries.NOISE_SETTINGS, location);
	}

	public static Holder<DimensionType> getDimensionType(MinecraftServer server, ResourceLocation location)
	{
		ResourceKey<DimensionType> key = ResourceKey.create(Registries.DIMENSION_TYPE, location);
		return server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(key);
	}

	public static ServerLevel createDarkWorld(MinecraftServer server, BlockPos pos, ResourceKey<Level> origin,
											  DarkWorldType type)
	{
		ResourceLocation typeKey = server.registryAccess().registryOrThrow(DarkWorldType.REGISTRY_KEY).getKey(type);
		if(typeKey == null)
			return null;

		ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION,
				new ResourceLocation(PenumbraPhantasm.MODID,
						("dark_world_"+pos.asLong()+"_"+origin.location()+"_"+typeKey).replace(':', '-')));

		ServerLevel existingLevel = server.getLevel(key);
		if(existingLevel != null)
			return existingLevel;

		long seed = createUniqueDarkWorldSeed(server, key);

		RandomState randomState = RandomState.create(server.registryAccess().asGetterLookup(), getNoiseGeneratorKey(type.noiseSettings()), seed);
		ChunkGenerator chunkGenerator = new SeededNoiseBasedChunkGenerator(type.source(),
				getNoiseGenerator(server, type.noiseSettings()), randomState, seed);

		LevelStem stem = new LevelStem(getDimensionType(server, type.dimensionType()), chunkGenerator);

		ServerLevel level = InfiniverseAPI.get().getOrCreateLevel(server, key, () -> stem);
		return level;
	}

	private static long createUniqueDarkWorldSeed(MinecraftServer server, ResourceKey<Level> levelKey)
	{
		long serverSeed = server.overworld().getSeed();
		UUID dimensionId = UUID.nameUUIDFromBytes(levelKey.location().toString().getBytes(StandardCharsets.UTF_8));
		long seed = dimensionId.getMostSignificantBits() ^ dimensionId.getLeastSignificantBits() ^ serverSeed;
		if(seed == 0L)
			seed = 1L;

		Set<Long> usedSeeds = new HashSet<>();
		for(ServerLevel level : getAllDarkWorlds(server))
		{
			if(level.getChunkSource().getGenerator() instanceof SeededNoiseBasedChunkGenerator seededGenerator)
				usedSeeds.add(seededGenerator.getSeed());
		}

		while(usedSeeds.contains(seed))
		{
			seed = Long.rotateLeft(seed ^ 0x9E3779B97F4A7C15L, 17);
			if(seed == 0L)
				seed = 1L;
		}

		return seed;
	}

	public static boolean isDarkWorld(Level level)
	{
		return level.dimension().location().getPath().contains("dark_world");
	}

	public static boolean isDarkWorldKey(ResourceKey<Level> levelResourceKey)
	{
		return levelResourceKey.location().getPath().contains("dark_world");
	}

	public static List<ServerLevel> getAllDarkWorlds(MinecraftServer server)
	{
		List<ServerLevel> darkWorlds = new ArrayList<>();
		for(ServerLevel level : server.getAllLevels())
		{
			if(level.dimension().location().getPath().contains("dark_world"))
				darkWorlds.add(level);
		}

		return darkWorlds;
	}

}
