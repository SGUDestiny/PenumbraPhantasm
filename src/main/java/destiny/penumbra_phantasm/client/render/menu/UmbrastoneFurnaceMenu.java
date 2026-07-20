package destiny.penumbra_phantasm.client.render.menu;

import destiny.penumbra_phantasm.server.registry.MenuRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.Optional;

public class UmbrastoneFurnaceMenu extends AbstractFurnaceMenu {
    public UmbrastoneFurnaceMenu(int containerId, Inventory playerInventory) {
        super(MenuRegistry.UMBRASTONE_FURNACE_MENU.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, containerId, playerInventory);
    }

    public UmbrastoneFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(MenuRegistry.UMBRASTONE_FURNACE_MENU.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, containerId, playerInventory, container, data);
    }

    @Override
    protected boolean canSmelt(ItemStack pStack)
    {
        Optional<SmeltingRecipe> recipe = this.level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(pStack), this.level);
		return recipe.filter(value -> DarkWorldUtil.canUseRecipe(this.level.registryAccess(), value.getId())).isPresent();
	}

    @Override
    public boolean recipeMatches(Recipe<? super Container> pRecipe)
    {
        return DarkWorldUtil.canUseRecipe(this.level.registryAccess(), pRecipe.getId());
    }

    @Override
    public void handlePlacement(boolean pPlaceAll, Recipe<?> pRecipe, ServerPlayer pPlayer)
    {
        if(DarkWorldUtil.canUseRecipe(pPlayer.level().registryAccess(), pRecipe.getId()))
            super.handlePlacement(pPlaceAll, pRecipe, pPlayer);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return super.stillValid(pPlayer);
    }
}