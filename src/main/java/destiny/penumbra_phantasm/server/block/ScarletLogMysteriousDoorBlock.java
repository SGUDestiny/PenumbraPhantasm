package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomManager;
import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ScarletLogMysteriousDoorBlock extends HorizontalDirectionalBlock {
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public ScarletLogMysteriousDoorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false)
				.setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, OPEN, HALF);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!DarkWorldUtil.isDarkWorld(level) || !CardKingdomEggRoomUtil.isCardKingdomPlayable(level)) {
			return InteractionResult.SUCCESS;
		}
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		boolean open = !state.getValue(OPEN);
		setOpen(level, pos, state, open);

		level.playSound(null, pos, open ? SoundEvents.CHERRY_WOOD_DOOR_OPEN : SoundEvents.CHERRY_WOOD_DOOR_CLOSE, SoundSource.BLOCKS, 1, 1);
		return InteractionResult.CONSUME;
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level.isClientSide || !state.getValue(OPEN) || !(entity instanceof Player player)) {
			return;
		}
		if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
			return;
		}
		if (!DarkWorldUtil.isDarkWorld(level) || !CardKingdomEggRoomUtil.isCardKingdomPlayable(level)) {
			return;
		}
		if (!isEnteringFromFront(state, pos, player)) {
			return;
		}
		CardKingdomEggRoomManager.enterFromDoor(player, level, lowerPos(pos, state));
	}

	private static boolean isEnteringFromFront(BlockState state, BlockPos pos, Player player) {
		Direction facing = state.getValue(FACING);
		double dx = player.getX() - (pos.getX() + 0.5);
		double dz = player.getZ() - (pos.getZ() + 0.5);
		return dx * facing.getStepX() + dz * facing.getStepZ() > 0.05;
	}

	public static void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
		BlockPos lower = lowerPos(pos, state);
		BlockState lowerState = level.getBlockState(lower);
		BlockState upperState = level.getBlockState(lower.above());
		if (lowerState.getBlock() instanceof ScarletLogMysteriousDoorBlock) {
			level.setBlock(lower, lowerState.setValue(OPEN, open), 3);
		}

		if (upperState.getBlock() instanceof ScarletLogMysteriousDoorBlock) {
			level.setBlock(lower.above(), upperState.setValue(OPEN, open), 3);
		}
	}

	public static BlockPos lowerPos(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();

		if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(OPEN, false).setValue(HALF, DoubleBlockHalf.LOWER);
		}

		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		BlockState otherState = level.getBlockState(other);

		if (otherState.getBlock() instanceof ScarletLogMysteriousDoorBlock) {
			level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
			level.levelEvent(player, 2001, other, Block.getId(otherState));
		}

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
			return neighborState.getBlock() instanceof ScarletLogMysteriousDoorBlock && neighborState.getValue(HALF) != half
					? state.setValue(OPEN, neighborState.getValue(OPEN)).setValue(FACING, neighborState.getValue(FACING))
					: Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
