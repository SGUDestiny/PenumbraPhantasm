package destiny.penumbra_phantasm.server.datagen;

import java.util.List;

public interface StoneFamilyBlockset {
    String baseName();

    String variantStem();

    String textureKey();

    String recipeSubPath();

    String fullBlockMaterialItem();

    int miningTier();

    boolean isEmissive();

    boolean includeToStone();

    boolean includeToCobblestone();

    default String stairs() {
        return variantStem() + "_stairs";
    }

    default String slab() {
        return variantStem() + "_slab";
    }

    default String wall() {
        return variantStem() + "_wall";
    }

    default String button() {
        return variantStem() + "_button";
    }

    default String pressurePlate() {
        return variantStem() + "_pressure_plate";
    }

    default List<String> allPieces() {
        return List.of(baseName(), stairs(), slab(), wall(), button(), pressurePlate());
    }
}
