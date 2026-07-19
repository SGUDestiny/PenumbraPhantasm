package destiny.penumbra_phantasm.client.render.screen.component;

import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractDarkWorldFurnaceRecipeBookComponent extends DarkWorldRecipeBookComponent
{
	@Nullable
	private Ingredient fuels;

	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(152, 182, 28, 18, RECIPE_BOOK_LOCATION);
	}

	public void slotClicked(@Nullable Slot pSlot) {
		super.slotClicked(pSlot);
		if (pSlot != null && pSlot.index < this.menu.getSize()) {
			this.ghostRecipe.clear();
		}

	}

	public void setupGhostRecipe(Recipe<?> pRecipe, List<Slot> pSlots) {
		ItemStack itemstack = pRecipe.getResultItem(this.minecraft.level.registryAccess());
		this.ghostRecipe.setRecipe(pRecipe);
		this.ghostRecipe.addIngredient(Ingredient.of(itemstack), (pSlots.get(2)).x, (pSlots.get(2)).y);
		NonNullList<Ingredient> nonnulllist = pRecipe.getIngredients();
		Slot slot = pSlots.get(1);
		if (slot.getItem().isEmpty()) {
			if (this.fuels == null) {
				this.fuels = Ingredient.of(this.getFuelItems().stream().filter((p_280880_) -> {
					return p_280880_.isEnabled(this.minecraft.level.enabledFeatures());
				}).map(ItemStack::new));
			}

			this.ghostRecipe.addIngredient(this.fuels, slot.x, slot.y);
		}

		Iterator<Ingredient> iterator = nonnulllist.iterator();

		for(int i = 0; i < 2; ++i) {
			if (!iterator.hasNext()) {
				return;
			}

			Ingredient ingredient = iterator.next();
			if (!ingredient.isEmpty()) {
				Slot slot1 = pSlots.get(i);
				this.ghostRecipe.addIngredient(ingredient, slot1.x, slot1.y);
			}
		}

	}

	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, int yOffset) {
		if (this.isVisible()) {
			float t = (float) this.glowTicker / (float) GLOW_TICKER_UPPER_BOUND;
			float glow = Mth.sin(t * Mth.PI);

			pGuiGraphics.pose().pushPose();
			pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
			int i = (this.width - 151) / 2 - xOffset - 7;
			int j = (this.height - 174) / 2 - yOffset;
			pGuiGraphics.blit(RECIPE_BOOK_LOCATION, i, j, 0, 0, 151, 174);
			RenderBlitUtil.blitGui(pGuiGraphics, RECIPE_BOOK_GLOW_LOCATION, i, j, 0, 0, 151, 174, glow, glow, glow, 1);
			this.searchBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

			for (DarkWorldRecipeBookTabButton recipebooktabbutton : this.tabButtons) {
				recipebooktabbutton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
			}

			this.filterButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
			this.recipeBookPage.render(pGuiGraphics, i, j, pMouseX, pMouseY, pPartialTick);
			pGuiGraphics.pose().popPose();
		}
	}

	protected abstract Set<Item> getFuelItems();
}
