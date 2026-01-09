package net.exmo.exmodifier.content.refine;

import net.exmo.exmodifier.content.modifier.WashMaterialsItemHandle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static net.exmo.exmodifier.content.refine.RefineHandle.getRefineItem;

@Mod.EventBusSubscriber
public class RefineClientHandle {

    @SubscribeEvent
    public static void TooltipRender(ItemTooltipEvent event){
        final var item = event.getItemStack().getItem();
        final var key = ForgeRegistries.ITEMS.getKey(item);
        var refineItem = getRefineItem(key);
        if (refineItem != null){
            if (Screen.hasShiftDown()){
                event.getToolTip().addAll(refineItem.getDescriptionComponents());

            }else {

                if (!WashMaterialsItemHandle.show) {
                    event.getToolTip().add(Component.empty());
                }
                event.getToolTip().add(Component.translatable("exmodifier.refine.description.tip"));
            }
        }else {

        }
    }

}
