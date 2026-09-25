package destiny.penumbra_phantasm.server.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class AbilityCapability implements INBTSerializable<CompoundTag> {
    public List<DelayTicker> delayTickers = new ArrayList<>();

    public void tick(Level level, Player attacker) {
        List<Integer> tickersToRemove = new ArrayList<>();

        for (int i = 0; i < delayTickers.size(); i++) {
            DelayTicker delayTicker = delayTickers.get(i);

            if (delayTicker.delayTicker == -1) {
                tickersToRemove.add(i);
            } else {
                delayTicker.tick(level, attacker);
            }
        }

        for (int index : tickersToRemove) {
            if (index >= delayTickers.size()) continue;
            if (delayTickers.get(index) == null) continue;

            delayTickers.remove(index);
        }
    }

    public boolean isDelayTickerFree(UUID targetEntity, ItemStack weaponStack) {
        for (DelayTicker ticker : delayTickers) {
            UUID tickerEntity = ticker.targetEntity;
            ItemStack tickerWeapon = ticker.weaponStack;

            if (tickerEntity.equals(targetEntity) && tickerWeapon.getItem() == weaponStack.getItem()) return false;
        }

        return true;
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
