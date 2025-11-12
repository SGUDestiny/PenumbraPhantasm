package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainOpeningBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DarkFountainOpeningBlock extends Block implements EntityBlock {
    public DarkFountainOpeningBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DarkFountainOpeningBlockEntity(blockPos, blockState);
    }
}
