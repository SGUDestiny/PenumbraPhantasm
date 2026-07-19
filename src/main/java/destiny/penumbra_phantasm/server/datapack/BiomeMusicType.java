package destiny.penumbra_phantasm.server.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record BiomeMusicType(ResourceLocation biome, ResourceLocation sound, boolean looping, int minDelay, int maxDelay)
{
	public static final ResourceKey<Registry<BiomeMusicType>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(PenumbraPhantasm.MODID, "biome_music"));
	public static final Codec<ResourceKey<BiomeMusicType>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);

	public static final Codec<BiomeMusicType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("biome").forGetter(BiomeMusicType::biome),
			ResourceLocation.CODEC.fieldOf("sound_event").forGetter(BiomeMusicType::sound),
			Codec.BOOL.fieldOf("loops").forGetter(BiomeMusicType::looping),
			Codec.INT.fieldOf("min_delay").forGetter(BiomeMusicType::minDelay),
			Codec.INT.fieldOf("max_delay").forGetter(BiomeMusicType::maxDelay)
	).apply(instance, BiomeMusicType::new));
}
