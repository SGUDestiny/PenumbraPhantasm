package destiny.penumbra_phantasm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import destiny.penumbra_phantasm.client.render.menu.DarkWorldInventoryMenu;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin
{
	@Shadow @Final private Player owner;

	@Shadow @Final private ResultContainer resultSlots;

	@Shadow @Final private CraftingContainer craftSlots;

	@Inject(method = "slotsChanged(Lnet/minecraft/world/Container;)V", at = @At("HEAD"), cancellable = true)
	public void slotChangeDW(Container container, CallbackInfo ci) {
		InventoryMenu menu = ((InventoryMenu) (Object) this);

		if(DarkWorldUtil.isDarkWorld(owner.level())) {
			DarkWorldInventoryMenu.slotChangedCraftingGrid(menu, owner.level(), owner, craftSlots, resultSlots);

			ci.cancel();
		}
	}

	@WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Mob;getEquipmentSlotForItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;"))
	private EquipmentSlot penumbraPhantasm$quickMoveStack(ItemStack stack, Operation<EquipmentSlot> original) {
		EquipmentSlot slot = original.call(stack);

		if (DarkWorldUtil.isDarkWorld(this.owner.level()) && slot.getType() == EquipmentSlot.Type.ARMOR) {
			return EquipmentSlot.MAINHAND;
		}

		return slot;
	}
}
