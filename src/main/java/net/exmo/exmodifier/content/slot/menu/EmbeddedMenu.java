package net.exmo.exmodifier.content.slot.menu;

import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.content.helper.ModifierSlotHelper;
import net.exmo.exmodifier.content.modifier.*;
import net.exmo.exmodifier.content.selected.BaseItemSelected;
import net.exmo.exmodifier.content.slot.ModifierSlot;
import net.exmo.exmodifier.content.slot.ModifierSlotHandle;
import net.exmo.exmodifier.content.slot.UnLockSlotItem;
import net.exmo.exmodifier.init.RegisterOther;
import net.exmo.exmodifier.util.WeightedUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntryCurios;

public class EmbeddedMenu extends AbstractContainerMenu {

    public static Map<String,Integer> SlotMap = new HashMap<>();
    public static ContainerLevelAccess Con(FriendlyByteBuf extraData,Level level){
        if (extraData != null) {
            BlockPos pos;
            pos = extraData.readBlockPos();
            return ContainerLevelAccess.create(level, pos);
        }
        return ContainerLevelAccess.NULL;
    }
    private final DataSlot cost = DataSlot.standalone();
    public Level world;
    private static boolean canPlace2(ItemStack stack){
      return   ModifierSlotHandle.unLockSlotItems.stream().map(unLockSlotItem -> unLockSlotItem.item).toList().contains(stack.getItem()) || Exmodifier.ENTRY_ITEM.get() == stack.getItem();
    }
    public EmbeddedMenu(int p_39008_, Inventory inventory, FriendlyByteBuf extraData) {
        super(RegisterOther.MenuAbout.EMBEDDED_MENU.get(), p_39008_);
        this.addDataSlot(this.cost);
        Player player = inventory.player;
        this.world = player.level();
        this.access = Con(extraData, player.level());
        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
            public boolean mayPlace(ItemStack p_39508_) {
                return true;
            }

            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
            public boolean mayPlace(ItemStack p_39517_) {
                return (canPlace2(p_39517_));
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        this.addDataSlot(DataSlot.shared(this.costs, 0));
        this.addDataSlot(DataSlot.shared(this.costs, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 2));
        this.addDataSlot(this.enchantmentSeed).set(player.getEnchantmentSeed());
        this.addDataSlot(DataSlot.shared(this.ClueId, 0));
        this.addDataSlot(DataSlot.shared(this.ClueId, 1));
        this.addDataSlot(DataSlot.shared(this.ClueId, 2));
        this.addDataSlot(DataSlot.shared(this.levelClue, 0));
        this.addDataSlot(DataSlot.shared(this.levelClue, 1));
        this.addDataSlot(DataSlot.shared(this.levelClue, 2));
    }
    private final Container enchantSlots = new SimpleContainer(2) {
        public void setChanged() {
            super.setChanged();
            EmbeddedMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;
    private final RandomSource random = RandomSource.create();
    private final DataSlot enchantmentSeed = DataSlot.standalone();
    public final int[] costs = new int[3];
    public final int[] ClueId = new int[]{-1,-1,-1};
    public final int[] levelClue = new int[]{-1, -1, -1};



    public void slotsChanged(Container p_39461_) {
        if (p_39461_ == this.enchantSlots) {
            ItemStack input = p_39461_.getItem(0);
            ItemStack itemstack1 = p_39461_.getItem(1);
            if (itemstack1.isEmpty()|| input.isEmpty()){
                for (int i = 0; i < 3; i++) {
                    this.ClueId[i] = -1;
                    this.levelClue[i] = -1;
                    this.costs[i] = 0;

                }
                return;
            }

            if (ModifierSlotHandle.unLockSlotItems.stream().anyMatch(
                    unLockSlotItem -> unLockSlotItem.item == itemstack1.getItem()
            )) {
                UnLockSlotItem unLockSlotItem = ModifierSlotHandle.unLockSlotItems.stream()
                        .filter(unLockSlotItem1 -> unLockSlotItem1.item == itemstack1.getItem())
                        .findFirst()
                        .orElse(null); // 使用 orElse(null) 避免 NoSuchElementException

                if (unLockSlotItem != null) {
                    if (unLockSlotItem.getModifierItemSelector().containItem(input)){
                        this.access.execute((p_39485_, p_39486_) -> {
                            float j = 0;

                            for(BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                                if (EnchantmentTableBlock.isValidBookShelf(p_39485_, p_39486_, blockpos)) {
                                    j += p_39485_.getBlockState(p_39486_.offset(blockpos)).getEnchantPowerBonus(p_39485_, p_39486_.offset(blockpos));
                                }
                            }

                            this.random.setSeed((long)this.enchantmentSeed.get());

                            WeightedUtil<ModifierSlot> list = new WeightedUtil<>((ModifierSlotHandle.registerSlots.entrySet().stream().filter(
                                    e -> e.getValue().getModifierItemSelector().containItem(input)
                                            &&!ModifierSlotHelper.of(input).getModifierSlotList().stream().map(a -> a.getId()).toList().contains(e.getValue().getId())
                                            &&unLockSlotItem.getSlots().contains(e.getKey().toString())))
                                    .collect(Collectors.toMap(Map.Entry::getValue, e -> e.getValue().getWeight())));
                            for(int l = 0; l < 3; ++l) {
                                this.costs[l] = unLockSlotItem.getNeedCount() * (l+1);
                                if (this.costs[l] > 0) {
                                    if (itemstack1.getCount()>=this.costs[l]){




                                    if (list != null && !list.isEmpty()) {
                                        var a = list.selectRandomKeyBasedOnWeightsAndRemoved();
                                        if (a==null){
                                            this.ClueId[l] = -1;
                                            this.levelClue[l] = -1;
                                            this.costs[l] = 0;
                                            continue;

                                        }
                                        this.ClueId[l] = a.getId();
                                        this.levelClue[l] = EnchantmentHelper.getEnchantmentCost(this.random, l, (int) (j+unLockSlotItem.getCostExp()), input);;

                                    }else {
                                        this.ClueId[l] = -1;
                                        this.levelClue[l] = -1;
                                        this.costs[l] = 0;
                                    }
                                }
                                }
                            }

                            this.broadcastChanges();
                        });


                    }
                }
            }else {
                if (itemstack1.getItem() == Exmodifier.ENTRY_ITEM.get()){
                    String modifierID = EntryItem.getModifierID(itemstack1);
                    ModifierEntry modifierEntry = ModifierHandle.findModifierEntry(modifierID);
                   // String itemId = ForgeRegistries.ITEMS.getKey(input.getItem()).toString();
                    if (modifierEntry==null)return;
                    if (!modifierEntry.getModifierItemSelector().containItem(input))return;
//                    if (!exElement.OnlyItems.isEmpty() && exElement.OnlyItems.contains(itemId) )return;
//                    if (!exElement.containTag(input))return;
//                    if (!exElement.UnlessItemIds)return;

                    ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(input);

                if (modifierEntryHelper.getModifierEntries().stream().map(e->e.getModifierEntry().getResId()).toList().contains(modifierID)){

                        if(modifierEntryHelper.getModifierEntryLevel(modifierID)>=EntryItem.getModifierLevel(itemstack1)){
                            for (int i = 0; i < 3; i++)
                            {
                                this.ClueId[i] = -1;
                                this.levelClue[i] = -1;
                                this.costs[i] = 0;
                            }
                            return;
                        }
                    }
                    this.access.execute((p_39481_, p_39482_) -> {
                        WeightedUtil<ModifierSlot> list = new WeightedUtil<>((ModifierSlotHandle.registerSlots.entrySet().stream().filter(
                                         e -> e.getValue().getModifierItemSelector().containItem(input)
                                        &&ModifierSlotHelper.of(input).getModifierSlotList().contains(e.getValue())
                                        &&modifierEntry.Slots.contains(e.getKey().toString())
                                        )
                                .collect(Collectors.toMap(Map.Entry::getValue, e -> e.getValue().getWeight()))));

                    for (int i = 0; i < 3; i++){
                        if (list != null && !list.isEmpty()) {
                            var a = list.selectRandomKeyBasedOnWeightsAndRemoved();

                            ResourceLocation key = ModifierSlotHandle.getKey(a);
                            if (key==null)continue;
//                            if (!exElement.Slots.contains(key.toString())){
//                                this.ClueId[i] = -1;
//                                this.levelClue[i] =-1;
//                                this.costs[i] = 0;
//                                continue;
//                            }
                            if (a==null){
                                this.ClueId[i] = -1;
                                this.levelClue[i] =-1;
                                this.costs[i] = 0;
                                continue;
                            }
                            this.ClueId[i] = a.getId();
                            this.levelClue[i] =i;
                            this.costs[i] = 1;
                        }else {
                            this.ClueId[i] = -1;
                            this.levelClue[i] =-1;
                            this.costs[i] = 0;
                        }
                    }
                    });
                }
            }

        }

    }

    public boolean clickMenuButton(Player player, int index) {

        if (index >= 0 && index < this.costs.length) {
            ItemStack itemstack = this.enchantSlots.getItem(0);
            ItemStack itemstack1 = this.enchantSlots.getItem(1);
            int i = index + 1;
            if (itemstack1.isEmpty()  && !player.getAbilities().instabuild) {
                return false;
            } else if (this.costs[index] <= 0 || itemstack.isEmpty() || (player.experienceLevel < i || player.experienceLevel < this.costs[index]) && !player.getAbilities().instabuild) {
                return false;
            } else {
                this.access.execute((p_39481_, p_39482_) -> {
                    //ItemStack itemstack2 = itemstack;
                    boolean flag = itemstack1.getItem() == Exmodifier.ENTRY_ITEM.get();
                    if (!flag) {
                        //   WeightedUtil<ModifierSlot> list = new WeightedUtil<>((ModifierSlotHandle.registerSlots.entrySet().stream().filter(e -> e.getValue().contain(itemstack)).collect(Collectors.toMap(Map.Entry::getValue, e -> e.getValue().getWeight()))));
//                        if (!list.isEmpty()) {
//                            //player.onEnchantmentPerformed(itemstack, i);
//
//                            for (int j = 0; j < 3; ++j) {
//                                var a = list.selectRandomKeyBasedOnWeightsAndRemoved();
//                                ModifierSlotHelper.of(itemstack).addSlot(a);
//                            }
//                        }
                        var slot = (ModifierSlot)BaseItemSelected.getValue(this.ClueId[index]);
                        if (slot != null) {
                            ModifierSlotHelper.of(itemstack).addSlot(slot);
                            itemstack1.shrink(this.costs[index]);
                        }
                    }else {
                        ModifierEntry a = ModifierHandle.findModifierEntry(EntryItem.getModifierID(itemstack1));
                        if (a==null)return;
                        ModifierEntryHelper modifierEntryHelper = ModifierEntryHelper.of(itemstack);
                        List<ModifierInstant> list = modifierEntryHelper.getModifierEntries().stream().filter(e -> e.getSlot().isPresent()&& a.Slots.contains(e.getSlot().get())).toList();
                        if (!list.isEmpty()) {
                            for (var a1 : list){
                                modifierEntryHelper.removeModifierEntry(a1,true);
                            }
                        }
                        itemstack1.shrink(1);
                        var slot = (ModifierSlot)BaseItemSelected.getValue(this.ClueId[index]);

                            if (modifierEntryHelper.getModifierEntryLevel(a.getId())>0){
                                modifierEntryHelper.removeModifierEntry(modifierEntryHelper.getModifierEntries().
                                        stream().filter(e->e.getModifierEntry().getId().equals(a.getId()))
                                        .findFirst().get()
                                        ,true
                                        );
                            }
                            if (slot!=null) modifierEntryHelper.addModifierEntry(ModifierInstant.of(a,EntryItem.getModifierLevel(itemstack1)).setSlot(ModifierSlotHandle.getKey(slot).toString()),true,false);

                    }
                        if (!player.getAbilities().instabuild) {
                            itemstack1.shrink(i);
                            if (itemstack1.isEmpty()) {
                                this.enchantSlots.setItem(1, ItemStack.EMPTY);
                            }
                        }


                        this.enchantSlots.setChanged();
                        this.enchantmentSeed.set(player.getEnchantmentSeed());
                        this.slotsChanged(this.enchantSlots);
                        p_39481_.playSound((Player)null, p_39482_, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, p_39481_.random.nextFloat() * 0.1F + 0.9F);


                });
                return true;
            }
        } else {
            Util.logAndPauseIfInIde(player.getName() + " pressed invalid button id: " + index);
            return false;
        }
    }



    public int getGoldCount() {
        ItemStack itemstack = this.enchantSlots.getItem(1);
        return itemstack.isEmpty() ? 0 : itemstack.getCount();
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    public void removed(Player p_39488_) {
        super.removed(p_39488_);
        this.access.execute((p_39469_, p_39470_) -> {
            this.clearContainer(p_39488_, this.enchantSlots);
        });
    }

    public boolean stillValid(Player p_39463_) {
        return stillValid(this.access, p_39463_, RegisterOther.BlockAbout.EMBEDDED_TABLE.get());
    }

    public ItemStack quickMoveStack(Player p_39490_, int p_39491_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39491_);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (p_39491_ == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (p_39491_ == 1) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (canPlace2(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemstack1)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack2 = itemstack1.copyWithCount(1);
                itemstack1.shrink(1);
                this.slots.get(0).setByPlayer(itemstack2);
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(p_39490_, itemstack1);
        }

        return itemstack;
    }
}