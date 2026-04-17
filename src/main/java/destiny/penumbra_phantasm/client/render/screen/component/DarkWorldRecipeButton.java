package destiny.penumbra_phantasm.client.render.screen.component;

import com.google.common.collect.Lists;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class DarkWorldRecipeButton extends AbstractWidget {
    private static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/recipe_book.png");
    private static final float ANIMATION_TIME = 15.0F;
    private static final int BACKGROUND_SIZE = 25;
    public static final int TICKS_TO_SWAP = 30;
    private static final Component MORE_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.moreRecipes");
    private RecipeBookMenu<?> menu;
    private RecipeBook book;
    private RecipeCollection collection;
    private float time;
    private float animationTime;
    private int currentIndex;

    public DarkWorldRecipeButton() {
        super(0, 0, 25, 25, CommonComponents.EMPTY);
    }

    public void init(RecipeCollection pCollection, DarkWorldRecipeBookPage pRecipeBookPage) {
        this.collection = pCollection;
        this.menu = (RecipeBookMenu)pRecipeBookPage.getMinecraft().player.containerMenu;
        this.book = pRecipeBookPage.getRecipeBook();
        List<Recipe<?>> $$2 = pCollection.getRecipes(this.book.isFiltering(this.menu));

        for(Recipe<?> $$3 : $$2) {
            if (this.book.willHighlight($$3)) {
                pRecipeBookPage.recipesShown($$2);
                this.animationTime = 15.0F;
                break;
            }
        }

    }

    public RecipeCollection getCollection() {
        return this.collection;
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (!Screen.hasControlDown()) {
            this.time += pPartialTick;
        }

        Minecraft $$4 = Minecraft.getInstance();
        int $$5 = 29;
        if (!this.collection.hasCraftable()) {
            $$5 += 25;
        }

        int $$6 = 206;
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            $$6 += 25;
        }

        boolean $$7 = this.animationTime > 0.0F;
        if ($$7) {
            float $$8 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float)Math.PI));
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
            pGuiGraphics.pose().scale($$8, $$8, 1.0F);
            pGuiGraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
            this.animationTime -= pPartialTick;
        }

        pGuiGraphics.blit(RECIPE_BOOK_LOCATION, this.getX(), this.getY(), $$5, $$6, this.width, this.height);
        List<Recipe<?>> $$9 = this.getOrderedRecipes();
        this.currentIndex = Mth.floor(this.time / 30.0F) % $$9.size();
        ItemStack $$10 = ((Recipe)$$9.get(this.currentIndex)).getResultItem(this.collection.registryAccess());
        int $$11 = 4;
        if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
            pGuiGraphics.renderItem($$10, this.getX() + $$11 + 1, this.getY() + $$11 + 1, 0, 10);
            --$$11;
        }

        pGuiGraphics.renderFakeItem($$10, this.getX() + $$11, this.getY() + $$11);
        if ($$7) {
            pGuiGraphics.pose().popPose();
        }

    }

    private List<Recipe<?>> getOrderedRecipes() {
        List<Recipe<?>> $$0 = this.collection.getDisplayRecipes(true);
        if (!this.book.isFiltering(this.menu)) {
            $$0.addAll(this.collection.getDisplayRecipes(false));
        }

        return $$0;
    }

    public boolean isOnlyOption() {
        return this.getOrderedRecipes().size() == 1;
    }

    public Recipe<?> getRecipe() {
        List<Recipe<?>> $$0 = this.getOrderedRecipes();
        return (Recipe)$$0.get(this.currentIndex);
    }

    public List<Component> getTooltipText() {
        ItemStack $$0 = ((Recipe)this.getOrderedRecipes().get(this.currentIndex)).getResultItem(this.collection.registryAccess());
        List<Component> $$1 = Lists.newArrayList(Screen.getTooltipFromItem(Minecraft.getInstance(), $$0));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            $$1.add(MORE_RECIPES_TOOLTIP);
        }

        return $$1;
    }

    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        ItemStack $$1 = ((Recipe)this.getOrderedRecipes().get(this.currentIndex)).getResultItem(this.collection.registryAccess());
        pNarrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.recipe", new Object[]{$$1.getHoverName()}));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            pNarrationElementOutput.add(NarratedElementType.USAGE, new Component[]{Component.translatable("narration.button.usage.hovered"), Component.translatable("narration.recipe.usage.more")});
        } else {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
        }

    }

    public int getWidth() {
        return 25;
    }

    protected boolean isValidClickButton(int pButton) {
        return pButton == 0 || pButton == 1;
    }
}