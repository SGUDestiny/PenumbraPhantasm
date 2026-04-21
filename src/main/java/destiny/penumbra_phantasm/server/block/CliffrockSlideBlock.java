package destiny.penumbra_phantasm.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class CliffrockSlideBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final String SLIDE_COOLDOWN_TAG = "penumbra_phantasm.cliffrock_slide_cooldown";

    public CliffrockSlideBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide() || !(entity instanceof LivingEntity)) {
            super.stepOn(level, pos, state, entity);
            return;
        }
        if (level.getGameTime() < entity.getPersistentData().getLong(SLIDE_COOLDOWN_TAG)) {
            super.stepOn(level, pos, state, entity);
            return;
        }
        if (level.getBlockState(pos.above()).is(this)) {
            super.stepOn(level, pos, state, entity);
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (level.getBlockState(cursor.below()).is(this)) {
            cursor.move(Direction.DOWN);
        }

        BlockPos landingPos = cursor.relative(facing);
        double targetX = landingPos.getX() + 0.5D;
        double targetY = level.getBlockState(landingPos).isAir() ? landingPos.getY() : landingPos.getY() + 1.0D;
        double targetZ = landingPos.getZ() + 0.5D;

        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(targetX, targetY, targetZ);
        } else {
            entity.teleportTo(targetX, targetY, targetZ);
        }
        entity.fallDistance = 0.0F;
        entity.setDeltaMovement(0.0D, -0.1D, 0.0D);
        entity.getPersistentData().putLong(SLIDE_COOLDOWN_TAG, level.getGameTime() + 10L);

        super.stepOn(level, pos, state, entity);
    }
}
