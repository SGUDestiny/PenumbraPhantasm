package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.worldgen.SeededNoiseBasedChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Structure.class)
public class StructureMixin {
	@Redirect(
			method = "*",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/biome/BiomeSource;getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;"
			)
	)
	private static Holder<Biome> redirectDarkWorldStructureBiomeSample(BiomeSource biomeSource, int quartX, int quartY, int quartZ, Climate.Sampler sampler, Structure.GenerationStub stub, Structure.GenerationContext context) {
		ChunkGenerator generator = context.chunkGenerator();
		if (!(generator instanceof SeededNoiseBasedChunkGenerator seededGenerator)) {
			return biomeSource.getNoiseBiome(quartX, quartY, quartZ, sampler);
		}

		Climate.Sampler fixedSampler = seededGenerator.getOrCreateSampler(context.registryAccess());
		return biomeSource.getNoiseBiome(quartX, quartY, quartZ, fixedSampler);
	}
}
