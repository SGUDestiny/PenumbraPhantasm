package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.block.entity.ScarletMarbleDiceBlockEntity;
import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

public class ScarletMarbleDiceBlock extends BaseEntityBlock {
    public ScarletMarbleDiceBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            if (pLevel.getBlockEntity(pPos) instanceof ScarletMarbleDiceBlockEntity dice) {
                dice.rotationX = pLevel.getRandom().nextInt(4) * 90;
                dice.rotationY = pLevel.getRandom().nextInt(4) * 90;
                dice.rotationZ = pLevel.getRandom().nextInt(4) * 90;

                dice.markUpdated();

                pLevel.updateNeighborsAt(pPos, this);
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }

        pLevel.playSound(null, pPos, SoundRegistry.DICE_THROW.get(), SoundSource.BLOCKS, 1f, 1f);
        spawnDestroyParticles(pLevel, pPlayer, pPos, pState);

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof ScarletMarbleDiceBlockEntity dice))
            return 0;

        Quaternionf rotation = new Quaternionf().rotateX((float) Math.toRadians(dice.rotationX)).rotateY((float) Math.toRadians(dice.rotationY))
                .rotateZ((float) Math.toRadians(dice.rotationZ));

        // Default face directions
        Map<Integer, Vector3f> faces = Map.of(
                1, new Vector3f(0, 0, -1), // north
                6, new Vector3f(0, 0, 1),  // south
                2, new Vector3f(1, 0, 0),  // east
                5, new Vector3f(-1, 0, 0), // west
                4, new Vector3f(0, 1, 0),  // up
                3, new Vector3f(0, -1, 0)  // down
        );

        int topFace = 0;
        float highestY = -999f;

        for (Map.Entry<Integer, Vector3f> entry : faces.entrySet()) {
            Vector3f rotated = new Vector3f(entry.getValue());
            rotated.rotate(rotation);

            if (rotated.y > highestY) {
                highestY = rotated.y;
                topFace = entry.getKey();
            }
        }

        return topFace;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getSignal(state, level, pos, Direction.UP);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.SCARLET_MARBLE_DICE_BLOCK_ENTITY.get().create(blockPos, blockState);
    }
}