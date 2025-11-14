package destiny.penumbra_phantasm.server.fountain;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GenericProvider <T extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {
    private final T object;
    private final LazyOptional<T> holder;
    private final Capability<T> instance;

    public GenericProvider(Capability<T> instance, T object) {
        this.instance = instance;
        this.object = object;
        this.holder = LazyOptional.of(() -> this.object);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == this.instance ? this.holder.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.object.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.object.deserializeNBT(nbt);
    }
}