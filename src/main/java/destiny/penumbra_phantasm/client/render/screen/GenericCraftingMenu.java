package destiny.penumbra_phantasm.client.render.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.block.Block;

public class GenericCraftingMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    private final Block tableBlock;

    public GenericCraftingMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess, Block tableBlock) {
        super(pContainerId, pPlayerInventory, pAccess);
        this.access = pAccess;
        this.tableBlock = tableBlock;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, tableBlock);
    }
}
