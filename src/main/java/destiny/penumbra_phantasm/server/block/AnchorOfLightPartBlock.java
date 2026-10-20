package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.entity.AnchorOfLightPartBlockEntity;
import destiny.penumbra_phantasm.server.capability.AnchorOfLightCapability;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AnchorOfLightPartBlock extends BaseEntityBlock {
    public AnchorOfLightPartBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (pLevel.isClientSide()) return;

        if (pLevel.getBlockEntity(pPos) instanceof AnchorOfLightPartBlockEntity anchorPart) {
            BlockPos originPos = anchorPart.originPos;

            for (int i = 0; i < 5; i++) {
                BlockPos offsetPos = originPos.offset(0, i, 0);

                if (i == 0) {
                    pLevel.destroyBlock(offsetPos, !pPlayer.isCreative());

                    AnchorOfLightCapability capability =  pLevel.getCapability(CapabilityRegistry.ANCHOR_OF_LIGHT).resolve().orElse(null);

                    if (capability == null) return;

                    capability.anchors.remove(offsetPos);
                }
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.ANCHOR_OF_LIGHT_PART_BLOCK_ENTITY.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityRegistry.ANCHOR_OF_LIGHT_PART_BLOCK_ENTITY.get(), AnchorOfLightPartBlockEntity::tick);
    }
}
