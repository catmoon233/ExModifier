package net.exmo.exmodifier.content.slot;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.helper.ModifierSlotHelper;
import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.events.ExRegisterSlotEvent;
import net.exmo.exmodifier.events.ExRegisterUnLockSlotEvent;
import net.exmo.exmodifier.util.StringToIntConverter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.generateEntryTooltip;
@Mod.EventBusSubscriber
public class ModifierSlotHandle
{
    public static Map<ResourceLocation, ModifierSlot> registerSlots = new HashMap<>() ;
    public static List<UnLockSlotItem> unLockSlotItems = new ArrayList<>();


    public static void registerUnLockSlotItem(UnLockSlotItem unLockSlotItem)
    {
        unLockSlotItems.add(unLockSlotItem);
        BaseItemSelected.IDS.put( StringToIntConverter.stringToInt(unLockSlotItem.item.toString()),unLockSlotItem);
        Exmodifier.LOGGER.debug("Registry UnLockSlotItem: " + unLockSlotItem.item.getDescriptionId());
    }
    public static void registerSlot(ResourceLocation id, ModifierSlot modifierSlot)
    {
        int id1 = StringToIntConverter.stringToInt(id.toString());
        modifierSlot.setId(id1);
        registerSlots.put(id, modifierSlot);
        BaseItemSelected.IDS.put(id1,modifierSlot);

        Exmodifier.LOGGER.debug("Registry ModifierSlot: " + id);
    }
    public static ModifierSlot getSlot(ResourceLocation id)
    {
        return registerSlots.get(id);
    }
    public static ResourceLocation getKey(ModifierSlot modifierSlot)
    {
        if (modifierSlot==null)return null;
        for (Map.Entry<ResourceLocation, ModifierSlot> entry : registerSlots.entrySet())
        {
            if (entry.getValue().getId() == modifierSlot.getId())
            {
                return entry.getKey();
            }
        }
        return null;
    }
    public static List<Component> getTooltip(ItemStack stack, Player player){
        var slotHelper = ModifierSlotHelper.of(stack);
        var modifierHelper = ModifierEntryHelper.of(stack);
        var moList = modifierHelper.getModifierEntries();
        List<String> slots = slotHelper.getSlotListId();

        if (slotHelper.validList()){
            List<Component> tooltip = new ArrayList<>();
            for (int i = 0; i < moList.size(); i++) {
            var entry = moList.get(i);
                if (entry.getSlot().isPresent()){
                    var id =entry.getSlot().get();
                    slots.remove(id);
                    if (slotHelper.containSlot(id)){
                        tooltip.add(Component.translatable("modifier.slot."+id));
                        tooltip.addAll(generateEntryTooltip(entry, player, stack));
                  tooltip.add(Component.translatable("null"));
                    }
                }
            }
            for (int i = 0; i < slots.size(); i++) {
                var slot = slots.get(i);
                tooltip.add(Component.translatable("modifier.slot." + slot));
                tooltip.add(Component.translatable("modifier.slot.null"));
                tooltip.add(Component.translatable("null"));

            }
            tooltip.remove(tooltip.size()-1);

            return tooltip;
        }
        return new ArrayList<>();
    }

    public static void reload() {

        ExampleSlotRegister();
        ExRegisterUnLockSlotEvent event = new ExRegisterUnLockSlotEvent();
        ExampleUnLockItemRegister(event);
        MinecraftForge.EVENT_BUS.post(new ExRegisterSlotEvent());
        MinecraftForge.EVENT_BUS.post(event);

    }

    @SubscribeEvent
    public static void registerSlot(ExRegisterSlotEvent event) {

    }
    @SubscribeEvent
    public static void registerSlot(ExRegisterUnLockSlotEvent event) {

    }
    private static void ExampleSlotRegister() {
        registerSlot(new ResourceLocation("exmodifier", "front"), new ModifierSlot());
        registerSlot(new ResourceLocation("exmodifier", "centre"), new ModifierSlot());
        registerSlot(new ResourceLocation("exmodifier", "after"), new ModifierSlot());
    }



    private static void ExampleUnLockItemRegister(ExRegisterUnLockSlotEvent event) {
        event.registerUnLockSlot(new UnLockSlotItem(Items.ZOMBIE_HEAD, 0).setCostExp(3).setNeedCount(2).setSlots(List.of("exmodifier:front")));
        event.registerUnLockSlot(new UnLockSlotItem(Items.SKELETON_SKULL, 0).setCostExp(3).setNeedCount(2).setSlots(List.of("exmodifier:centre")));
        event.registerUnLockSlot(new UnLockSlotItem(Items.WITHER_SKELETON_SKULL, 0).setCostExp(3).setNeedCount(2).setSlots(List.of("exmodifier:after")));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

    }
}
