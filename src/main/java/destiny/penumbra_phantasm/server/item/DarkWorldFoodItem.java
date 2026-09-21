package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.capability.VerticalBarCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DarkWorldFoodItem extends Item {
    public int determinationDifference;
    public SoundEvent consumeSound;

    public DarkWorldFoodItem(Properties pProperties, int determinationDifference, SoundEvent consumeSound) {
        super(pProperties);
        this.determinationDifference = determinationDifference;
        this.consumeSound = consumeSound;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (pLivingEntity instanceof ServerPlayer player) {
            SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
            VerticalBarCapability verticalBarCap = player.getCapability(CapabilityRegistry.VERTICAL_BAR).orElse(null);

            verticalBarCap.determinationDifferenceTicker = 0;
            verticalBarCap.determinationTargetTicker = 0;
            verticalBarCap.oldDetermination = soulCap.determination;
            soulCap.determination = Mth.clamp(soulCap.determination + determinationDifference, 0, 100);

            pLevel.playSound(null, player.getOnPos(), consumeSound, SoundSource.PLAYERS, 0.5f, 1);
        }

        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> components, TooltipFlag pIsAdvanced) {
        FoodProperties foodProperties = pStack.getFoodProperties(null);

        int nutrition = 0;
        float saturationModifier = 0f;
        if (foodProperties != null) {
            nutrition = foodProperties.getNutrition();
            saturationModifier = foodProperties.getSaturationModifier();
        }

        if (nutrition != 0) {
            components.add(Component.literal("+" + nutrition + " ")
                    .append(Component.translatable("tooltip.penumbra_phantasm.nutrition"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
        }

        if (saturationModifier != 0) {
            double saturationPoints = (nutrition * saturationModifier * 2);

            components.add(Component.literal("+" + String.format("%.1f", saturationPoints) + " ")
                    .append(Component.translatable("tooltip.penumbra_phantasm.saturation"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));
        }

        if (determinationDifference != 0) {
            MutableComponent mutableComponent = Component.empty();

            if (determinationDifference > 0) {
                mutableComponent.append(Component.literal("+"));
            } else {
                mutableComponent.append(Component.literal("-"));
            }

            mutableComponent.append(Component.literal(determinationDifference + " ")
                    .append(Component.translatable("tooltip.penumbra_phantasm.soul_hearth.soul_type.1"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));

            components.add(mutableComponent);
        }
    }
}
