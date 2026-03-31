package destiny.penumbra_phantasm.server.advancement;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;

public class TriggerCriterions {
    public static final PlayerTrigger DARK_FOUNTAIN_MAKE = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_make"));
    public static final PlayerTrigger DARK_FOUNTAIN_SEAL = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_seal"));
}
