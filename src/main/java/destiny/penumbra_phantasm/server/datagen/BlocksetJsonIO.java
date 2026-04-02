package destiny.penumbra_phantasm.server.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

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
}