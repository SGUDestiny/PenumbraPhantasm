package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.client.render.item.DeltaShieldRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

//TODO:
// - Make parry mechanic

public class DeltaShieldItem extends ShieldItem {
    public DeltaShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return DeltaShieldRenderer.INSTANCE;
            }
        });
    }


}
