package destiny.penumbra_phantasm.client.render.screen.component;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DarkWorldOverlayRecipeComponent extends OverlayRecipeComponent implements Renderable, GuiEventListener {
    static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/recipe_book.png");
    private static final int MAX_ROW = 4;
    private static final int MAX_ROW_LARGE = 5;
    private static final float ITEM_RENDER_SCALE = 0.375F;
    public static final int BUTTON_SIZE = 25;
    private final List<OverlayRecipeButton> recipeButtons = Lists.newArrayList();
    private boolean isVisible;
    private int x;
    private int y;
    private Minecraft minecraft;
    private RecipeCollection collection;
    @Nullable
    private Recipe<?> lastRecipeClicked;
    float time;
    boolean isFurnaceMenu;

    public DarkWorldOverlayRecipeComponent() {
    }

    public void init(Minecraft pMinecraft, RecipeCollection pCollection, int pX, int pY, int p_100199_, int p_100200_, float p_100201_) {
        this.minecraft = pMinecraft;
        this.collection = pCollection;
        if (pMinecraft.player.containerMenu instanceof AbstractFurnaceMenu) {
            this.isFurnaceMenu = true;
        }

        boolean $$7 = pMinecraft.player.getRecipeBook().isFiltering((RecipeBookMenu)pMinecraft.player.containerMenu);
        List<Recipe<?>> $$8 = pCollection.getDisplayRecipes(true);
        List<Recipe<?>> $$9 = $$7 ? Collections.emptyList() : pCollection.getDisplayRecipes(false);
        int $$10 = $$8.size();
        int $$11 = $$10 + $$9.size();
        int $$12 = $$11 <= 16 ? 4 : 5;
        int $$13 = (int)Math.ceil((double)((float)$$11 / (float)$$12));
        this.x = pX;
        this.y = pY;
        float $$14 = (float)(this.x + Math.min($$11, $$12) * 25);
        float $$15 = (float)(p_100199_ + 50);
        if ($$14 > $$15) {
            this.x = (int)((float)this.x - p_100201_ * (float)((int)(($$14 - $$15) / p_100201_)));
        }

        float $$16 = (float)(this.y + $$13 * 25);
        float $$17 = (float)(p_100200_ + 50);
        if ($$16 > $$17) {
            this.y = (int)((float)this.y - p_100201_ * (float) Mth.ceil(($$16 - $$17) / p_100201_));
        }

        float $$18 = (float)this.y;
        float $$19 = (float)(p_100200_ - 100);
        if ($$18 < $$19) {
            this.y = (int)((float)this.y - p_100201_ * (float)Mth.ceil(($$18 - $$19) / p_100201_));
        }

        this.isVisible = true;
        this.recipeButtons.clear();

        for(int $$20 = 0; $$20 < $$11; ++$$20) {
            boolean $$21 = $$20 < $$10;
            Recipe<?> $$22 = $$21 ? (Recipe)$$8.get($$20) : (Recipe)$$9.get($$20 - $$10);
            int $$23 = this.x + 4 + 25 * ($$20 % $$12);
            int $$24 = this.y + 5 + 25 * ($$20 / $$12);
            if (this.isFurnaceMenu) {
                this.recipeButtons.add(new OverlaySmeltingRecipeButton($$23, $$24, $$22, $$21));
            } else {
                this.recipeButtons.add(new OverlayRecipeButton($$23, $$24, $$22, $$21));
            }
        }

        this.lastRecipeClicked = null;
    }

    public RecipeCollection getRecipeCollection() {
        return this.collection;
    }

    @Nullable
    public Recipe<?> getLastRecipeClicked() {
        return this.lastRecipeClicked;
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0) {
            return false;
        } else {
            for(OverlayRecipeButton $$3 : this.recipeButtons) {
                if ($$3.mouseClicked(pMouseX, pMouseY, pButton)) {
                    this.lastRecipeClicked = $$3.recipe;
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return false;
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.isVisible) {
            this.time += pPartialTick;
            RenderSystem.enableBlend();
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 1000.0F);
            int $$4 = this.recipeButtons.size() <= 16 ? 4 : 5;
            int $$5 = Math.min(this.recipeButtons.size(), $$4);
            int $$6 = Mth.ceil((float)this.recipeButtons.size() / (float)$$4);
            int $$7 = 4;
            pGuiGraphics.blitNineSliced(RECIPE_BOOK_LOCATION, this.x, this.y, $$5 * 25 + 8, $$6 * 25 + 8, 4, 32, 32, 82, 208);
            RenderSystem.disableBlend();

            for(OverlayRecipeButton $$8 : this.recipeButtons) {
                $$8.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }

            pGuiGraphics.pose().popPose();
        }
    }

    public void setVisible(boolean pIsVisible) {
        this.isVisible = pIsVisible;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setFocused(boolean pFocused) {
    }

    public boolean isFocused() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    class OverlaySmeltingRecipeButton extends OverlayRecipeButton {
        public OverlaySmeltingRecipeButton(int p_100262_, int p_100263_, Recipe<?> p_100264_, boolean p_100265_) {
            super(p_100262_, p_100263_, p_100264_, p_100265_);
        }

        protected void calculateIngredientsPositions(Recipe<?> p_100267_) {
            ItemStack[] $$1 = ((Ingredient)p_100267_.getIngredients().get(0)).getItems();
            this.ingredientPos.add(new OverlayRecipeButton.Pos(10, 10, $$1));
        }
    }

    @OnlyIn(Dist.CLIENT)
    class OverlayRecipeButton extends AbstractWidget implements PlaceRecipe<Ingredient> {
        final Recipe<?> recipe;
        private final boolean isCraftable;
        protected final List<OverlayRecipeButton.Pos> ingredientPos = Lists.newArrayList();

        public OverlayRecipeButton(int pX, int pY, Recipe<?> pRecipe, boolean pIsCraftable) {
            super(pX, pY, 200, 20, CommonComponents.EMPTY);
            this.width = 24;
            this.height = 24;
            this.recipe = pRecipe;
            this.isCraftable = pIsCraftable;
            this.calculateIngredientsPositions(pRecipe);
        }

        protected void calculateIngredientsPositions(Recipe<?> pRecipe) {
            this.placeRecipe(3, 3, -1, pRecipe, pRecipe.getIngredients().iterator(), 0);
        }

        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }

        public void addItemToSlot(Iterator<Ingredient> pIngredients, int pSlot, int pMaxAmount, int pY, int pX) {
            ItemStack[] $$5 = ((Ingredient)pIngredients.next()).getItems();
            if ($$5.length != 0) {
                this.ingredientPos.add(new OverlayRecipeButton.Pos(3 + pX * 7, 3 + pY * 7, $$5));
            }

        }

        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            int $$4 = 152;
            if (!this.isCraftable) {
                $$4 += 26;
            }

            int $$5 = isFurnaceMenu ? 130 : 78;
            if (this.isHoveredOrFocused()) {
                $$5 += 26;
            }

            pGuiGraphics.blit(RECIPE_BOOK_LOCATION, this.getX(), this.getY(), $$4, $$5, this.width, this.height);
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate((double)(this.getX() + 2), (double)(this.getY() + 2), (double)150.0F);

            for(OverlayRecipeButton.Pos $$6 : this.ingredientPos) {
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate((double)$$6.x, (double)$$6.y, (double)0.0F);
                pGuiGraphics.pose().scale(0.375F, 0.375F, 1.0F);
                pGuiGraphics.pose().translate((double)-8.0F, (double)-8.0F, (double)0.0F);
                if ($$6.ingredients.length > 0) {
                    pGuiGraphics.renderItem($$6.ingredients[Mth.floor(time / 30.0F) % $$6.ingredients.length], 0, 0);
                }

                pGuiGraphics.pose().popPose();
            }

            pGuiGraphics.pose().popPose();
        }

        @OnlyIn(Dist.CLIENT)
        protected class Pos {
            public final ItemStack[] ingredients;
            public final int x;
            public final int y;

            public Pos(int pX, int pY, ItemStack[] pIngredients) {
                this.x = pX;
                this.y = pY;
                this.ingredients = pIngredients;
            }
        }
    }
}