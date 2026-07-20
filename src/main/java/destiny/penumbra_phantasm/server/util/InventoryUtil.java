package destiny.penumbra_phantasm.server.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InventoryUtil
{
	public static List<ItemStack> getAllMatchingStacks(Inventory inventory, Predicate<ItemStack> predicate)
	{
		List<ItemStack> items = new ArrayList<>();
		List<NonNullList<ItemStack>> lists = ImmutableList.of(inventory.items,
				inventory.armor, inventory.offhand);
		for(NonNullList<ItemStack> list : lists)
			for(ItemStack stack : list)
			{
				if(!stack.isEmpty() && predicate.test(stack))
					items.add(stack);
			}
		return items;
	}

	public static List<Integer> getAllMatchingSlots(Inventory inventory, Predicate<ItemStack> predicate)
	{
		List<Integer> items = new ArrayList<>();
		List<NonNullList<ItemStack>> lists = ImmutableList.of(inventory.items,
				inventory.armor, inventory.offhand);
		for(NonNullList<ItemStack> list : lists)
			for(ItemStack stack : list)
			{
				if(!stack.isEmpty() && predicate.test(stack))
					items.add(inventory.findSlotMatchingItem(stack));
			}
		return items;
	}

	public static List<Pair<ItemStack, Integer>> getAllMatching(Inventory inventory, Predicate<ItemStack> predicate)
	{
		List<Pair<ItemStack, Integer>> items = new ArrayList<>();
		List<NonNullList<ItemStack>> lists = ImmutableList.of(inventory.items,
				inventory.armor, inventory.offhand);
		for(NonNullList<ItemStack> list : lists)
			for(ItemStack stack : list)
			{
				if(!stack.isEmpty() && predicate.test(stack))
					items.add(new Pair<>(stack, inventory.findSlotMatchingItem(stack)));
			}
		return items;
	}

	public static void removeAllMatching(Inventory inventory, Predicate<ItemStack> predicate)
	{
		List<ItemStack> matches = getAllMatchingStacks(inventory, predicate);
		for(ItemStack stack : matches)
			inventory.removeItem(stack);

	}
}
