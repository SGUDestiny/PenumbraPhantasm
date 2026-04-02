package destiny.penumbra_phantasm.server.datagen;

public record BricksBlockset(
        String baseName,
        String textureKey,
        String recipeSubPath,
        String fullBlockMaterialItem,
        int miningTier,
        boolean isEmissive,
        boolean includeToStone,
        boolean includeToCobblestone
) implements StoneFamilyBlockset {
    public BricksBlockset {
        if (!baseName.endsWith("_bricks")) {
            throw new IllegalArgumentException("BricksBlockset baseName must end with _bricks: " + baseName);
        }
    }

    @Override
    public String variantStem() {
        return baseName.substring(0, baseName.length() - 1);
    }
}
