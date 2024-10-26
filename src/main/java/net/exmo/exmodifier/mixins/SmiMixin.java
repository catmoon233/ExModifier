package net.exmo.exmodifier.mixins;

import com.google.gson.internal.bind.JsonTreeReader;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.config;
import net.exmo.exmodifier.content.helper.ItemInfo;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.*;
import net.exmo.exmodifier.events.ExRefreshEvent;
import net.exmo.exmodifier.util.CuriosUtil;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntryCurios;

@Mixin(AnvilMenu.class)
public abstract class SmiMixin extends ItemCombinerMenu {
    @Shadow public int repairItemCountCost;

    @Shadow @Final private DataSlot cost;

    @Shadow protected abstract void onTake(Player p_150474_, ItemStack p_150475_);

    //
//
//    @Shadow protected abstract void shrinkStackInSlot(int p_40271_);
//    @Inject(at = @At("HEAD"), method = "canMoveIntoInputSlots", cancellable = true)
//    public void canMoveIntoInputSlots(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
//       // Exmodifier.LOGGER.debug("Checking if item is washing material");
//        if (inputSlots.getItem(2).isEmpty()) {
//
//            for (WashingMaterials washingMaterials : ModifierHandle.materialsList) {
//
//                if (washingMaterials.item.equals(itemStack.getItem())) {
//                    cir.setReturnValue(true);
//
//                    this.inputSlots.setItem(2, itemStack.copy());
//                    itemStack.shrink(itemStack.getCount());
//                    //       Exmodifier.LOGGER.debug("Checking if item is washing material -> true");
//                    return;
//                }
//            }
//        }
//        if (inputSlots.getItem(1).isEmpty()) {
//            this.inputSlots.setItem(1, itemStack.copy());
//            itemStack.shrink(itemStack.getCount());
//            cir.setReturnValue(true);
//        }
//        if (inputSlots.getItem(2).isEmpty()){
//            if (itemStack.getItem() instanceof EntryItem item){
//                this.inputSlots.setItem(2, itemStack.copy());
//                itemStack.shrink(itemStack.getCount());
//                cir.setReturnValue(true);
//            }
//        }
//    }
//@Inject(at = @At("HEAD"), method = "findSlotMatchingIngredient", cancellable = true)
//private static void findSlotMatchingIngredient(SmithingRecipe p_266790_, ItemStack p_266818_, CallbackInfoReturnable<Optional<Integer>> cir) {
//
//        for (WashingMaterials washingMaterials : ModifierHandle.materialsList) {
//
//            if (washingMaterials.item.equals(p_266818_.getItem())) {
//                cir.setReturnValue(Optional.of(2));
//                return;
//            }
//        }
//        cir.setReturnValue(Optional.of(1));
//        return;
//    }
    public SmiMixin(@Nullable MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
        super(p_39773_, p_39774_, p_39775_, p_39776_);
        player.getPersistentData().putBoolean("modifier_refresh_not_enough", false);
    }
    @Inject(at = @At("HEAD"), method = "mayPickup", cancellable = true)
    public void mayPickup(Player p_39792_, boolean p_39793_, CallbackInfoReturnable<Boolean> cir) {

        ItemStack item = this.resultSlots.getItem(0);
        if (item.getTag()!=null){
        if (item.getTag().getBoolean("modifier_refresh")){
            cir.setReturnValue(true);
        }
        if (item.getTag().getBoolean("entry_item_add")) {
            //this.resultSlots.getItem(0).getOrCreateTag().remove("entry_item_add");
            cir.setReturnValue(true);
        }
        }
    }

    @Inject(at = @At("HEAD"), method = "onTake")
    public void onTake(Player p_39790_, ItemStack p_39791_, CallbackInfo ci) {
        if (p_39791_.getTag() ==null)return;
        this.repairItemCountCost = p_39791_.getOrCreateTag().getInt("NeedCount");
        if (p_39791_.getOrCreateTag().getBoolean("modifier_refresh")){
            this.inputSlots.setItem(0, ItemStack.EMPTY);
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied0","");
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");


            CompoundTag orCreateTag = p_39791_.getOrCreateTag();
            orCreateTag.putBoolean("modifier_refresh", false);
            //orCreateTag.putInt("exmodifier_armor_modifier_applied", 0);
            orCreateTag.putBoolean("UNKNOWN",false);

            if (p_39790_.level().isClientSide)return;
            List<String> curios = CuriosUtil.getSlotsFromItemstack(p_39791_);
            MinecraftForge.EVENT_BUS.post(new ExRefreshEvent(p_39790_, orCreateTag.getInt("modifier_refresh_add"), orCreateTag.getInt("modifier_refresh_rarity"), orCreateTag.getString("wash_item")));
            if (curios.isEmpty()) ModifierHandle.CommonEvent.RandomEntry(p_39791_, orCreateTag.getInt("modifier_refresh_rarity"), orCreateTag.getInt("modifier_refresh_add"), orCreateTag.getString("wash_item"));
            else   RandomEntryCurios(p_39791_, orCreateTag.getInt("modifier_refresh_rarity"), orCreateTag.getInt("modifier_refresh_add"), orCreateTag.getString("wash_item"));
            orCreateTag.remove("modifier_refresh_rarity");
            orCreateTag.remove("wash_item");
            orCreateTag.remove("modifier_refresh_add");

        }
    }

    @Inject(at = @At("HEAD"), method = "createResult", cancellable = true)
    public void createResult(CallbackInfo ci) {
        ItemStack WashItem = this.inputSlots.getItem(1);
        ItemStack item = this.inputSlots.getItem(0);
        boolean isFound = false;
        ItemInfo itemInfo = new ItemInfo(item);
        if (item.isEmpty())return;
        ModifierEntryHelper modifierEntryHelper = itemInfo.getModifierEntryHelper();
        for (WashingMaterials washingMaterials : ModifierHandle.materialsList){

            if (washingMaterials.item.equals(WashItem.getItem())) {
                if (WashItem.getCount() >= washingMaterials.NeedCount) {
                    player.getPersistentData().putBoolean("modifier_refresh_not_enough", false);
                    if (modifierEntryHelper.getModifierEntriesSize()>0 || config.refresh_time == 0) {
                        if (washingMaterials.OnlyTypes.isEmpty() || ModifierEntry.containItemTypes(item, washingMaterials.OnlyTypes)) {

                            if (washingMaterials.OnlyItems == null || washingMaterials.OnlyItems.contains(ForgeRegistries.ITEMS.getKey(item.getItem()).toString())) {
                                if (washingMaterials.OnlyTags == null || washingMaterials.containTag(item)) {
                                    ci.cancel();
                                    ItemStack input = item.copy();
                                    itemInfo = new ItemInfo(input);
                                    modifierEntryHelper = itemInfo.reloadModifierEntryHelper();
                                    modifierEntryHelper.removeAllEntry(true);
                                    //ModifierHandle.CommonEvent.clearEntry(input);
//                    input.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
//                    input.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");
                                    CompoundTag orCreateTag = input.getOrCreateTag();
                                    orCreateTag.putInt("entryitem_add", 0);
                                    orCreateTag.putBoolean("UNKNOWN",true);
                                    orCreateTag.putInt("NeedCount", washingMaterials.NeedCount);
                                    //input.getOrCreateTag().putDouble("CostExp", washingMaterials.CostExp);
                                    this.cost.set((int) washingMaterials.CostExp + this.cost.get());
                                    orCreateTag.putBoolean("modifier_refresh", true);
                                    orCreateTag.putBoolean("can_add_max", false);
                                    if (washingMaterials.MinRandomTime * washingMaterials.MaxRandomTime == 0) {
                                        orCreateTag.putInt("modifier_refresh_rarity", washingMaterials.rarity);
                                    } else {
                                        Random random = new Random();
                                        orCreateTag.putInt("modifier_refresh_rarity", washingMaterials.rarity + random.nextInt(washingMaterials.MaxRandomTime - washingMaterials.MinRandomTime) + washingMaterials.MinRandomTime);
                                    }
                                    orCreateTag.putString("wash_item", washingMaterials.ItemId);
                                    orCreateTag.putInt("modifier_refresh_add", washingMaterials.additionEntry);
                                    this.resultSlots.setItem(0, input);
                                    isFound = true;

                                }

                                break;
                            }
                        }
                    }
                }else player.getPersistentData().putBoolean("modifier_refresh_not_enough", true);
            }


        }
        if (!isFound) {
            if (WashItem.getItem() instanceof EntryItem) {
                ItemStack input = item.copy();
                if ( ModifierEntry.containItemType(item, ModifierEntry.StringToType(WashItem.getOrCreateTag().getString("modifier_type")))) {

                    //  Exmodifier.LOGGER.debug("WashItem is EntryItem");
                    CompoundTag orCreateTag = input.getOrCreateTag();
                    int entryitemAdd = orCreateTag.getInt("entryitem_add");
                    if (entryitemAdd < config.canAddEntry) {
                        ci.cancel();
                        if (entryitemAdd + 1 == config.canAddEntry)
                            orCreateTag.putBoolean("can_add_max", true);
                        orCreateTag.putInt("entryitem_add", entryitemAdd + 1);
                        orCreateTag.putInt("NeedCount", 1);
                        orCreateTag.putBoolean("entry_item_add", true);
                        itemInfo = new ItemInfo(input);
                       modifierEntryHelper = itemInfo.reloadModifierEntryHelper();
                        modifierEntryHelper.addModifierEntry(new ModifierInstant(ModifierEntryHelper.getEntry(WashItem.getOrCreateTag().getString("modifier_id")),1),true);
                        this.resultSlots.setItem(0, input);
                    }
                }
            }
        }
    }

}
