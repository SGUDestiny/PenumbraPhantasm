package destiny.penumbra_phantasm.server.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.dimension.CardKingdomDimensionEffects;
import destiny.penumbra_phantasm.client.ClientConfig;
import destiny.penumbra_phantasm.client.render.GreatDoorRenderUtil;
import destiny.penumbra_phantasm.client.render.fluid.NegativePhotonsRenderUtil;
import destiny.penumbra_phantasm.client.render.screen.DarkWorldInventoryScreen;
import destiny.penumbra_phantasm.client.render.screen.DarkWorldLanScreen;
import destiny.penumbra_phantasm.client.render.screen.DarkWorldPauseScreen;
import destiny.penumbra_phantasm.client.render.screen.EggRoomCoverScreen;
import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.client.KeyBindings;
import destiny.penumbra_phantasm.client.render.textbox.DarkWorldDialogue;
import destiny.penumbra_phantasm.client.render.tooltip.DarkMoneyTooltipComponent;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.item.DarkWallerItem;
import destiny.penumbra_phantasm.server.network.ServerBoundEggRoomInteractPacket;
import destiny.penumbra_phantasm.server.registry.FluidTypeRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.lwjgl.opengl.GL11;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.fountain.FountainRenderUtil;
import destiny.penumbra_phantasm.client.render.fountain.DepthsFountainSwirls;
import destiny.penumbra_phantasm.client.sound.MusicManager;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
	private static final BufferBuilder FOUNTAIN_BUFFER = new BufferBuilder(65536);

	private static final ResourceLocation DARK_WORLD_WIDGETS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets.png");
	private static final ResourceLocation DARK_WORLD_ICONS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/icons_1.png");
	private static final ResourceLocation DARK_WORLD_HOTBAR = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/hotbar.png");
	private static final ResourceLocation DARK_WORLD_HOTBAR_GLOW = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/hotbar_glow.png");

	private static int lastHealth = -1;
	private static int displayHealth = -1;
	private static long lastHealthTime = 0L;
	private static long healthBlinkTime = 0L;
	private static final Random random = new Random();
	private static ResourceKey<Level> lastClientDim;
	private static int eggRoomRebuildLeft;

	public static Map<ChunkPos, Set<BlockPos>> negativePhotons = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void eggRoomCameraAngles(ViewportEvent.ComputeCameraAngles event) {
		LocalPlayer player = Minecraft.getInstance().player;

		if(player == null || !CardKingdomEggRoomUtil.isEggRoom(player.level())) return;

		float partialTick = (float) event.getPartialTick();
		double targetX = Mth.lerp(partialTick, player.xo, player.getX());
		double targetY = Mth.lerp(partialTick, player.yo, player.getY()) + player.getEyeHeight();
		double targetZ = Mth.lerp(partialTick, player.zo, player.getZ());

		Vec2 look = CardKingdomEggRoomUtil.cameraLook(CardKingdomEggRoomUtil.CAMERA_X, CardKingdomEggRoomUtil.CAMERA_Y, CardKingdomEggRoomUtil.CAMERA_Z,
				targetX, targetY, targetZ);
		event.setYaw(look.x);
		event.setPitch(look.y);
		event.setRoll(0f);
	}

	@SubscribeEvent
	public static void levelRender(RenderLevelStageEvent event) {
		boolean renderSkyPass = event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SKY);
		boolean renderShockwavePass = event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS);
		if(renderSkyPass || renderShockwavePass) {
			ClientLevel level = Minecraft.getInstance().level;

			if(level == null) return;

			float partialTick = event.getPartialTick();

			Camera camera = event.getCamera();

			int length = 16;
			ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

			PoseStack pose = event.getPoseStack();
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(FOUNTAIN_BUFFER);

			GL11.glEnable(GL_DEPTH_CLAMP);

			if(renderSkyPass && CardKingdomDimensionEffects.isCardKingdomDarkWorld(level)) {
				CardKingdomDimensionEffects cardKingdomDimensionEffects = CardKingdomDimensionEffects.getInstance();

				if(cardKingdomDimensionEffects != null) {
					pose.pushPose();
					cardKingdomDimensionEffects.renderOverlay(level, partialTick, pose, camera, event.getProjectionMatrix());
					pose.popPose();
				}
			}

			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
				cap.darkFountains.forEach((key, fountain) -> {
					float openingTick = fountain.getOpeningTick(partialTick);

					if(!DarkWorldUtil.isDarkWorld(level)) {
						pose.pushPose();
						pose.translate(fountain.getFountainPos().getX() - camera.getPosition().x(),
								fountain.getFountainPos().getY() - camera.getPosition().y(),
								fountain.getFountainPos().getZ() - camera.getPosition().z());

						if(renderSkyPass) {
							if(openingTick < FountainRenderUtil.OPENING_POSTERIZE_TICK_END && openingTick >= 0) {
								FountainRenderUtil.renderOpeningFoutain(openingTick, length, textureCrack, pose, buffer, OverlayTexture.NO_OVERLAY);
							} else {
								double viewDistance = event.getLevelRenderer().getLastViewDistance();

								if(fountain.getFountainPos().getCenter().distanceTo(camera.getPosition()) < viewDistance * 16) {
									FountainRenderUtil.renderLightWorldOpenFountain(textureCrack, pose, buffer, OverlayTexture.NO_OVERLAY);
								}
							}
						}

						if(renderShockwavePass) {
							FountainRenderUtil.renderShockwaves(fountain, pose, buffer, OverlayTexture.NO_OVERLAY, partialTick);
						}

						pose.popPose();
					} else if(DarkWorldUtil.isDepths(level)) {
						pose.pushPose();
						pose.translate(fountain.getFountainPos().getX() - camera.getPosition().x(),
								fountain.getFountainPos().getY() - camera.getPosition().y(),
								fountain.getFountainPos().getZ() - camera.getPosition().z());

						Vec2 fountain2dPos = new Vec2(fountain.getFountainPos().getX(), fountain.getFountainPos().getZ());
						Vec2 camera2dPos = new Vec2((float) camera.getPosition().x, (float) camera.getPosition().z);

						double distance2d = Mth.sqrt(fountain2dPos.distanceToSqr(camera2dPos));
						double referenceDistance = 128;
						float distanceScale = (float) (distance2d / referenceDistance);
						distanceScale = Math.max(distanceScale, 1);

						pose.scale(distanceScale, distanceScale, distanceScale);

						if(renderSkyPass) {
							double fadeDistance = ClientConfig.fountainLodDistance;
							float fade = (float) ((distance2d - fadeDistance) / fadeDistance);
							fade = Math.max(0f, Math.min(1f, fade));
							FountainRenderUtil.renderDepthsFountain(fountain, pose, buffer, camera, distance2d, partialTick, fade);
						}

						if(renderShockwavePass) {
							FountainRenderUtil.renderDepthsFountainBeam(fountain, pose, buffer, camera, distance2d);
						}

						pose.popPose();
					} else if(renderSkyPass) {
						pose.pushPose();
						pose.translate(fountain.getFountainPos().getX() - camera.getPosition().x(),
								fountain.getFountainPos().getY() - camera.getPosition().y(),
								fountain.getFountainPos().getZ() - camera.getPosition().z());

						Vec2 fountain2dPos = new Vec2(fountain.getFountainPos().getX(), fountain.getFountainPos().getZ());
						Vec2 camera2dPos = new Vec2((float) camera.getPosition().x, (float) camera.getPosition().z);

						double distance3d = fountain.getFountainPos().getCenter().distanceTo(camera.getPosition());
						double distance2d = Mth.sqrt(fountain2dPos.distanceToSqr(camera2dPos));
						double referenceDistance = 64;
						float distanceScale = (float) (distance2d / referenceDistance);

						distanceScale = Math.max(distanceScale, 1);

						pose.scale(distanceScale, distanceScale, distanceScale);

						double fadeDistance = ClientConfig.fountainLodDistance;
						float fade = (float) ((distance3d - fadeDistance) / fadeDistance);
						fade = Math.max(0, Math.min(1, fade));

						if(fade < 1) {
							if(fountain.sealingTick >= 0) {
								FountainRenderUtil.renderSealingFountain(fountain, level, length, textureCrack, partialTick, pose, buffer,
										OverlayTexture.NO_OVERLAY, 1 - fade);
							} else {
								FountainRenderUtil.renderOpenFountain(fountain, level, length, textureCrack, partialTick, pose, buffer,
										OverlayTexture.NO_OVERLAY, 1 - fade);
							}
						}

						if(fade > 0) {
							FountainRenderUtil.renderOpenFountainOptimized(fountain, length, pose, buffer, OverlayTexture.NO_OVERLAY, fade, camera.getPosition());
						}

						pose.popPose();
					}
				});
			});

			level.getCapability(CapabilityRegistry.GREAT_DOOR).ifPresent(cap -> {
				for(GreatDoor greatDoor : new ArrayList<>(cap.greatDoors.values())) {
					pose.pushPose();
					pose.translate((double) greatDoor.greatDoorPos.getX() - camera.getPosition().x(),
							(double) greatDoor.greatDoorPos.getY() - camera.getPosition().y(),
							(double) greatDoor.greatDoorPos.getZ() - camera.getPosition().z());

					if(Minecraft.getInstance().level.isLoaded(greatDoor.greatDoorPos)) {
						int packedLight = LevelRenderer.getLightColor(level, greatDoor.greatDoorPos);

						if(renderShockwavePass) {
							if(greatDoor.isOpen) {
								GreatDoorRenderUtil.renderOpenGreatDoor(greatDoor, pose, buffer, packedLight,
										OverlayTexture.NO_OVERLAY);
							} else {
								GreatDoorRenderUtil.renderClosedGreatDoor(greatDoor, pose, buffer, packedLight,
										OverlayTexture.NO_OVERLAY);
							}
						}
					}

					pose.popPose();
				}
			});

			if(renderShockwavePass) {
				NegativePhotonsRenderUtil.renderNegativePhotonsBlocks(Minecraft.getInstance().level, buffer, camera, pose, negativePhotons);
			}

			buffer.endBatch();

			GL11.glDisable(GL_DEPTH_CLAMP);
		}
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event) {
		if(!event.getLevel().isClientSide()) return;
		if(!(event.getLevel() instanceof ClientLevel level)) return;

		ChunkAccess chunk = event.getChunk();

		if(chunk == null) return;

		ChunkPos chunkPos = chunk.getPos();
		int chunkMinX = chunkPos.getMinBlockX();
		int chunkMinZ = chunkPos.getMinBlockZ();

		Set<BlockPos> negativePhotonsSet = new HashSet<>();
		for(int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
			LevelChunkSection section = chunk.getSection(sectionIndex);

			if(section.hasOnlyAir()) continue;
			if(!section.maybeHas(state -> state.getFluidState().getFluidType() == FluidTypeRegistry.NEGATIVE_PHOTONS.get()))
				continue;

			int sectionChunkY = chunk.getSectionYFromSectionIndex(sectionIndex);
			int sectionMinY = SectionPos.sectionToBlockCoord(sectionChunkY);

			for(int sectionX = 0; sectionX < 16; sectionX++) {
				for(int sectionY = 0; sectionY < 16; sectionY++) {
					for(int sectionZ = 0; sectionZ < 16; sectionZ++) {
						FluidState fluidState = section.getFluidState(sectionX, sectionY, sectionZ);

						if(fluidState.getFluidType() != FluidTypeRegistry.NEGATIVE_PHOTONS.get()) continue;

						BlockPos negativePhotonsPos = new BlockPos(chunkMinX + sectionX, sectionMinY + sectionY, chunkMinZ + sectionZ);
						negativePhotonsSet.add(negativePhotonsPos);
					}
				}
			}
		}

		negativePhotons.put(chunkPos, negativePhotonsSet);

		if(CardKingdomEggRoomUtil.isEggRoom(level)) {
			Minecraft minecraft = Minecraft.getInstance();
			int minY = level.getMinSection();
			int maxY = level.getMaxSection();

			for(int y = minY; y < maxY; y++) {
				minecraft.levelRenderer.setSectionDirty(chunkPos.x, y, chunkPos.z);
			}
		}
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event) {
		if(!event.getLevel().isClientSide()) return;
		if(!(event.getLevel() instanceof ClientLevel level)) return;

		ChunkAccess chunk = event.getChunk();
		ChunkPos chunkPos = chunk.getPos();

		negativePhotons.remove(chunkPos);
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			IntroScreen.tickWorldThumbnail(Minecraft.getInstance());
			MusicManager.getInstance().tick();
			Minecraft minecraft = Minecraft.getInstance();

			if(EggRoomCoverScreen.isCovering() && minecraft.screen instanceof ReceivingLevelScreen) {
				Screen cover = EggRoomCoverScreen.replaceLoadingScreen();

				if(cover != null) {
					minecraft.setScreen(cover);
				}
			}

			LocalPlayer player = minecraft.player;
			ClientLevel level = minecraft.level;
			if(player == null || level == null) {
				DarkWorldDialogue.stop();
				lastClientDim = null;
				return;
			}

			if(lastClientDim == null || !lastClientDim.equals(level.dimension())) {
				if(lastClientDim != null) {
					DarkWorldDialogue.stop();
				}

				lastClientDim = level.dimension();
				eggRoomRebuildLeft = CardKingdomEggRoomUtil.isEggRoom(level) ? 20 : 0;

				if(eggRoomRebuildLeft > 0) {
					Minecraft.getInstance().levelRenderer.allChanged();
				}
			}

			//Tick current dialogue
			DarkWorldDialogue.tick();

			//Rebuild egg room chunks to not have unloaded chunks
			if(CardKingdomEggRoomUtil.isEggRoom(level)) {
				player.setXRot(0f);
				player.xRotO = 0f;
				player.setSprinting(false);
				player.fallDistance = 0f;

				if(eggRoomRebuildLeft > 0) {
					eggRoomRebuildLeft--;

					if(eggRoomRebuildLeft == 15 || eggRoomRebuildLeft == 10 || eggRoomRebuildLeft == 5 || eggRoomRebuildLeft == 0) {
						Minecraft.getInstance().levelRenderer.allChanged();
					}
				}
			}

			//Stop swirls from swirling if paused
			if(!Minecraft.getInstance().isPaused()) {
				level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
					cap.darkFountains.forEach((pos, fountain) -> fountain.clientTickOpening());

					if(DarkWorldUtil.isDepths(level)) {
						DepthsFountainSwirls.tick(level, cap);
					}
				});
			}

			//Stop vanilla music in dark worlds
			if(DarkWorldUtil.isDarkWorld(level)) {
				Minecraft.getInstance().getMusicManager().stopPlaying();
			}

			//Cancel if in the depths
			if(DarkWorldUtil.isDepths(level)) {
				return;
			}

			//DW fountain hue and alpha beyond this point
			DarkFountainCapability cap;
			LazyOptional<DarkFountainCapability> lazyCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
			if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent()) {
				cap = lazyCapability.resolve().get();
			} else return;

			DarkFountain fountain = null;

			for(Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet()) {
				if(entry.getValue().openingTick == -1 && entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 64) {
					fountain = entry.getValue();
					break;
				}
			}

			if(fountain == null) return;

			double playerX = player.getX();
			double playerZ = player.getZ();

			boolean isInDarkWorld = player.level().dimension().equals(fountain.getDestinationDimension());

			double fountainX = isInDarkWorld ? fountain.getDestinationPos().getX() : fountain.getFountainPos().getX();
			double fountainZ = isInDarkWorld ? fountain.getDestinationPos().getZ() : fountain.getFountainPos().getZ();

			Vec2 flatPlayerPos = new Vec2((float) playerX, (float) playerZ);
			Vec2 flatFountainPos = new Vec2((float) fountainX, (float) fountainZ);
			float distance = flatPlayerPos.distanceToSqr(flatFountainPos);

			if(distance < Math.pow(16, 2)) {
				if(FountainRenderUtil.fountainHueAlpha != 1) {
					FountainRenderUtil.fountainHueAlpha += 0.01f;
				}
			} else if(FountainRenderUtil.fountainHueAlpha != 0) {
				FountainRenderUtil.fountainHueAlpha -= 0.01f;
			}
		}
	}

	@SubscribeEvent
	public static void pressKey(InputEvent.Key event) {
		if(DarkWorldDialogue.isActive()) {
			if(DarkWorldDialogue.handleKey(event.getKey(), event.getAction()) && event.isCancelable()) {
				event.setCanceled(true);
			}
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();

		if(event.getAction() == InputConstants.PRESS && KeyBindings.isConfirmKey(event.getKey()) && minecraft.screen == null && minecraft.player != null
				&& CardKingdomEggRoomUtil.isEggRoom(minecraft.player.level())) {
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundEggRoomInteractPacket());

			if(event.isCancelable()) {
				event.setCanceled(true);
			}

			return;
		}

		if(minecraft.screen instanceof IntroScreen introScreen) {
			if(event.getAction() != 1 || !introScreen.isChoosing) return;

			if(InputConstants.getKey(InputConstants.KEY_W, event.getScanCode()).equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(-1);
			}

			if(InputConstants.getKey(InputConstants.KEY_S, event.getScanCode()).equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(1);
			}

			if(InputConstants.getKey(InputConstants.KEY_RETURN, event.getScanCode()).equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.pickChoice();
			}

			if(InputConstants.getKey(InputConstants.KEY_UP, event.getScanCode()).equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(-1);
			}

			if(InputConstants.getKey(InputConstants.KEY_DOWN, event.getScanCode()).equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(1);
			}

			if(InputConstants.getKey(InputConstants.KEY_Z, event.getScanCode()).equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.pickChoice();
			}
		}
	}

	@SubscribeEvent
	public static void movementInput(MovementInputUpdateEvent event) {
		DarkWorldDialogue.applyChoiceMovement(event.getInput());

		if(DarkWorldDialogue.shouldBlockSneak()) {
			event.getInput().shiftKeyDown = false;
		}

		if(!(event.getEntity() instanceof LocalPlayer player) || !CardKingdomEggRoomUtil.isEggRoom(player.level())) return;

		player.setXRot(0f);
		player.xRotO = 0f;

		if(DarkWorldDialogue.isChoosing()) return;

		Input input = event.getInput();
		Vec2 away = CardKingdomEggRoomUtil.cameraAwayFlat(player.getX(), player.getZ());
		Vec2 wish = CardKingdomEggRoomUtil.worldWish(away, input.forwardImpulse, input.leftImpulse);

		if(wish.x * wish.x + wish.y * wish.y > 1.0E-6f) {
			float targetYaw = CardKingdomEggRoomUtil.yawFromWish(wish.x, wish.y);
			float yaw = Mth.rotLerp(CardKingdomEggRoomUtil.LOOK_SMOOTH, player.getYRot(), targetYaw);

			player.setYRot(yaw);
			player.setYHeadRot(yaw);
			player.yBodyRot = yaw;
		}

		Vec2 local = CardKingdomEggRoomUtil.worldToLocal(wish, player.getYRot());
		input.leftImpulse = local.x;
		input.forwardImpulse = local.y;
	}

	@SubscribeEvent
	public static void onScreenOpen(ScreenEvent.Opening event) {
		Screen newScreen = event.getNewScreen();
		if(newScreen != null) {
			DarkWorldDialogue.stop();
		}

		if(newScreen instanceof ReceivingLevelScreen) {
			Screen cover = EggRoomCoverScreen.replaceLoadingScreen();
			if(cover != null) {
				event.setNewScreen(cover);
				return;
			}
		}

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if(player == null) return;

		if(DarkWorldUtil.isDarkWorld(player.level())) {
			if(newScreen instanceof InventoryScreen) {
				event.setNewScreen(new DarkWorldInventoryScreen(player));
			}
			if(newScreen instanceof PauseScreen) {
				event.setNewScreen(new DarkWorldPauseScreen(true));
			}
			if(newScreen instanceof ShareToLanScreen) {
				event.setNewScreen(new DarkWorldLanScreen(event.getCurrentScreen()));
			}
		}
	}

	@SubscribeEvent
	public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();

		if(mc.level == null || mc.player == null) return;
		if(!DarkWorldUtil.isDarkWorld(mc.level)) return;

		Player player = mc.player;
		NamedGuiOverlay overlay = event.getOverlay();
		GuiGraphics gui = event.getGuiGraphics();
		Window window = event.getWindow();

		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		if(overlay == VanillaGuiOverlay.FOOD_LEVEL.type()) {
			event.setCanceled(true);
		}
		if(overlay == VanillaGuiOverlay.ARMOR_LEVEL.type()) {
			event.setCanceled(true);
		}
		if(overlay == VanillaGuiOverlay.MOUNT_HEALTH.type()) {
			event.setCanceled(true);
		}
		if(overlay == VanillaGuiOverlay.AIR_LEVEL.type()) {
			event.setCanceled(true);
		}

		if(overlay == VanillaGuiOverlay.CROSSHAIR.type()) {
			event.setCanceled(true);
			renderDarkWorldCrosshair(gui, window, mc);
		} else if(overlay == VanillaGuiOverlay.PLAYER_HEALTH.type()) {
			if(DarkWorldDialogue.shouldHideHud()) {
				event.setCanceled(true);
				return;
			}

			SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
			int soulType = soulCap.soulType;

			event.setCanceled(true);
			renderDarkWorldHealth(mc, gui, window, mc.player, soulType);
		} else if(overlay == VanillaGuiOverlay.HOTBAR.type()) {
			if(DarkWorldDialogue.shouldHideHud()) {
				event.setCanceled(true);
				return;
			}

			event.setCanceled(true);
			renderDarkWorldHotbar(mc, gui, window, mc.player);
		} else if(overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
			if(DarkWorldDialogue.shouldHideHud()) {
				event.setCanceled(true);
				return;
			}

			PlayerRideableJumping playerrideablejumping = mc.player.jumpableVehicle();

			if(playerrideablejumping == null && mc.gameMode.hasExperience()) {
				int i = screenWidth / 2 - 91;

				event.setCanceled(true);
				renderDarkWorldExperienceBar(gui, i, mc, window);
			}
		}
	}

	private static void renderDarkWorldHotbar(Minecraft mc, GuiGraphics gui, Window window, Player player) {
		if(mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			mc.gui.getSpectatorGui().renderHotbar(gui);
			return;
		}

		if(mc.options.hideGui) return;

		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		int x = screenWidth / 2 - 182 / 2;
		int y = screenHeight - 22 - 12;

		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR);
		gui.blit(DARK_WORLD_HOTBAR, x, y, 0, 0, 182, 38);

		for(int slot = 0; slot < 9; slot++) {
			ItemStack stack = player.getInventory().items.get(slot);
			if(!stack.isEmpty()) {
				int slotX = x + slot * 18;
				gui.renderItem(stack, slotX + 11, y + 11);
				gui.renderItemDecorations(mc.font, stack, slotX + 11, y + 11);
			}
		}

		int selected = player.getInventory().selected;
		int selectedSlotX = x + selected * 18;
		long period = 20 * 5;
		long elapsed = mc.level.getGameTime() % period;
		float t = (float) elapsed / period;
		float glow = Mth.sin(t * Mth.PI);

		if(DarkWorldUtil.isDepths(player.level())) {
			glow = 0;
		}

		HumanoidArm humanoidarm = player.getMainArm().getOpposite();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR);
		gui.blit(DARK_WORLD_HOTBAR, selectedSlotX, y, 182, 0, 38, 38);
		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR_GLOW);
		RenderBlitUtil.blitGui(gui, DARK_WORLD_HOTBAR_GLOW, selectedSlotX, y, 182, 0, 38, 38, glow, glow, glow, 1);

		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR);
		ItemStack offhand = player.getOffhandItem();

		if(!offhand.isEmpty()) {
			int offX = x - 29;
			int offY = y + 8;
			gui.blit(DARK_WORLD_HOTBAR, offX + 2, offY + 2, 223, 15, 18, 18);
			gui.renderItem(offhand, offX + 3, offY + 3);
			gui.renderItemDecorations(mc.font, offhand, offX + 3, offY + 3);
		}

		RenderSystem.enableBlend();
		if(mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
			float f = mc.player.getAttackStrengthScale(0);

			if(f < 1) {
				int j2 = screenHeight - 20;
				int i = screenWidth / 2;
				int k2 = i + 91 + 6;

				if(humanoidarm == HumanoidArm.RIGHT) {
					k2 = i - 91 - 22;
				}

				int l1 = (int) (f * 19.0F);
				gui.blit(DARK_WORLD_ICONS, k2, j2, 0, 94, 18, 18);
				gui.blit(DARK_WORLD_ICONS, k2, j2 + 18 - l1, 18, 112 - l1, 18, l1);
			}
		}

		RenderSystem.disableBlend();
	}

	private static void renderDarkWorldCrosshair(GuiGraphics gui, Window window, Minecraft minecraft) {
		if(minecraft.options.hideGui) return;

		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		Options options = minecraft.options;
		if(options.getCameraType().isFirstPerson() && (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || canRenderCrosshairForSpectator(
				minecraft.hitResult, minecraft))) {
			if(options.renderDebug && !options.hideGui && !minecraft.player.isReducedDebugInfo() && !(Boolean) options.reducedDebugInfo().get()) {
				Camera camera = minecraft.gameRenderer.getMainCamera();
				PoseStack posestack = RenderSystem.getModelViewStack();

				posestack.pushPose();

				posestack.mulPoseMatrix(gui.pose().last().pose());
				posestack.translate((float) (screenWidth / 2), (float) (screenHeight / 2), 0);
				posestack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
				posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));

				posestack.scale(-1, -1, -1);

				RenderSystem.applyModelViewMatrix();
				RenderSystem.renderCrosshair(10);

				posestack.popPose();

				RenderSystem.applyModelViewMatrix();
			} else {
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
						GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

				gui.blit(DARK_WORLD_ICONS, (screenWidth - 15) / 2, (screenHeight - 15) / 2, 0, 0, 15, 15);

				if(minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
					float f = minecraft.player.getAttackStrengthScale(0);
					boolean flag = false;

					if(minecraft.crosshairPickEntity != null && minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1) {
						flag = minecraft.player.getCurrentItemAttackStrengthDelay() > 5;
						flag &= minecraft.crosshairPickEntity.isAlive();
					}

					int j = screenHeight / 2 - 7 + 16;
					int k = screenWidth / 2 - 8;

					if(flag) {
						gui.blit(DARK_WORLD_ICONS, k, j, 68, 94, 16, 16);
					} else if(f < 1.0F) {
						int l = (int) (f * 17.0F);

						gui.blit(DARK_WORLD_ICONS, k, j, 36, 94, 16, 4);
						gui.blit(DARK_WORLD_ICONS, k, j, 52, 94, l, 4);
					}
				}
				RenderSystem.defaultBlendFunc();
			}
		}
	}

	private static boolean canRenderCrosshairForSpectator(HitResult pRayTrace, Minecraft minecraft) {
		if(pRayTrace == null) {
			return false;
		} else if(pRayTrace.getType() == HitResult.Type.ENTITY) {
			return ((EntityHitResult) pRayTrace).getEntity() instanceof MenuProvider;
		} else if(pRayTrace.getType() == HitResult.Type.BLOCK) {
			BlockPos blockpos = ((BlockHitResult) pRayTrace).getBlockPos();
			Level level = minecraft.level;

			return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
		} else {
			return false;
		}
	}

	private static void renderDarkWorldHealth(Minecraft mc, GuiGraphics gui, Window window, Player player, int soulType) {
		if(player == null) return;

		if(!mc.gameMode.canHurtPlayer()) return;

		ResourceLocation iconsTexture = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/icons_" + soulType + ".png");
		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();
		int currentHealth = Mth.ceil(player.getHealth());
		int tickCount = mc.gui.getGuiTicks();
		boolean blinking = healthBlinkTime > (long) tickCount && (healthBlinkTime - (long) tickCount) / 3L % 2L == 1L;
		long now = Util.getMillis();

		if(currentHealth < lastHealth && player.invulnerableTime > 0) {
			lastHealthTime = now;
			healthBlinkTime = tickCount + 20;
		} else if(currentHealth > lastHealth && player.invulnerableTime > 0) {
			lastHealthTime = now;
			healthBlinkTime = tickCount + 10;
		}

		if(now - lastHealthTime > 1000L) {
			lastHealth = currentHealth;
			displayHealth = currentHealth;
			lastHealthTime = now;
		}

		lastHealth = currentHealth;
		int displayHealthValue = displayHealth;
		random.setSeed(tickCount * 312871L);
		FoodData foodData = player.getFoodData();

		int foodLevel = foodData.getFoodLevel();
		int leftX = screenWidth / 2 - 91;
		int rightX = screenWidth / 2 + 91;
		int healthY = screenHeight - (39 + 10);
		float maxHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), (float) Math.max(displayHealthValue, currentHealth));
		int absorption = Mth.ceil(player.getAbsorptionAmount());
		int heartRows = Mth.ceil((maxHealth + (float) absorption) / 2 / 10);
		int rowHeight = Math.max(10 - (heartRows - 2), 3);
		int armorY = healthY - (heartRows - 1) * rowHeight - 10;
		int foodY = healthY - 10;
		int armor = player.getArmorValue();
		int regenerationOffset = -1;

		if(player.hasEffect(MobEffects.REGENERATION)) {
			regenerationOffset = tickCount % Mth.ceil(maxHealth + 5);
		}

		mc.getProfiler().push("armor");
		for(int i = 0; i < 10; ++i) {
			if(armor > 0) {
				int x = leftX + i * 8;
				if(i * 2 + 1 < armor) {
					gui.blit(DARK_WORLD_ICONS, x, armorY, 34, 9, 9, 9);
				}
				if(i * 2 + 1 == armor) {
					gui.blit(DARK_WORLD_ICONS, x, armorY, 25, 9, 9, 9);
				}
				if(i * 2 + 1 > armor) {
					gui.blit(DARK_WORLD_ICONS, x, armorY, 16, 9, 9, 9);
				}
			}
		}
		mc.getProfiler().popPush("health");

		renderHearts(gui, player, leftX, healthY, rowHeight, regenerationOffset, maxHealth, currentHealth, displayHealthValue, absorption, blinking, iconsTexture);

		LivingEntity mount = getPlayerVehicleWithHealth(mc, player);
		int mountHearts = getVehicleMaxHearts(mount);

		if(mountHearts == 0) {
			mc.getProfiler().popPush("food");
			for(int i = 0; i < 10; ++i) {
				int y = healthY;
				int textureX = 16;
				int textureY = 0;

				if(player.hasEffect(MobEffects.HUNGER)) {
					textureX += 36;
					textureY = 13;
				}
				if(foodData.getSaturationLevel() <= 0.0F && tickCount % (foodLevel * 3 + 1) == 0) {
					y = healthY + (random.nextInt(3) - 1);
				}

				int x = rightX - i * 8 - 9;
				gui.blit(DARK_WORLD_ICONS, x, y, 16 + textureY * 9, 27, 9, 9);

				if(i * 2 + 1 < foodLevel) {
					gui.blit(DARK_WORLD_ICONS, x, y, textureX + 36, 27, 9, 9);
				}

				if(i * 2 + 1 == foodLevel) {
					gui.blit(DARK_WORLD_ICONS, x, y, textureX + 45, 27, 9, 9);
				}
			}

			foodY -= 10;
			mc.getProfiler().pop();
		} else {
			renderVehicleHealth(mc, gui, window, mount);
		}

		mc.getProfiler().push("air");
		int maxAir = player.getMaxAirSupply();
		int currentAir = Math.min(player.getAirSupply(), maxAir);

		if(player.isEyeInFluid(FluidTags.WATER) || currentAir < maxAir) {
			int mountRows = getVisibleVehicleHeartRows(mountHearts) - 1;
			int airY = foodY - mountRows * 10;
			int fullBubbles = Mth.ceil((double) (currentAir - 2) * 10 / maxAir);
			int partialBubbles = Mth.ceil((double) currentAir * 10 / maxAir) - fullBubbles;

			for(int i = 0; i < fullBubbles + partialBubbles; ++i) {
				int x = rightX - i * 8 - 9;

				if(i < fullBubbles) {
					gui.blit(DARK_WORLD_ICONS, x, airY, 16, 18, 9, 9);
				} else {
					gui.blit(DARK_WORLD_ICONS, x, airY, 25, 18, 9, 9);
				}
			}
		}

		mc.getProfiler().pop();
	}

	private static LivingEntity getPlayerVehicleWithHealth(Minecraft mc, Player player) {
		if(player.getVehicle() instanceof LivingEntity living && living.showVehicleHealth()) {
			return living;
		}

		return null;
	}

	private static int getVehicleMaxHearts(LivingEntity vehicle) {
		if(vehicle == null) return 0;

		return (int) Math.ceil(vehicle.getMaxHealth() / 2);
	}

	private static int getVisibleVehicleHeartRows(int maxHearts) {
		return (int) Math.ceil((double) maxHearts / 10);
	}

	private static void renderHearts(GuiGraphics gui, Player player, int x, int y, int rowHeight, int regenerationOffset, float maxHealth, int currentHealth,
									 int displayHealth, int absorption, boolean blink, ResourceLocation iconsTexture) {
		Gui.HeartType heartType = Gui.HeartType.forPlayer(player);
		int hardcoreOffset = (player.level().getLevelData().isHardcore() ? 5 : 0) * 9;
		int totalHearts = Mth.ceil(maxHealth / 2.0F);
		int absorptionHearts = Mth.ceil(absorption / 2.0F);
		int totalRows = totalHearts + absorptionHearts;

		for(int i = totalRows - 1; i >= 0; --i) {
			int row = i / 10;
			int col = i % 10;
			int heartX = x + col * 8;
			int heartY = y - row * rowHeight;

			if(currentHealth + absorption <= 4) {
				heartY += random.nextInt(2);
			}
			if(i < totalHearts && i == regenerationOffset) {
				heartY -= 2;
			}

			renderHeart(gui, Gui.HeartType.CONTAINER, heartX, heartY, hardcoreOffset, false, false, iconsTexture);

			int heartIndex = i * 2;
			boolean isAbsorption = i >= totalHearts;
			if(isAbsorption) {
				int absorptionIndex = heartIndex - totalHearts * 2;

				if(absorptionIndex < absorption) {
					boolean half = absorptionIndex + 1 == absorption;
					renderHeart(gui, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING, heartX, heartY, hardcoreOffset, false,
							half, iconsTexture);
				}
			}

			if(blink && heartIndex < displayHealth) {
				boolean half = heartIndex + 1 == displayHealth;
				renderHeart(gui, heartType, heartX, heartY, hardcoreOffset, false, half, iconsTexture);
			}
			if(heartIndex < currentHealth) {
				boolean half = heartIndex + 1 == currentHealth;
				renderHeart(gui, heartType, heartX, heartY, hardcoreOffset, false, half, iconsTexture);
			}
		}

		if(blink) {
			for(int i = totalRows - 1; i >= 0; --i) {
				int row = i / 10;
				int col = i % 10;
				int heartX = x + col * 8;
				int heartY = y - row * rowHeight;

				if(currentHealth + absorption <= 4) {
					heartY += random.nextInt(2);
				}

				if(i < totalHearts && i == regenerationOffset) {
					heartY -= 2;
				}

				renderHeart(gui, Gui.HeartType.CONTAINER, heartX, heartY, hardcoreOffset, true, false, iconsTexture);
			}
		}
	}

	private static void renderHeart(GuiGraphics gui, Gui.HeartType type, int x, int y, int textureYOffset, boolean blink, boolean half, ResourceLocation iconsTexture) {
		gui.blit(iconsTexture, x, y, type.getX(half, blink), textureYOffset, 9, 9);
	}

	private static void renderVehicleHealth(Minecraft mc, GuiGraphics gui, Window window, LivingEntity vehicle) {
		if(vehicle == null) return;

		int screenHeight = window.getGuiScaledHeight();
		int screenWidth = window.getGuiScaledWidth();
		int maxHearts = getVehicleMaxHearts(vehicle);

		if(maxHearts == 0) return;

		int currentHealth = (int) Math.ceil(vehicle.getHealth());
		mc.getProfiler().popPush("mountHealth");
		int y = screenHeight - (39 + 10);
		int x = screenWidth / 2 + 91;
		int rowY = y;
		int heartsDrawn = 0;

		while(maxHearts > 0) {
			int heartsInRow = Math.min(maxHearts, 10);
			maxHearts -= heartsInRow;

			for(int i = 0; i < heartsInRow; ++i) {
				int heartX = x - i * 8 - 9;
				gui.blit(DARK_WORLD_ICONS, heartX, rowY, 52, 9, 9, 9);
				int healthIndex = heartsDrawn * 2 + 1 + i * 2;

				if(healthIndex < currentHealth) {
					gui.blit(DARK_WORLD_ICONS, heartX, rowY, 88, 9, 9, 9);
				} else if(healthIndex == currentHealth) {
					gui.blit(DARK_WORLD_ICONS, heartX, rowY, 97, 9, 9, 9);
				}
			}

			rowY -= 10;
			heartsDrawn += heartsInRow;
		}
	}

	public static void renderDarkWorldExperienceBar(GuiGraphics pGuiGraphics, int pX, Minecraft minecraft, Window window) {
		int screenHeight = window.getGuiScaledHeight();
		int screenWidth = window.getGuiScaledWidth();

		minecraft.getProfiler().push("expBar");
		int i = minecraft.player.getXpNeededForNextLevel();
		if(i > 0) {
			int j = 182;
			int k = (int) (minecraft.player.experienceProgress * 183.0F);
			int l = screenHeight - 40;

			pGuiGraphics.blit(DARK_WORLD_ICONS, pX, l, 0, 64, 182, 5);

			if(k > 0) {
				pGuiGraphics.blit(DARK_WORLD_ICONS, pX, l, 0, 69, k, 5);
			}
		}

		minecraft.getProfiler().pop();
		if(minecraft.player.experienceLevel > 0) {
			minecraft.getProfiler().push("expLevel");

			String s = "" + minecraft.player.experienceLevel;
			int i1 = (screenWidth - minecraft.font.width(s)) / 2;
			int j1 = screenHeight - 31 - (4 + 9);

			pGuiGraphics.drawString(minecraft.font, s, i1 + 1, j1, 0, false);
			pGuiGraphics.drawString(minecraft.font, s, i1 - 1, j1, 0, false);
			pGuiGraphics.drawString(minecraft.font, s, i1, j1 + 1, 0, false);
			pGuiGraphics.drawString(minecraft.font, s, i1, j1 - 1, 0, false);
			pGuiGraphics.drawString(minecraft.font, s, i1, j1, 0xFF7F27, false);

			minecraft.getProfiler().pop();
		}
	}

	@SubscribeEvent
	public static void renderTooltipEvent(RenderTooltipEvent.GatherComponents event) {
		ItemStack stack = event.getItemStack();

		if(stack.is(ItemRegistry.DARK_WALLET.get())) {
			int dollars = 0, dimes = 0;
			if(stack.getTag() != null) {
				dollars = stack.getTag().getInt(DarkWallerItem.DOLLARS);
				dimes = stack.getTag().getInt(DarkWallerItem.DIMES);
			}

			event.getTooltipElements().add(3, Either.right(new DarkMoneyTooltipComponent(dollars, dimes)));
		}
	}
}