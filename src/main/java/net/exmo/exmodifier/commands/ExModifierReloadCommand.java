package net.exmo.exmodifier.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.event.MainEvent;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntry;

public class ExModifierReloadCommand {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("exmodifierreload").requires(s -> s.hasPermission(4)).executes(arguments -> {
            try {
                MainEvent.CommonEvent.init();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return 0;

        }));
    }
}