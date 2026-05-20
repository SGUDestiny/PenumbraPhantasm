package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RosegoldLighterItem extends Item {
    public static final String OPEN = "open";
    public static final String ATTEMPTS = "attempts";

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
        }

        boolean open = stack.getTag().getBoolean(OPEN);

        if (open) {
            stack.getTag().putBoolean(OPEN, false);

            level.playSound(null, pos, SoundRegistry.LIGHTER_CLOSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.2F, 2.0F);
        } else {
            int attempts = stack.getTag().getInt(ATTEMPTS);
            RandomSource random = level.getRandom();

            if (attempts >= 3) {
                stack.getTag().putBoolean(OPEN, true);
                stack.getTag().putInt(ATTEMPTS, 0);

                level.playSound(null, pos, SoundRegistry.LIGHTER_LIGHT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                return InteractionResultHolder.success(stack);
            } else {
                if (random.nextFloat() > 0.5f) {
                    stack.getTag().putBoolean(OPEN, true);
                    stack.getTag().putInt(ATTEMPTS, 0);

                    level.playSound(null, pos, SoundRegistry.LIGHTER_LIGHT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    return InteractionResultHolder.success(stack);
                } else {
                    stack.getTag().putInt(ATTEMPTS, attempts + 1);

                    level.playSound(null, pos, SoundRegistry.LIGHTER_TRY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    return InteractionResultHolder.consume(stack);
                }
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            stack.getOrCreateTag().putBoolean(OPEN, false);
            stack.getOrCreateTag().putInt(ATTEMPTS, 0);
        }
    }
}
