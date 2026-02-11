package destiny.penumbra_phantasm.server.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;

import java.util.function.Supplier;

public class MusicMediumItem extends RecordItem {
    public MusicMediumItem(int comparatorValue, Supplier<SoundEvent> soundSupplier, Properties builder, int lengthInTicks) {
        super(comparatorValue, soundSupplier, builder, lengthInTicks);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable(this.getDescriptionId(pStack)).withStyle(Style.EMPTY.withColor(getColor()));
    }

    public static int getColor() {
        int period = 10000;
        long time = System.currentTimeMillis();

        float cyclePos = (float) ((time % period) / (double) period);

        float angle = (float) (cyclePos * 2 * Math.PI);

        int red = (int) ((Math.sin(angle) * 127) + 128);
        int green = (int) ((Math.sin(angle + 2 * Math.PI / 3) * 127) + 128);
        int blue = (int) ((Math.sin(angle + 4 * Math.PI / 3) * 127) + 128);

        return (red << 16) | (green << 8) | blue;
    }
}
