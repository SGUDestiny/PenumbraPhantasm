package destiny.penumbra_phantasm.client.render.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class DarkWorldRecipeBookTabButton extends StateSwitchingButton {
    private final RecipeBookCategories category;
    private static final float ANIMATION_TIME = 15.0F;
    private float animationTime;

    public DarkWorldRecipeBookTabButton(RecipeBookCategories pCategory) {
        super(0, 0, 36, 27, false);
        this.category = pCategory;
        this.initTextureValues(153, 2, 35, 0, DarkWorldRecipeBookComponent.RECIPE_BOOK_LOCATION);
    }

    public void startAnimation(Minecraft pMinecraft) {
        ClientRecipeBook $$1 = pMinecraft.player.getRecipeBook();
        List<RecipeCollection> $$2 = $$1.getCollection(this.category);
        if (pMinecraft.player.containerMenu instanceof RecipeBookMenu) {
            for(RecipeCollection $$3 : $$2) {
                for(Recipe<?> $$4 : $$3.getRecipes($$1.isFiltering((RecipeBookMenu)pMinecraft.player.containerMenu))) {
                    if ($$1.willHighlight($$4)) {
                        this.animationTime = 15.0F;
                        return;
                    }
                }
            }

        }
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.animationTime > 0.0F) {
            float $$4 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float)Math.PI));
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
            pGuiGraphics.pose().scale(1.0F, $$4, 1.0F);
            pGuiGraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
        }

        Minecraft $$5 = Minecraft.getInstance();
        RenderSystem.disableDepthTest();
        int $$6 = this.xTexStart;
        int $$7 = this.yTexStart;
        if (this.isStateTriggered) {
            $$6 += this.xDiffTex + 1;
        }

        if (this.isHoveredOrFocused()) {
            $$7 += this.yDiffTex;
        }

        int $$8 = this.getX();
        if (this.isStateTriggered) {
            $$8 -= 2;
        }

        pGuiGraphics.blit(this.resourceLocation, $$8, this.getY(), $$6, $$7, this.width, this.height);
        RenderSystem.enableDepthTest();
        this.renderIcon(pGuiGraphics, $$5.getItemRenderer());
        if (this.animationTime > 0.0F) {
            pGuiGraphics.pose().popPose();
            this.animationTime -= pPartialTick;
        }

    }

    private void renderIcon(GuiGraphics pGuiGraphics, ItemRenderer pItemRenderer) {
        List<ItemStack> $$2 = this.category.getIconItems();
        int $$3 = this.isStateTriggered ? -2 : 0;
        if ($$2.size() == 1) {
            pGuiGraphics.renderFakeItem((ItemStack)$$2.get(0), this.getX() + 9 + $$3, this.getY() + 5);
        } else if ($$2.size() == 2) {
            pGuiGraphics.renderFakeItem((ItemStack)$$2.get(0), this.getX() + 3 + $$3, this.getY() + 5);
            pGuiGraphics.renderFakeItem((ItemStack)$$2.get(1), this.getX() + 14 + $$3, this.getY() + 5);
        }

    }

    public RecipeBookCategories getCategory() {
        return this.category;
    }

    public boolean updateVisibility(ClientRecipeBook pRecipeBook) {
        List<RecipeCollection> $$1 = pRecipeBook.getCollection(this.category);
        this.visible = false;
        if ($$1 != null) {
            for(RecipeCollection $$2 : $$1) {
                if ($$2.hasKnownRecipes() && $$2.hasFitting()) {
                    this.visible = true;
                    break;
                }
            }
        }

        return this.visible;
    }
}