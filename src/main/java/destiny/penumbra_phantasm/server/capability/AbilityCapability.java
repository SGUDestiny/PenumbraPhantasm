package destiny.penumbra_phantasm.server.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
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
            delayTickers.remove(index);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {

    }
}
