package destiny.penumbra_phantasm.server.datagen.blockset;

import java.util.List;

public final class BlocksetDefinitions {

    public static final List<StoneBlockset> STONE_BLOCKSETS = List.of(
            new StoneBlockset(
                    "polished_umbrastone",
                    "polished_umbrastone",
                    "umbrastone/polished",
                    "cobbled_umbrastone",
                    false,
                    false,
                    1,
                    false
            )
    );

    public static final List<WoodBlockset> WOOD_BLOCKSETS = List.of(
            new WoodBlockset(
                    "scarlet",
                    "scarlet_planks",
                    "woods/scarlet",
                    "scarlet_log",
                    false,
                    false,
                    0,
                    true
            )
    );

    private BlocksetDefinitions() {
    }
}
