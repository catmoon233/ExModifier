package net.exmo.exmodifier.content.element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.exmo.exmodifier.content.modifier.ModifierEntryDataBuilder;
import net.exmo.exmodifier.util.ItemSelector;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
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

public abstract class DefaultItemElementDataProvider implements DataProvider {
    private String MODID = "exmodifier";

    private final DataGenerator generator;
    private final ExistingFileHelper existingFileHelper;

    public DefaultItemElementDataProvider(DataGenerator generator, ExistingFileHelper existingFileHelper, String modid) {
        this.generator = generator;
        this.MODID = modid;
        this.existingFileHelper = existingFileHelper;
        path = Path.of(PackType.SERVER_DATA.getDirectory() + "/" + MODID + "/default_elements");
    }

    public abstract void add(Map<String, ModifierEntryDataBuilder> map);

    private final Map<String, ModifierEntryDataBuilder> map = new HashMap<>();

    public CompletableFuture<?> read(CachedOutput cache) {
        Path folder = generator.getPackOutput().getOutputFolder().resolve(path);
        List<CompletableFuture<?>> list = new ArrayList<>();
        Map<ItemSelector, DefaultItemElement> entries = new HashMap<>();
        try {
            Files.walk(folder)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        list.add(CompletableFuture.runAsync(() -> {
                            try {
                                String content = new String(Files.readAllBytes(path));
                                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                                for (var read : DefaultItemElement.EX_SERIALIZE.fromJson(jsonObject)){
                                    if (read.getItemSelector() !=null) entries.put(read.itemSelector(),read);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
                    });
            ExElementHandle.elementDefaultMap.putAll(entries);
        } catch (IOException e) {
            e.printStackTrace();
        }
        read(cache);
        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }

    public Path path;

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {

        Path folder = this.generator.getPackOutput().getOutputFolder().resolve(path);

        add(map);
        List<CompletableFuture<?>> list = new ArrayList<>();
        map.forEach((k, v) -> {

            JsonElement elem = v.toJson();
            if (elem != null) {
                Path path = folder.resolve(k + ".json");
                list.add(DataProvider.saveStable(cache, elem, path));
            }
        });
        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));

    }

    @Override
    public String getName() {
        return "Default Item Element Entry Data Provider";
    }

    public abstract void gatherData(GatherDataEvent event);
//        DataGenerator generator = event.getGenerator();
//        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
//        generator.addProvider(true,new ModifierEntryDataProvider(generator, existingFileHelper));

}