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
}
