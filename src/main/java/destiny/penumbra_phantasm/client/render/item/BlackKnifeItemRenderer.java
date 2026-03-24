package destiny.penumbra_phantasm.client.render.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.item.BlackKnifeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BlackKnifeItemRenderer extends GeoItemRenderer<BlackKnifeItem> {
    public BlackKnifeItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(PenumbraPhantasm.MODID, "black_knife")));
    }
}
