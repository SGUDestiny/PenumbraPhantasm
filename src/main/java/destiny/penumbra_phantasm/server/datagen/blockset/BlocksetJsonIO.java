package destiny.penumbra_phantasm.server.datagen.blockset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    public static void mergeTag(Path root, String relative, Collection<String> ids) throws IOException {
        Path path = root.resolve(relative);
        JsonObject o;
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                o = JsonParser.parseReader(r).getAsJsonObject();
            }
        } else {
            o = new JsonObject();
            o.addProperty("replace", false);
            o.add("values", new JsonArray());
        }
        if (!o.has("replace")) {
            o.addProperty("replace", false);
        }
        if (!o.has("values")) {
            o.add("values", new JsonArray());
        }
        JsonArray arr = o.getAsJsonArray("values");
        Set<String> have = new LinkedHashSet<>();
        for (JsonElement e : arr) {
            stringFromTagValue(e).ifPresent(have::add);
        }
        for (String id : ids) {
            if (have.contains(id)) {
                continue;
            }
            arr.add(id);
            have.add(id);
        }
        writeJson(path, o);
    }

    private static Optional<String> stringFromTagValue(JsonElement e) {
        if (e.isJsonPrimitive()) {
            return Optional.of(e.getAsString());
        }
        if (e.isJsonObject() && e.getAsJsonObject().has("id")) {
            return Optional.of(e.getAsJsonObject().get("id").getAsString());
        }
        return Optional.empty();
    }

    public static void mergeLang(Path root, String relative, Map<String, String> keys) throws IOException {
        Path path = root.resolve(relative);
        JsonObject o = new JsonObject();
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                o = JsonParser.parseReader(r).getAsJsonObject();
            }
        }
        for (Map.Entry<String, String> e : keys.entrySet()) {
            if (o.has(e.getKey())) {
                continue;
            }
            o.addProperty(e.getKey(), e.getValue());
        }
        writeJson(path, o);
    }
}
