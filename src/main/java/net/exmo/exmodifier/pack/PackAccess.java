package net.exmo.exmodifier.pack;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PackAccess {
    private PackAccess() {}

    public static boolean allowed(CommandSourceStack source) {
        if (source.hasPermission(2)) return true;
        if (source.getEntity() instanceof ServerPlayer player) return allowed(player);
        MinecraftServer server = source.getServer();
        return server != null && server.isSingleplayer();
    }

    public static boolean allowed(ServerPlayer player) {
        if (player.hasPermissions(2)) return true;
        MinecraftServer server = player.getServer();
        return server != null && server.isSingleplayerOwner(player.getGameProfile());
    }
}
