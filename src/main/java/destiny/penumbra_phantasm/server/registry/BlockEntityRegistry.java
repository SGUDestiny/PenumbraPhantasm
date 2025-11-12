package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainOpeningBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PenumbraPhantasm.MODID);

    public static final RegistryObject<BlockEntityType<DarkFountainOpeningBlockEntity>> DARK_FOUNTAIN_OPENING = BLOCK_ENTITIES.register("dark_fountain_opening", () -> BlockEntityType.Builder.of(DarkFountainOpeningBlockEntity::new, BlockRegistry.DARK_FOUNTAIN_OPENING.get()).build(null));
}
