package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.screen.component.DarkWorldButton;
import destiny.penumbra_phantasm.client.render.screen.component.DarkWorldCycleButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

import javax.annotation.Nullable;

public class DarkWorldLanScreen extends ShareToLanScreen {
    ResourceLocation WIDGETS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets.png");

    private static final int PORT_LOWER_BOUND = 1024;
    private static final int PORT_HIGHER_BOUND = 65535;
    private static final Component ALLOW_COMMANDS_LABEL = Component.literal(Component.translatable("selectWorld.allowCommands").getString().toUpperCase());
    private static final Component GAME_MODE_LABEL = Component.literal(Component.translatable("selectWorld.gameMode").getString().toUpperCase());
    private static final Component INFO_TEXT = Component.literal(Component.translatable("lanServer.otherPlayers").getString().toUpperCase());
    private static final Component PORT_INFO_TEXT = Component.literal(Component.translatable("lanServer.port").getString().toUpperCase());
    private static final Component START = Component.literal(Component.translatable("lanServer.start").getString().toUpperCase());
    private static final Component PORT_UNAVAILABLE = Component.literal(Component.translatable("lanServer.port.unavailable.new", 1024, 65535).getString().toUpperCase());
    private static final Component INVALID_PORT = Component.literal(Component.translatable("lanServer.port.invalid.new", 1024, 65535).getString().toUpperCase());
    private static final int INVALID_PORT_COLOR = 16733525;
    private static final Component TITLE = Component.literal(Component.translatable("lanServer.title").getString().toUpperCase());
    private static final Component PUBLISH_FAILED = Component.literal(Component.translatable("commands.publish.failed").getString().toUpperCase());
    private final Screen lastScreen;
    private GameType gameMode;
    private boolean commands;
    private int port;
    @Nullable
    private EditBox portEdit;

    public DarkWorldLanScreen(Screen pLastScreen) {
        super(pLastScreen);
        this.gameMode = GameType.SURVIVAL;
        this.port = HttpUtil.getAvailablePort();
        this.lastScreen = pLastScreen;
    }

    @Override
    protected void init() {
        IntegratedServer $$0 = this.minecraft.getSingleplayerServer();
        this.gameMode = $$0.getDefaultGameType();
        this.commands = $$0.getWorldData().getAllowCommands();
        this.addRenderableWidget(DarkWorldCycleButton.builder(GameType::getShortDisplayName)
                .withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE)
                .withInitialValue(this.gameMode)
                .texture(WIDGETS)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .create(this.width / 2 - 155, 100, 150 + 8, 20 + 8, GAME_MODE_LABEL,
                        (p_169429_, p_169430_) -> this.gameMode = p_169430_));
        this.addRenderableWidget(DarkWorldCycleButton.onOffBuilder(this.commands)
                .texture(WIDGETS)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .create(this.width / 2 + 5, 100, 150 + 8, 20 + 8, ALLOW_COMMANDS_LABEL,
                        (p_169432_, p_169433_) -> this.commands = p_169433_));
        DarkWorldButton $$1 = DarkWorldButton.builder(START, (p_280826_) -> {
            this.minecraft.setScreen(null);
            Component $$2;
            if ($$0.publishServer(this.gameMode, this.commands, this.port)) {
                $$2 = Component.literal(PublishCommand.getSuccessMessage(this.port).getString().toUpperCase());
            } else {
                $$2 = PUBLISH_FAILED;
            }

            this.minecraft.gui.getChat().addMessage($$2);
            this.minecraft.updateTitle();}
                ).texture(WIDGETS)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .bounds(this.width / 2 - 155 + 4, this.height - 28, 150 + 8, 20 + 8)
                .build();
        this.portEdit = new EditBox(this.font, this.width / 2 - 75, 160, 150, 20, PORT_INFO_TEXT);
        this.portEdit.setResponder((p_258130_) -> {
            Component $$2 = this.tryParsePort(p_258130_);
            this.portEdit.setHint(Component.literal("" + this.port).withStyle(ChatFormatting.DARK_GRAY));
            if ($$2 == null) {
                this.portEdit.setTextColor(14737632);
                this.portEdit.setTooltip(null);
                $$1.active = true;
            } else {
                this.portEdit.setTextColor(INVALID_PORT_COLOR);
                this.portEdit.setTooltip(Tooltip.create($$2));
                $$1.active = false;
            }

        });
        this.portEdit.setHint(Component.literal("" + this.port).withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(this.portEdit);
        this.addRenderableWidget($$1);
        this.addRenderableWidget(DarkWorldButton.builder(Component.literal(CommonComponents.GUI_CANCEL.getString().toUpperCase()), (p_280824_) ->
                this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 5, this.height - 28, 150, 20
                ).texture(WIDGETS)
                .size(150 + 8, 20 + 8)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .build());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.portEdit != null) {
            this.portEdit.tick();
        }

    }

    @Nullable
    private Component tryParsePort(String pPort) {
        if (pPort.isBlank()) {
            this.port = HttpUtil.getAvailablePort();
            return null;
        } else {
            try {
                this.port = Integer.parseInt(pPort);
                if (this.port >= PORT_LOWER_BOUND && this.port <= PORT_HIGHER_BOUND) {
                    return !HttpUtil.isPortAvailable(this.port) ? PORT_UNAVAILABLE : null;
                } else {
                    return INVALID_PORT;
                }
            } catch (NumberFormatException var3) {
                this.port = HttpUtil.getAvailablePort();
                return INVALID_PORT;
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        pGuiGraphics.drawCenteredString(this.font, TITLE, this.width / 2, 50, 16777215);
        pGuiGraphics.drawCenteredString(this.font, INFO_TEXT, this.width / 2, 82, 16777215);
        pGuiGraphics.drawCenteredString(this.font, PORT_INFO_TEXT, this.width / 2, 142, 16777215);

        for(Renderable renderable : this.renderables) {
            renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
    }
}
