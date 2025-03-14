package net.exmo.exmodifier.content.modifier;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.Exmodifier.MODID;
import static se.mickelus.mutil.data.deserializer.ResourceLocationDeserializer.deserialize;

public abstract class ModifierEntryDataProvider implements DataProvider {
    private String MODID = "exmodifier";

    private final DataGenerator generator;
    private final ExistingFileHelper existingFileHelper;

    public ModifierEntryDataProvider(DataGenerator generator, ExistingFileHelper existingFileHelper,String modid) {
        this.generator = generator;
        this.MODID = modid;
        this.existingFileHelper = existingFileHelper;
        path =  Path.of(PackType.SERVER_DATA.getDirectory() + "/" + MODID + "/modifier_entries");
    }

    public abstract void add(Map<String, ModifierEntryDataBuilder> map);

    private final Map<String, ModifierEntryDataBuilder> map = new HashMap<>();
    public CompletableFuture<?> read(CachedOutput cache) {
        Path folder = generator.getPackOutput().getOutputFolder().resolve(path);
        List<CompletableFuture<?>> list = new ArrayList<>();
        List<ModifierEntry> entries = new ArrayList<>();
        try {
            Files.walk(folder)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        list.add(CompletableFuture.runAsync(() -> {
                            try {
                                String content = new String(Files.readAllBytes(path));
                                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                                ModifierHandle.processModifierEntry(jsonObject.toString(), entries);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
                    });
            for (ModifierEntry entry : entries) {
                ModifierHandle.RegisterModifierEntry(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        read(cache);
        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }
    public  Path   path ;
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
       // Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Path folder = this.generator.getPackOutput().getOutputFolder().resolve( path);
        //Path outputPath = outputFolder.resolve("data/modid/modifier_entries.json");

//        // 示例数据，实际应用中应从配置文件或其他来源读取
//        List<ModifierEntry> entries = List.of(
//                new ModifierEntryDataBuilder()
//                        .setId("example_entry")
//                        .setType(ModifierEntry.Type.SWORD)
//                        .setWeight(1.0f)
//                        .build()
//        );

        // 将 ModifierEntry 列表转换为 JSON 格式
        add(map);
        List<CompletableFuture<?>> list = new ArrayList<>();
        map.forEach((k, v) -> {

//            JsonElement elem = gson.toJsonTree(v, new TypeToken<ModifierEntryDataBuilder>() {}.getType());
            JsonElement elem = v.toJson();
            if (elem != null) {
                Path path = folder.resolve(k + ".json");
                list.add(DataProvider.saveStable(cache, elem, path));
            }
        });
        // 写入文件
        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));

    }

    @Override
    public String getName() {
        return "Modifier Entry Data Provider";
    }

    public abstract void gatherData(GatherDataEvent event) ;
//        DataGenerator generator = event.getGenerator();
//        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
//        generator.addProvider(true,new ModifierEntryDataProvider(generator, existingFileHelper));

}