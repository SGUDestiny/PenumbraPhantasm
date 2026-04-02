package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.JsonObject;
import destiny.penumbra_phantasm.PenumbraPhantasm;

public final class BlocksetStoneModels {

    private BlocksetStoneModels() {
    }

    private static String tex(String textureKey) {
        return PenumbraPhantasm.MODID + ":block/" + textureKey;
    }

    private static JsonObject threeFace(String textureKey) {
        JsonObject t = new JsonObject();
        String p = tex(textureKey);
        t.addProperty("bottom", p);
        t.addProperty("side", p);
        t.addProperty("top", p);
        return t;
    }

    public static JsonObject cubeAll(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/cube_all");
        JsonObject t = new JsonObject();
        t.addProperty("all", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject stairs(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/stairs");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject stairsInner(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/inner_stairs");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject stairsOuter(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/outer_stairs");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject slab(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/slab");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject slabTop(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/slab_top");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject wallPost(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/template_wall_post");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject wallSide(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/template_wall_side");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject wallSideTall(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/template_wall_side_tall");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject wallInventory(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/wall_inventory");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject button(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/button");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject buttonPressed(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/button_pressed");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject pressurePlateUp(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/pressure_plate_up");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject pressurePlateDown(String textureKey) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:block/pressure_plate_down");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }
}
