package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.worldgen.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FeatureRegistry {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNKS = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, PenumbraPhantasm.MODID);
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGES = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, PenumbraPhantasm.MODID);
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, PenumbraPhantasm.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, PenumbraPhantasm.MODID);

    public static final RegistryObject<Feature<ProbabilityFeatureConfiguration>> SCARLET_BUSH = FEATURES.register("scarlet_bush", () -> new ScarletBushFeature(ProbabilityFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GREAT_BOARD_CHESS_RANDOM = FEATURES.register("great_board_chess_random", () -> new GreatBoardChessRandomFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CLIFFS_PATHS = FEATURES.register("cliffs_paths", () -> new CliffsPathFeature(NoneFeatureConfiguration.CODEC));

    public static final ResourceKey<ConfiguredFeature<?, ?>> SCARLET_TREE_GENERATED = registerKey("tree/scarlet_tree_generated");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCARLET_TREE_GROWN = registerKey("tree/scarlet_tree_grown");
    public static final RegistryObject<TrunkPlacerType<ScarletTrunkPlacer>> SCARLET_TRUNK = TRUNKS.register("scarlet_trunk_placer", () -> new TrunkPlacerType<>(ScarletTrunkPlacer.CODEC));
    public static final RegistryObject<FoliagePlacerType<ScarletFoliagePlacer>> SCARLET_FOLIAGE = FOLIAGES.register("scarlet_foliage_placer", () -> new FoliagePlacerType<>(ScarletFoliagePlacer.CODEC));

    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_CANDY_TREE_GENERATED = registerKey("tree/dark_candy_tree_generated");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_CANDY_TREE_GROWN = registerKey("tree/dark_candy_tree_grown");
    public static final RegistryObject<TrunkPlacerType<DarkCandyTrunkPlacer>> DARK_CANDY_TRUNK = TRUNKS.register("dark_candy_trunk_placer", () -> new TrunkPlacerType<>(DarkCandyTrunkPlacer.CODEC));
    public static final RegistryObject<FoliagePlacerType<DarkCandyFoliagePlacer>> DARK_CANDY_FOLIAGE = FOLIAGES.register("dark_candy_foliage_placer", () -> new FoliagePlacerType<>(DarkCandyFoliagePlacer.CODEC));
    public static final RegistryObject<TreeDecoratorType<DarkCandyOnLeavesTreeDecorator>> DARK_CANDY_ON_LEAVES = TREE_DECORATORS.register("dark_candy_on_leaves", () -> new TreeDecoratorType<>(DarkCandyOnLeavesTreeDecorator.CODEC));

    public static final RegistryObject<TreeDecoratorType<FallenLeafTreeDecorator>> FALLEN_LEAF_TREE_DECORATOR = TREE_DECORATORS.register("fallen_leaf_tree_decorator", () -> new TreeDecoratorType<>(FallenLeafTreeDecorator.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> STARTAIL_FEATURE = FEATURES.register("startail_feature", () -> new StartailFeature(NoneFeatureConfiguration.CODEC));

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(PenumbraPhantasm.MODID, name));
    }
}
