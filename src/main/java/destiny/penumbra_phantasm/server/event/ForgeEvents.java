package destiny.penumbra_phantasm.server.event;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.DarknessBlock;
import destiny.penumbra_phantasm.server.block.GreatDoorShapeBlock;
import destiny.penumbra_phantasm.server.block.LuminescentWaterFluidBlock;
import destiny.penumbra_phantasm.server.capability.*;
import destiny.penumbra_phantasm.server.fluid.PureDarknessFluidType;
import destiny.penumbra_phantasm.server.fountain.GenericProvider;
import destiny.penumbra_phantasm.server.item.ScarletBucketItem;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.FluidRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void attachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "dark_fountains"), new GenericProvider<>(CapabilityRegistry.DARK_FOUNTAIN, new DarkFountainCapability()));
        event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "great_doors"), new GenericProvider<>(CapabilityRegistry.GREAT_DOOR, new GreatDoorCapability()));
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "soul"), new GenericProvider<>(CapabilityRegistry.SOUL, new SoulCapability()));
            event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "screen_animation"), new GenericProvider<>(CapabilityRegistry.SCREEN_ANIMATION, new ScreenAnimationCapability()));
            event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "cheshire_chest"), new CheshireChestCapability());
            event.addCapability(new ResourceLocation(PenumbraPhantasm.MODID, "fire_doors"), new GenericProvider<>(CapabilityRegistry.FIRE_DOORS, new FireDoorsCapability()));
        }
    }

    @SubscribeEvent
    public static void onFillBucket(FillBucketEvent event) {
        ItemStack emptyBucket = event.getEmptyBucket();

        if (!(emptyBucket.getItem() instanceof BucketItem bucketItem) || bucketItem.getFluid() != Fluids.EMPTY) {
            return;
        }

        Level level = event.getLevel();
        Vec3 location = event.getTarget().getLocation();
        BlockPos clickPos = BlockPos.containing(location.x, location.y, location.z);
        BlockState blockState = level.getBlockState(clickPos);
        FluidState fluidState = blockState.getFluidState();

        boolean isCustomFluid = blockState.getBlock() instanceof LuminescentWaterFluidBlock
                || fluidState.is(FluidRegistry.SOURCE_PURE_DARKNESS.get());

        boolean isScarletBucket = emptyBucket.getItem() instanceof ScarletBucketItem;

        if ((isCustomFluid && !isScarletBucket) || (!isCustomFluid && isScarletBucket)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        Block block = entity.level().getBlockState(entity.blockPosition()).getBlock();

        if (source.equals(entity.damageSources().inWall())) {
            if (block instanceof DarknessBlock || block instanceof GreatDoorShapeBlock) {
                event.setCanceled(true);
            }
        }
    }
}
