package destiny.penumbra_phantasm.server.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record DarkWorldRecipeSeparation(List<ResourceLocation> darkWorldAllowed, List<ResourceLocation> darkWorldBlocked)
{
	public static final ResourceKey<Registry<DarkWorldRecipeSeparation>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(PenumbraPhantasm.MODID, "dark_world_transforms/recipe"));
	public static final Codec<ResourceKey<DarkWorldRecipeSeparation>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);

	public static final Codec<DarkWorldRecipeSeparation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.listOf().fieldOf("dark_world_allowed").forGetter(DarkWorldRecipeSeparation::darkWorldAllowed),
			ResourceLocation.CODEC.listOf().fieldOf("dark_world_blocked").forGetter(DarkWorldRecipeSeparation::darkWorldAllowed)
	).apply(instance, DarkWorldRecipeSeparation::new));
}
