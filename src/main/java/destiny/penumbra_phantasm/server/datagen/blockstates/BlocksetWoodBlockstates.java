package destiny.penumbra_phantasm.server.datagen.blockstates;

public class BlocksetWoodBlockstates {
    public static final String FENCE = """
            {
              "multipart": [
                {
                  "apply": {
                    "model": "minecraft:block/cherry_fence_post"
                  }
                },
                {
                  "apply": {
                    "model": "minecraft:block/cherry_fence_side",
                    "uvlock": true
                  },
                  "when": {
                    "north": "true"
                  }
                },
                {
                  "apply": {
                    "model": "minecraft:block/cherry_fence_side",
                    "uvlock": true,
                    "y": 90
                  },
                  "when": {
                    "east": "true"
                  }
                },
                {
                  "apply": {
                    "model": "minecraft:block/cherry_fence_side",
                    "uvlock": true,
                    "y": 180
                  },
                  "when": {
                    "south": "true"
                  }
                },
                {
                  "apply": {
                    "model": "minecraft:block/cherry_fence_side",
                    "uvlock": true,
                    "y": 270
                  },
                  "when": {
                    "west": "true"
                  }
                }
              ]
            }""";

    public static final String FENCE_GATE = """
            {
              "variants": {
                "facing=east,in_wall=false,open=false": {
                  "model": "minecraft:block/cherry_fence_gate",
                  "uvlock": true,
                  "y": 270
                },
                "facing=east,in_wall=false,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_open",
                  "uvlock": true,
                  "y": 270
                },
                "facing=east,in_wall=true,open=false": {
                  "model": "minecraft:block/cherry_fence_gate_wall",
                  "uvlock": true,
                  "y": 270
                },
                "facing=east,in_wall=true,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_wall_open",
                  "uvlock": true,
                  "y": 270
                },
                "facing=north,in_wall=false,open=false": {
                  "model": "minecraft:block/cherry_fence_gate",
                  "uvlock": true,
                  "y": 180
                },
                "facing=north,in_wall=false,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_open",
                  "uvlock": true,
                  "y": 180
                },
                "facing=north,in_wall=true,open=false": {
                  "model": "minecraft:block/cherry_fence_gate_wall",
                  "uvlock": true,
                  "y": 180
                },
                "facing=north,in_wall=true,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_wall_open",
                  "uvlock": true,
                  "y": 180
                },
                "facing=south,in_wall=false,open=false": {
                  "model": "minecraft:block/cherry_fence_gate",
                  "uvlock": true
                },
                "facing=south,in_wall=false,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_open",
                  "uvlock": true
                },
                "facing=south,in_wall=true,open=false": {
                  "model": "minecraft:block/cherry_fence_gate_wall",
                  "uvlock": true
                },
                "facing=south,in_wall=true,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_wall_open",
                  "uvlock": true
                },
                "facing=west,in_wall=false,open=false": {
                  "model": "minecraft:block/cherry_fence_gate",
                  "uvlock": true,
                  "y": 90
                },
                "facing=west,in_wall=false,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_open",
                  "uvlock": true,
                  "y": 90
                },
                "facing=west,in_wall=true,open=false": {
                  "model": "minecraft:block/cherry_fence_gate_wall",
                  "uvlock": true,
                  "y": 90
                },
                "facing=west,in_wall=true,open=true": {
                  "model": "minecraft:block/cherry_fence_gate_wall_open",
                  "uvlock": true,
                  "y": 90
                }
              }
            }""";

    public static final String TRAPDOOR = """
            {
              "variants": {
                "facing=east,half=bottom,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_bottom",
                  "y": 90
                },
                "facing=east,half=bottom,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open",
                  "y": 90
                },
                "facing=east,half=top,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_top",
                  "y": 90
                },
                "facing=east,half=top,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open",
                  "x": 180,
                  "y": 270
                },
                "facing=north,half=bottom,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_bottom"
                },
                "facing=north,half=bottom,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open"
                },
                "facing=north,half=top,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_top"
                },
                "facing=north,half=top,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open",
                  "x": 180,
                  "y": 180
                },
                "facing=south,half=bottom,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_bottom",
                  "y": 180
                },
                "facing=south,half=bottom,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open",
                  "y": 180
                },
                "facing=south,half=top,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_top",
                  "y": 180
                },
                "facing=south,half=top,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open",
                  "x": 180,
                  "y": 0
                },
                "facing=west,half=bottom,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_bottom",
                  "y": 270
                },
                "facing=west,half=bottom,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open",
                  "y": 270
                },
                "facing=west,half=top,open=false": {
                  "model": "minecraft:block/cherry_trapdoor_top",
                  "y": 270
                },
                "facing=west,half=top,open=true": {
                  "model": "minecraft:block/cherry_trapdoor_open",
                  "x": 180,
                  "y": 90
                }
              }
            }""";

    public static final String BUTTON = """
            {
              "variants": {
                "face=ceiling,facing=east,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "x": 180,
                  "y": 270
                },
                "face=ceiling,facing=east,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "x": 180,
                  "y": 270
                },
                "face=ceiling,facing=north,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "x": 180,
                  "y": 180
                },
                "face=ceiling,facing=north,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "x": 180,
                  "y": 180
                },
                "face=ceiling,facing=south,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "x": 180
                },
                "face=ceiling,facing=south,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "x": 180
                },
                "face=ceiling,facing=west,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "x": 180,
                  "y": 90
                },
                "face=ceiling,facing=west,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "x": 180,
                  "y": 90
                },
                "face=floor,facing=east,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "y": 90
                },
                "face=floor,facing=east,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "y": 90
                },
                "face=floor,facing=north,powered=false": {
                  "model": "minecraft:block/cherry_button"
                },
                "face=floor,facing=north,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed"
                },
                "face=floor,facing=south,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "y": 180
                },
                "face=floor,facing=south,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "y": 180
                },
                "face=floor,facing=west,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "y": 270
                },
                "face=floor,facing=west,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "y": 270
                },
                "face=wall,facing=east,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "uvlock": true,
                  "x": 90,
                  "y": 90
                },
                "face=wall,facing=east,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "uvlock": true,
                  "x": 90,
                  "y": 90
                },
                "face=wall,facing=north,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "uvlock": true,
                  "x": 90
                },
                "face=wall,facing=north,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "uvlock": true,
                  "x": 90
                },
                "face=wall,facing=south,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "uvlock": true,
                  "x": 90,
                  "y": 180
                },
                "face=wall,facing=south,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "uvlock": true,
                  "x": 90,
                  "y": 180
                },
                "face=wall,facing=west,powered=false": {
                  "model": "minecraft:block/cherry_button",
                  "uvlock": true,
                  "x": 90,
                  "y": 270
                },
                "face=wall,facing=west,powered=true": {
                  "model": "minecraft:block/cherry_button_pressed",
                  "uvlock": true,
                  "x": 90,
                  "y": 270
                }
              }
            }""";

    public static final String PRESSURE_PLATE = """
            {
              "variants": {
                "powered=false": {
                  "model": "minecraft:block/cherry_pressure_plate"
                },
                "powered=true": {
                  "model": "minecraft:block/cherry_pressure_plate_down"
                }
              }
            }""";

    public static final String SLAB = """
            {
              "variants": {
                "type=bottom": {
                  "model": "minecraft:block/cherry_slab"
                },
                "type=double": {
                  "model": "minecraft:block/cherry_planks"
                },
                "type=top": {
                  "model": "minecraft:block/cherry_slab_top"
                }
              }
            }""";

    public static final String PLANKS = """
            {
              "variants": {
                "": {
                  "model": "minecraft:block/cherry_planks"
                }
              }
            }""";

    public BlocksetWoodBlockstates() {
    }
}