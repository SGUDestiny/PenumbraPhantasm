package destiny.penumbra_phantasm.server.transformations.inventory.handlers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public interface IStorageHandler
{
	String getStorageId();
	boolean requireCapabilities();
	CompoundTag extract(ServerPlayer player);
	void place(ServerPlayer player, CompoundTag tag);

	List<Pair<ItemStack, Integer>> takeItems(ServerPlayer player, Predicate<ItemStack> predicate);
	void addItemStack(ServerPlayer player, ItemStack stack, int slot);
}
