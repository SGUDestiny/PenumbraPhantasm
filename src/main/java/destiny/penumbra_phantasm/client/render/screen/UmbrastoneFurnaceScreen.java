package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.menu.UmbrastoneFurnaceMenu;
import destiny.penumbra_phantasm.client.render.screen.component.DarkWorldRecipeBookComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.Random;

public class UmbrastoneFurnaceScreen extends AbstractFurnaceScreen<UmbrastoneFurnaceMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/umbrastone_furnace.png");
    private static final ResourceLocation TEXTURE_GLOW = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/umbrastone_furnace_glow.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID,"textures/gui/dark_world/recipe_button.png");
    private final DarkWorldRecipeBookComponent recipeBookComponent = new DarkWorldRecipeBookComponent();
    private boolean widthTooNarrow;

    public static final int GLOW_TICKER_UPPER_BOUND = 5 * 20;

    public Component inventoryLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_title");
    public Component furnaceLabel = Component.translatable("gui.penumbra_phantasm.umbrastone_furnace");

    private int glowTicker;

    public UmbrastoneFurnaceScreen(UmbrastoneFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, new SmeltingRecipeBookComponent(), playerInventory, title, TEXTURE);
        this.imageHeight = 178;
        this.imageWidth = 188;
        this.inventoryLabelX = this.inventoryLabelX + 17;
        this.inventoryLabelY = this.inventoryLabelY + 1;
        this.titleLabelX = this.titleLabelX + 17;
        this.titleLabelY = this.titleLabelY - 8;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_289628_) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            p_289628_.setPosition(this.leftPos + 20, this.height / 2 - 49);
        }));
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        Random random = new Random();
        this.glowTicker = random.nextInt(0, 21);
    }

    @Override
    public void containerTick() {
        this.recipeBookComponent.tick();
        this.glowTicker++;
        if (this.glowTicker >= GLOW_TICKER_UPPER_BOUND) {
            this.glowTicker = 0;
        }
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        float t = (float) this.glowTicker / (float) GLOW_TICKER_UPPER_BOUND;
        float glow = Mth.sin(t * Mth.PI);

        pGuiGraphics.blit(TEXTURE, this.leftPos - 6, this.topPos - 6, 0, 0, this.imageWidth, this.imageHeight);
        RenderBlitUtil.blitGui(pGuiGraphics, TEXTURE_GLOW, this.leftPos - 6, this.topPos - 6, 0, 0, this.imageWidth, this.imageHeight, glow, glow, glow, 1);

        if ((this.menu).isLit()) {
            int litProgress = (this.menu).getLitProgress();
            pGuiGraphics.blit(TEXTURE, leftPos + 56, topPos + 36 + 12 - litProgress, 176 + 12, 12 - litProgress, 14, litProgress + 1);
        }

        int burnProgress = (this.menu).getBurnProgress();
        pGuiGraphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176 + 12, 14, burnProgress + 1, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, furnaceLabel, this.titleLabelX - 17, this.titleLabelY, -1, false);
        pGuiGraphics.drawString(this.font, inventoryLabel, this.inventoryLabelX, this.inventoryLabelY, -1, false);
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.recipeBookComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    @Override
    protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
        boolean $$5 = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(pMouseX, pMouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pMouseButton) && $$5;
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        this.recipeBookComponent.slotClicked(pSlot);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
