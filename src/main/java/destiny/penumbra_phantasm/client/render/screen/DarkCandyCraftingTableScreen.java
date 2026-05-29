package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.menu.DarkCandyCraftingTableMenu;
import destiny.penumbra_phantasm.client.render.screen.component.DarkWorldRecipeBookComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class DarkCandyCraftingTableScreen extends AbstractContainerScreen<DarkCandyCraftingTableMenu> implements RecipeUpdateListener {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_candy_crafting_table.png");
    private static final ResourceLocation CRAFTING_TABLE_GLOW_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_candy_crafting_table_glow.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID,"textures/gui/dark_world/recipe_button.png");

    public static final int GLOW_TICKER_UPPER_BOUND = 5 * 20;

    private final DarkWorldRecipeBookComponent recipeBookComponent = new DarkWorldRecipeBookComponent();
    private boolean widthTooNarrow;

    private int glowTicker;

    public DarkCandyCraftingTableScreen(DarkCandyCraftingTableMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 184;
        this.imageHeight = 174;
        this.inventoryLabelX = this.inventoryLabelX + 17;
        this.inventoryLabelY = this.inventoryLabelY + 1;
        this.titleLabelX = this.titleLabelX + 6;
        this.titleLabelY = this.titleLabelY - 6;
    }

    @Override
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height - 2, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth - 4);
        this.topPos = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(new ImageButton(
                this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19,
                RECIPE_BUTTON_LOCATION, (p_289630_) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth - 4);
            p_289630_.setPosition(this.leftPos + 5, this.height / 2 - 49);
        }
        ));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);

        Random random = new Random();
        this.glowTicker = random.nextInt(0, 21);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -1, false);
        pGuiGraphics.drawString(this.font, Component.translatable("gui.penumbra_phantasm.dark_world_inventory_title"), this.inventoryLabelX, this.inventoryLabelY, -1, false);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();

        this.glowTicker++;
        if (this.glowTicker >= GLOW_TICKER_UPPER_BOUND) {
            this.glowTicker = 0;
        }
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick, 3);
        } else {
            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick, 3);
            super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            this.recipeBookComponent.renderGhostRecipe(pGuiGraphics, this.leftPos, this.topPos, true, pPartialTick);
        }

        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        this.recipeBookComponent.renderTooltip(pGuiGraphics, this.leftPos, this.topPos, pMouseX, pMouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        float t = (float) this.glowTicker / (float) GLOW_TICKER_UPPER_BOUND;
        float glow = Mth.sin(t * Mth.PI);

        int x = this.leftPos - 4;
        int y = this.topPos - 4;
        pGuiGraphics.blit(CRAFTING_TABLE_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
        RenderBlitUtil.blitGui(pGuiGraphics, CRAFTING_TABLE_GLOW_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight, glow, glow, glow, 1);
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.recipeBookComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
        boolean $$5 = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(pMouseX, pMouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pMouseButton) && $$5;
    }

    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        this.recipeBookComponent.slotClicked(pSlot);
    }

    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
