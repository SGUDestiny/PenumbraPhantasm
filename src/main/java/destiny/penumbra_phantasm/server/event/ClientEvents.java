package destiny.penumbra_phantasm.server.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.FountainRenderUtil;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
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

			Camera camera = event.getCamera();

			int length = level.getHeight() / 6;
			ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

			PoseStack stack = event.getPoseStack();
			MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
				cap.darkFountains.forEach((key, fountain) ->
					{
						stack.pushPose();
						stack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
						stack.translate(fountain.getFountainPos().getX(), fountain.getFountainPos().getY(),
								fountain.getFountainPos().getZ());

						float animTimeInitial = fountain.animationTimer;
						float animationTime = animTimeInitial + event.getPartialTick();

						if(animTimeInitial < 140 && animTimeInitial >= 0)
						{
							FountainRenderUtil.renderOpeningFoutain(animationTime, length, textureCrack, stack, buffer,
									OverlayTexture.NO_OVERLAY);
						} else
						{
							FountainRenderUtil.renderOpenFountain(fountain, level, animationTime, length, textureCrack,
									event.getPartialTick(), stack, buffer, OverlayTexture.NO_OVERLAY);
						}

						stack.popPose();
					});
			});
		}
	}
}
