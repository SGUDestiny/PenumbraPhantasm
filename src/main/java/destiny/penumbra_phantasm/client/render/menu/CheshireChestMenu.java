package destiny.penumbra_phantasm.client.render.menu;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.entity.CheshireChestBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.MenuRegistry;
import destiny.penumbra_phantasm.server.capability.CheshireChestInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class CheshireChestMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Player player;
    private final ContainerLevelAccess access;

    public CheshireChestMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        this(windowId, playerInventory, new CheshireChestInventory(), pos, null, ContainerLevelAccess.NULL);
    }

    public CheshireChestMenu(int windowId, Inventory playerInventory, CheshireChestInventory cheshireInventory, BlockPos pos, Player player, ContainerLevelAccess access) {
        super(MenuRegistry.CHESHIRE_CHEST_MENU.get(), windowId);
        PenumbraPhantasm.LOGGER.info("Chest inventory class: {}", cheshireInventory.getClass().getName());
        this.pos = pos;
        this.player = player;
        this.access = access;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new SlotItemHandler(cheshireInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem().copy();
        ItemStack stack = slot.getItem();

        if (index < 27) {
            if (!this.moveItemStackTo(stack, 27, 63, true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 27, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return original;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        if (!pPlayer.level().isClientSide && player != null) {
            BlockEntity be = pPlayer.level().getBlockEntity(pos);
            if (be instanceof CheshireChestBlockEntity enderBe) {
                enderBe.stopOpen(player);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BlockRegistry.CHESHIRE_CHEST.get());
    }
}
