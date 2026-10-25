package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import destiny.penumbra_phantasm.server.datagen.blocksets.BricksBlockset;
import destiny.penumbra_phantasm.server.datagen.blocksets.StoneBlockset;
import destiny.penumbra_phantasm.server.datagen.blocksets.StoneFamilyBlockset;
import destiny.penumbra_phantasm.server.datagen.blocksets.WoodBlockset;
import destiny.penumbra_phantasm.server.registry.BlocksetRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static destiny.penumbra_phantasm.PenumbraPhantasm.MODID;

public class BlocksetServerProvider {
    public BlocksetServerProvider() {
    }

    public static void run(Path mainResourcesRoot) throws IOException {
        for (StoneBlockset stoneSet : BlocksetRegistry.STONE_BLOCKSETS) {
            createStoneFamilyRecipesAndLootTables(mainResourcesRoot, stoneSet);
        }
        for (BricksBlockset bricksSet : BlocksetRegistry.BRICKS_BLOCKSETS) {
            createStoneFamilyRecipesAndLootTables(mainResourcesRoot, bricksSet);
        }
        for (WoodBlockset woodSet : BlocksetRegistry.WOOD_BLOCKSETS) {
            createWoodFamilyRecipesAndLootTables(mainResourcesRoot, woodSet);
        }
        
        mergeAllBlocksetTags(mainResourcesRoot);
    }

    public static void mergeAllBlocksetTags(Path root) throws IOException {
        for (StoneBlockset stoneSet : BlocksetRegistry.STONE_BLOCKSETS) {
            mergeStoneFamilyTags(root, stoneSet);
        }
        for (BricksBlockset bricksSet : BlocksetRegistry.BRICKS_BLOCKSETS) {
            mergeStoneFamilyTags(root, bricksSet);
        }
        for (WoodBlockset woodSet : BlocksetRegistry.WOOD_BLOCKSETS) {
            mergeWoodTags(root, woodSet);
        }
    }

    public static List<String> blockIds(Iterable<String> names) {
        List<String> out = new ArrayList<>();

        for (String name : names) {
            out.add(MODID + ":" + name);
        }

        return out;
    }

    public static void mergeStoneFamilyTags(Path root, StoneFamilyBlockset stoneSet) throws IOException {
        List<String> allIds = blockIds(stoneSet.allPieces());

        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/mineable/pickaxe.json", allIds);

        String needsTier = switch (stoneSet.miningTier()) {
            case 1 -> "data/minecraft/tags/blocks/needs_stone_tool.json";
            case 2 -> "data/minecraft/tags/blocks/needs_iron_tool.json";
            case 3 -> "data/minecraft/tags/blocks/needs_diamond_tool.json";

            default -> null;
        };

        if (needsTier != null) {
            BlocksetJsonIO.mergeTagBlockAndItem(root, needsTier, allIds);
        }

        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stairs.json", List.of(MODID + ":" + stoneSet.stairs()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/slabs.json", List.of(MODID + ":" + stoneSet.slab()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/walls.json", List.of(MODID + ":" + stoneSet.wall()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stone_buttons.json", List.of(MODID + ":" + stoneSet.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stone_pressure_plates.json", List.of(MODID + ":" + stoneSet.pressurePlate()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/buttons.json", List.of(MODID + ":" + stoneSet.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/pressure_plates.json", List.of(MODID + ":" + stoneSet.pressurePlate()));

        String baseId = MODID + ":" + stoneSet.baseName();
        if (stoneSet.includeToStone()) {
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/stone.json", List.of(baseId));
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/forge/tags/blocks/stone.json", List.of(baseId));
        }

        if (stoneSet.includeToCobblestone()) {
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/cobblestone.json", List.of(baseId));
            BlocksetJsonIO.mergeTagBlockAndItem(root, "data/forge/tags/blocks/cobblestone.json", List.of(baseId));
        }
    }

    public static void mergeWoodTags(Path root, WoodBlockset woodSet) throws IOException {
        List<String> all = blockIds(woodSet.allPieces());

        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/mineable/axe.json", all);
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/planks.json", List.of(MODID + ":" + woodSet.planks()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_stairs.json", List.of(MODID + ":" + woodSet.stairs()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_slabs.json", List.of(MODID + ":" + woodSet.slab()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_fences.json", List.of(MODID + ":" + woodSet.fence()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/fences.json", List.of(MODID + ":" + woodSet.fence()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/fence_gates.json", List.of(MODID + ":" + woodSet.fenceGate()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_doors.json", List.of(MODID + ":" + woodSet.door()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/doors.json", List.of(MODID + ":" + woodSet.door()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_trapdoors.json", List.of(MODID + ":" + woodSet.trapdoor()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/trapdoors.json", List.of(MODID + ":" + woodSet.trapdoor()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_buttons.json", List.of(MODID + ":" + woodSet.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/buttons.json", List.of(MODID + ":" + woodSet.button()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/wooden_pressure_plates.json", List.of(MODID + ":" + woodSet.pressurePlate()));
        BlocksetJsonIO.mergeTagBlockAndItem(root, "data/minecraft/tags/blocks/pressure_plates.json", List.of(MODID + ":" + woodSet.pressurePlate()));
    }

    public static String recipesFolder(String recipeSubPath) {
        if (recipeSubPath == null || recipeSubPath.isEmpty()) {
            return "data/" + MODID + "/recipes/";
        }

        return "data/" + MODID + "/recipes/" + recipeSubPath + "/";
    }

    public static void createStoneFamilyRecipesAndLootTables(Path root, StoneFamilyBlockset stoneSet) throws IOException {
        String recipesFolder = recipesFolder(stoneSet.recipeSubPath());
        String baseName = stoneSet.baseName();
        String material = MODID + ":" + stoneSet.fullBlockMaterialItem();
        String name = MODID + ":" + baseName;

        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + baseName + stoneSet.addAtEndOfSingular() + ".json", parseRecipeFullBlock(material, name + stoneSet.addAtEndOfSingular(), 4));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + stoneSet.stairs() + ".json", parseRecipeStairs(name, MODID + ":" + stoneSet.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + stoneSet.slab() + ".json", parseRecipeSlab(name, MODID + ":" + stoneSet.slab()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + stoneSet.wall() + ".json", parseRecipeWall(name, MODID + ":" + stoneSet.wall()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + stoneSet.button() + ".json", parseRecipeStoneButton(name, MODID + ":" + stoneSet.button()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + stoneSet.pressurePlate() + ".json", parseRecipeStonePressurePlate(name, MODID + ":" + stoneSet.pressurePlate()));

        String lootBase = "data/" + MODID + "/loot_tables/blocks/";
        BlocksetJsonIO.writeIfAbsent(root, lootBase + baseName + stoneSet.addAtEndOfSingular() + ".json", parseLootSimpleBlock(MODID + ":" + baseName + stoneSet.addAtEndOfSingular()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + stoneSet.stairs() + ".json", parseLootSimpleBlock(MODID + ":" + stoneSet.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + stoneSet.slab() + ".json", parseLootSlab(MODID + ":" + stoneSet.slab()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + stoneSet.wall() + ".json", parseLootSimpleBlock(MODID + ":" + stoneSet.wall()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + stoneSet.button() + ".json", parseLootSimpleBlock(MODID + ":" + stoneSet.button()));
        BlocksetJsonIO.writeIfAbsent(root, lootBase + stoneSet.pressurePlate() + ".json", parseLootSimpleBlock(MODID + ":" + stoneSet.pressurePlate()));
    }

    public static void createWoodFamilyRecipesAndLootTables(Path root, WoodBlockset woodSet) throws IOException {
        String recipesFolder = recipesFolder(woodSet.recipeSubPath());
        String plankId = MODID + ":" + woodSet.planks();
        String material = woodSet.fullBlockMaterialItem();

        if (material != null && !material.isEmpty()) {
            BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.planks() + ".json",
                    parseRecipePlanksFromMaterial(MODID + ":" + material, plankId, 4));
        }

        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.stairs() + ".json", parseRecipeWoodStairs(plankId, MODID + ":" + woodSet.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.slab() + ".json", parseRecipeSlab3(plankId, MODID + ":" + woodSet.slab(), 6));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.fence() + ".json", parseRecipeFence(plankId, MODID + ":" + woodSet.fence(), 3));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.fenceGate() + ".json", parseRecipeFenceGate(plankId, MODID + ":" + woodSet.fenceGate()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.door() + ".json", parseRecipeDoor(plankId, MODID + ":" + woodSet.door()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.trapdoor() + ".json", parseRecipeTrapdoor(plankId, MODID + ":" + woodSet.trapdoor()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.button() + ".json", parseRecipeWoodButton(plankId, MODID + ":" + woodSet.button()));
        BlocksetJsonIO.writeIfAbsent(root, recipesFolder + woodSet.pressurePlate() + ".json", parseRecipeWoodPressurePlate(plankId, MODID + ":" + woodSet.pressurePlate()));

        String lootBase = "data/" + MODID + "/loot_tables/blocks/";
        for (String piece : woodSet.allPieces()) {
            String id = MODID + ":" + piece;

            if (piece.equals(woodSet.door())) {
                BlocksetJsonIO.writeIfAbsent(root, lootBase + piece + ".json", parseLootDoor(id));
            } else if (piece.equals(woodSet.slab())) {
                BlocksetJsonIO.writeIfAbsent(root, lootBase + piece + ".json", parseLootSlab(id));
            } else {
                BlocksetJsonIO.writeIfAbsent(root, lootBase + piece + ".json", parseLootSimpleBlock(id));
            }
        }
    }

    public static JsonElement parseRecipePlanksFromMaterial(String materialItem, String plankItem, int count) {
        String j = """
                {
                  "type": "minecraft:crafting_shapeless",
                  "group": "planks",
                  "ingredients": [ { "item": "%MAT%" } ],
                  "result": { "count": %CNT%, "item": "%OUT%" }
                }""".replace("%MAT%", materialItem).replace("%OUT%", plankItem).replace("%CNT%", Integer.toString(count));
        return JsonParser.parseString(j);
    }

    public static JsonElement parseRecipeFullBlock(String material, String result, int count) {
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

    public static JsonElement parseRecipeStairs(String baseItem, String out) {
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

    public static JsonElement parseRecipeWoodStairs(String plankItem, String out) {
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

    public static JsonElement parseRecipeSlab(String baseItem, String out) {
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

    public static JsonElement parseRecipeSlab3(String plankItem, String out, int count) {
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

    public static JsonElement parseRecipeWall(String baseItem, String out) {
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

    public static JsonElement parseRecipeStoneButton(String baseItem, String out) {
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

    public static JsonElement parseRecipeStonePressurePlate(String baseItem, String out) {
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

    public static JsonElement parseRecipeFence(String plank, String out, int count) {
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

    public static JsonElement parseRecipeFenceGate(String plank, String out) {
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

    public static JsonElement parseRecipeDoor(String plank, String out) {
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

    public static JsonElement parseRecipeTrapdoor(String plank, String out) {
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

    public static JsonElement parseRecipeWoodButton(String plank, String out) {
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

    public static JsonElement parseRecipeWoodPressurePlate(String plank, String out) {
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

    public static JsonElement parseLootSimpleBlock(String blockItem) {
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

    public static JsonElement parseLootSlab(String slabItem) {
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
                }""".replace("%MOD%", MODID).replace("%BLK%", block).replace("%ITEM%", slabItem);
        return JsonParser.parseString(j);
    }

    public static JsonElement parseLootDoor(String doorItem) {
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
                }""".replace("%MOD%", MODID).replace("%BLK%", block).replace("%ITEM%", doorItem);
        return JsonParser.parseString(j);
    }
}