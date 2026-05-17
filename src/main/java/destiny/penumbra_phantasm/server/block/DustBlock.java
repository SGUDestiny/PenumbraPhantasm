package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static destiny.penumbra_phantasm.server.block.GenericHorizontalOrientableBlock.HORIZONTAL_FACING;

public class DustBlock extends BaseEntityBlock {
    public static final IntegerProperty ANIMATION_OFFSET = IntegerProperty.create("animation_offset", 0, 3);
    public static final BooleanProperty SPAWN_PARTICLES = BooleanProperty.create("spawn_particles");

    public DustBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(ANIMATION_OFFSET, 1).setValue(SPAWN_PARTICLES, false).setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ANIMATION_OFFSET);
        pBuilder.add(SPAWN_PARTICLES);
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(SPAWN_PARTICLES)) {
            pLevel.addParticle(new DustParticleOptions(new Vector3f(1f, 1f, 1f), 1.25f), pPos.getX() + 0.5, pPos.getY() + 1, pPos.getZ() + 0.5, 0, 15, 0);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();

        BlockState blockState = this.defaultBlockState().setValue(HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
        if (pContext.getPlayer().isCrouching()) {
            blockState.setValue(ANIMATION_OFFSET, 0);
        } else  {
            blockState.setValue(ANIMATION_OFFSET, level.random.nextInt(1, 3));
        }

        int neighborCount = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(dir)).is(BlockRegistry.DUST_BLOCK.get())) {
                neighborCount++;
            }
        }

        if (neighborCount == 4) {
            blockState.setValue(SPAWN_PARTICLES, true);
        }

        return blockState;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.DUST_BLOCK_ENTITY.get().create(blockPos, blockState);
    }
}
