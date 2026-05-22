package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

public class CheshireChestCapability implements ICapabilitySerializable<CompoundTag> {
    private CheshireChestInventory inventory = new CheshireChestInventory();
    private LazyOptional<CheshireChestInventory> optional = LazyOptional.of(() -> inventory);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityRegistry.CHESHIRE_CHEST) {
            return this.optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }
}
