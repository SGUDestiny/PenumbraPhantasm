package destiny.penumbra_phantasm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin
{
	@Shadow
	private Button exitToTitleButton;
	@Unique
	private static boolean diedWithSoulHearth = false;
	@Unique
	private static int soulType = 1;

	private int tick = 0;
	private final int tickSoulBreak = 10;
	private final int tickSoulShatter = 30;
	private final int tickSoulFadeStart = 10 + 3 * 20;
	private final int tickSoulFadeDuration = 10;
	private final int tickSoulFadeEnd = tickSoulFadeStart + tickSoulFadeDuration;
	private int fragment = 1;

	private Vec2 soulShardPos1 = new Vec2(0, 0);
	private Vec2 soulShardVel1 = new Vec2(0, 0);
	private Vec2 soulShardPos2 = new Vec2(0, 0);
	private Vec2 soulShardVel2 = new Vec2(0, 0);
	private Vec2 soulShardPos3 = new Vec2(0, 0);
	private Vec2 soulShardVel3 = new Vec2(0, 0);
	private Vec2 soulShardPos4 = new Vec2(0, 0);
	private Vec2 soulShardVel4 = new Vec2(0, 0);
	private Vec2 soulShardPos5 = new Vec2(0, 0);
	private Vec2 soulShardVel5 = new Vec2(0, 0);

	private final float gravity = 0.4f; // Adjustable gravity (pixels per tick squared, positive for downward)
	private final float horizontalFriction = 0.95f; // Adjustable friction multiplier per tick (less than 1 to decay horizontal velocity)

	private final Minecraft minecraft = Minecraft.getInstance();

	@Inject(method = "render", at = @At(value = "HEAD"))
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci)
	{
		PoseStack pose = graphics.pose();
		DeathScreen screen = (DeathScreen) (Object) this;

		int width = screen.width;
		int height = screen.height;

		if(Minecraft.getInstance().player == null || Minecraft.getInstance().level == null)
			return;

		Minecraft.getInstance().player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> {
			diedWithSoulHearth = cap.diedWithSoulHearth;
			soulType = cap.soulType;
		});

		if(!diedWithSoulHearth)
			return;

		ResourceLocation SOUL = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");
		ResourceLocation SOUL_BROKEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + "_broken.png");
		ResourceLocation SOUL_FRAGMENT = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + "_fragment_" + fragment + ".png");

		pose.pushPose();
		pose.translate(width / 2f, ((this.exitToTitleButton.getY() - 40f) + 125f) / 2f, 5);
		pose.scale(1.5f, 1.5f, 1f);

		if (tick < tickSoulBreak) {
			pose.translate(-15f / 2f, -15f / 2f, 0);
			RenderBlitUtil.blit(SOUL, pose, 0, 0, 1, 1, 1, 1, 0, 0, 15, 15, 15, 15);
		} else if (tick < tickSoulShatter) {
			pose.translate(-20f / 2f, -16f / 2f, 0);
			RenderBlitUtil.blit(SOUL_BROKEN, pose, 0, 0, 1, 1, 1, 1, 0, 0, 20, 16, 20, 16);
		} else if (tick < tickSoulFadeEnd) {
			float fragmentAlpha = 1f;
			if (tick >= tickSoulFadeStart) {
				float fragmentAlphaDelta = (float) (tick - tickSoulFadeStart) / tickSoulFadeDuration;
				fragmentAlpha = Mth.lerp(fragmentAlphaDelta, 1f, 0f);
			}

			pose.translate(-10f / 2f, -10f / 2f, 0);

			pose.pushPose();
			pose.translate(soulShardPos1.x, soulShardPos1.y, 0);
			RenderBlitUtil.blit(SOUL_FRAGMENT, pose, 0, 0, 1, 1, 1, fragmentAlpha, 0, 0, 10f, 10f, 10f, 10f);
			pose.popPose();

			pose.pushPose();
			pose.translate(soulShardPos2.x, soulShardPos2.y, 0);
			RenderBlitUtil.blit(SOUL_FRAGMENT, pose, 0, 0, 1, 1, 1, fragmentAlpha, 0, 0, 10f, 10f, 10f, 10f);
			pose.popPose();

			pose.pushPose();
			pose.translate(soulShardPos3.x, soulShardPos3.y, 0);
			RenderBlitUtil.blit(SOUL_FRAGMENT, pose, 0, 0, 1, 1, 1, fragmentAlpha, 0, 0, 10f, 10f, 10f, 10f);
			pose.popPose();

			pose.pushPose();
			pose.translate(soulShardPos4.x, soulShardPos4.y, 0);
			RenderBlitUtil.blit(SOUL_FRAGMENT, pose, 0, 0, 1, 1, 1, fragmentAlpha, 0, 0, 10f, 10f, 10f, 10f);
			pose.popPose();

			pose.pushPose();
			pose.translate(soulShardPos5.x, soulShardPos5.y, 0);
			RenderBlitUtil.blit(SOUL_FRAGMENT, pose, 0, 0, 1, 1, 1, fragmentAlpha, 0, 0, 10f, 10f, 10f, 10f);
			pose.popPose();
		}

		pose.popPose();
	}

	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo ci)
	{
		soulShardVel1 = new Vec2(Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10),
				Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10));
		soulShardVel2 = new Vec2(Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10),
				Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10));
		soulShardVel3 = new Vec2(Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10),
				Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10));
		soulShardVel4 = new Vec2(Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10),
				Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10));
		soulShardVel5 = new Vec2(Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10),
				Mth.randomBetweenInclusive(minecraft.level.getRandom(), -10, 10));
	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void tick(CallbackInfo ci)
	{
		if(!diedWithSoulHearth)
			return;

		if (tick >= tickSoulShatter) {
			soulShardVel1 = new Vec2(soulShardVel1.x * horizontalFriction, soulShardVel1.y + gravity);
			soulShardPos1 = soulShardPos1.add(soulShardVel1);

			soulShardVel2 = new Vec2(soulShardVel2.x * horizontalFriction, soulShardVel2.y + gravity);
			soulShardPos2 = soulShardPos2.add(soulShardVel2);

			soulShardVel3 = new Vec2(soulShardVel3.x * horizontalFriction, soulShardVel3.y + gravity);
			soulShardPos3 = soulShardPos3.add(soulShardVel3);

			soulShardVel4 = new Vec2(soulShardVel4.x * horizontalFriction, soulShardVel4.y + gravity);
			soulShardPos4 = soulShardPos4.add(soulShardVel4);

			soulShardVel5 = new Vec2(soulShardVel5.x * horizontalFriction, soulShardVel5.y + gravity);
			soulShardPos5 = soulShardPos5.add(soulShardVel5);

			if (tick % 3 == 0) {
				if (fragment < 4) {
					fragment++;
				} else {
					fragment = 1;
				}
			}
		}

		if (tick == tickSoulBreak) {
			minecraft.player.playSound(SoundRegistry.SOUL_BREAK.get(), 0.5f, 1f);
		}
		if (tick == tickSoulShatter) {
			minecraft.player.playSound(SoundRegistry.SOUL_SHATTER.get(), 0.5f, 1f);
		}

		tick++;
	}
}