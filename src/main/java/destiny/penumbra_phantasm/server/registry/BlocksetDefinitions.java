package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.server.datagen.StoneBlockset;
import destiny.penumbra_phantasm.server.datagen.WoodBlockset;

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
                    0,
                    true
            ),
            new WoodBlockset(
                    "dark_candy",
                    "dark_candy",
                    "woods/dark_candy",
                    "dark_candy_log",
                    0,
                    true
            )
    );

    private BlocksetDefinitions() {
    }
}
