package net.exmo.exmodifier.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.exmo.exmodifier.Exmodifier;
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
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntry;

@Mod.EventBusSubscriber
public class AddHandItemEntry {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("addhanditementry").requires(s -> s.hasPermission(4)).then(Commands.argument("name1", EntityArgument.player()).then(Commands.argument("name", StringArgumentType.word()).executes(arguments -> {
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
            String _setval = StringArgumentType.getString(arguments, "name");
            Player player = EntityArgument.getPlayer(arguments, "name1");
            try {


                ItemStack itemStack = player.getMainHandItem();

                List<ModifierEntry> modifierEntries = ModifierHandle.getEntrysFromItemStack(itemStack);

                ModifierHandle.CommonEvent.clearEntry(itemStack);

                ModifierEntry modifier = ModifierHandle.modifierEntryMap.get(_setval);
                if (!modifierEntries.contains(modifier)) modifierEntries.add(modifier);

                Map<String, Float> weightedUtilmap = new HashMap<>();
                for (ModifierEntry modifierEntry : modifierEntries) {
                    Exmodifier.LOGGER.info(modifierEntry.getId());
                    weightedUtilmap.put(modifierEntry.getId(), 1.0f);
                }
                ModifierEntry.Type type = ModifierEntry.findTypeFormEntry(modifier);
                EquipmentSlot slot = ModifierEntry.TypeToEquipmentSlot(type);
                RandomEntry(itemStack, new WeightedUtil<String>(weightedUtilmap), slot, modifierEntries.size());

            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }))));
    }
}
