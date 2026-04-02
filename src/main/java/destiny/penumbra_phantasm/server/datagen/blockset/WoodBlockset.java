package destiny.penumbra_phantasm.server.datagen.blockset;

import destiny.penumbra_phantasm.PenumbraPhantasm;

import java.util.List;

public record WoodBlockset(
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

    public String planks() {
        return baseName + "_planks";
    }

    public String stairs() {
        return baseName + "_stairs";
    }

    public String slab() {
        return baseName + "_slab";
    }

    public String fence() {
        return baseName + "_fence";
    }

    public String fenceGate() {
        return baseName + "_fence_gate";
    }

    public String door() {
        return baseName + "_door";
    }

    public String trapdoor() {
        return baseName + "_trapdoor";
    }

    public String button() {
        return baseName + "_button";
    }

    public String pressurePlate() {
        return baseName + "_pressure_plate";
    }

    public List<String> allPieces() {
        return List.of(planks(), stairs(), slab(), fence(), fenceGate(), door(), trapdoor(), button(), pressurePlate());
    }
}
