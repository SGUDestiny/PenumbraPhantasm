package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.entity.AnchorOfLightBlockEntity;
import destiny.penumbra_phantasm.server.capability.AnchorOfLightCapability;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class AnchorOfLightBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AnchorOfLightBlock(Properties pProperties) {
        super(pProperties);
        this.defaultBlockState().setValue(FACING, Direction.NORTH);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(FACING);
        super.createBlockStateDefinition(stateBuilder);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        AnchorOfLightCapability capability =  pLevel.getCapability(CapabilityRegistry.ANCHOR_OF_LIGHT).resolve().orElse(null);

        if (capability == null) return;

        capability.anchors.add(pPos);

        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        AnchorOfLightCapability capability =  pLevel.getCapability(CapabilityRegistry.ANCHOR_OF_LIGHT).resolve().orElse(null);

        if (capability == null) return;

        capability.anchors.remove(pPos);

        if (pLevel.isClientSide()) return;

        for (int i = 0; i < 5; i++) {
            BlockPos offsetPos = pPos.offset(0, i, 0);

            pLevel.destroyBlock(offsetPos, i == 0 && !pPlayer.isCreative());
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.ANCHOR_OF_LIGHT_BLOCK_ENTITY.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityRegistry.ANCHOR_OF_LIGHT_BLOCK_ENTITY.get(), AnchorOfLightBlockEntity::tick);
    }
}
