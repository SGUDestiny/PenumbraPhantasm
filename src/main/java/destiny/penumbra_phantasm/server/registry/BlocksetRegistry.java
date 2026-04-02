package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.server.datagen.StoneBlockset;
import destiny.penumbra_phantasm.server.datagen.WoodBlockset;

import java.util.List;

public final class BlocksetRegistry {

    public static final List<StoneBlockset> STONE_BLOCKSETS = List.of(
            new StoneBlockset(
                    "polished_umbrastone",
                    "polished_umbrastone",
                    "umbrastone/polished",
                    "cobbled_umbrastone",
                    1,
                    false
            ),
            new StoneBlockset(
                    "umbrastone_brick",
                    "umbrastone_bricks",
                    "umbrastone/bricks",
                    "",
                    1,
                    false
            ),
            new StoneBlockset(
                    "umbrastone",
                    "umbrastone",
                    "umbrastone",
                    "",
                    1,
                    false
            ),
            new StoneBlockset(
                    "cobbled_umbrastone",
                    "cobbled_umbrastone",
                    "umbrastone/cobbled",
                    "",
                    1,
                    false
            ),

            new StoneBlockset(
                    "polished_scarlet_marble",
                    "polished_scarlet_marble",
                    "marble/scarlet/polished",
                    "",
                    1,
                    true
            ),
            new StoneBlockset(
                    "scarlet_marble_brick",
                    "scarlet_marble_bricks",
                    "marble/scarlet/bricks",
                    "",
                    1,
                    true
            ),

            new StoneBlockset(
                    "polished_dark_marble",
                    "polished_dark_marble",
                    "marble/dark/polished",
                    "",
                    1,
                    true
            ),
            new StoneBlockset(
                    "dark_marble_brick",
                    "dark_marble_bricks",
                    "marble/dark/bricks",
                    "",
                    1,
                    true
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
                    "dark_candy_planks",
                    "woods/dark_candy",
                    "dark_candy_log",
                    0,
                    true
            )
    );

    private BlocksetRegistry() {
    }
}
