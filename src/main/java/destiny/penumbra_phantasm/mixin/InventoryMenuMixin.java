package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.client.render.screen.DarkWorldInventoryMenu;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin
{
	@Shadow @Final private Player owner;

	@Shadow @Final private ResultContainer resultSlots;

	@Shadow @Final private CraftingContainer craftSlots;

	@Inject(method = "slotsChanged(Lnet/minecraft/world/Container;)V", at = @At("HEAD"), cancellable = true)
	public void slotChangeDW(Container container, CallbackInfo ci)
	{
		InventoryMenu menu = ((InventoryMenu) (Object) this);
		if(DarkWorldUtil.isDarkWorld(owner.level()))
		{
			DarkWorldInventoryMenu.slotChangedCraftingGrid(menu, owner.level(), owner, craftSlots, resultSlots);
			ci.cancel();
		}
	}
}
