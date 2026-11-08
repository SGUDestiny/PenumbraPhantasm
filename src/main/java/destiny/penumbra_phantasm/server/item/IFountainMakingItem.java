package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.capability.FountainMakingCapability;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.fountain.RoomScanner;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IFountainMakingItem
{
	default boolean canMakeFountain(Level level, ItemStack stack, Player player)
	{
		if(getPlayerDetermination(player) < 100)
		{
			player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_not_enough_determination"), true);
			return false;
		}
		if (!FountainMakingCapability.isStandingOnFullFace(level, player))
		{
			player.displayClientMessage(Component.translatable("message.penumbra_phantasm.making_fountain_invalid_position"), true);
			return false;
		}
		return true;
	}

	default boolean canAffectDarkWorldType(Level level, ItemStack stack, Player player)
	{
		return false;
	}

	default boolean shouldIgnoreRoomChecks(Level level, ItemStack stack, Player player)
	{
		return false;
	}

	default boolean shouldCheckForSoulHearth(Level level, ItemStack stack, Player player)
	{
		return true;
	}

	@Nullable
	default ResourceKey<DarkWorldType> affectDarkWorldType(Level level, ItemStack stack, Player player,
														   RoomScanner.RoomScanResult roomResult)
	{
		return null;
	}

	default BlockPos positionDarkFountain(Level level, ItemStack stack, Player player, BlockPos originalPos, BlockPos lightFountainPos)
	{
		return originalPos;
	}

	default boolean shouldRerouteTargetDarkWorldLevel(Level level, ItemStack stack, Player player,
													  DarkWorldType darkWorldType)
	{
		return false;
	}

	default ServerLevel rerouteTargetDarkWorldLevel(Level level, ItemStack stack, Player player,
													DarkWorldType darkWorldType)
	{
		return null;
	}

	static int getPlayerDetermination(Player player)
	{
		return player.getCapability(CapabilityRegistry.SOUL).map(cap -> cap.determination).orElse(0);
	}

	default int useAmount() {
		return 1;
	}
}
