package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ScarletBushBlock extends Block implements SimpleWaterloggedBlock {
    public enum HoleStates implements StringRepresentable {
        NONE("none"),
        NORTH("north"),
        SOUTH("south"),
        WEST("west"),
        EAST("east");

        public final String name;

        HoleStates(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
    public static final BooleanProperty TALL = BooleanProperty.create("tall");
    public static final EnumProperty<HoleStates> HOLE = EnumProperty.create("hole", HoleStates.class);

    public ScarletBushBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(TALL, false).setValue(HOLE, HoleStates.NONE));

    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.5d, 0.5d, 0.5d));

        if (!state.getValue(HOLE).getSerializedName().equals("none")) return;

        double deltaX = entity.xOld - pos.getCenter().x;
        double deltaZ = entity.zOld - pos.getCenter().z;
        Direction entryDirection = Direction.getNearest(deltaX, 0d, deltaZ);

        if (!entryDirection.getAxis().isHorizontal()) return;

        if (level.getBlockState(pos.relative(entryDirection)).getBlock() instanceof ScarletBushBlock) return;

        level.setBlock(pos, state.setValue(HOLE, HoleStates.valueOf(entryDirection.getName().toUpperCase())), 2);

        level.playSound(null, pos, SoundEvents.AZALEA_BREAK, SoundSource.BLOCKS, 1f, 1f);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.getBlockState(pos.below()) == BlockRegistry.SCARLET_BUSH.get().defaultBlockState()) {
            level.setBlock(pos, state.setValue(TALL, true), 2);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(TALL);
        blockStateBuilder.add(HOLE);
        super.createBlockStateDefinition(blockStateBuilder);
    }
}
