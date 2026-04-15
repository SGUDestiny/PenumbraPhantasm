package destiny.penumbra_phantasm.server.util;

import commoble.infiniverse.api.InfiniverseAPI;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.ServerConfig;
import destiny.penumbra_phantasm.server.block.entity.GreatDoorShapeBlockEntity;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
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
import net.minecraft.world.level.chunk.ChunkAccess;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DarkWorldUtil
{
	public record GreatDoorStructureResult(BlockPos anchorPos, Direction facing) {}

	public static BlockPos getDoubleDoorPartnerLower(Level level, BlockPos lowerDoorFoot) {
		BlockState low = level.getBlockState(lowerDoorFoot);
		if (!(low.getBlock() instanceof DoorBlock) || low.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
			return null;
		}
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
			if (os.getBlock() instanceof DoorBlock && os.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
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
		BlockPos partner = getDoubleDoorPartnerLower(level, lower);
		if (partner == null) {
			return lower;
		}
		return lower.compareTo(partner) < 0 ? lower : partner;
	}

	public static void addDoorStandingFeet(Level level, BlockPos anyDoorPart, Set<BlockPos> out) {
		BlockPos c = canonicalLowerDoorFoot(level, anyDoorPart);
		out.add(c);
		out.add(c.above());
		BlockPos partner = getDoubleDoorPartnerLower(level, c);
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

	private static Optional<GreatDoorStructureResult> clearGreatDoorSpawnerInPlacedTemplate(ServerLevel level, StructureTemplate template,
																						   StructurePlaceSettings settings, BlockPos origin) {
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
		return Optional.empty();
	}

	private static Optional<GreatDoorStructureResult> placeGreatDoorStructureTemplate(ServerLevel level, StructureTemplate template, BlockPos origin,
																					  Rotation rot, RandomSource random) {
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
			return Optional.empty();
		}
		return clearGreatDoorSpawnerInPlacedTemplate(level, template, settings, origin);
	}

	public static void createGreatDoor(Level pLevel, BlockPos greatDoorPos, Direction direction, boolean isOpen, @Nullable BlockPos lightDoorPos,
									   @Nullable BlockPos lightDoorSecondLower, @Nullable ResourceKey<Level> lightDoorLevel, @Nullable Direction lightDoorExitDirection, boolean isDestinationDarkWorld,
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

		greatDoorCapability.addGreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos, lightDoorSecondLower, lightDoorLevel,
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
			Optional<GreatDoorStructureResult> placed = placeGreatDoorStructureTemplate(level, template, origin, rot, random);
			if (placed.isPresent()) {
				return placed;
			}
		}
		return Optional.empty();
	}

	public static void convertGreatDoorSpawnersInChunk(ServerLevel level, ChunkAccess chunk) {
		if (!isDarkWorld(level)) {
			return;
		}
		Optional<GreatDoorCapability> capOpt = level.getCapability(CapabilityRegistry.GREAT_DOOR).resolve();
		if (capOpt.isEmpty()) {
			return;
		}
		GreatDoorCapability cap = capOpt.get();
		int minY = level.getMinBuildHeight();
		int maxY = level.getMaxBuildHeight();
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		int x0 = chunk.getPos().getMinBlockX();
		int x1 = chunk.getPos().getMaxBlockX();
		int z0 = chunk.getPos().getMinBlockZ();
		int z1 = chunk.getPos().getMaxBlockZ();
		for (int x = x0; x <= x1; x++) {
			int relX = x - x0;
			for (int z = z0; z <= z1; z++) {
				int relZ = z - z0;
				int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, relX, relZ);
				int yLo = Math.max(minY, surface - 40);
				int yHi = Math.min(maxY - 1, surface + 24);
				for (int y = yLo; y <= yHi; y++) {
					m.set(x, y, z);
					BlockState st = chunk.getBlockState(m);
					if (!st.is(BlockRegistry.GREAT_DOOR_SPAWNER.get())) {
						continue;
					}
					BlockPos anchor = m.immutable();
					if (cap.greatDoors.containsKey(anchor)) {
						continue;
					}
					Direction facing = st.getValue(HorizontalDirectionalBlock.FACING);
					level.setBlock(anchor, Blocks.AIR.defaultBlockState(), 3);
					createGreatDoor(level, anchor, facing, false, null, null, null, null, false, null, null);
				}
			}
		}
	}

	public static void tryBindUnlinkedGreatDoor(ServerLevel darkLevel, GreatDoor door) {
		if (!door.isUnlinkedForAutoBinding()) {
			return;
		}
		ResourceKey<Level> darkDim = darkLevel.dimension();
		MinecraftServer server = darkLevel.getServer();
		for (ServerLevel lightLevel : server.getAllLevels()) {
			if (isDarkWorld(lightLevel)) {
				continue;
			}
			Optional<DarkFountainCapability> capOpt = lightLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).resolve();
			if (capOpt.isEmpty()) {
				continue;
			}
			for (DarkFountain fountain : capOpt.get().darkFountains.values()) {
				if (!fountain.destinationDimension.equals(darkDim)) {
					continue;
				}
				if (tryBindSharedDoorsForUnlinked(darkLevel, door, lightLevel, fountain)) {
					return;
				}
				if (tryBindOutsideDoorsForUnlinked(darkLevel, door, lightLevel, fountain)) {
					return;
				}
			}
		}
	}

	private static boolean lightDoorPairTaken(ServerLevel darkLevel, BlockPos lower, @Nullable BlockPos secondLower, ResourceKey<Level> lightDim) {
		return darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
				.resolve()
				.map(c -> c.findByLightDoor(lower, lightDim) != null
						|| (secondLower != null && c.findByLightDoor(secondLower, lightDim) != null))
				.orElse(false);
	}

	private static boolean isValidOutsideDoorGreatDoorTarget(GreatDoor g) {
		return g.destinationGreatDoorPos == null
				&& g.destinationGreatDoorDimension == null
				&& !g.isDestinationDarkWorld;
	}

	private static boolean tryBindOutsideDoorsForUnlinked(ServerLevel darkLevel, GreatDoor door, ServerLevel lightLevel, DarkFountain fountain) {
		for (DarkRoom room : fountain.rooms) {
			for (Map.Entry<BlockPos, DarkRoom.OutsideDoorExit> e : room.getOutsideDoors().entrySet()) {
				BlockPos lower = e.getKey();
				DarkRoom.OutsideDoorExit exit = e.getValue();
				BlockPos secondLower = exit.secondLowerHalf();
				if (lightDoorPairTaken(darkLevel, lower, secondLower, lightLevel.dimension())) {
					continue;
				}
				door.lightDoorPos = lower;
				door.lightDoorSecondLower = secondLower;
				door.lightDoorDimension = lightLevel.dimension();
				door.lightDoorExitDirection = exit.exitFromInterior();
				door.isDestinationDarkWorld = false;
				door.destinationGreatDoorPos = null;
				door.destinationGreatDoorDimension = null;
				door.broadcastSync(darkLevel);
				return true;
			}
		}
		return false;
	}

	private static boolean tryBindSharedDoorsForUnlinked(ServerLevel darkLevel, GreatDoor door, ServerLevel lightLevel, DarkFountain fountain) {
		for (DarkRoom room : fountain.rooms) {
			for (Map.Entry<BlockPos, DarkRoom.SharedDoorLink> e : room.getSharedDoors().entrySet()) {
				BlockPos lower = e.getKey();
				DarkRoom.SharedDoorLink link = e.getValue();
				ResourceKey<Level> otherDarkKey = link.otherDarkWorld();
				BlockPos secondLower = link.secondLowerHalf();
				if (!isDarkWorldKey(otherDarkKey)) {
					continue;
				}
				if (lightDoorPairTaken(darkLevel, lower, secondLower, lightLevel.dimension())) {
					continue;
				}
				ServerLevel otherLevel = darkLevel.getServer().getLevel(otherDarkKey);
				if (otherLevel == null) {
					continue;
				}
				Direction exitDir = room.interiorHorizontalDirectionTowardDoor(lower).orElse(Direction.NORTH);
				door.lightDoorPos = lower;
				door.lightDoorSecondLower = secondLower;
				door.lightDoorDimension = lightLevel.dimension();
				door.lightDoorExitDirection = exitDir;
				door.isDestinationDarkWorld = true;
				door.destinationGreatDoorDimension = otherDarkKey;
				door.destinationGreatDoorPos = null;
				ensurePeerGreatDoor(door, darkLevel, otherLevel);
				if (door.destinationGreatDoorPos == null) {
					door.lightDoorPos = null;
					door.lightDoorSecondLower = null;
					door.lightDoorDimension = null;
					door.lightDoorExitDirection = null;
					door.isDestinationDarkWorld = false;
					door.destinationGreatDoorDimension = null;
					continue;
				}
				door.broadcastSync(darkLevel);
				return true;
			}
		}
		return false;
	}

	public static void ensurePeerGreatDoor(GreatDoor source, ServerLevel sourceLevel, ServerLevel destLevel) {
		if (!source.isDestinationDarkWorld || source.destinationGreatDoorDimension == null) {
			return;
		}
		if (source.lightDoorPos == null || source.lightDoorDimension == null || source.lightDoorExitDirection == null) {
			return;
		}
		if (!source.destinationGreatDoorDimension.equals(destLevel.dimension())) {
			return;
		}
		if (source.destinationGreatDoorPos != null) {
			GreatDoor existing = destLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
					.resolve()
					.map(c -> c.greatDoors.get(source.destinationGreatDoorPos))
					.orElse(null);
			if (existing != null) {
				return;
			}
		}
		BlockPos anchor = findDarkFountainAnchor(destLevel);
		if (anchor == null) {
			return;
		}
		Optional<GreatDoorStructureResult> placed = tryPlaceGreatDoorStructure(destLevel, anchor, destLevel.random);
		if (placed.isEmpty()) {
			return;
		}
		GreatDoorStructureResult r = placed.get();
		createGreatDoor(destLevel, r.anchorPos(), r.facing(), true, source.lightDoorPos, source.lightDoorSecondLower, source.lightDoorDimension,
				source.lightDoorExitDirection, true, source.greatDoorPos, sourceLevel.dimension());
		source.destinationGreatDoorPos = r.anchorPos();
		source.destinationGreatDoorDimension = destLevel.dimension();
	}

	@Nullable
	public static GreatDoor ensureGreatDoorForOutsideDoor(ServerLevel darkLevel, BlockPos darkFountainAnchor, BlockPos lightDoorLower,
														  ResourceKey<Level> lightDimension, Direction lightDoorExitFromInterior, @Nullable BlockPos lightDoorSecondLower) {
		GreatDoor existing = darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
				.resolve()
				.map(c -> c.findByLightDoor(lightDoorLower, lightDimension))
				.orElse(null);
		if (existing == null && lightDoorSecondLower != null) {
			existing = darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
					.resolve()
					.map(c -> c.findByLightDoor(lightDoorSecondLower, lightDimension))
					.orElse(null);
		}
		if (existing != null) {
			if (!isValidOutsideDoorGreatDoorTarget(existing)) {
				existing.isDestinationDarkWorld = false;
				existing.destinationGreatDoorPos = null;
				existing.destinationGreatDoorDimension = null;
			}
			existing.lightDoorPos = lightDoorLower;
			existing.lightDoorSecondLower = lightDoorSecondLower;
			existing.lightDoorDimension = lightDimension;
			existing.lightDoorExitDirection = lightDoorExitFromInterior;
			existing.broadcastSync(darkLevel);
			return existing;
		}
		Optional<GreatDoorStructureResult> placed = tryPlaceGreatDoorStructure(darkLevel, darkFountainAnchor, darkLevel.random);
		if (placed.isEmpty()) {
			return null;
		}
		GreatDoorStructureResult r = placed.get();
		createGreatDoor(darkLevel, r.anchorPos(), r.facing(), true, lightDoorLower, lightDoorSecondLower, lightDimension, lightDoorExitFromInterior,
				false, null, null);
		return darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR)
				.resolve()
				.map(c -> c.greatDoors.get(r.anchorPos()))
				.orElse(null);
	}

	public static boolean levelHasDarkFountain(ServerLevel level) {
		return level.getCapability(CapabilityRegistry.DARK_FOUNTAIN)
				.map(cap -> !cap.darkFountains.isEmpty())
				.orElse(false);
	}

	@Nullable
	public static BlockPos findDarkFountainAnchor(ServerLevel darkLevel) {
		if (!levelHasDarkFountain(darkLevel)) {
			return null;
		}
		return darkLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN)
				.resolve()
				.map(cap -> cap.darkFountains.keySet().iterator().next())
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
		if (existingLevel != null) {
			return existingLevel;
		}

		long seed = createUniqueDarkWorldSeed(server, key);

		RandomState randomState = RandomState.create(server.registryAccess().asGetterLookup(), getNoiseGeneratorKey(type.noiseSettings()), seed);
		ChunkGenerator chunkGenerator = new SeededNoiseBasedChunkGenerator(type.source(),
				getNoiseGenerator(server, type.noiseSettings()), randomState, seed);

		LevelStem stem = new LevelStem(getDimensionType(server, type.dimensionType()), chunkGenerator);

		return InfiniverseAPI.get().getOrCreateLevel(server, key, () -> stem);
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
