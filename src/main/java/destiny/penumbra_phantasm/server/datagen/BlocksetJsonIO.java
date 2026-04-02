package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BlocksetJsonIO {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private BlocksetJsonIO() {
    }

    public static void writeIfAbsent(Path root, String relative, JsonElement json) throws IOException {
        Path path = root.resolve(relative);
        if (Files.exists(path)) {
            return;
        }
        writeJson(path, json);
    }

    public static void writeJson(Path path, JsonElement json) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, PRETTY.toJson(json), StandardCharsets.UTF_8);
    }

    public static void mergeTag(Path root, String relative, Iterable<String> ids) throws IOException {
        Path path = root.resolve(relative);
        JsonObject obj;
        if (Files.exists(path)) {
            obj = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
        } else {
            obj = new JsonObject();
        }
        obj.addProperty("replace", false);
        JsonArray values = obj.has("values") && obj.get("values").isJsonArray()
                ? obj.getAsJsonArray("values").deepCopy()
                : new JsonArray();
        for (String id : ids) {
            if (!valuesContainsEntry(values, id)) {
                values.add(id);
            }
        }
        obj.add("values", values);
        writeJson(path, obj);
    }

    public static void mergeTagBlockAndItem(Path root, String blockTagRelative, Iterable<String> ids) throws IOException {
        mergeTag(root, blockTagRelative, ids);
        if (blockTagRelative.contains("/mineable/")) {
            return;
        }
        String itemRel = blockTagRelative.replace("tags/blocks/", "tags/items/");
        if (itemRel.equals(blockTagRelative)) {
            return;
        }
        mergeTag(root, itemRel, ids);
    }

    private static boolean valuesContainsEntry(JsonArray values, String id) {
        for (JsonElement e : values) {
            if (e.isJsonPrimitive() && e.getAsString().equals(id)) {
                return true;
            }
            if (e.isJsonObject()) {
                JsonObject o = e.getAsJsonObject();
                if (o.has("id") && o.get("id").isJsonPrimitive() && id.equals(o.get("id").getAsString())) {
                    return true;
                }
            }
        }
        return false;
    }
}