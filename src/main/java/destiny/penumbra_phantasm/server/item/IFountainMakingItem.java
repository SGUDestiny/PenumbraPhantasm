package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IFountainMakingItem
{
	default boolean canMakeFountain(Level level, ItemStack stack, Player player)
	{
		return getPlayerDetermination(player) >= 100;
	}

	default boolean canAffectDarkWorldType(Level level, ItemStack stack, Player player)
	{
		return false;
	}

	@Nullable
	default ResourceKey<DarkWorldType> affectDarkWorldType()
	{
		return null;
	}

	static int getPlayerDetermination(Player player)
	{
		return player.getCapability(CapabilityRegistry.SOUL).map(cap -> cap.determination).orElse(0);
	}
}
