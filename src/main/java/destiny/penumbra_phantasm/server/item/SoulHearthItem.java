package destiny.penumbra_phantasm.server.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
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
                    model.rightArm.xRot = 4.8f + entity.getXRot() / 90;
                    model.rightArm.yRot = Mth.clamp(wrapRad(0F + model.head.yRot), -0.5f, 1);
                } else {
                    model.leftArm.xRot = 4.8f + entity.getXRot() / 90;
                    model.leftArm.yRot = Mth.clamp(wrapRad(0F + model.head.yRot), -0.5f, 1);
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

    public static float wrapRad(float pValue) {
        float p = (float) (Math.PI * 2);
        float d0 = pValue % p;
        if (d0 >= Math.PI) {
            d0 -= p;
        }

        if (d0 < -Math.PI) {
            d0 += p;
        }

        return d0;
    }
}
