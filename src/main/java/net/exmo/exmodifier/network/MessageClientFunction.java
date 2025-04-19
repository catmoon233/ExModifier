package net.exmo.exmodifier.network;

import net.exmo.exmodifier.content.modifier.menu.RefreshMenuScreenPlus;
import net.exmo.exmodifier.util.ExClientUtils;
import net.minecraft.world.entity.player.Player;

import static net.exmo.exmodifier.content.modifier.menu.RefreshMenuScreenPlus.filterItems;
import static net.exmo.exmodifier.content.modifier.menu.RefreshMenuScreenPlus.filterItems2;

public class MessageClientFunction {
    public static void ApplyChangeRefreshMenuTextListMessage(int a,int b){
        Object currentGuiAs = ExClientUtils.getCurrentGuiAs(RefreshMenuScreenPlus.class);
        if (currentGuiAs !=null){
            RefreshMenuScreenPlus currentGuiAs1 = (RefreshMenuScreenPlus) currentGuiAs;
            if (!currentGuiAs1.selectedItemStack.isEmpty()) {
                Player player = currentGuiAs1.getPlayer();
                player.containerMenu.slotsChanged(null);
                player.containerMenu.transferState(player.containerMenu);
                player.inventoryMenu.sendAllDataToRemote();
                player.containerMenu.broadcastChanges();
                player.inventoryMenu.broadcastChanges();
                player.getInventory().setChanged();
                currentGuiAs1.selectedRefreshItem = player.getInventory().getItem(a);
                currentGuiAs1.selectedItemStack = player.getInventory().getItem(b);
                currentGuiAs1.init(currentGuiAs1.getMinecraft(), currentGuiAs1.width, currentGuiAs1.height);

//                currentGuiAs1.textList.entries = currentGuiAs1.getTextEntries();
//                currentGuiAs1.textList.calculateLayout();;

//                currentGuiAs1.itemListViewer.items = currentGuiAs1.manageSlot == 0 ? filterItems(player.getInventory().items) : filterItems2(player.getInventory().items);
            };
        }
    }
}
