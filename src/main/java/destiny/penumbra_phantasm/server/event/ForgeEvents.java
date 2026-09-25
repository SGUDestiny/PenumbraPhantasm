package destiny.penumbra_phantasm.server.event;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundParticlePacket;
import destiny.penumbra_phantasm.server.block.LuminescentWaterFluidBlock;
import destiny.penumbra_phantasm.server.block.NegativePhotonsFluidBlock;
import destiny.penumbra_phantasm.server.capability.*;
import destiny.penumbra_phantasm.server.fountain.GenericProvider;
import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import destiny.penumbra_phantasm.server.item.EggItem;
import destiny.penumbra_phantasm.server.item.ScarletBucketItem;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static destiny.penumbra_phantasm.server.item.BlackKnifeItem.SWOON_READY_TICK;
import static destiny.penumbra_phantasm.server.item.BlackKnifeItem.SWOON_TICKER;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void attachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "dark_fountains"), new GenericProvider<>(CapabilityRegistry.DARK_FOUNTAIN,
                new DarkFountainCapability()));
        event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "great_doors"), new GenericProvider<>(CapabilityRegistry.GREAT_DOOR,
                new GreatDoorCapability()));
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "soul"), new GenericProvider<>(CapabilityRegistry.SOUL,
                    new SoulCapability()));
            event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "screen_animation"),
                    new GenericProvider<>(CapabilityRegistry.SCREEN_ANIMATION, new ScreenAnimationCapability()));
            event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "cheshire_chest"), new CheshireChestCapability());
            event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "fire_doors"), new GenericProvider<>(CapabilityRegistry.FIRE_DOORS,
                    new FireDoorsCapability()));
            event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "vertical_bar"), new GenericProvider<>(CapabilityRegistry.VERTICAL_BAR,
                    new VerticalBarCapability()));
            event.addCapability(ResourceLocation.tryBuild(PenumbraPhantasm.MODID, "ability"), new GenericProvider<>(CapabilityRegistry.ABILITY,
                    new AbilityCapability()));
        }
    }

    @SubscribeEvent
    public static void onFillBucket(FillBucketEvent event) {
        ItemStack bucketStack = event.getEmptyBucket();

        if (!(bucketStack.getItem() instanceof BucketItem bucketItem) || bucketItem.getFluid() != Fluids.EMPTY) return;

        Level level = event.getLevel();
        Vec3 clickVec = event.getTarget().getLocation();
        BlockPos clickPos = BlockPos.containing(clickVec.x, clickVec.y, clickVec.z);
        BlockState clickState = level.getBlockState(clickPos);
        FluidState clickFluid = clickState.getFluidState();

        boolean isCustomFluid = clickState.getBlock() instanceof LuminescentWaterFluidBlock || clickFluid.is(FluidRegistry.SOURCE_PURE_DARKNESS.get())
                || clickState.getBlock() instanceof NegativePhotonsFluidBlock;

        boolean isScarletBucket = bucketStack.getItem() instanceof ScarletBucketItem;

        if ((isCustomFluid && !isScarletBucket) || (!isCustomFluid && isScarletBucket)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.hasEffect(MobEffects.REGENERATION) || player.hasEffect(MobEffects.HEAL)) return;
        if (event.getAmount() > 1) return;

        if (!DarkWorldUtil.isDepths(player.level())) return;

        player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap -> {
            int determination = cap.determination;

            if (determination <= 0) {
                event.setAmount(event.getAmount() * 0);
            }
        });
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Level level = player.level();

        if (!CardKingdomEggRoomUtil.isEggRoom(level)) return;
        if (!DarkWorldUtil.isDepths(level)) return;

        player.fallDistance = 0f;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();

        if (!CardKingdomEggRoomUtil.isEggRoom(level)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();

        if (!CardKingdomEggRoomUtil.isEggRoom(level)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();

        if (!CardKingdomEggRoomUtil.isEggRoom(level)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();

        if (!CardKingdomEggRoomUtil.isEggRoom(level)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();

        if (!CardKingdomEggRoomUtil.isEggRoom(level)) return;

        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof EggItem) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        Level level = attacker.level();

        if (level.isClientSide()) return;

        ItemStack stack = attacker.getMainHandItem();
        Entity target = event.getTarget();

        if (stack.getItem() == ItemRegistry.BLACK_KNIFE.get()) {
            attackWithBlackKnife(level, attacker, target, stack);
        } else if (stack.getItem() == ItemRegistry.REAL_KNIFE.get()) {
            attackWithRealKnife(attacker, target, stack);
        }

        event.setCanceled(true);
    }

    public static void attackWithBlackKnife(Level level, Player attacker, Entity target, ItemStack weaponStack) {
        int swoonTicker = weaponStack.getTag().getInt(SWOON_TICKER);

        if (swoonTicker < SWOON_READY_TICK) return;

        AABB playerBox = new AABB(attacker.blockPosition()).inflate(16);

        for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, playerBox)) {
            ScreenAnimationCapability screenCap = serverPlayer.getCapability(CapabilityRegistry.SCREEN_ANIMATION).resolve().orElse(null);

            if (screenCap == null) continue;

            screenCap.swoonAnimationTicker = 0;
        }

        AbilityCapability abilityCap = attacker.getCapability(CapabilityRegistry.ABILITY).resolve().orElse(null);

        if (abilityCap == null) return;

        DelayTicker delayTicker = new DelayTicker(target.getUUID(), weaponStack, DelayTicker.SWOON_DELAY, 0);
        abilityCap.delayTickers.add(delayTicker);

        weaponStack.getOrCreateTag().putInt(SWOON_TICKER, -1);
    }

    public static void attackWithRealKnife(Player attacker, Entity target, ItemStack weaponStack) {
        Vec3 particleVec = new Vec3(target.getX(), target.getEyeY(), target.getZ());

        particleVec.add(attacker.getX(), attacker.getEyeY(), attacker.getZ());
        particleVec.add(attacker.getX(), attacker.getEyeY(), attacker.getZ());
        particleVec.add(attacker.getX(), attacker.getEyeY(), attacker.getZ());

        PacketHandlerRegistry.INSTANCE.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(),
                        32, target.level().dimension())),
                new ClientBoundParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypeRegistry.REAL_KNIFE_SLASH.get()),
                        particleVec.x, particleVec.y, particleVec.z, 0, 0, 0, 1)
        );

        AbilityCapability abilityCap = attacker.getCapability(CapabilityRegistry.ABILITY).resolve().orElse(null);

        if (abilityCap == null) return;

        DelayTicker delayTicker = new DelayTicker(target.getUUID(), weaponStack, DelayTicker.REAL_KNIFE_DELAY, 0);
        abilityCap.delayTickers.add(delayTicker);
    }
}