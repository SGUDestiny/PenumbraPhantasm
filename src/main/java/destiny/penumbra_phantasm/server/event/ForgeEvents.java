package destiny.penumbra_phantasm.server.event;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.IntroCapability;
import destiny.penumbra_phantasm.server.fountain.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fountain.GenericProvider;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void attachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountains"), new GenericProvider<>(CapabilityRegistry.DARK_FOUNTAIN, new DarkFountainCapability()));
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "intro"), new GenericProvider<>(CapabilityRegistry.INTRO, new IntroCapability()));
        }
    }
}
