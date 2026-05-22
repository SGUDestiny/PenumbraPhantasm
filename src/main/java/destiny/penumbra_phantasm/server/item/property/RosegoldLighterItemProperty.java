package destiny.penumbra_phantasm.server.item.property;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static destiny.penumbra_phantasm.server.item.RosegoldLighterItem.OPEN;
import static destiny.penumbra_phantasm.server.item.RosegoldLighterItem.USES;

public class RosegoldLighterItemProperty implements ClampedItemPropertyFunction {
    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i) {
        if (stack.getTag() != null) {
            boolean open = stack.getTag().getBoolean(OPEN);
            int fuel = stack.getTag().getInt(USES);

            if (open && fuel <= 0) return 0.5f;

            return open ? 1.0f : 0.0f;
        }
        return 0.0f;
    }
}
