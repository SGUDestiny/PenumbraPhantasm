package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.GreatDoorShapeBlock;
import destiny.penumbra_phantasm.server.block.entity.DarknessBlockEntity;
import destiny.penumbra_phantasm.server.block.entity.GreatDoorShapeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PenumbraPhantasm.MODID);

    public static final RegistryObject<BlockEntityType<DarknessBlockEntity>> DARKNESS_BLOCK_ENTITY = BLOCK_ENTITIES.register("darkness", () -> BlockEntityType.Builder.of(DarknessBlockEntity::new, BlockRegistry.DARKNESS.get()).build(null));
    public static final RegistryObject<BlockEntityType<GreatDoorShapeBlockEntity>> GREAT_DOOR_SHAPE_BLOCK_ENTITY = BLOCK_ENTITIES.register("great_door_shape", () -> BlockEntityType.Builder.of(GreatDoorShapeBlockEntity::new, BlockRegistry.GREAT_DOOR_SHAPE.get()).build(null));
}
