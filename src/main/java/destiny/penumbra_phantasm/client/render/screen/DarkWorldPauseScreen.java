package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.realmsclient.RealmsMainScreen;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.screen.component.DarkWorldButton;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ModListScreen;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class DarkWorldPauseScreen extends PauseScreen {
    ResourceLocation WIDGETS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets.png");

    private static final int COLUMNS = 2;
    private static final int MENU_PADDING_TOP = 50;
    private static final int BUTTON_PADDING = 4;
    private static final int BUTTON_WIDTH_FULL = 204;
    private static final int BUTTON_WIDTH_HALF = 98;
    private static final Component RETURN_TO_GAME = Component.literal(Component.translatable("menu.returnToGame").getString().toUpperCase());
    private static final Component ADVANCEMENTS = Component.literal(Component.translatable("gui.advancements").getString().toUpperCase());
    private static final Component STATS = Component.literal(Component.translatable("gui.stats").getString().toUpperCase());
    private static final Component SEND_FEEDBACK = Component.literal(Component.translatable("menu.sendFeedback").getString().toUpperCase());
    private static final Component REPORT_BUGS = Component.literal(Component.translatable("menu.reportBugs").getString().toUpperCase());
    private static final Component OPTIONS = Component.literal(Component.translatable("menu.options").getString().toUpperCase());
    private static final Component SHARE_TO_LAN = Component.literal(Component.translatable("menu.shareToLan").getString().toUpperCase());
    private static final Component PLAYER_REPORTING = Component.literal(Component.translatable("menu.playerReporting").getString().toUpperCase());
    private static final Component RETURN_TO_MENU = Component.literal(Component.translatable("menu.returnToMenu").getString().toUpperCase());
    private static final Component DISCONNECT = Component.literal(Component.translatable("menu.disconnect").getString().toUpperCase());
    private static final Component SAVING_LEVEL = Component.literal(Component.translatable("menu.savingLevel").getString().toUpperCase());
    private static final Component MODS = Component.literal(Component.translatable("fml.menu.mods").getString().toUpperCase());
    private static final Component GAME = Component.literal(Component.translatable("menu.game").getString().toUpperCase());
    private static final Component PAUSED = Component.literal(Component.translatable("menu.paused").getString().toUpperCase());
    private final boolean showPauseMenu;
    @Nullable
    private DarkWorldButton disconnectButton;

    public DarkWorldPauseScreen(boolean pShowPauseMenu) {
        super(pShowPauseMenu);
        this.showPauseMenu = pShowPauseMenu;
    }

    @Override
    public Component getTitle() {
        return showPauseMenu ? GAME : PAUSED;
    }

    @Override
    protected void init() {
        if (this.showPauseMenu) {
            this.createPauseMenu();
        }

        boolean isSinglePlayer = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();

        this.addRenderableWidget(new StringWidget(0, this.showPauseMenu ? 40 : 10, this.width, 9, isSinglePlayer ? PAUSED : GAME, this.font));
    }

    private void createPauseMenu() {
        boolean isSinglePlayer = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();

        GridLayout gridlayout = new GridLayout();
        gridlayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper gridlayout$rowhelper = gridlayout.createRowHelper(2);
        gridlayout$rowhelper.addChild(DarkWorldButton.builder(RETURN_TO_GAME, (p_280814_) -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();}
                ).texture(WIDGETS)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .size(220, 28)
                .build(), 2, gridlayout.newCellSettings().paddingTop(50));
        gridlayout$rowhelper.addChild(this.openScreenButton(ADVANCEMENTS, () -> new AdvancementsScreen(this.minecraft.player.connection.getAdvancements())));
        gridlayout$rowhelper.addChild(this.openScreenButton(STATS, () -> new StatsScreen(this, this.minecraft.player.getStats())));
        gridlayout$rowhelper.addChild(this.openLinkButton(SEND_FEEDBACK, SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game"));
        (gridlayout$rowhelper.addChild(this.openLinkButton(REPORT_BUGS, "https://aka.ms/snapshotbugs?ref=game"))).active = !SharedConstants.getCurrentVersion().getDataVersion().isSideSeries();
        gridlayout$rowhelper.addChild(this.openScreenButton(OPTIONS, () -> new OptionsScreen(this, this.minecraft.options)));
        if (isSinglePlayer) {
            gridlayout$rowhelper.addChild(this.openScreenButton(SHARE_TO_LAN, () -> new ShareToLanScreen(this)));
        } else {
            gridlayout$rowhelper.addChild(this.openScreenButton(PLAYER_REPORTING, SocialInteractionsScreen::new));
        }

        gridlayout$rowhelper.addChild(DarkWorldButton.builder(MODS, (button) ->
                this.minecraft.setScreen(new ModListScreen(this)))
                .texture(WIDGETS)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .size(220, 28)
                .build(), 2);
        Component component = this.minecraft.isLocalServer() ? RETURN_TO_MENU : DISCONNECT;
        this.disconnectButton = gridlayout$rowhelper.addChild(DarkWorldButton.builder(component, (button) -> {
            button.active = false;
            this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::onDisconnect, true);}
                ).texture(WIDGETS)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .size(220, 28)
                .build(), 2);
        gridlayout.arrangeElements();
        FrameLayout.alignInRectangle(gridlayout, 0, 0, this.width, this.height, 0.5F, 0.25F);
        gridlayout.visitWidgets(this::addRenderableWidget);
    }

    private void onDisconnect() {
        boolean flag = this.minecraft.isLocalServer();
        boolean flag1 = this.minecraft.isConnectedToRealms();
        this.minecraft.level.disconnect();
        if (flag) {
            this.minecraft.clearLevel(new GenericDirtMessageScreen(SAVING_LEVEL));
        } else {
            this.minecraft.clearLevel();
        }

        TitleScreen titlescreen = new TitleScreen();
        if (flag) {
            this.minecraft.setScreen(titlescreen);
        } else if (flag1) {
            this.minecraft.setScreen(new RealmsMainScreen(titlescreen));
        } else {
            this.minecraft.setScreen(new JoinMultiplayerScreen(titlescreen));
        }

    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.showPauseMenu && this.minecraft != null && this.minecraft.getReportingContext().hasDraftReport() && this.disconnectButton != null) {
            pGuiGraphics.blit(WIDGETS, this.disconnectButton.getX() + this.disconnectButton.getWidth() - 17, this.disconnectButton.getY() + 3, 182, 24, 15, 15);
        }

    }

    private DarkWorldButton openScreenButton(Component pMessage, Supplier<Screen> pScreenSupplier) {
        return DarkWorldButton.builder(pMessage, (p_280817_) ->
                this.minecraft.setScreen(pScreenSupplier.get()))
                .texture(WIDGETS)
                .textureRegion(44, 132, 208, 84, 28, 3)
                .nineSlice(13,  28)
                .size(106, 28)
                .build();
    }

    private DarkWorldButton openLinkButton(Component pMessage, String pLinkUri) {
        return this.openScreenButton(pMessage, () -> new ConfirmLinkScreen((p_280813_) -> {
            if (p_280813_) {
                Util.getPlatform().openUri(pLinkUri);
            }

            this.minecraft.setScreen(this);
        }, pLinkUri, true));
    }
}
