package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundIntroPacket
{
	public ClientBoundIntroPacket()
	{

	}

	public void encode(FriendlyByteBuf buffer)
	{

	}

	public static ClientBoundIntroPacket decode(FriendlyByteBuf buffer)
	{
		return new ClientBoundIntroPacket();
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			Minecraft minecraft = Minecraft.getInstance();

			minecraft.setScreen(new IntroScreen(() -> {
				minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
				minecraft.setScreen((Screen)null);
			}));
		});
		return true;
	}
}
