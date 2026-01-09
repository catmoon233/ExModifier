package net.exmo.exmodifier.content.modifier;

import net.exmo.exmodifier.content.refine.RefineClientHandle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static net.exmo.exmodifier.content.refine.RefineHandle.getRefineItem;

@Mod.EventBusSubscriber
public class WashMaterialsItemHandle {
    public static boolean show = false;
    @SubscribeEvent
    public static void TooltipRender(ItemTooltipEvent event){
        final var item = event.getItemStack().getItem();
        final var key = ForgeRegistries.ITEMS.getKey(item);
        var washing = ModifierHandle.materialsList.stream().filter(washingMaterials -> washingMaterials.ItemId.equals(key.toString())).findFirst().orElse(null);
        if (washing != null){
            if (Screen.hasShiftDown()){
                event.getToolTip().addAll(washing.getTooltip());
                show = true;
            }else {

                    event.getToolTip().add(Component.empty());

                event.getToolTip().add(Component.translatable("exmodifier.washing_materials.description.tip"));
            }
        }else {
            show=false ;
        }
    }

}
