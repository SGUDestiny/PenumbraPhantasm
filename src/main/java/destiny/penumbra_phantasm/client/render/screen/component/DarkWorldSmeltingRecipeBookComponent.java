package destiny.penumbra_phantasm.client.render.screen.component;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.Set;

public class DarkWorldSmeltingRecipeBookComponent extends AbstractDarkWorldFurnaceRecipeBookComponent
{
	private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smeltable");

	protected Component getRecipeFilterName() {
		return FILTER_NAME;
	}

	@Override
	protected Set<Item> getFuelItems()
	{
		return AbstractFurnaceBlockEntity.getFuel().keySet();
	}
}
