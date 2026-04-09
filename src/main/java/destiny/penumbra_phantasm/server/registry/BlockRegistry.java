package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.*;
import destiny.penumbra_phantasm.server.worldgen.DarkCandyGrower;
import destiny.penumbra_phantasm.server.worldgen.ScarletGrower;
import destiny.penumbra_phantasm.server.block.TenebralithSpikeBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PenumbraPhantasm.MODID);

    public static final BlockBehaviour.Properties SCARLET_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.CHERRY_PLANKS).mapColor(MapColor.COLOR_PINK);
    public static final BlockBehaviour.Properties DARK_CANDY_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.CHERRY_PLANKS).mapColor(MapColor.COLOR_RED);
    public static final BlockBehaviour.Properties SCARLET_MARBLE_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops();
    public static final BlockBehaviour.Properties DARK_MARBLE_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops();
    public static final BlockBehaviour.Properties UMBRASTONE_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops();
    public static final BlockBehaviour.Properties CLIFFROCK_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().sound(SoundTypeRegistry.CLIFF);
    public static final BlockBehaviour.Properties TENEBRALITH_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE);
    public static final BlockBehaviour.Properties ROSEGOLD_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().sound(SoundType.METAL);

    public static final BlockSetType SCARLET_MARBLE_BLOCKSET = new BlockSetType("scarlet_marble", true, SoundType.CALCITE, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
    public static final BlockSetType DARK_MARBLE_BLOCKSET = new BlockSetType("dark_marble", true, SoundType.CALCITE, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
    public static final BlockSetType UMBRASTONE_BLOCKSET = new BlockSetType("umbrastone", true, SoundType.CALCITE, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
    public static final BlockSetType TENEBRALITH_BLOCKSET = new BlockSetType("tenebralith", true, SoundType.DEEPSLATE, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);
    public static final BlockSetType ROSEGOLD_BLOCKSET = new BlockSetType("rosegold", true, SoundType.METAL, SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON);


    public static final RegistryObject<Block> TWILIGHT_GRASS_BLOCK = registerBlock("twilight_grass_block",
            () -> new TwilightGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.GRASS).randomTicks()));
    public static final RegistryObject<Block> TWILIGHT_GRASS = registerBlock("twilight_grass",
            () -> new TallGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.GRASS)));

    public static final RegistryObject<Block> NIGHT_GRASS_BLOCK = registerBlock("night_grass_block",
            () -> new NightGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.GRASS).randomTicks()));
    public static final RegistryObject<Block> NIGHT_GRASS = registerBlock("night_grass",
            () -> new TallGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> NIGHT_DIRT = registerBlock("night_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.GRAVEL)));



    //Umbrastone misc
    public static final RegistryObject<Block> UMBRASTONE_PILLAR = registerBlock("umbrastone_pillar", () -> new RotatedPillarBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> CHISELED_UMBRASTONE = registerBlock("chiseled_umbrastone", () -> new Block(UMBRASTONE_PROPERTIES));

    //Polished umbrastone
    public static final RegistryObject<Block> POLISHED_UMBRASTONE = registerBlock("polished_umbrastone", () -> new Block(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_UMBRASTONE_STAIRS = registerBlock("polished_umbrastone_stairs", () -> new StairBlock(BlockRegistry.POLISHED_UMBRASTONE.get().defaultBlockState(), UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_UMBRASTONE_SLAB = registerBlock("polished_umbrastone_slab", () -> new SlabBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_UMBRASTONE_WALL = registerBlock("polished_umbrastone_wall", () -> new WallBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_UMBRASTONE_BUTTON = registerBlock("polished_umbrastone_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> POLISHED_UMBRASTONE_PRESSURE_PLATE = registerBlock("polished_umbrastone_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET));

    //Umbrastone bricks
    public static final RegistryObject<Block> UMBRASTONE_BRICKS = registerBlock("umbrastone_bricks", () -> new Block(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_BRICK_STAIRS = registerBlock("umbrastone_brick_stairs", () -> new StairBlock(BlockRegistry.UMBRASTONE_BRICKS.get().defaultBlockState(), UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_BRICK_SLAB = registerBlock("umbrastone_brick_slab", () -> new SlabBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_BRICK_WALL = registerBlock("umbrastone_brick_wall", () -> new WallBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_BRICK_BUTTON = registerBlock("umbrastone_brick_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> UMBRASTONE_BRICK_PRESSURE_PLATE = registerBlock("umbrastone_brick_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET));

    //Umbrastone
    public static final RegistryObject<Block> UMBRASTONE = registerBlock("umbrastone", () -> new Block(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_STAIRS = registerBlock("umbrastone_stairs", () -> new StairBlock(BlockRegistry.UMBRASTONE.get().defaultBlockState(), UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_SLAB = registerBlock("umbrastone_slab", () -> new SlabBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_WALL = registerBlock("umbrastone_wall", () -> new WallBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> UMBRASTONE_BUTTON = registerBlock("umbrastone_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> UMBRASTONE_PRESSURE_PLATE = registerBlock("umbrastone_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET));

    //Cobbled umbrastone
    public static final RegistryObject<Block> COBBLED_UMBRASTONE = registerBlock("cobbled_umbrastone", () -> new Block(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_UMBRASTONE_STAIRS = registerBlock("cobbled_umbrastone_stairs", () -> new StairBlock(BlockRegistry.COBBLED_UMBRASTONE.get().defaultBlockState(), UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_UMBRASTONE_SLAB = registerBlock("cobbled_umbrastone_slab", () -> new SlabBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_UMBRASTONE_WALL = registerBlock("cobbled_umbrastone_wall", () -> new WallBlock(UMBRASTONE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_UMBRASTONE_BUTTON = registerBlock("cobbled_umbrastone_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> COBBLED_UMBRASTONE_PRESSURE_PLATE = registerBlock("cobbled_umbrastone_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_MAGENTA).requiresCorrectToolForDrops().noCollission(), UMBRASTONE_BLOCKSET));



    //Scarlet misc
    public static final RegistryObject<Block> SCARLET_BUSH = registerBlock("scarlet_bush",
            () -> new ScarletBushBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA_LEAVES)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.AZALEA_LEAVES).noCollission()));
    public static final RegistryObject<Block> SCARLET_LEAVES = registerBlock("scarlet_leaves",
            () -> new ScarletLeavesBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA_LEAVES)
                    .mapColor(MapColor.COLOR_PINK).sound(SoundType.AZALEA_LEAVES)));
    public static final RegistryObject<Block> SCARLET_LOG = registerBlock("scarlet_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.CHERRY_WOOD)));
    public static final RegistryObject<Block> SCARLET_SAPLING = registerBlock("scarlet_sapling",
            () -> new SaplingBlock(new ScarletGrower(), BlockBehaviour.Properties.copy(Blocks.CHERRY_SAPLING)
                    .mapColor(MapColor.COLOR_PINK).sound(SoundType.CHERRY_SAPLING)));
    public static final RegistryObject<Block> POTTED_SCARLET_SAPLING = BLOCKS.register("potted_scarlet_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockRegistry.SCARLET_SAPLING,
                    BlockBehaviour.Properties.copy(Blocks.POTTED_ACACIA_SAPLING).noOcclusion()));

    //Scarlet planks
    public static final RegistryObject<Block> SCARLET_PLANKS = registerBlock("scarlet_planks", () -> new Block(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_STAIRS = registerBlock("scarlet_stairs", () -> new StairBlock(SCARLET_PLANKS.get().defaultBlockState(), SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_SLAB = registerBlock("scarlet_slab", () -> new SlabBlock(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_DOOR = registerBlock("scarlet_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noOcclusion(), BlockSetType.CHERRY));
    public static final RegistryObject<Block> SCARLET_TRAPDOOR = registerBlock("scarlet_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noOcclusion(), BlockSetType.CHERRY));
    public static final RegistryObject<Block> SCARLET_FENCE = registerBlock("scarlet_fence", () -> new FenceBlock(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_FENCE_GATE = registerBlock("scarlet_fence_gate", () -> new FenceGateBlock(SCARLET_PROPERTIES, WoodType.CHERRY));
    public static final RegistryObject<Block> SCARLET_BUTTON = registerBlock("scarlet_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().noCollission(), BlockSetType.CHERRY, 30, true));
    public static final RegistryObject<Block> SCARLET_PRESSURE_PLATE = registerBlock("scarlet_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().noCollission(), BlockSetType.CHERRY));



    //Dark candy misc
    public static final RegistryObject<Block> DARK_CANDY_BLOCK = registerBlock("dark_candy_block",
            () -> new DarkCandyBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA_LEAVES)
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.AZALEA_LEAVES).noCollission().noOcclusion().instabreak().randomTicks()));
    public static final RegistryObject<Block> DARK_CANDY_LEAVES = registerBlock("dark_candy_leaves",
            () -> new DarkCandyLeaves(BlockBehaviour.Properties.copy(Blocks.AZALEA_LEAVES)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.AZALEA_LEAVES).randomTicks()));
    public static final RegistryObject<Block> DARK_CANDY_LOG = registerBlock("dark_candy_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.CHERRY_WOOD)));
    public static final RegistryObject<Block>DARK_CANDY_SAPLING = registerBlock("dark_candy_sapling",
            () -> new SaplingBlock(new DarkCandyGrower(), BlockBehaviour.Properties.copy(Blocks.CHERRY_SAPLING)
                    .mapColor(MapColor.COLOR_PURPLE).sound(SoundType.CHERRY_SAPLING)));
    public static final RegistryObject<Block> POTTED_DARK_CANDY_SAPLING = BLOCKS.register("potted_dark_candy_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), BlockRegistry.DARK_CANDY_SAPLING,
                    BlockBehaviour.Properties.copy(Blocks.POTTED_ACACIA_SAPLING).noOcclusion()));

    public static final RegistryObject<Block> DARK_CANDY_CRAFTING_TABLE = registerBlock("dark_candy_crafting_table",
            () -> new GenericCraftingTableBlock(DARK_CANDY_PROPERTIES));

    //Dark candy planks
    public static final RegistryObject<Block> DARK_CANDY_PLANKS = registerBlock("dark_candy_planks", () -> new Block(DARK_CANDY_PROPERTIES));
    public static final RegistryObject<Block> DARK_CANDY_STAIRS = registerBlock("dark_candy_stairs", () -> new StairBlock(DARK_CANDY_PLANKS.get().defaultBlockState(), DARK_CANDY_PROPERTIES));
    public static final RegistryObject<Block> DARK_CANDY_SLAB = registerBlock("dark_candy_slab", () -> new SlabBlock(DARK_CANDY_PROPERTIES));
    public static final RegistryObject<Block> DARK_CANDY_DOOR = registerBlock("dark_candy_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noOcclusion(), BlockSetType.CHERRY));
    public static final RegistryObject<Block> DARK_CANDY_TRAPDOOR = registerBlock("dark_candy_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noOcclusion(), BlockSetType.CHERRY));
    public static final RegistryObject<Block> DARK_CANDY_FENCE = registerBlock("dark_candy_fence", () -> new FenceBlock(DARK_CANDY_PROPERTIES));
    public static final RegistryObject<Block> DARK_CANDY_FENCE_GATE = registerBlock("dark_candy_fence_gate", () -> new FenceGateBlock(DARK_CANDY_PROPERTIES, WoodType.CHERRY));
    public static final RegistryObject<Block> DARK_CANDY_BUTTON = registerBlock("dark_candy_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), BlockSetType.CHERRY, 30, true));
    public static final RegistryObject<Block> DARK_CANDY_PRESSURE_PLATE = registerBlock("dark_candy_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CHERRY_WOOD).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), BlockSetType.CHERRY));



    //Scarlet marble misc
    public static final RegistryObject<Block> SCARLET_MARBLE_PILLAR = registerBlock("scarlet_marble_pillar", () -> new RotatedPillarBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> CHISELED_SCARLET_MARBLE = registerBlock("chiseled_scarlet_marble", () -> new Block(SCARLET_MARBLE_PROPERTIES));

    //Polished scarlet marble
    public static final RegistryObject<Block> POLISHED_SCARLET_MARBLE = registerBlock("polished_scarlet_marble", () -> new Block(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_SCARLET_MARBLE_STAIRS = registerBlock("polished_scarlet_marble_stairs", () -> new StairBlock(BlockRegistry.POLISHED_SCARLET_MARBLE.get().defaultBlockState(), SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_SCARLET_MARBLE_SLAB = registerBlock("polished_scarlet_marble_slab", () -> new SlabBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_SCARLET_MARBLE_WALL = registerBlock("polished_scarlet_marble_wall", () -> new WallBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_SCARLET_MARBLE_BUTTON = registerBlock("polished_scarlet_marble_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> POLISHED_SCARLET_MARBLE_PRESSURE_PLATE = registerBlock("polished_scarlet_marble_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET));

    //Scarlet marble bricks
    public static final RegistryObject<Block> SCARLET_MARBLE_BRICKS = registerBlock("scarlet_marble_bricks", () -> new Block(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_BRICK_STAIRS = registerBlock("scarlet_marble_brick_stairs", () -> new StairBlock(BlockRegistry.SCARLET_MARBLE_BRICKS.get().defaultBlockState(), SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_BRICK_SLAB = registerBlock("scarlet_marble_brick_slab", () -> new SlabBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_BRICK_WALL = registerBlock("scarlet_marble_brick_wall", () -> new WallBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_BRICK_BUTTON = registerBlock("scarlet_marble_brick_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> SCARLET_MARBLE_BRICK_PRESSURE_PLATE = registerBlock("scarlet_marble_brick_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET));

    //Scarlet marble
    public static final RegistryObject<Block> SCARLET_MARBLE = registerBlock("scarlet_marble", () -> new Block(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_STAIRS = registerBlock("scarlet_marble_stairs", () -> new StairBlock(BlockRegistry.SCARLET_MARBLE.get().defaultBlockState(), SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_SLAB = registerBlock("scarlet_marble_slab", () -> new SlabBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_WALL = registerBlock("scarlet_marble_wall", () -> new WallBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_BUTTON = registerBlock("scarlet_marble_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> SCARLET_MARBLE_PRESSURE_PLATE = registerBlock("scarlet_marble_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET));

    //Cobbled scarlet marble
    public static final RegistryObject<Block> COBBLED_SCARLET_MARBLE = registerBlock("cobbled_scarlet_marble", () -> new Block(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_SCARLET_MARBLE_STAIRS = registerBlock("cobbled_scarlet_marble_stairs", () -> new StairBlock(BlockRegistry.COBBLED_SCARLET_MARBLE.get().defaultBlockState(), SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_SCARLET_MARBLE_SLAB = registerBlock("cobbled_scarlet_marble_slab", () -> new SlabBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_SCARLET_MARBLE_WALL = registerBlock("cobbled_scarlet_marble_wall", () -> new WallBlock(SCARLET_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_SCARLET_MARBLE_BUTTON = registerBlock("cobbled_scarlet_marble_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> COBBLED_SCARLET_MARBLE_PRESSURE_PLATE = registerBlock("cobbled_scarlet_marble_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().noCollission(), SCARLET_MARBLE_BLOCKSET));

    //Scarlet marble chess
    public static final RegistryObject<Block> SCARLET_MARBLE_PAWN = registerBlock("scarlet_marble_pawn", () -> new ChessPawnBlock(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_ROOK = registerBlock("scarlet_marble_rook", () -> new ChessRookBlock(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_KNIGHT = registerBlock("scarlet_marble_knight", () -> new ChessKnightBlock(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_BISHOP = registerBlock("scarlet_marble_bishop", () -> new ChessBishopBlock(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_QUEEN = registerBlock("scarlet_marble_queen", () -> new ChessQueenBlock(SCARLET_PROPERTIES));
    public static final RegistryObject<Block> SCARLET_MARBLE_KING = registerBlock("scarlet_marble_king", () -> new ChessKingBlock(SCARLET_PROPERTIES));



    //Dark marble misc
    public static final RegistryObject<Block> DARK_MARBLE_PILLAR = registerBlock("dark_marble_pillar", () -> new RotatedPillarBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> CHISELED_DARK_MARBLE = registerBlock("chiseled_dark_marble", () -> new Block(DARK_MARBLE_PROPERTIES));

    //Polished dark marble
    public static final RegistryObject<Block> POLISHED_DARK_MARBLE = registerBlock("polished_dark_marble", () -> new Block(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_DARK_MARBLE_STAIRS = registerBlock("polished_dark_marble_stairs", () -> new StairBlock(BlockRegistry.POLISHED_DARK_MARBLE.get().defaultBlockState(), DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_DARK_MARBLE_SLAB = registerBlock("polished_dark_marble_slab", () -> new SlabBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_DARK_MARBLE_WALL = registerBlock("polished_dark_marble_wall", () -> new WallBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_DARK_MARBLE_BUTTON = registerBlock("polished_dark_marble_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission(), DARK_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> POLISHED_DARK_MARBLE_PRESSURE_PLATE = registerBlock("polished_dark_marble_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission(), DARK_MARBLE_BLOCKSET));

    //Dark marble bricks
    public static final RegistryObject<Block> DARK_MARBLE_BRICKS = registerBlock("dark_marble_bricks", () -> new Block(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_BRICK_STAIRS = registerBlock("dark_marble_brick_stairs", () -> new StairBlock(BlockRegistry.DARK_MARBLE_BRICKS.get().defaultBlockState(), DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_BRICK_SLAB = registerBlock("dark_marble_brick_slab", () -> new SlabBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_BRICK_WALL = registerBlock("dark_marble_brick_wall", () -> new WallBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_BRICK_BUTTON = registerBlock("dark_marble_brick_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission(), DARK_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> DARK_MARBLE_BRICK_PRESSURE_PLATE = registerBlock("dark_marble_brick_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().noCollission(), DARK_MARBLE_BLOCKSET));

    //Dark marble
    public static final RegistryObject<Block> DARK_MARBLE = registerBlock("dark_marble", () -> new Block(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_STAIRS = registerBlock("dark_marble_stairs", () -> new StairBlock(BlockRegistry.DARK_MARBLE.get().defaultBlockState(), DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_SLAB = registerBlock("dark_marble_slab", () -> new SlabBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_WALL = registerBlock("dark_marble_wall", () -> new WallBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_BUTTON = registerBlock("dark_marble_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).noCollission(), DARK_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> DARK_MARBLE_PRESSURE_PLATE = registerBlock("dark_marble_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).noCollission(), DARK_MARBLE_BLOCKSET));

    //Cobbled dark marble
    public static final RegistryObject<Block> COBBLED_DARK_MARBLE = registerBlock("cobbled_dark_marble", () -> new Block(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_DARK_MARBLE_STAIRS = registerBlock("cobbled_dark_marble_stairs", () -> new StairBlock(BlockRegistry.COBBLED_DARK_MARBLE.get().defaultBlockState(), DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_DARK_MARBLE_SLAB = registerBlock("cobbled_dark_marble_slab", () -> new SlabBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_DARK_MARBLE_WALL = registerBlock("cobbled_dark_marble_wall", () -> new WallBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_DARK_MARBLE_BUTTON = registerBlock("cobbled_dark_marble_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).noCollission(), DARK_MARBLE_BLOCKSET, 30, false));
    public static final RegistryObject<Block> COBBLED_DARK_MARBLE_PRESSURE_PLATE = registerBlock("cobbled_dark_marble_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_GRAY).noCollission(), DARK_MARBLE_BLOCKSET));

    //Dark marble chess
    public static final RegistryObject<Block> DARK_MARBLE_PAWN = registerBlock("dark_marble_pawn", () -> new ChessPawnBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_ROOK = registerBlock("dark_marble_rook", () -> new ChessRookBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_KNIGHT = registerBlock("dark_marble_knight", () -> new ChessKnightBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_BISHOP = registerBlock("dark_marble_bishop", () -> new ChessBishopBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_QUEEN = registerBlock("dark_marble_queen", () -> new ChessQueenBlock(DARK_MARBLE_PROPERTIES));
    public static final RegistryObject<Block> DARK_MARBLE_KING = registerBlock("dark_marble_king", () -> new ChessKingBlock(DARK_MARBLE_PROPERTIES));



    //Cliffrock misc
    public static final RegistryObject<Block> CLIFFROCK_PATH = registerBlock("cliffrock_path", () -> new Block(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_PILLAR = registerBlock("cliffrock_pillar", () -> new RotatedPillarBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CHISELED_CLIFFROCK = registerBlock("chiseled_cliffrock", () -> new Block(CLIFFROCK_PROPERTIES));

    //Polished cliffrock
    public static final RegistryObject<Block> POLISHED_CLIFFROCK = registerBlock("polished_cliffrock", () -> new Block(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_CLIFFROCK_STAIRS = registerBlock("polished_cliffrock_stairs", () -> new StairBlock(BlockRegistry.POLISHED_CLIFFROCK.get().defaultBlockState(), CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_CLIFFROCK_SLAB = registerBlock("polished_cliffrock_slab", () -> new SlabBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_CLIFFROCK_WALL = registerBlock("polished_cliffrock_wall", () -> new WallBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_CLIFFROCK_BUTTON = registerBlock("polished_cliffrock_button", () -> new CliffrockButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().sound(SoundTypeRegistry.CLIFF).noCollission(), 30, false));
    public static final RegistryObject<Block> POLISHED_CLIFFROCK_PRESSURE_PLATE = registerBlock("polished_cliffrock_pressure_plate", () -> new GenericPressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().noCollission(), SoundTypeRegistry.CLIFF, SoundRegistry.CLIFFROCK_PRESS.get(), SoundRegistry.CLIFFROCK_UNPRESS.get()));

    //Cliffrock bricks
    public static final RegistryObject<Block> CLIFFROCK_BRICKS = registerBlock("cliffrock_bricks", () -> new Block(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_BRICK_STAIRS = registerBlock("cliffrock_brick_stairs", () -> new StairBlock(BlockRegistry.CLIFFROCK_BRICKS.get().defaultBlockState(), CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_BRICK_SLAB = registerBlock("cliffrock_brick_slab", () -> new SlabBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_BRICK_WALL = registerBlock("cliffrock_brick_wall", () -> new WallBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_BRICK_BUTTON = registerBlock("cliffrock_brick_button", () -> new CliffrockButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().sound(SoundTypeRegistry.CLIFF).noCollission(), 30, false));
    public static final RegistryObject<Block> CLIFFROCK_BRICK_PRESSURE_PLATE = registerBlock("cliffrock_brick_pressure_plate", () -> new GenericPressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().noCollission(), SoundTypeRegistry.CLIFF, SoundRegistry.CLIFFROCK_PRESS.get(), SoundRegistry.CLIFFROCK_UNPRESS.get()));

    //Cliffrock
    public static final RegistryObject<Block> CLIFFROCK = registerBlock("cliffrock", () -> new PathableBlock(CLIFFROCK_PROPERTIES, BlockRegistry.CLIFFROCK_PATH.get()));
    public static final RegistryObject<Block> CLIFFROCK_STAIRS = registerBlock("cliffrock_stairs", () -> new StairBlock(BlockRegistry.CLIFFROCK.get().defaultBlockState(), CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_SLAB = registerBlock("cliffrock_slab", () -> new SlabBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_WALL = registerBlock("cliffrock_wall", () -> new WallBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> CLIFFROCK_BUTTON = registerBlock("cliffrock_button", () -> new CliffrockButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().sound(SoundTypeRegistry.CLIFF).noCollission(), 30, false));
    public static final RegistryObject<Block> CLIFFROCK_PRESSURE_PLATE = registerBlock("cliffrock_pressure_plate", () -> new GenericPressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().noCollission(), SoundTypeRegistry.CLIFF, SoundRegistry.CLIFFROCK_PRESS.get(), SoundRegistry.CLIFFROCK_UNPRESS.get()));

    //Cobbled cliffrock
    public static final RegistryObject<Block> COBBLED_CLIFFROCK = registerBlock("cobbled_cliffrock", () -> new Block(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_CLIFFROCK_STAIRS = registerBlock("cobbled_cliffrock_stairs", () -> new StairBlock(BlockRegistry.COBBLED_CLIFFROCK.get().defaultBlockState(), CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_CLIFFROCK_SLAB = registerBlock("cobbled_cliffrock_slab", () -> new SlabBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_CLIFFROCK_WALL = registerBlock("cobbled_cliffrock_wall", () -> new WallBlock(CLIFFROCK_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_CLIFFROCK_BUTTON = registerBlock("cobbled_cliffrock_button", () -> new CliffrockButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().sound(SoundTypeRegistry.CLIFF).noCollission(), 30, false));
    public static final RegistryObject<Block> COBBLED_CLIFFROCK_PRESSURE_PLATE = registerBlock("cobbled_cliffrock_pressure_plate", () -> new GenericPressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().noCollission(), SoundTypeRegistry.CLIFF, SoundRegistry.CLIFFROCK_PRESS.get(), SoundRegistry.CLIFFROCK_UNPRESS.get()));



    //Tenebralith misc
    public static final RegistryObject<Block> TENEBRALITH_SPIKE = registerBlock("tenebralith_spike", () -> new TenebralithSpikeBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_PATH = registerBlock("tenebralith_path", () -> new Block(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> DARK_SAND = registerBlock("dark_sand", () -> new FallingBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_SAND).mapColor(DyeColor.BLACK).sound(SoundType.SOUL_SAND).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> TENEBRALITH_PILLAR = registerBlock("tenebralith_pillar", () -> new RotatedPillarBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> CHISELED_TENEBRALITH = registerBlock("chiseled_tenebralith", () -> new Block(TENEBRALITH_PROPERTIES));

    //Polished tenebralith
    public static final RegistryObject<Block> POLISHED_TENEBRALITH = registerBlock("polished_tenebralith", () -> new Block(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_TENEBRALITH_STAIRS = registerBlock("polished_tenebralith_stairs", () -> new StairBlock(BlockRegistry.POLISHED_TENEBRALITH.get().defaultBlockState(), TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_TENEBRALITH_SLAB = registerBlock("polished_tenebralith_slab", () -> new SlabBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_TENEBRALITH_WALL = registerBlock("polished_tenebralith_wall", () -> new WallBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> POLISHED_TENEBRALITH_BUTTON = registerBlock("polished_tenebralith_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().noCollission(), TENEBRALITH_BLOCKSET, 30, false));
    public static final RegistryObject<Block> POLISHED_TENEBRALITH_PRESSURE_PLATE = registerBlock("polished_tenebralith_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().noCollission(), TENEBRALITH_BLOCKSET));

    //Tenebralith bricks
    public static final RegistryObject<Block> TENEBRALITH_BRICKS = registerBlock("tenebralith_bricks", () -> new Block(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_BRICK_STAIRS = registerBlock("tenebralith_brick_stairs", () -> new StairBlock(BlockRegistry.TENEBRALITH_BRICKS.get().defaultBlockState(), TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_BRICK_SLAB = registerBlock("tenebralith_brick_slab", () -> new SlabBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_BRICK_WALL = registerBlock("tenebralith_brick_wall", () -> new WallBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_BRICK_BUTTON = registerBlock("tenebralith_brick_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().noCollission(), TENEBRALITH_BLOCKSET, 30, false));
    public static final RegistryObject<Block> TENEBRALITH_BRICK_PRESSURE_PLATE = registerBlock("tenebralith_brick_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().noCollission(), TENEBRALITH_BLOCKSET));

    //Tenebralith
    public static final RegistryObject<Block> TENEBRALITH = registerBlock("tenebralith", () -> new PathableBlock(TENEBRALITH_PROPERTIES, BlockRegistry.TENEBRALITH_PATH.get()));
    public static final RegistryObject<Block> TENEBRALITH_STAIRS = registerBlock("tenebralith_stairs", () -> new StairBlock(BlockRegistry.TENEBRALITH.get().defaultBlockState(), TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_SLAB = registerBlock("tenebralith_slab", () -> new SlabBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_WALL = registerBlock("tenebralith_wall", () -> new WallBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> TENEBRALITH_BUTTON = registerBlock("tenebralith_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).noCollission(), TENEBRALITH_BLOCKSET, 30, false));
    public static final RegistryObject<Block> TENEBRALITH_PRESSURE_PLATE = registerBlock("tenebralith_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).noCollission(), TENEBRALITH_BLOCKSET));

    //Cobbled tenebralith
    public static final RegistryObject<Block> COBBLED_TENEBRALITH = registerBlock("cobbled_tenebralith", () -> new Block(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_TENEBRALITH_STAIRS = registerBlock("cobbled_tenebralith_stairs", () -> new StairBlock(BlockRegistry.COBBLED_TENEBRALITH.get().defaultBlockState(), TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_TENEBRALITH_SLAB = registerBlock("cobbled_tenebralith_slab", () -> new SlabBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_TENEBRALITH_WALL = registerBlock("cobbled_tenebralith_wall", () -> new WallBlock(TENEBRALITH_PROPERTIES));
    public static final RegistryObject<Block> COBBLED_TENEBRALITH_BUTTON = registerBlock("cobbled_tenebralith_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).noCollission(), TENEBRALITH_BLOCKSET, 30, false));
    public static final RegistryObject<Block> COBBLED_TENEBRALITH_PRESSURE_PLATE = registerBlock("cobbled_tenebralith_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLACK).noCollission(), TENEBRALITH_BLOCKSET));



    //Rosegold ores
    public static final RegistryObject<Block> UMBRASTONE_ROSEGOLD_ORE = registerBlock("umbrastone_rosegold_ore", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GOLD_ORE).mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    //Rosegold misc
    public static final RegistryObject<Block> GREAT_DOOR_SPAWNER = registerBlock("great_door_spawner",
            () -> new GreatDoorSpawnerBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noLootTable().pushReaction(PushReaction.BLOCK)));
    public static final RegistryObject<Block> ROSEGOLD_PILLAR = registerBlock("rosegold_pillar", () -> new RotatedPillarBlock(ROSEGOLD_PROPERTIES));

    //Polished rosegold
    public static final RegistryObject<Block> ROSEGOLD_BLOCK = registerBlock("rosegold_block", () -> new Block(ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_STAIRS = registerBlock("rosegold_stairs", () -> new StairBlock(BlockRegistry.ROSEGOLD_BLOCK.get().defaultBlockState(), ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_SLAB = registerBlock("rosegold_slab", () -> new SlabBlock(ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_WALL = registerBlock("rosegold_wall", () -> new WallBlock(ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_BUTTON = registerBlock("rosegold_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().noCollission(), ROSEGOLD_BLOCKSET, 30, false));
    public static final RegistryObject<Block> ROSEGOLD_PRESSURE_PLATE = registerBlock("rosegold_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().noCollission(), ROSEGOLD_BLOCKSET));

    //Rosegold bricks
    public static final RegistryObject<Block> ROSEGOLD_BRICKS = registerBlock("rosegold_bricks", () -> new Block(ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_BRICK_STAIRS = registerBlock("rosegold_brick_stairs", () -> new StairBlock(BlockRegistry.ROSEGOLD_BRICKS.get().defaultBlockState(), ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_BRICK_SLAB = registerBlock("rosegold_brick_slab", () -> new SlabBlock(ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_BRICK_WALL = registerBlock("rosegold_brick_wall", () -> new WallBlock(ROSEGOLD_PROPERTIES));
    public static final RegistryObject<Block> ROSEGOLD_BRICK_BUTTON = registerBlock("rosegold_brick_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().noCollission(), ROSEGOLD_BLOCKSET, 30, false));
    public static final RegistryObject<Block> ROSEGOLD_BRICK_PRESSURE_PLATE = registerBlock("rosegold_brick_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().noCollission(), ROSEGOLD_BLOCKSET));



    public static final RegistryObject<LiquidBlock> LUMINESCENT_WATER = BLOCKS.register("luminescent_water",
            () -> new LuminescentWaterFluidBlock(FluidRegistry.SOURCE_LUMINESCENT_WATER, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()
                    .lightLevel(state -> 15).randomTicks()));
    public static final RegistryObject<LiquidBlock> PURE_DARKNESS = BLOCKS.register("pure_darkness",
            () -> new LiquidBlock(FluidRegistry.SOURCE_PURE_DARKNESS, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<Block> HEARTH = registerBlock("hearth",
            () -> new HearthBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN)
                    .mapColor(MapColor.COLOR_GRAY).sound(SoundType.LANTERN).noOcclusion().lightLevel(state -> 0)));

    public static final RegistryObject<Block> DARKNESS = BLOCKS.register("darkness",
            () -> new DarknessBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(PushReaction.BLOCK)
                    .randomTicks().noOcclusion()));

    public static final RegistryObject<Block> GREAT_DOOR_SHAPE = BLOCKS.register("great_door_shape",
            () -> new GreatDoorShapeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(PushReaction.BLOCK)
                    .noOcclusion().noCollission()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, Supplier<T> block) {
        return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}