package destiny.penumbra_phantasm.client.render.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.render.RenderBlitUtil;
import destiny.penumbra_phantasm.client.render.screen.component.DarkWorldButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.CreativeModeTabSearchRegistry;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import net.minecraftforge.common.CreativeModeTabRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DarkWorldCreativeModeInventoryScreen extends EffectRenderingInventoryScreen<DarkWorldCreativeModeInventoryScreen.ItemPickerMenu> {
    private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/creative_inventory/tabs.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_HELMET = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_1.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_CHESTPLATE = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_2.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_LEGGINGS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_3.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_BOOTS = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_4.png");
    private static final ResourceLocation EMPTY_EQUIPMENT_SLOT_OFFHAND = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/empty_equipment_slot_5.png");
    private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
    private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
    private static final int NUM_ROWS = 5;
    private static final int NUM_COLS = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    static final SimpleContainer CONTAINER = new SimpleContainer(45);
    private static final Component TRASH_SLOT_TOOLTIP = Component.translatable("inventory.binSlot");
    private static final int TEXT_COLOR = 16777215;
    public static final int GLOW_TICKER_UPPER_BOUND = 5 * 20;

    private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    @Nullable
    private List<Slot> originalSlots;
    @Nullable
    private Slot destroyItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTextInput;
    private boolean hasClickedOutside;
    private final Set<TagKey<Item>> visibleTags = new HashSet();
    private final boolean displayOperatorCreativeTab;
    private final List<CreativeTabsScreenPage> pages = new ArrayList();
    private CreativeTabsScreenPage currentPage = new CreativeTabsScreenPage(new ArrayList());

    private int glowTicker;

    public DarkWorldCreativeModeInventoryScreen(Player pPlayer, FeatureFlagSet pEnabledFeatures, boolean pDisplayOperatorCreativeTab) {
        super(new DarkWorldCreativeModeInventoryScreen.ItemPickerMenu(pPlayer), pPlayer.getInventory(), CommonComponents.EMPTY);
        pPlayer.containerMenu = this.menu;
        this.imageHeight = 136;
        this.imageWidth = 195;
        this.displayOperatorCreativeTab = pDisplayOperatorCreativeTab;
        CreativeModeTabs.tryRebuildTabContents(pEnabledFeatures, this.hasPermissions(pPlayer), pPlayer.level().registryAccess());
    }

    private boolean hasPermissions(Player pPlayer) {
        return pPlayer.canUseGameMasterBlocks() && this.displayOperatorCreativeTab;
    }

    private void tryRefreshInvalidatedTabs(FeatureFlagSet pEnabledFeatures, boolean pHasPermissions, HolderLookup.Provider pHolders) {
        if (CreativeModeTabs.tryRebuildTabContents(pEnabledFeatures, pHasPermissions, pHolders)) {
            for(CreativeModeTab creativemodetab : CreativeModeTabs.allTabs()) {
                Collection<ItemStack> collection = creativemodetab.getDisplayItems();
                if (creativemodetab == selectedTab) {
                    if (creativemodetab.getType() == CreativeModeTab.Type.CATEGORY && collection.isEmpty()) {
                        this.selectTab(CreativeModeTabs.getDefaultTab());
                    } else {
                        this.refreshCurrentTabContents(collection);
                    }
                }
            }
        }

    }

    private void refreshCurrentTabContents(Collection<ItemStack> pItems) {
        int i = this.menu.getRowIndexForScroll(this.scrollOffs);
        this.menu.items.clear();
        if (selectedTab.hasSearchBar()) {
            this.refreshSearchResults();
        } else {
            this.menu.items.addAll(pItems);
        }

        this.scrollOffs = this.menu.getScrollForRowIndex(i);
        this.menu.scrollTo(this.scrollOffs);
    }

    public void containerTick() {
        super.containerTick();
        if (this.minecraft != null) {
            if (this.minecraft.player != null) {
                this.tryRefreshInvalidatedTabs(this.minecraft.player.connection.enabledFeatures(), this.hasPermissions(this.minecraft.player), this.minecraft.player.level().registryAccess());
            }

            if (!this.minecraft.gameMode.hasInfiniteItems()) {
                this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
            } else {
                this.searchBox.tick();
            }
        }

        this.glowTicker++;
        if (this.glowTicker >= GLOW_TICKER_UPPER_BOUND) {
            this.glowTicker = 0;
        }
    }

    protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        if (this.isCreativeSlot(pSlot)) {
            this.searchBox.moveCursorToEnd();
            this.searchBox.setHighlightPos(0);
        }

        boolean flag = pType == ClickType.QUICK_MOVE;
        pType = pSlotId == -999 && pType == ClickType.PICKUP ? ClickType.THROW : pType;
        if (pSlot == null && selectedTab.getType() != CreativeModeTab.Type.INVENTORY && pType != ClickType.QUICK_CRAFT) {
            if (!(this.menu).getCarried().isEmpty() && this.hasClickedOutside) {
                if (pMouseButton == 0) {
                    this.minecraft.player.drop((this.menu).getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop((this.menu).getCarried());
                    (this.menu).setCarried(ItemStack.EMPTY);
                }

                if (pMouseButton == 1) {
                    ItemStack itemstack5 = (this.menu).getCarried().split(1);
                    this.minecraft.player.drop(itemstack5, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack5);
                }
            }
        } else {
            if (pSlot != null && !pSlot.mayPickup(this.minecraft.player)) {
                return;
            }

            if (pSlot == this.destroyItemSlot && flag) {
                for(int j = 0; j < this.minecraft.player.inventoryMenu.getItems().size(); ++j) {
                    this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, j);
                }
            } else if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
                if (pSlot == this.destroyItemSlot) {
                    (this.menu).setCarried(ItemStack.EMPTY);
                } else if (pType == ClickType.THROW && pSlot != null && pSlot.hasItem()) {
                    ItemStack itemstack = pSlot.remove(pMouseButton == 0 ? 1 : pSlot.getItem().getMaxStackSize());
                    ItemStack itemstack1 = pSlot.getItem();
                    this.minecraft.player.drop(itemstack, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack1, ((DarkWorldCreativeModeInventoryScreen.SlotWrapper)pSlot).target.index);
                } else if (pType == ClickType.THROW && !(this.menu).getCarried().isEmpty()) {
                    this.minecraft.player.drop((this.menu).getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop((this.menu).getCarried());
                    (this.menu).setCarried(ItemStack.EMPTY);
                } else {
                    this.minecraft.player.inventoryMenu.clicked(pSlot == null ? pSlotId : ((DarkWorldCreativeModeInventoryScreen.SlotWrapper)pSlot).target.index, pMouseButton, pType, this.minecraft.player);
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            } else if (pType != ClickType.QUICK_CRAFT && pSlot.container == CONTAINER) {
                ItemStack itemstack4 = (this.menu).getCarried();
                ItemStack itemstack7 = pSlot.getItem();
                if (pType == ClickType.SWAP) {
                    if (!itemstack7.isEmpty()) {
                        this.minecraft.player.getInventory().setItem(pMouseButton, itemstack7.copyWithCount(itemstack7.getMaxStackSize()));
                        this.minecraft.player.inventoryMenu.broadcastChanges();
                    }

                    return;
                }

                if (pType == ClickType.CLONE) {
                    if ((this.menu).getCarried().isEmpty() && pSlot.hasItem()) {
                        ItemStack itemstack9 = pSlot.getItem();
                        (this.menu).setCarried(itemstack9.copyWithCount(itemstack9.getMaxStackSize()));
                    }

                    return;
                }

                if (pType == ClickType.THROW) {
                    if (!itemstack7.isEmpty()) {
                        ItemStack itemstack8 = itemstack7.copyWithCount(pMouseButton == 0 ? 1 : itemstack7.getMaxStackSize());
                        this.minecraft.player.drop(itemstack8, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack8);
                    }

                    return;
                }

                if (!itemstack4.isEmpty() && !itemstack7.isEmpty() && ItemStack.isSameItemSameTags(itemstack4, itemstack7)) {
                    if (pMouseButton == 0) {
                        if (flag) {
                            itemstack4.setCount(itemstack4.getMaxStackSize());
                        } else if (itemstack4.getCount() < itemstack4.getMaxStackSize()) {
                            itemstack4.grow(1);
                        }
                    } else {
                        itemstack4.shrink(1);
                    }
                } else if (!itemstack7.isEmpty() && itemstack4.isEmpty()) {
                    int l = flag ? itemstack7.getMaxStackSize() : itemstack7.getCount();
                    (this.menu).setCarried(itemstack7.copyWithCount(l));
                } else if (pMouseButton == 0) {
                    (this.menu).setCarried(ItemStack.EMPTY);
                } else if (!(this.menu).getCarried().isEmpty()) {
                    (this.menu).getCarried().shrink(1);
                }
            } else if (this.menu != null) {
                ItemStack itemstack3 = pSlot == null ? ItemStack.EMPTY : (this.menu).getSlot(pSlot.index).getItem();
                (this.menu).clicked(pSlot == null ? pSlotId : pSlot.index, pMouseButton, pType, this.minecraft.player);
                if (AbstractContainerMenu.getQuickcraftHeader(pMouseButton) == 2) {
                    for(int k = 0; k < 9; ++k) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd((this.menu).getSlot(45 + k).getItem(), 36 + k);
                    }
                } else if (pSlot != null) {
                    ItemStack itemstack6 = (this.menu).getSlot(pSlot.index).getItem();
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack6, pSlot.index - (this.menu).slots.size() + 9 + 36);
                    int i = 45 + pMouseButton;
                    if (pType == ClickType.SWAP) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack3, i - (this.menu).slots.size() + 9 + 36);
                    } else if (pType == ClickType.THROW && !itemstack3.isEmpty()) {
                        ItemStack itemstack2 = itemstack3.copyWithCount(pMouseButton == 0 ? 1 : itemstack3.getMaxStackSize());
                        this.minecraft.player.drop(itemstack2, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack2);
                    }

                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            }
        }

    }

    private boolean isCreativeSlot(@Nullable Slot pSlot) {
        return pSlot != null && pSlot.container == CONTAINER;
    }

    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            super.init();
            this.pages.clear();
            int tabIndex = 0;
            List<CreativeModeTab> currentPage = new ArrayList();

            for(CreativeModeTab sortedCreativeModeTab : CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
                currentPage.add(sortedCreativeModeTab);
                ++tabIndex;
                if (tabIndex == 10) {
                    this.pages.add(new CreativeTabsScreenPage(currentPage));
                    currentPage = new ArrayList();
                    tabIndex = 0;
                }
            }

            if (tabIndex != 0) {
                this.pages.add(new CreativeTabsScreenPage(currentPage));
            }

            if (this.pages.isEmpty()) {
                this.currentPage = new CreativeTabsScreenPage(new ArrayList());
            } else {
                this.currentPage = this.pages.get(0);
            }

            if (this.pages.size() > 1) {
                ResourceLocation button = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/widgets.png");

                this.addRenderableWidget(DarkWorldButton.builder(Component.literal("<"), (b) -> this.setCurrentPage(this.pages.get(Math.max(this.pages.indexOf(this.currentPage) - 1, 0)))).pos(this.leftPos, this.topPos - 50).size(20, 20).texture(button).build());
                this.addRenderableWidget(DarkWorldButton.builder(Component.literal(">"), (b) -> this.setCurrentPage(this.pages.get(Math.min(this.pages.indexOf(this.currentPage) + 1, this.pages.size() - 1)))).pos(this.leftPos + this.imageWidth - 20, this.topPos - 50).size(20, 20).texture(button).build());
            }

            this.currentPage = this.pages.stream().filter((page) -> page.getVisibleTabs().contains(selectedTab)).findFirst().orElse(this.currentPage);
            if (!this.currentPage.getVisibleTabs().contains(selectedTab)) {
                selectedTab = this.currentPage.getVisibleTabs().get(0);
            }

            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, Component.translatable("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(16777215);
            this.addWidget(this.searchBox);
            CreativeModeTab creativemodetab = selectedTab;
            selectedTab = CreativeModeTabs.getDefaultTab();
            this.selectTab(creativemodetab);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
            if (!selectedTab.shouldDisplay()) {
                this.selectTab(CreativeModeTabs.getDefaultTab());
            }
        } else {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }

    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        int i = (this.menu).getRowIndexForScroll(this.scrollOffs);
        String s = this.searchBox.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.searchBox.setValue(s);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }

        this.scrollOffs = (this.menu).getScrollForRowIndex(i);
        (this.menu).scrollTo(this.scrollOffs);
    }

    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }

    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (this.ignoreTextInput) {
            return false;
        } else if (!selectedTab.hasSearchBar()) {
            return false;
        } else {
            String s = this.searchBox.getValue();
            if (this.searchBox.charTyped(pCodePoint, pModifiers)) {
                if (!Objects.equals(s, this.searchBox.getValue())) {
                    this.refreshSearchResults();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        this.ignoreTextInput = false;
        if (!selectedTab.hasSearchBar()) {
            if (this.minecraft.options.keyChat.matches(pKeyCode, pScanCode)) {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTabs.searchTab());
                return true;
            } else {
                return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
        } else {
            boolean flag = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
            boolean flag1 = InputConstants.getKey(pKeyCode, pScanCode).getNumericKeyValue().isPresent();
            if (flag && flag1 && this.checkHotbarKeyPressed(pKeyCode, pScanCode)) {
                this.ignoreTextInput = true;
                return true;
            } else {
                String s = this.searchBox.getValue();
                if (this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                    if (!Objects.equals(s, this.searchBox.getValue())) {
                        this.refreshSearchResults();
                    }

                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && pKeyCode != 256 ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
                }
            }
        }
    }

    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        this.ignoreTextInput = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    private void refreshSearchResults() {
        if (selectedTab.hasSearchBar()) {
            (this.menu).items.clear();
            this.visibleTags.clear();
            String s = this.searchBox.getValue();
            if (s.isEmpty()) {
                (this.menu).items.addAll(selectedTab.getDisplayItems());
            } else {
                SearchTree<ItemStack> searchtree;
                if (s.startsWith("#")) {
                    s = s.substring(1);
                    searchtree = this.minecraft.getSearchTree(CreativeModeTabSearchRegistry.getTagSearchKey(selectedTab));
                    this.updateVisibleTags(s);
                } else {
                    searchtree = this.minecraft.getSearchTree(CreativeModeTabSearchRegistry.getNameSearchKey(selectedTab));
                }

                (this.menu).items.addAll(searchtree.search(s.toLowerCase(Locale.ROOT)));
            }

            this.scrollOffs = 0.0F;
            (this.menu).scrollTo(0.0F);
        }
    }

    private void updateVisibleTags(String pSearch) {
        int i = pSearch.indexOf(58);
        Predicate<ResourceLocation> predicate;
        if (i == -1) {
            predicate = (p_98609_) -> p_98609_.getPath().contains(pSearch);
        } else {
            String s = pSearch.substring(0, i).trim();
            String s1 = pSearch.substring(i + 1).trim();
            predicate = (p_98606_) -> p_98606_.getNamespace().contains(s) && p_98606_.getPath().contains(s1);
        }

        Stream var10000 = BuiltInRegistries.ITEM.getTagNames().filter((p_205410_) -> predicate.test(p_205410_.location()));
        Set var10001 = this.visibleTags;
        Objects.requireNonNull(var10001);
        var10000.forEach(var10001::add);
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        if (selectedTab.showTitle()) {
            RenderSystem.disableBlend();
            pGuiGraphics.drawString(this.font, selectedTab.getDisplayName().getString().toUpperCase(), 8, 6, -1, false);
        }

    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            double d0 = pMouseX - (double)this.leftPos;
            double d1 = pMouseY - (double)this.topPos;

            for(CreativeModeTab creativemodetab : this.currentPage.getVisibleTabs()) {
                if (this.checkTabClicked(creativemodetab, d0, d1)) {
                    return true;
                }
            }

            if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.insideScrollbar(pMouseX, pMouseY)) {
                this.scrolling = this.canScroll();
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            double d0 = pMouseX - (double)this.leftPos;
            double d1 = pMouseY - (double)this.topPos;
            this.scrolling = false;

            for(CreativeModeTab creativemodetab : this.currentPage.getVisibleTabs()) {
                if (this.checkTabClicked(creativemodetab, d0, d1)) {
                    this.selectTab(creativemodetab);
                    return true;
                }
            }
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    private boolean canScroll() {
        return selectedTab.canScroll() && (this.menu).canScroll();
    }

    private void selectTab(CreativeModeTab pTab) {
        CreativeModeTab creativemodetab = selectedTab;
        selectedTab = pTab;
        this.slotColor = pTab.getSlotColor();
        this.quickCraftSlots.clear();
        (this.menu).items.clear();
        this.clearDraggingState();

        if (selectedTab.getType() == CreativeModeTab.Type.HOTBAR) {
            HotbarManager hotbarmanager = this.minecraft.getHotbarManager();
            for (int i = 0; i < 9; ++i) {
                Hotbar hotbar = hotbarmanager.get(i);
                if (hotbar.isEmpty()) {
                    for (int j = 0; j < 9; ++j) {
                        if (j == i) {
                            ItemStack itemstack = new ItemStack(Items.PAPER);
                            itemstack.getOrCreateTagElement("CustomCreativeLock");
                            Component component = this.minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
                            Component component1 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                            itemstack.setHoverName(Component.translatable("inventory.hotbarInfo", component1, component));
                            (this.menu).items.add(itemstack);
                        } else {
                            (this.menu).items.add(ItemStack.EMPTY);
                        }
                    }
                } else {
                    (this.menu).items.addAll(hotbar);
                }
            }
        } else if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
            (this.menu).items.addAll(selectedTab.getDisplayItems());
        }

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
            if (this.originalSlots == null) {
                this.originalSlots = ImmutableList.copyOf((this.menu).slots);
            }

            (this.menu).slots.clear();

            for (int k = 0; k < abstractcontainermenu.slots.size(); ++k) {
                int l;
                int i1;
                if (k >= 5 && k < 9) {
                    int k1 = k - 5;
                    int i2 = k1 / 2;
                    int k2 = k1 % 2;
                    l = (i2 == 0) ? 55 : 107;
                    i1 = 6 + k2 * 27;
                } else if (k >= 0 && k < 5) {
                    l = -2000;
                    i1 = -2000;
                } else if (k == 45) {
                    l = 35;
                    i1 = 20;
                } else {
                    int j1 = k - 9;
                    int l1 = j1 % 9;
                    int j2 = j1 / 9;
                    l = 9 + l1 * 18;
                    if (k >= 36) {
                        i1 = 112;
                    } else {
                        i1 = 54 + j2 * 18;
                    }
                }

                Slot slot = new DarkWorldCreativeModeInventoryScreen.SlotWrapper(abstractcontainermenu.slots.get(k), k, l, i1);
                (this.menu).slots.add(slot);
            }

            this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
            (this.menu).slots.add(this.destroyItemSlot);
        } else if (creativemodetab.getType() == CreativeModeTab.Type.INVENTORY) {
            (this.menu).slots.clear();
            (this.menu).slots.addAll(this.originalSlots);
            this.originalSlots = null;
        }

        if (selectedTab.hasSearchBar()) {
            this.searchBox.setVisible(true);
            this.searchBox.setCanLoseFocus(false);
            this.searchBox.setFocused(true);
            if (creativemodetab != pTab) {
                this.searchBox.setValue("");
            }
            this.searchBox.setWidth(selectedTab.getSearchBarWidth());
            this.searchBox.setX(this.leftPos + 171 - this.searchBox.getWidth());
            this.refreshSearchResults();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setCanLoseFocus(true);
            this.searchBox.setFocused(false);
            this.searchBox.setValue("");
        }

        this.scrollOffs = 0.0F;
        (this.menu).scrollTo(0.0F);
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (!this.canScroll()) {
            return false;
        } else {
            this.scrollOffs = (this.menu).subtractInputFromScroll(this.scrollOffs, pDelta);
            (this.menu).scrollTo(this.scrollOffs);
            return true;
        }
    }

    protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
        boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
        this.hasClickedOutside = flag && !this.checkTabClicked(selectedTab, pMouseX, pMouseY);
        return this.hasClickedOutside;
    }

    protected boolean insideScrollbar(double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        int k = i + 175;
        int l = j + 18;
        int i1 = k + 14;
        int j1 = l + 112;
        return pMouseX >= (double)k && pMouseY >= (double)l && pMouseX < (double)i1 && pMouseY < (double)j1;
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling) {
            int i = this.topPos + 18;
            int j = i + 112;
            this.scrollOffs = ((float)pMouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            (this.menu).scrollTo(this.scrollOffs);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        for(CreativeModeTab creativemodetab : this.currentPage.getVisibleTabs()) {
            if (this.checkTabHovering(pGuiGraphics, creativemodetab, pMouseX, pMouseY)) {
                break;
            }
        }

        if (this.destroyItemSlot != null && selectedTab.getType() == CreativeModeTab.Type.INVENTORY && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, pMouseX, pMouseY)) {
            pGuiGraphics.renderTooltip(this.font, TRASH_SLOT_TOOLTIP, pMouseX, pMouseY);
        }

        if (this.pages.size() != 1) {
            Component page = Component.literal(String.format("%d / %d", this.pages.indexOf(this.currentPage) + 1, this.pages.size()));
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
            pGuiGraphics.drawString(this.font, page.getVisualOrderText(), this.leftPos + this.imageWidth / 2 - this.font.width(page) / 2, this.topPos - 44, -1);
            pGuiGraphics.pose().popPose();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    public List<Component> getTooltipFromContainerItem(ItemStack pStack) {
        boolean flag = this.hoveredSlot != null && this.hoveredSlot instanceof DarkWorldCreativeModeInventoryScreen.CustomCreativeSlot;
        boolean flag1 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
        boolean flag2 = selectedTab.hasSearchBar();
        TooltipFlag.Default tooltipflag$default = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        TooltipFlag tooltipflag = flag ? tooltipflag$default.asCreative() : tooltipflag$default;
        List<Component> list = pStack.getTooltipLines(this.minecraft.player, tooltipflag);
        if (flag1 && flag) {
            return list;
        } else {
            List<Component> list1 = Lists.newArrayList(list);
            if (flag2 && flag) {
                this.visibleTags.forEach((p_205407_) -> {
                    if (pStack.is(p_205407_)) {
                        list1.add(1, Component.literal("#" + p_205407_.location()).withStyle(ChatFormatting.DARK_PURPLE));
                    }

                });
            }

            int i = 1;

            for(CreativeModeTab creativemodetab : CreativeModeTabs.tabs()) {
                if (!creativemodetab.hasSearchBar() && creativemodetab.contains(pStack)) {
                    list1.add(i++, creativemodetab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
                }
            }

            return list1;
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        for(CreativeModeTab creativemodetab : this.currentPage.getVisibleTabs()) {
            if (creativemodetab != selectedTab) {
                this.renderTabButton(pGuiGraphics, creativemodetab);
            }
        }

        ResourceLocation backgroundLocation;
        ResourceLocation glowLocation = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/creative_inventory/tab_items_glow.png");
        float t = (float) this.glowTicker / (float) GLOW_TICKER_UPPER_BOUND;
        float glow = Mth.sin(t * Mth.PI);
        int w = 207;
        int h = 148;

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            backgroundLocation = new ResourceLocation(PenumbraPhantasm.MODID,  "textures/gui/dark_world/container/creative_inventory/tab_inventory.png");
        } else if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            backgroundLocation = new ResourceLocation(PenumbraPhantasm.MODID,  "textures/gui/dark_world/container/creative_inventory/tab_item_search.png");
        } else {
            backgroundLocation = new ResourceLocation(PenumbraPhantasm.MODID,  "textures/gui/dark_world/container/creative_inventory/tab_items.png");
        }

        pGuiGraphics.blit(backgroundLocation, this.leftPos - 6, this.topPos - 6, 0, 0, w, h);
        RenderBlitUtil.blitGui(pGuiGraphics, glowLocation, this.leftPos - 6, this.topPos - 6, 0, 0, w, h, glow, glow, glow, 1);

        this.searchBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int j = this.leftPos + 175;
        int k = this.topPos + 18;
        int i = k + 112;
        if (selectedTab.canScroll()) {
            pGuiGraphics.blit(CREATIVE_TABS_LOCATION, j, k + (int)((float)(i - k - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
        }

        if (this.currentPage.getVisibleTabs().contains(selectedTab)) {
            this.renderTabButton(pGuiGraphics, selectedTab);
        }

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(pGuiGraphics, this.leftPos + 88, this.topPos + 45, 20, (float)(this.leftPos + 88 - pMouseX), (float)(this.topPos + 45 - 30 - pMouseY), this.minecraft.player);
        }

    }

    private int getTabX(CreativeModeTab pTab) {
        int i = this.currentPage.getColumn(pTab);
        int j = 27;
        int k = 27 * i;
        if (pTab.isAlignedRight()) {
            k = this.imageWidth - 27 * (7 - i) + 1;
        }

        return k;
    }

    private int getTabY(CreativeModeTab pTab) {
        int i = 0;
        if (this.currentPage.isTop(pTab)) {
            i -= 32;
        } else {
            i += this.imageHeight;
        }

        return i;
    }

    protected boolean checkTabClicked(CreativeModeTab pCreativeModeTab, double pRelativeMouseX, double pRelativeMouseY) {
        int i = this.getTabX(pCreativeModeTab);
        int j = this.getTabY(pCreativeModeTab);
        return pRelativeMouseX >= (double)i && pRelativeMouseX <= (double)(i + 26) && pRelativeMouseY >= (double)j && pRelativeMouseY <= (double)(j + 32);
    }

    protected boolean checkTabHovering(GuiGraphics pGuiGraphics, CreativeModeTab pCreativeModeTab, int pMouseX, int pMouseY) {
        int i = this.getTabX(pCreativeModeTab);
        int j = this.getTabY(pCreativeModeTab);
        if (this.isHovering(i + 3, j + 3, 21, 27, pMouseX, pMouseY)) {
            pGuiGraphics.renderTooltip(this.font, pCreativeModeTab.getDisplayName(), pMouseX, pMouseY);
            return true;
        } else {
            return false;
        }
    }

    protected void renderTabButton(GuiGraphics pGuiGraphics, CreativeModeTab pCreativeModeTab) {
        boolean flag = pCreativeModeTab == selectedTab;
        boolean flag1 = this.currentPage.isTop(pCreativeModeTab);
        int i = this.currentPage.getColumn(pCreativeModeTab);
        int j = i * 26;
        int k = 0;
        int l = this.leftPos + this.getTabX(pCreativeModeTab);
        int i1 = this.topPos;
        int j1 = 32;
        if (flag) {
            k += 32;
        }

        if (flag1) {
            i1 -= 28;
        } else {
            k += 64;
            i1 += this.imageHeight - 4;
        }

        RenderSystem.enableBlend();
        pGuiGraphics.blit(CREATIVE_TABS_LOCATION, l, i1, j, k, 26, 32);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        l += 5;
        i1 += 8 + (flag1 ? 1 : -1);
        ItemStack itemstack = pCreativeModeTab.getIconItem();
        pGuiGraphics.renderItem(itemstack, l, i1);
        pGuiGraphics.renderItemDecorations(this.font, itemstack, l, i1);
        pGuiGraphics.pose().popPose();
    }

    public boolean isInventoryOpen() {
        return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    public static void handleHotbarLoadOrSave(Minecraft pClient, int pIndex, boolean pLoad, boolean pSave) {
        LocalPlayer localplayer = pClient.player;
        HotbarManager hotbarmanager = pClient.getHotbarManager();
        Hotbar hotbar = hotbarmanager.get(pIndex);
        if (pLoad) {
            for(int i = 0; i < Inventory.getSelectionSize(); ++i) {
                ItemStack itemstack = hotbar.get(i);
                ItemStack itemstack1 = itemstack.isItemEnabled(localplayer.level().enabledFeatures()) ? itemstack.copy() : ItemStack.EMPTY;
                localplayer.getInventory().setItem(i, itemstack1);
                pClient.gameMode.handleCreativeModeItemAdd(itemstack1, 36 + i);
            }

            localplayer.inventoryMenu.broadcastChanges();
        } else if (pSave) {
            for(int j = 0; j < Inventory.getSelectionSize(); ++j) {
                hotbar.set(j, localplayer.getInventory().getItem(j).copy());
            }

            Component component = pClient.options.keyHotbarSlots[pIndex].getTranslatedKeyMessage();
            Component component1 = pClient.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            Component component2 = Component.translatable("inventory.hotbarSaved", new Object[]{component1, component});
            pClient.gui.setOverlayMessage(component2, false);
            pClient.getNarrator().sayNow(component2);
            hotbarmanager.save();
        }

    }

    public CreativeTabsScreenPage getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(CreativeTabsScreenPage currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot) {
        int x = pSlot.x;
        int y = pSlot.y;
        ItemStack itemstack = pSlot.getItem();
        boolean flag = false;
        boolean flag1 = pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemstack1 = this.menu.getCarried();
        String s = null;

        if (pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(pSlot) && !itemstack1.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }
            if (AbstractContainerMenu.canItemQuickReplace(pSlot, itemstack1, true) && this.menu.canDragTo(pSlot)) {
                flag = true;
                int k = Math.min(itemstack1.getMaxStackSize(), pSlot.getMaxStackSize(itemstack1));
                int l = pSlot.getItem().isEmpty() ? 0 : pSlot.getItem().getCount();
                int i1 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemstack1) + l;
                if (i1 > k) {
                    i1 = k;
                    s = ChatFormatting.YELLOW + String.valueOf(k);
                }
                itemstack = itemstack1.copyWithCount(i1);
            } else {
                this.quickCraftSlots.remove(pSlot);
                this.recalculateQuickCraftRemaining();
            }
        }

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);

        if (!flag1) {
            ResourceLocation emptyTexture = this.getEmptySlotTexture(pSlot, itemstack);
            if (emptyTexture != null) {
                pGuiGraphics.blit(emptyTexture, x, y, 0, 0, 16, 16, 16, 16);
            }

            if (flag) {
                pGuiGraphics.fill(x, y, x + 16, y + 16, -2130706433);
            }

            pGuiGraphics.renderItem(itemstack, x, y, x + y * this.imageWidth);
            pGuiGraphics.renderItemDecorations(this.font, itemstack, x, y, s);
        }

        pGuiGraphics.pose().popPose();
    }

    private ResourceLocation getEmptySlotTexture(Slot pSlot, ItemStack pStack) {
        if (!pStack.isEmpty() || !(pSlot instanceof SlotWrapper)) {
            return null;
        }

        int slotIndex = ((SlotWrapper) pSlot).target.index;

        return switch (slotIndex) {
            case 5 -> EMPTY_EQUIPMENT_SLOT_HELMET;
            case 6 -> EMPTY_EQUIPMENT_SLOT_CHESTPLATE;
            case 7 -> EMPTY_EQUIPMENT_SLOT_LEGGINGS;
            case 8 -> EMPTY_EQUIPMENT_SLOT_BOOTS;
            case 45 -> EMPTY_EQUIPMENT_SLOT_OFFHAND;
            default -> null;
        };
    }

    @OnlyIn(Dist.CLIENT)
    static class CustomCreativeSlot extends Slot {
        public CustomCreativeSlot(Container pContainer, int pSlot, int pX, int pY) {
            super(pContainer, pSlot, pX, pY);
        }

        public boolean mayPickup(Player pPlayer) {
            ItemStack itemstack = this.getItem();
            if (super.mayPickup(pPlayer) && !itemstack.isEmpty()) {
                return itemstack.isItemEnabled(pPlayer.level().enabledFeatures()) && itemstack.getTagElement("CustomCreativeLock") == null;
            } else {
                return itemstack.isEmpty();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemPickerMenu extends AbstractContainerMenu {
        public final NonNullList<ItemStack> items = NonNullList.create();
        private final AbstractContainerMenu inventoryMenu;

        public ItemPickerMenu(Player pPlayer) {
            super(null, 0);
            this.inventoryMenu = pPlayer.inventoryMenu;
            Inventory inventory = pPlayer.getInventory();

            for(int i = 0; i < 5; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new DarkWorldCreativeModeInventoryScreen.CustomCreativeSlot(DarkWorldCreativeModeInventoryScreen.CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k, 9 + k * 18, 112));
            }

            this.scrollTo(0.0F);
        }

        public boolean stillValid(Player pPlayer) {
            return true;
        }

        protected int calculateRowCount() {
            return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
        }

        protected int getRowIndexForScroll(float pScrollOffs) {
            return Math.max((int)((double)(pScrollOffs * (float)this.calculateRowCount()) + (double)0.5F), 0);
        }

        protected float getScrollForRowIndex(int pRowIndex) {
            return Mth.clamp((float)pRowIndex / (float)this.calculateRowCount(), 0.0F, 1.0F);
        }

        protected float subtractInputFromScroll(float pScrollOffs, double pInput) {
            return Mth.clamp(pScrollOffs - (float)(pInput / (double)this.calculateRowCount()), 0.0F, 1.0F);
        }

        public void scrollTo(float pPos) {
            int i = this.getRowIndexForScroll(pPos);

            for(int j = 0; j < 5; ++j) {
                for(int k = 0; k < 9; ++k) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < this.items.size()) {
                        DarkWorldCreativeModeInventoryScreen.CONTAINER.setItem(k + j * 9, (ItemStack)this.items.get(l));
                    } else {
                        DarkWorldCreativeModeInventoryScreen.CONTAINER.setItem(k + j * 9, ItemStack.EMPTY);
                    }
                }
            }

        }

        public boolean canScroll() {
            return this.items.size() > 45;
        }

        public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
            if (pIndex >= this.slots.size() - 9 && pIndex < this.slots.size()) {
                Slot slot = (Slot)this.slots.get(pIndex);
                if (slot != null && slot.hasItem()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
            return pSlot.container != DarkWorldCreativeModeInventoryScreen.CONTAINER;
        }

        public boolean canDragTo(Slot pSlot) {
            return pSlot.container != DarkWorldCreativeModeInventoryScreen.CONTAINER;
        }

        public ItemStack getCarried() {
            return this.inventoryMenu.getCarried();
        }

        public void setCarried(ItemStack pStack) {
            this.inventoryMenu.setCarried(pStack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class SlotWrapper extends Slot {
        final Slot target;

        public SlotWrapper(Slot pSlot, int pIndex, int pX, int pY) {
            super(pSlot.container, pIndex, pX, pY);
            this.target = pSlot;
        }

        public void onTake(Player pPlayer, ItemStack pStack) {
            this.target.onTake(pPlayer, pStack);
        }

        public boolean mayPlace(ItemStack pStack) {
            return this.target.mayPlace(pStack);
        }

        public ItemStack getItem() {
            return this.target.getItem();
        }

        public boolean hasItem() {
            return this.target.hasItem();
        }

        public void setByPlayer(ItemStack pStack) {
            this.target.setByPlayer(pStack);
        }

        public void set(ItemStack pStack) {
            this.target.set(pStack);
        }

        public void setChanged() {
            this.target.setChanged();
        }

        public int getMaxStackSize() {
            return this.target.getMaxStackSize();
        }

        public int getMaxStackSize(ItemStack pStack) {
            return this.target.getMaxStackSize(pStack);
        }

        @Nullable
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return this.target.getNoItemIcon();
        }

        public ItemStack remove(int pAmount) {
            return this.target.remove(pAmount);
        }

        public boolean isActive() {
            return this.target.isActive();
        }

        public boolean mayPickup(Player pPlayer) {
            return this.target.mayPickup(pPlayer);
        }

        public int getSlotIndex() {
            return this.target.getSlotIndex();
        }

        public boolean isSameInventory(Slot other) {
            return this.target.isSameInventory(other);
        }

        public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
            this.target.setBackground(atlas, sprite);
            return this;
        }
    }
}