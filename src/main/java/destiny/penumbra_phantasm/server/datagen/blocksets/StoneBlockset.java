package destiny.penumbra_phantasm.server.datagen.blocksets;

public record StoneBlockset(String baseName, String textureKey, String recipeSubPath, String fullBlockMaterialItem, int miningTier, boolean isEmissive,
                            boolean includeToStone, boolean includeToCobblestone, String addAtEndOfSingular) implements StoneFamilyBlockset {
    @Override
    public String variantPath() {
        return baseName;
    }
}