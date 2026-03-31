package destiny.penumbra_phantasm.server.datagen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.datagen.BlockFamilyDatagenSets.BlockFamilyDatagenSet;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, PenumbraPhantasm.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (BlockFamilyDatagenSet family : BlockFamilyDatagenSets.ALL) {
            generateFamily(family);
        }
    }

    private void generateFamily(BlockFamilyDatagenSet family) {
        if (family.baseBlock() != null) {
            simpleBlockWithItem(family.baseBlock().get(), cubeAll(family.baseBlock().get()));
        }
        if (family.door() != null && family.door().get() instanceof DoorBlock doorBlock) {
            doorBlockWithRenderType(doorBlock, family.doorBottomTexture(), family.doorTopTexture(), "cutout");
        }
        if (family.trapdoor() != null && family.trapdoor().get() instanceof TrapDoorBlock trapDoorBlock) {
            trapdoorBlockWithRenderType(trapDoorBlock, family.trapdoorTexture(), true, "cutout");
        }
        if (family.fenceGate() != null && family.fenceGate().get() instanceof FenceGateBlock fenceGateBlock) {
            fenceGateBlock(fenceGateBlock, family.baseTexture());
        }
        if (family.fence() != null && family.fence().get() instanceof FenceBlock fenceBlock) {
            fenceBlock(fenceBlock, family.baseTexture());
        }
        if (family.wall() != null && family.wall().get() instanceof WallBlock wallBlock) {
            wallBlock(wallBlock, family.baseTexture());
        }
        if (family.stairs() != null && family.stairs().get() instanceof StairBlock stairBlock) {
            stairsBlock(stairBlock, family.baseTexture());
        }
        if (family.slab() != null && family.slab().get() instanceof SlabBlock slabBlock) {
            slabBlock(slabBlock, family.baseTexture(), family.baseTexture());
        }
        if (family.button() != null && family.button().get() instanceof ButtonBlock buttonBlock) {
            buttonBlock(buttonBlock, family.baseTexture());
        }
        if (family.pressurePlate() != null && family.pressurePlate().get() instanceof PressurePlateBlock pressurePlateBlock) {
            pressurePlateBlock(pressurePlateBlock, family.baseTexture());
        }
    }

    public static String name(RegistryObject<? extends Block> block) {
        ResourceLocation id = block.getId();
        return id == null ? "" : id.getPath();
    }
}
