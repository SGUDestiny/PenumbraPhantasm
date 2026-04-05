package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.entity.DarknessBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DarknessBlock extends BaseEntityBlock {
    public DarknessBlock(Properties properties) {
        super(properties);
    }

    public static boolean getDoorOpenState(Level level, BlockPos doorPos, BlockState doorState) {
        if (!(doorState.getBlock() instanceof DoorBlock)) return false;
        if (doorState.getValue(DoorBlock.OPEN)) return true;
        BlockPos otherHalf = doorState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                ? doorPos.above()
                : doorPos.below();
        BlockState other = level.getBlockState(otherHalf);
        return other.getBlock() instanceof DoorBlock && other.getValue(DoorBlock.OPEN);
    }

    public static boolean isDoorVisuallyOpenFromSide(Level level, BlockPos doorPos, BlockState doorState, Direction fromDoorToRoom) {
        boolean open = getDoorOpenState(level, doorPos, doorState);
        Direction facing = doorState.getValue(DoorBlock.FACING);
        boolean facingParallelToRoom = facing.getAxis() == fromDoorToRoom.getAxis();
        return facingParallelToRoom ? open : !open;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighbor = level.getBlockState(neighborPos);

            boolean shouldSpawn = false;
            Direction particleDirection = dir;

            if (neighbor.is(Blocks.AIR) || neighbor.is(Blocks.CAVE_AIR) || neighbor.is(Blocks.VOID_AIR)) {
                shouldSpawn = true;
            } else if (neighbor.getBlock() instanceof DoorBlock) {
                Direction fromDoorToRoom = dir.getOpposite();
                if (isDoorVisuallyOpenFromSide(level, neighborPos, neighbor, fromDoorToRoom)) {
                    BlockPos beyondDoor = neighborPos.relative(dir);
                    if (!(level.getBlockState(beyondDoor).getBlock() instanceof DarknessBlock)) {
                        shouldSpawn = true;
                        particleDirection = dir;
                    }
                }
            }

            if (!shouldSpawn) continue;

            double px = pos.getX() + 0.5 + dir.getStepX() * 0.4;
            double py = pos.getY() + 0.5 + dir.getStepY() * 0.4;
            double pz = pos.getZ() + 0.5 + dir.getStepZ() * 0.4;

            double baseSpeed = 0.15;
            double vx = particleDirection.getStepX() * baseSpeed + (random.nextDouble() - 0.5) * 0.02;
            double vy = particleDirection.getStepY() * baseSpeed + random.nextDouble() * 0.02;
            double vz = particleDirection.getStepZ() * baseSpeed + (random.nextDouble() - 0.5) * 0.02;

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), px, py, pz, 1, vx, vy, vz, 0);
            } else {
                level.addParticle(ParticleTypeRegistry.FOUNTAIN_DARKNESS.get(), px, py, pz, vx, vy, vz);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.getBlock() instanceof DarknessBlock || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.DARKNESS_BLOCK_ENTITY.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityRegistry.DARKNESS_BLOCK_ENTITY.get(), DarknessBlockEntity::tick);
    }
}
