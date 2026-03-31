package destiny.penumbra_phantasm.server.datagen;

import destiny.penumbra_phantasm.server.datagen.BlockFamilyDatagenSets.BlockFamilyDatagenSet;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ModLootTableProvider {
    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK))
        );
    }

    private static class ModBlockLootSubProvider extends BlockLootSubProvider {
        protected ModBlockLootSubProvider() {
            super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            for (BlockFamilyDatagenSet family : BlockFamilyDatagenSets.ALL) {
                addDrops(family.baseBlock());
                addDrops(family.door());
                addDrops(family.trapdoor());
                addDrops(family.fenceGate());
                addDrops(family.fence());
                addDrops(family.wall());
                addDrops(family.stairs());
                addDrops(family.slab());
                addDrops(family.button());
                addDrops(family.pressurePlate());
            }
        }

        private void addDrops(Block block) {
            if (block instanceof SlabBlock slabBlock) {
                add(slabBlock, createSlabItemTable(slabBlock));
                return;
            }
            dropSelf(block);
        }

        private void addDrops(net.minecraftforge.registries.RegistryObject<? extends Block> block) {
            if (block != null) {
                addDrops(block.get());
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BlockFamilyDatagenSets.ALL.stream()
                    .flatMap(this::familyBlocks)
                    .toList();
        }

        private Stream<Block> familyBlocks(BlockFamilyDatagenSet family) {
            return Stream.of(
                    family.baseBlock(),
                    family.door(),
                    family.trapdoor(),
                    family.fenceGate(),
                    family.fence(),
                    family.wall(),
                    family.stairs(),
                    family.slab(),
                    family.button(),
                    family.pressurePlate()
            ).filter(java.util.Objects::nonNull).map(net.minecraftforge.registries.RegistryObject::get);
        }
    }
}
