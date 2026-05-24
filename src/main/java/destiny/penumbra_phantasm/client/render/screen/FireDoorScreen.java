package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.client.render.screen.component.FireDoorScreenButton;
import destiny.penumbra_phantasm.server.fountain.FireDoor;
import destiny.penumbra_phantasm.server.network.ServerBoundFireDoorScreenPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class FireDoorScreen extends Screen {
    private final List<FireDoor> doors;
    private final ResourceKey<Level> originDarkWorld;
    private final BlockPos originPos;
    private final Consumer<FireDoor> onExternalSelect;
    private boolean destinationSelected = false;

    public FireDoorScreen(List<FireDoor> doors, ResourceKey<Level> originDarkWorld, BlockPos originPos, Consumer<FireDoor> onExternalSelect) {
        super(GameNarrator.NO_TITLE);
        this.doors = doors;
        this.originDarkWorld = originDarkWorld;
        this.originPos = originPos;
        this.onExternalSelect = onExternalSelect;
    }

    @Override
    protected void init() {
        super.init();
        int maxButtons = Math.min(doors.size(), 10);
        int buttonWidth = 128;
        int buttonHeight = 24;
        int gap = 8;

        int totalHeight = maxButtons * buttonHeight + (maxButtons - 1) * gap;
        int startY = (this.height - totalHeight) / 2;
        int x = (this.width - buttonWidth) / 2;

        for (int i = 0; i < maxButtons; i++) {
            FireDoor door = doors.get(i);
            int y = startY + i * (buttonHeight + gap);

            FireDoorScreenButton button = new FireDoorScreenButton(x, y, door.name(), button1 -> {
                destinationSelected = true;
                onExternalSelect.accept(door);
            });
            this.addRenderableWidget(button);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        if (!destinationSelected) {
            PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundFireDoorScreenPacket(originDarkWorld, originPos));
        }
        super.removed();
    }
}