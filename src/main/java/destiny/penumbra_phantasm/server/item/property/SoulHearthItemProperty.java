package destiny.penumbra_phantasm.server.item.property;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static destiny.penumbra_phantasm.server.item.SoulHearthItem.SOUL_TYPE;

public class SoulHearthItemProperty implements ClampedItemPropertyFunction {
    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i) {
        if (stack.getTag() != null) {
            int soulType = stack.getTag().getInt(SOUL_TYPE);

            switch (soulType) {
                case 1:
                    return 0.1f;
                case 2:
                    return 0.2f;
                case 3:
                    return 0.3f;
                case 4:
                    return 0.4f;
                case 5:
                    return 0.5f;
                case 6:
                    return 0.6f;
                case 7:
                    return 0.7f;
                default:
                    return 0.1f;
            }
        }
        return 0.1f;
    }
}
