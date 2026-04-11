package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.BlocksetRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BlocksetServerProvider {
    private BlocksetServerProvider() {
    }

    public static void run(Path mainResourcesRoot) throws IOException {
        String mod = PenumbraPhantasm.MODID;
        for (StoneBlockset s : BlocksetRegistry.STONE_BLOCKSETS) {
            emitStoneFamilyRecipesAndLoot(mainResourcesRoot, mod, s);
        }
        for (BricksBlockset b : BlocksetRegistry.BRICKS_BLOCKSETS) {
            emitStoneFamilyRecipesAndLoot(mainResourcesRoot, mod, b);
        }
        for (WoodBlockset w : BlocksetRegistry.WOOD_BLOCKSETS) {
            emitWoodRecipesAndLoot(mainResourcesRoot, mod, w);
        }
        mergeAllBlocksetTags(mainResourcesRoot, mod);
    }

    private static void mergeAllBlocksetTags(Path root, String mod) throws IOException {
        for (StoneBlockset s : BlocksetRegistry.STONE_BLOCKSETS) {
            mergeStoneFamilyTags(root, mod, s);
        }
        for (BricksBlockset b : BlocksetRegistry.BRICKS_BLOCKSETS) {
            mergeStoneFamilyTags(root, mod, b);
        }
        for (WoodBlockset w : BlocksetRegistry.WOOD_BLOCKSETS) {
            mergeWoodTags(root, mod, w);
        }
    }

    private static List<String> blockIds(String mod, Iterable<String> names) {
        List<String> out = new ArrayList<>();
        for (String n : names) {
            out.add(mod + ":" + n);
        }
        return out;
    }

    private static void mergeStoneFamilyTags(Path root, String mod, StoneFamilyBlockset s) throws IOException {
        List<String> all = blockIds(mod, s.allPieces());
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/mineable/pickaxe.json", all);
        String needsTier = switch (s.miningTier()) {
            case 1 -> "data/minecraft/tags/blocks/needs_stone_tool.json";
            case 2 -> "data/minecraft/tags/blocks/needs_iron_tool.json";
            case 3 -> "data/minecraft/tags/blocks/needs_diamond_tool.json";
            default -> null;
        };
        if (needsTier != null) {
            BlocksetJsonIO.mergeTagBlockAndItem(root, needsTier, all);
        }
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stairs.json", List.of(mod + ":" + s.stairs()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/slabs.json", List.of(mod + ":" + s.slab()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/walls.json", List.of(mod + ":" + s.wall()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stone_buttons.json", List.of(mod + ":" + s.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stone_pressure_plates.json", List.of(mod + ":" + s.pressurePlate()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/buttons.json", List.of(mod + ":" + s.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/pressure_plates.json", List.of(mod + ":" + s.pressurePlate()));
        String baseId = mod + ":" + s.baseName();
        if (s.includeToStone()) {
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stone.json", List.of(baseId));
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/forge/tags/blocks/stone.json", List.of(baseId));
        }
        if (s.includeToCobblestone()) {
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/cobblestone.json", List.of(baseId));
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/forge/tags/blocks/cobblestone.json", List.of(baseId));
        }
    }

    private static void mergeWoodTags(Path root, String mod, WoodBlockset w) throws IOException {
        List<String> all = blockIds(mod, w.allPieces());
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/mineable/axe.json", all);
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/planks.json", List.of(mod + ":" + w.planks()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_stairs.json", List.of(mod + ":" + w.stairs()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_slabs.json", List.of(mod + ":" + w.slab()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_fences.json", List.of(mod + ":" + w.fence()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/fences.json", List.of(mod + ":" + w.fence()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/fence_gates.json", List.of(mod + ":" + w.fenceGate()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_doors.json", List.of(mod + ":" + w.door()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/doors.json", List.of(mod + ":" + w.door()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_trapdoors.json", List.of(mod + ":" + w.trapdoor()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/trapdoors.json", List.of(mod + ":" + w.trapdoor()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_buttons.json", List.of(mod + ":" + w.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/buttons.json", List.of(mod + ":" + w.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_pressure_plates.json", List.of(mod + ":" + w.pressurePlate()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/pressure_plates.json", List.of(mod + ":" + w.pressurePlate()));
    }

    private static String recipesFolder(String mod, String recipeSubPath) {
        if (recipeSubPath == null || recipeSubPath.isEmpty()) {
            return "data/" + mod + "/recipes/";
        }
        return "data/" + mod + "/recipes/" + recipeSubPath + "/";
    }

    private static void emitStoneFamilyRecipesAndLoot(Path root, String mod, StoneFamilyBlockset s) throws IOException {
        String folder = recipesFolder(mod, s.recipeSubPath());
        String base = s.baseName();
        String mat = mod + ":" + s.fullBlockMaterialItem();
        String b = mod + ":" + base;

        BlocksetJsonIO.writeIfAbsent(root, folder + base + ".json", parseRecipeFullBlock(mat, b, 4));
        BlocksetJsonIO.writeIfAbsent(root, folder + s.stairs() + ".json", parseRecipeStairs(b, mod + ":" + s.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, folder + s.slab() + ".json", parseRecipeSlab(b, mod + ":" + s.slab()));
        BlocksetJsonIO.writeIfAbsent(root, folder + s.wall() + ".json", parseRecipeWall(b, mod + ":" + s.wall()));
        BlocksetJsonIO.writeIfAbsent(root, folder + s.button() + ".json", parseRecipeStoneButton(b, mod + ":" + s.button()));
        BlocksetJsonIO.writeIfAbsent(root, folder + s.pressurePlate() + ".json", parseRecipeStonePressurePlate(b, mod + ":" + s.pressurePlate()));

        String lootBase = "data/" + mod + "/loot_tables/blocks/";
        BlocksetJsonIO.writeIfAbsent(root, lootBase + base + ".json", parseLootSimpleBlock(mod + ":" + base));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + s.stairs() + ".json", parseLootSimpleBlock(mod + ":" + s.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + s.slab() + ".json", parseLootSlab(mod + ":" + s.slab()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + s.wall() + ".json", parseLootSimpleBlock(mod + ":" + s.wall()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + s.button() + ".json", parseLootSimpleBlock(mod + ":" + s.button()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + s.pressurePlate() + ".json", parseLootSimpleBlock(mod + ":" + s.pressurePlate()));
    }

    private static void emitWoodRecipesAndLoot(Path root, String mod, WoodBlockset w) throws IOException {
        String folder = recipesFolder(mod, w.recipeSubPath());
        String plank = mod + ":" + w.planks();
        String mat = w.fullBlockMaterialItem();
        if (mat != null && !mat.isEmpty()) {
            BlocksetJsonIO.writeIfAbsent(root, folder + w.planks() + ".json",
                    parseRecipePlanksFromMaterial(mod + ":" + mat, plank, 4));
        }

        BlocksetJsonIO.writeIfAbsent(root, folder + w.stairs() + ".json", parseRecipeWoodStairs(plank, mod + ":" + w.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, folder + w.slab() + ".json", parseRecipeSlab3(plank, mod + ":" + w.slab(), 6));
        BlocksetJsonIO.writeIfAbsent(root, folder + w.fence() + ".json", parseRecipeFence(plank, mod + ":" + w.fence(), 3));
        BlocksetJsonIO.writeIfAbsent(root, folder + w.fenceGate() + ".json", parseRecipeFenceGate(plank, mod + ":" + w.fenceGate()));
        BlocksetJsonIO.writeIfAbsent(root, folder + w.door() + ".json", parseRecipeDoor(plank, mod + ":" + w.door()));
        BlocksetJsonIO.writeIfAbsent(root, folder + w.trapdoor() + ".json", parseRecipeTrapdoor(plank, mod + ":" + w.trapdoor()));
        BlocksetJsonIO.writeIfAbsent(root, folder + w.button() + ".json", parseRecipeWoodButton(plank, mod + ":" + w.button()));
        BlocksetJsonIO.writeIfAbsent(root, folder + w.pressurePlate() + ".json", parseRecipeWoodPressurePlate(plank, mod + ":" + w.pressurePlate()));

        String lootBase = "data/" + mod + "/loot_tables/blocks/";
        for (String p : w.allPieces()) {
            String id = mod + ":" + p;
            if (p.equals(w.door())) {
                BlocksetJsonIO.writeIfAbsent(root, lootBase + p + ".json", parseLootDoor(id));
            } else if (p.equals(w.slab())) {
                BlocksetJsonIO.writeIfAbsent(root, lootBase + p + ".json", parseLootSlab(id));
            } else {
                BlocksetJsonIO.writeIfAbsent(root, lootBase + p + ".json", parseLootSimpleBlock(id));
            }
        }
    }

    private static JsonElement parseRecipePlanksFromMaterial(String materialItem, String plankItem, int count) {
        String j = """
                {
                  "type": "minecraft:crafting_shapeless",
                  "group": "planks",
                  "ingredients": [ { "item": "%MAT%" } ],
                  "result": { "count": %CNT%, "item": "%OUT%" }
                }""".replace("%MAT%", materialItem).replace("%OUT%", plankItem).replace("%CNT%", Integer.toString(count));
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeFullBlock(String material, String result, int count) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "building",
                  "key": { "S": { "item": "%MAT%" } },
                  "pattern": [ "SS", "SS" ],
                  "result": { "count": %CNT%, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%MAT%", material).replace("%OUT%", result).replace("%CNT%", Integer.toString(count));
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeStairs(String baseItem, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "building",
                  "group": "stone_stairs",
                  "key": { "#": { "item": "%BASE%" } },
                  "pattern": [ "#  ", "## ", "###" ],
                  "result": { "count": 4, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%BASE%", baseItem).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeWoodStairs(String plankItem, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "building",
                  "group": "wooden_stairs",
                  "key": { "#": { "item": "%PLK%" } },
                  "pattern": [ "#  ", "## ", "###" ],
                  "result": { "count": 4, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%PLK%", plankItem).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeSlab(String baseItem, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "building",
                  "group": "stone_slab",
                  "key": { "#": { "item": "%BASE%" } },
                  "pattern": [ "###" ],
                  "result": { "count": 6, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%BASE%", baseItem).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeSlab3(String plankItem, String out, int count) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "building",
                  "group": "wooden_slab",
                  "key": { "#": { "item": "%BASE%" } },
                  "pattern": [ "###" ],
                  "result": { "count": %CNT%, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%BASE%", plankItem).replace("%OUT%", out).replace("%CNT%", Integer.toString(count));
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeWall(String baseItem, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "misc",
                  "key": { "#": { "item": "%BASE%" } },
                  "pattern": [ "###", "###" ],
                  "result": { "count": 6, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%BASE%", baseItem).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeStoneButton(String baseItem, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shapeless",
                  "category": "redstone",
                  "group": "stone_button",
                  "ingredients": [ { "item": "%BASE%" } ],
                  "result": { "item": "%OUT%" }
                }""".replace("%BASE%", baseItem).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeStonePressurePlate(String baseItem, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "redstone",
                  "group": "stone_pressure_plate",
                  "key": { "#": { "item": "%BASE%" } },
                  "pattern": [ "##" ],
                  "result": { "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%BASE%", baseItem).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeFence(String plank, String out, int count) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "misc",
                  "group": "wooden_fence",
                  "key": { "#": { "item": "%PLK%" }, "W": { "item": "minecraft:stick" } },
                  "pattern": [ "W#W", "W#W" ],
                  "result": { "count": %CNT%, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%PLK%", plank).replace("%OUT%", out).replace("%CNT%", Integer.toString(count));
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeFenceGate(String plank, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "redstone",
                  "group": "wooden_fence_gate",
                  "key": { "#": { "item": "%PLK%" }, "W": { "item": "minecraft:stick" } },
                  "pattern": [ "   ", "W#W", "W#W" ],
                  "result": { "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%PLK%", plank).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeDoor(String plank, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "redstone",
                  "group": "wooden_door",
                  "key": { "#": { "item": "%PLK%" } },
                  "pattern": [ "##", "##", "##" ],
                  "result": { "count": 3, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%PLK%", plank).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeTrapdoor(String plank, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "redstone",
                  "group": "wooden_trapdoor",
                  "key": { "#": { "item": "%PLK%" } },
                  "pattern": [ "###", "###" ],
                  "result": { "count": 2, "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%PLK%", plank).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeWoodButton(String plank, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shapeless",
                  "category": "redstone",
                  "group": "wooden_button",
                  "ingredients": [ { "item": "%PLK%" } ],
                  "result": { "item": "%OUT%" }
                }""".replace("%PLK%", plank).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseRecipeWoodPressurePlate(String plank, String out) {
        String j = """
                {
                  "type": "minecraft:crafting_shaped",
                  "category": "redstone",
                  "group": "wooden_pressure_plate",
                  "key": { "#": { "item": "%PLK%" } },
                  "pattern": [ "##" ],
                  "result": { "item": "%OUT%" },
                  "show_notification": true
                }""".replace("%PLK%", plank).replace("%OUT%", out);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseLootSimpleBlock(String blockItem) {
        String j = """
                {
                  "type": "minecraft:block",
                  "pools": [
                    {
                      "bonus_rolls": 0,
                      "entries": [
                        {
                          "type": "minecraft:item",
                          "functions": [
                            {
                              "add": false,
                              "count": { "type": "minecraft:constant", "value": 1 },
                              "function": "minecraft:set_count"
                            },
                            { "function": "minecraft:explosion_decay" }
                          ],
                          "name": "%ITEM%"
                        }
                      ],
                      "rolls": 1
                    }
                  ]
                }""".replace("%ITEM%", blockItem);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseLootSlab(String slabItem) {
        String block = slabItem.substring(slabItem.indexOf(':') + 1);
        String j = """
                {
                  "type": "minecraft:block",
                  "pools": [
                    {
                      "bonus_rolls": 0,
                      "entries": [
                        {
                          "type": "minecraft:item",
                          "functions": [
                            {
                              "add": false,
                              "conditions": [
                                {
                                  "block": "%MOD%:%BLK%",
                                  "condition": "minecraft:block_state_property",
                                  "properties": { "type": "double" }
                                }
                              ],
                              "count": 2,
                              "function": "minecraft:set_count"
                            },
                            { "function": "minecraft:explosion_decay" }
                          ],
                          "name": "%ITEM%"
                        }
                      ],
                      "rolls": 1
                    }
                  ]
                }"""
                .replace("%MOD%", PenumbraPhantasm.MODID)
                .replace("%BLK%", block)
                .replace("%ITEM%", slabItem);
        return JsonParser.parseString(j);
    }

    private static JsonElement parseLootDoor(String doorItem) {
        String block = doorItem.substring(doorItem.indexOf(':') + 1);
        String j = """
                {
                  "type": "minecraft:block",
                  "pools": [
                    {
                      "bonus_rolls": 0,
                      "conditions": [ { "condition": "minecraft:survives_explosion" } ],
                      "entries": [
                        {
                          "type": "minecraft:item",
                          "conditions": [
                            {
                              "block": "%MOD%:%BLK%",
                              "condition": "minecraft:block_state_property",
                              "properties": { "half": "lower" }
                            }
                          ],
                          "name": "%ITEM%"
                        }
                      ],
                      "rolls": 1
                    }
                  ]
                }"""
                .replace("%MOD%", PenumbraPhantasm.MODID)
                .replace("%BLK%", block)
                .replace("%ITEM%", doorItem);
        return JsonParser.parseString(j);
    }
}