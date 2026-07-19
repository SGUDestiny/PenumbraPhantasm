package destiny.penumbra_phantasm.server.transformations.inventory;

import com.mojang.datafixers.util.Pair;
import destiny.penumbra_phantasm.server.datapack.DarkWorldItemTransforms;
import destiny.penumbra_phantasm.server.transformations.inventory.handlers.IStorageHandler;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Predicate;

public class StoredInventory
{
	private final HashMap<String, CompoundTag> storage = new HashMap<>();

	public void addStorage(String id, CompoundTag data)
	{
		storage.put(id, data);
	}

	public CompoundTag getStorageTag(String id)
	{
		return storage.get(id);
	}

	public void swap(ServerPlayer player)
	{
		swap(player, true);
	}

	public void swap(ServerPlayer player, boolean placeItems)
	{
		swap(player, placeItems, true);
		swap(player, placeItems, false);
	}

	public void swap(ServerPlayer player, boolean placeItems, boolean hasCapabilities)
	{
		Set<Item> persisting = new HashSet<>();
		Map<Item, Item> transforming = new HashMap<>();

		if(player.getServer() != null)
		{
			Registry<DarkWorldItemTransforms> registry = player.getServer().registryAccess().registryOrThrow(DarkWorldItemTransforms.REGISTRY_KEY);
			for(Map.Entry<ResourceKey<DarkWorldItemTransforms>, DarkWorldItemTransforms> entry : registry.entrySet())
			{
				DarkWorldItemTransforms transform = entry.getValue();
				persisting.addAll(transform.persist());
				for(DarkWorldItemTransforms.ItemTransform itemTransform : transform.transforms())
				{
					transforming.put(itemTransform.getLightWorldForm(), itemTransform.getDarkWorldForm());
					transforming.put(itemTransform.getDarkWorldForm(), itemTransform.getLightWorldForm());
				}
			}
		}

		Predicate<ItemStack> persistingPredicate = stack -> persisting.contains(stack.getItem());
		Predicate<ItemStack> transformingPredicate = stack -> transforming.containsKey(stack.getItem());
		Map<String, List<Pair<ItemStack, Integer>>> items = new HashMap<>();

		for(IStorageHandler handler : StorageManager.getHandlers())
		{
			if(handler.requireCapabilities() != hasCapabilities)
				continue;

			List<Pair<ItemStack, Integer>> itemList = new ArrayList<>();
			itemList.addAll(handler.takeItems(player, persistingPredicate));

			List<Pair<ItemStack, Integer>> transformingItems = handler.takeItems(player, transformingPredicate);
			transformingItems.replaceAll(pair ->
				{
					ItemStack newStack = new ItemStack(transforming.get(pair.getFirst().getItem()), pair.getFirst().getCount());
					return new Pair<>(newStack, pair.getSecond());
				});

			itemList.addAll(transformingItems);
			items.put(handler.getStorageId(), itemList);
		}

		HashMap<String, CompoundTag> preSwap = new HashMap<>(storage);
		for(IStorageHandler handler : StorageManager.getHandlers())
		{
			if(handler.requireCapabilities() != hasCapabilities)
				continue;
			addStorage(handler.getStorageId(), handler.extract(player));
		}

		if(placeItems)
			for(IStorageHandler handler : StorageManager.getHandlers())
			{
				if(handler.requireCapabilities() != hasCapabilities)
					continue;

				preSwap.computeIfAbsent(handler.getStorageId(), k -> new CompoundTag());
				handler.place(player, preSwap.get(handler.getStorageId()));
			}

		for(IStorageHandler handler : StorageManager.getHandlers())
		{
			if(handler.requireCapabilities() != hasCapabilities)
				continue;

			List<Pair<ItemStack, Integer>> toInsert = items.getOrDefault(handler.getStorageId(), Collections.emptyList());
			for(Pair<ItemStack, Integer> pair : toInsert)
				handler.addItemStack(player, pair.getFirst(), pair.getSecond());
		}
	}


	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();
		storage.forEach(tag::put);

		return tag;
	}

	public static StoredInventory load(CompoundTag tag)
	{
		StoredInventory data = new StoredInventory();
		for(String key : tag.getAllKeys())
			data.addStorage(key, tag.getCompound(key));

		return data;
	}
}
