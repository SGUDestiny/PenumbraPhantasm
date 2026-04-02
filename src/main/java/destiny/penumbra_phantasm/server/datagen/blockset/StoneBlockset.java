package destiny.penumbra_phantasm.server.datagen.blockset;

import destiny.penumbra_phantasm.PenumbraPhantasm;

import java.util.List;

public record StoneBlockset(
        String baseName,
        String textureKey,
        String recipeSubPath,
        String fullBlockMaterialItem,
        boolean includeToStone,
        boolean includeToCobblestone,
        int miningTier,
        boolean isEmissive
) {
    public String blockId(String path) {
        return PenumbraPhantasm.MODID + ":" + path;
    }

    public String stairs() {
        return baseName + "_stairs";
    }

    public String slab() {
        return baseName + "_slab";
    }

    public String wall() {
        return baseName + "_wall";
    }

    public String button() {
        return baseName + "_button";
    }

    public String pressurePlate() {
        return baseName + "_pressure_plate";
    }

    public List<String> allPieces() {
        return List.of(baseName, stairs(), slab(), wall(), button(), pressurePlate());
    }
}
