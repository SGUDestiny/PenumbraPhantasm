package destiny.penumbra_phantasm.server.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class SoulHearthRecipe implements CraftingRecipe, IShapedRecipe<CraftingContainer> {
    @Override
    public CraftingBookCategory category() {
        return null;
    }

    @Override
    public int getRecipeWidth() {
        return 0;
    }

    @Override
    public int getRecipeHeight() {
        return 0;
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }
}
