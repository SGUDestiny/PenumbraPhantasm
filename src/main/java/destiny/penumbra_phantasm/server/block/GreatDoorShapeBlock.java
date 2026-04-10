package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.entity.GreatDoorShapeBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GreatDoorShapeBlock extends BaseEntityBlock {
    public static final BooleanProperty IS_DOOR_OPEN = BooleanProperty.create("is_door_open");

    public GreatDoorShapeBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(IS_DOOR_OPEN, false));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer,
                                          @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof GreatDoorShapeBlockEntity greatDoorShape) {
            return greatDoorShape.cycleGreatDoorState(pLevel, pPos, greatDoorShape, pPlayer);
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pState.getValue(IS_DOOR_OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState pState, @NotNull Fluid pFluid) {
        return false;
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState pState, @NotNull BlockPlaceContext pUseContext) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(IS_DOOR_OPEN);
        super.createBlockStateDefinition(stateBuilder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return BlockEntityRegistry.GREAT_DOOR_SHAPE_BLOCK_ENTITY.get().create(blockPos, blockState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityRegistry.GREAT_DOOR_SHAPE_BLOCK_ENTITY.get(), GreatDoorShapeBlockEntity::tick);
    }
}
