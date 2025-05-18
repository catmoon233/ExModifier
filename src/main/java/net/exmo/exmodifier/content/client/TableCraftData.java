package net.exmo.exmodifier.content.client;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.exmo.exmodifier.network.RefreshCraftContentMessage;
import net.exmo.exmodifier.util.ExClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class TableCraftData {
    public static Map<Integer,ItemStack> itemStackSet = new HashMap<>();

    public static boolean changeIndex = false;
    public static ServerPlayer _player;
    @SubscribeEvent
    public static void renderTableCraft(RenderTooltipEvent event){
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack itemStack = event.getItemStack();
        Object currentGuiAs = ExClientUtils.getCurrentGuiAs(CraftingScreen.class);

    }
    @SubscribeEvent
    public static void selectedInheritItemStack(ItemTooltipEvent event){
        if (!Screen.hasControlDown())return;
        Object currentGuiAs = ExClientUtils.getCurrentGuiAs(CraftingScreen.class);
        if (currentGuiAs instanceof CraftingScreen craftingScreen){
            NonNullList<ItemStack> items = craftingScreen.getMenu().getItems();
            for(int i = 0; i < items.size(); ++i) {
                ItemStack nowItem = items.get(i);
                if (!nowItem.isEmpty() && ItemStack.isSameItemSameTags(event.getItemStack(), nowItem)) {
                    //                    event.getEntity().getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY).ifPresent(e ->{
//                        e.craftIndex = finalI;
//                        e.syncPlayerVariables(event.getEntity());
//                    });
                    Exmodifier.PACKET_HANDLER.sendToServer(new RefreshCraftContentMessage(i));
                }
            }


        }
    }
}
