package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.client.network.ClientBoundIntroPacket;
import destiny.penumbra_phantasm.client.network.ClientBoundSoulSyncPacket;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.item.SoulHearthItem;
import destiny.penumbra_phantasm.server.registry.*;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

//TODO:
// - Transition soul hearth stuff to the capability

public class SoulCapability implements INBTSerializable<CompoundTag> {
    public static final String SEEN_INTRO = "seenIntro";
    public static final String SOUL_TYPE = "soulType";
    public static final String DIED_WITH_SOUL_HEARTH = "diedSoulHearth";
    public static final String DETERMINATION = "determination";
    public static final String CONNECTION_LEVEL = "connectionLevel";
    public static final String EGG_ROOM_MAN_GONE = "eggRoomManGone";
    public static final String EGG_OBTAINED = "eggObtained";
    public static final String EGG_RETURN_DIM = "eggReturnDim";
    public static final String EGG_RETURN_X = "eggReturnX";
    public static final String EGG_RETURN_Y = "eggReturnY";
    public static final String EGG_RETURN_Z = "eggReturnZ";
    public static final String EGG_RETURN_YAW = "eggReturnYaw";
    public static final String EGG_DOOR_X = "eggDoorX";
    public static final String EGG_DOOR_Y = "eggDoorY";
    public static final String EGG_DOOR_Z = "eggDoorZ";
	public static final String EGG_LEFT_ENTRANCE = "eggLeftEntrance";
	public static final String EGG_ROOM_FRONT_HINT = "eggRoomFrontHint";

    public boolean seenIntro = false;
    public int soulType = 1;
    public boolean diedWithSoulHearth = false;
    public int determination = 0;
    public int connectionLevel = 0;

    public int eggRoomManGone = 0;
    public int eggObtained = 0;
    public String eggReturnDim = "";
    public double eggReturnX;
    public double eggReturnY;
    public double eggReturnZ;
    public float eggReturnYaw;
    public int eggDoorX;
    public int eggDoorY;
    public int eggDoorZ;
    public boolean eggLeftEntrance;
    public int eggRoomFrontHint;

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

        if (isInNegativePhotons(level, player)) {
            if (!player.isCreative() && !player.isSpectator()) {
                if (level.getGameTime() % 5 == 0) {
                    if (determination > 0) {
                        determination = determination - 1;
                    }
                }
            }
        } else if (!DarkWorldUtil.isDepths(level)) {
            if (hasOwnSoulHearth(player)) {
                if (determination < 100) {
                    if (level.getGameTime() % (5 * 20) == 0) {
                        determination = determination + 1;
                    }
                }
            }
        } else {
            if (determination > 0) {
                DarkFountainCapability fountainCapability = null;
                LazyOptional<DarkFountainCapability> lazyFountainCapability = level.getCapability(CapabilityRegistry.DARK_FOUNTAIN);
                if (lazyFountainCapability.isPresent() && lazyFountainCapability.resolve().isPresent()) {
                    fountainCapability = lazyFountainCapability.resolve().get();
                }

                if (fountainCapability != null) {
                    BlockPos playerPos = player.blockPosition();

                    for (Map.Entry<BlockPos, DarkFountain> entry : fountainCapability.darkFountains.entrySet()) {
                        BlockPos fountainPos = entry.getKey();

                        if (playerPos.distSqr(fountainPos) > Mth.square(96)) {
                            if (!player.isCreative() && !player.isSpectator()) {
                                if (level.getGameTime() % (5 * 20) == 0) {
                                    determination = determination - 1;
                                }
                            }
                            break;
                        }
                    }
                }
            } else {
                //Remove all potentially beneficial effects so players suffer
                List<MobEffect> effectsToRemove = new ArrayList<>();
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    MobEffectCategory category = effect.getEffect().getCategory();

                    if (category == MobEffectCategory.BENEFICIAL || category == MobEffectCategory.NEUTRAL) {
                        effectsToRemove.add(effect.getEffect());
                    }
                }
                for (MobEffect mobEffect : effectsToRemove) {
                    player.removeEffect(mobEffect);
                }

                //Remove saturation so players suffer even more
                player.getFoodData().setSaturation(0f);

                if (level.getGameTime() % 60 == 0) {
                    player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, DamageTypeRegistry.EROSION), 6);
                }
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundSoulSyncPacket(seenIntro, diedWithSoulHearth, soulType, determination, connectionLevel, eggRoomManGone, eggObtained));
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

    public static boolean isInNegativePhotons(Level level, Player player) {
        FluidType fluidUp = player.getEyeInFluidType();
        FluidType fluidDown = level.getFluidState(player.getOnPos().above()).getFluidType();

        return fluidUp == FluidTypeRegistry.NEGATIVE_PHOTONS.get() || fluidDown == FluidTypeRegistry.NEGATIVE_PHOTONS.get();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean(SEEN_INTRO, seenIntro);
        tag.putInt(SOUL_TYPE, Mth.clamp(soulType, 1, 7));
        tag.putBoolean(DIED_WITH_SOUL_HEARTH, diedWithSoulHearth);
        tag.putInt(DETERMINATION, Mth.clamp(determination, 0, 100));
        tag.putInt(CONNECTION_LEVEL, Mth.clamp(connectionLevel, 0, 3));
        tag.putInt(EGG_ROOM_MAN_GONE, eggRoomManGone);
        tag.putInt(EGG_OBTAINED, eggObtained);
        tag.putString(EGG_RETURN_DIM, eggReturnDim == null ? "" : eggReturnDim);
        tag.putDouble(EGG_RETURN_X, eggReturnX);
        tag.putDouble(EGG_RETURN_Y, eggReturnY);
        tag.putDouble(EGG_RETURN_Z, eggReturnZ);
        tag.putFloat(EGG_RETURN_YAW, eggReturnYaw);
        tag.putInt(EGG_DOOR_X, eggDoorX);
        tag.putInt(EGG_DOOR_Y, eggDoorY);
        tag.putInt(EGG_DOOR_Z, eggDoorZ);
        tag.putInt(EGG_ROOM_FRONT_HINT, eggRoomFrontHint);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.seenIntro = tag.getBoolean(SEEN_INTRO);
        this.soulType = Mth.clamp(tag.getInt(SOUL_TYPE), 1, 7);
        this.diedWithSoulHearth = tag.getBoolean(DIED_WITH_SOUL_HEARTH);
        this.determination = tag.getInt(DETERMINATION);
        this.connectionLevel = tag.getInt(CONNECTION_LEVEL);
        this.eggRoomManGone = tag.getInt(EGG_ROOM_MAN_GONE);
        this.eggObtained = tag.getInt(EGG_OBTAINED);
        this.eggReturnDim = tag.getString(EGG_RETURN_DIM);
        this.eggReturnX = tag.getDouble(EGG_RETURN_X);
        this.eggReturnY = tag.getDouble(EGG_RETURN_Y);
        this.eggReturnZ = tag.getDouble(EGG_RETURN_Z);
        this.eggReturnYaw = tag.getFloat(EGG_RETURN_YAW);
        this.eggDoorX = tag.getInt(EGG_DOOR_X);
        this.eggDoorY = tag.getInt(EGG_DOOR_Y);
        this.eggDoorZ = tag.getInt(EGG_DOOR_Z);
        this.eggLeftEntrance = false;
        this.eggRoomFrontHint = tag.getInt(EGG_ROOM_FRONT_HINT);
    }

    public boolean hasEggRoomManGone(int bit) {
        return (eggRoomManGone & bit) != 0;
    }

    public void setEggRoomManGone(int bit) {
        eggRoomManGone |= bit;
    }

    public boolean hasEggObtained(int bit) {
        return (eggObtained & bit) != 0;
    }

    public void setEggObtained(int bit) {
        eggObtained |= bit;
    }

    public void sync(@NotNull SoulCapability cap) {
        this.soulType = cap.soulType;
        this.diedWithSoulHearth = cap.diedWithSoulHearth;
        this.seenIntro = cap.seenIntro;
        this.determination = cap.determination;
        this.connectionLevel = cap.connectionLevel;
        this.eggRoomManGone = cap.eggRoomManGone;
        this.eggObtained = cap.eggObtained;
        this.eggReturnDim = cap.eggReturnDim;
        this.eggReturnX = cap.eggReturnX;
        this.eggReturnY = cap.eggReturnY;
        this.eggReturnZ = cap.eggReturnZ;
        this.eggReturnYaw = cap.eggReturnYaw;
        this.eggDoorX = cap.eggDoorX;
        this.eggDoorY = cap.eggDoorY;
        this.eggDoorZ = cap.eggDoorZ;
        this.eggRoomFrontHint = cap.eggRoomFrontHint;
    }
}
