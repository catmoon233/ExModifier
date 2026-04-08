package net.exmo.exmodifier.util.module;

import net.exmo.exmodifier.Exmodifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 统一模块注册器 - 管理所有ExDataModule实例的初始化和重载。
 */
public final class ExModuleRegistry {
    private static final List<ExDataModule<?, ?>> modules = new ArrayList<>();

    private ExModuleRegistry() {}

    public static void registerModule(ExDataModule<?, ?> module) {
        modules.add(module);
        Exmodifier.LOGGER.debug("Module registered: " + module.getModuleName());
    }

    public static void initAll() throws IOException {
        long start = System.nanoTime();
        for (ExDataModule<?, ?> module : modules) {
            try {
                module.load();
            } catch (IOException e) {
                Exmodifier.LOGGER.Logger.error("Failed to init module: " + module.getModuleName(), e);
                throw e;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        Exmodifier.LOGGER.debug("All " + modules.size() + " modules initialized in " + ms + " ms");
    }

    public static void reloadAll() throws IOException {
        long start = System.nanoTime();
        for (ExDataModule<?, ?> module : modules) {
            try {
                module.reload();
            } catch (IOException e) {
                Exmodifier.LOGGER.Logger.error("Failed to reload module: " + module.getModuleName(), e);
                throw e;
            }
        }
        long ms2 = (System.nanoTime() - start) / 1_000_000;
        Exmodifier.LOGGER.debug("All " + modules.size() + " modules reloaded in " + ms2 + " ms");
    }

    public static List<ExDataModule<?, ?>> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public static void clear() {
        modules.clear();
    }
}
