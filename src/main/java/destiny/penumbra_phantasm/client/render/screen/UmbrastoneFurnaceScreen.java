package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.menu.UmbrastoneFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class UmbrastoneFurnaceScreen extends AbstractFurnaceScreen<UmbrastoneFurnaceMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/umbrastone_furnace.png");

    public Component inventoryLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_title");
    public Component furnaceLabel = Component.translatable("gui.penumbra_phantasm.umbrastone_furnace");

    public UmbrastoneFurnaceScreen(UmbrastoneFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, new SmeltingRecipeBookComponent(), playerInventory, title, TEXTURE);
        this.imageHeight = 178;
        this.imageWidth = 188;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(TEXTURE, this.leftPos - 6, this.topPos - 6, 0, 0, this.imageWidth, this.imageHeight);
        if ((this.menu).isLit()) {
            int litProgress = (this.menu).getLitProgress();
            pGuiGraphics.blit(TEXTURE, leftPos + 56, topPos + 36 + 12 - litProgress, 176 + 12, 12 - litProgress, 14, litProgress + 1);
        }

        int burnProgress = (this.menu).getBurnProgress();
        pGuiGraphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176 + 12, 14, burnProgress + 1, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, furnaceLabel, this.titleLabelX, this.titleLabelY, -1, false);
        pGuiGraphics.drawString(this.font, inventoryLabel, this.inventoryLabelX, this.inventoryLabelY, -1, false);
    }
}
