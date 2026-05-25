package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.menu.CheshireChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class CheshireChestScreen extends AbstractContainerScreen<CheshireChestMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/cheshire_chest.png");
    private static final ResourceLocation TEXTURE_GLOW = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/cheshire_chest_glow.png");

    public static final int GLOW_TICKER_UPPER_BOUND = 5 * 20;

    public Component inventoryLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_title");
    public Component chestLabel = Component.translatable("gui.penumbra_phantasm.cheshire_chest");

    private int glowTicker = 0;

    public CheshireChestScreen(CheshireChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 168;
        this.imageWidth = 188;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = this.inventoryLabelX + 17;
        this.inventoryLabelY = this.inventoryLabelY - 1;
        this.titleLabelX = this.titleLabelX + 17;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.glowTicker = this.minecraft.level.random.nextInt(0, 21);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.glowTicker++;
        if (this.glowTicker >= GLOW_TICKER_UPPER_BOUND) {
            this.glowTicker = 0;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        float t = (float) this.glowTicker / (float) GLOW_TICKER_UPPER_BOUND;
        float glow = Mth.sin(t * Mth.PI);

        graphics.blit(TEXTURE, this.leftPos - 6, this.topPos + 4, 0, 0, this.imageWidth, this.imageHeight);
        RenderBlitUtil.blitGui(graphics, TEXTURE_GLOW, this.leftPos - 6, this.topPos + 4, 0, 0, this.imageWidth, this.imageHeight, glow, glow, glow, 1);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, chestLabel, this.titleLabelX, this.titleLabelY, -1, false);
        pGuiGraphics.drawString(this.font, inventoryLabel, this.inventoryLabelX, this.inventoryLabelY, -1, false);
    }
}