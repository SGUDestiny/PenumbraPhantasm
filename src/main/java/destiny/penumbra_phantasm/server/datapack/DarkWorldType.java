package destiny.penumbra_phantasm.server.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;

public record DarkWorldType(ResourceLocation blockTag, int blockAmount, ResourceLocation noiseSettings,
							ResourceLocation dimensionType, BiomeSource source) {
	public static final ResourceKey<Registry<DarkWorldType>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(PenumbraPhantasm.MODID, "dark_world_type"));
	public static final Codec<ResourceKey<DarkWorldType>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);

	public static final Codec<DarkWorldType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("block_tag").forGetter(DarkWorldType::blockTag),
			Codec.INT.fieldOf("block_amount").forGetter(DarkWorldType::blockAmount),
			ResourceLocation.CODEC.fieldOf("noise_settings").forGetter(DarkWorldType::noiseSettings),
			ResourceLocation.CODEC.fieldOf("dimension_type").forGetter(DarkWorldType::dimensionType),
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(DarkWorldType::source)
	).apply(instance, DarkWorldType::new));
}
