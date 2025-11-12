package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.DarkFountainBlock;
import destiny.penumbra_phantasm.server.block.DarkFountainOpeningBlock;
import destiny.penumbra_phantasm.server.block.ScarletLeavesBlock;
import destiny.penumbra_phantasm.server.block.ScarletLogBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PenumbraPhantasm.MODID);

    public static final RegistryObject<Block> NIGHT_GRASS_BLOCK = registerBlock("night_grass_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.GRASS)));

    public static final RegistryObject<Block> NIGHT_GRASS = registerBlock("night_grass",
            () -> new GrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.GRASS)));

    public static final RegistryObject<Block> NIGHT_DIRT = registerBlock("night_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.GRAVEL)));

    public static final RegistryObject<Block> UMBRASTONE = registerBlock("umbrastone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_BLUE).sound(SoundType.STONE)));

    public static final RegistryObject<Block> COBBLED_UMBRASTONE = registerBlock("cobbled_umbrastone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE)
                    .mapColor(MapColor.COLOR_BLUE).sound(SoundType.STONE)));

    public static final RegistryObject<Block> SCARLET_LEAVES = registerBlock("scarlet_leaves",
            () -> new ScarletLeavesBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA_LEAVES)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.AZALEA_LEAVES)));

    public static final RegistryObject<Block> SCARLET_LOG = registerBlock("scarlet_log",
            () -> new ScarletLogBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.CHERRY_WOOD)));

    public static final RegistryObject<Block> DARK_FOUNTAIN_OPENING = registerBlock("dark_fountain_opening",
            () -> new DarkFountainOpeningBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noOcclusion().noCollission().lightLevel(state -> (15))));

    public static final RegistryObject<Block> DARK_FOUNTAIN = registerBlock("dark_fountain",
            () -> new DarkFountainBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noOcclusion().noCollission().lightLevel(state -> (15))));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, Supplier<T> block) {
        return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}