package net.exmo.exmodifier.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.exmo.exmodifier.content.event.MainEvent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.AddEntryToItem;

@Mod.EventBusSubscriber
public class GiveEntryItemStack {

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("giveEntryItem").requires(s -> s.hasPermission(4)).then(Commands.argument("name1", EntityArgument.player()).then(Commands.argument("name", StringArgumentType.word()).executes(arguments -> {
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
                ItemStack itemStack = MainEvent.CommonEvent.stackList().stream().filter(itemStack1 -> itemStack1.getOrCreateTag().getString("modifier_id").equals(_setval)).findFirst().get();
                if (itemStack!=null) {
                    player.addItem(itemStack);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }))));
    }
}
