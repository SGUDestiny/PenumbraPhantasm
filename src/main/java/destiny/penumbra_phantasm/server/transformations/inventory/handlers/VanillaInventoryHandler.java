package destiny.penumbra_phantasm.server.transformations.inventory.handlers;

import com.mojang.datafixers.util.Pair;
import destiny.penumbra_phantasm.server.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class VanillaInventoryHandler implements IStorageHandler
{
	@Override
	public String getStorageId()
	{
		return "vanilla";
	}

	@Override
	public boolean requireCapabilities()
	{
		return false;
	}

	@Override
	public CompoundTag extract(ServerPlayer player)
	{
		CompoundTag tag = new CompoundTag();
		tag.put(getStorageId(), player.getInventory().save(new ListTag()));
		player.getInventory().clearContent();
		return tag;
	}

	@Override
	public void place(ServerPlayer player, CompoundTag tag)
	{
		ListTag listTag = tag.getList(getStorageId(), Tag.TAG_COMPOUND);
		player.getInventory().load(listTag);
	}

	@Override
	public List<Pair<ItemStack, Integer>> takeItems(ServerPlayer player, Predicate<ItemStack> predicate)
	{
		List<Pair<ItemStack, Integer>> list = InventoryUtil.getAllMatching(player.getInventory(), predicate);
		InventoryUtil.removeAllMatching(player.getInventory(), predicate);
		return list;
	}

	@Override
	public void addItemStack(ServerPlayer player, ItemStack stack, int slot)
	{
		if(slot == -1)
		{
			if(player.getItemInHand(InteractionHand.OFF_HAND).isEmpty())
				player.setItemInHand(InteractionHand.OFF_HAND, stack);
			else if(!player.addItem(stack))
				player.drop(stack, true, false);
		}
		else if(player.getInventory().getItem(slot).isEmpty())
			player.getInventory().setItem(slot, stack);
		else if(!player.addItem(stack))
			player.drop(stack, true, false);
	}
}
