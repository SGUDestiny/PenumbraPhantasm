package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.client.network.ClientBoundFireDoorPacket;
import destiny.penumbra_phantasm.server.block.entity.FireDoorBlockEntity;
import destiny.penumbra_phantasm.server.capability.FireDoorsCapability;
import destiny.penumbra_phantasm.server.fountain.FireDoor;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FireDoorBlock extends BaseEntityBlock {
    public static final VoxelShape SOUTH_AABB = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 3.0F);
    public static final VoxelShape NORTH_AABB = Block.box(0.0F, 0.0F, 13.0F, 16.0F, 16.0F, 16.0F);
    public static final VoxelShape WEST_AABB = Block.box(13.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F);
    public static final VoxelShape EAST_AABB = Block.box(0.0F, 0.0F, 0.0F, 3.0F, 16.0F, 16.0F);

    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public FireDoorBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER).setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
        pBuilder.add(OPEN);
        pBuilder.add(HALF);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = pState.getValue(HORIZONTAL_FACING);

        return switch (facing) {
            case EAST -> EAST_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            default -> NORTH_AABB;
        };
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer,
                                 InteractionHand pHand, BlockHitResult pHit) {
        if (!DarkWorldUtil.isDarkWorld(pLevel)) {
            pPlayer.displayClientMessage(Component.translatable("message.penumbra_phantasm.fire_door_not_in_dark_world"), true);
            return InteractionResult.SUCCESS;
        }

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            FireDoorsCapability cap = serverPlayer.getCapability(CapabilityRegistry.FIRE_DOORS).orElse(null);

            List<FireDoor> allDoors = cap.playerFireDoors;
            ResourceKey<Level> currentDim = pLevel.dimension();

            List<FireDoor> sameDimDoors = new ArrayList<>();
            List<FireDoor> invalidDoors = new ArrayList<>();

            for (FireDoor fd : allDoors) {
                if (!fd.darkWorld().equals(currentDim)) continue;

                if (pLevel.getBlockState(fd.doorPos()).getBlock() == this) {
                    sameDimDoors.add(fd);
                } else {
                    invalidDoors.add(fd);
                }
            }

            allDoors.removeAll(invalidDoors);

            BlockPos lowerPos = pState.getValue(HALF) == DoubleBlockHalf.UPPER ? pPos.below() : pPos;
            BlockEntity be = pLevel.getBlockEntity(lowerPos);
            if (!(be instanceof FireDoorBlockEntity fireDoorEntity)) return InteractionResult.FAIL;

            int doorIndex = cap.findDoorIndexInList(currentDim, lowerPos);
            boolean hasDoor = doorIndex != -1;

            if (pPlayer.isCrouching()) {
                if (hasDoor) {
                    pPlayer.displayClientMessage(Component.translatable("message.penumbra_phantasm.fire_door_unlink"), true);
                    cap.removeFireDoor(currentDim, lowerPos);
                }
                return InteractionResult.SUCCESS;
            }

            if (!hasDoor) {
                int sameWorldCount = sameDimDoors.size();
                if (sameWorldCount >= 10) {
                    pPlayer.displayClientMessage(Component.translatable("message.penumbra_phantasm.fire_door_limit_reached"), true);
                } else {
                    pPlayer.displayClientMessage(Component.translatable("message.penumbra_phantasm.fire_door_link"), true);
                    Component name = fireDoorEntity.getCustomName() != null ? fireDoorEntity.getCustomName() : Component.translatable("block.penumbra_phantasm.fire_door");
                    cap.addFireDoor(currentDim, lowerPos, pState.getValue(HORIZONTAL_FACING).toYRot(), name);
                }
                return InteractionResult.SUCCESS;
            }

            FireDoor currentDoor = cap.getDoorFromIndex(doorIndex);
            List<FireDoor> finalList = sameDimDoors.stream().filter(d -> !d.equals(currentDoor)).toList();

            if (finalList.isEmpty()) {
                pPlayer.displayClientMessage(Component.translatable("message.penumbra_phantasm.fire_door_not_enough_doors"), true);
                return InteractionResult.FAIL;
            }

            fireDoorEntity.setDoorState(pLevel, lowerPos, true);
            fireDoorEntity.incrementOpenCount();

            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new ClientBoundFireDoorPacket(finalList, currentDim, lowerPos));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        DoubleBlockHalf half = pState.getValue(HALF);

        if (pFacing.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && pFacing == Direction.UP) {
                return pFacingState.is(this) && pFacingState.getValue(HALF) == DoubleBlockHalf.UPPER
                        ? pState.setValue(HORIZONTAL_FACING, pFacingState.getValue(HORIZONTAL_FACING)).setValue(OPEN, pFacingState.getValue(OPEN))
                        : Blocks.AIR.defaultBlockState();
            }
            if (half == DoubleBlockHalf.UPPER && pFacing == Direction.DOWN) {
                return pFacingState.is(this) && pFacingState.getValue(HALF) == DoubleBlockHalf.LOWER
                        ? pState.setValue(HORIZONTAL_FACING, pFacingState.getValue(HORIZONTAL_FACING)).setValue(OPEN, pFacingState.getValue(OPEN))
                        : Blocks.AIR.defaultBlockState();
            }
        }

        if (half == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        if (pState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return canSupportCenter(pLevel, pPos.below(), Direction.UP);
        } else {
            BlockState belowState = pLevel.getBlockState(pPos.below());
            return belowState.is(this) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock() && !movedByPiston && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos lowerPos = pos;
            BlockEntity blockEntity = level.getBlockEntity(lowerPos);

            if (blockEntity instanceof FireDoorBlockEntity fireDoor) {
                if (!fireDoor.droppedByPlayer) {
                    ItemStack itemStack = new ItemStack(this.asItem());
                    fireDoor.saveToItem(itemStack);

                    if (fireDoor.hasCustomName()) {
                        itemStack.setHoverName(fireDoor.getCustomName());
                    }

                    popResource(level, lowerPos, itemStack);
                }
                fireDoor.droppedByPlayer = false;
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockEntity blockEntity = level.getBlockEntity(lowerPos);

        if (blockEntity instanceof FireDoorBlockEntity fireDoor) {
            if (!level.isClientSide() && !player.isCreative()) {
                fireDoor.droppedByPlayer = true;

                ItemStack itemStack = new ItemStack(this.asItem());
                blockEntity.saveToItem(itemStack);

                if (fireDoor.hasCustomName()) {
                    itemStack.setHoverName(fireDoor.getCustomName());
                }

                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack);
                itemEntity.setDefaultPickUpDelay();

                level.addFreshEntity(itemEntity);
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            FireDoorsCapability cap = serverPlayer.getCapability(CapabilityRegistry.FIRE_DOORS).orElse(null);
            if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                cap.removeFireDoor(pLevel.dimension(), pPos.below());
            } else {
                cap.removeFireDoor(pLevel.dimension(), pPos);
            }
        }

        super.playerDestroy(pLevel, pPlayer, pPos, pState, pBlockEntity, pTool);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();

        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(pContext)) {
            return this.defaultBlockState()
                    .setValue(HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos upperPos = pos.above();
            level.setBlock(upperPos, this.defaultBlockState()
                    .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING))
                    .setValue(HALF, DoubleBlockHalf.UPPER)
                    .setValue(OPEN, false), Block.UPDATE_ALL);
        }

        if (stack.hasCustomHoverName()) {
            BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            if (level.getBlockEntity(lowerPos) instanceof FireDoorBlockEntity be) {
                be.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof FireDoorBlockEntity be && be.getCustomName() != null) {
            stack.setHoverName(be.getCustomName());
        }
        return stack;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? new FireDoorBlockEntity(blockPos, blockState) : null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntityRegistry.FIRE_DOOR_BLOCK_ENTITY.get(), FireDoorBlockEntity::tick);
    }
}