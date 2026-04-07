package destiny.penumbra_phantasm.server.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.client.ClientConfig;
import destiny.penumbra_phantasm.client.render.GreatDoorRenderUtil;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.FountainRenderUtil;
import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.client.sound.MusicManager;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

	private static final BufferBuilder FOUNTAIN_BUFFER = new BufferBuilder(65536);

	@SubscribeEvent
	public static void levelRender(RenderLevelStageEvent event)
	{
		boolean renderSkyPass = event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SKY);
		boolean renderShockwavePass = event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS);
		if(renderSkyPass || renderShockwavePass)
		{
			ClientLevel level = Minecraft.getInstance().level;
			if(level == null)
				return;

			float partialTick = event.getPartialTick();

			Camera camera = event.getCamera();

			int length = level.getHeight() / 6;
			ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

			PoseStack pose = event.getPoseStack();
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(FOUNTAIN_BUFFER);

			GL11.glEnable(0x864F);

			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
				cap.darkFountains.forEach((key, fountain) ->
				{
					float openingTick = fountain.getOpeningTick(partialTick);

					if (!DarkWorldUtil.isDarkWorld(level)) {
						pose.pushPose();
						pose.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
						pose.translate(fountain.getFountainPos().getX(), fountain.getFountainPos().getY(),
								fountain.getFountainPos().getZ());

						if (renderSkyPass) {
							if (openingTick < 130 && openingTick >= 0) {
								FountainRenderUtil.renderOpeningFoutain(openingTick, length, textureCrack, pose, buffer, OverlayTexture.NO_OVERLAY);
							} else {
								double viewDistance = event.getLevelRenderer().getLastViewDistance();

								if (fountain.getFountainPos().getCenter().distanceTo(camera.getPosition()) < viewDistance * 16) {
									FountainRenderUtil.renderLightWorldOpenFountain(textureCrack, pose, buffer, OverlayTexture.NO_OVERLAY);
								}
							}
						}

						if (renderShockwavePass) {
							FountainRenderUtil.renderShockwaves(fountain, pose, buffer, OverlayTexture.NO_OVERLAY, partialTick);
						}
						pose.popPose();
					}
					else if (renderSkyPass)
					{
						pose.pushPose();
						pose.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
						pose.translate(fountain.getFountainPos().getX(), fountain.getFountainPos().getY(),
								fountain.getFountainPos().getZ());

						Vec2 fountain2dPos = new Vec2(fountain.getFountainPos().getX(), fountain.getFountainPos().getZ());
						Vec2 camera2dPos = new Vec2((float) camera.getPosition().x, (float) camera.getPosition().z);

						double distance3d =fountain.getFountainPos().getCenter().distanceTo(camera.getPosition());
						double distance2d = Mth.sqrt(fountain2dPos.distanceToSqr(camera2dPos));
						double referenceDistance = 64;
						float distanceScale = (float)(distance2d / referenceDistance);
						distanceScale = Math.max(distanceScale, 1.0f);

						pose.scale(distanceScale, distanceScale, distanceScale);

						double fadeDistance = ClientConfig.fountainLodDistance;
						float fade = (float)((distance3d - fadeDistance) / fadeDistance);
						fade = Math.max(0f, Math.min(1f, fade));

						if (fade < 1.0f) {
							if (fountain.sealingTick >= 0) {
								FountainRenderUtil.renderSealingFountain(fountain, level, length, textureCrack, partialTick, pose, buffer, OverlayTexture.NO_OVERLAY, 1.0f - fade);
							} else {
								FountainRenderUtil.renderOpenFountain(fountain, level, length, textureCrack, partialTick, pose, buffer, OverlayTexture.NO_OVERLAY, 1.0f - fade);
							}
						}
						if (fade > 0.0f) {
							FountainRenderUtil.renderOpenFountainOptimized(fountain, length, pose, buffer, OverlayTexture.NO_OVERLAY, fade);
						}
						pose.popPose();
					}
				});
			});

			GL11.glDisable(0x864F);

			level.getCapability(CapabilityRegistry.GREAT_DOOR).ifPresent(cap -> {
				cap.greatDoors.forEach((key, greatDoor) -> {
					pose.pushPose();
					pose.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
					pose.translate(greatDoor.greatDoorPos.getX(), greatDoor.greatDoorPos.getY(),
							greatDoor.greatDoorPos.getZ());

					int packedLight = LevelRenderer.getLightColor(level, greatDoor.greatDoorPos);

					if (renderShockwavePass) {
						if (greatDoor.isOpen) {
							GreatDoorRenderUtil.renderOpenGreatDoor(greatDoor, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
						} else {
							GreatDoorRenderUtil.renderClosedGreatDoor(greatDoor, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
						}
					}
					pose.popPose();
				});
			});

			buffer.endBatch();
		}
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			LocalPlayer player = Minecraft.getInstance().player;
			ClientLevel level = Minecraft.getInstance().level;
			if (player == null) return;
			if (level == null) return;

			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
				cap.darkFountains.forEach((pos, fountain) -> fountain.clientTickOpening());
			});

			if (DarkWorldUtil.isDarkWorld(level)) {
				Minecraft.getInstance().getMusicManager().stopPlaying();
			}
			MusicManager.getInstance().tick();

			DarkFountainCapability cap;
			LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
			if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
				cap = lazyCapability.resolve().get();
			else return;

			DarkFountain fountain = null;

			for(Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet())
			{
				if(entry.getValue().openingTick == -1 && entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 64)
				{
					fountain = entry.getValue();
					break;
				}
			}

			if (fountain == null)
				return;

			double playerX = player.getX();
			double playerZ = player.getZ();

			boolean isInDarkWorld = player.level().dimension().equals(fountain.getDestinationDimension());

			double fountainX = isInDarkWorld ? fountain.getDestinationPos().getX() : fountain.getFountainPos().getX();
			double fountainZ = isInDarkWorld ? fountain.getDestinationPos().getZ() : fountain.getFountainPos().getZ();

			Vec2 flatPlayerPos = new Vec2((float)playerX, (float) playerZ);
			Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);
			float distance = flatPlayerPos.distanceToSqr(flatFountainPos);

			if (distance < Math.pow(16, 2)) {
				if (FountainRenderUtil.fountainHueAlpha != 1F) {
					FountainRenderUtil.fountainHueAlpha += 0.01F;
				}
			} else if (FountainRenderUtil.fountainHueAlpha != 0F) {
				FountainRenderUtil.fountainHueAlpha -= 0.01F;
			}
		}
	}

	@SubscribeEvent
	public static void pressKey(InputEvent.Key event)
	{
		if(Minecraft.getInstance().screen instanceof IntroScreen introScreen)
		{
			if(event.getAction() != 1 || !introScreen.isChoosing)
				return;

			if(InputConstants.getKey(InputConstants.KEY_W, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode())))
			{
				introScreen.incrementChoice(-1);
			}
			if(InputConstants.getKey(InputConstants.KEY_S, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode())))
			{
				introScreen.incrementChoice(1);
			}
			if(InputConstants.getKey(InputConstants.KEY_RETURN, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode())))
			{
				introScreen.pickChoice();
			}

			if(InputConstants.getKey(InputConstants.KEY_UP, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode())))
			{
				introScreen.incrementChoice(-1);
			}
			if(InputConstants.getKey(InputConstants.KEY_DOWN, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode())))
			{
				introScreen.incrementChoice(1);
			}
			if(InputConstants.getKey(InputConstants.KEY_Z, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode())))
			{
				introScreen.pickChoice();
			}
		}
	}
}