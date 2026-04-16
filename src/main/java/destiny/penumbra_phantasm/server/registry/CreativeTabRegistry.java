package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> DEF_REG  = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PenumbraPhantasm.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = DEF_REG.register("main", () -> CreativeModeTab.builder()
            .icon(() -> ItemRegistry.SOUL_HEARTH.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.penumbra_phantasm.main"))
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.SOUL_HEARTH.get());
                output.accept(BlockRegistry.HEARTH.get());

                output.accept(ItemRegistry.DELTA_SHIELD.get());
                output.accept(ItemRegistry.FAMILIAR_SWORD.get());
                output.accept(ItemRegistry.FAMILIAR_AX.get());

                output.accept(ItemRegistry.GOLD_KNIFE.get());
                output.accept(ItemRegistry.IRON_KNIFE.get());
                output.accept(ItemRegistry.DIAMOND_KNIFE.get());
                output.accept(ItemRegistry.NETHERITE_KNIFE.get());
                output.accept(ItemRegistry.REAL_KNIFE.get());
                output.accept(ItemRegistry.BLACK_KNIFE.get());

                output.accept(ItemRegistry.BLACK_SHARD.get());
                output.accept(ItemRegistry.SHADOW_CRYSTAL.get());

                output.accept(ItemRegistry.FRIEND.get());

                output.accept(ItemRegistry.ITEM_MUSIC_MEDIUM_THE_HOLY.get());

                output.accept(ItemRegistry.RAW_ROSEGOLD.get());
                output.accept(ItemRegistry.ROSEGOLD_INGOT.get());

                output.accept(BlockRegistry.UMBRASTONE_ROSEGOLD_ORE.get());

                output.accept(BlockRegistry.GREAT_DOOR_SPAWNER.get());
                output.accept(BlockRegistry.ROSEGOLD_PILLAR.get());

                output.accept(BlockRegistry.ROSEGOLD_BLOCK.get());
                output.accept(BlockRegistry.ROSEGOLD_STAIRS.get());
                output.accept(BlockRegistry.ROSEGOLD_SLAB.get());
                output.accept(BlockRegistry.ROSEGOLD_WALL.get());
                output.accept(BlockRegistry.ROSEGOLD_BUTTON.get());
                output.accept(BlockRegistry.ROSEGOLD_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.ROSEGOLD_BRICKS.get());
                output.accept(BlockRegistry.ROSEGOLD_BRICK_STAIRS.get());
                output.accept(BlockRegistry.ROSEGOLD_BRICK_SLAB.get());
                output.accept(BlockRegistry.ROSEGOLD_BRICK_WALL.get());
                output.accept(BlockRegistry.ROSEGOLD_BRICK_BUTTON.get());
                output.accept(BlockRegistry.ROSEGOLD_BRICK_PRESSURE_PLATE.get());

            })
            .build()
    );

    public static final RegistryObject<CreativeModeTab> CARD_KINGDOM = DEF_REG.register("card_kingdom", () -> CreativeModeTab.builder()
            .icon(() -> BlockRegistry.SCARLET_LEAVES.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.penumbra_phantasm.card_kingdom"))
            .withTabsBefore(MAIN.getKey())
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.DARk_CANDY_SWORD.get());
                output.accept(ItemRegistry.DARk_CANDY_AXE.get());
                output.accept(ItemRegistry.DARk_CANDY_PICKAXE.get());
                output.accept(ItemRegistry.DARk_CANDY_SHOVEL.get());
                output.accept(ItemRegistry.DARk_CANDY_HOE.get());

                output.accept(ItemRegistry.UMBRASTONE_SWORD.get());
                output.accept(ItemRegistry.UMBRASTONE_AXE.get());
                output.accept(ItemRegistry.UMBRASTONE_PICKAXE.get());
                output.accept(ItemRegistry.UMBRASTONE_SHOVEL.get());
                output.accept(ItemRegistry.UMBRASTONE_HOE.get());

                output.accept(ItemRegistry.DARK_CANDY_STICK.get());

                output.accept(ItemRegistry.DARK_CANDY_BUCKET.get());
                output.accept(ItemRegistry.LUMINESCENT_WATER_BUCKET.get());
                output.accept(ItemRegistry.PURE_DARKNESS_BUCKET.get());

                output.accept(BlockRegistry.STARTAIL.get());
                output.accept(BlockRegistry.ECHO_FLOWER.get());

                output.accept(BlockRegistry.TWILIGHT_GRASS.get());
                output.accept(BlockRegistry.TWILIGHT_GRASS_BLOCK.get());

                output.accept(BlockRegistry.SCARLET_ROSE.get());
                output.accept(BlockRegistry.TALL_SCARLET_BUSH.get());

                output.accept(BlockRegistry.SCARLET_BUSH.get());

                output.accept(BlockRegistry.SCARLET_SAPLING.get());
                output.accept(BlockRegistry.SCARLET_LEAVES.get());
                output.accept(BlockRegistry.FALLEN_SCARLET_LEAVES.get());

                output.accept(BlockRegistry.SCARLET_LOG.get());
                output.accept(BlockRegistry.SCARLET_PLANKS.get());
                output.accept(BlockRegistry.SCARLET_STAIRS.get());
                output.accept(BlockRegistry.SCARLET_SLAB.get());
                output.accept(BlockRegistry.SCARLET_FENCE.get());
                output.accept(BlockRegistry.SCARLET_FENCE_GATE.get());
                output.accept(BlockRegistry.SCARLET_DOOR.get());
                output.accept(BlockRegistry.SCARLET_TRAPDOOR.get());
                output.accept(BlockRegistry.SCARLET_BUTTON.get());
                output.accept(BlockRegistry.SCARLET_PRESSURE_PLATE.get());

                output.accept(ItemRegistry.DARK_CANDY.get());
                output.accept(BlockRegistry.DARK_CANDY_BLOCK.get());

                output.accept(BlockRegistry.DARK_CANDY_SAPLING.get());
                output.accept(BlockRegistry.DARK_CANDY_LEAVES.get());
                output.accept(BlockRegistry.FALLEN_DARK_CANDY_LEAVES.get());

                output.accept(BlockRegistry.DARK_CANDY_LOG.get());

                output.accept(BlockRegistry.DARK_CANDY_CRAFTING_TABLE.get());

                output.accept(BlockRegistry.DARK_CANDY_PLANKS.get());
                output.accept(BlockRegistry.DARK_CANDY_STAIRS.get());
                output.accept(BlockRegistry.DARK_CANDY_SLAB.get());
                output.accept(BlockRegistry.DARK_CANDY_FENCE.get());
                output.accept(BlockRegistry.DARK_CANDY_FENCE_GATE.get());
                output.accept(BlockRegistry.DARK_CANDY_DOOR.get());
                output.accept(BlockRegistry.DARK_CANDY_TRAPDOOR.get());
                output.accept(BlockRegistry.DARK_CANDY_BUTTON.get());
                output.accept(BlockRegistry.DARK_CANDY_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.NIGHT_GRASS.get());
                output.accept(BlockRegistry.NIGHT_GRASS_BLOCK.get());
                output.accept(BlockRegistry.NIGHT_DIRT.get());

                output.accept(BlockRegistry.UMBRASTONE_PILLAR.get());
                output.accept(BlockRegistry.CHISELED_UMBRASTONE.get());

                output.accept(BlockRegistry.POLISHED_UMBRASTONE.get());
                output.accept(BlockRegistry.POLISHED_UMBRASTONE_STAIRS.get());
                output.accept(BlockRegistry.POLISHED_UMBRASTONE_SLAB.get());
                output.accept(BlockRegistry.POLISHED_UMBRASTONE_WALL.get());
                output.accept(BlockRegistry.POLISHED_UMBRASTONE_BUTTON.get());
                output.accept(BlockRegistry.POLISHED_UMBRASTONE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.UMBRASTONE_BRICKS.get());
                output.accept(BlockRegistry.UMBRASTONE_BRICK_STAIRS.get());
                output.accept(BlockRegistry.UMBRASTONE_BRICK_SLAB.get());
                output.accept(BlockRegistry.UMBRASTONE_BRICK_WALL.get());
                output.accept(BlockRegistry.UMBRASTONE_BRICK_BUTTON.get());
                output.accept(BlockRegistry.UMBRASTONE_BRICK_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.UMBRASTONE.get());
                output.accept(BlockRegistry.UMBRASTONE_STAIRS.get());
                output.accept(BlockRegistry.UMBRASTONE_SLAB.get());
                output.accept(BlockRegistry.UMBRASTONE_WALL.get());
                output.accept(BlockRegistry.UMBRASTONE_BUTTON.get());
                output.accept(BlockRegistry.UMBRASTONE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.COBBLED_UMBRASTONE.get());
                output.accept(BlockRegistry.COBBLED_UMBRASTONE_STAIRS.get());
                output.accept(BlockRegistry.COBBLED_UMBRASTONE_SLAB.get());
                output.accept(BlockRegistry.COBBLED_UMBRASTONE_WALL.get());
                output.accept(BlockRegistry.COBBLED_UMBRASTONE_BUTTON.get());
                output.accept(BlockRegistry.COBBLED_UMBRASTONE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.SCARLET_MARBLE_PAWN.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_ROOK.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_KNIGHT.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BISHOP.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_QUEEN.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_KING.get());

                output.accept(BlockRegistry.SCARLET_MARBLE_PILLAR.get());
                output.accept(BlockRegistry.CHISELED_SCARLET_MARBLE.get());

                output.accept(BlockRegistry.POLISHED_SCARLET_MARBLE.get());
                output.accept(BlockRegistry.POLISHED_SCARLET_MARBLE_STAIRS.get());
                output.accept(BlockRegistry.POLISHED_SCARLET_MARBLE_SLAB.get());
                output.accept(BlockRegistry.POLISHED_SCARLET_MARBLE_WALL.get());
                output.accept(BlockRegistry.POLISHED_SCARLET_MARBLE_BUTTON.get());
                output.accept(BlockRegistry.POLISHED_SCARLET_MARBLE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.SCARLET_MARBLE_BRICKS.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BRICK_STAIRS.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BRICK_SLAB.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BRICK_WALL.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BRICK_BUTTON.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BRICK_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.SCARLET_MARBLE.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_STAIRS.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_SLAB.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_WALL.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BUTTON.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.COBBLED_SCARLET_MARBLE.get());
                output.accept(BlockRegistry.COBBLED_SCARLET_MARBLE_STAIRS.get());
                output.accept(BlockRegistry.COBBLED_SCARLET_MARBLE_SLAB.get());
                output.accept(BlockRegistry.COBBLED_SCARLET_MARBLE_WALL.get());
                output.accept(BlockRegistry.COBBLED_SCARLET_MARBLE_BUTTON.get());
                output.accept(BlockRegistry.COBBLED_SCARLET_MARBLE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.DARK_MARBLE_PAWN.get());
                output.accept(BlockRegistry.DARK_MARBLE_ROOK.get());
                output.accept(BlockRegistry.DARK_MARBLE_KNIGHT.get());
                output.accept(BlockRegistry.DARK_MARBLE_BISHOP.get());
                output.accept(BlockRegistry.DARK_MARBLE_QUEEN.get());
                output.accept(BlockRegistry.DARK_MARBLE_KING.get());

                output.accept(BlockRegistry.DARK_MARBLE_PILLAR.get());
                output.accept(BlockRegistry.CHISELED_DARK_MARBLE.get());

                output.accept(BlockRegistry.POLISHED_DARK_MARBLE.get());
                output.accept(BlockRegistry.POLISHED_DARK_MARBLE_STAIRS.get());
                output.accept(BlockRegistry.POLISHED_DARK_MARBLE_SLAB.get());
                output.accept(BlockRegistry.POLISHED_DARK_MARBLE_WALL.get());
                output.accept(BlockRegistry.POLISHED_DARK_MARBLE_BUTTON.get());
                output.accept(BlockRegistry.POLISHED_DARK_MARBLE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.DARK_MARBLE_BRICKS.get());
                output.accept(BlockRegistry.DARK_MARBLE_BRICK_STAIRS.get());
                output.accept(BlockRegistry.DARK_MARBLE_BRICK_SLAB.get());
                output.accept(BlockRegistry.DARK_MARBLE_BRICK_WALL.get());
                output.accept(BlockRegistry.DARK_MARBLE_BRICK_BUTTON.get());
                output.accept(BlockRegistry.DARK_MARBLE_BRICK_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.DARK_MARBLE.get());
                output.accept(BlockRegistry.DARK_MARBLE_STAIRS.get());
                output.accept(BlockRegistry.DARK_MARBLE_SLAB.get());
                output.accept(BlockRegistry.DARK_MARBLE_WALL.get());
                output.accept(BlockRegistry.DARK_MARBLE_BUTTON.get());
                output.accept(BlockRegistry.DARK_MARBLE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.COBBLED_DARK_MARBLE.get());
                output.accept(BlockRegistry.COBBLED_DARK_MARBLE_STAIRS.get());
                output.accept(BlockRegistry.COBBLED_DARK_MARBLE_SLAB.get());
                output.accept(BlockRegistry.COBBLED_DARK_MARBLE_WALL.get());
                output.accept(BlockRegistry.COBBLED_DARK_MARBLE_BUTTON.get());
                output.accept(BlockRegistry.COBBLED_DARK_MARBLE_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.CLIFFROCK_PATH.get());
                output.accept(BlockRegistry.CLIFFROCK_PILLAR.get());
                output.accept(BlockRegistry.CHISELED_CLIFFROCK.get());

                output.accept(BlockRegistry.POLISHED_CLIFFROCK.get());
                output.accept(BlockRegistry.POLISHED_CLIFFROCK_STAIRS.get());
                output.accept(BlockRegistry.POLISHED_CLIFFROCK_SLAB.get());
                output.accept(BlockRegistry.POLISHED_CLIFFROCK_WALL.get());
                output.accept(BlockRegistry.POLISHED_CLIFFROCK_BUTTON.get());
                output.accept(BlockRegistry.POLISHED_CLIFFROCK_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.CLIFFROCK_BRICKS.get());
                output.accept(BlockRegistry.CLIFFROCK_BRICK_STAIRS.get());
                output.accept(BlockRegistry.CLIFFROCK_BRICK_SLAB.get());
                output.accept(BlockRegistry.CLIFFROCK_BRICK_WALL.get());
                output.accept(BlockRegistry.CLIFFROCK_BRICK_BUTTON.get());
                output.accept(BlockRegistry.CLIFFROCK_BRICK_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.CLIFFROCK.get());
                output.accept(BlockRegistry.CLIFFROCK_STAIRS.get());
                output.accept(BlockRegistry.CLIFFROCK_SLAB.get());
                output.accept(BlockRegistry.CLIFFROCK_WALL.get());
                output.accept(BlockRegistry.CLIFFROCK_BUTTON.get());
                output.accept(BlockRegistry.CLIFFROCK_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.COBBLED_CLIFFROCK.get());
                output.accept(BlockRegistry.COBBLED_CLIFFROCK_STAIRS.get());
                output.accept(BlockRegistry.COBBLED_CLIFFROCK_SLAB.get());
                output.accept(BlockRegistry.COBBLED_CLIFFROCK_WALL.get());
                output.accept(BlockRegistry.COBBLED_CLIFFROCK_BUTTON.get());
                output.accept(BlockRegistry.COBBLED_CLIFFROCK_PRESSURE_PLATE.get());
            })
            .build()
    );

    public static final RegistryObject<CreativeModeTab> THE_DEPTHS = DEF_REG.register("the_depths", () -> CreativeModeTab.builder()
            .icon(() -> BlockRegistry.CHISELED_TENEBRALITH.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.penumbra_phantasm.the_depths"))
            .withTabsBefore(CARD_KINGDOM.getKey())
            .displayItems((parameters, output) -> {
                output.accept(BlockRegistry.TENEBRALITH_SPIKE.get());
                output.accept(BlockRegistry.TENEBRALITH_PATH.get());
                output.accept(BlockRegistry.DARK_SAND.get());
                output.accept(BlockRegistry.TENEBRALITH_PILLAR.get());
                output.accept(BlockRegistry.CHISELED_TENEBRALITH.get());

                output.accept(BlockRegistry.POLISHED_TENEBRALITH.get());
                output.accept(BlockRegistry.POLISHED_TENEBRALITH_STAIRS.get());
                output.accept(BlockRegistry.POLISHED_TENEBRALITH_SLAB.get());
                output.accept(BlockRegistry.POLISHED_TENEBRALITH_WALL.get());
                output.accept(BlockRegistry.POLISHED_TENEBRALITH_BUTTON.get());
                output.accept(BlockRegistry.POLISHED_TENEBRALITH_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.TENEBRALITH_BRICKS.get());
                output.accept(BlockRegistry.TENEBRALITH_BRICK_STAIRS.get());
                output.accept(BlockRegistry.TENEBRALITH_BRICK_SLAB.get());
                output.accept(BlockRegistry.TENEBRALITH_BRICK_WALL.get());
                output.accept(BlockRegistry.TENEBRALITH_BRICK_BUTTON.get());
                output.accept(BlockRegistry.TENEBRALITH_BRICK_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.TENEBRALITH.get());
                output.accept(BlockRegistry.TENEBRALITH_STAIRS.get());
                output.accept(BlockRegistry.TENEBRALITH_SLAB.get());
                output.accept(BlockRegistry.TENEBRALITH_WALL.get());
                output.accept(BlockRegistry.TENEBRALITH_BUTTON.get());
                output.accept(BlockRegistry.TENEBRALITH_PRESSURE_PLATE.get());

                output.accept(BlockRegistry.COBBLED_TENEBRALITH.get());
                output.accept(BlockRegistry.COBBLED_TENEBRALITH_STAIRS.get());
                output.accept(BlockRegistry.COBBLED_TENEBRALITH_SLAB.get());
                output.accept(BlockRegistry.COBBLED_TENEBRALITH_WALL.get());
                output.accept(BlockRegistry.COBBLED_TENEBRALITH_BUTTON.get());
                output.accept(BlockRegistry.COBBLED_TENEBRALITH_PRESSURE_PLATE.get());
            })
            .build()
    );
}