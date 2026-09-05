package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 500)
public abstract class CameraMixin {
	@Shadow
	private boolean detached;

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Shadow
	protected abstract void setPosition(double x, double y, double z);

	@Inject(method = "setup", at = @At("TAIL"))
	private void penumbraPhantasm$eggRoomCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
		if (!(entity instanceof Player player) || !CardKingdomEggRoomUtil.isEggRoom(player.level())) {
			return;
		}

		this.detached = true;
		double camX = CardKingdomEggRoomUtil.CAMERA_X;
		double camY = CardKingdomEggRoomUtil.CAMERA_Y;
		double camZ = CardKingdomEggRoomUtil.CAMERA_Z;
		double targetX = Mth.lerp(partialTick, player.xo, player.getX());
		double targetY = Mth.lerp(partialTick, player.yo, player.getY()) + player.getEyeHeight();
		double targetZ = Mth.lerp(partialTick, player.zo, player.getZ());

		Vec2 look = CardKingdomEggRoomUtil.cameraLook(camX, camY, camZ, targetX, targetY, targetZ);
		this.setPosition(camX, camY, camZ);
		this.setRotation(look.x, look.y);
	}
}
