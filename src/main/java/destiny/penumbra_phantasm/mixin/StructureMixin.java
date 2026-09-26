package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.worldgen.SeededNoiseBasedChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Structure.class)
public class StructureMixin {
	@Inject(method = "isValidBiome", at = @At("RETURN"), cancellable = true)
	private static void isValidBiome(Structure.GenerationStub stub, Structure.GenerationContext context, CallbackInfoReturnable<Boolean> cir) {
		ChunkGenerator generator = context.chunkGenerator();
		BlockPos blockpos = stub.position();
		Climate.Sampler sampler = context.randomState().sampler();
		BiomeSource biomeSource = context.chunkGenerator().getBiomeSource();

		int quartX = QuartPos.fromBlock(blockpos.getX());
		int quartY = QuartPos.fromBlock(blockpos.getY());
		int quartZ = QuartPos.fromBlock(blockpos.getZ());

		if (generator instanceof SeededNoiseBasedChunkGenerator seededGenerator) {
			Climate.Sampler fixedSampler = seededGenerator.getOrCreateSampler(context.registryAccess());

			cir.setReturnValue(context.validBiome().test(biomeSource.getNoiseBiome(quartX, quartY, quartZ, fixedSampler)));
		} else {
			cir.setReturnValue(context.validBiome().test(biomeSource.getNoiseBiome(quartX, quartY, quartZ, sampler)));
		}
	}
}
