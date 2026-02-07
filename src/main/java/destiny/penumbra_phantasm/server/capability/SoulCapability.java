package destiny.penumbra_phantasm.server.capability;

import destiny.penumbra_phantasm.client.network.ClientBoundIntroPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;

public class SoulCapability implements INBTSerializable<CompoundTag> {
    public static final String SEEN_INTRO = "seenIntro";
    public static final String SOUL_TYPE = "soul_type";

    public boolean seenIntro = false;
    public SoulType soulType = SoulType.DETERMINATION;

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
        tag.putInt(SOUL_TYPE, soulType.id);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.seenIntro = tag.getBoolean(SEEN_INTRO);
        this.soulType = SoulType.byId(tag.getInt(SOUL_TYPE));
    }
}
