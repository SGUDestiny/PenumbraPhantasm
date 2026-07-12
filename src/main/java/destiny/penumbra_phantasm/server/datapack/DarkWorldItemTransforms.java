package destiny.penumbra_phantasm.server.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public record DarkWorldItemTransforms(List<Item> persist, List<ItemTransform> transforms)
{
	public static final ResourceKey<Registry<DarkWorldItemTransforms>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(PenumbraPhantasm.MODID, "dark_world_transforms/item"));
	public static final Codec<ResourceKey<DarkWorldItemTransforms>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);

	public static final Codec<DarkWorldItemTransforms> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ForgeRegistries.ITEMS.getCodec().listOf().fieldOf("persistent_items").forGetter(DarkWorldItemTransforms::persist),
			ItemTransform.CODEC.listOf().fieldOf("converting_items").forGetter(DarkWorldItemTransforms::transforms)
	).apply(instance, DarkWorldItemTransforms::new));

	public static class ItemTransform
	{
		public static final Codec<ItemTransform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ForgeRegistries.ITEMS.getCodec().fieldOf("light_world").forGetter(ItemTransform::getLightWorldForm),
				ForgeRegistries.ITEMS.getCodec().fieldOf("dark_world").forGetter(ItemTransform::getDarkWorldForm)
		).apply(instance, ItemTransform::new));

		private final Item lightWorld;
		private final Item darkWorld;

		public ItemTransform(Item lightWorld, Item darkWorld)
		{
			this.lightWorld = lightWorld;
			this.darkWorld = darkWorld;
		}

		public Item getLightWorldForm()
		{
			return lightWorld;
		}

		public Item getDarkWorldForm()
		{
			return darkWorld;
		}
	}
}
