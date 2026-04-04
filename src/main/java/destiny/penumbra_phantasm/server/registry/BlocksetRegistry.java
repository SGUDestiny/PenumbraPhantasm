package destiny.penumbra_phantasm.server.registry;

import destiny.penumbra_phantasm.server.datagen.BricksBlockset;
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
                    false,
                    false,
                    false
            ),
            new StoneBlockset(
                    "umbrastone",
                    "umbrastone",
                    "umbrastone",
                    "",
                    1,
                    false,
                    true,
                    false
            ),
            new StoneBlockset(
                    "cobbled_umbrastone",
                    "cobbled_umbrastone",
                    "umbrastone/cobbled",
                    "",
                    1,
                    false,
                    false,
                    true
            ),

            new StoneBlockset(
                    "polished_scarlet_marble",
                    "polished_scarlet_marble",
                    "marble/scarlet/polished",
                    "cobbled_scarlet_marble",
                    1,
                    true,
                    false,
                    false
            ),
            new StoneBlockset(
                    "scarlet_marble",
                    "scarlet_marble",
                    "marble/scarlet",
                    "",
                    1,
                    true,
                    true,
                    false
            ),
            new StoneBlockset(
                    "cobbled_scarlet_marble",
                    "cobbled_scarlet_marble",
                    "marble/scarlet/cobbled",
                    "",
                    1,
                    true,
                    false,
                    true
            ),

            new StoneBlockset(
                    "polished_dark_marble",
                    "polished_dark_marble",
                    "marble/dark/polished",
                    "cobbled_dark_marble",
                    1,
                    true,
                    false,
                    false
            ),
            new StoneBlockset(
                    "dark_marble",
                    "dark_marble",
                    "marble/dark",
                    "",
                    1,
                    true,
                    true,
                    false
            ),
            new StoneBlockset(
                    "cobbled_dark_marble",
                    "cobbled_dark_marble",
                    "marble/dark/cobbled",
                    "",
                    1,
                    true,
                    false,
                    true
            ),

            new StoneBlockset(
                    "polished_cliffrock",
                    "polished_cliffrock",
                    "cliffrock/polished",
                    "cobbled_cliffrock",
                    1,
                    false,
                    false,
                    false
            ),
            new StoneBlockset(
                    "cliffrock",
                    "cliffrock",
                    "cliffrock",
                    "",
                    1,
                    false,
                    true,
                    false
            ),
            new StoneBlockset(
                    "cobbled_cliffrock",
                    "cobbled_cliffrock",
                    "cliffrock/cobbled",
                    "",
                    1,
                    false,
                    false,
                    true
            )
    );

    public static final List<BricksBlockset> BRICKS_BLOCKSETS = List.of(
            new BricksBlockset(
                    "umbrastone_bricks",
                    "umbrastone_bricks",
                    "umbrastone/bricks",
                    "",
                    1,
                    false,
                    false,
                    false
            ),
            new BricksBlockset(
                    "scarlet_marble_bricks",
                    "scarlet_marble_bricks",
                    "marble/scarlet/bricks",
                    "",
                    1,
                    true,
                    false,
                    false
            ),
            new BricksBlockset(
                    "dark_marble_bricks",
                    "dark_marble_bricks",
                    "marble/dark/bricks",
                    "",
                    1,
                    true,
                    false,
                    false
            ),
            new BricksBlockset(
                    "cliffrock_bricks",
                    "cliffrock_bricks",
                    "cliffrock/bricks",
                    "",
                    1,
                    false,
                    false,
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
