package destiny.penumbra_phantasm.server.util;

import commoble.infiniverse.api.InfiniverseAPI;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.ServerConfig;
import destiny.penumbra_phantasm.server.block.entity.GreatDoorShapeBlockEntity;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.datapack.DarkWorldRecipeSeparation;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.fountain.DarkRoom;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.worldgen.SeededNoiseBasedChunkGenerator;
import net.minecraft.core.*;
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

public class DarkWorldUtil {
	public record GreatDoorStructureResult(BlockPos anchorPos, Direction facing) {}

	public static BlockPos getDoubleDoorLower(Level level, BlockPos lowerPos) {
		BlockState lowerState = level.getBlockState(lowerPos);

		if (!(lowerState.getBlock() instanceof DoorBlock) || lowerState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
			return null;
		}

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos lowerPosRelative = lowerPos.relative(direction);
			BlockState lowerStateRelative = level.getBlockState(lowerPosRelative);

			if (!(lowerStateRelative.getBlock() instanceof DoorBlock)) continue;

			BlockPos doubleLowerPos = lowerStateRelative.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? lowerPosRelative.below() : lowerPosRelative;
			lowerStateRelative = level.getBlockState(doubleLowerPos);

			if (doubleLowerPos.equals(lowerPos)) continue;

			if (lowerStateRelative.getBlock() instanceof DoorBlock && lowerStateRelative.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
				return doubleLowerPos;
			}
		}
		return null;
	}

	public static BlockPos getLowerDoor(Level level, BlockPos anyDoorPart) {
		BlockState doorState = level.getBlockState(anyDoorPart);

		if (!(doorState.getBlock() instanceof DoorBlock)) {
			return anyDoorPart;
		}

		BlockPos lower = doorState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? anyDoorPart.below() : anyDoorPart;
		BlockState lowerState = level.getBlockState(lower);
		if (!(lowerState.getBlock() instanceof DoorBlock) || lowerState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
			return lower;
		}

		BlockPos doubleLowerPos = getDoubleDoorLower(level, lower);
		if (doubleLowerPos == null) {
			return lower;
		}

		return lower.compareTo(doubleLowerPos) < 0 ? lower : doubleLowerPos;
	}

	public static void addDoorStandingLower(Level level, BlockPos anyDoorPart, Set<BlockPos> outsideLowerPositions) {
		BlockPos lowerPos = getLowerDoor(level, anyDoorPart);

		outsideLowerPositions.add(lowerPos);
		outsideLowerPositions.add(lowerPos.above());

		BlockPos doubleLowerPos = getDoubleDoorLower(level, lowerPos);

		if (doubleLowerPos != null) {
			outsideLowerPositions.add(doubleLowerPos);
			outsideLowerPositions.add(doubleLowerPos.above());
		}
	}

	private static boolean hasGreatDoorObstructions(BlockState state) {
		if (state.isAir()) return false;

		if (!state.getFluidState().isEmpty()) return true;

		if (state.is(BlockTags.LEAVES)) return true;
		if (state.is(BlockTags.SAPLINGS)) return true;
		if (state.is(BlockTags.REPLACEABLE)) return true;
		if (state.is(Blocks.SPORE_BLOSSOM)) return true;
		if (state.is(Blocks.SWEET_BERRY_BUSH)) return true;
		if (state.is(Blocks.CACTUS)) return true;
		if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.SMALL_FLOWERS)) return true;
		if (state.is(Blocks.BAMBOO) || state.is(Blocks.BAMBOO_SAPLING)) return true;
		if (state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK)) return true;
		if (state.is(Blocks.CHORUS_PLANT) || state.is(Blocks.CHORUS_FLOWER)) return true;
		if (state.is(Blocks.VINE) || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) return true;
		if (state.is(Blocks.MOSS_CARPET) || state.is(Blocks.AZALEA) || state.is(Blocks.FLOWERING_AZALEA)) return true;
		if (state.is(Blocks.BIG_DRIPLEAF) || state.is(Blocks.BIG_DRIPLEAF_STEM) || state.is(Blocks.SMALL_DRIPLEAF)) return true;
		if (state.is(BlockTags.LOGS) || state.is(BlockTags.LOGS_THAT_BURN) || state.is(BlockTags.CRIMSON_STEMS) || state.is(BlockTags.WARPED_STEMS)) return true;

		return false;
	}

	private static boolean isUnsuitableGreatDoorBase(ServerLevel level, BlockPos groundTop) {
		BlockState solid = level.getBlockState(groundTop);

		if (solid.isAir()) return true;
		if (!solid.getFluidState().isEmpty()) return true;
		if (solid.is(Blocks.BEDROCK)) return true;
		if (hasGreatDoorObstructions(solid)) return true;

		int minY = level.getMinBuildHeight();
		if (groundTop.getY() <= minY + 5) {
			return true;
		}

		return false;
	}

	private static int resolveGreatDoorLowerY(ServerLevel level, BlockPos fountainAnchor, int x, int z) {
		BlockPos doorPos = new BlockPos(x + 27, level.getMaxBuildHeight() - 1, z - 14);
		long chunk = ChunkPos.asLong(doorPos);

		level.setChunkForced(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), true);

		int y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, doorPos).getY();
		int minY = level.getMinBuildHeight() + 5;
		int steps = 64;

		while (steps-- > 0 && y > minY) {
			BlockPos test = new BlockPos(x, y, z);
			BlockState st = level.getBlockState(test);

			if (hasGreatDoorObstructions(st)) {
				y--;
				continue;
			}

			if (!isUnsuitableGreatDoorBase(level, test)) {
				level.setChunkForced(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false);
				return y;
			}
			y--;
		}

		y = fountainAnchor.getY();
		steps = 32;

		while (steps-- > 0 && y > minY) {
			BlockPos basePos = new BlockPos(x, y, z);
			BlockState baseState = level.getBlockState(basePos);

			if (hasGreatDoorObstructions(baseState)) {
				y--;
				continue;
			}

			if (!isUnsuitableGreatDoorBase(level, basePos)) {
				level.setChunkForced(ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false);
				return y;
			}
			y--;
		}

		return Integer.MIN_VALUE;
	}

	private static Optional<GreatDoorStructureResult> removeSpawnerAfterPlacing(ServerLevel level, StructureTemplate template,
																				StructurePlaceSettings settings, BlockPos origin) {
		BoundingBox boundingBox = template.getBoundingBox(settings, origin);
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		BlockPos foundPos = null;
		Direction facing = Direction.NORTH;

		for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
			for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
				for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
					mutablePos.set(x, y, z);
					BlockState currentState = level.getBlockState(mutablePos);

					if (currentState.is(BlockRegistry.GREAT_DOOR_SPAWNER.get())) {
						foundPos = mutablePos.immutable();
						facing = currentState.getValue(HorizontalDirectionalBlock.FACING);
						break;
					}
				}

				if (foundPos != null) break;

			}
			if (foundPos != null) break;

		}

		if (foundPos != null) {
			level.setBlock(foundPos, Blocks.AIR.defaultBlockState(), 3);

			return Optional.of(new GreatDoorStructureResult(foundPos, facing));
		}

		return Optional.empty();
	}

	private static Optional<GreatDoorStructureResult> placeGreatDoorStructure(ServerLevel level, StructureTemplate template, BlockPos origin, Rotation rot,
																			  RandomSource random) {
		StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rot).setMirror(Mirror.NONE).setIgnoreEntities(false);
		ChunkPos chunkPos = new ChunkPos(origin);

		level.setChunkForced(chunkPos.x, chunkPos.z, true);

		boolean isPlaced = template.placeInWorld(level, origin, origin, settings, random, 2);

		level.setChunkForced(chunkPos.x, chunkPos.z, false);

		if (!isPlaced) {
			return Optional.empty();
		}

		return removeSpawnerAfterPlacing(level, template, settings, origin);
	}

	public static void createGreatDoor(Level pLevel, BlockPos greatDoorPos, Direction direction, boolean isOpen, @Nullable BlockPos lightDoorPos,
									   @Nullable BlockPos lightDoorSecondLower, @Nullable ResourceKey<Level> lightDoorLevel,
									   @Nullable Direction lightDoorExitDirection, boolean isDestinationDarkWorld, @Nullable BlockPos destinationGreatDoorPos,
									   @Nullable ResourceKey<Level> destinationGreatDoorLevel) {
		GreatDoorCapability greatDoorCapability = null;
		LazyOptional<GreatDoorCapability> lightLazyCapability = pLevel.getCapability(CapabilityRegistry.GREAT_DOOR);
		if(lightLazyCapability.isPresent() && lightLazyCapability.resolve().isPresent())
			greatDoorCapability = lightLazyCapability.resolve().get();

		if (greatDoorCapability == null) return;


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

		greatDoorCapability.addGreatDoor(greatDoorPos, direction, isOpen, volumePositions, lightDoorPos, lightDoorSecondLower, lightDoorLevel, lightDoorExitDirection,
				isDestinationDarkWorld, destinationGreatDoorPos, destinationGreatDoorLevel);

		BlockState block = BlockRegistry.GREAT_DOOR_SHAPE.get().defaultBlockState();
		for (BlockPos target : volumePositions) {
			pLevel.setBlock(target, block, 3);

			if (pLevel.getBlockEntity(target) instanceof GreatDoorShapeBlockEntity greatDoorShape) {
				greatDoorShape.greatDoorPos = greatDoorPos;
			}
		}
	}

	public static Optional<GreatDoorStructureResult> tryPlaceGreatDoorStructure(ServerLevel level, BlockPos fountainPos, RandomSource random) {
		ResourceLocation structureLocation = new ResourceLocation(PenumbraPhantasm.MODID, "great_door");
		Optional<StructureTemplate> templateOptional = level.getStructureManager().get(structureLocation);

		if (templateOptional.isEmpty()) {
			return Optional.empty();
		}

		StructureTemplate template = templateOptional.get();
		int minR = Math.min(ServerConfig.greatDoorPlaceMinRadius, ServerConfig.greatDoorPlaceMaxRadius);
		int maxR = Math.max(ServerConfig.greatDoorPlaceMinRadius, ServerConfig.greatDoorPlaceMaxRadius);

		for (int attempt = 0; attempt < 48; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double dist = minR + random.nextDouble() * (maxR - minR);
			int x = fountainPos.getX() + Mth.floor(Mth.cos((float) angle) * dist);
			int z = fountainPos.getZ() + Mth.floor(Mth.sin((float) angle) * dist);

			Rotation facing = switch (random.nextInt(4)) {
				case 0 -> Rotation.NONE;
				case 1 -> Rotation.CLOCKWISE_90;
				case 2 -> Rotation.CLOCKWISE_180;
				default -> Rotation.COUNTERCLOCKWISE_90;
			};

			int xDoorOffset = 0;
			int zDoorOffset = 0;
			if(facing == Rotation.NONE) {
				xDoorOffset -= 14;
				zDoorOffset -= 27;
			}
			if(facing == Rotation.CLOCKWISE_90) {
				xDoorOffset += 27;
				zDoorOffset += 14;
			}
			if(facing == Rotation.CLOCKWISE_180) {
				xDoorOffset += 14;
				zDoorOffset += 27;
			}
			if(facing == Rotation.COUNTERCLOCKWISE_90) {
				xDoorOffset -= 27;
				zDoorOffset += 14;
			}

			int footY = resolveGreatDoorLowerY(level, fountainPos, x+xDoorOffset, z+zDoorOffset);

			if (footY == Integer.MIN_VALUE) continue;

            if (footY <= level.getMinBuildHeight()) continue;

			BlockPos origin = new BlockPos(x, footY, z);
			Optional<GreatDoorStructureResult> placed = placeGreatDoorStructure(level, template, origin, facing.getRotated(Rotation.CLOCKWISE_180), random);

			if (placed.isPresent()) {
				return placed;
			}
		}

		return Optional.empty();
	}

	public static void convertGreatDoorSpawnerInChunk(ServerLevel level, ChunkAccess chunk) {
		if (!isDarkWorld(level)) return;

		Optional<GreatDoorCapability> capOptional = level.getCapability(CapabilityRegistry.GREAT_DOOR).resolve();
		if (capOptional.isEmpty()) return;

		GreatDoorCapability greatDoorCapability = capOptional.get();
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		int minX = chunk.getPos().getMinBlockX();
		int maxX = chunk.getPos().getMaxBlockX();
		int minY = level.getMinBuildHeight();
		int maxY = level.getMaxBuildHeight();
		int minZ = chunk.getPos().getMinBlockZ();
		int maxZ = chunk.getPos().getMaxBlockZ();

		for (int x = minX; x <= maxX; x++) {
			int relativeX = x - minX;
			for (int z = minZ; z <= maxZ; z++) {
				int relativeZ = z - minZ;
				int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, relativeX, relativeZ);

				int yLow = Math.max(minY, surfaceY - 40);
				int yHigh = Math.min(maxY - 1, surfaceY + 24);
				for (int y = yLow; y <= yHigh; y++) {
					mutablePos.set(x, y, z);
					BlockState state = chunk.getBlockState(mutablePos);

					if (!state.is(BlockRegistry.GREAT_DOOR_SPAWNER.get())) continue;

					BlockPos potentialDoorPos = mutablePos.immutable();
					if (greatDoorCapability.greatDoors.containsKey(potentialDoorPos)) continue;

					Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
					level.setBlock(potentialDoorPos, Blocks.AIR.defaultBlockState(), 3);
					createGreatDoor(level, potentialDoorPos, facing, false, null, null, null, null,
							false, null, null);
				}
			}
		}
	}

	public static void bindUnlinkedGreatDoor(ServerLevel darkLevel, GreatDoor door) {
		if (!door.isUnlinkedForAutoBinding()) return;

		ResourceKey<Level> darkKey = darkLevel.dimension();
		MinecraftServer server = darkLevel.getServer();
		for (ServerLevel lightLevel : server.getAllLevels()) {
			if (isDarkWorld(lightLevel)) continue;

			Optional<DarkFountainCapability> capOptional = lightLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).resolve();
			if (capOptional.isEmpty()) continue;

			for (DarkFountain fountain : capOptional.get().darkFountains.values()) {
				if (!fountain.destinationDimension.equals(darkKey)) continue;

				if (bindSharedDoorsForUnlinked(darkLevel, door, lightLevel, fountain)) {
					return;
				}
				if (bindOutsideDoorForUnlinked(darkLevel, door, lightLevel, fountain)) {
					return;
				}
			}
		}
	}

	private static boolean isLightDoorPairTaken(ServerLevel darkLevel, BlockPos lower, @Nullable BlockPos doubleLower, ResourceKey<Level> lightKey) {
		return darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().map(c -> c.findByLightDoor(lower, lightKey) != null
						|| (doubleLower != null && c.findByLightDoor(doubleLower, lightKey) != null)).orElse(false);
	}

	private static boolean isValidOutsideDoorToGreatDoor(GreatDoor door) {
		return door.destinationGreatDoorPos == null && door.destinationGreatDoorDimension == null && !door.isDestinationDarkWorld;
	}

	public static boolean isLightDoorValidForFountain(ServerLevel darkLevel, BlockPos lightDoorAnyPart, ResourceKey<Level> lightDimension) {
		if (lightDoorAnyPart == null || lightDimension == null) return false;

		BlockPos darkFountainPos = findDarkFountainPos(darkLevel);
		if (darkFountainPos == null) return false;

		DarkFountain darkFountain = darkLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).map(cap -> cap.darkFountains
				.get(darkFountainPos)).orElse(null);
		if (darkFountain == null) return false;

		ResourceKey<Level> lightFountainKey = darkFountain.destinationDimension;
		BlockPos lightFountainPos = darkFountain.destinationPos;

		if (!lightFountainKey.equals(lightDimension)) return false;

		ServerLevel lightLevel = darkLevel.getServer().getLevel(lightFountainKey);
		if (lightLevel == null) return false;

		DarkFountain lightFountain = lightLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).map(cap -> cap.darkFountains
				.get(lightFountainPos)).orElse(null);
		if (lightFountain == null) return false;

		BlockPos canonicalLower = getLowerDoor(lightLevel, lightDoorAnyPart);

		for (DarkRoom room : lightFountain.rooms) {
			if (room.isDissipating()) continue;

			if (room.getOutsideDoors().containsKey(canonicalLower) || room.getSharedDoors().containsKey(canonicalLower)) {
				return true;
			}
		}

		return false;
	}

	private static boolean bindOutsideDoorForUnlinked(ServerLevel darkLevel, GreatDoor door, ServerLevel lightLevel, DarkFountain fountain) {
		for (DarkRoom room : fountain.rooms) {
			for (Map.Entry<BlockPos, DarkRoom.OutsideDoorExit> entry : room.getOutsideDoors().entrySet()) {
				BlockPos lowerPos = entry.getKey();
				DarkRoom.OutsideDoorExit exit = entry.getValue();
				BlockPos doubleLower = exit.doubleLowerHalf();

				if (isLightDoorPairTaken(darkLevel, lowerPos, doubleLower, lightLevel.dimension())) continue;

				door.lightDoorPos = lowerPos;
				door.lightDoorSecondLower = doubleLower;
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

	private static boolean bindSharedDoorsForUnlinked(ServerLevel darkLevel, GreatDoor door, ServerLevel lightLevel, DarkFountain fountain) {
		for (DarkRoom room : fountain.rooms) {
			for (Map.Entry<BlockPos, DarkRoom.SharedDoorLink> entry : room.getSharedDoors().entrySet()) {
				BlockPos lowerPos = entry.getKey();
				DarkRoom.SharedDoorLink sharedLink = entry.getValue();
				ResourceKey<Level> otherDarkWorldKey = sharedLink.otherDarkWorld();
				BlockPos doubleLower = sharedLink.doubleLowerHalf();

				if (!isDarkWorldKey(otherDarkWorldKey)) continue;

				if (isLightDoorPairTaken(darkLevel, lowerPos, doubleLower, lightLevel.dimension())) continue;

				ServerLevel otherLevel = darkLevel.getServer().getLevel(otherDarkWorldKey);
				if (otherLevel == null) continue;

				Direction exitDir = room.insideHorizontalDirectionTowardDoor(lowerPos).orElse(Direction.NORTH);
				door.lightDoorPos = lowerPos;
				door.lightDoorSecondLower = doubleLower;
				door.lightDoorDimension = lightLevel.dimension();
				door.lightDoorExitDirection = exitDir;
				door.isDestinationDarkWorld = true;
				door.destinationGreatDoorDimension = otherDarkWorldKey;
				door.destinationGreatDoorPos = null;

				ensureGreatDoorToGreatDoor(door, darkLevel, otherLevel);

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

	public static void ensureGreatDoorToGreatDoor(GreatDoor source, ServerLevel sourceLevel, ServerLevel destLevel) {
		if (!source.isDestinationDarkWorld || source.destinationGreatDoorDimension == null) return;

		if (source.lightDoorPos == null || source.lightDoorDimension == null || source.lightDoorExitDirection == null) return;

		if (!source.destinationGreatDoorDimension.equals(destLevel.dimension())) return;

		if (source.destinationGreatDoorPos != null) {
			GreatDoor existing = destLevel.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().map(cap -> cap.greatDoors
					.get(source.destinationGreatDoorPos)).orElse(null);

			if (existing != null) return;
		}

		BlockPos fountainPos = findDarkFountainPos(destLevel);
		if (fountainPos == null) return;

		Optional<GreatDoorStructureResult> placed = tryPlaceGreatDoorStructure(destLevel, fountainPos, destLevel.random);
		if (placed.isEmpty()) return;

		GreatDoorStructureResult structureResult = placed.get();

		createGreatDoor(destLevel, structureResult.anchorPos(), structureResult.facing(), true, source.lightDoorPos, source.lightDoorSecondLower,
				source.lightDoorDimension, source.lightDoorExitDirection, true, source.greatDoorPos, sourceLevel.dimension());
		source.destinationGreatDoorPos = structureResult.anchorPos();
		source.destinationGreatDoorDimension = destLevel.dimension();
	}

	@Nullable
	public static GreatDoor ensureGreatDoorForOutsideDoor(ServerLevel darkLevel, BlockPos darkFountainAnchor, BlockPos lightDoorLower,
														  ResourceKey<Level> lightDimension, Direction lightDoorExitFromInside,
														  @Nullable BlockPos lightDoorDoubleLower) {
		GreatDoor existingDoor = darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().map(cap -> cap
				.findByLightDoor(lightDoorLower, lightDimension)).orElse(null);

		if (existingDoor == null && lightDoorDoubleLower != null) {
			existingDoor = darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().map(cap -> cap
					.findByLightDoor(lightDoorDoubleLower, lightDimension)).orElse(null);
		}

		if (existingDoor != null) {
			if (!isValidOutsideDoorToGreatDoor(existingDoor)) {
				existingDoor.isDestinationDarkWorld = false;
				existingDoor.destinationGreatDoorPos = null;
				existingDoor.destinationGreatDoorDimension = null;
			}

			existingDoor.lightDoorPos = lightDoorLower;
			existingDoor.lightDoorSecondLower = lightDoorDoubleLower;
			existingDoor.lightDoorDimension = lightDimension;
			existingDoor.lightDoorExitDirection = lightDoorExitFromInside;
			existingDoor.broadcastSync(darkLevel);

			return existingDoor;
		}
		Optional<GreatDoorStructureResult> placed = tryPlaceGreatDoorStructure(darkLevel, darkFountainAnchor, darkLevel.random);
		if (placed.isEmpty()) return null;

		GreatDoorStructureResult structureResult = placed.get();
		createGreatDoor(darkLevel, structureResult.anchorPos(), structureResult.facing(), true, lightDoorLower, lightDoorDoubleLower, lightDimension,
				lightDoorExitFromInside, false, null, null);

		return darkLevel.getCapability(CapabilityRegistry.GREAT_DOOR).resolve().map(cap -> cap.greatDoors.get(structureResult.anchorPos()))
				.orElse(null);
	}

	public static boolean levelHasDarkFountain(ServerLevel level) {
		return level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).map(cap -> !cap.darkFountains.isEmpty()).orElse(false);
	}

	@Nullable
	public static BlockPos findDarkFountainPos(ServerLevel darkLevel) {
		if (!levelHasDarkFountain(darkLevel)) return null;

		return darkLevel.getCapability(CapabilityRegistry.DARK_FOUNTAIN).resolve().map(cap -> cap.darkFountains.keySet().iterator().next())
				.orElse(null);
	}

	public static TagKey<Block> getBlockTag(ResourceLocation location) {
		return TagKey.create(Registries.BLOCK, location);
	}

	public static Holder<NoiseGeneratorSettings> getNoiseGenerator(MinecraftServer server, ResourceLocation location) {
		ResourceKey<NoiseGeneratorSettings> key = ResourceKey.create(Registries.NOISE_SETTINGS, location);

		return server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(key);
	}

	public static ResourceKey<NoiseGeneratorSettings> getNoiseGeneratorKey(ResourceLocation location) {
		return ResourceKey.create(Registries.NOISE_SETTINGS, location);
	}

	public static Holder<DimensionType> getDimensionType(MinecraftServer server, ResourceLocation location) {
		ResourceKey<DimensionType> key = ResourceKey.create(Registries.DIMENSION_TYPE, location);

		return server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(key);
	}

	public static ServerLevel createDarkWorld(MinecraftServer server, BlockPos pos, ResourceKey<Level> originKey, DarkWorldType type) {
		ResourceLocation typeKey = server.registryAccess().registryOrThrow(DarkWorldType.REGISTRY_KEY).getKey(type);
		if(typeKey == null) return null;

		ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(PenumbraPhantasm.MODID,
						("dark_world_"+pos.asLong()+"_"+originKey.location()+"_"+typeKey).replace(':', '-')));

		ServerLevel existingLevel = server.getLevel(key);
		if (existingLevel != null) return existingLevel;


		long seed = createUniqueDarkWorldSeed(server, key);

		RandomState randomState = RandomState.create(server.registryAccess().asGetterLookup(), getNoiseGeneratorKey(type.noiseSettings()), seed);
		ChunkGenerator chunkGenerator = new SeededNoiseBasedChunkGenerator(type.source(),
				getNoiseGenerator(server, type.noiseSettings()), randomState, seed);

		LevelStem stem = new LevelStem(getDimensionType(server, type.dimensionType()), chunkGenerator);

		return InfiniverseAPI.get().getOrCreateLevel(server, key, () -> stem);
	}

	private static long createUniqueDarkWorldSeed(MinecraftServer server, ResourceKey<Level> levelKey) {
		long serverSeed = server.overworld().getSeed();
		UUID dimensionId = UUID.nameUUIDFromBytes(levelKey.location().toString().getBytes(StandardCharsets.UTF_8));
		long seed = dimensionId.getMostSignificantBits() ^ dimensionId.getLeastSignificantBits() ^ serverSeed;

		if(seed == 0L) seed = 1L;

		Set<Long> usedSeeds = new HashSet<>();
		for(ServerLevel level : getAllDarkWorlds(server)) {
			if(level.getChunkSource().getGenerator() instanceof SeededNoiseBasedChunkGenerator seededGenerator)
				usedSeeds.add(seededGenerator.getSeed());
		}

		while(usedSeeds.contains(seed)) {
			seed = Long.rotateLeft(seed ^ 0x9E3779B97F4A7C15L, 17);
			if(seed == 0L)
				seed = 1L;
		}

		return seed;
	}

	public static boolean isDarkWorld(Level level) {
		return isDarkWorldPath(level.dimension().location().getPath());
	}

	public static boolean isDarkWorldKey(ResourceKey<Level> levelResourceKey) {
		return isDarkWorldPath(levelResourceKey.location().getPath());
	}

	private static boolean isDarkWorldPath(String path) {
		return path.contains("dark_world") || path.contains("egg_room") || path.contains("depths");
	}

	public static List<ServerLevel> getAllDarkWorlds(MinecraftServer server) {
		List<ServerLevel> darkWorlds = new ArrayList<>();
		for(ServerLevel level : server.getAllLevels()) {
			if(isDarkWorldPath(level.dimension().location().getPath()))
				darkWorlds.add(level);
		}

		return darkWorlds;
	}

	public static final ResourceKey<Level> DEPTHS = ResourceKey.create(Registries.DIMENSION,
			new ResourceLocation(PenumbraPhantasm.MODID, "depths"));

	public static boolean isDepths(Level level) {
		return isDepthsKey(level.dimension());
	}

	public static boolean isDepthsKey(ResourceKey<Level> levelResourceKey) {
		return levelResourceKey.location().getPath().contains("depths");
	}

	@Nullable
	public static ServerLevel getDepths(MinecraftServer server) {
		return server.getLevel(DEPTHS);
	}

	public static List<ResourceLocation> getAllDarkWorldAllowedRecipes(RegistryAccess registryAccess) {
		List<ResourceLocation> locations = new ArrayList<>();
		Registry<DarkWorldRecipeSeparation> registry = registryAccess.registryOrThrow(DarkWorldRecipeSeparation.REGISTRY_KEY);

		for(Map.Entry<ResourceKey<DarkWorldRecipeSeparation>, DarkWorldRecipeSeparation> entry : registry.entrySet())
			locations.addAll(entry.getValue().darkWorldAllowed());

		return locations;
	}

	public static List<ResourceLocation> getAllDarkWorldBlockedRecipes(RegistryAccess registryAccess) {
		List<ResourceLocation> locations = new ArrayList<>();
		Registry<DarkWorldRecipeSeparation> registry = registryAccess.registryOrThrow(DarkWorldRecipeSeparation.REGISTRY_KEY);

		for(Map.Entry<ResourceKey<DarkWorldRecipeSeparation>, DarkWorldRecipeSeparation> entry : registry.entrySet())
			locations.addAll(entry.getValue().darkWorldBlocked());

		return locations;
	}

	public static boolean canUseRecipe(RegistryAccess registryAccess, ResourceLocation recipeID) {
		List<ResourceLocation> allowedRecipes = getAllDarkWorldAllowedRecipes(registryAccess);
		if(recipeID.getNamespace().equals(PenumbraPhantasm.MODID))
			allowedRecipes.add(recipeID);

		List<ResourceLocation> blockedRecipes = getAllDarkWorldBlockedRecipes(registryAccess);

		return allowedRecipes.contains(recipeID) && !blockedRecipes.contains(recipeID);
	}
}