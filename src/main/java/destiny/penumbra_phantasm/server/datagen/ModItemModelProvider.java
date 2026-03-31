package destiny.penumbra_phantasm.server.datagen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.datagen.BlockFamilyDatagenSets.BlockFamilyDatagenSet;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PenumbraPhantasm.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (BlockFamilyDatagenSet family : BlockFamilyDatagenSets.ALL) {
            generateFamily(family);
        }
    }

    private void generateFamily(BlockFamilyDatagenSet family) {
        if (family.baseBlock() != null) {
            simpleBlockItem(family.baseBlock());
        }
        if (family.door() != null && family.door().get() instanceof DoorBlock) {
            String name = ModBlockStateProvider.name(family.door());
            withExistingParent(name, "item/generated").texture("layer0", family.doorBottomTexture());
        }
        if (family.trapdoor() != null && family.trapdoor().get() instanceof TrapDoorBlock) {
            String name = ModBlockStateProvider.name(family.trapdoor());
            withExistingParent(name, modLoc("block/" + name + "_bottom"));
        }
        if (family.fenceGate() != null) {
            simpleBlockItem(family.fenceGate());
        }
        if (family.fence() != null && family.fence().get() instanceof FenceBlock) {
            String name = ModBlockStateProvider.name(family.fence());
            withExistingParent(name, modLoc("block/" + name + "_inventory"));
        }
        if (family.wall() != null && family.wall().get() instanceof WallBlock) {
            String name = ModBlockStateProvider.name(family.wall());
            withExistingParent(name, modLoc("block/" + name + "_inventory"));
        }
        if (family.stairs() != null && family.stairs().get() instanceof StairBlock) {
            simpleBlockItem(family.stairs());
        }
        if (family.slab() != null && family.slab().get() instanceof SlabBlock) {
            simpleBlockItem(family.slab());
        }
        if (family.button() != null && family.button().get() instanceof ButtonBlock) {
            String name = ModBlockStateProvider.name(family.button());
            withExistingParent(name, modLoc("block/" + name + "_inventory"));
        }
        if (family.pressurePlate() != null) {
            simpleBlockItem(family.pressurePlate());
        }
    }

    private void simpleBlockItem(RegistryObject<? extends Block> block) {
        String name = ModBlockStateProvider.name(block);
        withExistingParent(name, modLoc("block/" + name));
    }
}
