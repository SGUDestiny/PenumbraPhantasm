package destiny.penumbra_phantasm.server.util;

import commoble.infiniverse.api.InfiniverseAPI;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.worldgen.SeededNoiseBasedChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.joml.Random;

import java.util.ArrayList;
import java.util.List;

public class DarkWorldUtil
{
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

		long seed = Random.newSeed();

		RandomState randomState = RandomState.create(server.registryAccess().asGetterLookup(), getNoiseGeneratorKey(type.noiseSettings()), seed);
		ChunkGenerator chunkGenerator = new SeededNoiseBasedChunkGenerator(type.source(),
				getNoiseGenerator(server, type.noiseSettings()), randomState, seed);

		LevelStem stem = new LevelStem(getDimensionType(server, type.dimensionType()), chunkGenerator);

		ServerLevel level = InfiniverseAPI.get().getOrCreateLevel(server, key, () -> stem);
		return level;
	}

	public static boolean isDarkWorld(Level level)
	{
		return level.dimension().location().getPath().contains("dark_world");
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
