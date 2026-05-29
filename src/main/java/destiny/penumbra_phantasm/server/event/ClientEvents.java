package destiny.penumbra_phantasm.server.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.dimension.CardKingdomDimensionEffects;
import destiny.penumbra_phantasm.client.ClientConfig;
import destiny.penumbra_phantasm.client.render.GreatDoorRenderUtil;
import destiny.penumbra_phantasm.client.render.screen.DarkWorldInventoryScreen;
import destiny.penumbra_phantasm.server.fountain.GreatDoor;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
	private static final BufferBuilder FOUNTAIN_BUFFER = new BufferBuilder(65536);
	private static final ResourceLocation DARK_WORLD_WIDGETS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets.png");
	private static final ResourceLocation DARK_WORLD_ICONS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/icons.png");
	private static final ResourceLocation DARK_WORLD_HOTBAR = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/hotbar.png");
	private static final ResourceLocation DARK_WORLD_HOTBAR_GLOW = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/hotbar_glow.png");

	private static int lastHealth = -1;
	private static int displayHealth = -1;
	private static long lastHealthTime = 0L;
	private static long healthBlinkTime = 0L;
	private static final Random random = new Random();

	@SubscribeEvent
	public static void levelRender(RenderLevelStageEvent event) {
		boolean renderSkyPass = event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SKY);
		boolean renderShockwavePass = event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS);
		if (renderSkyPass || renderShockwavePass) {
			ClientLevel level = Minecraft.getInstance().level;
			if (level == null)
				return;

			float partialTick = event.getPartialTick();

			Camera camera = event.getCamera();

			int length = 16;
			ResourceLocation textureCrack = new ResourceLocation(PenumbraPhantasm.MODID, "textures/fountain/fountain_ground_crack.png");

			PoseStack pose = event.getPoseStack();
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(FOUNTAIN_BUFFER);

			GL11.glEnable(GL_DEPTH_CLAMP);

			if (renderSkyPass && CardKingdomDimensionEffects.isCardKingdomDarkWorld(level)) {
				CardKingdomDimensionEffects cardKingdomDimensionEffects = CardKingdomDimensionEffects.getInstance();

				if (cardKingdomDimensionEffects != null) {
					pose.pushPose();
					cardKingdomDimensionEffects.renderOverlay(level, partialTick, pose, camera, event.getProjectionMatrix());
					pose.popPose();
				}
			}

			level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
				cap.darkFountains.forEach((key, fountain) ->
				{
					float openingTick = fountain.getOpeningTick(partialTick);

					if (!DarkWorldUtil.isDarkWorld(level)) {
						pose.pushPose();
						pose.translate(
								fountain.getFountainPos().getX() - camera.getPosition().x(),
								fountain.getFountainPos().getY() - camera.getPosition().y(),
								fountain.getFountainPos().getZ() - camera.getPosition().z()
						);

						if (renderSkyPass) {
							if (openingTick < FountainRenderUtil.OPENING_POSTERIZE_TICK_END && openingTick >= 0) {
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
					} else if (renderSkyPass) {
						pose.pushPose();
						pose.translate(
								fountain.getFountainPos().getX() - camera.getPosition().x(),
								fountain.getFountainPos().getY() - camera.getPosition().y(),
								fountain.getFountainPos().getZ() - camera.getPosition().z()
						);

						Vec2 fountain2dPos = new Vec2(fountain.getFountainPos().getX(), fountain.getFountainPos().getZ());
						Vec2 camera2dPos = new Vec2((float) camera.getPosition().x, (float) camera.getPosition().z);

						double distance3d = fountain.getFountainPos().getCenter().distanceTo(camera.getPosition());
						double distance2d = Mth.sqrt(fountain2dPos.distanceToSqr(camera2dPos));
						double referenceDistance = 64;
						float distanceScale = (float) (distance2d / referenceDistance);
						distanceScale = Math.max(distanceScale, 1.0f);

						pose.scale(distanceScale, distanceScale, distanceScale);

						double fadeDistance = ClientConfig.fountainLodDistance;
						float fade = (float) ((distance3d - fadeDistance) / fadeDistance);
						fade = Math.max(0f, Math.min(1f, fade));

						if (fade < 1.0f) {
							if (fountain.sealingTick >= 0) {
								FountainRenderUtil.renderSealingFountain(fountain, level, length, textureCrack, partialTick, pose, buffer, OverlayTexture.NO_OVERLAY, 1.0f - fade);
							} else {
								FountainRenderUtil.renderOpenFountain(fountain, level, length, textureCrack, partialTick, pose, buffer, OverlayTexture.NO_OVERLAY, 1.0f - fade);
							}
						}
						if (fade > 0.0f) {
							FountainRenderUtil.renderOpenFountainOptimized(fountain, length, pose, buffer, OverlayTexture.NO_OVERLAY, fade, camera.getPosition());
						}
						pose.popPose();
					}
				});
			});

			level.getCapability(CapabilityRegistry.GREAT_DOOR).ifPresent(cap -> {
				for (GreatDoor greatDoor : new ArrayList<>(cap.greatDoors.values())) {
					pose.pushPose();
					pose.translate(
							(double) greatDoor.greatDoorPos.getX() - camera.getPosition().x(),
							(double) greatDoor.greatDoorPos.getY() - camera.getPosition().y(),
							(double) greatDoor.greatDoorPos.getZ() - camera.getPosition().z()
					);

					if (Minecraft.getInstance().level.isLoaded(greatDoor.greatDoorPos)) {
						int packedLight = LevelRenderer.getLightColor(level, greatDoor.greatDoorPos);

						if (renderShockwavePass) {
							if (greatDoor.isOpen) {
								GreatDoorRenderUtil.renderOpenGreatDoor(greatDoor, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
							} else {
								GreatDoorRenderUtil.renderClosedGreatDoor(greatDoor, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
							}
						}
					}

					pose.popPose();
				}
			});

			buffer.endBatch();

			GL11.glDisable(GL_DEPTH_CLAMP);
		}
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			IntroScreen.tickWorldThumbnail(Minecraft.getInstance());
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
			if (lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
				cap = lazyCapability.resolve().get();
			else return;

			DarkFountain fountain = null;

			for (Map.Entry<BlockPos, DarkFountain> entry : cap.darkFountains.entrySet()) {
				if (entry.getValue().openingTick == -1 && entry.getValue().getFountainPos().distSqr(player.getOnPos()) < 64) {
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

			Vec2 flatPlayerPos = new Vec2((float) playerX, (float) playerZ);
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
	public static void pressKey(InputEvent.Key event) {
		if (Minecraft.getInstance().screen instanceof IntroScreen introScreen) {
			if (event.getAction() != 1 || !introScreen.isChoosing)
				return;

			if (InputConstants.getKey(InputConstants.KEY_W, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(-1);
			}
			if (InputConstants.getKey(InputConstants.KEY_S, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(1);
			}
			if (InputConstants.getKey(InputConstants.KEY_RETURN, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.pickChoice();
			}

			if (InputConstants.getKey(InputConstants.KEY_UP, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(-1);
			}
			if (InputConstants.getKey(InputConstants.KEY_DOWN, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.incrementChoice(1);
			}
			if (InputConstants.getKey(InputConstants.KEY_Z, event.getScanCode())
					.equals(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
				introScreen.pickChoice();
			}
		}
	}

	@SubscribeEvent
	public static void onScreenOpen(ScreenEvent.Opening event) {
		Screen newScreen = event.getNewScreen();
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;

		if (DarkWorldUtil.isDarkWorld(player.level())) {
			if (newScreen instanceof InventoryScreen) {
				event.setNewScreen(new DarkWorldInventoryScreen(player));
			}
		}
	}

	@SubscribeEvent
	public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;
		if (!DarkWorldUtil.isDarkWorld(mc.level)) return;

		NamedGuiOverlay overlay = event.getOverlay();
		GuiGraphics gui = event.getGuiGraphics();
		Window window = event.getWindow();

		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		if (overlay == VanillaGuiOverlay.FOOD_LEVEL.type()) {
			event.setCanceled(true);
		}
		if (overlay == VanillaGuiOverlay.ARMOR_LEVEL.type()) {
			event.setCanceled(true);
		}
		if (overlay == VanillaGuiOverlay.MOUNT_HEALTH.type()) {
			event.setCanceled(true);
		}
		if (overlay == VanillaGuiOverlay.AIR_LEVEL.type()) {
			event.setCanceled(true);
		}

		if (overlay == VanillaGuiOverlay.CROSSHAIR.type()) {
			event.setCanceled(true);
			renderDarkWorldCrosshair(gui, window, mc);
		} else if (overlay == VanillaGuiOverlay.PLAYER_HEALTH.type()) {
			event.setCanceled(true);
			renderDarkWorldHealth(mc, gui, window, mc.player);
		} else if (overlay == VanillaGuiOverlay.HOTBAR.type()) {
			event.setCanceled(true);
			renderDarkWorldHotbar(mc, gui, window, mc.player);
		} else if (overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
			PlayerRideableJumping playerrideablejumping = mc.player.jumpableVehicle();
			if (playerrideablejumping == null && mc.gameMode.hasExperience()) {
				int i = screenWidth / 2 - 91;
				event.setCanceled(true);
				renderDarkWorldExperienceBar(gui, i, mc, window);
			}
		}
	}

	private static void renderDarkWorldHotbar(Minecraft mc, GuiGraphics gui, Window window, Player player) {
		if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			mc.gui.getSpectatorGui().renderHotbar(gui);
			return;
		}

		if (mc.options.hideGui) return;

		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		// Position of the main hotbar bar
		int x = screenWidth / 2 - 182 / 2;
		int y = screenHeight - 22 - 12;

		// First pass: draw the background
		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR);
		gui.blit(DARK_WORLD_HOTBAR, x, y, 0, 0, 182, 38);

		// Second pass: draw all items (and their decorations)
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = player.getInventory().items.get(slot);
			if (!stack.isEmpty()) {
				int slotX = x + slot * 18;
				gui.renderItem(stack, slotX + 11, y + 11);
				gui.renderItemDecorations(mc.font, stack, slotX + 11, y + 11);
			}
		}

		// Third pass: draw selection highlight (now definitely above all items)
		int selected = player.getInventory().selected;
		int selectedSlotX = x + selected * 18;
		long period = 20 * 5; // 100
		long elapsed = mc.level.getGameTime() % period;
		float t = (float) elapsed / period;
		float glow = Mth.sin(t * Mth.PI);
		HumanoidArm humanoidarm = player.getMainArm().getOpposite();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR);
		gui.blit(DARK_WORLD_HOTBAR, selectedSlotX, y, 182, 0, 38, 38);
		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR_GLOW);
		RenderBlitUtil.blitGui(gui, DARK_WORLD_HOTBAR_GLOW, selectedSlotX, y, 182, 0, 38, 38, glow, glow, glow, 1);

		// 3. Off‑hand slot (only if not empty)
		RenderSystem.setShaderTexture(0, DARK_WORLD_HOTBAR);
		ItemStack offhand = player.getOffhandItem();

		if (!offhand.isEmpty()) {
			int offX = x - 29;
			int offY = y + 8;
			gui.blit(DARK_WORLD_HOTBAR, offX + 2, offY + 2, 223, 15, 18, 18);
			// Items need their own binding, but after we draw them we should rebind again
			gui.renderItem(offhand, offX + 3, offY + 3);
			gui.renderItemDecorations(mc.font, offhand, offX + 3, offY + 3);
			// No more blitting of widgets after this, so rebinding isn't critical, but good practice
		}

		RenderSystem.enableBlend();
		if (mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
			float f = mc.player.getAttackStrengthScale(0.0F);
			if (f < 1.0F) {
				int j2 = screenHeight - 20;
				int i = screenWidth / 2;
				int k2 = i + 91 + 6;
				if (humanoidarm == HumanoidArm.RIGHT) {
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
		if (minecraft.options.hideGui) return;

		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		Options options = minecraft.options;
		if (options.getCameraType().isFirstPerson() && (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || canRenderCrosshairForSpectator(minecraft.hitResult, minecraft))) {
			if (options.renderDebug && !options.hideGui && !minecraft.player.isReducedDebugInfo() && !(Boolean) options.reducedDebugInfo().get()) {
				Camera camera = minecraft.gameRenderer.getMainCamera();
				PoseStack posestack = RenderSystem.getModelViewStack();
				posestack.pushPose();
				posestack.mulPoseMatrix(gui.pose().last().pose());
				posestack.translate((float) (screenWidth / 2), (float) (screenHeight / 2), 0.0F);
				posestack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
				posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
				posestack.scale(-1.0F, -1.0F, -1.0F);
				RenderSystem.applyModelViewMatrix();
				RenderSystem.renderCrosshair(10);
				posestack.popPose();
				RenderSystem.applyModelViewMatrix();
			} else {
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				int i = 15;
				gui.blit(DARK_WORLD_ICONS, (screenWidth - 15) / 2, (screenHeight - 15) / 2, 0, 0, 15, 15);
				if (minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
					float f = minecraft.player.getAttackStrengthScale(0.0F);
					boolean flag = false;
					if (minecraft.crosshairPickEntity != null && minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
						flag = minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
						flag &= minecraft.crosshairPickEntity.isAlive();
					}

					int j = screenHeight / 2 - 7 + 16;
					int k = screenWidth / 2 - 8;
					if (flag) {
						gui.blit(DARK_WORLD_ICONS, k, j, 68, 94, 16, 16);
					} else if (f < 1.0F) {
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
		if (pRayTrace == null) {
			return false;
		} else if (pRayTrace.getType() == HitResult.Type.ENTITY) {
			return ((EntityHitResult) pRayTrace).getEntity() instanceof MenuProvider;
		} else if (pRayTrace.getType() == HitResult.Type.BLOCK) {
			BlockPos blockpos = ((BlockHitResult) pRayTrace).getBlockPos();
			Level level = minecraft.level;
			return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
		} else {
			return false;
		}
	}

	private static void renderDarkWorldHealth(Minecraft mc, GuiGraphics gui, Window window, Player player) {
		if (player == null) return;

		if (!mc.gameMode.canHurtPlayer()) return;

		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();
		int currentHealth = Mth.ceil(player.getHealth());
		int tickCount = mc.gui.getGuiTicks();
		boolean blinking = healthBlinkTime > (long) tickCount && (healthBlinkTime - (long) tickCount) / 3L % 2L == 1L;
		long now = Util.getMillis();

		// Update health blink state
		if (currentHealth < lastHealth && player.invulnerableTime > 0) {
			lastHealthTime = now;
			healthBlinkTime = (long) (tickCount + 20);
		} else if (currentHealth > lastHealth && player.invulnerableTime > 0) {
			lastHealthTime = now;
			healthBlinkTime = (long) (tickCount + 10);
		}

		if (now - lastHealthTime > 1000L) {
			lastHealth = currentHealth;
			displayHealth = currentHealth;
			lastHealthTime = now;
		}

		lastHealth = currentHealth;
		int displayHealthValue = displayHealth;
		random.setSeed((long) (tickCount * 312871));
		FoodData foodData = player.getFoodData();
		int foodLevel = foodData.getFoodLevel();
		int leftX = screenWidth / 2 - 91;
		int rightX = screenWidth / 2 + 91;
		int healthY = screenHeight - (39 + 10);
		float maxHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), (float) Math.max(displayHealthValue, currentHealth));
		int absorption = Mth.ceil(player.getAbsorptionAmount());
		int heartRows = Mth.ceil((maxHealth + (float) absorption) / 2.0F / 10.0F);
		int rowHeight = Math.max(10 - (heartRows - 2), 3);
		int armorY = healthY - (heartRows - 1) * rowHeight - 10;
		int foodY = healthY - 10;
		int armor = player.getArmorValue();
		int regenerationOffset = -1;
		if (player.hasEffect(MobEffects.REGENERATION)) {
			regenerationOffset = tickCount % Mth.ceil(maxHealth + 5.0F);
		}

		// Armor
		mc.getProfiler().push("armor");
		for (int i = 0; i < 10; ++i) {
			if (armor > 0) {
				int x = leftX + i * 8;
				if (i * 2 + 1 < armor) {
					gui.blit(DARK_WORLD_ICONS, x, armorY, 34, 9, 9, 9);
				}
				if (i * 2 + 1 == armor) {
					gui.blit(DARK_WORLD_ICONS, x, armorY, 25, 9, 9, 9);
				}
				if (i * 2 + 1 > armor) {
					gui.blit(DARK_WORLD_ICONS, x, armorY, 16, 9, 9, 9);
				}
			}
		}
		mc.getProfiler().popPush("health");

		// Hearts
		renderHearts(gui, player, leftX, healthY, rowHeight, regenerationOffset, maxHealth, currentHealth, displayHealthValue, absorption, blinking);

		// Mount health (if riding)
		LivingEntity mount = getPlayerVehicleWithHealth(mc, player);
		int mountHearts = getVehicleMaxHearts(mount);
		if (mountHearts == 0) {
			// Food (hunger)
			mc.getProfiler().popPush("food");
			for (int i = 0; i < 10; ++i) {
				int y = healthY;
				int textureX = 16;
				int textureY = 0;
				if (player.hasEffect(MobEffects.HUNGER)) {
					textureX += 36;
					textureY = 13;
				}
				if (foodData.getSaturationLevel() <= 0.0F && tickCount % (foodLevel * 3 + 1) == 0) {
					y = healthY + (random.nextInt(3) - 1);
				}
				int x = rightX - i * 8 - 9;
				gui.blit(DARK_WORLD_ICONS, x, y, 16 + textureY * 9, 27, 9, 9);
				if (i * 2 + 1 < foodLevel) {
					gui.blit(DARK_WORLD_ICONS, x, y, textureX + 36, 27, 9, 9);
				}
				if (i * 2 + 1 == foodLevel) {
					gui.blit(DARK_WORLD_ICONS, x, y, textureX + 45, 27, 9, 9);
				}
			}
			foodY -= 10;
			mc.getProfiler().pop();
		} else {
			// Mount health (rendered later in vanilla, but we can call it here)
			renderVehicleHealth(mc, gui, window, mount);
		}

		// Air bubbles
		mc.getProfiler().push("air");
		int maxAir = player.getMaxAirSupply();
		int currentAir = Math.min(player.getAirSupply(), maxAir);
		if (player.isEyeInFluid(FluidTags.WATER) || currentAir < maxAir) {
			int mountRows = getVisibleVehicleHeartRows(mountHearts) - 1;
			int airY = foodY - mountRows * 10;
			int fullBubbles = Mth.ceil((double) (currentAir - 2) * 10.0 / maxAir);
			int partialBubbles = Mth.ceil((double) currentAir * 10.0 / maxAir) - fullBubbles;
			for (int i = 0; i < fullBubbles + partialBubbles; ++i) {
				int x = rightX - i * 8 - 9;
				if (i < fullBubbles) {
					gui.blit(DARK_WORLD_ICONS, x, airY, 16, 18, 9, 9);
				} else {
					gui.blit(DARK_WORLD_ICONS, x, airY, 25, 18, 9, 9);
				}
			}
		}
		mc.getProfiler().pop();
	}

	private static LivingEntity getPlayerVehicleWithHealth(Minecraft mc, Player player) {
		if (player.getVehicle() instanceof LivingEntity living && living.showVehicleHealth()) {
			return living;
		}
		return null;
	}

	private static int getVehicleMaxHearts(LivingEntity vehicle) {
		if (vehicle == null) return 0;
		return (int) Math.ceil(vehicle.getMaxHealth() / 2.0);
	}

	private static int getVisibleVehicleHeartRows(int maxHearts) {
		return (int) Math.ceil((double) maxHearts / 10.0);
	}

	private static void renderHearts(GuiGraphics gui, Player player, int x, int y, int rowHeight, int regenerationOffset,
									 float maxHealth, int currentHealth, int displayHealth, int absorption, boolean blink) {
		Gui.HeartType heartType = Gui.HeartType.forPlayer(player);
		int hardcoreOffset = (player.level().getLevelData().isHardcore() ? 5 : 0) * 9;
		int totalHearts = Mth.ceil(maxHealth / 2.0F);
		int absorptionHearts = Mth.ceil(absorption / 2.0F);
		int totalRows = totalHearts + absorptionHearts;

		for (int i = totalRows - 1; i >= 0; --i) {
			int row = i / 10;
			int col = i % 10;
			int heartX = x + col * 8;
			int heartY = y - row * rowHeight;
			if (currentHealth + absorption <= 4) {
				heartY += random.nextInt(2);
			}
			if (i < totalHearts && i == regenerationOffset) {
				heartY -= 2;
			}

			// Container (empty heart)
			renderHeart(gui, Gui.HeartType.CONTAINER, heartX, heartY, hardcoreOffset, blink, false);

			int heartIndex = i * 2;
			boolean isAbsorption = i >= totalHearts;
			if (isAbsorption) {
				int absorptionIndex = heartIndex - totalHearts * 2;
				if (absorptionIndex < absorption) {
					boolean half = absorptionIndex + 1 == absorption;
					renderHeart(gui, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING,
							heartX, heartY, hardcoreOffset, false, half);
				}
			}

			if (blink && heartIndex < displayHealth) {
				boolean half = heartIndex + 1 == displayHealth;
				renderHeart(gui, heartType, heartX, heartY, hardcoreOffset, true, half);
			}

			if (heartIndex < currentHealth) {
				boolean half = heartIndex + 1 == currentHealth;
				renderHeart(gui, heartType, heartX, heartY, hardcoreOffset, false, half);
			}
		}
	}

	private static void renderHeart(GuiGraphics gui, Gui.HeartType type, int x, int y, int textureYOffset, boolean blink, boolean half) {
		gui.blit(DARK_WORLD_ICONS, x, y, type.getX(half, blink), textureYOffset, 9, 9);
	}

	private static void renderVehicleHealth(Minecraft mc, GuiGraphics gui, Window window, LivingEntity vehicle) {
		if (vehicle == null) return;
		int screenHeight = window.getGuiScaledHeight();
		int screenWidth = window.getGuiScaledWidth();
		int maxHearts = getVehicleMaxHearts(vehicle);
		if (maxHearts == 0) return;
		int currentHealth = (int) Math.ceil(vehicle.getHealth());
		mc.getProfiler().popPush("mountHealth");
		int y = screenHeight - (39 + 10);
		int x = screenWidth / 2 + 91;
		int rowY = y;
		int heartsDrawn = 0;
		while (maxHearts > 0) {
			int heartsInRow = Math.min(maxHearts, 10);
			maxHearts -= heartsInRow;
			for (int i = 0; i < heartsInRow; ++i) {
				int heartX = x - i * 8 - 9;
				gui.blit(DARK_WORLD_ICONS, heartX, rowY, 52, 9, 9, 9);
				int healthIndex = heartsDrawn * 2 + 1 + i * 2;
				if (healthIndex < currentHealth) {
					gui.blit(DARK_WORLD_ICONS, heartX, rowY, 88, 9, 9, 9);
				} else if (healthIndex == currentHealth) {
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
		if (i > 0) {
			int j = 182;
			int k = (int)(minecraft.player.experienceProgress * 183.0F);
			int l = screenHeight - 40;
			pGuiGraphics.blit(DARK_WORLD_ICONS, pX, l, 0, 64, 182, 5);
			if (k > 0) {
				pGuiGraphics.blit(DARK_WORLD_ICONS, pX, l, 0, 69, k, 5);
			}
		}

		minecraft.getProfiler().pop();
		if (minecraft.player.experienceLevel > 0) {
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
}