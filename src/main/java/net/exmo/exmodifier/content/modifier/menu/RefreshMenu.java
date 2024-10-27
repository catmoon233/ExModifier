package net.exmo.exmodifier.content.modifier.menu;

import net.exmo.exmodifier.config;
import net.exmo.exmodifier.content.helper.ItemInfo;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.modifier.*;
import net.exmo.exmodifier.events.ExRefreshEvent;
import net.exmo.exmodifier.init.RegisterOther;
import net.exmo.exmodifier.util.CuriosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntryCurios;

public class RefreshMenu extends ItemCombinerMenu implements Supplier<Map<Integer, Slot>> {
    public static ContainerLevelAccess Con(FriendlyByteBuf extraData,Level level){
        if (extraData != null) {
            BlockPos pos;
            pos = extraData.readBlockPos();
            return ContainerLevelAccess.create(level, pos);
        }
        return ContainerLevelAccess.NULL;
    }

    public RefreshMenu(int p_39008_, Inventory inventory, FriendlyByteBuf extraData ) {
        super(RegisterOther.MenuAbout.REFRESH_MENU.get(), p_39008_, inventory, Con(extraData,inventory.player.level()));
        this.addDataSlot(this.cost);
        this.world = inventory.player.level();

    }
    private final DataSlot cost = DataSlot.standalone();
    public int repairItemCountCost;
    public int x,y,z;
    public Level world;
    @Override
    public boolean mayPickup(Player p_39792_, boolean p_39793_) {

        ItemStack item = this.resultSlots.getItem(0);
        if (item.getTag()!=null){
            if (item.getTag().getBoolean("modifier_refresh")){
                return player.experienceLevel >= this.cost.get() && this.cost.get() > 0;
            }
            if (item.getTag().getBoolean("entry_item_add")) {
                //this.resultSlots.getItem(0).getOrCreateTag().remove("entry_item_add");
                return player.experienceLevel >= this.cost.get() && this.cost.get() > 0;
            }
        }
        return false;
    }
    public int getCost() {
        return this.cost.get();
    }
    public void setMaximumCost(int value) {
        this.cost.set(value);
    }
    @Override
    protected void onTake(Player p_39790_, ItemStack p_39791_) {
        if (p_39791_.getTag() ==null)return;
        if (!p_39790_.getAbilities().instabuild) {
            p_39790_.giveExperienceLevels(-this.cost.get());
        }
        this.repairItemCountCost = p_39791_.getOrCreateTag().getInt("NeedCount");
        if (p_39791_.getOrCreateTag().getBoolean("modifier_refresh")){
            this.inputSlots.setItem(0, ItemStack.EMPTY);
            if (this.repairItemCountCost > 0) {
                ItemStack itemstack = this.inputSlots.getItem(1);
                if (!itemstack.isEmpty() && itemstack.getCount() > this.repairItemCountCost) {
                    itemstack.shrink(this.repairItemCountCost);
                    this.inputSlots.setItem(1, itemstack);
                } else {
                    this.inputSlots.setItem(1, ItemStack.EMPTY);
                }
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
            this.cost.set(0);

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

    @Override
    protected boolean isValidBlock(BlockState p_39019_) {
        return p_39019_.is(RegisterOther.BlockAbout.REFRESH_TABLE.get());
    }


    @Override
    public void createResult() {
        ItemStack WashItem = this.inputSlots.getItem(1);
        ItemStack item = this.inputSlots.getItem(0);
        if (item.isEmpty() || WashItem.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            return;
        }
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
    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 27, 47, (p_266635_) -> {
            return true;
        }).withSlot(1, 76, 47, (p_266634_) -> {
            return true;
        }).withResultSlot(2, 134, 47).build();
    }

    @Override
    public Map<Integer, Slot> get() {
        return Map.of();
    }
}