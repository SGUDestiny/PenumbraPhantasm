package destiny.penumbra_phantasm.server.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class AbilityCapability implements INBTSerializable<CompoundTag> {
    public List<DelayTicker> delayTickers = new ArrayList<>();

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
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {

    }
}
