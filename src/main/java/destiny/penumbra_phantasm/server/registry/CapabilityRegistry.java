package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.*;
import destiny.penumbra_phantasm.server.capability.CheshireChestInventory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityRegistry {
    public static final Capability<DarkFountainCapability> DARK_FOUNTAIN = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<SoulCapability> SOUL = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ScreenAnimationCapability> SCREEN_ANIMATION = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<GreatDoorCapability> GREAT_DOOR = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<CheshireChestInventory> CHESHIRE_CHEST = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(DarkFountainCapability.class);
        event.register(SoulCapability.class);
        event.register(ScreenAnimationCapability.class);
        event.register(GreatDoorCapability.class);
        event.register(CheshireChestCapability.class);
    }
}
