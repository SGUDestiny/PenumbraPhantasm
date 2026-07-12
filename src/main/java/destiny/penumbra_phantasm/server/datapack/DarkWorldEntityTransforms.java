package destiny.penumbra_phantasm.server.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public record DarkWorldEntityTransforms(List<EntityType<?>> persist, List<EntityTransform> transforms)
{
	public static final ResourceKey<Registry<DarkWorldEntityTransforms>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(PenumbraPhantasm.MODID, "dark_world_transforms/entity"));
	public static final Codec<ResourceKey<DarkWorldEntityTransforms>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);

	public static final Codec<DarkWorldEntityTransforms> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ForgeRegistries.ENTITY_TYPES.getCodec().listOf().fieldOf("persistent_entities").forGetter(DarkWorldEntityTransforms::persist),
			EntityTransform.CODEC.listOf().fieldOf("converting_entities").forGetter(DarkWorldEntityTransforms::transforms)
	).apply(instance, DarkWorldEntityTransforms::new));

	public static class EntityTransform
	{
		public static final Codec<EntityTransform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("light_world").forGetter(EntityTransform::getLightWorldForm),
				ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("dark_world").forGetter(EntityTransform::getDarkWorldForm)
		).apply(instance, EntityTransform::new));

		private final EntityType<?> lightWorld;
		private final EntityType<?> darkWorld;

		public EntityTransform(EntityType<?> lightWorld, EntityType<?> darkWorld)
		{
			this.lightWorld = lightWorld;
			this.darkWorld = darkWorld;
		}

		public EntityType<?> getLightWorldForm()
		{
			return lightWorld;
		}

		public EntityType<?> getDarkWorldForm()
		{
			return darkWorld;
		}
	}
}
