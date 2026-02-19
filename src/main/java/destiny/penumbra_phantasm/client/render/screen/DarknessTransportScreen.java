package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.client.network.ServerBoundTransportIntroPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DarknessTransportScreen extends Screen {
    Minecraft minecraft = Minecraft.getInstance();
    public final Runnable onFinished;
    public final net.minecraft.core.BlockPos destinationPos;
    public final double spawnX;
    public final double spawnY;
    public final double spawnZ;
    public final float spawnYaw;
    public final ResourceKey<Level> dimension;

    public int tick = 0;
    public int screenDuration = 3 * 20;
    public boolean shouldClose = false;

    public DarknessTransportScreen(Runnable onFinished, net.minecraft.core.BlockPos destinationPos, double spawnX, double spawnY, double spawnZ, float spawnYaw, ResourceKey<Level> dimension) {
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

            if (tick >= 0 && tick <= 30) {
                if (tick == 1 || tick % 10 == 0) {
                    minecraft.player.playSound(SoundRegistry.DARK_WORLD_FALL.get(), 1, 1);
                }
            }

            tick++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
    }

    public void closeScreen() {
        onFinished.run();
        Minecraft.getInstance().getSoundManager().stop();
        PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundTransportIntroPacket(destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dimension));
    }
}
