package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.item.RosegoldLighterItem;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static destiny.penumbra_phantasm.server.item.RosegoldLighterItem.OPEN;

public class IchorCandleBlock extends GenericHorizontalOrientableBlock{
    public static final VoxelShape SHAPE_GENERIC = ModUtil.buildShape(
            Block.box(6, 0, 6, 10, 1, 10),
            Block.box(7, 1, 7, 9, 7, 9)
    );

    public static final VoxelShape SHAPE_NORTH = ModUtil.buildShape(
            Block.box(6, 6, 12, 10, 7, 16),
            Block.box(7, 7, 13, 9, 13, 15)
    );
    public static final VoxelShape SHAPE_EAST = ModUtil.buildShape(
            Block.box(0, 6, 6, 4, 7, 10),
            Block.box(1, 7, 7, 3, 13, 9)
    );
    public static final VoxelShape SHAPE_SOUTH = ModUtil.buildShape(
            Block.box(6, 6, 0, 10, 7, 4),
            Block.box(7, 7, 1, 9, 13, 3)
    );
    public static final VoxelShape SHAPE_WEST = ModUtil.buildShape(
            Block.box(12, 6, 6, 16, 7, 10),
            Block.box(13, 7, 7, 15, 13, 9)
    );

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;

    public IchorCandleBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(LIT, false).setValue(ATTACHED, false));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        boolean lit = pState.getValue(LIT);

        if (lit) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(LIT, false));
            pLevel.playSound(null, pPos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);

            return InteractionResult.SUCCESS;
        } else {
            Item item = pPlayer.getItemInHand(pHand).getItem();

            if (item instanceof RosegoldLighterItem) {
                CompoundTag tag = pPlayer.getItemInHand(pHand).getTag();

                if (tag != null && tag.getBoolean(OPEN)) {
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(LIT, true));
                    pLevel.playSound(null, pPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(HORIZONTAL_FACING);
        boolean attached = pState.getValue(ATTACHED);

        if (!attached) {
            return SHAPE_GENERIC;
        }

        switch (direction) {
            case NORTH -> {
                return SHAPE_NORTH;
            }
            case SOUTH -> {
                return SHAPE_SOUTH;
            }
            case WEST -> {
                return SHAPE_WEST;
            }
            case EAST -> {
                return SHAPE_EAST;
            }
        }
        return SHAPE_GENERIC;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        Direction direction = pState.getValue(HORIZONTAL_FACING);
        boolean attached = pState.getValue(ATTACHED);
        Vec3 offset = new Vec3(0, 0, 0);

        if (!attached) {
            offset = new Vec3(0.5, 0.65, 0.5);

            if (pState.getValue(LIT)) {
                addParticlesAndSound(pLevel, new Vec3(pPos.getX() + offset.x, pPos.getY() + offset.y, pPos.getZ() + offset.z), pRandom);
                return;
            }
        }

        switch (direction) {
            case NORTH -> {
                offset = new Vec3(0.5, 1, 1 - 0.125);
            }
            case SOUTH -> {
                offset = new Vec3(0.5, 1, 0.125);
            }
            case WEST -> {
                offset = new Vec3(1 - 0.125, 1, 0.5);
            }
            case EAST -> {
                offset = new Vec3(0.125, 1, 0.5);
            }
        }

        if (pState.getValue(LIT)) {
            addParticlesAndSound(pLevel, new Vec3(pPos.getX() + offset.x, pPos.getY() + offset.y, pPos.getZ() + offset.z), pRandom);
        }
    }

    private static void addParticlesAndSound(Level pLevel, Vec3 pOffset, RandomSource pRandom) {
        float nextFloat = pRandom.nextFloat();

        if (nextFloat < 0.3F) {
            pLevel.addParticle(ParticleTypes.SMOKE, pOffset.x, pOffset.y, pOffset.z, (double)0.0F, (double)0.0F, (double)0.0F);

            if (nextFloat < 0.17F) {
                pLevel.playLocalSound(pOffset.x + (double)0.5F, pOffset.y + (double)0.5F, pOffset.z + (double)0.5F, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + pRandom.nextFloat(), pRandom.nextFloat() * 0.7F + 0.3F, false);
            }
        }

        pLevel.addParticle(ParticleTypeRegistry.ICHOR_FIRE_FLAME.get(), pOffset.x, pOffset.y, pOffset.z, (double)0.0F, (double)0.0F, (double)0.0F);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
        pBuilder.add(LIT);
        pBuilder.add(ATTACHED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState state = this.defaultBlockState();
        Direction direction = pContext.getClickedFace();

        if (direction != Direction.UP && direction != Direction.DOWN) {
            state = state.setValue(ATTACHED, true).setValue(HORIZONTAL_FACING, direction);
        } else {
            state = state.setValue(ATTACHED, false).setValue(HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
        }

        return state;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (!isOnBlock(pLevel, pPos, pState)) {
            pLevel.destroyBlock(pPos, true);
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    public boolean isOnBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        Direction blockDirection = pState.getValue(HORIZONTAL_FACING);
        boolean attached = pState.getValue(ATTACHED);
        Block parentBlock = pLevel.getBlockState(pPos.relative(blockDirection.getOpposite())).getBlock();

        if (!attached) {
            parentBlock = pLevel.getBlockState(pPos.below()).getBlock();
        }

        return !parentBlock.equals(Blocks.AIR);
    }
}
