package net.exmo.exmodifier.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class PackStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int MAX_JSON = 262144;

    private final Path root;

    public PackStore(Path datapacksRoot) {
        this.root = datapacksRoot;
    }

    public List<String> packs() {
        if (root == null || !Files.isDirectory(root)) return List.of();
        try (Stream<Path> stream = Files.list(root)) {
            return stream.filter(Files::isDirectory)
                    .map(path -> PackIds.nameOfFolder(path.getFileName().toString()))
                    .filter(name -> name != null && Files.isRegularFile(folder(name).resolve("exmodifier-pack.json")))
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            return List.of();
        }
    }

    public PackData load(String name) {
        if (!PackIds.validName(name) || !Files.isDirectory(folder(name))) {
            return new PackData(name, name, List.of());
        }
        String namespace = readNamespace(name);
        List<PackDocument> documents = new ArrayList<>();
        Path data = folder(name).resolve("data");
        if (Files.isDirectory(data)) {
            try (Stream<Path> namespaces = Files.list(data)) {
                for (Path namespaceDir : namespaces.filter(Files::isDirectory).toList()) {
                    Path base = namespaceDir.resolve("exmodifier");
                    if (!Files.isDirectory(base)) continue;
                    for (PackKind kind : PackKind.values()) {
                        Path kindDir = base.resolve(kind.folder());
                        if (!Files.isDirectory(kindDir)) continue;
                        try (Stream<Path> files = Files.walk(kindDir)) {
                            for (Path file : files.filter(path -> path.getFileName().toString().endsWith(".json")).toList()) {
                                ResourceLocation id = idFor(namespaceDir.getFileName().toString(), kindDir, file);
                                if (id == null) continue;
                                documents.add(new PackDocument(kind, id, readJson(file)));
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
        documents.sort(Comparator.comparing((PackDocument doc) -> doc.kind().folder()).thenComparing(doc -> doc.id().toString()));
        return new PackData(name, namespace, List.copyOf(documents));
    }

    public PackResult create(String name, String namespace) {
        if (!PackIds.validName(name)) return PackResult.fail("包名只能是小写字母、数字和下划线");
        String ns = namespace == null || namespace.isBlank() ? name : namespace.trim();
        if (ResourceLocation.tryParse(ns + ":placeholder") == null) return PackResult.fail("命名空间不合法");
        Path folder = folder(name);
        if (Files.exists(folder)) return PackResult.fail("包已存在");
        try {
            Files.createDirectories(folder);
            write(folder.resolve("pack.mcmeta"), """
                    {
                      "pack": {
                        "pack_format": 48,
                        "description": "ExModifier pack %s"
                      }
                    }
                    """.formatted(name));
            JsonObject meta = new JsonObject();
            meta.addProperty("namespace", ns);
            meta.addProperty("description", name);
            write(folder.resolve("exmodifier-pack.json"), GSON.toJson(meta));
            return PackResult.ok("已创建包 " + name);
        } catch (IOException ex) {
            return PackResult.fail("无法创建包: " + ex.getMessage());
        }
    }

    public PackResult delete(String name) {
        if (!PackIds.validName(name)) return PackResult.fail("包名不合法");
        Path folder = folder(name);
        if (!Files.isDirectory(folder)) return PackResult.fail("包不存在");
        try {
            deleteRecursively(folder);
            return PackResult.ok("已删除包 " + name);
        } catch (IOException ex) {
            return PackResult.fail("无法删除包: " + ex.getMessage());
        }
    }

    public PackResult upsert(String name, PackKind kind, ResourceLocation id, JsonElement json) {
        PackResult ready = requirePack(name, kind, id);
        if (!ready.ok()) return ready;
        if (json == null || !json.isJsonObject()) return PackResult.fail("内容必须是 JSON 对象");
        if (json.toString().length() > MAX_JSON) return PackResult.fail("内容过大");
        Optional<String> invalid = kind.invalid(json);
        if (invalid.isPresent()) return PackResult.fail(invalid.get());
        try {
            Path file = file(name, kind, id);
            Files.createDirectories(file.getParent());
            write(file, GSON.toJson(json));
            return PackResult.ok("已保存 " + kind.folder() + " " + id);
        } catch (IOException ex) {
            return PackResult.fail("无法写入: " + ex.getMessage());
        }
    }

    public PackResult remove(String name, PackKind kind, ResourceLocation id) {
        PackResult ready = requirePack(name, kind, id);
        if (!ready.ok()) return ready;
        try {
            Path file = file(name, kind, id);
            if (!Files.isRegularFile(file)) return PackResult.fail("条目不存在");
            Files.delete(file);
            return PackResult.ok("已删除 " + id);
        } catch (IOException ex) {
            return PackResult.fail("无法删除条目: " + ex.getMessage());
        }
    }

    public Optional<JsonElement> read(String name, PackKind kind, ResourceLocation id) {
        if (!PackIds.validName(name) || kind == null || id == null) return Optional.empty();
        Path file = file(name, kind, id);
        if (!Files.isRegularFile(file)) return Optional.empty();
        return Optional.of(readJson(file));
    }

    public Optional<JsonElement> find(PackKind kind, ResourceLocation id) {
        if (kind == null || id == null) return Optional.empty();
        for (String name : packs()) {
            Optional<JsonElement> found = read(name, kind, id);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    public List<String> ids(String name, PackKind kind) {
        if (kind == null) return List.of();
        return load(name).documents().stream()
                .filter(doc -> doc.kind() == kind)
                .map(doc -> doc.id().toString())
                .toList();
    }

    public List<String> idsExcept(String name, PackKind kind) {
        List<String> ids = new ArrayList<>();
        for (String pack : packs()) {
            if (pack.equals(name)) continue;
            ids.addAll(ids(pack, kind));
        }
        return ids;
    }

    public String namespace(String name) {
        return readNamespace(name);
    }

    public Path folder(String name) {
        Path folder = root.resolve(PackIds.folder(name)).normalize();
        if (root != null && !folder.startsWith(root.normalize())) {
            throw new IllegalArgumentException("pack path escaped datapacks");
        }
        return folder;
    }

    private PackResult requirePack(String name, PackKind kind, ResourceLocation id) {
        if (!PackIds.validName(name)) return PackResult.fail("包名不合法");
        if (!Files.isDirectory(folder(name))) return PackResult.fail("包不存在");
        if (kind == null) return PackResult.fail("未知类型");
        if (id == null || !PackIds.safe(id)) return PackResult.fail("ID 不合法");
        return PackResult.ok("");
    }

    private String readNamespace(String name) {
        Path meta = folder(name).resolve("exmodifier-pack.json");
        if (!Files.isRegularFile(meta)) return name;
        try {
            JsonElement element = JsonParser.parseString(Files.readString(meta));
            if (element.isJsonObject() && element.getAsJsonObject().has("namespace")) {
                return element.getAsJsonObject().get("namespace").getAsString();
            }
        } catch (Exception ignored) {
        }
        return name;
    }

    private Path file(String name, PackKind kind, ResourceLocation id) {
        Path dir = folder(name).resolve("data").resolve(id.getNamespace()).resolve("exmodifier").resolve(kind.folder());
        String[] parts = id.getPath().split("/");
        for (int i = 0; i < parts.length - 1; i++) dir = dir.resolve(parts[i]);
        return dir.resolve(parts[parts.length - 1] + ".json");
    }

    private static ResourceLocation idFor(String namespace, Path kindDir, Path file) {
        String path = kindDir.relativize(file).toString().replace('\\', '/');
        if (!path.endsWith(".json")) return null;
        path = path.substring(0, path.length() - 5);
        return PackIds.qualify(namespace + ":" + path, namespace);
    }

    private static JsonElement readJson(Path file) {
        try {
            return JsonParser.parseString(Files.readString(file));
        } catch (Exception ex) {
            JsonObject error = new JsonObject();
            error.addProperty("_error", ex.getMessage() == null ? "unreadable" : ex.getMessage());
            return error;
        }
    }

    private static void write(Path file, String text) throws IOException {
        Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");
        Files.writeString(tmp, text, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
