package destiny.penumbra_phantasm.server.datagen.blocksets;

import java.util.List;

public interface StoneFamilyBlockset {
    String baseName();

    String variantPath();

    String textureKey();

    String recipeSubPath();

    String fullBlockMaterialItem();

    int miningTier();

    boolean isEmissive();

    boolean includeToStone();

    boolean includeToCobblestone();

    String addAtEndOfSingular();

    default String stairs() {
        return variantPath() + "_stairs";
    }

    default String slab() {
        return variantPath() + "_slab";
    }

    default String wall() {
        return variantPath() + "_wall";
    }

    default String button() {
        return variantPath() + "_button";
    }

    default String pressurePlate() {
        return variantPath() + "_pressure_plate";
    }

    default List<String> allPieces() {
        return List.of(baseName() + addAtEndOfSingular(), stairs(), slab(), wall(), button(), pressurePlate());
    }
}