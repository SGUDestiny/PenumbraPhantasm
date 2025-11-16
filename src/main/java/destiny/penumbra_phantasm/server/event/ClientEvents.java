package destiny.penumbra_phantasm.server.event;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.FountainRenderUtil;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void levelRender(RenderLevelStageEvent event)
	{
		if(event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SKY))
		{
			ClientLevel level = Minecraft.getInstance().level;
			if(level == null)
				return;

			float partialTick = event.getPartialTick();

			Camera camera = event.getCamera();

			int length = level.getHeight() / 6;
			ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

			PoseStack stack = event.getPoseStack();
			MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
				cap.darkFountains.forEach((key, fountain) ->
					{
						float animationTime = fountain.animationTimer;

						if (level.dimension() == fountain.getFountainDimension()) {
							stack.pushPose();
							stack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
							stack.translate(fountain.getFountainPos().getX(), fountain.getFountainPos().getY(),
									fountain.getFountainPos().getZ());

							if (animationTime < 140 && animationTime >= 0) {
								FountainRenderUtil.renderOpeningFoutain(partialTick, animationTime, length, textureCrack, stack, buffer,
										OverlayTexture.NO_OVERLAY);
							} else {
								double viewDistance = event.getLevelRenderer().getLastViewDistance();

								if (fountain.getFountainPos().getCenter().distanceTo(camera.getPosition()) < viewDistance * 16) {
									FountainRenderUtil.renderLightWorldOpenFountain(textureCrack, stack, buffer, OverlayTexture.NO_OVERLAY);
								}
							}
						}
						if (level.dimension() == fountain.getDestinationDimension()) {
							stack.pushPose();
							stack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
							stack.translate(fountain.getDestinationPos().getX(), fountain.getDestinationPos().getY(),
									fountain.getDestinationPos().getZ());

							if (animationTime < 140 && animationTime >= 0) {
								FountainRenderUtil.renderOpeningFoutain(partialTick, animationTime, length, textureCrack, stack, buffer,
										OverlayTexture.NO_OVERLAY);
							} else {
								double viewDistance = event.getLevelRenderer().getLastViewDistance();

								if (fountain.getDestinationPos().getCenter().distanceTo(camera.getPosition()) < viewDistance * 16) {
									FountainRenderUtil.renderOpenFountain(fountain, level, animationTime, length, textureCrack, partialTick, stack, buffer, OverlayTexture.NO_OVERLAY);
								} else {
									FountainRenderUtil.renderOpenFountainOptimized(fountain, partialTick, animationTime, length, textureCrack, stack, buffer, OverlayTexture.NO_OVERLAY);
								}
							}
						}

						stack.popPose();
					});
			});
		}
	}
}
