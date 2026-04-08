package net.exmo.exmodifier.content.refine;

import com.google.gson.JsonObject;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.util.ExRegistryHelper;
import net.exmo.exmodifier.util.module.ExDataModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Map;

public class RefineHandle extends ExDataModule<ResourceLocation, RefineItemRecord> {

    public static final RefineHandle INSTANCE = new RefineHandle();

    /** @deprecated 使用 INSTANCE.getAll() */
    @Deprecated public static final Map<ResourceLocation, RefineItemRecord> refineItemRecords = INSTANCE.registry;

    private RefineHandle() {
        super("RefineItem");
    }

    @Override
    protected Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("exmo/refine_items/");
    }

    @Override
    protected void processEntry(String entryKey, JsonObject json, MoConfig moConfig) {
        var refineItems = RefineItemRecord.SERIALIZE.fromJson(moConfig.jsonObject);
        refineItems.forEach(r -> register(r.getItem(), r));
    }

    @Override
    public void processMoConfig(MoConfig moConfig) {
        try {
            if (moConfig.readEntrys().isEmpty()) return;
            var refineItems = RefineItemRecord.SERIALIZE.fromJson(moConfig.jsonObject);
            refineItems.forEach(r -> register(r.getItem(), r));
        } catch (Exception e) {
            // 使用父类逻辑的日志回退
            super.processMoConfig(moConfig);
        }
    }

    // region 兼容旧API的静态方法

    public static void registerRefineItem(RefineItemRecord refineItemRecord) {
        INSTANCE.register(refineItemRecord.getItem(), refineItemRecord);
    }

    @Deprecated(forRemoval = false)
    public static void registryRefineItem(RefineItemRecord refineItemRecord) {
        registerRefineItem(refineItemRecord);
    }

    public static RefineItemRecord getRefineItem(ResourceLocation id) {
        return INSTANCE.get(id);
    }

    public static RefineItemRecord getRefineItem(String id) {
        return ExRegistryHelper.get(INSTANCE.registry, id);
    }

    public static Map<ResourceLocation, RefineItemRecord> getRefineItems() {
        return INSTANCE.getAll();
    }

    /** @deprecated 使用 INSTANCE.load() */
    @Deprecated
    public static void init_legacy() throws java.io.IOException {
        INSTANCE.load();
    }

    public static void init() throws java.io.IOException {
        INSTANCE.load();
    }

    // endregion
}
