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

                output.accept(BlockRegistry.TWILIGHT_GRASS.get());
                output.accept(BlockRegistry.TWILIGHT_GRASS_BLOCK.get());

                output.accept(BlockRegistry.SCARLET_SAPLING.get());
                output.accept(BlockRegistry.SCARLET_LEAVES.get());
                output.accept(BlockRegistry.SCARLET_BUSH.get());
                output.accept(BlockRegistry.SCARLET_LOG.get());
                output.accept(BlockRegistry.SCARLET_PLANKS.get());
                output.accept(BlockRegistry.SCARLET_SLAB.get());
                output.accept(BlockRegistry.SCARLET_STAIRS.get());
                output.accept(BlockRegistry.SCARLET_FENCE.get());
                output.accept(BlockRegistry.SCARLET_FENCE_GATE.get());
                output.accept(BlockRegistry.SCARLET_DOOR.get());
                output.accept(BlockRegistry.SCARLET_TRAPDOOR.get());
                output.accept(BlockRegistry.SCARLET_BUTTON.get());
                output.accept(BlockRegistry.SCARLET_PRESSURE_PLATE.get());

                output.accept(ItemRegistry.DARK_CANDY.get());
                output.accept(BlockRegistry.DARK_CANDY_BLOCK.get());
                output.accept(BlockRegistry.DARK_CANDY_LEAVES.get());
                output.accept(BlockRegistry.DARK_CANDY_LOG.get());
                output.accept(BlockRegistry.DARK_CANDY_PLANKS.get());
                output.accept(BlockRegistry.DARK_CANDY_SLAB.get());
                output.accept(BlockRegistry.DARK_CANDY_STAIRS.get());
                output.accept(BlockRegistry.DARK_CANDY_DOOR.get());

                output.accept(BlockRegistry.SCARLET_MARBLE.get());
                output.accept(BlockRegistry.DARK_MARBLE.get());

                output.accept(BlockRegistry.SCARLET_MARBLE_PAWN.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_ROOK.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_KNIGHT.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_BISHOP.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_QUEEN.get());
                output.accept(BlockRegistry.SCARLET_MARBLE_KING.get());

                output.accept(BlockRegistry.DARK_MARBLE_PAWN.get());
                output.accept(BlockRegistry.DARK_MARBLE_ROOK.get());
                output.accept(BlockRegistry.DARK_MARBLE_KNIGHT.get());
                output.accept(BlockRegistry.DARK_MARBLE_BISHOP.get());
                output.accept(BlockRegistry.DARK_MARBLE_QUEEN.get());
                output.accept(BlockRegistry.DARK_MARBLE_KING.get());
            })
            .build()
    );
}
