package destiny.penumbra_phantasm.server.util;

import com.mojang.datafixers.util.Pair;
import destiny.penumbra_phantasm.client.sounds.DarkFountainSoundWrapper;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.UUID;

public class FountainSoundUtil
{
	public static HashMap<BlockPos, DarkFountainSoundWrapper.DarkFountainMusic> AMBIENT_MUSICS = new HashMap<>();
	public static HashMap<BlockPos, DarkFountainSoundWrapper.DarkFountainWind> AMBIENT_DARK_WINDS = new HashMap<>();
	public static HashMap<BlockPos, DarkFountainSoundWrapper.DarkFountainWind> AMBIENT_LIGHT_WINDS = new HashMap<>();


	public static DarkFountainSoundWrapper.DarkFountainMusic getAmbientMusicSound(BlockPos pos)
	{
		return AMBIENT_MUSICS.getOrDefault(pos, null);
	}

	public static void setAmbientSound(DarkFountainSoundWrapper.DarkFountainMusic ambient, BlockPos pos)
	{
		AMBIENT_MUSICS.put(pos, ambient);
	}

	public static DarkFountainSoundWrapper.DarkFountainWind getDarkWindSound(BlockPos pos)
	{
		return AMBIENT_DARK_WINDS.getOrDefault(pos, null);
	}

	public static void setDarkWindSound(DarkFountainSoundWrapper.DarkFountainWind ambient, BlockPos pos)
	{
		AMBIENT_DARK_WINDS.put(pos, ambient);
	}

	public static DarkFountainSoundWrapper.DarkFountainWind getLightWindSound(BlockPos pos)
	{
		return AMBIENT_LIGHT_WINDS.getOrDefault(pos, null);
	}

	public static void setLightWindSound(DarkFountainSoundWrapper.DarkFountainWind ambient, BlockPos pos)
	{
		AMBIENT_LIGHT_WINDS.put(pos, ambient);
	}
}
