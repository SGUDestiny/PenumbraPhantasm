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
            .icon(() -> BlockRegistry.NIGHT_GRASS_BLOCK.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.penumbra_phantasm.main"))
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.GOLD_KNIFE.get());
                output.accept(ItemRegistry.IRON_KNIFE.get());
                output.accept(ItemRegistry.DIAMOND_KNIFE.get());
                output.accept(ItemRegistry.NETHERITE_KNIFE.get());
                output.accept(ItemRegistry.REAL_KNIFE.get());
                output.accept(ItemRegistry.BLACK_KNIFE.get());

                output.accept(ItemRegistry.DARK_CANDY.get());
                output.accept(ItemRegistry.FRIEND.get());

                output.accept(BlockRegistry.SCARLET_LEAVES.get());
                output.accept(BlockRegistry.SCARLET_LOG.get());
                output.accept(BlockRegistry.SCARLET_SAPLING.get());

                output.accept(BlockRegistry.NIGHT_GRASS.get());
                output.accept(BlockRegistry.NIGHT_GRASS_BLOCK.get());
                output.accept(BlockRegistry.NIGHT_DIRT.get());
                output.accept(BlockRegistry.UMBRASTONE.get());
                output.accept(BlockRegistry.COBBLED_UMBRASTONE.get());

                output.accept(BlockRegistry.DARK_FOUNTAIN_OPENING.get());
                output.accept(BlockRegistry.DARK_FOUNTAIN.get());
                output.accept(BlockRegistry.DARK_FOUNTAIN_FULL.get());
            })
            .build()
    );
}
