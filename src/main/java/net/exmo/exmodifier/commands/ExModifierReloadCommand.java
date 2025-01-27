package net.exmo.exmodifier.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.ModifierPreparableReloadListener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;


import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber
public class ExModifierReloadCommand {

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("exmodifierreload")
                        .requires(s -> s.hasPermission(4))
                        .executes(ExModifierReloadCommand::reloadModifiers)
        );
    }

    private static int reloadModifiers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            MainEvent.CommonEvent.init(null);
        }
        catch (IOException e){
            Exmodifier.LOGGER.Logger.error("ExModifierReloadError:",e);
        }
//        CommandSourceStack source = context.getSource();
//        MinecraftServer server = source.getServer();
//
//        if (server == null) {
//            SimpleCommandExceptionType simpleCommandExceptionType = new SimpleCommandExceptionType(Component.literal("Command can only be executed on a server"));
//            throw new CommandSyntaxException(simpleCommandExceptionType,simpleCommandExceptionType.create().getRawMessage());
//        }
//
//        // 注册特定的资源重载监听器
//        MinecraftForge.EVENT_BUS.register(new ModifierPreparableReloadListener());
//
//        MinecraftServer minecraftserver = source.getServer();
//        PackRepository packrepository = minecraftserver.getPackRepository();
//        WorldData worlddata = minecraftserver.getWorldData();
//        Collection<String> collection = packrepository.getSelectedIds();
//        Collection<String> collection1 = discoverNewPacks(packrepository, worlddata, collection);
//        // 触发资源重载，并等待完成
//        CompletableFuture<Void> future = server.reloadResources(collection1); // 只重载data包
//
//        future.thenRun(() -> {
//            // 在资源重载后发送更新到客户端
//            sendUpdatedModifiersToClients(server);
//
//            source.sendSuccess(() -> {
//                return Component.translatable("commands.exmodifierreload.success");
//            }, true);
//        }).exceptionally((throwable) -> {
//            source.sendFailure(Component.translatable("commands.exmodifierreload.failure"));
//            Exmodifier.LOGGER.Logger.error("ExModifierReloadError: {}", throwable.getMessage());
//            return null;
//        });

        return Command.SINGLE_SUCCESS;
    }
    private static Collection<String> discoverNewPacks(PackRepository p_138223_, WorldData p_138224_, Collection<String> p_138225_) {
        p_138223_.reload();
        Collection<String> collection = Lists.newArrayList(p_138225_);
        Collection<String> collection1 = p_138224_.getDataConfiguration().dataPacks().getDisabled();

        for(String s : p_138223_.getAvailableIds()) {
            if (!collection1.contains(s) && !collection.contains(s)) {
                collection.add(s);
            }
        }

        return collection;
    }
    public static void sendUpdatedModifiersToClients(MinecraftServer server) {
        if (server != null && server.getPlayerList() != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                for (ModifierEntry modifierEntry : ModifierHandle.modifierEntryMap.values()) {
                    ModifierHandle.sendModifierEntryToClient(modifierEntry, player);
                }
            }
        }
    }
}