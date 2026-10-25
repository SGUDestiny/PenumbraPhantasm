package destiny.penumbra_phantasm.server.recipe.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SoulHearthContainer implements Container {
    public List<ItemStack> inputs;
    public Player player;

    public SoulHearthContainer(List<ItemStack> inputs, Player player) {
        this.inputs = inputs;
        this.player = player;
    }

    @Override
    public int getContainerSize() {
        return 32;
    }

    @Override
    public boolean isEmpty() {
        return inputs.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return inputs.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = inputs.get(slot);
        stack.shrink(amount);

        if (!stack.isEmpty()) {
            this.setChanged();
        }

        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return inputs.remove(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inputs.set(slot, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        inputs = new ArrayList<>();
    }
}
