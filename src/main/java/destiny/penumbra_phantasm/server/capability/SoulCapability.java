package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.client.network.ClientBoundIntroPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundSoulSyncPacket;
import destiny.penumbra_phantasm.server.item.SoulHearthItem;
import destiny.penumbra_phantasm.server.registry.ItemRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

//TODO:
// - Transition soul hearth stuff to the capability

public class SoulCapability implements INBTSerializable<CompoundTag> {
    public static final String SEEN_INTRO = "seenIntro";
    public static final String SOUL_TYPE = "soulType";
    public static final String DIED_WITH_SOUL_HEARTH = "diedSoulHearth";
    public static final String DETERMINATION = "determination";
    public static final String CONNECTION_LEVEL = "connectionLevel";

    public boolean seenIntro = false;
    public int soulType = 1;
    public boolean diedWithSoulHearth = false;
    public int determination = 0;
    public int connectionLevel = 0;

    public void tick(Level level, Player player) {
        if (!seenIntro) {
            if (player instanceof ServerPlayer serverPlayer) {
                ServerLevel serverLevel = serverPlayer.serverLevel();
                PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundIntroPacket(player.getOnPos().above(), player.level().dimension()));
                serverLevel.getServer().execute(() -> {
                    if (!serverPlayer.hasDisconnected()) {
                        serverLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
                    }
                });
            }
            seenIntro = true;
        }

        if (!DarkWorldUtil.isDepths(level)) {
            if (hasOwnSoulHearth(player) && determination < 100) {
                if (level.getGameTime() % (5 * 20) == 0) {
                    determination = determination + 1;
                }
            }
        } else {
            if (determination > 0) {
                if (level.getGameTime() % (5 * 20) == 0) {
                    determination = determination - 1;
                }
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundSoulSyncPacket(seenIntro, diedWithSoulHearth, soulType, determination, connectionLevel));
        }
    }

    public static boolean hasOwnSoulHearth(Player player) {
        UUID playerUUID = player.getUUID();

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof SoulHearthItem) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.hasUUID(SoulHearthItem.OWNER_UUID) && playerUUID.equals(tag.getUUID(SoulHearthItem.OWNER_UUID))) {
                    return true;
                }
            }
        }

        ItemStack offhand = player.getInventory().offhand.get(0);
        if (!offhand.isEmpty() && offhand.getItem() instanceof SoulHearthItem) {
            CompoundTag tag = offhand.getTag();

            return tag != null && tag.hasUUID(SoulHearthItem.OWNER_UUID) && playerUUID.equals(tag.getUUID(SoulHearthItem.OWNER_UUID));
        }

        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean(SEEN_INTRO, seenIntro);
        tag.putInt(SOUL_TYPE, Mth.clamp(soulType, 1, 7));
        tag.putBoolean(DIED_WITH_SOUL_HEARTH, diedWithSoulHearth);
        tag.putInt(DETERMINATION, Mth.clamp(determination, 0, 100));
        tag.putInt(CONNECTION_LEVEL, Mth.clamp(connectionLevel, 0, 3));

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.seenIntro = tag.getBoolean(SEEN_INTRO);
        this.soulType = Mth.clamp(tag.getInt(SOUL_TYPE), 1, 7);
        this.diedWithSoulHearth = tag.getBoolean(DIED_WITH_SOUL_HEARTH);
        this.determination = tag.getInt(DETERMINATION);
        this.connectionLevel = tag.getInt(CONNECTION_LEVEL);
    }

    public void sync(@NotNull SoulCapability cap) {
        this.soulType = cap.soulType;
        this.diedWithSoulHearth = cap.diedWithSoulHearth;
        this.seenIntro = cap.seenIntro;
        this.determination = cap.determination;
        this.connectionLevel = cap.connectionLevel;
    }
}
