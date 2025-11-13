package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.worldgen.ScarletFoliagePlacer;
import destiny.penumbra_phantasm.server.worldgen.ScarletTrunkPlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class FeatureRegistry {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNKS = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, PenumbraPhantasm.MODID);
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGES = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, PenumbraPhantasm.MODID);

    public static final RegistryObject<TrunkPlacerType<ScarletTrunkPlacer>> SCARLET_TRUNK = TRUNKS.register("scarlet_trunk_placer", () -> new TrunkPlacerType<>(ScarletTrunkPlacer.CODEC));
    public static final RegistryObject<FoliagePlacerType<?>> SCARLET_FOLIAGE = FOLIAGES.register("scarlet_foliage_placer", () -> new FoliagePlacerType<>(ScarletFoliagePlacer.CODEC));

    public static final ResourceKey<ConfiguredFeature<?, ?>> SCARLET_TREE = registerKey("tree/scarlet_tree");

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(PenumbraPhantasm.MODID, name));
    }
}
