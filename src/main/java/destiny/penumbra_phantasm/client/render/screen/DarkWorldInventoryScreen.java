package destiny.penumbra_phantasm.client.render.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    public DarkWorldInventoryScreen(Player pPlayer) {
        super(pPlayer.inventoryMenu, pPlayer.getInventory(), Component.translatable("container.crafting"));
        this.titleLabelX = 97;
        this.imageWidth = 184;
        this.imageHeight = 180;
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
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (RecipeBookMenu)this.menu);
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
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        } else {
            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            this.recipeBookComponent.renderGhostRecipe(pGuiGraphics, this.leftPos, this.topPos, false, pPartialTick);
        }

        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        this.recipeBookComponent.renderTooltip(pGuiGraphics, this.leftPos, this.topPos, pMouseX, pMouseY);
        this.xMouse = (float)pMouseX;
        this.yMouse = (float)pMouseY;
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(DARK_WORLD_INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(pGuiGraphics, i + 51, j + 75, 30, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float pMouseX, float pMouseY, LivingEntity pEntity) {
        float f = (float)Math.atan((double)(pMouseX / 40.0F));
        float f1 = (float)Math.atan((double)(pMouseY / 40.0F));
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
        pGuiGraphics.pose().translate((double)pX, (double)pY, (double)50.0F);
        pGuiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)pScale, (float)pScale, (float)(-pScale)));
        pGuiGraphics.pose().mulPose(pPose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (pCameraOrientation != null) {
            pCameraOrientation.conjugate();
            entityrenderdispatcher.overrideCameraOrientation(pCameraOrientation);
        }

        entityrenderdispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(pEntity, (double)0.0F, (double)0.0F, (double)0.0F, 0.0F, 1.0F, pGuiGraphics.pose(), pGuiGraphics.bufferSource(), 15728880));
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
        super.renderSlot(pGuiGraphics, pSlot);
    }
}
