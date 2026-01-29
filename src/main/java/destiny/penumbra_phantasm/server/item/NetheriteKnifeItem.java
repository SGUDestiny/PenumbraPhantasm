package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetheriteKnifeItem extends KnifeItem {
    public NetheriteKnifeItem(Tier tier, int damage, float speed, boolean isSingleUse, boolean needsNetherStar, Properties properties) {
        super(tier, damage, speed, isSingleUse, needsNetherStar, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        if (level != null) {
            if (stack.getTag() != null) {
                if (stack.getTag().get("LV") != null && stack.getTag().get("EXP") != null) {
                    int lv = stack.getTag().getInt("LV");
                    int exp = stack.getTag().getInt("EXP");
                    int nextexp = 20 * lv;

                    list.add(Component.translatable("tooltip.penumbra_phantasm.netherite_knife.lv")
                            .append(Component.literal(" " + lv)
                                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
                            )
                    );
                    list.add(Component.translatable("tooltip.penumbra_phantasm.netherite_knife.exp")
                            .append(Component.literal(" " + exp + " / " + nextexp)
                                    .withStyle(Style.EMPTY.withFont(new ResourceLocation(PenumbraPhantasm.MODID, "8_bit_operator")))
                            )
                    );
                }
            }
        }
    }
}
