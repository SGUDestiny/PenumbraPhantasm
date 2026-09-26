package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.AbilityCapability;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TensionItem extends FlavorTooltipItem {
    public int tensionDifference;
    public SoundEvent consumeSound;

    public TensionItem(Properties pProperties, int tensionDifference, SoundEvent consumeSound) {
        super(pProperties);
        this.tensionDifference = tensionDifference;
        this.consumeSound = consumeSound;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player instanceof ServerPlayer serverPlayer) {
            AbilityCapability abilityCap = serverPlayer.getCapability(CapabilityRegistry.ABILITY).orElse(null);
            VerticalBarCapability verticalBarCap = serverPlayer.getCapability(CapabilityRegistry.VERTICAL_BAR).orElse(null);

            verticalBarCap.determinationDifferenceTicker = 0;
            verticalBarCap.determinationTargetTicker = 0;
            verticalBarCap.oldDetermination = abilityCap.tension;
            abilityCap.tension = Mth.clamp(abilityCap.tension + tensionDifference, 0, 250);

            level.playSound(null, serverPlayer.getOnPos(), consumeSound, SoundSource.PLAYERS, 0.5f, 1);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> components, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, components, pIsAdvanced);

        if (tensionDifference != 0) {
            MutableComponent mutableComponent = Component.empty();

            if (tensionDifference > 0) {
                mutableComponent.append(Component.literal("+"));
            }

            mutableComponent.append(Component.literal((int)(tensionDifference / 2.5) + "")
                    .append(Component.literal("% "))
                    .append(Component.translatable("tooltip.penumbra_phantasm.tension"))
                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator"))));

            components.add(mutableComponent);
        }
    }
}