package destiny.penumbra_phantasm.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.client.render.blockentity.CheshireChestBlockEntityRenderer;
import destiny.penumbra_phantasm.server.block.entity.CheshireChestBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CheshireChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static CheshireChestBlockEntity dummyChest = null;
    private final CheshireChestBlockEntityRenderer blockRenderer;

    public CheshireChestItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);

        Minecraft mc = Minecraft.getInstance();

        BlockEntityRendererProvider.Context context = new BlockEntityRendererProvider.Context(dispatcher, mc.getBlockRenderer(), mc.getItemRenderer(),
                mc.getEntityRenderDispatcher(), modelSet, mc.font
        );

        this.blockRenderer = new CheshireChestBlockEntityRenderer(context);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (dummyChest == null) {
            dummyChest = new CheshireChestBlockEntity(BlockPos.ZERO, BlockRegistry.CHESHIRE_CHEST.get().defaultBlockState());
        }
        blockRenderer.render(dummyChest, 0, poseStack, buffer, packedLight, packedOverlay);
    }
}
