package destiny.penumbra_phantasm.client.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSoulSyncPacket {
	public boolean seenIntro;
	public boolean diedWithSoulHearth;
	public int soulType;
	public int determination;
	public int connectionLevel;

	public ClientBoundSoulSyncPacket(boolean seenIntro, boolean diedWithSoulHearth, int soulType, int determination, int connectionLevel) {
		this.seenIntro = seenIntro;
		this.diedWithSoulHearth = diedWithSoulHearth;
		this.soulType = soulType;
		this.determination = determination;
		this.connectionLevel = connectionLevel;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.seenIntro);
		buffer.writeBoolean(this.diedWithSoulHearth);
		buffer.writeInt(this.soulType);
		buffer.writeInt(this.determination);
		buffer.writeInt(this.connectionLevel);
	}

	public static ClientBoundSoulSyncPacket decode(FriendlyByteBuf buffer) {
		boolean seenIntro = buffer.readBoolean();
		boolean diedWithSoulHearth = buffer.readBoolean();
		int soulType = buffer.readInt();
		int determination = buffer.readInt();
		int connectionLevel = buffer.readInt();

		return new ClientBoundSoulSyncPacket(seenIntro, diedWithSoulHearth, soulType, determination, connectionLevel);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientBoundPacketHandler.syncSoulStuff(seenIntro, diedWithSoulHearth, soulType, determination, connectionLevel));
		return true;
	}
}
