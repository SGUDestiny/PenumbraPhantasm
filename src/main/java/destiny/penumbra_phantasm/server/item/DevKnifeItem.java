package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.fountain.RoomScanner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DevKnifeItem extends KnifeItem implements IFountainMakingItem
{
	public static final ResourceKey<DarkWorldType> KEY = ResourceKey.create(DarkWorldType.REGISTRY_KEY,
			new ResourceLocation(PenumbraPhantasm.MODID, "mindscape"));

	public DevKnifeItem(Tier tier, int damage, float speed, int useAmount, Properties properties) {
		super(tier, damage, speed, useAmount, properties);
	}

	@Override
	public boolean canMakeFountain(Level level, ItemStack stack, Player player)
	{
		return true;
	}

	@Override
	public boolean canAffectDarkWorldType(Level level, ItemStack stack, Player player)
	{
		return true;
	}

	@Override
	public boolean shouldIgnoreRoomChecks(Level level, ItemStack stack, Player player)
	{
		return true;
	}

	@Override
	public boolean shouldCheckForSoulHearth(Level level, ItemStack stack, Player player)
	{
		return false;
	}

	@Override
	public @Nullable ResourceKey<DarkWorldType> affectDarkWorldType(Level level, ItemStack stack, Player player,
																	RoomScanner.RoomScanResult roomResult)
	{
		return KEY;
	}
}
