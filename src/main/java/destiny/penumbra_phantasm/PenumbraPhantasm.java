package destiny.penumbra_phantasm;

import com.mojang.logging.LogUtils;
import destiny.penumbra_phantasm.client.render.particles.CrimsonLeafParticle;
import destiny.penumbra_phantasm.server.event.CommonEvents;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PenumbraPhantasm.MODID)
public class PenumbraPhantasm
{
    public static final String MODID = "penumbra_phantasm";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PenumbraPhantasm()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(new CommonEvents());

        SoundRegistry.SOUNDS.register(modEventBus);
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        CreativeTabRegistry.DEF_REG.register(modEventBus);
        BlockEntityRegistry.BLOCK_ENTITIES.register(modEventBus);
        EffectRegistry.DEF_REG.register(modEventBus);
        ParticleTypeRegistry.PARTICLE_TYPES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }

        @SubscribeEvent
        public static void registerParticleProvider(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ParticleTypeRegistry.CRIMSON_LEAF_PARTICLE_TYPE.get(), CrimsonLeafParticle.Provider::new);
        }
    }
}
