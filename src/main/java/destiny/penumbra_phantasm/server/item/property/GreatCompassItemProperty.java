package destiny.penumbra_phantasm.server.item.property;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class GreatCompassItemProperty implements ClampedItemPropertyFunction {
    public static final int DEFAULT_ROTATION = 0;
    private final GreatCompassItemProperty.CompassWobble wobble = new GreatCompassItemProperty.CompassWobble();
    private final GreatCompassItemProperty.CompassWobble wobbleRandom = new GreatCompassItemProperty.CompassWobble();
    public final GreatCompassItemProperty.CompassTarget compassTarget;

    public GreatCompassItemProperty(GreatCompassItemProperty.CompassTarget pCompassTarget) {
        this.compassTarget = pCompassTarget;
    }

    public float unclampedCall(ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
        Entity entity = pEntity != null ? pEntity : pStack.getEntityRepresentation();
        if (entity == null) {
            return 0.0F;
        } else {
            pLevel = this.tryFetchLevelIfMissing(entity, pLevel);
            return pLevel == null ? 0.0F : this.getCompassRotation(pStack, pLevel, pSeed, entity);
        }
    }

    private float getCompassRotation(ItemStack pStack, ClientLevel pLevel, int pSeed, Entity pEntity) {
        GlobalPos $$4 = this.compassTarget.getPos(pLevel, pStack, pEntity);
        long $$5 = pLevel.getGameTime();
        return !this.isValidCompassTargetPos(pEntity, $$4) ? this.getRandomlySpinningRotation(pSeed, $$5) : this.getRotationTowardsCompassTarget(pEntity, $$5, $$4.pos());
    }

    private float getRandomlySpinningRotation(int pSeed, long pTicks) {
        if (this.wobbleRandom.shouldUpdate(pTicks)) {
            this.wobbleRandom.update(pTicks, Math.random());
        }

        double $$2 = this.wobbleRandom.rotation + (double)((float)this.hash(pSeed) / (float)Integer.MAX_VALUE);
        return Mth.positiveModulo((float)$$2, 1.0F);
    }

    private float getRotationTowardsCompassTarget(Entity pEntity, long pTicks, BlockPos pPos) {
        double $$3 = this.getAngleFromEntityToPos(pEntity, pPos);
        double $$4 = this.getWrappedVisualRotationY(pEntity);
        double $$6;
        if (pEntity instanceof Player $$5) {
            if ($$5.isLocalPlayer()) {
                if (this.wobble.shouldUpdate(pTicks)) {
                    this.wobble.update(pTicks, (double)0.5F - ($$4 - (double)0.25F));
                }

                $$6 = $$3 + this.wobble.rotation;
                return Mth.positiveModulo((float)$$6, 1.0F);
            }
        }

        $$6 = (double)0.5F - ($$4 - (double)0.25F - $$3);
        return Mth.positiveModulo((float)$$6, 1.0F);
    }

    @Nullable
    private ClientLevel tryFetchLevelIfMissing(Entity pEntity, @Nullable ClientLevel pLevel) {
        return pLevel == null && pEntity.level() instanceof ClientLevel ? (ClientLevel)pEntity.level() : pLevel;
    }

    private boolean isValidCompassTargetPos(Entity pEntity, @Nullable GlobalPos pPos) {
        return pPos != null && pPos.dimension() == pEntity.level().dimension() && !(pPos.pos().distToCenterSqr(pEntity.position()) < (double)1.0E-5F);
    }

    private double getAngleFromEntityToPos(Entity pEntity, BlockPos pPos) {
        Vec3 $$2 = Vec3.atCenterOf(pPos);
        return Math.atan2($$2.z() - pEntity.getZ(), $$2.x() - pEntity.getX()) / (double)((float)Math.PI * 2F);
    }

    private double getWrappedVisualRotationY(Entity pEntity) {
        return Mth.positiveModulo(pEntity.getVisualRotationYInDegrees() / 360.0F, (double)1.0F);
    }

    private int hash(int pValue) {
        return pValue * 1327217883;
    }

    @OnlyIn(Dist.CLIENT)
    static class CompassWobble {
        double rotation;
        private double deltaRotation;
        private long lastUpdateTick;

        CompassWobble() {
        }

        boolean shouldUpdate(long pTicks) {
            return this.lastUpdateTick != pTicks;
        }

        void update(long pTicks, double pRotation) {
            this.lastUpdateTick = pTicks;
            double $$2 = pRotation - this.rotation;
            $$2 = Mth.positiveModulo($$2 + (double)0.5F, 1.0F) - (double)0.5F;
            this.deltaRotation += $$2 * 0.1;
            this.deltaRotation *= 0.8;
            this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface CompassTarget {
        @Nullable
        GlobalPos getPos(ClientLevel var1, ItemStack var2, Entity var3);
    }
}
