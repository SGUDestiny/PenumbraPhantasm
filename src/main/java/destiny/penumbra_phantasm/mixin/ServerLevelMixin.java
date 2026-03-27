package destiny.penumbra_phantasm.mixin;

import com.mojang.datafixers.util.Pair;
import destiny.penumbra_phantasm.server.worldgen.SeededNoiseBasedChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(method = "findClosestBiome3d", at = @At("HEAD"), cancellable = true)
	private void penumbraPhantasm$findClosestBiome3d(Predicate<Holder<Biome>> biomePredicate, BlockPos pos, int radius, int horizontalBlockCheckInterval, int verticalBlockCheckInterval, CallbackInfoReturnable<Pair<BlockPos, Holder<Biome>>> cir) {
		ServerLevel level = (ServerLevel)(Object)this;
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		if(generator instanceof SeededNoiseBasedChunkGenerator seededGenerator) {
			Climate.Sampler sampler = seededGenerator.getOrCreateSampler(level.registryAccess());
			cir.setReturnValue(generator.getBiomeSource().findClosestBiome3d(pos, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, biomePredicate, sampler, level));
		}
	}
}
