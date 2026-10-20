package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.server.registry.BlockEntityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AnchorOfLightBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final String IS_JUST_PLACED = "isJustPlaced";
    public static final String ANIMATION_TICKER = "animationTicker";
    public static final String SOUL_TYPE = "soulType";

    public boolean isJustPlaced = true;
    public int animationTicker = 0;
    public int soulType = 1;

    protected static class Animations {
        protected static final String MAIN_CONTROLLER = "main";
        protected static final RawAnimation PLACE = RawAnimation.begin().thenPlay("anchor_of_light.place").thenLoop("anchor_of_light.idle");
        protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("anchor_of_light.idle");

        private Animations() {}
    }

    public AnchorOfLightBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.ANCHOR_OF_LIGHT_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AnchorOfLightBlockEntity anchor) {
        if (level.isClientSide()) return;

        if (anchor.isJustPlaced) {
            anchor.isJustPlaced = false;

            level.playSound(null, pos, SoundRegistry.ANCHOR_OF_LIGHT_PLACE.get(), SoundSource.BLOCKS, 1, 1);
        }

        if (anchor.animationTicker < 17) {
            anchor.animationTicker++;
            anchor.markUpdated();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        pTag.putBoolean(IS_JUST_PLACED, isJustPlaced);
        pTag.putInt(ANIMATION_TICKER, animationTicker);
        pTag.putInt(SOUL_TYPE, soulType);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        this.isJustPlaced = pTag.getBoolean(IS_JUST_PLACED);
        this.animationTicker = pTag.getInt(ANIMATION_TICKER);
        this.soulType = pTag.getInt(SOUL_TYPE);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(2, 5, 2);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        AnimationController<AnchorOfLightBlockEntity> controller = new AnimationController<>(this, Animations.MAIN_CONTROLLER, 0, this::handleAnimationState);
        controllerRegistrar.add(controller);
    }

    private PlayState handleAnimationState(AnimationState<AnchorOfLightBlockEntity> state) {
        AnimationController<AnchorOfLightBlockEntity> controller = state.getController();

        if (!state.isCurrentAnimation(Animations.PLACE) && this.animationTicker > 6) {
            controller.setAnimation(Animations.IDLE);
        } else {
            controller.setAnimation(Animations.PLACE);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    private void markUpdated() {
        super.setChanged();
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
