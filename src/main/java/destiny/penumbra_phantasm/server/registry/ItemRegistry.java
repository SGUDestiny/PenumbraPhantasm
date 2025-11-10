package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PenumbraPhantasm.MODID);

    public static Item.Properties basicItem() {
        return new Item.Properties().stacksTo(1);
    }

    public static final RegistryObject<Item> BLACK_KNIFE = ITEMS.register("black_knife",
            () -> new Item(basicItem().durability(-1)));
}
