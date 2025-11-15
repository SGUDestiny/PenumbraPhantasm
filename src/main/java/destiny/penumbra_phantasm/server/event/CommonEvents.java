package destiny.penumbra_phantasm.server.event;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.client.network.ClientBoundFountainData;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class CommonEvents {
    @SubscribeEvent
    public void attackEntity(AttackEntityEvent event) {
        Level level = event.getEntity().level();
        Entity target = event.getTarget();
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        Vec3 vec = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        vec.add(player.getX(), player.getEyeY(), player.getZ());
        vec.add(player.getX(), player.getEyeY(), player.getZ());
        vec.add(player.getX(), player.getEyeY(), player.getZ());

        if (stack.getItem() == ItemRegistry.REAL_KNIFE.get()) {
            level.addParticle(ParticleTypeRegistry.REAL_KNIFE_SLASH.get(), vec.x, vec.y, vec.z, 0, 0, 0);
            level.playLocalSound(target.getX(), target.getEyeY(), target.getZ(), SoundRegistry.REAL_KNIFE_HIT.get(), SoundSource.AMBIENT, 1f, 1f, false);

            int addition = Config.realKnifeOP ? 5 : 0;

            for (int i = 0; i < level.random.nextInt(3, 6) + addition; i++) {
                level.addParticle(ParticleTypeRegistry.REAL_KNIFE_HIT.get(), target.getX(), target.getY() + 1, target.getZ(), -0.15 + level.random.nextDouble() * 0.3, 0.3, -0.15 + level.random.nextDouble() * 0.3);
            }
        }
    }

    @SubscribeEvent
    public void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Level level = event.level;

            level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientBoundFountainData(cap.darkFountains));
                cap.darkFountains.forEach((uid, fountain) -> {
                    fountain.tick(level);
                });
            });
        }
    }
}
