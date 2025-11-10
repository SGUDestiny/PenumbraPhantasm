package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CrimsonLeavesBlock extends LeavesBlock {
    public CrimsonLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource randomSource) {
        super.animateTick(state, level, pos, randomSource);
        if (randomSource.nextInt(20) == 0) {
            BlockPos posBelow = pos.below();
            BlockState stateBelow = level.getBlockState(posBelow);
            if (!isFaceFull(stateBelow.getCollisionShape(level, posBelow), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(level, pos, randomSource, ParticleTypeRegistry.CRIMSON_LEAF_PARTICLE_TYPE.get());
            }
        }
    }
}
