package destiny.penumbra_phantasm.server.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.client.render.screen.DarknessFallScreen;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.FountainRenderUtil;
import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.client.sound.MusicManager;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import destiny.penumbra_phantasm.server.network.ClientboundPacketHandler;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
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
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(FOUNTAIN_BUFFER);

			GL11.glEnable(0x864F);

			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
				cap.darkFountains.forEach((key, fountain) ->
					{
						float animationTime = fountain.animationTimer;

						if (!DarkWorldUtil.isDarkWorld(level)) {
							stack.pushPose();
							stack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
							stack.translate(fountain.getFountainPos().getX(), fountain.getFountainPos().getY(),
									fountain.getFountainPos().getZ());

							if (animationTime < 130 && animationTime >= 0) {
								FountainRenderUtil.renderOpeningFoutain(partialTick, animationTime, length, textureCrack, stack, buffer,
										OverlayTexture.NO_OVERLAY);
							} else {
								double viewDistance = event.getLevelRenderer().getLastViewDistance();

								if (fountain.getFountainPos().getCenter().distanceTo(camera.getPosition()) < viewDistance * 16) {
									FountainRenderUtil.renderLightWorldOpenFountain(textureCrack, stack, buffer, OverlayTexture.NO_OVERLAY);
								}
							}
						}
						else
						{
							stack.pushPose();
							stack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
							stack.translate(fountain.getFountainPos().getX(), fountain.getFountainPos().getY(),
									fountain.getFountainPos().getZ());

							Vec2 fountain2dPos = new Vec2(fountain.getFountainPos().getX(), fountain.getFountainPos().getZ());
							Vec2 camera2dPos = new Vec2((float) camera.getPosition().x, (float) camera.getPosition().z);

							double distance2d = Mth.sqrt(fountain2dPos.distanceToSqr(camera2dPos));
							double referenceDistance = 64.0;
							float distanceScale = (float)(distance2d / referenceDistance);
							distanceScale = Math.max(distanceScale, 1.0f);

							stack.scale(distanceScale, distanceScale, distanceScale);

							float fade = (float)((distance2d - referenceDistance) / referenceDistance);
							fade = Math.max(0f, Math.min(1f, fade));

							if (fade < 1.0f) {
								FountainRenderUtil.renderOpenFountain(fountain, level, animationTime, length, textureCrack, partialTick, stack, buffer, OverlayTexture.NO_OVERLAY, 1.0f - fade);
							}
							if (fade > 0.0f) {
								FountainRenderUtil.renderOpenFountainOptimized(fountain, length, stack, buffer, OverlayTexture.NO_OVERLAY, fade);
							}
						}

						stack.popPose();
					});
			});

			buffer.endBatch();

			GL11.glDisable(0x864F);
		}
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			LocalPlayer player = Minecraft.getInstance().player;
			ClientLevel level = Minecraft.getInstance().level;
			if (player == null) return;
			if (level != null && DarkWorldUtil.isDarkWorld(level)) {
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
				if(entry.getValue().animationTimer == -1 && entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 64)
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
