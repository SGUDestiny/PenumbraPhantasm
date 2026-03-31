package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.advancement.ChangedDimensionContainsTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementRegistry {
    public static void register() {
        CriteriaTriggers.register(TriggerCriterions.DARK_FOUNTAIN_MAKE);
        CriteriaTriggers.register(TriggerCriterions.DARK_FOUNTAIN_SEAL);
        CriteriaTriggers.register(ChangedDimensionContainsTrigger.INSTANCE);
    }
}
