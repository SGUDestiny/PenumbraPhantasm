package destiny.penumbra_phantasm.client.render.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.item.FamiliarAxItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class FamiliarAxItemRenderer extends GeoItemRenderer<FamiliarAxItem> {
    public FamiliarAxItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(PenumbraPhantasm.MODID, "familiar_ax")));
    }
}