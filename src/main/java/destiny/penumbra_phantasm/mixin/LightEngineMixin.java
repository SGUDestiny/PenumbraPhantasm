package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.client.light.SoulHearthLightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public abstract class LightEngineMixin {
    @Shadow
    @Final
    protected LightChunkGetter chunkSource;

    @Inject(method = "getLightValue(Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"), cancellable = true)
    private void penumbraPhantasm$soulHearthLightValue(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        BlockGetter blockGetter = this.chunkSource.getLevel();
        if (!(blockGetter instanceof Level level) || !level.isClientSide) {
            return;
        }

        if (!((Object) this instanceof BlockLightEngine)) {
            return;
        }

        int contribution = SoulHearthLightManager.getBlockLightContribution(level, pos);
        if (contribution > cir.getReturnValue()) {
            cir.setReturnValue(contribution);
        }
    }
}
