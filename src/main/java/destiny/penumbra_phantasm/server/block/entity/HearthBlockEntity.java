package destiny.penumbra_phantasm.server.block.entity;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.client.network.ClientBoundCancelPlayerAnimationPacket;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static destiny.penumbra_phantasm.server.item.HearthSoulItem.HEARTH_POS;
import static destiny.penumbra_phantasm.server.item.SoulHearthItem.SOUL_TYPE;
import static destiny.penumbra_phantasm.server.registry.DamageTypeRegistry.SOUL_DAMAGE_1;
import static destiny.penumbra_phantasm.server.registry.DamageTypeRegistry.SOUL_DAMAGE_2;

public class HearthBlockEntity extends BlockEntity {
    public static final String SOUL_RIP_TICKER = "soulRipTicker";
    public static final String PLAYER_UUID = "playerUuid";
    public static final int SOUL_RIP_TIME = 4 * 20;

    public int soulRipTicker = -1;
    public UUID playerUuid;

    public HearthBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.HEARTH_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HearthBlockEntity hearth) {
        if (level.isClientSide()) return;

        if (hearth.playerUuid == null) return;

        if (hearth.soulRipTicker > -1 && hearth.soulRipTicker < SOUL_RIP_TIME) {
            hearth.soulRipTicker = hearth.soulRipTicker + 1;

            Player player = level.getPlayerByUUID(hearth.playerUuid);

            if (player == null) return;

            if (player.getOnPos().getCenter().distanceTo(pos.getCenter()) > 8 || player.isDeadOrDying()) {
                hearth.soulRipTicker = -1;
                hearth.playerUuid = null;

                PacketHandlerRegistry.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new ClientBoundCancelPlayerAnimationPacket(player.getId(), new ResourceLocation(PenumbraPhantasm.MODID, "soul_rip"))
                );
                return;
            }

            if (hearth.soulRipTicker == 23) {
                level.playSound(null, player.getOnPos(), SoundRegistry.SOUL_GRAB.get(), SoundSource.PLAYERS, 0.5f, 1);
                player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, SOUL_DAMAGE_1), 1);
            }
            if (hearth.soulRipTicker == 2 * 20) {
                player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, SOUL_DAMAGE_1), 1);
            }
            if (hearth.soulRipTicker == 2.75 * 20) {
                player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, SOUL_DAMAGE_1), 1);
            }
            if (hearth.soulRipTicker == 3.25 * 20) {
                player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, SOUL_DAMAGE_1), 1);
            }
            if (hearth.soulRipTicker == 3.63 * 20) {
                player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, SOUL_DAMAGE_1), 1);
            }
            if (hearth.soulRipTicker == 4 * 20) {
                level.playSound(null, player.getOnPos(), SoundRegistry.SOUL_GRAB.get(), SoundSource.PLAYERS, 0.5f, 1);
                player.hurt(DamageTypeRegistry.getSimpleDamageSource(level, SOUL_DAMAGE_2), 1);
            }
            hearth.setChanged();

            return;
        }

        if (hearth.soulRipTicker == SOUL_RIP_TIME) {
            hearth.soulRipTicker = -1;

            Player player = level.getPlayerByUUID(hearth.playerUuid);

            if (player == null) return;

            SoulCapability soulCap = player.getCapability(CapabilityRegistry.SOUL).orElse(null);

            ItemStack soul = new ItemStack(ItemRegistry.HEARTH_SOUL.get());
            soul.getOrCreateTag().put(HEARTH_POS, NbtUtils.writeBlockPos(pos));
            soul.getOrCreateTag().putInt(SOUL_TYPE, soulCap.soulType);

            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, soul);
            } else {
                player.addItem(soul);
            }

            hearth.setChanged();
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.soulRipTicker = tag.getInt(SOUL_RIP_TICKER);
        if (playerUuid != null) {
            this.playerUuid = tag.getUUID(PLAYER_UUID);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(SOUL_RIP_TICKER, soulRipTicker);
        if (playerUuid != null) {
            tag.putUUID(PLAYER_UUID, playerUuid);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
