package destiny.penumbra_phantasm.server.datagen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class BlockFamilyDatagenSets {
    public static final List<BlockFamilyDatagenSet> ALL = List.of(
            new BlockFamilyDatagenSet(
                    "scarlet",
                    modBlock("scarlet_planks"),
                    BlockRegistry.SCARLET_PLANKS,
                    modBlock("scarlet_door_bottom"),
                    modBlock("scarlet_door_top"),
                    modBlock("scarlet_trapdoor"),
                    BlockRegistry.SCARLET_DOOR,
                    BlockRegistry.SCARLET_TRAPDOOR,
                    null,
                    null,
                    null,
                    BlockRegistry.SCARLET_STAIRS,
                    BlockRegistry.SCARLET_SLAB,
                    null,
                    null
            ),
            new BlockFamilyDatagenSet(
                    "dark_candy",
                    modBlock("dark_candy_planks"),
                    BlockRegistry.DARK_CANDY_PLANKS,
                    modBlock("dark_candy_door_bottom"),
                    modBlock("dark_candy_door_top"),
                    modBlock("dark_candy_trapdoor"),
                    BlockRegistry.DARK_CANDY_DOOR,
                    BlockRegistry.DARK_CANDY_TRAPDOOR,
                    null,
                    null,
                    null,
                    BlockRegistry.DARK_CANDY_STAIRS,
                    BlockRegistry.DARK_CANDY_SLAB,
                    null,
                    null
            ),
            new BlockFamilyDatagenSet(
                    "umbrastone",
                    modBlock("umbrastone"),
                    BlockRegistry.UMBRASTONE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    BlockRegistry.UMBRASTONE_WALL,
                    BlockRegistry.UMBRASTONE_STAIRS,
                    BlockRegistry.UMBRASTONE_SLAB,
                    BlockRegistry.UMBRASTONE_BUTTON,
                    BlockRegistry.UMBRASTONE_PRESSURE_PLATE
            ),
            new BlockFamilyDatagenSet(
                    "cobbled_umbrastone",
                    modBlock("cobbled_umbrastone"),
                    BlockRegistry.COBBLED_UMBRASTONE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    BlockRegistry.COBBLED_UMBRASTONE_WALL,
                    BlockRegistry.COBBLED_UMBRASTONE_STAIRS,
                    BlockRegistry.COBBLED_UMBRASTONE_SLAB,
                    BlockRegistry.COBBLED_UMBRASTONE_BUTTON,
                    BlockRegistry.COBBLED_UMBRASTONE_PRESSURE_PLATE
            ),
            new BlockFamilyDatagenSet(
                    "polished_umbrastone",
                    modBlock("polished_umbrastone"),
                    BlockRegistry.POLISHED_UMBRASTONE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    BlockRegistry.POLISHED_UMBRASTONE_WALL,
                    BlockRegistry.POLISHED_UMBRASTONE_STAIRS,
                    BlockRegistry.POLISHED_UMBRASTONE_SLAB,
                    BlockRegistry.POLISHED_UMBRASTONE_BUTTON,
                    BlockRegistry.POLISHED_UMBRASTONE_PRESSURE_PLATE
            ),
            new BlockFamilyDatagenSet(
                    "umbrastone_bricks",
                    modBlock("umbrastone_bricks"),
                    BlockRegistry.UMBRASTONE_BRICKS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    BlockRegistry.UMBRASTONE_BRICK_WALL,
                    BlockRegistry.UMBRASTONE_BRICK_STAIRS,
                    BlockRegistry.UMBRASTONE_BRICK_SLAB,
                    BlockRegistry.UMBRASTONE_BRICK_BUTTON,
                    BlockRegistry.UMBRASTONE_BRICK_PRESSURE_PLATE
            )
    );

    private static ResourceLocation modBlock(String path) {
        return new ResourceLocation(PenumbraPhantasm.MODID, "block/" + path);
    }

    public record BlockFamilyDatagenSet(
            String name,
            ResourceLocation baseTexture,
            RegistryObject<? extends Block> baseBlock,
            ResourceLocation doorBottomTexture,
            ResourceLocation doorTopTexture,
            ResourceLocation trapdoorTexture,
            RegistryObject<? extends Block> door,
            RegistryObject<? extends Block> trapdoor,
            RegistryObject<? extends Block> fenceGate,
            RegistryObject<? extends Block> fence,
            RegistryObject<? extends Block> wall,
            RegistryObject<? extends Block> stairs,
            RegistryObject<? extends Block> slab,
            RegistryObject<? extends Block> button,
            RegistryObject<? extends Block> pressurePlate
    ) {
    }
}
