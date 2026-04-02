package destiny.penumbra_phantasm.server.datagen;

final class WoodBlockstateTemplates {

    static final String FENCE = """
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

    static final String FENCE_GATE = """
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

    static final String TRAPDOOR = """
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

    private WoodBlockstateTemplates() {
    }
}
