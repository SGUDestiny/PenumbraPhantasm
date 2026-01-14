package destiny.penumbra_phantasm.server.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SoulHearthItem extends Item {
    public SoulHearthItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private static final HumanoidModel.ArmPose POSE = HumanoidModel.ArmPose.create("POSE", false, (model, entity, arm) -> {
                if (arm == HumanoidArm.RIGHT) {
                    model.rightArm.xRot = 4.8f;
                } else {
                    model.leftArm.xRot = 4.8f;
                }
            });

            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (itemStack.getItem() instanceof SoulHearthItem) {
                    return POSE;
                }

                return IClientItemExtensions.super.getArmPose(entityLiving, hand, itemStack);
            }
        });
    }
}
