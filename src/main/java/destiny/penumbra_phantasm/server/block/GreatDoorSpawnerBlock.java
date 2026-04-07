package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class GreatDoorSpawnerBlock extends HorizontalDirectionalBlock {
    public GreatDoorSpawnerBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        pPlayer.displayClientMessage(Component.literal("Trying to do the thing"), true);

        GreatDoorCapability greatDoorCapability = null;
        LazyOptional<GreatDoorCapability> lightLazyCapability = pLevel.getCapability(CapabilityRegistry.GREAT_DOOR);
        if(lightLazyCapability.isPresent() && lightLazyCapability.resolve().isPresent())
            greatDoorCapability = lightLazyCapability.resolve().get();

        if (greatDoorCapability == null) {
            return InteractionResult.FAIL;
        }

        Direction facing = pState.getValue(FACING);
        greatDoorCapability.addGreatDoor(pPos, facing, true, pPos, pLevel.dimension());

        Direction widthDir = facing.getClockWise();
        Direction depthDir = facing.getOpposite();
        BlockState block = BlockRegistry.GREAT_DOOR_SHAPE.get().defaultBlockState();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 6; x++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos target = pPos.relative(widthDir, x).relative(depthDir, z).above(y);
                    pLevel.setBlock(target, block, 3);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(FACING);
        super.createBlockStateDefinition(stateBuilder);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
}
