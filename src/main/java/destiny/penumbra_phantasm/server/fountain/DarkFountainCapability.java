package destiny.penumbra_phantasm.server.fountain;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.UUID;

public class DarkFountainCapability implements INBTSerializable<CompoundTag> {

    private static final String DARK_FOUNTAINS = "dark_fountains";

    public HashMap<UUID, DarkFountain> darkFountains = new HashMap<>();

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.put(DARK_FOUNTAINS, serializeDarkFountains());

        return tag;
    }

    private CompoundTag serializeDarkFountains() {
        CompoundTag objectsTag = new CompoundTag();

        this.darkFountains.forEach((uuid, pad) -> {
            objectsTag.put(uuid.toString(), pad.save());
        });

        return objectsTag;
    }

    public void addDarkFountain(UUID uuid, BlockPos fountainPos, ResourceKey<Level> fountainDimension, BlockPos destinationPos, ResourceKey<Level> destinationDimension, int animationTick) {
        this.darkFountains.put(uuid, new DarkFountain(fountainPos, fountainDimension, destinationPos, destinationDimension, animationTick));
    }

    public void removeDarkFountain(UUID uuid) {
        this.darkFountains.remove(uuid);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        deserializeDarkFountains(tag.getCompound(DARK_FOUNTAINS));
    }

    private void deserializeDarkFountains(CompoundTag tag) {
        for(String key : tag.getAllKeys()) {
            this.darkFountains.put(UUID.fromString(key), DarkFountain.load(tag.getCompound(key)));
        }
    }
}
