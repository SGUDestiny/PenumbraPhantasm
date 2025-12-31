package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.fluid.LuminescentWaterFluidType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

public class FluidTypeRegistry {
    public static final ResourceLocation LUMINESCENT_WATER_STILL = new ResourceLocation(PenumbraPhantasm.MODID, "block/luminescent_water_still");
    public static final ResourceLocation LUMINESCENT_WATER_FLOW = new ResourceLocation(PenumbraPhantasm.MODID, "block/luminescent_water_flow");
    public static final ResourceLocation LUMINESCENT_WATER_OVERLAY = new ResourceLocation(PenumbraPhantasm.MODID, "misc/luminescent_water_overlay");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, PenumbraPhantasm.MODID);

    public static final RegistryObject<FluidType> LUMINESCENT_WATER = registerFluidType("luminescent_water",
            new LuminescentWaterFluidType(LUMINESCENT_WATER_STILL, LUMINESCENT_WATER_FLOW, LUMINESCENT_WATER_OVERLAY, 0xFFFFFFFF,
                    new Vector3f(21f / 255f, 18f / 255f, 38f / 255f),
                    FluidType.Properties.create().lightLevel(15).viscosity(4).density(7).canExtinguish(true)));

    private static RegistryObject<FluidType> registerFluidType(String name, FluidType fluidType) {
        return FLUID_TYPES.register(name, () -> fluidType);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
