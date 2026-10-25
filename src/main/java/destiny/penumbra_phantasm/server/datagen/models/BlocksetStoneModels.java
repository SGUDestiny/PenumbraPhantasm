package destiny.penumbra_phantasm.server.datagen.models;

import com.google.gson.JsonObject;

import static destiny.penumbra_phantasm.PenumbraPhantasm.MODID;

public class BlocksetStoneModels {
    public BlocksetStoneModels() {
    }

    public static String getFullTextureId(String textureKey) {
        return MODID + ":block/" + textureKey;
    }

    public static JsonObject makeThreeFace(String textureKey) {
        JsonObject json = new JsonObject();

        String texture = getFullTextureId(textureKey);
        json.addProperty("bottom", texture);
        json.addProperty("side", texture);
        json.addProperty("top", texture);

        return json;
    }

    public static JsonObject makeCubeAll(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_cube_all_emissive" : "minecraft:block/cube_all");

        JsonObject texture = new JsonObject();
        texture.addProperty("all", getFullTextureId(textureKey));

        json.add("textures", texture);

        return json;
    }

    public static JsonObject makeStairs(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_stairs_emissive" : "minecraft:block/stairs");
        json.add("textures", makeThreeFace(textureKey));

        return json;
    }

    public static JsonObject makeStairsInner(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_inner_stairs_emissive" : "minecraft:block/inner_stairs");
        json.add("textures", makeThreeFace(textureKey));

        return json;
    }

    public static JsonObject makeStairsOuter(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_outer_stairs_emissive" : "minecraft:block/outer_stairs");
        json.add("textures", makeThreeFace(textureKey));

        return json;
    }

    public static JsonObject makeSlab(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_slab_emissive" : "minecraft:block/slab");
        json.add("textures", makeThreeFace(textureKey));

        return json;
    }

    public static JsonObject makeSlabTop(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_slab_top_emissive" : "minecraft:block/slab_top");
        json.add("textures", makeThreeFace(textureKey));

        return json;
    }

    public static JsonObject makeWallPost(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_wall_post_emissive" : "minecraft:block/template_wall_post");
        JsonObject texture = new JsonObject();

        texture.addProperty("wall", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makeWallSide(String textureKey, boolean isEmissive) {
        JsonObject json
                = new JsonObject();
        json.addProperty("parent", isEmissive ? MODID + ":block/template_wall_side_emissive" : "minecraft:block/template_wall_side");
        JsonObject texture = new JsonObject();

        texture.addProperty("wall", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makeWallSideTall(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_wall_side_tall_emissive" : "minecraft:block/template_wall_side_tall");

        JsonObject texture = new JsonObject();
        texture.addProperty("wall", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makeWallInventory(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_wall_inventory_emissive" : "minecraft:block/wall_inventory");

        JsonObject texture = new JsonObject();
        texture.addProperty("wall", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makeButton(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_button_emissive" : "minecraft:block/button");

        JsonObject texture = new JsonObject();
        texture.addProperty("texture", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makeButtonInventory(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_button_inventory_emissive" : "minecraft:block/button_inventory");

        JsonObject texture = new JsonObject();
        texture.addProperty("texture", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makeButtonPressed(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_button_pressed_emissive" : "minecraft:block/button_pressed");

        JsonObject texture = new JsonObject();
        texture.addProperty("texture", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makePressurePlateUp(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_pressure_plate_up_emissive" : "minecraft:block/pressure_plate_up");

        JsonObject texture = new JsonObject();
        texture.addProperty("texture", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }

    public static JsonObject makePressurePlateDown(String textureKey, boolean isEmissive) {
        JsonObject json = new JsonObject();

        json.addProperty("parent", isEmissive ? MODID + ":block/template_pressure_plate_down_emissive" : "minecraft:block/pressure_plate_down");

        JsonObject texture = new JsonObject();
        texture.addProperty("texture", getFullTextureId(textureKey));
        json.add("textures", texture);

        return json;
    }
}