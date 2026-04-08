package net.exmo.exmodifier.util.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.modifier.MoConfig;
import net.exmo.exmodifier.util.ExConfigHandle;
import net.exmo.exmodifier.util.ExRegistryHelper;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * Handle统一基类 - 封装配置加载、注册、查找的通用流程。
 * 各子系统Handle只需继承此类并提供反序列化逻辑即可。
 *
 * @param <K> 注册表key类型（String 或 ResourceLocation）
 * @param <V> 注册值类型
 */
public abstract class ExDataModule<K, V> {
    protected final Map<K, V> registry = new LinkedHashMap<>();
    protected final List<MoConfig> foundConfigs = new ArrayList<>();
    private final String moduleName;

    protected ExDataModule(String moduleName) {
        this.moduleName = moduleName;
    }

    // region 子类必须实现

    /** 配置文件目录路径 */
    protected abstract Path getConfigPath();

    /** 从 JSON entry 解析单个对象。entryKey = JSON对象的key, json = 该key的值 */
    protected abstract void processEntry(String entryKey, JsonObject json, MoConfig moConfig);

    // endregion

    // region 子类可选覆写

    /** 在init之前调用, 用于清理旧数据 */
    protected void onPreInit() {
        registry.clear();
    }

    /** 在所有配置读取完毕后调用 */
    protected void onPostInit() {
    }

    // endregion

    // region 注册方法

    public void register(K id, V value) {
        if (id != null && value != null) {
            registry.put(id, value);
            Exmodifier.LOGGER.debug("Register " + moduleName + ": " + id);
        }
    }

    public V get(K id) {
        return registry.get(id);
    }

    public Map<K, V> getAll() {
        return Collections.unmodifiableMap(registry);
    }

    public int size() {
        return registry.size();
    }

    public void clear() {
        registry.clear();
        foundConfigs.clear();
    }

    // endregion

    // region 初始化流程

    public void load() throws IOException {
        long startTime = System.nanoTime();
        onPreInit();

        Path configPath = getConfigPath();
        if (configPath != null) {
            foundConfigs.clear();
            foundConfigs.addAll(ExRegistryHelper.listConfigs(configPath));
            for (MoConfig moConfig : foundConfigs) {
                processMoConfig(moConfig);
            }
        }

        onPostInit();

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        Exmodifier.LOGGER.debug("ReadConfig " + moduleName + " Over time: " + duration + " ms (entries: " + registry.size() + ")");
    }

    /** 重新加载（先清理再初始化） */
    public void reload() throws IOException {
        clear();
        load();
    }

    // endregion

    // region 内部处理

    public void processMoConfig(MoConfig moConfig) {
        try {
            Set<Map.Entry<String, JsonElement>> entries = moConfig.readEntrys();
            if (entries == null || entries.isEmpty()) {
                Exmodifier.LOGGER.info("No " + moduleName + " Config Found: " + moConfig.configFile);
                return;
            }
            for (Map.Entry<String, JsonElement> entry : entries) {
                if (entry.getValue().isJsonObject()) {
                    try {
                        processEntry(entry.getKey(), entry.getValue().getAsJsonObject(), moConfig);
                    } catch (Exception e) {
                        Exmodifier.LOGGER.Logger.error("Error processing {} entry: {}", moduleName, entry.getKey(), e);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Exmodifier.LOGGER.Logger.error("Config file not found for {}: {}", moduleName, moConfig.configFile, e);
        }
    }

    // endregion

    // region 便利方法

    /** 快速获取（String key版本） */
    @SuppressWarnings("unchecked")
    public V getByString(String id) {
        if (id == null || id.isBlank()) return null;
        // 尝试直接作为 K 类型获取
        try {
            return registry.get((K) id);
        } catch (ClassCastException ignored) {
        }
        // 尝试作为 ResourceLocation
        try {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            return registry.get((K) rl);
        } catch (ClassCastException ignored) {
        }
        return null;
    }

    public String getModuleName() {
        return moduleName;
    }

    public List<MoConfig> getFoundConfigs() {
        return Collections.unmodifiableList(foundConfigs);
    }

    // endregion
}
