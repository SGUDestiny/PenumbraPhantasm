package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.*;
import destiny.penumbra_phantasm.server.worldgen.ScarletGrower;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PenumbraPhantasm.MODID);

    public static final RegistryObject<Block> NIGHT_GRASS_BLOCK = registerBlock("night_grass_block",
            () -> new NightGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.GRASS).randomTicks()));

    public static final RegistryObject<Block> NIGHT_GRASS = registerBlock("night_grass",
            () -> new TallGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
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
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.CHERRY_WOOD)));

    public static final RegistryObject<Block> SCARLET_SAPLING = registerBlock("scarlet_sapling",
            () -> new SaplingBlock(new ScarletGrower(), BlockBehaviour.Properties.copy(Blocks.CHERRY_SAPLING)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CHERRY_SAPLING)));

    public static final RegistryObject<Block> DARK_CANDY_BLOCK = registerBlock("dark_candy_block",
            () -> new DarkCandyBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA_LEAVES)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.AZALEA_LEAVES).noCollission().noOcclusion().instabreak().randomTicks()));

    public static final RegistryObject<Block> DARK_CANDY_LEAVES = registerBlock("dark_candy_leaves",
            () -> new DarkCandyLeaves(BlockBehaviour.Properties.copy(Blocks.AZALEA_LEAVES)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.AZALEA_LEAVES).randomTicks()));

    public static final RegistryObject<Block> DARK_CANDY_LOG = registerBlock("dark_candy_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.CHERRY_WOOD)));

    public static final RegistryObject<Block> SCARLET_MARBLE = registerBlock("scarlet_marble",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> DARK_MARBLE = registerBlock("dark_marble",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> SCARLET_MARBLE_PAWN = registerBlock("scarlet_marble_pawn",
            () -> new ChessPawnBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> SCARLET_MARBLE_ROOK = registerBlock("scarlet_marble_rook",
            () -> new ChessRookBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> SCARLET_MARBLE_KNIGHT = registerBlock("scarlet_marble_knight",
            () -> new ChessKnightBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> SCARLET_MARBLE_BISHOP = registerBlock("scarlet_marble_bishop",
            () -> new ChessBishopBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> SCARLET_MARBLE_QUEEN = registerBlock("scarlet_marble_queen",
            () -> new ChessQueenBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> SCARLET_MARBLE_KING = registerBlock("scarlet_marble_king",
            () -> new ChessKingBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> DARK_MARBLE_PAWN = registerBlock("dark_marble_pawn",
            () -> new ChessPawnBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> DARK_MARBLE_ROOK = registerBlock("dark_marble_rook",
            () -> new ChessRookBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> DARK_MARBLE_KNIGHT = registerBlock("dark_marble_knight",
            () -> new ChessKnightBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> DARK_MARBLE_BISHOP = registerBlock("dark_marble_bishop",
            () -> new ChessBishopBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> DARK_MARBLE_QUEEN = registerBlock("dark_marble_queen",
            () -> new ChessQueenBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<Block> DARK_MARBLE_KING = registerBlock("dark_marble_king",
            () -> new ChessKingBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.CALCITE).noOcclusion()));

    public static final RegistryObject<LiquidBlock> LUMINESCENT_WATER = BLOCKS.register("luminescent_water",
            () -> new LuminescentWaterFluidBlock(FluidRegistry.SOURCE_LUMINESCENT_WATER, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable().lightLevel(state -> 15).randomTicks()));
    public static final RegistryObject<LiquidBlock> PURE_DARKNESS = BLOCKS.register("pure_darkness",
            () -> new LiquidBlock(FluidRegistry.SOURCE_PURE_DARKNESS, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable().lightLevel(state -> 15)));

    public static final RegistryObject<Block> HEARTH = registerBlock("hearth",
            () -> new HearthBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN)
                    .mapColor(MapColor.COLOR_GRAY).sound(SoundType.LANTERN).noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, Supplier<T> block) {
        return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}