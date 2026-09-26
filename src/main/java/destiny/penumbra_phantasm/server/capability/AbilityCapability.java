package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.client.network.ClientBoundAbilityPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AbilityCapability implements INBTSerializable<CompoundTag> {
    public static final String TENSION = "tension";

    public List<DelayTicker> delayTickers = new ArrayList<>();

    public int tension = 0;

    public void tick(Level level, Player attacker) {
        List<DelayTicker> tickersToRemove = new ArrayList<>();

        for (DelayTicker delayTicker : delayTickers) {
            if (delayTicker.delayTicker == -1) {
                tickersToRemove.add(delayTicker);
            } else {
                delayTicker.tick(level, attacker);
            }
        }

        for (DelayTicker delayTicker : tickersToRemove) {
            delayTickers.remove(delayTicker);
        }

        if (attacker instanceof ServerPlayer serverPlayer) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientBoundAbilityPacket(tension));
        }
    }

    public boolean hasDelayTicker(UUID targetEntity, ItemStack weaponStack) {
        for (DelayTicker ticker : delayTickers) {
            UUID tickerEntity = ticker.targetEntity;
            ItemStack tickerWeapon = ticker.weaponStack;

            if (tickerEntity.equals(targetEntity) && tickerWeapon.getItem() == weaponStack.getItem()) return true;
        }

        return false;
    }

    public boolean hasDelayTickerOfItem(Item weaponItem) {
        for (DelayTicker ticker : delayTickers) {
            Item tickerWeapon = ticker.weaponStack.getItem();

            if (tickerWeapon == weaponItem) return true;
        }

        return false;
    }

    public void createDelayTicker(UUID targetEntity, ItemStack weaponStack, int delayGoal) {
        DelayTicker delayTicker = new DelayTicker(targetEntity, weaponStack, delayGoal, 0);

        delayTickers.add(delayTicker);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putInt(TENSION, tension);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.tension = tag.getInt(TENSION);
    }

    public void sync(@NotNull AbilityCapability cap) {
        this.tension = cap.tension;
    }
}