package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.client.render.screen.GenericCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GenericCraftingTableBlock extends CraftingTableBlock {
    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

    public GenericCraftingTableBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        Block tableBlock = pLevel.getBlockState(pPos).getBlock();
        return new SimpleMenuProvider((p_52229_, p_52230_, p_52231_) -> new GenericCraftingMenu(p_52229_, p_52230_, ContainerLevelAccess.create(pLevel, pPos), tableBlock), CONTAINER_TITLE);
    }
}
