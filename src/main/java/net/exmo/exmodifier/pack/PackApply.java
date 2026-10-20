package net.exmo.exmodifier.pack;

import net.exmo.exmodifier.core.registry.ExModifierCatalog;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.List;

public final class PackApply {
    private PackApply() {}

    public static PackResult apply(MinecraftServer server, String name, ServerPlayer notify) {
        PackStore store = PackLiveIndex.store(server);
        List<String> names = name == null || name.isBlank() ? store.packs() : List.of(name);
        if (names.isEmpty()) return PackResult.fail("没有可应用的包");
        for (String pack : names) {
            if (!store.packs().contains(pack)) return PackResult.fail("包不存在: " + pack);
        }
        PackRepository repo = server.getPackRepository();
        repo.reload();
        List<String> selected = new ArrayList<>(repo.getSelectedIds());
        for (String pack : names) {
            String found = find(repo, pack);
            if (found == null) return PackResult.fail("世界没有识别数据包 " + PackIds.folder(pack));
            if (!selected.contains(found)) selected.add(found);
        }
        try {
            server.reloadResources(selected).whenCompleteAsync((done, error) -> {
                if (error != null) {
                    Component message = Component.literal("重载失败: " + error.getMessage());
                    if (notify != null) notify.sendSystemMessage(message);
                    return;
                }
                ExModifierCatalog.bind(server.registryAccess());
                PackNetwork.syncOpen(server, false, "已应用数据包");
                if (notify != null) notify.sendSystemMessage(Component.translatable("command.exmodifier.pack.applied"));
            }, server);
        } catch (RuntimeException ex) {
            return PackResult.fail("无法重载: " + ex.getMessage());
        }
        return PackResult.ok("正在重载数据包");
    }

    private static String find(PackRepository repo, String name) {
        String folder = PackIds.folder(name);
        for (String id : repo.getAvailableIds()) {
            if (id.equals("file/" + folder) || id.endsWith("/" + folder) || id.endsWith(folder)) return id;
        }
        return null;
    }
}
