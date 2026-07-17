package destiny.penumbra_phantasm.client.render.menu;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.MenuRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class DarkCandyCraftingTableMenu extends RecipeBookMenu<CraftingContainer> {
    public static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_END = 10;
    private static final int INV_SLOT_START = 10;
    private static final int INV_SLOT_END = 37;
    private static final int USE_ROW_SLOT_START = 37;
    private static final int USE_ROW_SLOT_END = 46;
    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots;
    private final ContainerLevelAccess access;
    private final Player player;

    public DarkCandyCraftingTableMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }

    public DarkCandyCraftingTableMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(MenuRegistry.DARK_CANDY_CRAFTING_TABLE.get(), pContainerId);
        this.craftSlots = new TransientCraftingContainer(this, 3, 3);
        this.resultSlots = new ResultContainer();
        this.access = pAccess;
        this.player = pPlayerInventory.player;
        this.addSlot(new ResultSlot(pPlayerInventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));

        for(int $$3 = 0; $$3 < 3; ++$$3) {
            for(int $$4 = 0; $$4 < 3; ++$$4) {
                this.addSlot(new Slot(this.craftSlots, $$4 + $$3 * 3, 30 + $$4 * 18, 17 + $$3 * 18));
            }
        }

        for(int $$5 = 0; $$5 < 3; ++$$5) {
            for(int $$6 = 0; $$6 < 9; ++$$6) {
                this.addSlot(new Slot(pPlayerInventory, $$6 + $$5 * 9 + 9, 8 + $$6 * 18, 84 + $$5 * 18));
            }
        }

        for(int $$7 = 0; $$7 < 9; ++$$7) {
            this.addSlot(new Slot(pPlayerInventory, $$7, 8 + $$7 * 18, 142));
        }

    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult) {
        if (!pLevel.isClientSide) {
            ServerPlayer $$5 = (ServerPlayer)pPlayer;
            ItemStack $$6 = ItemStack.EMPTY;
            Optional<CraftingRecipe> $$7 = pLevel.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, pContainer, pLevel);
            if ($$7.isPresent()) {
                CraftingRecipe $$8 = (CraftingRecipe)$$7.get();
                if (pResult.setRecipeUsed(pLevel, $$5, $$8)) {
                    ItemStack $$9 = $$8.assemble(pContainer, pLevel.registryAccess());
                    if ($$9.isItemEnabled(pLevel.enabledFeatures())) {
                        $$6 = $$9;
                    }
                }
            }

            pResult.setItem(0, $$6);
            pMenu.setRemoteSlot(0, $$6);
            $$5.connection.send(new ClientboundContainerSetSlotPacket(pMenu.containerId, pMenu.incrementStateId(), 0, $$6));
        }
    }

    public void slotsChanged(Container pInventory) {
        this.access.execute((p_39386_, p_39387_) -> slotChangedCraftingGrid(this, p_39386_, this.player, this.craftSlots, this.resultSlots));
    }

    public void fillCraftSlotsStackedContents(StackedContents pItemHelper) {
        this.craftSlots.fillStackedContents(pItemHelper);
    }

    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }

    public void handlePlacement(boolean pPlaceAll, Recipe<?> pRecipe, ServerPlayer pPlayer)
    {
        if(DarkWorldUtil.getAllDarkWorldRecipes(pPlayer.level().registryAccess()).contains(pRecipe.getId()))
            super.handlePlacement(pPlaceAll, pRecipe, pPlayer);
    }

    public boolean recipeMatches(Recipe<? super CraftingContainer> pRecipe) {
        if(DarkWorldUtil.getAllDarkWorldRecipes(this.player.level().registryAccess()).contains(pRecipe.getId()))
            return pRecipe.matches(this.craftSlots, this.player.level());
        else return false;
    }

    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.access.execute((p_39371_, p_39372_) -> this.clearContainer(pPlayer, this.craftSlots));
    }

    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, BlockRegistry.DARK_CANDY_CRAFTING_TABLE.get());
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get(pIndex);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if (pIndex == 0) {
                this.access.execute((p_39378_, p_39379_) -> $$4.getItem().onCraftedBy($$4, p_39378_, pPlayer));
                if (!this.moveItemStackTo($$4, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }

                $$3.onQuickCraft($$4, $$2);
            } else if (pIndex >= 10 && pIndex < 46) {
                if (!this.moveItemStackTo($$4, 1, 10, false)) {
                    if (pIndex < 37) {
                        if (!this.moveItemStackTo($$4, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo($$4, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo($$4, 10, 46, false)) {
                return ItemStack.EMPTY;
            }

            if ($$4.isEmpty()) {
                $$3.setByPlayer(ItemStack.EMPTY);
            } else {
                $$3.setChanged();
            }

            if ($$4.getCount() == $$2.getCount()) {
                return ItemStack.EMPTY;
            }

            $$3.onTake(pPlayer, $$4);
            if (pIndex == 0) {
                pPlayer.drop($$4, false);
            }
        }

        return $$2;
    }

    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
    }

    public int getResultSlotIndex() {
        return 0;
    }

    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    public int getSize() {
        return 10;
    }

    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public boolean shouldMoveToInventory(int pSlotIndex) {
        return pSlotIndex != this.getResultSlotIndex();
    }
}
