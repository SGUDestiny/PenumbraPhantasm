package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FluidRegistry {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, PenumbraPhantasm.MODID);

    public static final RegistryObject<FlowingFluid> SOURCE_LUMINESCENT_WATER = FLUIDS.register("luminescent_water",
            () -> new ForgeFlowingFluid.Source(FluidRegistry.LUMINESCENT_WATER_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_LUMINESCENT_WATER = FLUIDS.register("flowing_luminescent_water",
            () -> new ForgeFlowingFluid.Flowing(FluidRegistry.LUMINESCENT_WATER_PROPERTIES));


    public static final ForgeFlowingFluid.Properties LUMINESCENT_WATER_PROPERTIES = new ForgeFlowingFluid.Properties(
            FluidTypeRegistry.LUMINESCENT_WATER, SOURCE_LUMINESCENT_WATER, FLOWING_LUMINESCENT_WATER)
            .slopeFindDistance(2).levelDecreasePerBlock(1).block(BlockRegistry.LUMINESCENT_WATER);
            //.bucket(ItemInit.CELESTIAL_OIL_BUCKET);


    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
