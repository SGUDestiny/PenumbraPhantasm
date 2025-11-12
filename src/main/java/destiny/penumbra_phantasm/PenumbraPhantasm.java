package destiny.penumbra_phantasm;

import com.mojang.logging.LogUtils;
import destiny.penumbra_phantasm.client.dimension.DarkDepthsDimensionEffects;
import destiny.penumbra_phantasm.client.render.blockentity.DarkFountainBlockEntityRenderer;
import destiny.penumbra_phantasm.client.render.blockentity.DarkFountainOpeningBlockEntityRenderer;
import destiny.penumbra_phantasm.client.render.model.DarkFountainEdgesModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainGroundCrackModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainModel;
import destiny.penumbra_phantasm.client.render.model.DarkFountainOpeningModel;
import destiny.penumbra_phantasm.client.render.particles.FountainTargetParticle;
import destiny.penumbra_phantasm.client.render.particles.ScarletLeafParticle;
import destiny.penumbra_phantasm.server.event.CommonEvents;
import destiny.penumbra_phantasm.server.item.property.FriendItemProperty;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
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
        public static DarkFountainOpeningModel fountainOpeningModel;
        public static DarkFountainGroundCrackModel fountainGroundCrackModel;
        public static DarkFountainModel fountainModel;
        public static DarkFountainEdgesModel fountainEdgesModel;

        @SubscribeEvent
        public static void bakeModels(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(DarkFountainOpeningModel.LAYER_LOCATION, DarkFountainOpeningModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainGroundCrackModel.LAYER_LOCATION, DarkFountainGroundCrackModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainModel.LAYER_LOCATION, DarkFountainModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainEdgesModel.LAYER_LOCATION, DarkFountainEdgesModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerBlockEntityRenderer(BlockEntityRegistry.DARK_FOUNTAIN_OPENING.get(),
                    context -> {fountainOpeningModel = new DarkFountainOpeningModel(context.bakeLayer(DarkFountainOpeningModel.LAYER_LOCATION)); fountainGroundCrackModel = new DarkFountainGroundCrackModel(context.bakeLayer(DarkFountainGroundCrackModel.LAYER_LOCATION));
                        return new DarkFountainOpeningBlockEntityRenderer(fountainOpeningModel, fountainGroundCrackModel);
                    });
            event.registerBlockEntityRenderer(BlockEntityRegistry.DARK_FOUNTAIN.get(),
                    context -> {fountainModel = new DarkFountainModel(context.bakeLayer(DarkFountainModel.LAYER_LOCATION)); fountainEdgesModel = new DarkFountainEdgesModel(context.bakeLayer(DarkFountainEdgesModel.LAYER_LOCATION)); fountainGroundCrackModel = new DarkFountainGroundCrackModel(context.bakeLayer(DarkFountainGroundCrackModel.LAYER_LOCATION));
                        return new DarkFountainBlockEntityRenderer(fountainModel, fountainEdgesModel, fountainGroundCrackModel);
                    });
        }

        @SubscribeEvent
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            DarkDepthsDimensionEffects dark_depths = new DarkDepthsDimensionEffects();

            event.register(DarkDepthsDimensionEffects.DARK_DEPTHS_EFFECT, dark_depths);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                ItemProperties.register(ItemRegistry.FRIEND.get(), new ResourceLocation(MODID, "animation"), new FriendItemProperty());
            });
        }

        @SubscribeEvent
        public static void registerParticleProvider(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ParticleTypeRegistry.SCARLET_LEAF.get(), ScarletLeafParticle.Provider::new);
            event.registerSpriteSet(ParticleTypeRegistry.FOUNTAIN_TARGET.get(), FountainTargetParticle.Provider::new);
        }
    }
}
