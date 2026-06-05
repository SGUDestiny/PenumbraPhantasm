package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HearthSoulItem extends Item {
    public static final String HEARTH_POS = "hearthPos";

    public HearthSoulItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;

        if (stack.getTag() == null) {
            stack.shrink(stack.getCount());
        }

        if (stack.getTag().get(HEARTH_POS) == null) {
            stack.shrink(stack.getCount());
        }

        BlockPos hearthPos = NbtUtils.readBlockPos(stack.getTag().getCompound(HEARTH_POS));

        if (!level.getBlockState(hearthPos).is(BlockRegistry.HEARTH.get())) {
            stack.shrink(stack.getCount());
        }

        if (entity.getOnPos().getCenter().distanceTo(hearthPos.getCenter()) > 8) {
            stack.shrink(stack.getCount());
        }
    }
}
