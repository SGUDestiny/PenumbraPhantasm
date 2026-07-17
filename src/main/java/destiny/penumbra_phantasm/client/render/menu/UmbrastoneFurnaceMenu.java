package destiny.penumbra_phantasm.client.render.menu;

import destiny.penumbra_phantasm.server.registry.MenuRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class UmbrastoneFurnaceMenu extends AbstractFurnaceMenu {
    public UmbrastoneFurnaceMenu(int containerId, Inventory playerInventory) {
        super(MenuRegistry.UMBRASTONE_FURNACE_MENU.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, containerId, playerInventory);
    }

    public UmbrastoneFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(MenuRegistry.UMBRASTONE_FURNACE_MENU.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, containerId, playerInventory, container, data);
    }

    @Override
    public boolean recipeMatches(Recipe<? super Container> pRecipe)
    {
        if(DarkWorldUtil.getAllDarkWorldRecipes(this.level.registryAccess()).contains(pRecipe.getId()))
            return super.recipeMatches(pRecipe);
        else return false;
    }

    @Override
    public void handlePlacement(boolean pPlaceAll, Recipe<?> pRecipe, ServerPlayer pPlayer)
    {
        if(DarkWorldUtil.getAllDarkWorldRecipes(pPlayer.level().registryAccess()).contains(pRecipe.getId()))
            super.handlePlacement(pPlaceAll, pRecipe, pPlayer);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return super.stillValid(pPlayer);
    }
}