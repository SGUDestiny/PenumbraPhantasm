package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class AnchorOfLightCapability implements INBTSerializable<CompoundTag> {
    public static final String ANCHORS = "anchors";

    public Set<BlockPos> anchors = new HashSet<>();

    public AnchorOfLightCapability() {}

    public boolean isProtectedByAnchor(AnchorOfLightCapability cap, BlockPos pos) {
        List<BlockPos> anchors = new ArrayList<>(cap.anchors.stream().toList());

        anchors.sort((Comparator.comparingDouble(pos1 -> pos.getCenter().distanceTo(pos.getCenter()))));

        return !anchors.isEmpty() && pos.distSqr(anchors.get(0)) < Mth.square(64);
    }

    public void revalidateAnchorsForLevel(Level level, AnchorOfLightCapability cap) {
        Set<BlockPos> anchors = cap.anchors;
        List<BlockPos> toRemove = new ArrayList<>();

        for (BlockPos pos : anchors) {
            if (!level.getBlockState(pos).is(BlockRegistry.ANCHOR_OF_LIGHT.get())) {
                toRemove.add(pos);
            }
        }

        for (BlockPos pos : toRemove) {
            cap.anchors.remove(pos);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag listTag = new ListTag();
        for (BlockPos pos : anchors) {
            listTag.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put(ANCHORS, listTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        ListTag listTag = compoundTag.getList(ANCHORS, ListTag.TAG_COMPOUND);

        for (Tag tag : listTag) {
            this.anchors.add(NbtUtils.readBlockPos((CompoundTag) tag));
        }
    }

    public void sync(AnchorOfLightCapability cap) {
        this.anchors = cap.anchors;
    }
}
