package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.entity.AnchorOfLightPartBlockEntity;
import destiny.penumbra_phantasm.server.capability.AnchorOfLightCapability;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
        destroyAnchor(pLevel, pPlayer, pPos);

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    public void destroyAnchor(Level level, @Nullable Player player, BlockPos pos) {
        if (level.isClientSide()) return;

        if (level.getBlockEntity(pos) instanceof AnchorOfLightPartBlockEntity anchorPart) {
            BlockPos originPos = anchorPart.originPos;

            for (int i = 0; i < 5; i++) {
                BlockPos offsetPos = originPos.offset(0, i, 0);

                if (i == 0) {
                    level.destroyBlock(offsetPos, player == null || !player.isCreative());

                    AnchorOfLightCapability capability =  level.getCapability(CapabilityRegistry.ANCHOR_OF_LIGHT).resolve().orElse(null);

                    if (capability == null) return;

                    capability.anchors.remove(offsetPos);
                }
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (pLevel.isClientSide()) return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);

        if (!isOnBlock(pLevel, pPos)) {
            destroyAnchor((Level) pLevel, null, pPos);
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    public boolean isOnBlock(LevelAccessor pLevel, BlockPos pPos) {
        if (pLevel.getBlockEntity(pPos) instanceof AnchorOfLightPartBlockEntity part) {
            BlockPos originPos = part.originPos;

            BlockPos attachmentPos = originPos.offset(0, 5, 0);
            BlockState attachmentState = pLevel.getBlockState(attachmentPos);

            return attachmentState.isCollisionShapeFullBlock(pLevel, attachmentPos);
        } else {
            return false;
        }
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
