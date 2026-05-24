package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public class CliffrockSlideBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    //private static final String SOUND_COOLDOWN = "penumbra_phantasm.cliffrock_slide_sound_cooldown";

    private static final VoxelShape SHAPE_NORTH = Shapes.create(0, 0, (1 / 16.0) * 2, 1, 1, 1);
    private static final VoxelShape SHAPE_SOUTH = Shapes.create(0, 0, 0, 1, 1, 14/16.0);
    private static final VoxelShape SHAPE_WEST = Shapes.create((1/16.0) * 2, 0, 0, 1, 1, 1);
    private static final VoxelShape SHAPE_EAST = Shapes.create(0, 0, 0, 14/16.0, 1, 1);

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
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> Shapes.block();
        };
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide() || !(entity instanceof LivingEntity)) {
            super.stepOn(level, pos, state, entity);
            return;
        }

        entity.setDeltaMovement(Vec3.ZERO);

        if (level.getBlockState(pos.above()).is(this)) {
            super.stepOn(level, pos, state, entity);
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockPos targetPos = pos.relative(facing);

        double backOff = 0.3;
        double targetX = targetPos.getX() + 0.5 - facing.getStepX() * backOff;
        double targetY = targetPos.getY() + 1.0;
        double targetZ = targetPos.getZ() + 0.5 - facing.getStepZ() * backOff;

        entity.teleportTo(targetX, targetY, targetZ);
        entity.fallDistance = 0.0F;

        level.playSound(null, pos, SoundRegistry.SLIDE_DOWN.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(1.0d, 1.0d, 1.0d));
        entity.fallDistance = 0.0F;
    }
}
