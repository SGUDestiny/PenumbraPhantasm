package destiny.penumbra_phantasm.server.event;

import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.registry.ParticleTypeRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEvents {
    @SubscribeEvent
    public void attackEntity(AttackEntityEvent event) {
        Level level = event.getEntity().level();
        Entity target = event.getTarget();
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() == ItemRegistry.REAL_KNIFE.get()) {
            level.addParticle(ParticleTypeRegistry.REAL_KNIFE_SLASH.get(), target.getX(), target.getY() + 0.5, target.getZ(), 0, 0, 0);
        }
    }
}
