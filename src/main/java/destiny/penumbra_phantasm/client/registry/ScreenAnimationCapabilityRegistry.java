package destiny.penumbra_phantasm.client.registry;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.capability.ScreenAnimationCapability;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ScreenAnimationCapabilityRegistry {
    public static final Capability<ScreenAnimationCapability> SCREEN_ANIMATION = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ScreenAnimationCapability.class);
    }
}
