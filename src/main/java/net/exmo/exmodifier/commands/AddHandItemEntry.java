package net.exmo.exmodifier.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.element.ExElementHandle;
import net.exmo.exmodifier.content.element.ExElementInstant;
import net.exmo.exmodifier.content.helper.ExElementHelper;
import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.helper.ModifierSlotHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.ModifierInstant;
import net.exmo.exmodifier.content.quality.ItemQuality;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.exmo.exmodifier.content.quality.ItemQualityHandle.itemQualityMap;


@Mod.EventBusSubscriber
public class AddHandItemEntry {
    public static final SuggestionProvider<CommandSourceStack> Suggestion_Entries = (ctx, builder) -> SharedSuggestionProvider.suggest(ModifierHandle.modifierEntryMap.keySet(), builder);
    public static final SuggestionProvider<CommandSourceStack> Suggestion_Qualities = (ctx, builder) -> SharedSuggestionProvider.suggest(itemQualityMap.keySet(), builder);
    public static final SuggestionProvider<CommandSourceStack> Suggestion_Slots = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(
                ModifierSlotHandle.registerSlots.keySet().stream()
                        .map(resourceLocation -> "\"" + resourceLocation.toString() + "\""),
                builder
        );
    };
    public static final SuggestionProvider<CommandSourceStack> Suggestion_Elements = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(
                ExElementHandle.exElements.keySet().stream()
                        .map(resourceLocation -> "\"" + resourceLocation.toString() + "\""),
                builder
        );
    };




    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("addHandItemEntryS").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("entryid", StringArgumentType.word()).suggests(Suggestion_Entries).then(Commands.argument("level", IntegerArgumentType.integer(1)).then(Commands.argument("slotid", StringArgumentType.string()).suggests(Suggestion_Slots).executes(arguments -> {
            extracted(arguments);
            String _setval = StringArgumentType.getString(arguments, "entryid");
            int level = IntegerArgumentType.getInteger(arguments, "level");
            Player player = EntityArgument.getPlayer(arguments, "player");
            String slotid = StringArgumentType.getString(arguments, "slotid");


            try {
                ModifierEntryHelper mh = ModifierEntryHelper.of(player.getMainHandItem());
            mh.addModifierEntry(ModifierInstant.of(ModifierEntryHelper.getEntry(_setval),level).setSlot(slotid.replace("\"","")),true,true);


            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }))))));
        event.getDispatcher().register(Commands.literal("addHandItemEntry").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("entryid", StringArgumentType.word()).suggests(Suggestion_Entries).then(Commands.argument("level", IntegerArgumentType.integer(1)).executes(arguments -> {
            extracted(arguments);
            String _setval = StringArgumentType.getString(arguments, "entryid");
            int level = IntegerArgumentType.getInteger(arguments, "level");
            Player player = EntityArgument.getPlayer(arguments, "player");

            try {
                ModifierEntryHelper mh = ModifierEntryHelper.of(player.getMainHandItem());
            mh.addModifierEntry(ModifierInstant.of(ModifierEntryHelper.getEntry(_setval),level),true,true);


            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        })))));
        event.getDispatcher().register(Commands.literal("removeHandItemEntry").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("entryid", StringArgumentType.word()).suggests(Suggestion_Entries).then(Commands.argument("level", IntegerArgumentType.integer(1)).executes(arguments -> {
            extracted(arguments);
            String _setval = StringArgumentType.getString(arguments, "entryid");
            int level = IntegerArgumentType.getInteger(arguments, "level");
            Player player = EntityArgument.getPlayer(arguments, "player");

            try {
                ModifierEntryHelper mh = ModifierEntryHelper.of(player.getMainHandItem());
            mh.removeModifierEntryLevel(ModifierInstant.of(ModifierEntryHelper.getEntry(_setval),level),true);


            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        })))));
        event.getDispatcher().register(Commands.literal("setHandItemEntry").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("entryid", StringArgumentType.word()).suggests(Suggestion_Entries).then(Commands.argument("level", IntegerArgumentType.integer(1)).executes(arguments -> {
            extracted(arguments);
            String _setval = StringArgumentType.getString(arguments, "entryid");
            int level = IntegerArgumentType.getInteger(arguments, "level");
            Player player = EntityArgument.getPlayer(arguments, "player");

            try {
                ModifierEntryHelper mh = ModifierEntryHelper.of(player.getMainHandItem());
            mh.setModifierEntryLevel(_setval, level);


            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        })))));
        event.getDispatcher().register(Commands.literal("addHandItemSlot").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("slotid", StringArgumentType.string()).suggests(Suggestion_Slots).executes(arguments -> {
            extracted(arguments);
            String _setval = StringArgumentType.getString(arguments, "slotid").replace("\"","");
            Player player = EntityArgument.getPlayer(arguments, "player");

            try {
                ModifierSlotHelper mh = ModifierSlotHelper.of(player.getMainHandItem());
            mh.addSlot(ModifierSlotHandle.getSlot(ResourceLocation.tryParse(_setval)));


            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }))));
        event.getDispatcher().register(Commands.literal("addHandElement").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("elementId", StringArgumentType.string()).suggests(Suggestion_Elements).then(Commands.argument("level",IntegerArgumentType.integer(1)).executes(arguments -> {
            extracted(arguments);
            String _setval = StringArgumentType.getString(arguments, "elementId").replace("\"","");
            Player player = EntityArgument.getPlayer(arguments, "player");
            int level = IntegerArgumentType.getInteger(arguments, "level");

            try {
                ExElementHelper mh = ExElementHelper.of(player.getMainHandItem());
                mh.addExElement(ExElementInstant.of(ExElementHandle.getExElement(_setval),level),true);


            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        })))));
        event.getDispatcher().register(Commands.literal("addHandItemQuality").requires(s -> s.hasPermission(4)).
                then(Commands.argument("player", EntityArgument.player()).
                        then(Commands.argument("QualityId", StringArgumentType.word())
                                .suggests(Suggestion_Qualities)
                                .executes(arguments -> {
                                    extracted(arguments);
                                    String _setval = StringArgumentType.getString(arguments, "QualityId");
            Player player = EntityArgument.getPlayer(arguments, "player");
            try {
                ItemQuality itemQuality = itemQualityMap.get(_setval);
                if (itemQuality!=null) ItemQualityHelper.of(player.getMainHandItem()).addQualityEntry(itemQuality,true,true);


            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }))));
    }

    private static void extracted(CommandContext<CommandSourceStack> arguments) {
        Level world = arguments.getSource().getUnsidedLevel();
        double x = arguments.getSource().getPosition().x();
        double y = arguments.getSource().getPosition().y();
        double z = arguments.getSource().getPosition().z();
        Entity entity = arguments.getSource().getEntity();
        if (entity == null && world instanceof ServerLevel _servLevel)
            entity = FakePlayerFactory.getMinecraft(_servLevel);
        Direction direction = Direction.DOWN;
        if (entity != null)
            direction = entity.getDirection();
    }

}
