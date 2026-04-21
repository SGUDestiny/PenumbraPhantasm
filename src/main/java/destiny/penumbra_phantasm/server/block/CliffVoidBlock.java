package destiny.penumbra_phantasm.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CliffVoidBlock extends Block {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    private static final String CLIFF_VOID_TICK_TAG = "penumbra_phantasm.cliff_void_tick";
    private static final float CLIFF_VOID_DAMAGE = 4.0F;
    private static final int DARKNESS_DURATION = 5 * 20;
    private static final int DARKNESS_REFRESH_THRESHOLD = 2 * 20;

    public CliffVoidBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP);
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
    public boolean skipRendering(BlockState state, BlockState adjacentState, net.minecraft.core.Direction direction) {
        return adjacentState.getBlock() instanceof CliffVoidBlock || super.skipRendering(state, adjacentState, direction);
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
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return;
        }
        long gameTime = level.getGameTime();
        if (entity.getPersistentData().getLong(CLIFF_VOID_TICK_TAG) == gameTime) {
            return;
        }
        entity.getPersistentData().putLong(CLIFF_VOID_TICK_TAG, gameTime);
        if (!entity.isInvulnerableTo(level.damageSources().fellOutOfWorld())) {
            entity.hurt(level.damageSources().fellOutOfWorld(), CLIFF_VOID_DAMAGE);
        }
        if (entity instanceof LivingEntity livingEntity) {
            MobEffectInstance current = livingEntity.getEffect(MobEffects.DARKNESS);
            if (current == null || current.getDuration() <= DARKNESS_REFRESH_THRESHOLD) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_DURATION, 0, true, true, true));
            }
        }
        super.entityInside(state, level, pos, entity);
    }
}
