package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.ServerConfig;
import destiny.penumbra_phantasm.client.render.item.BlackKnifeItemRenderer;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class BlackKnifeItem extends KnifeItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final String SWOON_TICKER = "swoonTicker";
    public static final int SWOON_READY_TICK = 40;

    public BlackKnifeItem(Tier tier, int damage, float speed, boolean isSingleUse, Properties properties) {
        super(tier, damage, speed, isSingleUse, properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        super.use(level, player, hand);

        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (ServerConfig.blackKnifeOP) {
                if (stack.getTag() == null || stack.getTag().get(SWOON_TICKER) == null) {
                    stack.getOrCreateTag().putInt(SWOON_TICKER, -1);
                }

                int animationTicker = stack.getTag().getInt(SWOON_TICKER);

                if (animationTicker == -1) {
                    stack.getOrCreateTag().putInt(SWOON_TICKER, 0);
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int i, boolean b) {
        super.inventoryTick(stack, level, entity, i, b);

        if (level.isClientSide()) return;

        if (stack.getTag() == null || stack.getTag().get(SWOON_TICKER) == null) {
            stack.getOrCreateTag().putInt(SWOON_TICKER, -1);
        }

        int animationTicker = stack.getTag().getInt(SWOON_TICKER);

        if (animationTicker < SWOON_READY_TICK && animationTicker > -1) {
            if (animationTicker == 1) {
                level.playSound(null, entity.blockPosition(), SoundRegistry.KNIGHT_POWERUP.get(), SoundSource.PLAYERS, 1f, 1f);
            }

            stack.getOrCreateTag().putInt(SWOON_TICKER, animationTicker + 1);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlackKnifeItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new BlackKnifeItemRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
