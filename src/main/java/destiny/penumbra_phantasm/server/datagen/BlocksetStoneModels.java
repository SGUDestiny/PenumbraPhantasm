package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.JsonObject;
import destiny.penumbra_phantasm.PenumbraPhantasm;

public final class BlocksetStoneModels {
    private static final String MOD = PenumbraPhantasm.MODID;

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

    public static JsonObject cubeAll(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_cube_all_emissive" : "minecraft:block/cube_all");
        JsonObject t = new JsonObject();
        t.addProperty("all", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject stairs(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_stairs_emissive" : "minecraft:block/stairs");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject stairsInner(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_inner_stairs_emissive" : "minecraft:block/inner_stairs");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject stairsOuter(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_outer_stairs_emissive" : "minecraft:block/outer_stairs");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject slab(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_slab_emissive" : "minecraft:block/slab");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject slabTop(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_slab_top_emissive" : "minecraft:block/slab_top");
        o.add("textures", threeFace(textureKey));
        return o;
    }

    public static JsonObject wallPost(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_wall_post_emissive" : "minecraft:block/template_wall_post");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject wallSide(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_wall_side_emissive" : "minecraft:block/template_wall_side");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject wallSideTall(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_wall_side_tall_emissive" : "minecraft:block/template_wall_side_tall");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject wallInventory(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_wall_inventory_emissive" : "minecraft:block/wall_inventory");
        JsonObject t = new JsonObject();
        t.addProperty("wall", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject button(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_button_emissive" : "minecraft:block/button");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject buttonInventory(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_button_inventory_emissive" : "minecraft:block/button_inventory");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject buttonPressed(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_button_pressed_emissive" : "minecraft:block/button_pressed");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject pressurePlateUp(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_pressure_plate_up_emissive" : "minecraft:block/pressure_plate_up");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }

    public static JsonObject pressurePlateDown(String textureKey, boolean emissive) {
        JsonObject o = new JsonObject();
        o.addProperty("parent", emissive ? MOD + ":block/template_pressure_plate_down_emissive" : "minecraft:block/pressure_plate_down");
        JsonObject t = new JsonObject();
        t.addProperty("texture", tex(textureKey));
        o.add("textures", t);
        return o;
    }
}