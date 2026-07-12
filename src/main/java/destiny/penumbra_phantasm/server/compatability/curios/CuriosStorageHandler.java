package destiny.penumbra_phantasm.server.compatability.curios;

import com.mojang.datafixers.util.Pair;
import destiny.penumbra_phantasm.server.transformations.inventory.handlers.IStorageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CuriosStorageHandler implements IStorageHandler
{
	@Override
	public String getStorageId()
	{
		return "curios";
	}

	@Override
	public boolean requireCapabilities()
	{
		return true;
	}

	@Override
	public CompoundTag extract(ServerPlayer player)
	{
		CompoundTag tag = new CompoundTag();
		LazyOptional<ICuriosItemHandler> lazy = CuriosApi.getCuriosInventory(player);
		lazy.ifPresent(cap ->
			{
				tag.put(getStorageId(), cap.writeTag());
				IItemHandlerModifiable equipped = cap.getEquippedCurios();
				for(int i = 0; i < equipped.getSlots(); i++)
					equipped.setStackInSlot(i, ItemStack.EMPTY);
			});
		return tag;
	}

	@Override
	public void place(ServerPlayer player, CompoundTag tag)
	{
		LazyOptional<ICuriosItemHandler> lazy = CuriosApi.getCuriosInventory(player);
		lazy.ifPresent(cap ->
			{
				cap.readTag(tag.getCompound(getStorageId()));
				cap.getCurios();
			});
	}

	@Override
	public List<Pair<ItemStack, Integer>> takeItems(ServerPlayer player, Predicate<ItemStack> predicate)
	{
		List<Pair<ItemStack, Integer>> list = new ArrayList<>();
		LazyOptional<ICuriosItemHandler> lazy = CuriosApi.getCuriosInventory(player);
		lazy.ifPresent(cap ->
			{
				IItemHandlerModifiable equipped = cap.getEquippedCurios();
				for(int i = 0; i < equipped.getSlots(); i++)
				{
					ItemStack stack = equipped.getStackInSlot(i).copy();
					if(!predicate.test(stack))
						continue;

					list.add(new Pair<>(stack, i));
					equipped.setStackInSlot(i, ItemStack.EMPTY);
				}
			});
		return list;
	}

	@Override
	public void addItemStack(ServerPlayer player, ItemStack stack, int slot)
	{
		LazyOptional<ICuriosItemHandler> lazy = CuriosApi.getCuriosInventory(player);
		lazy.ifPresent(cap ->
			{
				IItemHandlerModifiable equipped = cap.getEquippedCurios();
				equipped.setStackInSlot(slot, stack);
			});
	}
}
