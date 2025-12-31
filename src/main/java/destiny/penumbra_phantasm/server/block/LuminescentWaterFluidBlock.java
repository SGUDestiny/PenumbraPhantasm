package destiny.penumbra_phantasm.server.block;

import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import destiny.penumbra_phantasm.server.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class LuminescentWaterFluidBlock extends LiquidBlock {
    public LuminescentWaterFluidBlock(Supplier<? extends FlowingFluid> p_54694_, Properties p_54695_) {
        super(p_54694_, p_54695_);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource randomSource) {
        BlockPos blockpos = pos.above();
        if (level.getBlockState(blockpos).isAir() && !level.getBlockState(blockpos).isSolidRender(level, blockpos)) {
            if (randomSource.nextFloat() > 0.9f) {
                level.addParticle(ParticleTypeRegistry.LUMINESCENT_PARTICLE.get(), pos.getX() + ModUtil.getBoundRandomDoubleStatic(level, -0.5, 0.5), pos.getY() + 1, pos.getZ() + ModUtil.getBoundRandomDoubleStatic(level, -0.5, 0.5), 0, 0, 0);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, true, true));
        }
    }
}
