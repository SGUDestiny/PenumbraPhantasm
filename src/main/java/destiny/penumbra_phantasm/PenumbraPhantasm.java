package destiny.penumbra_phantasm;

import com.mojang.logging.LogUtils;
import destiny.penumbra_phantasm.client.dimension.DarkDepthsDimensionEffects;
import destiny.penumbra_phantasm.client.models.item.DeltashieldModel;
import destiny.penumbra_phantasm.client.render.screen.FountainDarknessOverlay;
import destiny.penumbra_phantasm.client.render.item.DeltashieldRenderer;
import destiny.penumbra_phantasm.client.render.model.*;
import destiny.penumbra_phantasm.client.render.particles.*;
import destiny.penumbra_phantasm.server.event.CommonEvents;
import destiny.penumbra_phantasm.server.item.MusicMediumItem;
import destiny.penumbra_phantasm.server.item.property.FriendItemProperty;
import destiny.penumbra_phantasm.server.item.property.SoulHearthItemProperty;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static destiny.penumbra_phantasm.server.item.SoulHearthItem.SOUL_TYPE;

@Mod(PenumbraPhantasm.MODID)
public class PenumbraPhantasm {
    public static final String MODID = "penumbra_phantasm";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation EMPTY_LOCATION = new ResourceLocation(MODID, "empty");
    public static final String EMPTY = EMPTY_LOCATION.toString();

    public PenumbraPhantasm() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(new CommonEvents());

        SoundRegistry.SOUNDS.register(modEventBus);
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        FluidRegistry.FLUIDS.register(modEventBus);
        FluidTypeRegistry.FLUID_TYPES.register(modEventBus);
        CreativeTabRegistry.DEF_REG.register(modEventBus);
        BlockEntityRegistry.BLOCK_ENTITIES.register(modEventBus);
        EffectRegistry.DEF_REG.register(modEventBus);
        ParticleTypeRegistry.PARTICLE_TYPES.register(modEventBus);
        FeatureRegistry.FOLIAGES.register(modEventBus);
        FeatureRegistry.TRUNKS.register(modEventBus);
        PacketHandlerRegistry.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, colorIn) -> colorIn != 0 ? -1 : MusicMediumItem.getColor(), ItemRegistry.ITEM_MUSIC_MEDIUM_THE_HOLY.get());
        }

        @SubscribeEvent
        public static void bakeModels(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(DarkFountainOpeningModel.LAYER_LOCATION, DarkFountainOpeningModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainGroundCrackModel.LAYER_LOCATION, DarkFountainGroundCrackModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainBackModel.LAYER_LOCATION, DarkFountainBackModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainMiddleModel.LAYER_LOCATION, DarkFountainMiddleModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainFrontModel.LAYER_LOCATION, DarkFountainFrontModel::createBodyLayer);
            event.registerLayerDefinition(DeltashieldModel.LAYER_LOCATION, DeltashieldModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(DeltashieldRenderer.INSTANCE);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("fountain_darkness", FountainDarknessOverlay.OVERLAY);
        }

        @SubscribeEvent
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            DarkDepthsDimensionEffects dark_depths = new DarkDepthsDimensionEffects();

            event.register(DarkDepthsDimensionEffects.DARK_DEPTHS_EFFECT, dark_depths);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(ItemRegistry.FRIEND.get(), new ResourceLocation(MODID, "animation"), new FriendItemProperty());
                ItemProperties.register(ItemRegistry.SOUL_HEARTH.get(), new ResourceLocation(MODID, SOUL_TYPE), new SoulHearthItemProperty());
                ItemProperties.register(ItemRegistry.DELTASHIELD.get(), new ResourceLocation("blocking"), (stack, level, entity, duration) -> {
                    return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1F : 0F;
                });
            });
        }

        @SubscribeEvent
        public static void registerParticleProvider(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ParticleTypeRegistry.SCARLET_LEAF.get(), ScarletLeafParticle.Provider::new);
            event.registerSpriteSet(ParticleTypeRegistry.FOUNTAIN_TARGET.get(), FountainTargetParticle.Provider::new);
            event.registerSpriteSet(ParticleTypeRegistry.REAL_KNIFE_SLASH.get(), RealKnifeSlashParticle.Provider::new);
            event.registerSpriteSet(ParticleTypeRegistry.REAL_KNIFE_HIT.get(), RealKnifeHitParticle.Provider::new);
            event.registerSpriteSet(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), FountainDarknessParticle.Provider::new);
            event.registerSpriteSet(ParticleTypeRegistry.LUMINESCENT_PARTICLE.get(), LuminescentParticle.Provider::new);
        }
    }
}
