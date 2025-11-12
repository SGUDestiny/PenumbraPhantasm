package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.blockentity.DarkFountainOpeningBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DarkFountainOpeningBlock extends BaseEntityBlock {
    public DarkFountainOpeningBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DarkFountainOpeningBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntityRegistry.DARK_FOUNTAIN_OPENING.get(), DarkFountainOpeningBlockEntity::clientTick);
    }
}
