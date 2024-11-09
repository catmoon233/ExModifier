package net.exmo.exmodifier.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ItemQualityHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.ModifierInstant;
import net.exmo.exmodifier.content.quality.ItemQuality;
import net.exmo.exmodifier.content.quality.ItemQualityHandle;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
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


    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("addHandItemEntry").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("entryid", StringArgumentType.word()).suggests(Suggestion_Entries).then(Commands.argument("level", IntegerArgumentType.integer(1)).executes(arguments -> {
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
        event.getDispatcher().register(Commands.literal("addHandItemQuality").requires(s -> s.hasPermission(4)).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("QualityId", StringArgumentType.word()).suggests(Suggestion_Qualities).executes(arguments -> {
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

}
