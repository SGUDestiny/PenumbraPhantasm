package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.transformations.inventory.StorageData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FractalMirrorItem extends Item
{

	public FractalMirrorItem(Properties pProperties)
	{
		super(pProperties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(level instanceof ServerLevel serverLevel)
		{
			StorageData data = StorageData.get(serverLevel);
			data.getInventory(player.getUUID()).swap(((ServerPlayer) player), true);
			data.setDirty();
		}

		return InteractionResultHolder.fail(stack);
	}
}
