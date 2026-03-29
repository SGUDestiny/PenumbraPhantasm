package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.entity.SealingSoulEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PenumbraPhantasm.MODID);

    public static final RegistryObject<EntityType<SealingSoulEntity>> SEALING_SOUL =
            ENTITY_TYPES.register("sealing_soul",
                    () -> EntityType.Builder.of(SealingSoulEntity::new, MobCategory.MISC)
                            .sized(1f, 1f)
                            .build(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "sealing_soul").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
