package destiny.penumbra_phantasm.server.registry;

import com.mojang.serialization.Codec;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.worldgen.SeededNoiseBasedChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ChunkGeneratorRegistry
{
	public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
			DeferredRegister.create(Registries.CHUNK_GENERATOR, PenumbraPhantasm.MODID);

	public static final RegistryObject<Codec<? extends ChunkGenerator>> SEEDED_GENERATOR =
			CHUNK_GENERATORS.register("seeded_chunk_generator", () -> SeededNoiseBasedChunkGenerator.CODEC);
}
