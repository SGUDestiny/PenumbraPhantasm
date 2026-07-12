package destiny.penumbra_phantasm;

import destiny.penumbra_phantasm.client.ClientConfig;
import destiny.penumbra_phantasm.client.render.blockentity.CheshireChestBlockEntityRenderer;
import destiny.penumbra_phantasm.client.render.blockentity.DarkMarbleDiceBlockEntityRenderer;
import destiny.penumbra_phantasm.client.render.blockentity.DustBlockEntityRenderer;
import destiny.penumbra_phantasm.client.render.blockentity.ScarletMarbleDiceBlockEntityRenderer;
import destiny.penumbra_phantasm.client.render.entity.SealingSoulEntityRenderer;
import destiny.penumbra_phantasm.client.render.model.*;
import destiny.penumbra_phantasm.client.render.model.great_door.GreatDoorBacksideModel;
import destiny.penumbra_phantasm.client.render.model.great_door.GreatDoorClosedModel;
import destiny.penumbra_phantasm.client.render.model.great_door.GreatDoorOpenModel;
import destiny.penumbra_phantasm.client.render.particle.*;
import destiny.penumbra_phantasm.client.render.screen.CheshireChestScreen;
import destiny.penumbra_phantasm.client.render.screen.DarkCandyCraftingTableScreen;
import destiny.penumbra_phantasm.client.render.screen.UmbrastoneFurnaceScreen;
import destiny.penumbra_phantasm.server.datapack.DarkWorldEntityTransforms;
import destiny.penumbra_phantasm.server.datapack.DarkWorldItemTransforms;
import destiny.penumbra_phantasm.server.datapack.DarkWorldType;
import destiny.penumbra_phantasm.server.item.property.RosegoldLighterItemProperty;
import destiny.penumbra_phantasm.server.registry.*;
import destiny.penumbra_phantasm.client.render.model.item.DeltashieldModel;
import destiny.penumbra_phantasm.client.render.dimension.CardKingdomDimensionEffects;
import destiny.penumbra_phantasm.client.render.dimension.DarkWorldDimensionEffects;
import destiny.penumbra_phantasm.client.render.item.DeltaShieldRenderer;
import destiny.penumbra_phantasm.client.render.overlay.DarknessLandOverlay;
import destiny.penumbra_phantasm.client.render.overlay.FountainDarknessOverlay;
import destiny.penumbra_phantasm.client.render.overlay.LocationTitleOverlay;
import destiny.penumbra_phantasm.client.sound.DarkWorldMusicReloadListener;
import destiny.penumbra_phantasm.server.event.CommonEvents;
import destiny.penumbra_phantasm.server.item.MusicMediumItem;
import destiny.penumbra_phantasm.server.item.property.FriendItemProperty;
import destiny.penumbra_phantasm.server.item.property.SoulHearthItemProperty;
import destiny.penumbra_phantasm.server.transformations.inventory.StorageManager;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
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
import com.mojang.logging.LogUtils;

import static destiny.penumbra_phantasm.server.item.SoulHearthItem.SOUL_TYPE;

@Mod(PenumbraPhantasm.MODID)
public class PenumbraPhantasm {
    public static final String MODID = "penumbra_phantasm";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation EMPTY_LOCATION = new ResourceLocation(MODID, "empty");
    public static final String EMPTY = EMPTY_LOCATION.toString();

    public PenumbraPhantasm() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

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
        FeatureRegistry.TREE_DECORATORS.register(modEventBus);
        FeatureRegistry.FEATURES.register(modEventBus);
        MenuRegistry.MENUS.register(modEventBus);
        ChunkGeneratorRegistry.CHUNK_GENERATORS.register(modEventBus);
        PacketHandlerRegistry.register();
        AdvancementRegistry.register();

        modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) ->
            {
                event.dataPackRegistry(DarkWorldType.REGISTRY_KEY, DarkWorldType.CODEC, null);
                event.dataPackRegistry(DarkWorldItemTransforms.REGISTRY_KEY, DarkWorldItemTransforms.CODEC, null);
                event.dataPackRegistry(DarkWorldEntityTransforms.REGISTRY_KEY, DarkWorldEntityTransforms.CODEC, null);
            });
        modEventBus.addListener(PenumbraPhantasm::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(StorageManager::init);
    }

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
            event.registerLayerDefinition(DarkFountainMiddleOptimizedModel.LAYER_LOCATION, DarkFountainMiddleOptimizedModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainFrontModel.LAYER_LOCATION, DarkFountainFrontModel::createBodyLayer);
            event.registerLayerDefinition(DarkFountainVortexModel.LAYER_LOCATION, DarkFountainVortexModel::createBodyLayer);
            event.registerLayerDefinition(DeltashieldModel.LAYER_LOCATION, DeltashieldModel::createBodyLayer);
            event.registerLayerDefinition(GreatDoorClosedModel.LAYER_LOCATION, GreatDoorClosedModel::createBodyLayer);
            event.registerLayerDefinition(GreatDoorOpenModel.LAYER_LOCATION, GreatDoorOpenModel::createBodyLayer);
            event.registerLayerDefinition(GreatDoorBacksideModel.LAYER_LOCATION, GreatDoorBacksideModel::createBodyLayer);
            event.registerLayerDefinition(CheshireChestModel.LAYER_LOCATION, CheshireChestModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(DeltaShieldRenderer.INSTANCE);
            event.registerReloadListener(DarkWorldMusicReloadListener.INSTANCE);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("fountain_darkness", FountainDarknessOverlay.OVERLAY);
            event.registerAboveAll("darkness_land", DarknessLandOverlay.OVERLAY);
            event.registerAboveAll("location_title", LocationTitleOverlay.OVERLAY);
        }

        @SubscribeEvent
        public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
            DarkWorldDimensionEffects darkWorldDimensionEffects = new DarkWorldDimensionEffects();
            CardKingdomDimensionEffects cardKingdomDimensionEffects = new CardKingdomDimensionEffects();

            event.register(DarkWorldDimensionEffects.DARK_WORLD_DIMENSION_EFFECTS, darkWorldDimensionEffects);
            event.register(CardKingdomDimensionEffects.CARD_KINGDOM_DIMENSION_EFFECTS, cardKingdomDimensionEffects);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(BlockRegistry.SCARLET_DOOR.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(BlockRegistry.SCARLET_TRAPDOOR.get(), RenderType.cutout());

                EntityRenderers.register(EntityRegistry.SEALING_SOUL.get(), SealingSoulEntityRenderer::new);

                ItemProperties.register(ItemRegistry.FRIEND.get(), new ResourceLocation(MODID, "animation"), new FriendItemProperty());
                ItemProperties.register(ItemRegistry.SOUL_HEARTH.get(), new ResourceLocation(MODID, SOUL_TYPE), new SoulHearthItemProperty());
                ItemProperties.register(ItemRegistry.HEARTH_SOUL.get(), new ResourceLocation(MODID, SOUL_TYPE), new SoulHearthItemProperty());
                ItemProperties.register(ItemRegistry.DELTA_SHIELD.get(), new ResourceLocation("blocking"), (stack, level, entity, duration) -> {
                    return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1F : 0F;
                });
                ItemProperties.register(ItemRegistry.ROSEGOLD_LIGHTER.get(), new ResourceLocation(MODID, "open"), new RosegoldLighterItemProperty());

                MenuScreens.register(MenuRegistry.DARK_CANDY_CRAFTING_TABLE.get(), DarkCandyCraftingTableScreen::new);
                MenuScreens.register(MenuRegistry.UMBRASTONE_FURNACE_MENU.get(), UmbrastoneFurnaceScreen::new);
                MenuScreens.register(MenuRegistry.CHESHIRE_CHEST_MENU.get(), CheshireChestScreen::new);
            });
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockRegistry.SCARLET_SAPLING.getId(), BlockRegistry.POTTED_SCARLET_SAPLING);
                ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockRegistry.DARK_CANDY_SAPLING.getId(), BlockRegistry.POTTED_DARK_CANDY_SAPLING);
                ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(BlockRegistry.SCARLET_ROSE.getId(), BlockRegistry.POTTED_SCARLET_ROSE);
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
            event.registerSpriteSet(ParticleTypeRegistry.ICHOR_FIRE_FLAME.get(), FlameParticle.Provider::new);
            event.registerSpriteSet(ParticleTypeRegistry.FRIEND_DISAPPEAR.get(), FriendDisappearParticle.Provider::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockEntityRegistry.DARK_MARBLE_DICE_BLOCK_ENTITY.get(), DarkMarbleDiceBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(BlockEntityRegistry.SCARLET_MARBLE_DICE_BLOCK_ENTITY.get(), ScarletMarbleDiceBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(BlockEntityRegistry.DUST_BLOCK_ENTITY.get(), DustBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(BlockEntityRegistry.CHESHIRE_CHEST_BLOCK_ENTITY.get(), CheshireChestBlockEntityRenderer::new);
        }
    }
}
