package destiny.penumbra_phantasm.client.event;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.client.registry.ScreenAnimationCapabilityRegistry;
import destiny.penumbra_phantasm.server.fountain.GenericProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "screen_animation"), new GenericProvider<>(ScreenAnimationCapabilityRegistry.SCREEN_ANIMATION, new ScreenAnimationCapability()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isClient()) {
            return;
        }
        if (!(event.player instanceof LocalPlayer)) {
            return;
        }
        event.player.getCapability(ScreenAnimationCapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> cap.tick(event.player.level(), event.player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();
        original.reviveCaps();
        original.getCapability(ScreenAnimationCapabilityRegistry.SCREEN_ANIMATION).ifPresent(cap -> {
            player.getCapability(ScreenAnimationCapabilityRegistry.SCREEN_ANIMATION).ifPresent(copyCap -> copyCap.sync(cap));
        });
        original.invalidateCaps();
    }
}
