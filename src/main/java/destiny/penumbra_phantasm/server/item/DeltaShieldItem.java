package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.client.render.item.DeltaShieldRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

//TODO:
// - Make parry mechanic

public class DeltaShieldItem extends ShieldItem {
    public static final String PARRY_TICKER = "parryTicker";

    public DeltaShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return DeltaShieldRenderer.INSTANCE;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);

        stack.getOrCreateTag().putInt(PARRY_TICKER, 0);
        pPlayer.startUsingItem(pHand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLevel.isClientSide()) return;
        if (!(pLivingEntity instanceof ServerPlayer player)) return;

        player.getCooldowns().addCooldown(pStack.getItem(), 20);
        pStack.getOrCreateTag().putInt(PARRY_TICKER, -1);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        if (pLevel.isClientSide()) return;

        int parryTicker = pStack.getOrCreateTag().getInt(PARRY_TICKER);
        if (parryTicker >= 10) {
            pStack.getOrCreateTag().putInt(PARRY_TICKER, -1);
        } else if (parryTicker > -1) {
            pStack.getOrCreateTag().putInt(PARRY_TICKER, parryTicker + 1);
        }
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pLevel.isClientSide()) return;

        if (pStack.getTag() == null || pStack.getTag().get(PARRY_TICKER) == null) {
            pStack.getOrCreateTag().putInt(PARRY_TICKER, -1);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }
}