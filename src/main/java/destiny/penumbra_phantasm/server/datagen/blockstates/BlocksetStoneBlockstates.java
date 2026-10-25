package destiny.penumbra_phantasm.server.datagen.blockstates;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import destiny.penumbra_phantasm.server.datagen.blocksets.StoneFamilyBlockset;

public class BlocksetStoneBlockstates {
    public static final String STAIR_BLOCKSTATE = """
            {
              "variants": {
                "facing=east,half=bottom,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "y": 270
                },
                "facing=east,half=bottom,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner"
                },
                "facing=east,half=bottom,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "y": 270
                },
                "facing=east,half=bottom,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer"
                },
                "facing=east,half=bottom,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs"
                },
                "facing=east,half=top,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180
                },
                "facing=east,half=top,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180,
                  "y": 90
                },
                "facing=east,half=top,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180
                },
                "facing=east,half=top,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180,
                  "y": 90
                },
                "facing=east,half=top,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs",
                  "uvlock": true,
                  "x": 180
                },
                "facing=north,half=bottom,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "y": 180
                },
                "facing=north,half=bottom,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "y": 270
                },
                "facing=north,half=bottom,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "y": 180
                },
                "facing=north,half=bottom,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "y": 270
                },
                "facing=north,half=bottom,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs",
                  "uvlock": true,
                  "y": 270
                },
                "facing=north,half=top,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180,
                  "y": 270
                },
                "facing=north,half=top,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180
                },
                "facing=north,half=top,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180,
                  "y": 270
                },
                "facing=north,half=top,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180
                },
                "facing=north,half=top,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs",
                  "uvlock": true,
                  "x": 180,
                  "y": 270
                },
                "facing=south,half=bottom,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner"
                },
                "facing=south,half=bottom,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "y": 90
                },
                "facing=south,half=bottom,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer"
                },
                "facing=south,half=bottom,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "y": 90
                },
                "facing=south,half=bottom,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs",
                  "uvlock": true,
                  "y": 90
                },
                "facing=south,half=top,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180,
                  "y": 90
                },
                "facing=south,half=top,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180,
                  "y": 180
                },
                "facing=south,half=top,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180,
                  "y": 90
                },
                "facing=south,half=top,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180,
                  "y": 180
                },
                "facing=south,half=top,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs",
                  "uvlock": true,
                  "x": 180,
                  "y": 90
                },
                "facing=west,half=bottom,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "y": 90
                },
                "facing=west,half=bottom,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "y": 180
                },
                "facing=west,half=bottom,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "y": 90
                },
                "facing=west,half=bottom,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "y": 180
                },
                "facing=west,half=bottom,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs",
                  "uvlock": true,
                  "y": 180
                },
                "facing=west,half=top,shape=inner_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180,
                  "y": 180
                },
                "facing=west,half=top,shape=inner_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_inner",
                  "uvlock": true,
                  "x": 180,
                  "y": 270
                },
                "facing=west,half=top,shape=outer_left": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180,
                  "y": 180
                },
                "facing=west,half=top,shape=outer_right": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs_outer",
                  "uvlock": true,
                  "x": 180,
                  "y": 270
                },
                "facing=west,half=top,shape=straight": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_stairs",
                  "uvlock": true,
                  "x": 180,
                  "y": 180
                }
              }
            }""";

    public static final String WALL_BLOCKSTATE = """
            {
              "multipart": [
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_post"
                  },
                  "when": {
                    "up": "true"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side",
                    "uvlock": true
                  },
                  "when": {
                    "north": "low"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side",
                    "uvlock": true,
                    "y": 90
                  },
                  "when": {
                    "east": "low"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side",
                    "uvlock": true,
                    "y": 180
                  },
                  "when": {
                    "south": "low"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side",
                    "uvlock": true,
                    "y": 270
                  },
                  "when": {
                    "west": "low"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side_tall",
                    "uvlock": true
                  },
                  "when": {
                    "north": "tall"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side_tall",
                    "uvlock": true,
                    "y": 90
                  },
                  "when": {
                    "east": "tall"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side_tall",
                    "uvlock": true,
                    "y": 180
                  },
                  "when": {
                    "south": "tall"
                  }
                },
                {
                  "apply": {
                    "model": "penumbra_phantasm:block/polished_umbrastone_wall_side_tall",
                    "uvlock": true,
                    "y": 270
                  },
                  "when": {
                    "west": "tall"
                  }
                }
              ]
            }""";

    public static final String BUTTON_BLOCKSTATE = """
            {
              "variants": {
                "face=ceiling,facing=east,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "x": 180,
                  "y": 270
                },
                "face=ceiling,facing=east,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "x": 180,
                  "y": 270
                },
                "face=ceiling,facing=north,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "x": 180,
                  "y": 180
                },
                "face=ceiling,facing=north,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "x": 180,
                  "y": 180
                },
                "face=ceiling,facing=south,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "x": 180
                },
                "face=ceiling,facing=south,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "x": 180
                },
                "face=ceiling,facing=west,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "x": 180,
                  "y": 90
                },
                "face=ceiling,facing=west,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "x": 180,
                  "y": 90
                },
                "face=floor,facing=east,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "y": 90
                },
                "face=floor,facing=east,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "y": 90
                },
                "face=floor,facing=north,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button"
                },
                "face=floor,facing=north,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed"
                },
                "face=floor,facing=south,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "y": 180
                },
                "face=floor,facing=south,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "y": 180
                },
                "face=floor,facing=west,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "y": 270
                },
                "face=floor,facing=west,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "y": 270
                },
                "face=wall,facing=east,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "uvlock": true,
                  "x": 90,
                  "y": 90
                },
                "face=wall,facing=east,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "uvlock": true,
                  "x": 90,
                  "y": 90
                },
                "face=wall,facing=north,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "uvlock": true,
                  "x": 90
                },
                "face=wall,facing=north,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "uvlock": true,
                  "x": 90
                },
                "face=wall,facing=south,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "uvlock": true,
                  "x": 90,
                  "y": 180
                },
                "face=wall,facing=south,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "uvlock": true,
                  "x": 90,
                  "y": 180
                },
                "face=wall,facing=west,powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button",
                  "uvlock": true,
                  "x": 90,
                  "y": 270
                },
                "face=wall,facing=west,powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_button_pressed",
                  "uvlock": true,
                  "x": 90,
                  "y": 270
                }
              }
            }""";

    public static final String PRESSURE_PLATE_BLOCKSTATE = """
            {
              "variants": {
                "powered=false": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_pressure_plate"
                },
                "powered=true": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_pressure_plate_down"
                }
              }
            }""";

    public static final String SLAB_BLOCKSTATE = """
            {
              "variants": {
                "type=bottom": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_slab"
                },
                "type=double": {
                  "model": "penumbra_phantasm:block/polished_umbrastone"
                },
                "type=top": {
                  "model": "penumbra_phantasm:block/polished_umbrastone_slab_top"
                }
              }
            }""";

    public static final String FULL_BLOCKSTATE = """
            {
              "variants": {
                "": {
                  "model": "penumbra_phantasm:block/polished_umbrastone"
                }
              }
            }""";

    public BlocksetStoneBlockstates() {
    }

    public static JsonElement makeStairBlockstate(StoneFamilyBlockset stoneSet) {
        return makeStairBlockstate(stoneSet.stairs());
    }

    public static JsonElement makeStairBlockstate(String stairs) {
        return parse(STAIR_BLOCKSTATE.replace("polished_umbrastone_stairs", stairs));
    }

    public static JsonElement makeWallBlockstate(StoneFamilyBlockset stoneSet) {
        return parse(replaceWith(stoneSet, WALL_BLOCKSTATE));
    }

    public static JsonElement makeButtonBlockstate(StoneFamilyBlockset stoneSet) {
        return parse(replaceWith(stoneSet, BUTTON_BLOCKSTATE));
    }

    public static JsonElement makePressurePlateBlockstate(StoneFamilyBlockset stoneSet) {
        return parse(replaceWith(stoneSet, PRESSURE_PLATE_BLOCKSTATE));
    }

    public static JsonElement makeSlabBlockstate(StoneFamilyBlockset stoneSet) {
        return parse(replaceWithAppend(stoneSet, SLAB_BLOCKSTATE));
    }

    public static JsonElement makeFullBlockstate(StoneFamilyBlockset stoneSet) {
        return parse(replaceWithAppend(stoneSet, FULL_BLOCKSTATE));
    }

    public static String replaceWith(StoneFamilyBlockset stoneSet, String blockState) {
        return blockState.replace("polished_umbrastone_", stoneSet.variantPath() + "_").replace("polished_umbrastone", stoneSet.baseName());
    }

    public static String replaceWithAppend(StoneFamilyBlockset stoneSet, String blockState) {
        return blockState.replace("polished_umbrastone_", stoneSet.variantPath() + "_").replace("polished_umbrastone", stoneSet.baseName() + stoneSet.addAtEndOfSingular());
    }

    public static JsonElement parse(String json) {
        return JsonParser.parseString(json);
    }
}