package destiny.penumbra_phantasm.server.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class SoulCapability implements INBTSerializable<CompoundTag> {
    public static final String SEEN_INTRO = "seenIntro";
    public static final String SOUL_TYPE = "soul_type";

    public boolean seenIntro = false;
    public SoulType soulType = SoulType.DETERMINATION;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean(SEEN_INTRO, seenIntro);
        tag.putInt(SOUL_TYPE, soulType.id);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.seenIntro = tag.getBoolean(SEEN_INTRO);
        this.soulType = SoulType.byId(tag.getInt(SOUL_TYPE));
    }
}
