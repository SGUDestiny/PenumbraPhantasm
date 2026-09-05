package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.client.light.SoulHearthLightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {
    @Inject(method = "getEmission(JLnet/minecraft/world/level/block/state/BlockState;)I", at = @At("RETURN"), cancellable = true)
    private void penumbraPhantasm$soulHearthBlockEmission(long packedPos, BlockState state, CallbackInfoReturnable<Integer> cir) {
        LightChunkGetter chunkSource = ((LightEngineAccessor) this).penumbraPhantasm$getChunkSource();
        BlockGetter blockGetter = chunkSource.getLevel();

        if (!(blockGetter instanceof Level level)) return;

        int contribution = SoulHearthLightManager.getBlockLightContribution(level, BlockPos.of(packedPos));
        if (contribution > cir.getReturnValue()) {
            cir.setReturnValue(contribution);
        }
    }
}
