package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PenumbraPhantasm.MODID);

    public static Item.Properties basicItem() {
        return new Item.Properties().stacksTo(1);
    }

    public static final RegistryObject<Item> GOLD_KNIFE = ITEMS.register("gold_knife",
            () -> new KnifeItem(Tiers.GOLD, 2, -2, true, true, basicItem()));
    public static final RegistryObject<Item> IRON_KNIFE = ITEMS.register("iron_knife",
            () -> new KnifeItem(Tiers.IRON, 3, -2, true, true, basicItem()));
    public static final RegistryObject<Item> DIAMOND_KNIFE = ITEMS.register("diamond_knife",
            () -> new KnifeItem(Tiers.DIAMOND, 4, -2, true, true, basicItem()));
    public static final RegistryObject<Item> NETHERITE_KNIFE = ITEMS.register("netherite_knife",
            () -> new NetheriteKnifeItem(Tiers.NETHERITE, 5, -2, true, true, basicItem()));
    public static final RegistryObject<Item> REAL_KNIFE = ITEMS.register("real_knife",
            () -> new RealKnifeItem(Tiers.NETHERITE, 4,-2, true, true, basicItem()));
    public static final RegistryObject<Item> BLACK_KNIFE = ITEMS.register("black_knife",
            () -> new KnifeItem(Tiers.NETHERITE, 0, -2, false, false, basicItem()));

    public static final RegistryObject<Item> FAMILIAR_SWORD = ITEMS.register("familiar_sword",
            () -> new SwordItem(Tiers.NETHERITE, 9, -3, basicItem()));

    public static final RegistryObject<Item> DELTASHIELD = ITEMS.register("deltashield",
            () -> new DeltashieldItem(basicItem()));

    public static final RegistryObject<Item> SHADOW_CRYSTAL = ITEMS.register("shadow_crystal",
            () -> new Item(basicItem()));

    public static final RegistryObject<Item> DARK_CANDY = ITEMS.register("dark_candy",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationMod(8).fast().alwaysEat().build())));

    public static final RegistryObject<Item> FRIEND = ITEMS.register("friend",
            () -> new FriendItem(basicItem()));

    public static final RegistryObject<Item> SOUL_HEARTH = ITEMS.register("soul_hearth",
            () -> new SoulHearthItem(basicItem()));
}
