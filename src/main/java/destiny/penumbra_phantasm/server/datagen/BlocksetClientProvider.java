package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.JsonObject;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.registry.BlocksetRegistry;

import java.io.IOException;
import java.nio.file.Path;

public final class BlocksetClientProvider {
    private BlocksetClientProvider() {
    }

    public static void run(Path mainResourcesRoot) throws IOException {
        for (StoneBlockset s : BlocksetRegistry.STONE_BLOCKSETS) {
            emitStoneFamily(mainResourcesRoot, s);
        }
        for (BricksBlockset b : BlocksetRegistry.BRICKS_BLOCKSETS) {
            emitStoneFamily(mainResourcesRoot, b);
        }
        for (WoodBlockset w : BlocksetRegistry.WOOD_BLOCKSETS) {
            emitWood(mainResourcesRoot, w);
        }
    }

    private static void emitStoneFamily(Path root, StoneFamilyBlockset s) throws IOException {
        String mod = PenumbraPhantasm.MODID;
        String tx = s.textureKey();
        String b = s.baseName();
        boolean em = s.isEmissive();
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + b + ".json", BlocksetStoneTemplates.fullBlockstate(s));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + s.stairs() + ".json", BlocksetStoneTemplates.stairBlockstate(s));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + s.slab() + ".json", BlocksetStoneTemplates.slabBlockstate(s));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + s.wall() + ".json", BlocksetStoneTemplates.wallBlockstate(s));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + s.button() + ".json", BlocksetStoneTemplates.buttonBlockstate(s));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + s.pressurePlate() + ".json", BlocksetStoneTemplates.pressurePlateBlockstate(s));

        writeModel(root, mod, b, BlocksetStoneModels.cubeAll(tx, em));
        writeModel(root, mod, s.stairs(), BlocksetStoneModels.stairs(tx, em));
        writeModel(root, mod, s.stairs() + "_inner", BlocksetStoneModels.stairsInner(tx, em));
        writeModel(root, mod, s.stairs() + "_outer", BlocksetStoneModels.stairsOuter(tx, em));
        writeModel(root, mod, s.slab(), BlocksetStoneModels.slab(tx, em));
        writeModel(root, mod, s.slab() + "_top", BlocksetStoneModels.slabTop(tx, em));
        writeModel(root, mod, s.wall() + "_post", BlocksetStoneModels.wallPost(tx, em));
        writeModel(root, mod, s.wall() + "_side", BlocksetStoneModels.wallSide(tx, em));
        writeModel(root, mod, s.wall() + "_side_tall", BlocksetStoneModels.wallSideTall(tx, em));
        writeModel(root, mod, s.wall() + "_inventory", BlocksetStoneModels.wallInventory(tx, em));
        writeModel(root, mod, s.button(), BlocksetStoneModels.button(tx, em));
        writeModel(root, mod, s.button() + "_inventory", BlocksetStoneModels.buttonInventory(tx, em));
        writeModel(root, mod, s.button() + "_pressed", BlocksetStoneModels.buttonPressed(tx, em));
        writeModel(root, mod, s.pressurePlate(), BlocksetStoneModels.pressurePlateUp(tx, em));
        writeModel(root, mod, s.pressurePlate() + "_down", BlocksetStoneModels.pressurePlateDown(tx, em));

        for (String p : s.allPieces()) {
            if (p.equals(s.wall())) {
                JsonObject o = new JsonObject();
                o.addProperty("parent", mod + ":block/" + s.wall() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/item/" + p + ".json", o);
            } else if (p.equals(s.button())) {
                JsonObject o = new JsonObject();
                o.addProperty("parent", mod + ":block/" + s.button() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/item/" + p + ".json", o);
            } else {
                itemParentBlock(root, mod, p);
            }
        }
    }

    private static void emitWood(Path root, WoodBlockset w) throws IOException {
        String mod = PenumbraPhantasm.MODID;
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.planks() + ".json", BlocksetVanillaWoodSubst.json(WoodBlockstateTemplates2.PLANKS, w));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.stairs() + ".json", BlocksetStoneTemplates.stairBlockstateStem(w.stairs()));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.slab() + ".json", BlocksetVanillaWoodSubst.json(WoodBlockstateTemplates2.SLAB, w));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.fence() + ".json", BlocksetVanillaWoodSubst.json(WoodBlockstateTemplates.FENCE, w));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.fenceGate() + ".json", BlocksetVanillaWoodSubst.json(WoodBlockstateTemplates.FENCE_GATE, w));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.door() + ".json", WoodDoorBlockstate.asJson(w));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.trapdoor() + ".json", BlocksetVanillaWoodSubst.json(WoodBlockstateTemplates.TRAPDOOR, w));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.button() + ".json", BlocksetVanillaWoodSubst.json(WoodBlockstateTemplates2.BUTTON, w));
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/blockstates/" + w.pressurePlate() + ".json", BlocksetVanillaWoodSubst.json(WoodBlockstateTemplates2.PRESSURE_PLATE, w));

        writeModel(root, mod, w.planks(), VanillaWoodBlockModels.planks(w));
        writeModel(root, mod, w.stairs(), VanillaWoodBlockModels.stairs(w));
        writeModel(root, mod, w.stairs() + "_inner", VanillaWoodBlockModels.stairsInner(w));
        writeModel(root, mod, w.stairs() + "_outer", VanillaWoodBlockModels.stairsOuter(w));
        writeModel(root, mod, w.slab(), VanillaWoodBlockModels.slab(w));
        writeModel(root, mod, w.slab() + "_top", VanillaWoodBlockModels.slabTop(w));
        writeModel(root, mod, w.fence() + "_post", VanillaWoodBlockModels.fencePost(w));
        writeModel(root, mod, w.fence() + "_side", VanillaWoodBlockModels.fenceSide(w));
        writeModel(root, mod, w.fence() + "_inventory", VanillaWoodBlockModels.fenceInventory(w));
        writeModel(root, mod, w.fenceGate(), VanillaWoodBlockModels.fenceGate(w));
        writeModel(root, mod, w.fenceGate() + "_open", VanillaWoodBlockModels.fenceGateOpen(w));
        writeModel(root, mod, w.fenceGate() + "_wall", VanillaWoodBlockModels.fenceGateWall(w));
        writeModel(root, mod, w.fenceGate() + "_wall_open", VanillaWoodBlockModels.fenceGateWallOpen(w));
        String d = w.door();
        writeModel(root, mod, d + "_bottom_left", VanillaWoodBlockModels.doorBottomLeft(w));
        writeModel(root, mod, d + "_bottom_left_open", VanillaWoodBlockModels.doorBottomLeftOpen(w));
        writeModel(root, mod, d + "_bottom_right", VanillaWoodBlockModels.doorBottomRight(w));
        writeModel(root, mod, d + "_bottom_right_open", VanillaWoodBlockModels.doorBottomRightOpen(w));
        writeModel(root, mod, d + "_top_left", VanillaWoodBlockModels.doorTopLeft(w));
        writeModel(root, mod, d + "_top_left_open", VanillaWoodBlockModels.doorTopLeftOpen(w));
        writeModel(root, mod, d + "_top_right", VanillaWoodBlockModels.doorTopRight(w));
        writeModel(root, mod, d + "_top_right_open", VanillaWoodBlockModels.doorTopRightOpen(w));
        writeModel(root, mod, w.trapdoor() + "_bottom", VanillaWoodBlockModels.trapdoorBottom(w));
        writeModel(root, mod, w.trapdoor() + "_top", VanillaWoodBlockModels.trapdoorTop(w));
        writeModel(root, mod, w.trapdoor() + "_open", VanillaWoodBlockModels.trapdoorOpen(w));
        writeModel(root, mod, w.button() + "_inventory", VanillaWoodBlockModels.buttonInventory(w));
        writeModel(root, mod, w.button(), VanillaWoodBlockModels.button(w));
        writeModel(root, mod, w.button() + "_pressed", VanillaWoodBlockModels.buttonPressed(w));
        writeModel(root, mod, w.pressurePlate(), VanillaWoodBlockModels.pressurePlateUp(w));
        writeModel(root, mod, w.pressurePlate() + "_down", VanillaWoodBlockModels.pressurePlateDown(w));

        for (String p : w.allPieces()) {
            if (p.equals(w.door())) {
                itemDoor(root, mod, w.door());
            } else if (p.equals(w.fence())) {
                JsonObject o = new JsonObject();
                o.addProperty("parent", mod + ":block/" + w.fence() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/item/" + w.fence() + ".json", o);
            } else if (p.equals(w.trapdoor())) {
                JsonObject o = new JsonObject();
                o.addProperty("parent", mod + ":block/" + w.trapdoor() + "_bottom");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/item/" + w.trapdoor() + ".json", o);
            } else if (p.equals(w.button())) {
                JsonObject o = new JsonObject();
                o.addProperty("parent", mod + ":block/" + w.button() + "_inventory");
                BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/item/" + w.button() + ".json", o);
            } else {
                itemParentBlock(root, mod, p);
            }
        }
    }

    private static void writeModel(Path root, String mod, String name, com.google.gson.JsonElement json) throws IOException {
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/block/" + name + ".json", json);
    }

    private static void itemParentBlock(Path root, String mod, String name) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("parent", mod + ":block/" + name);
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/item/" + name + ".json", o);
    }

    private static void itemDoor(Path root, String mod, String doorName) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:item/generated");
        JsonObject t = new JsonObject();
        t.addProperty("layer0", mod + ":item/" + doorName);
        o.add("textures", t);
        BlocksetJsonIO.writeIfAbsent(root, "assets/" + mod + "/models/item/" + doorName + ".json", o);
    }
}