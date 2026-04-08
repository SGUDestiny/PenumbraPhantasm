package destiny.penumbra_phantasm.server.event;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.block.LuminescentWaterFluidBlock;
import destiny.penumbra_phantasm.server.capability.GreatDoorCapability;
import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.capability.DarkFountainCapability;
import destiny.penumbra_phantasm.server.fluid.PureDarknessFluidType;
import destiny.penumbra_phantasm.server.fountain.GenericProvider;
import destiny.penumbra_phantasm.server.item.ScarletBucketItem;
import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.FluidRegistry;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PenumbraPhantasm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!DarkWorldUtil.isDarkWorld(serverLevel)) {
            return;
        }
        DarkWorldUtil.tryRandomGreatDoorForDarkChunk(serverLevel, event.getChunk().getPos());
    }

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
        }
    }

    @SubscribeEvent
    public static void fillBucketEvent(FillBucketEvent event) {
        Level level = event.getLevel();
        ItemStack stack = event.getEmptyBucket();
        Vec3 location = event.getTarget().getLocation();
        BlockPos clickPos = BlockPos.containing(location.x, location.y, location.z);

        if (level.getBlockState(clickPos).getBlock() instanceof LuminescentWaterFluidBlock
                || level.getBlockState(clickPos).getFluidState().is(FluidRegistry.SOURCE_PURE_DARKNESS.get()))
        {
            if (!(stack.getItem() instanceof ScarletBucketItem)) {
                event.setCanceled(true);
            }
        }
    }
}
