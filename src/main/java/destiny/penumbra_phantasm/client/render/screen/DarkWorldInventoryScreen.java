package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class DarkWorldInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    public static final ResourceLocation DARK_WORLD_INVENTORY_LOCATION = new ResourceLocation(PenumbraPhantasm.MODID, "textures/gui/dark_world/container/inventory.png");
    private float xMouse;
    private float yMouse;
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;
    private boolean buttonClicked;
    public Component inventoryLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_title");
    public Component craftingLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_crafting");
    public Component equippedLabel = Component.translatable("gui.penumbra_phantasm.dark_world_inventory_equipped");
    public int equippedLabelX = 57;
    public int equippedLabelY = 4;
    public int slotsOffsetX = 0;
    public int slotsOffsetY = 10;

    public DarkWorldInventoryScreen(Player pPlayer) {
        super(pPlayer.inventoryMenu, pPlayer.getInventory(), Component.translatable("container.crafting"));
        this.imageWidth = 188;
        this.imageHeight = 185;
        this.inventoryLabelX = 57;
        this.inventoryLabelY = 87;
        this.titleLabelX = 137;
        this.titleLabelY = 4;
    }

    public void containerTick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
        } else {
            this.recipeBookComponent.tick();
        }

    }

    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
        } else {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_289631_) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                p_289631_.setPosition(this.leftPos + 104, this.height / 2 - 22);
                this.buttonClicked = true;
            }));
            this.addWidget(this.recipeBookComponent);
            this.setInitialFocus(this.recipeBookComponent);
        }

    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        //Equipped label
        this.drawCenteredString(pGuiGraphics, this.font, this.equippedLabel, this.equippedLabelX, this.equippedLabelY, -1, false);
        //Crafting label
        this.drawCenteredString(pGuiGraphics, this.font, this.craftingLabel, this.titleLabelX, this.titleLabelY, -1, false);
        //Inventory label
        this.drawCenteredString(pGuiGraphics, this.font, this.inventoryLabel, this.inventoryLabelX, this.inventoryLabelY, -1, false);
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, pGuiGraphics, pMouseX, pMouseY));
        RenderSystem.disableDepthTest();
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((float)i, (float)j, 0.0F);
        this.hoveredSlot = null;

        for(int k = 0; k < this.menu.slots.size(); ++k) {
            Slot slot = this.menu.slots.get(k);
            if (slot.isActive()) {
                this.renderSlot(pGuiGraphics, slot);
            }

            if (this.isHovering(slot, pMouseX, pMouseY) && slot.isActive()) {
                this.hoveredSlot = slot;
                int x = slot.x + slotsOffsetX - slotsOffsetX;
                int y = slot.y + slotsOffsetY - slotsOffsetY;
                if (this.hoveredSlot.isHighlightable()) {
                    renderSlotHighlight(pGuiGraphics, x, y, 0, this.getSlotColor(k));
                }
            }
        }

        this.renderLabels(pGuiGraphics, pMouseX, pMouseY);
        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, pGuiGraphics, pMouseX, pMouseY));
        ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (!itemstack.isEmpty()) {
            int l1 = 8;
            int i2 = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemstack = itemstack.copyWithCount(Mth.ceil((float)itemstack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemstack = itemstack.copyWithCount(this.quickCraftingRemainder);
                if (itemstack.isEmpty()) {
                    s = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(pGuiGraphics, itemstack, pMouseX - i - 8, pMouseY - j - i2, s);
        }

        if (!this.snapbackItem.isEmpty()) {
            float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int j2 = this.snapbackEnd.x - this.snapbackStartX;
            int k2 = this.snapbackEnd.y - this.snapbackStartY;
            int j1 = this.snapbackStartX + (int)((float)j2 * f);
            int k1 = this.snapbackStartY + (int)((float)k2 * f);
            this.renderFloatingItem(pGuiGraphics, this.snapbackItem, j1, k1, null);
        }

        pGuiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(DARK_WORLD_INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(pGuiGraphics, i + 57, j + 81, 30, (float)(i + 57) - this.xMouse, (float)(j + 81 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float pMouseX, float pMouseY, LivingEntity pEntity) {
        float f = (float)Math.atan((pMouseX / 40.0F));
        float f1 = (float)Math.atan((pMouseY / 40.0F));
        renderEntityInInventoryFollowsAngle(pGuiGraphics, pX, pY, pScale, f, f1, pEntity);
    }

    public static void renderEntityInInventoryFollowsAngle(GuiGraphics p_282802_, int p_275688_, int p_275245_, int p_275535_, float angleXComponent, float angleYComponent, LivingEntity p_275689_) {
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f2 = p_275689_.yBodyRot;
        float f3 = p_275689_.getYRot();
        float f4 = p_275689_.getXRot();
        float f5 = p_275689_.yHeadRotO;
        float f6 = p_275689_.yHeadRot;
        p_275689_.yBodyRot = 180.0F + angleXComponent * 20.0F;
        p_275689_.setYRot(180.0F + angleXComponent * 40.0F);
        p_275689_.setXRot(-angleYComponent * 20.0F);
        p_275689_.yHeadRot = p_275689_.getYRot();
        p_275689_.yHeadRotO = p_275689_.getYRot();
        renderEntityInInventory(p_282802_, p_275688_, p_275245_, p_275535_, quaternionf, quaternionf1, p_275689_);
        p_275689_.yBodyRot = f2;
        p_275689_.setYRot(f3);
        p_275689_.setXRot(f4);
        p_275689_.yHeadRotO = f5;
        p_275689_.yHeadRot = f6;
    }

    public static void renderEntityInInventory(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, Quaternionf pPose, @Nullable Quaternionf pCameraOrientation, LivingEntity pEntity) {
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(pX, pY, (double)50.0F);
        pGuiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)pScale, (float)pScale, (float)(-pScale)));
        pGuiGraphics.pose().mulPose(pPose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (pCameraOrientation != null) {
            pCameraOrientation.conjugate();
            entityrenderdispatcher.overrideCameraOrientation(pCameraOrientation);
        }

        entityrenderdispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(pEntity, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, pGuiGraphics.pose(), pGuiGraphics.bufferSource(), 15728880));
        pGuiGraphics.flush();
        entityrenderdispatcher.setRenderShadow(true);
        pGuiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.recipeBookComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(pMouseX, pMouseY, pButton);
        }
    }

    protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
        boolean flag = pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(pMouseX, pMouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pMouseButton) && flag;
    }

    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        this.recipeBookComponent.slotClicked(pSlot);
    }

    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    @Override
    public void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot) {
        int x = pSlot.x - slotsOffsetX;
        int y = pSlot.y - slotsOffsetY;
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
                    String var10000 = ChatFormatting.YELLOW.toString();
                    s = var10000 + k;
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
            if (flag) {
                pGuiGraphics.fill(x, y, x + 16, y + 16, -2130706433);
            }

            pGuiGraphics.renderItem(itemstack, x, y, x + y * this.imageWidth);
            pGuiGraphics.renderItemDecorations(this.font, itemstack, x, y, s);
        }

        pGuiGraphics.pose().popPose();
    }

    public void drawCenteredString(GuiGraphics graphics, Font pFont, Component pText, int pX, int pY, int pColor, boolean dropShadow) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        this.drawString(graphics, pFont, formattedcharsequence, pX - pFont.width(formattedcharsequence) / 2f, pY, pColor, dropShadow);
    }

    public void drawString(GuiGraphics graphics, Font font, FormattedCharSequence charSequence, float textX, float textY, int color, boolean dropShadow) {
        PoseStack poseStack = graphics.pose();
        MultiBufferSource bufferSource = graphics.bufferSource();

        font.drawInBatch(charSequence, textX, textY, color, dropShadow, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
    }
}
