package destiny.penumbra_phantasm.server.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class IchorItem extends ScalableHorizontalPlaneBlockItem {
    public int burnTicks;

    public IchorItem(Block block, Properties pProperties, int burnTicks) {
        super(block, pProperties);
        this.burnTicks = burnTicks;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTicks;
    }
}
