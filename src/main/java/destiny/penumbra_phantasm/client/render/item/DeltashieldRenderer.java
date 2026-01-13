package destiny.penumbra_phantasm.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.models.item.DeltashieldModel;
import destiny.penumbra_phantasm.server.item.DeltashieldItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DeltashieldRenderer extends BlockEntityWithoutLevelRenderer {
    public static DeltashieldRenderer INSTANCE = new DeltashieldRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    public final BlockEntityRenderDispatcher dispatcher;
    public final EntityModelSet modelSet;

    public DeltashieldModel deltashieldModel;

    public DeltashieldRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.dispatcher = dispatcher;
        this.modelSet = modelSet;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        deltashieldModel = new DeltashieldModel(this.modelSet.bakeLayer(DeltashieldModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source, int light, int overlay) {
        if (stack.getItem() instanceof DeltashieldItem) {
            poseStack.pushPose();
            poseStack.scale(1f, -1f, -1f);
            VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(source, this.deltashieldModel.renderType(new ResourceLocation(PenumbraPhantasm.MODID, "textures/model/shield/deltashield.png")), true, stack.hasFoil());
            this.deltashieldModel.renderToBuffer(poseStack, consumer, light, overlay, 1, 1, 1, 1);
            poseStack.popPose();
        }
    }
}
