package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.client.render.screen.DarknessFallScreen;
import destiny.penumbra_phantasm.client.render.screen.IntroScreen;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ClientBoundPacketHandler
{
	public static void openIntroScreen(BlockPos pos, ResourceKey<Level> dim)
	{
		Minecraft minecraft = Minecraft.getInstance();

		minecraft.setScreen(new IntroScreen(() -> {
			minecraft.setScreen(null);
			PacketHandlerRegistry.INSTANCE.sendToServer(new ServerBoundIntroPacket(pos, dim));
		}));
	}

	public static void openDarknessFallScreen(BlockPos destinationPos, double spawnX, double spawnY, double spawnZ, float spawnYaw, ResourceKey<Level> dim) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new DarknessFallScreen(() -> minecraft.setScreen(null), destinationPos, spawnX, spawnY, spawnZ, spawnYaw, dim));
	}

	public static void syncSoulBreak(boolean diedWithSoulHearth, int soulType)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if(player != null)
			player.getCapability(CapabilityRegistry.SOUL).ifPresent(cap ->
				{
					cap.diedWithSoulHearth = diedWithSoulHearth;
					cap.soulType = soulType;
				});
	}

	public static void sendParticle(ResourceLocation particleId, double x, double y, double z, double vx, double vy, double vz, int count) {
		Level level = Minecraft.getInstance().level;

		if (level == null) return;

		ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(particleId);

		if (!(type instanceof SimpleParticleType simpleType)) return;

		for (int i = 0; i < count; i++) {
			level.addParticle(simpleType, x, y, z, vx, vy, vz);
		}
	}
}
