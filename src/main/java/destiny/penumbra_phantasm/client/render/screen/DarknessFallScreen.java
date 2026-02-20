package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ServerBoundDarknessFallPacket;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DarknessFallScreen extends Screen {
    Minecraft minecraft = Minecraft.getInstance();

    public static final ResourceLocation FRAME = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/darkness_fall.png");
    public final Runnable onFinished;
    public final net.minecraft.core.BlockPos destinationPos;
    public final double spawnX;
    public final double spawnY;
    public final double spawnZ;
    public final float spawnYaw;
    public final ResourceKey<Level> dimension;

    public int tick = 0;
    public int screenDuration = 35;
    public boolean shouldClose = false;
    public final float frameLifeTime = 15f;
    public List<Float> activeFrames = new ArrayList<>();

    public DarknessFallScreen(Runnable onFinished, net.minecraft.core.BlockPos destinationPos, double spawnX, double spawnY, double spawnZ, float spawnYaw, ResourceKey<Level> dimension) {
        super(GameNarrator.NO_TITLE);
        this.onFinished = onFinished;
        this.destinationPos = destinationPos;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.spawnYaw = spawnYaw;
        this.dimension = dimension;
    }

    @Override
    public void tick() {
        if (shouldClose) {
            closeScreen();
        } else {
            if (tick >= screenDuration) {
                shouldClose = true;
            }
            if (!activeFrames.isEmpty()) {
                for (int i = 0; i < activeFrames.size(); i++) {
                    if (activeFrames.get(i) >= frameLifeTime) {
                        activeFrames.remove(i);
                    } else {
                        activeFrames.set(i, activeFrames.get(i) + 1f);
                    }
                }
            }

            if (tick >= 0 && tick <= 12) {
                if (tick == 1 || tick % 4 == 0) {
                    minecraft.player.playSound(SoundRegistry.DARK_WORLD_FALL.get(), 0.5f, 1);
                    activeFrames.add(0f);
                }
            }

            tick++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);

        PoseStack pose = graphics.pose();
        pose.pushPose();

        List<Float> sortedFrames = new ArrayList<>(activeFrames);
        sortedFrames.sort(Collections.reverseOrder());

        for (float frameTick : sortedFrames) {
            float frameLifeTimeDelta = frameTick / frameLifeTime;
            float frameAlpha = frameLifeTimeDelta <= 0.5f
                    ? Mth.lerp(frameLifeTimeDelta * 2.0f, 0f, 1f)
                    : Mth.lerp((frameLifeTimeDelta - 0.5f) * 2.0f, 1f, 0f);
            float frameSize = Mth.lerp(frameLifeTimeDelta, 0f, 3f);
            float frameRotation = Mth.lerp(frameLifeTimeDelta, 10, -10f);

            pose.pushPose();
            pose.translate(this.width / 2f, this.height / 2f, 0f);
            pose.scale(frameSize, frameSize, 1f);
            pose.rotateAround(Axis.ZP.rotationDegrees(frameRotation), 0, 0, 0f);
            pose.translate(-256 / 2f, -256 / 2f, 0f);
            RenderBlitUtil.blit(FRAME, pose, 0, 0, 1f, 1f, 1f, frameAlpha, 0, 0, 256, 256, 256, 256);
            pose.popPose();
        }

        pose.popPose();
    }

    public void closeScreen() {
        onFinished.run();
        Minecraft.getInstance().getSoundManager().stop();
        PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundDarknessFallPacket(destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dimension));
        Minecraft.getInstance().player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).ifPresent(anim -> anim.darknessLandTicker = 0);
    }
}
