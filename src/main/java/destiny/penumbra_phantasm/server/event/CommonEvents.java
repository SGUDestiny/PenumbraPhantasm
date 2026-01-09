package destiny.penumbra_phantasm.server.event;

import destiny.penumbra_phantasm.Config;
import destiny.penumbra_phantasm.client.network.ClientBoundFountainData;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
            level.playSound(null, player.getOnPos().above(), SoundRegistry.REAL_KNIFE_HIT.get(), SoundSource.PLAYERS, 0.7f, 1f);

            int addition = Config.realKnifeOP ? 5 : 0;

            for (int i = 0; i < level.random.nextInt(3, 6) + addition; i++) {
                level.addParticle(ParticleTypeRegistry.REAL_KNIFE_HIT.get(), target.getX(), target.getY() + 1, target.getZ(), -0.15 + level.random.nextDouble() * 0.3, 0.3, -0.15 + level.random.nextDouble() * 0.3);
            }
        }
    }

    @SubscribeEvent
    public void attackEvent(LivingAttackEvent event) {
        Level level = event.getEntity().level();
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        float damage = event.getAmount();

        if (attacker instanceof Player player) {
            if (target.getHealth() <= damage) {
                ItemStack stack = player.getMainHandItem();

                if (stack.getItem() == ItemRegistry.NETHERITE_KNIFE.get()) {
                    if (stack.getTag() == null || stack.getTag().get("LV") == null) {
                        level.playSound(null, player.getOnPos().above(), SoundRegistry.LEVEL_UP.get(), SoundSource.PLAYERS, 0.7f, 1f);
                        System.out.println("Played sound");

                        stack.getOrCreateTag().putInt("LV", 1);
                        stack.getOrCreateTag().putInt("EXP", 0);
                    } else {
                        int lv = stack.getTag().getInt("LV");
                        int exp = stack.getTag().getInt("EXP");
                        int nextexp = (20 * lv) - 1;

                        if (exp >= nextexp) {
                            lv = lv + 1;
                            exp = 0;

                            level.playSound(null, player.getOnPos().above(), SoundRegistry.LEVEL_UP.get(), SoundSource.PLAYERS, 1f, 1f);

                            if (lv >= 20) {
                                ItemStack realKnife = new ItemStack(ItemRegistry.REAL_KNIFE.get());
                                realKnife.setTag(stack.getTag());

                                stack.setCount(0);
                                player.setItemInHand(InteractionHand.MAIN_HAND, realKnife);
                            }
                        } else {
                            exp = exp + 1;
                        }

                        stack.getOrCreateTag().putInt("LV", lv);
                        stack.getOrCreateTag().putInt("EXP", exp);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
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
