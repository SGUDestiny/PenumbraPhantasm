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

public class BlocksetJsonIO {
    public static final Gson PRETTY_PRINTING = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public BlocksetJsonIO() {
    }

    public static void writeIfAbsent(Path root, String relativePath, JsonElement json) throws IOException {
        Path path = root.resolve(relativePath);

        if (Files.exists(path)) return;

        writeJson(path, json);
    }

    public static void writeJson(Path path, JsonElement json) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Files.writeString(path, PRETTY_PRINTING.toJson(json), StandardCharsets.UTF_8);
    }

    public static void mergeTag(Path root, String relativePath, Iterable<String> idsToAdd) throws IOException {
        Path path = root.resolve(relativePath);
        JsonObject json;

        if (Files.exists(path)) {
            json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
        } else {
            json = new JsonObject();
        }

        json.addProperty("replace", false);
        JsonArray values = json.has("values") && json.get("values").isJsonArray() ? json.getAsJsonArray("values").deepCopy() : new JsonArray();

        for (String id : idsToAdd) {
            if (!valuesContainEntry(values, id)) {
                values.add(id);
            }
        }

        json.add("values", values);
        writeJson(path, json);
    }

    public static void mergeTagBlockAndItem(Path root, String blockTagRelative, Iterable<String> ids) throws IOException {
        mergeTag(root, blockTagRelative, ids);

        if (blockTagRelative.contains("/mineable/")) return;

        String itemRelative = blockTagRelative.replace("tags/blocks/", "tags/items/");

        if (itemRelative.equals(blockTagRelative)) return;

        mergeTag(root, itemRelative, ids);
    }

    public static boolean valuesContainEntry(JsonArray values, String id) {
        for (JsonElement jsonElement : values) {
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsString().equals(id)) return true;

            if (jsonElement.isJsonObject()) {
                JsonObject json = jsonElement.getAsJsonObject();

                if (json.has("id") && json.get("id").isJsonPrimitive() && id.equals(json.get("id").getAsString())) return true;
            }
        }

        return false;
    }
}