package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.client.render.item.CheshireChestItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class CheshireChestItem extends BlockItem {
    public CheshireChestItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private CheshireChestItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    renderer = new CheshireChestItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }

                return renderer;
            }
        });
    }
}
