package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.item.*;
import net.minecraft.core.Direction;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PenumbraPhantasm.MODID);

    public static Item.Properties basicItem() {
        return new Item.Properties().stacksTo(1);
    }

    public static final RegistryObject<Item> GOLD_KNIFE = ITEMS.register("gold_knife",
            () -> new KnifeItem(Tiers.GOLD, 2, -2, true, basicItem()));
    public static final RegistryObject<Item> IRON_KNIFE = ITEMS.register("iron_knife",
            () -> new KnifeItem(Tiers.IRON, 3, -2, true, basicItem()));
    public static final RegistryObject<Item> DIAMOND_KNIFE = ITEMS.register("diamond_knife",
            () -> new KnifeItem(Tiers.DIAMOND, 4, -2, true, basicItem()));
    public static final RegistryObject<Item> NETHERITE_KNIFE = ITEMS.register("netherite_knife",
            () -> new NetheriteKnifeItem(Tiers.NETHERITE, 5, -2, true, basicItem()));
    public static final RegistryObject<Item> REAL_KNIFE = ITEMS.register("real_knife",
            () -> new RealKnifeItem(Tiers.NETHERITE, 4,8, true, basicItem()));
    public static final RegistryObject<Item> BLACK_KNIFE = ITEMS.register("black_knife",
            () -> new BlackKnifeItem(Tiers.NETHERITE, 11, -2, false, basicItem()));

    public static final RegistryObject<Item> FAMILIAR_SWORD = ITEMS.register("familiar_sword",
            () -> new FamiliarSwordItem(Tiers.NETHERITE, 3, -2, basicItem()));
    public static final RegistryObject<Item> FAMILIAR_AX = ITEMS.register("familiar_ax",
            () -> new FamiliarAxItem(Tiers.NETHERITE, 6, -3, basicItem()));

    public static final RegistryObject<Item> DELTA_SHIELD = ITEMS.register("delta_shield",
            () -> new DeltaShieldItem(basicItem()));

    public static final RegistryObject<Item> SHADOW_CRYSTAL = ITEMS.register("shadow_crystal",
            () -> new Item(basicItem()));
    public static final RegistryObject<Item> BLACK_SHARD = ITEMS.register("black_shard",
            () -> new SwordItem(Tiers.NETHERITE, 5, -2, basicItem()));

    public static final RegistryObject<Item> DARK_CANDY = ITEMS.register("dark_candy",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(0.5f).fast().alwaysEat().build())));

    public static final RegistryObject<Item> FRIEND = ITEMS.register("friend",
            () -> new FriendItem(basicItem()));

    public static final RegistryObject<Item> SOUL_HEARTH = ITEMS.register("soul_hearth",
            () -> new SoulHearthItem(basicItem()));

    public static final RegistryObject<Item> ITEM_MUSIC_MEDIUM_THE_HOLY = ITEMS.register("item_music_medium_the_holy",
            () -> new MusicMediumItem(6, SoundRegistry.FOUNTAIN_MUSIC_DISC, basicItem(), (int) (53.333 * 20)));

    public static final RegistryObject<Item> DARK_CANDY_STICK = ITEMS.register("dark_candy_stick",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IVORY = ITEMS.register("ivory",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_ROSEGOLD = ITEMS.register("raw_rosegold",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROSEGOLD_INGOT = ITEMS.register("rosegold_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DARk_CANDY_SWORD = ITEMS.register("dark_candy_sword",
            () -> new SwordItem(Tiers.WOOD, 4, -2, new Item.Properties()));
    public static final RegistryObject<Item> DARk_CANDY_AXE = ITEMS.register("dark_candy_axe",
            () -> new AxeItem(Tiers.WOOD, 7, -2.5f, new Item.Properties()));
    public static final RegistryObject<Item> DARk_CANDY_PICKAXE = ITEMS.register("dark_candy_pickaxe",
            () -> new PickaxeItem(Tiers.WOOD, 0, -2, new Item.Properties()));
    public static final RegistryObject<Item> DARk_CANDY_SHOVEL = ITEMS.register("dark_candy_shovel",
            () -> new ShovelItem(Tiers.WOOD, 0, -1.5f, new Item.Properties()));
    public static final RegistryObject<Item> DARk_CANDY_HOE = ITEMS.register("dark_candy_hoe",
            () -> new HoeItem(Tiers.WOOD, 0, 0.5f, new Item.Properties()));

    public static final RegistryObject<Item> UMBRASTONE_SWORD = ITEMS.register("umbrastone_sword",
            () -> new SwordItem(Tiers.STONE, 4, -2, new Item.Properties()));
    public static final RegistryObject<Item> UMBRASTONE_AXE = ITEMS.register("umbrastone_axe",
            () -> new AxeItem(Tiers.STONE, 7, -2.5f, new Item.Properties()));
    public static final RegistryObject<Item> UMBRASTONE_PICKAXE = ITEMS.register("umbrastone_pickaxe",
            () -> new PickaxeItem(Tiers.STONE, 2, -2, new Item.Properties()));
    public static final RegistryObject<Item> UMBRASTONE_SHOVEL = ITEMS.register("umbrastone_shovel",
            () -> new ShovelItem(Tiers.STONE, 1, -1.5f, new Item.Properties()));
    public static final RegistryObject<Item> UMBRASTONE_HOE = ITEMS.register("umbrastone_hoe",
            () -> new HoeItem(Tiers.STONE, 0, 0.5f, new Item.Properties()));

    public static final RegistryObject<Item> IVORY_SWORD = ITEMS.register("ivory_sword",
            () -> new SwordItem(Tiers.IRON, 4, -2, new Item.Properties()));
    public static final RegistryObject<Item> IVORY_AXE = ITEMS.register("ivory_axe",
            () -> new AxeItem(Tiers.IRON, 6, -2.5f, new Item.Properties()));
    public static final RegistryObject<Item> IVORY_PICKAXE = ITEMS.register("ivory_pickaxe",
            () -> new PickaxeItem(Tiers.IRON, 2, -2, new Item.Properties()));
    public static final RegistryObject<Item> IVORY_SHOVEL = ITEMS.register("ivory_shovel",
            () -> new ShovelItem(Tiers.IRON, 1, -1.5f, new Item.Properties()));
    public static final RegistryObject<Item> IVORY_HOE = ITEMS.register("ivory_hoe",
            () -> new HoeItem(Tiers.IRON, 0, 0.5f, new Item.Properties()));


    public static final RegistryObject<Item> ROSEGOLD_SWORD = ITEMS.register("rosegold_sword",
            () -> new SwordItem(Tiers.DIAMOND, 4, -2, new Item.Properties()));
    public static final RegistryObject<Item> ROSEGOLD_AXE = ITEMS.register("rosegold_axe",
            () -> new AxeItem(Tiers.DIAMOND, 7, -2.5f, new Item.Properties()));
    public static final RegistryObject<Item> ROSEGOLD_PICKAXE = ITEMS.register("rosegold_pickaxe",
            () -> new PickaxeItem(Tiers.DIAMOND, 2, -2, new Item.Properties()));
    public static final RegistryObject<Item> ROSEGOLD_SHOVEL = ITEMS.register("rosegold_shovel",
            () -> new ShovelItem(Tiers.DIAMOND, 1, -1.5f, new Item.Properties()));
    public static final RegistryObject<Item> ROSEGOLD_HOE = ITEMS.register("rosegold_hoe",
            () -> new HoeItem(Tiers.DIAMOND, 0, 0.5f, new Item.Properties()));

    public static final RegistryObject<Item> ROSEGOLD_LIGHTER = ITEMS.register("rosegold_lighter",
            () -> new RosegoldLighterItem(new Item.Properties().stacksTo(1).durability(-1)));

    public static final RegistryObject<Item> DARK_CANDY_BUCKET = ITEMS.register("dark_candy_bucket",
            () -> new ScarletBucketItem(Fluids.EMPTY, new Item.Properties()));
    public static final RegistryObject<Item> LUMINESCENT_WATER_BUCKET = ITEMS.register("luminescent_water_bucket",
            () -> new ScarletBucketItem(FluidRegistry.SOURCE_LUMINESCENT_WATER.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PURE_DARKNESS_BUCKET = ITEMS.register("pure_darkness_bucket",
            () -> new ScarletBucketItem(FluidRegistry.SOURCE_PURE_DARKNESS.get(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FALLEN_DARK_CANDY_LEAVES = ITEMS.register("fallen_dark_candy_leaves",
            () -> new ScalableHorizontalPlaneBlockItem(BlockRegistry.FALLEN_DARK_CANDY_LEAVES.get(), new Item.Properties()));
    public static final RegistryObject<Item> FALLEN_SCARLET_LEAVES = ITEMS.register("fallen_scarlet_leaves",
            () -> new ScalableHorizontalPlaneBlockItem(BlockRegistry.FALLEN_SCARLET_LEAVES.get(), new Item.Properties()));
    public static final RegistryObject<Item> ICHOR = ITEMS.register("ichor",
            () -> new IchorItem(BlockRegistry.ICHOR_PUDDLE.get(), new Item.Properties(), 2400));
    public static final RegistryObject<Item> ICHOR_TORCH = ITEMS.register("ichor_torch",
            () -> new StandingAndWallBlockItem(BlockRegistry.ICHOR_TORCH.get(), BlockRegistry.ICHOR_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<Item> ICHOR_CANDLE = ITEMS.register("ichor_candle",
            () -> new IchorCandleBlockItem(BlockRegistry.ICHOR_CANDLE.get(), new Item.Properties()));
}
