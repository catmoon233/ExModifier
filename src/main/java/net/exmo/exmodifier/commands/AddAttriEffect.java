package net.exmo.exmodifier.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.exmo.exmodifier.content.attributeEffect.AttriGetherEffect;
import net.exmo.exmodifier.content.attributeEffect.AttriGetherEffectHandle;
import net.exmo.exmodifier.content.attributeEffect.AttriGetherEffectInstance;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class AddAttriEffect {
    public static final SuggestionProvider<CommandSourceStack> Suggestion_Effect = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    AttriGetherEffectHandle.getAttriGetherMap().keySet()
                            .stream()
                            .map(key -> "\"" + key + "\"") // 在每个 key 前后加上双引号
                            .collect(Collectors.toList()), // 将流转换为列表
                    builder
            );



    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("attriEffect").requires(s -> s.hasPermission(4))
                        .then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("effectid", StringArgumentType.string()).suggests(Suggestion_Effect).then(Commands.argument("level", IntegerArgumentType.integer(1)).then(Commands.argument("duration", IntegerArgumentType.integer(1)).then(Commands.argument("replace", BoolArgumentType.bool())
                        .executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            String _setval = StringArgumentType.getString(arguments, "effectid");

            int level = IntegerArgumentType.getInteger(arguments, "level");
            int duration = IntegerArgumentType.getInteger(arguments, "duration");
            Player player = EntityArgument.getPlayer(arguments, "player");
            boolean replace = BoolArgumentType.getBool(arguments, "replace");
            try {
                AttriGetherEffect byId = AttriGetherEffectHandle.getById(new ResourceLocation(_setval));
                AttriGetherEffectInstance attriGetherEffectInstance = new AttriGetherEffectInstance(duration, byId, level, true, true, true);
                if (replace){
                    AttriGetherEffectHandle.addOrReplaceAttriGetherEffect(attriGetherEffectInstance,player);
                }else AttriGetherEffectHandle.addAttriGetherEffect(attriGetherEffectInstance,player);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }))))))));
    }


    }

