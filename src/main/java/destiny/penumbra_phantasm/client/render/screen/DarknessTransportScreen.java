package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.client.network.ServerBoundTransportIntroPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class DarknessTransportScreen extends Screen {

    private static final int DURATION_TICKS = 3 * 20;

    private final Runnable onFinished;
    private final net.minecraft.core.BlockPos destinationPos;
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;
    private final float spawnYaw;
    private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;

    private int tick = 0;
    private boolean shouldClose = false;

    public DarknessTransportScreen(Runnable onFinished, net.minecraft.core.BlockPos destinationPos,
                                   double spawnX, double spawnY, double spawnZ, float spawnYaw,
                                   net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
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
            tick++;
            if (tick >= DURATION_TICKS) {
                shouldClose = true;
            }
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
