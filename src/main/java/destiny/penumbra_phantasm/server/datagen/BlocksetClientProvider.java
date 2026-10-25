package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import destiny.penumbra_phantasm.server.datagen.blocksets.BricksBlockset;
import destiny.penumbra_phantasm.server.datagen.blocksets.StoneBlockset;
import destiny.penumbra_phantasm.server.datagen.blocksets.StoneFamilyBlockset;
import destiny.penumbra_phantasm.server.datagen.blocksets.WoodBlockset;
import destiny.penumbra_phantasm.server.datagen.blockstates.BlockSetWoodDoorBlockstates;
import destiny.penumbra_phantasm.server.datagen.blockstates.BlocksetStoneBlockstates;
import destiny.penumbra_phantasm.server.datagen.blockstates.BlocksetWoodBlockstates;
import destiny.penumbra_phantasm.server.datagen.models.BlocksetStoneModels;
import destiny.penumbra_phantasm.server.datagen.models.BlocksetWoodModels;
import destiny.penumbra_phantasm.server.registry.BlocksetRegistry;

import java.io.IOException;
import java.nio.file.Path;

import static destiny.penumbra_phantasm.PenumbraPhantasm.MODID;

public class BlocksetClientProvider {
    public BlocksetClientProvider() {
    }

    public static void run(Path mainResourcesRoot) throws IOException {
        for (StoneBlockset stoneSet : BlocksetRegistry.STONE_BLOCKSETS) {
            createStoneFamily(mainResourcesRoot, stoneSet);
        }
        for (BricksBlockset brickSet : BlocksetRegistry.BRICKS_BLOCKSETS) {
            createStoneFamily(mainResourcesRoot, brickSet);
        }
        for (WoodBlockset woodSet : BlocksetRegistry.WOOD_BLOCKSETS) {
            createWoodFamily(mainResourcesRoot, woodSet);
        }
    }

    public static void createStoneFamily(Path root, StoneFamilyBlockset stoneSet) throws IOException {
        String textureKey = stoneSet.textureKey();
        String baseName = stoneSet.baseName();
        boolean isEmissive = stoneSet.isEmissive();
        String addAtEndOfSingular = stoneSet.addAtEndOfSingular();

        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + baseName + addAtEndOfSingular + ".json", BlocksetStoneBlockstates.makeFullBlockstate(stoneSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + stoneSet.stairs() + ".json", BlocksetStoneBlockstates.makeStairBlockstate(stoneSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + stoneSet.slab() + ".json", BlocksetStoneBlockstates.makeSlabBlockstate(stoneSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + stoneSet.wall() + ".json", BlocksetStoneBlockstates.makeWallBlockstate(stoneSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + stoneSet.button() + ".json", BlocksetStoneBlockstates.makeButtonBlockstate(stoneSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + stoneSet.pressurePlate() + ".json", BlocksetStoneBlockstates.makePressurePlateBlockstate(stoneSet));

        writeBlock(root, baseName + addAtEndOfSingular, BlocksetStoneModels.makeCubeAll(textureKey, isEmissive));

        writeBlock(root, stoneSet.stairs(), BlocksetStoneModels.makeStairs(textureKey, isEmissive));
        writeBlock(root, stoneSet.stairs() + "_inner", BlocksetStoneModels.makeStairsInner(textureKey, isEmissive));
        writeBlock(root, stoneSet.stairs() + "_outer", BlocksetStoneModels.makeStairsOuter(textureKey, isEmissive));

        writeBlock(root, stoneSet.slab(), BlocksetStoneModels.makeSlab(textureKey, isEmissive));
        writeBlock(root, stoneSet.slab() + "_top", BlocksetStoneModels.makeSlabTop(textureKey, isEmissive));

        writeBlock(root, stoneSet.wall() + "_post", BlocksetStoneModels.makeWallPost(textureKey, isEmissive));
        writeBlock(root, stoneSet.wall() + "_side", BlocksetStoneModels.makeWallSide(textureKey, isEmissive));
        writeBlock(root, stoneSet.wall() + "_side_tall", BlocksetStoneModels.makeWallSideTall(textureKey, isEmissive));
        writeBlock(root, stoneSet.wall() + "_inventory", BlocksetStoneModels.makeWallInventory(textureKey, isEmissive));

        writeBlock(root, stoneSet.button(), BlocksetStoneModels.makeButton(textureKey, isEmissive));
        writeBlock(root, stoneSet.button() + "_inventory", BlocksetStoneModels.makeButtonInventory(textureKey, isEmissive));
        writeBlock(root, stoneSet.button() + "_pressed", BlocksetStoneModels.makeButtonPressed(textureKey, isEmissive));

        writeBlock(root, stoneSet.pressurePlate(), BlocksetStoneModels.makePressurePlateUp(textureKey, isEmissive));
        writeBlock(root, stoneSet.pressurePlate() + "_down", BlocksetStoneModels.makePressurePlateDown(textureKey, isEmissive));

        for (String piece : stoneSet.allPieces()) {
            if (piece.equals(stoneSet.baseName() + addAtEndOfSingular)) {
                JsonObject json = new JsonObject();

                json.addProperty("parent", MODID + ":block/" + stoneSet.baseName() + addAtEndOfSingular);
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + stoneSet.baseName() + addAtEndOfSingular + ".json", json);
            } else if (piece.equals(stoneSet.wall())) {
                JsonObject json = new JsonObject();

                json.addProperty("parent", MODID + ":block/" + stoneSet.wall() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + piece + ".json", json);
            } else if (piece.equals(stoneSet.button())) {
                JsonObject json = new JsonObject();

                json.addProperty("parent", MODID + ":block/" + stoneSet.button() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + piece + ".json", json);
            } else {
                writeItemParentBlock(root, piece);
            }
        }
    }

    public static void createWoodFamily(Path root, WoodBlockset woodSet) throws IOException {
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.planks() + ".json", BlocksetVanillaWoodReplace.replaceJson(BlocksetWoodBlockstates.PLANKS, woodSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.stairs() + ".json", BlocksetStoneBlockstates.makeStairBlockstate(woodSet.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.slab() + ".json", BlocksetVanillaWoodReplace.replaceJson(BlocksetWoodBlockstates.SLAB, woodSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.fence() + ".json", BlocksetVanillaWoodReplace.replaceJson(BlocksetWoodBlockstates.FENCE, woodSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.fenceGate() + ".json", BlocksetVanillaWoodReplace.replaceJson(BlocksetWoodBlockstates.FENCE_GATE, woodSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.door() + ".json", BlockSetWoodDoorBlockstates.asJson(woodSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.trapdoor() + ".json", BlocksetVanillaWoodReplace.replaceJson(BlocksetWoodBlockstates.TRAPDOOR, woodSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.button() + ".json", BlocksetVanillaWoodReplace.replaceJson(BlocksetWoodBlockstates.BUTTON, woodSet));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/blockstates/" + woodSet.pressurePlate() + ".json", BlocksetVanillaWoodReplace.replaceJson(BlocksetWoodBlockstates.PRESSURE_PLATE, woodSet));

        writeBlock(root, woodSet.planks(), BlocksetWoodModels.planks(woodSet));

        writeBlock(root, woodSet.stairs(), BlocksetWoodModels.stairs(woodSet));
        writeBlock(root, woodSet.stairs() + "_inner", BlocksetWoodModels.stairsInner(woodSet));
        writeBlock(root, woodSet.stairs() + "_outer", BlocksetWoodModels.stairsOuter(woodSet));

        writeBlock(root, woodSet.slab(), BlocksetWoodModels.slab(woodSet));
        writeBlock(root, woodSet.slab() + "_top", BlocksetWoodModels.slabTop(woodSet));

        writeBlock(root, woodSet.fence() + "_post", BlocksetWoodModels.fencePost(woodSet));
        writeBlock(root, woodSet.fence() + "_side", BlocksetWoodModels.fenceSide(woodSet));
        writeBlock(root, woodSet.fence() + "_inventory", BlocksetWoodModels.fenceInventory(woodSet));

        writeBlock(root, woodSet.fenceGate(), BlocksetWoodModels.fenceGate(woodSet));
        writeBlock(root, woodSet.fenceGate() + "_open", BlocksetWoodModels.fenceGateOpen(woodSet));
        writeBlock(root, woodSet.fenceGate() + "_wall", BlocksetWoodModels.fenceGateWall(woodSet));
        writeBlock(root, woodSet.fenceGate() + "_wall_open", BlocksetWoodModels.fenceGateWallOpen(woodSet));

        String door = woodSet.door();
        writeBlock(root, door + "_bottom_left", BlocksetWoodModels.doorBottomLeft(woodSet));
        writeBlock(root, door + "_bottom_left_open", BlocksetWoodModels.doorBottomLeftOpen(woodSet));
        writeBlock(root, door + "_bottom_right", BlocksetWoodModels.doorBottomRight(woodSet));
        writeBlock(root, door + "_bottom_right_open", BlocksetWoodModels.doorBottomRightOpen(woodSet));
        writeBlock(root, door + "_top_left", BlocksetWoodModels.doorTopLeft(woodSet));
        writeBlock(root, door + "_top_left_open", BlocksetWoodModels.doorTopLeftOpen(woodSet));
        writeBlock(root, door + "_top_right", BlocksetWoodModels.doorTopRight(woodSet));
        writeBlock(root, door + "_top_right_open", BlocksetWoodModels.doorTopRightOpen(woodSet));

        writeBlock(root, woodSet.trapdoor() + "_bottom", BlocksetWoodModels.trapdoorBottom(woodSet));
        writeBlock(root, woodSet.trapdoor() + "_top", BlocksetWoodModels.trapdoorTop(woodSet));
        writeBlock(root, woodSet.trapdoor() + "_open", BlocksetWoodModels.trapdoorOpen(woodSet));

        writeBlock(root, woodSet.button() + "_inventory", BlocksetWoodModels.buttonInventory(woodSet));
        writeBlock(root, woodSet.button(), BlocksetWoodModels.button(woodSet));
        writeBlock(root, woodSet.button() + "_pressed", BlocksetWoodModels.buttonPressed(woodSet));

        writeBlock(root, woodSet.pressurePlate(), BlocksetWoodModels.pressurePlateUp(woodSet));
        writeBlock(root, woodSet.pressurePlate() + "_down", BlocksetWoodModels.pressurePlateDown(woodSet));

        for (String piece : woodSet.allPieces()) {
            if (piece.equals(woodSet.door())) {
                writeItemDoor(root, woodSet.door());
            } else if (piece.equals(woodSet.fence())) {
                JsonObject json = new JsonObject();

                json.addProperty("parent", MODID + ":block/" + woodSet.fence() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + woodSet.fence() + ".json", json);
            } else if (piece.equals(woodSet.trapdoor())) {
                JsonObject json = new JsonObject();

                json.addProperty("parent", MODID + ":block/" + woodSet.trapdoor() + "_bottom");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + woodSet.trapdoor() + ".json", json);
            } else if (piece.equals(woodSet.button())) {
                JsonObject json = new JsonObject();

                json.addProperty("parent", MODID + ":block/" + woodSet.button() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + woodSet.button() + ".json", json);
            } else {
                writeItemParentBlock(root, piece);
            }
        }
    }

    public static void writeBlock(Path root, String name, JsonElement json) throws IOException {
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/block/" + name + ".json", json);
    }

    public static void writeItemParentBlock(Path root, String name) throws IOException {
        JsonObject json = new JsonObject();

        json.addProperty("parent", MODID + ":block/" + name);
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + name + ".json", json);
    }

    public static void writeItemDoor(Path root, String doorName) throws IOException {
        JsonObject json = new JsonObject();

        json.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", MODID + ":item/" + doorName);
        json.add("textures", textures);

        BlocksetJsonIO.writeIfAbsent(root, "assets/" + MODID + "/models/item/" + doorName + ".json", json);
    }
}