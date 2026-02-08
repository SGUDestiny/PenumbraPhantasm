package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.client.network.ClientBoundIntroPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class SoulCapability implements INBTSerializable<CompoundTag> {
    public static final String SEEN_INTRO = "seenIntro";
    public static final String SOUL_TYPE = "soulType";
    public static final String MADE_SOUL_HEARTH = "madeSoulHearth";
    public static final String DIED_WITH_SOUL_HEARTH = "diedSoulHearth";

    public boolean seenIntro = false;
    public int soulType = 1;
    public boolean madeSoulHearth = false;

    public boolean diedWithSoulHearth = false;

    public void tick(Level level, Player player) {
        if (!seenIntro) {
            PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player), new ClientBoundIntroPacket(player.getOnPos().above(), player.level().dimension()));
            seenIntro = true;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean(SEEN_INTRO, seenIntro);
        tag.putInt(SOUL_TYPE, Mth.clamp(soulType, 1, 7));
        tag.putBoolean(MADE_SOUL_HEARTH, madeSoulHearth);
        tag.putBoolean(DIED_WITH_SOUL_HEARTH, diedWithSoulHearth);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.seenIntro = tag.getBoolean(SEEN_INTRO);
        this.soulType = Mth.clamp(tag.getInt(SOUL_TYPE), 1, 7);
        this.madeSoulHearth = tag.getBoolean(MADE_SOUL_HEARTH);
        this.diedWithSoulHearth = tag.getBoolean(DIED_WITH_SOUL_HEARTH);
    }

    public void sync(@NotNull SoulCapability cap)
    {
        this.soulType = cap.soulType;
        this.madeSoulHearth = cap.madeSoulHearth;
        this.diedWithSoulHearth = cap.diedWithSoulHearth;
        this.seenIntro = cap.seenIntro;
    }
}
