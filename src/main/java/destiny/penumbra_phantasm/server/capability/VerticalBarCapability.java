package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.client.network.ClientBoundVerticalBarPacket;
import destiny.penumbra_phantasm.server.item.DarkWorldFoodFlavorItem;
import destiny.penumbra_phantasm.server.item.DarkWorldFoodItem;
import destiny.penumbra_phantasm.server.item.DeterminationInjectionItem;
import destiny.penumbra_phantasm.server.item.EmptyInjectionItem;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;

public class VerticalBarCapability implements INBTSerializable<CompoundTag> {
    public int oldDetermination = 0;
    public ItemStack determinationSelectedItem = ItemStack.EMPTY;
    public int determinationDifference = 0;
    public int determinationTargetTicker = -1;
    public int determinationDifferenceTicker = -1;
    public int determinationAppearTicker = -1;

    public void tick(Level level, Player player) {
        //Determination bar stuff below this point
        if (determinationTargetTicker >= 6) {
            determinationTargetTicker = -1;
            oldDetermination = 0;
        }
        if (determinationTargetTicker >= 0) {
            determinationTargetTicker++;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offhandStack = player.getOffhandItem();

        SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);
        int determination = soulCap.determination;

        if (isDeterminationItem(mainHandStack, determination)) {
            if (determinationSelectedItem.getItem() != mainHandStack.getItem()) {
                determinationSelectedItem = mainHandStack.copy();
                determinationDifferenceTicker = 0;
                determinationDifference = getDeterminationFromItemStack(mainHandStack, determination);
            }
        } else if (isDeterminationItem(offhandStack, determination)) {
            if (determinationSelectedItem.getItem() != offhandStack.getItem()) {
                determinationSelectedItem = offhandStack.copy();
                determinationDifferenceTicker = 0;
                determinationDifference = getDeterminationFromItemStack(offhandStack, determination);
            }
        } else {
            if (!determinationSelectedItem.isEmpty()) {
                determinationSelectedItem = ItemStack.EMPTY.copy();
            }
        }

        ScreenAnimationCapability animCap = player.getCapability(CapabilityRegistry.SCREEN_ANIMATION).orElse(null);
        int darknessOverlayTicker = animCap.darknessOverlayTicker;

        if (DarkWorldUtil.isDepths(level) && darknessOverlayTicker == -1) {
            if (determinationAppearTicker < 10) {
                determinationAppearTicker++;
            }

            if (isDeterminationItem(determinationSelectedItem, determination)) {
                if (determinationTargetTicker == -1 && determinationDifferenceTicker < 6) {
                    determinationDifferenceTicker++;
                }
            } else {
                if (determinationDifferenceTicker >= 0) {
                    determinationDifferenceTicker--;
                }
                if (determinationDifferenceTicker == -1) {
                    determinationDifference = 0;
                }
            }
        } else {
            if (isDeterminationItem(determinationSelectedItem, determination)) {
                if (determinationAppearTicker < 10) {
                    determinationAppearTicker++;
                }
                if (determinationTargetTicker == -1 && determinationDifferenceTicker < 6) {
                    determinationDifferenceTicker++;
                }
            } else {
                if (determinationAppearTicker >= 0) {
                    determinationAppearTicker--;
                }
                if (determinationDifferenceTicker >= 0) {
                    determinationDifferenceTicker--;
                }
                if (determinationDifferenceTicker == -1) {
                    determinationDifference = 0;
                }
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    public int getDeterminationFromItemStack(ItemStack stack, int determination) {
        if (stack.getItem() instanceof DeterminationInjectionItem) {
            return 100;
        } else if (stack.getItem() instanceof EmptyInjectionItem && determination >= 100) {
            return -100;
        } else if (stack.getItem() instanceof DarkWorldFoodItem determinationItem) {
            return determinationItem.determinationDifference;
        } else if (stack.getItem() instanceof DarkWorldFoodFlavorItem determinationItem) {
            return determinationItem.determinationDifference;
        }

        return 0;
    }

    public boolean isDeterminationItem(ItemStack stack, int determination) {
        return getDeterminationFromItemStack(stack, determination) != 0;
    }

    public void syncToClient(ServerPlayer serverPlayer) {
        PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundVerticalBarPacket(oldDetermination,
                determinationSelectedItem, determinationDifference, determinationTargetTicker, determinationDifferenceTicker, determinationAppearTicker));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
    }
}
