package net.exmo.exmodifier.content.refine;

import net.minecraft.resources.ResourceLocation;


import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RefineHandle {

    public static Map<ResourceLocation, RefineItemRecord> refineItemRecords = new java.util.HashMap<>();

    public static List<MoConfig> FoundRefineItemConfigs = new ArrayList<>();
    public static final Path RefineItemConfigPath = FMLPaths.CONFIGDIR.get().resolve("exmo/refine_items/");

    public static void registryRefineItem(RefineItemRecord refineItemRecord) {
        refineItemRecords.put(refineItemRecord.getItem(), refineItemRecord);
        Exmodifier.LOGGER.info("Registry RefineItem: " + refineItemRecord.getItem());
    }

    public static RefineItemRecord getRefineItem(ResourceLocation id) {
        return refineItemRecords.get(id);
    }

    public static RefineItemRecord getRefineItem(String id) {
        return refineItemRecords.get(new ResourceLocation(id));
    }

    public static Map<ResourceLocation, RefineItemRecord> getRefineItems() {
        return refineItemRecords;
    }

    public static void init() throws IOException {
        if (Files.exists(RefineItemConfigPath)) {
            long startTime = System.nanoTime();

            FoundRefineItemConfigs = ExConfigHandle.listFiles(RefineItemConfigPath);
            for (MoConfig moconfig : FoundRefineItemConfigs) {
                processMoConfigEntries(moconfig);
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            Exmodifier.LOGGER.debug("Read RefineItem Config Over time: " + duration / 1000000 + " ms");
        }
    }

    public static void processMoConfigEntries(MoConfig moconfig) throws FileNotFoundException {
        if (moconfig.readEntrys().isEmpty()) {
            Exmodifier.LOGGER.info("No Refine Item Config Found");
            return;
        }

        // 假设 RefineItemRecord 有类似的序列化方法 EX_SERIALIZE
        var refineItems = RefineItemRecord.SERIALIZE.fromJson(moconfig.jsonObject);
        refineItems.forEach(RefineHandle::registryRefineItem);
    }
}
