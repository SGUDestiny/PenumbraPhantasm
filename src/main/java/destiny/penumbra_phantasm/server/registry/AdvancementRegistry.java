package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.server.advancement.TriggerCriterions;
import destiny.penumbra_phantasm.server.advancement.ChangedDimensionContainsTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementRegistry {
    public static void register() {
        CriteriaTriggers.register(TriggerCriterions.DARK_FOUNTAIN_MAKE);
        CriteriaTriggers.register(TriggerCriterions.DARK_FOUNTAIN_SEAL);
        CriteriaTriggers.register(ChangedDimensionContainsTrigger.INSTANCE);
        CriteriaTriggers.register(TriggerCriterions.DETERMINATION_INJECTION);
        CriteriaTriggers.register(TriggerCriterions.DETERMINATION_INJECTION_DEATH);
        CriteriaTriggers.register(TriggerCriterions.DETERMINATION_INJECTION_OVERDOSE);
        CriteriaTriggers.register(TriggerCriterions.DETERMINATION_INJECTION_CAUSE_OVERDOSE);
        CriteriaTriggers.register(TriggerCriterions.DETERMINATION_INJECTION_STEAL);
        CriteriaTriggers.register(TriggerCriterions.SOUL_HEARTH);
    }
}
