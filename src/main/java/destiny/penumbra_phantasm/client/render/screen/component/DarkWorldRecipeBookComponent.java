package destiny.penumbra_phantasm.client.render.screen.component;

import com.google.common.collect.Lists;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.*;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class DarkWorldRecipeBookComponent extends RecipeBookComponent implements PlaceRecipe<Ingredient>, Renderable, GuiEventListener, NarratableEntry, RecipeShownListener {
    protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/recipe_book.png");
    private static final Component SEARCH_HINT;
    public static final int IMAGE_WIDTH = 147;
    public static final int IMAGE_HEIGHT = 166;
    private static final int OFFSET_X_POSITION = 86;
    private static final Component ONLY_CRAFTABLES_TOOLTIP;
    private static final Component ALL_RECIPES_TOOLTIP;
    private int xOffset;
    private int width;
    private int height;
    protected final GhostRecipe ghostRecipe = new GhostRecipe();
    private final List<RecipeBookTabButton> tabButtons = Lists.newArrayList();
    @Nullable
    private RecipeBookTabButton selectedTab;
    protected StateSwitchingButton filterButton;
    protected RecipeBookMenu<?> menu;
    protected Minecraft minecraft;
    @Nullable
    private EditBox searchBox;
    private String lastSearch = "";
    private ClientRecipeBook book;
    private final DarkWorldRecipeBookPage recipeBookPage = new DarkWorldRecipeBookPage();
    private final StackedContents stackedContents = new StackedContents();
    private int timesInventoryChanged;
    private boolean ignoreTextInput;
    private boolean visible;
    private boolean widthTooNarrow;

    public DarkWorldRecipeBookComponent() {
    }

    @Override
    public void init(int pWidth, int pHeight, Minecraft pMinecraft, boolean pWidthTooNarrow, RecipeBookMenu<?> pMenu) {
        this.minecraft = pMinecraft;
        this.width = pWidth;
        this.height = pHeight;
        this.menu = pMenu;
        this.widthTooNarrow = pWidthTooNarrow;
        pMinecraft.player.containerMenu = pMenu;
        this.book = pMinecraft.player.getRecipeBook();
        this.timesInventoryChanged = pMinecraft.player.getInventory().getTimesChanged();
        this.visible = this.isVisibleAccordingToBookData();
        if (this.visible) {
            this.initVisuals();
        }

    }

    @Override
    public void initVisuals() {
        this.xOffset = this.widthTooNarrow ? 0 : 86;
        int i = (this.width - 147) / 2 - this.xOffset;
        int j = (this.height - 166) / 2;
        this.stackedContents.clear();
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.minecraft.font, i + 26, j + 14, 79, 12, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(s);
        this.searchBox.setHint(SEARCH_HINT);
        this.recipeBookPage.init(this.minecraft, i, j);
        this.recipeBookPage.addListener(this);
        this.filterButton = new StateSwitchingButton(i + 110, j + 12, 26, 16, this.book.isFiltering(this.menu));
        this.updateFilterButtonTooltip();
        this.initFilterButtonTextures();
        this.tabButtons.clear();

        for (RecipeBookCategories recipebookcategories : this.menu.getRecipeBookCategories()) {
            this.tabButtons.add(new RecipeBookTabButton(recipebookcategories));
        }

        if (this.selectedTab != null) {
            this.selectedTab = (RecipeBookTabButton) this.tabButtons.stream().filter((p_100329_) -> p_100329_.getCategory().equals(this.selectedTab.getCategory())).findFirst().orElse((RecipeBookTabButton) null);
        }

        if (this.selectedTab == null) {
            this.selectedTab = (RecipeBookTabButton) this.tabButtons.get(0);
        }

        this.selectedTab.setStateTriggered(true);
        this.updateCollections(false);
        this.updateTabs();
    }

    private void updateFilterButtonTooltip() {
        this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(ALL_RECIPES_TOOLTIP));
    }

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 41, 28, 18, RECIPE_BOOK_LOCATION);
    }

    @Override
    public int updateScreenPosition(int pWidth, int pImageWidth) {
        int i;
        if (this.isVisible() && !this.widthTooNarrow) {
            i = 177 + (pWidth - pImageWidth - 200) / 2;
        } else {
            i = (pWidth - pImageWidth) / 2;
        }

        return i;
    }

    @Override
    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    private boolean isVisibleAccordingToBookData() {
        return this.book.isOpen(this.menu.getRecipeBookType());
    }

    @Override
    protected void setVisible(boolean pVisible) {
        if (pVisible) {
            this.initVisuals();
        }

        this.visible = pVisible;
        this.book.setOpen(this.menu.getRecipeBookType(), pVisible);
        if (!pVisible) {
            this.recipeBookPage.setInvisible();
        }

        this.sendUpdateSettings();
    }

    @Override
    public void slotClicked(@Nullable Slot pSlot) {
        if (pSlot != null && pSlot.index < this.menu.getSize()) {
            this.ghostRecipe.clear();
            if (this.isVisible()) {
                this.updateStackedContents();
            }
        }

    }

    private void updateCollections(boolean pResetPageNumber) {
        List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
        list.forEach((p_100381_) -> p_100381_.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
        List<RecipeCollection> list1 = Lists.newArrayList(list);
        list1.removeIf((p_100368_) -> !p_100368_.hasKnownRecipes());
        list1.removeIf((p_100360_) -> !p_100360_.hasFitting());
        String s = this.searchBox.getValue();
        if (!s.isEmpty()) {
            ObjectSet<RecipeCollection> objectset = new ObjectLinkedOpenHashSet(this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(s.toLowerCase(Locale.ROOT)));
            list1.removeIf((p_100334_) -> !objectset.contains(p_100334_));
        }

        if (this.book.isFiltering(this.menu)) {
            list1.removeIf((p_100331_) -> !p_100331_.hasCraftable());
        }

        this.recipeBookPage.updateCollections(list1, pResetPageNumber);
    }

    private void updateTabs() {
        int i = (this.width - 147) / 2 - this.xOffset - 30;
        int j = (this.height - 166) / 2 + 3;
        int k = 27;
        int l = 0;

        for (RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
            RecipeBookCategories recipebookcategories = recipebooktabbutton.getCategory();
            if (recipebookcategories != RecipeBookCategories.CRAFTING_SEARCH && recipebookcategories != RecipeBookCategories.FURNACE_SEARCH) {
                if (recipebooktabbutton.updateVisibility(this.book)) {
                    recipebooktabbutton.setPosition(i, j + 27 * l++);
                    recipebooktabbutton.startAnimation(this.minecraft);
                }
            } else {
                recipebooktabbutton.visible = true;
                recipebooktabbutton.setPosition(i, j + 27 * l++);
            }
        }

    }

    @Override
    public void tick() {
        boolean flag = this.isVisibleAccordingToBookData();
        if (this.isVisible() != flag) {
            this.setVisible(flag);
        }

        if (this.isVisible()) {
            if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
                this.updateStackedContents();
                this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
            }

            this.searchBox.tick();
        }

    }

    private void updateStackedContents() {
        this.stackedContents.clear();
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        this.updateCollections(false);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.isVisible()) {
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
            int i = (this.width - 147) / 2 - this.xOffset;
            int j = (this.height - 166) / 2;
            pGuiGraphics.blit(RECIPE_BOOK_LOCATION, i, j, 1, 1, 147, 166);
            this.searchBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

            for (RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                recipebooktabbutton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }

            this.filterButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            this.recipeBookPage.render(pGuiGraphics, i, j, pMouseX, pMouseY, pPartialTick);
            pGuiGraphics.pose().popPose();
        }

    }

    @Override
    public void renderTooltip(GuiGraphics pGuiGraphics, int pRenderX, int pRenderY, int pMouseX, int pMouseY) {
        if (this.isVisible()) {
            this.recipeBookPage.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
            this.renderGhostRecipeTooltip(pGuiGraphics, pRenderX, pRenderY, pMouseX, pMouseY);
        }

    }

    @Override
    protected Component getRecipeFilterName() {
        return ONLY_CRAFTABLES_TOOLTIP;
    }

    private void renderGhostRecipeTooltip(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
        ItemStack itemstack = null;

        for (int i = 0; i < this.ghostRecipe.size(); ++i) {
            GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ghostRecipe.get(i);
            int j = ghostrecipe$ghostingredient.getX() + pX;
            int k = ghostrecipe$ghostingredient.getY() + pY;
            if (pMouseX >= j && pMouseY >= k && pMouseX < j + 16 && pMouseY < k + 16) {
                itemstack = ghostrecipe$ghostingredient.getItem();
            }
        }

        if (itemstack != null && this.minecraft.screen != null) {
            pGuiGraphics.renderComponentTooltip(this.minecraft.font, Screen.getTooltipFromItem(this.minecraft, itemstack), pMouseX, pMouseY, itemstack);
        }

    }

    @Override
    public void renderGhostRecipe(GuiGraphics pGuiGraphics, int pLeftPos, int pTopPos, boolean p_283495_, float pPartialTick) {
        this.ghostRecipe.render(pGuiGraphics, this.minecraft, pLeftPos, pTopPos, p_283495_, pPartialTick);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.recipeBookPage.mouseClicked(pMouseX, pMouseY, pButton, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166)) {
                Recipe<?> recipe = this.recipeBookPage.getLastClickedRecipe();
                RecipeCollection recipecollection = this.recipeBookPage.getLastClickedRecipeCollection();
                if (recipe != null && recipecollection != null) {
                    if (!recipecollection.isCraftable(recipe) && this.ghostRecipe.getRecipe() == recipe) {
                        return false;
                    }

                    this.ghostRecipe.clear();
                    this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipe, Screen.hasShiftDown());
                    if (!this.isOffsetNextToMainGUI()) {
                        this.setVisible(false);
                    }
                }

                return true;
            } else if (this.searchBox.mouseClicked(pMouseX, pMouseY, pButton)) {
                this.searchBox.setFocused(true);
                return true;
            } else {
                this.searchBox.setFocused(false);
                if (this.filterButton.mouseClicked(pMouseX, pMouseY, pButton)) {
                    boolean flag = this.toggleFiltering();
                    this.filterButton.setStateTriggered(flag);
                    this.updateFilterButtonTooltip();
                    this.sendUpdateSettings();
                    this.updateCollections(false);
                    return true;
                } else {
                    for (RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                        if (recipebooktabbutton.mouseClicked(pMouseX, pMouseY, pButton)) {
                            if (this.selectedTab != recipebooktabbutton) {
                                if (this.selectedTab != null) {
                                    this.selectedTab.setStateTriggered(false);
                                }

                                this.selectedTab = recipebooktabbutton;
                                this.selectedTab.setStateTriggered(true);
                                this.updateCollections(true);
                            }

                            return true;
                        }
                    }

                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private boolean toggleFiltering() {
        RecipeBookType recipebooktype = this.menu.getRecipeBookType();
        boolean flag = !this.book.isFiltering(recipebooktype);
        this.book.setFiltering(recipebooktype, flag);
        return flag;
    }

    @Override
    public boolean hasClickedOutside(double pMouseX, double pMouseY, int pX, int pY, int pWidth, int pHeight, int p_100304_) {
        if (!this.isVisible()) {
            return true;
        } else {
            boolean flag = pMouseX < (double) pX || pMouseY < (double) pY || pMouseX >= (double) (pX + pWidth) || pMouseY >= (double) (pY + pHeight);
            boolean flag1 = (double) (pX - 147) < pMouseX && pMouseX < (double) pX && (double) pY < pMouseY && pMouseY < (double) (pY + pHeight);
            return flag && !flag1 && !this.selectedTab.isHoveredOrFocused();
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        this.ignoreTextInput = false;
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (pKeyCode == 256 && !this.isOffsetNextToMainGUI()) {
                this.setVisible(false);
                return true;
            } else if (this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                this.checkSearchStringUpdate();
                return true;
            } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && pKeyCode != 256) {
                return true;
            } else if (this.minecraft.options.keyChat.matches(pKeyCode, pScanCode) && !this.searchBox.isFocused()) {
                this.ignoreTextInput = true;
                this.searchBox.setFocused(true);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        this.ignoreTextInput = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (this.ignoreTextInput) {
            return false;
        } else if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.searchBox.charTyped(pCodePoint, pModifiers)) {
                this.checkSearchStringUpdate();
                return true;
            } else {
                return super.charTyped(pCodePoint, pModifiers);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return false;
    }

    @Override
    public void setFocused(boolean pFocused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    private void checkSearchStringUpdate() {
        String s = this.searchBox.getValue().toLowerCase(Locale.ROOT);
        this.pirateSpeechForThePeople(s);
        if (!s.equals(this.lastSearch)) {
            this.updateCollections(false);
            this.lastSearch = s;
        }

    }

    private void pirateSpeechForThePeople(String pText) {
        if ("excitedze".equals(pText)) {
            LanguageManager languagemanager = this.minecraft.getLanguageManager();
            String s = "en_pt";
            LanguageInfo languageinfo = languagemanager.getLanguage("en_pt");
            if (languageinfo == null || languagemanager.getSelected().equals("en_pt")) {
                return;
            }

            languagemanager.setSelected("en_pt");
            this.minecraft.options.languageCode = "en_pt";
            this.minecraft.reloadResourcePacks();
            this.minecraft.options.save();
        }

    }

    private boolean isOffsetNextToMainGUI() {
        return this.xOffset == 86;
    }

    @Override
    public void recipesUpdated() {
        this.updateTabs();
        if (this.isVisible()) {
            this.updateCollections(false);
        }

    }

    @Override
    public void recipesShown(List<Recipe<?>> pRecipes) {
        for (Recipe<?> recipe : pRecipes) {
            this.minecraft.player.removeRecipeHighlight(recipe);
        }

    }



    @Override
    public void setupGhostRecipe(Recipe<?> pRecipe, List<Slot> pSlots) {
        ItemStack itemstack = pRecipe.getResultItem(this.minecraft.level.registryAccess());
        this.ghostRecipe.setRecipe(pRecipe);
        this.ghostRecipe.addIngredient(Ingredient.of(new ItemStack[]{itemstack}), ((Slot) pSlots.get(0)).x, ((Slot) pSlots.get(0)).y);
        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), pRecipe, pRecipe.getIngredients().iterator(), 0);
    }

    @Override
    public void addItemToSlot(Iterator<Ingredient> pIngredients, int pSlot, int pMaxAmount, int pY, int pX) {
        Ingredient ingredient = (Ingredient) pIngredients.next();
        if (!ingredient.isEmpty()) {
            Slot slot = (Slot) this.menu.slots.get(pSlot);
            this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
        }

    }

    @Override
    protected void sendUpdateSettings() {
        if (this.minecraft.getConnection() != null) {
            RecipeBookType recipebooktype = this.menu.getRecipeBookType();
            boolean flag = this.book.getBookSettings().isOpen(recipebooktype);
            boolean flag1 = this.book.getBookSettings().isFiltering(recipebooktype);
            this.minecraft.getConnection().send(new ServerboundRecipeBookChangeSettingsPacket(recipebooktype, flag, flag1));
        }

    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.visible ? NarrationPriority.HOVERED : NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        List<NarratableEntry> list = Lists.newArrayList();
        this.recipeBookPage.listButtons((p_170049_) -> {
            if (p_170049_.isActive()) {
                list.add(p_170049_);
            }

        });
        list.add(this.searchBox);
        list.add(this.filterButton);
        list.addAll(this.tabButtons);
        Screen.NarratableSearchResult screen$narratablesearchresult = Screen.findNarratableWidget(list, (NarratableEntry) null);
        if (screen$narratablesearchresult != null) {
            screen$narratablesearchresult.entry.updateNarration(pNarrationElementOutput.nest());
        }

    }

    static {
        SEARCH_HINT = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
        ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
        ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");
    }
}