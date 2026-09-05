package destiny.penumbra_phantasm.client.render.screen;

import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.egg_room.CardKingdomEggRoomUtil;
import destiny.penumbra_phantasm.server.network.ServerBoundEggRoomReadyPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class EggRoomCoverScreen extends Screen {
	public static final ResourceLocation BLACK_SCREEN = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/black_screen.png");
	private static final int TIMEOUT_TICKS = 160;
	private static ResourceKey<Level> pendingDimension;
	private static int pendingChunkX;
	private static int pendingChunkZ;
	private static boolean pending;

	private ResourceKey<Level> dimension;
	private int chunkX;
	private int chunkZ;
	private int ticks;
	private boolean readySent;

	public EggRoomCoverScreen(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
		super(GameNarrator.NO_TITLE);
		this.dimension = dimension;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}

	public static boolean isCovering() {
		return pending;
	}

	public static Screen replaceLoadingScreen() {
		Minecraft minecraft = Minecraft.getInstance();
		if (pending && minecraft.screen instanceof EggRoomCoverScreen existing) {
			return existing;
		}
		if (pending && pendingDimension != null) {
			return new EggRoomCoverScreen(pendingDimension, pendingChunkX, pendingChunkZ);
		}
		if (minecraft.player != null && minecraft.player.level() != null && CardKingdomEggRoomUtil.isEggRoom(minecraft.player.level())) {
			ChunkPos spawn = new ChunkPos(BlockPos.containing(CardKingdomEggRoomUtil.SPAWN_X, CardKingdomEggRoomUtil.SPAWN_Y, CardKingdomEggRoomUtil.SPAWN_Z));
			pending = true;
			pendingDimension = minecraft.player.level().dimension();
			pendingChunkX = spawn.x;
			pendingChunkZ = spawn.z;
			return new EggRoomCoverScreen(pendingDimension, pendingChunkX, pendingChunkZ);
		}
		return null;
	}

	public static void open(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
		pending = true;
		pendingDimension = dimension;
		pendingChunkX = chunkX;
		pendingChunkZ = chunkZ;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen instanceof EggRoomCoverScreen cover) {
			cover.dimension = dimension;
			cover.chunkX = chunkX;
			cover.chunkZ = chunkZ;
			cover.ticks = 0;
			cover.readySent = false;
			return;
		}
		minecraft.setScreen(new EggRoomCoverScreen(dimension, chunkX, chunkZ));
	}

	@Override
	public void tick() {
		if (readySent) {
			return;
		}

		ticks++;

		Minecraft minecraft = Minecraft.getInstance();
		boolean chunkReady = minecraft.level != null && minecraft.level.dimension().equals(dimension) && minecraft.level.getChunkSource().getChunkNow(chunkX, chunkZ) != null;
		if (chunkReady) {
			finish(true);
		} else if (ticks >= TIMEOUT_TICKS) {
			finish(false);
		}
	}

	private void finish(boolean sendReady) {
		if (readySent) {
			return;
		}
		readySent = true;
		pending = false;
		pendingDimension = null;
		if (sendReady) {
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundEggRoomReadyPacket());
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen == this) {
			minecraft.setScreen(null);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.blit(BLACK_SCREEN, 0, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void onClose() {
	}
}
