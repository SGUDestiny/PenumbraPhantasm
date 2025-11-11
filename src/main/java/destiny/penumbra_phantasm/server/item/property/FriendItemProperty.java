package destiny.penumbra_phantasm.server.item.property;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FriendItemProperty implements ClampedItemPropertyFunction {
    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
        if (stack.getTag() != null) {
            int animation = stack.getTag().getInt("animation");

            switch (animation) {
                case 0:
                    return 0.0f;
                case 1:
                    return 0.01f;
                case 2:
                    return 0.02f;
                case 3:
                    return 0.03f;
                case 4:
                    return 0.04f;
                case 5:
                    return 0.05f;
                case 6:
                    return 0.06f;
                case 7:
                    return 0.07f;
                case 8:
                    return 0.08f;
                case 9:
                    return 0.09f;
                case 10:
                    return 0.10f;
                case 11:
                    return 0.11f;
                case 12:
                    return 0.12f;
                case 13:
                    return 0.13f;
                case 14:
                    return 0.14f;
                case 15:
                    return 0.15f;
                case 16:
                    return 0.16f;
                case 17:
                    return 0.17f;
                case 18:
                    return 0.18f;
            }
        }
        return 0.05f;
    }
}
