package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CheshireChestBlockEntity extends BlockEntity {
    private int openCount;
    private float lidAngle;
    private float prevLidAngle;

    private int animationTick;
    private static final int ANIMATION_DURATION = 10;
    private AnimationState animationState = AnimationState.IDLE;

    private enum AnimationState { IDLE, OPENING, CLOSING }

    public CheshireChestBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CHESHIRE_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            int oldCount = this.openCount;
            this.openCount++;
            if (level != null && oldCount == 0) {
                level.playSound(null, worldPosition, SoundEvents.ENDER_CHEST_OPEN,
                        SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
            }
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, this.openCount);
        }
    }

    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            int oldCount = this.openCount;
            this.openCount--;
            if (level != null && oldCount == 1) {
                level.playSound(null, worldPosition, SoundEvents.ENDER_CHEST_CLOSE,
                        SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
                level.playSound(null, worldPosition, SoundRegistry.CHESHIRE_CHEST_LAUGH.get(),
                        SoundSource.BLOCKS, 0.2F, 1f);
            }
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, this.openCount);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CheshireChestBlockEntity be) {
        float target = be.openCount > 0 ? 1.0F : 0.0F;
        be.prevLidAngle = be.lidAngle;

        if (target == 1.0F && be.lidAngle < 0.999F && be.animationState != AnimationState.OPENING) {
            be.animationState = AnimationState.OPENING;
            be.animationTick = (int)(easeOutInverse(be.lidAngle) * ANIMATION_DURATION);
        } else if (target == 0.0F && be.lidAngle > 0.001F && be.animationState != AnimationState.CLOSING) {
            be.animationState = AnimationState.CLOSING;
            be.animationTick = (int)(easeInInverse(1.0F - be.lidAngle) * ANIMATION_DURATION);
        }

        if (be.animationState != AnimationState.IDLE) {
            be.animationTick = Math.min(be.animationTick + 1, ANIMATION_DURATION);
            float progress = be.animationTick / (float) ANIMATION_DURATION;

            if (be.animationState == AnimationState.OPENING) {
                be.lidAngle = easeOut(progress);
            } else {
                be.lidAngle = 1.0F - easeIn(progress);
            }

            if (be.animationTick == ANIMATION_DURATION) {
                be.animationState = AnimationState.IDLE;
                be.lidAngle = target;
            }
        }
    }

    private static float easeOut(float t) {
        return 1.0F - (1.0F - t) * (1.0F - t);
    }

    private static float easeIn(float t) {
        return t * t;
    }

    private static float easeOutInverse(float y) {
        return 1.0F - (float) Math.sqrt(Math.max(0.0, 1.0 - y));
    }

    private static float easeInInverse(float y) {
        return (float) Math.sqrt(Math.max(0.0, y));
    }

    public float getLidAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.openCount = type;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) {
            this.openCount = 0;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.openCount = tag.getInt("OpenCount");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("OpenCount", this.openCount);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("OpenCount", this.openCount);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.openCount = tag.getInt("OpenCount");
    }
}