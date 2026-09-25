package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.event.ClientEvents;
import destiny.penumbra_phantasm.server.registry.FluidTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"), index = 3)
	private boolean penumbraPhantasm$eggRoomSkipOcclusion(boolean isSpectator) {
		Level level = Minecraft.getInstance().level;
		return isSpectator || (level != null && CardKingdomEggRoomUtil.isEggRoom(level));
	}

	@Inject(method = "blockChanged", at = @At(value = "HEAD"))
	private void penumbra_phantasm$block_changed(BlockGetter pLevel, BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags, CallbackInfo ci) {
		FluidState oldFluidState = pOldState.getFluidState();
		FluidState newFluidState = pNewState.getFluidState();

		if (!isNegativePhotonsFluid(oldFluidState) && !isNegativePhotonsFluid(newFluidState)) return;

		ChunkPos chunkPos = new ChunkPos(pPos);

		if (isNegativePhotonsFluid(oldFluidState) && !isNegativePhotonsFluid(newFluidState)) {
			ClientEvents.negativePhotons.get(chunkPos).remove(pPos);
		} else if (!isNegativePhotonsFluid(oldFluidState) && isNegativePhotonsFluid(newFluidState)) {
			ClientEvents.negativePhotons.get(chunkPos).add(pPos);
		}
	}

	@Unique
	private boolean isNegativePhotonsFluid(FluidState fluidState) {
		return fluidState.getFluidType() == FluidTypeRegistry.NEGATIVE_PHOTONS.get();
	}
}
