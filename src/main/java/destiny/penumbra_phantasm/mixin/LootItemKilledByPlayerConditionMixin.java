package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.registry.DamageTypeRegistry;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootItemKilledByPlayerCondition.class)
public class LootItemKilledByPlayerConditionMixin {
/*    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("HEAD"), cancellable = true)
    public void test(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        if(context != null && context.hasParam(LootContextParams.DAMAGE_SOURCE)
                && context.getParam(LootContextParams.DAMAGE_SOURCE) instanceof DamageTypeRegistry.ExperienceDroppingDamageSource) {
            cir.setReturnValue(true);
        }
    }*/
}