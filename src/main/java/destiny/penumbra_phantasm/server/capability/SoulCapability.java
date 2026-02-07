package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.client.network.ClientBoundIntroPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;

public class SoulCapability implements INBTSerializable<CompoundTag> {
    public static final String SEEN_INTRO = "seenIntro";
    public static final String SOUL_TYPE = "soulType";
    public static final String MADE_SOUL_HEARTH = "madeSoulHearth";

    public boolean seenIntro = false;
    public int soulType = 1;
    public boolean madeSoulHearth = false;

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
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.seenIntro = tag.getBoolean(SEEN_INTRO);
        this.soulType = Mth.clamp(tag.getInt(SOUL_TYPE), 1, 7);
        this.madeSoulHearth = tag.getBoolean(MADE_SOUL_HEARTH);
    }
}
