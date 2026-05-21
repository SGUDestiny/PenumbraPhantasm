package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.block.IchorFireBlock;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RosegoldLighterItem extends Item {
    public static final String OPEN = "open";
    public static final String ATTEMPTS = "attempts";
    public static final String USES = "uses";
    public static final int MAX_USES = 64;

    public RosegoldLighterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        BlockPos pos = player.blockPosition();
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            stack.getOrCreateTag().putBoolean(OPEN, false);
            stack.getOrCreateTag().putInt(ATTEMPTS, 0);
            stack.getOrCreateTag().putInt(USES, MAX_USES);
        }

        boolean open = stack.getTag().getBoolean(OPEN);

        if (open) {
            if (!level.isClientSide()) {
                stack.getTag().putBoolean(OPEN, false);
            }

            level.playSound(null, pos, SoundRegistry.LIGHTER_CLOSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            int attempts = stack.getTag().getInt(ATTEMPTS);
            int uses = stack.getTag().getInt(USES);
            RandomSource random = level.getRandom();

            if (uses > 0) {
                if (random.nextFloat() > 0.5f || attempts >= 3) {
                    if (!level.isClientSide()) {
                        stack.getTag().putBoolean(OPEN, true);
                        stack.getTag().putInt(ATTEMPTS, 0);
                    }

                    level.playSound(null, pos, SoundRegistry.LIGHTER_TRY.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
                    level.playSound(null, pos, SoundRegistry.LIGHTER_LIGHT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    if (!level.isClientSide()) {
                        stack.getTag().putInt(ATTEMPTS, attempts + 1);
                    }

                    level.playSound(null, pos, SoundRegistry.LIGHTER_TRY.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
                }
            } else {
                level.playSound(null, pos, SoundRegistry.LIGHTER_TRY.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Player player = pContext.getPlayer();

        if (player == null) return InteractionResult.PASS;

        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        ItemStack itemStack = player.getItemInHand(pContext.getHand());
        CompoundTag tag = itemStack.getTag();

        if (tag == null) {
            itemStack.getOrCreateTag().putBoolean(OPEN, false);
            itemStack.getOrCreateTag().putInt(ATTEMPTS, 0);
            itemStack.getOrCreateTag().putInt(USES, MAX_USES);
        }

        boolean open = itemStack.getTag().getBoolean(OPEN);
        int uses = itemStack.getTag().getInt(USES);

        if (!open || uses <= 0) return InteractionResult.PASS;

        if (!CampfireBlock.canLight(clickedState) && !CandleBlock.canLight(clickedState) && !CandleCakeBlock.canLight(clickedState)) {
            BlockPos frontPos = clickedPos.relative(pContext.getClickedFace());

            if (BaseFireBlock.canBePlacedAt(level, frontPos, pContext.getHorizontalDirection())) {
                level.playSound(player, frontPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState fireBlock = ((IchorFireBlock)BlockRegistry.ICHOR_FIRE.get()).getStateForPlacement(level, frontPos);
                level.setBlock(frontPos, fireBlock, 11);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, clickedPos);

                if (!level.isClientSide()) {
                    itemStack.getOrCreateTag().putInt(USES, uses - 1);
                }

                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, frontPos, itemStack);
                    itemStack.hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(pContext.getHand()));
                }

                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                return InteractionResult.FAIL;
            }
        } else {
            level.playSound(player, clickedPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(clickedPos, clickedState.setValue(BlockStateProperties.LIT, true), 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, clickedPos);

            if (!level.isClientSide()) {
                itemStack.getOrCreateTag().putInt(USES, uses - 1);
            }

            pContext.getItemInHand().hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(pContext.getHand()));

            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        CompoundTag tag = pStack.getTag();

        if (tag != null) {
            int uses = tag.getInt(USES);
            boolean open = tag.getBoolean(OPEN);

            return uses > 0 && open;
        }

        return false;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        CompoundTag tag = pStack.getTag();

        if (tag != null) {
            int uses = tag.getInt(USES);

            return Math.round(uses * 13.0F / MAX_USES);
        }

        return 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return 0xD03EE6;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced)
    {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        if (pStack.getTag() == null) return;

        int uses = pStack.getTag().getInt(USES);

        pTooltipComponents.add(Component.translatable("message.penumbra_phantasm.rosegold_lighter_fuel").withStyle(ChatFormatting.WHITE).append(uses + " / " + MAX_USES));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            stack.getOrCreateTag().putBoolean(OPEN, false);
            stack.getOrCreateTag().putInt(ATTEMPTS, 0);
            stack.getOrCreateTag().putInt(USES, 64);
        }
    }
}
