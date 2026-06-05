package destiny.penumbra_phantasm.server.advancement;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;

public class TriggerCriterions {
    public static final PlayerTrigger DARK_FOUNTAIN_MAKE = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_make"));
    public static final PlayerTrigger DARK_FOUNTAIN_SEAL = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountain_seal"));
    public static final PlayerTrigger DETERMINATION_INJECTION = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "determination_injection"));
    public static final PlayerTrigger DETERMINATION_INJECTION_DEATH = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "determination_injection_death"));
    public static final PlayerTrigger DETERMINATION_INJECTION_OVERDOSE = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "determination_injection_overdose"));
    public static final PlayerTrigger DETERMINATION_INJECTION_CAUSE_OVERDOSE = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "determination_injection_cause_overdose"));
    public static final PlayerTrigger DETERMINATION_INJECTION_STEAL = new PlayerTrigger(new ResourceLocation(PenumbraPhantasm.MODID, "determination_injection_steal"));
}