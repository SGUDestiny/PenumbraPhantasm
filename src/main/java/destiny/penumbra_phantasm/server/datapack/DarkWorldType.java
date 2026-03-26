package destiny.penumbra_phantasm.server.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;

public class DarkWorldType
{
	private final ResourceLocation blockTag;
	private final int blockAmount;

	private final ResourceLocation noiseSettings;
	private final ResourceLocation dimensionType;

	private final BiomeSource source;

	public static final ResourceKey<Registry<DarkWorldType>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(PenumbraPhantasm.MODID, "dark_world_type"));
	public static final Codec<ResourceKey<DarkWorldType>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);

	public static final Codec<DarkWorldType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("block_tag").forGetter(DarkWorldType::getBlockTag),
			Codec.INT.fieldOf("block_amount").forGetter(DarkWorldType::getBlockAmount),
			ResourceLocation.CODEC.fieldOf("noise_settings").forGetter(DarkWorldType::getNoiseSettings),
			ResourceLocation.CODEC.fieldOf("dimension_type").forGetter(DarkWorldType::getDimensionType),
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(DarkWorldType::getSource)
	).apply(instance, DarkWorldType::new));

	public DarkWorldType(ResourceLocation blockTag, int blockAmount,
						 ResourceLocation noiseSettings, ResourceLocation dimensionType,
						 BiomeSource source)
	{
		this.blockTag = blockTag;
		this.blockAmount = blockAmount;
		this.noiseSettings = noiseSettings;
		this.dimensionType = dimensionType;
		this.source = source;
	}

	public ResourceLocation getBlockTag()
	{
		return blockTag;
	}

	public int getBlockAmount()
	{
		return blockAmount;
	}

	public ResourceLocation getDimensionType()
	{
		return dimensionType;
	}

	public ResourceLocation getNoiseSettings()
	{
		return noiseSettings;
	}

	public BiomeSource getSource()
	{
		return source;
	}
}
