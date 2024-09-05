package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.exmo.exmodifier.util.CuriosUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.exmo.exmodifier.content.modifier.ModifierHandle.CommonEvent.RandomEntryCurios;

@Mixin(SmithingMenu.class)
public abstract class SmiMixin extends ItemCombinerMenu {


    @Shadow protected abstract void shrinkStackInSlot(int p_40271_);

    public SmiMixin(@Nullable MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
        super(p_39773_, p_39774_, p_39775_, p_39776_);
    }
    @Inject(at = @org.spongepowered.asm.mixin.injection.At("HEAD"), method = "mayPickup", cancellable = true)
    public void mayPickup(Player p_39792_, boolean p_39793_, CallbackInfoReturnable<Boolean> cir) {
        if (this.resultSlots.getItem(0).getOrCreateTag().getBoolean("modifier_refresh")){
            cir.setReturnValue(true);
        }
    }
    @Inject(at = @org.spongepowered.asm.mixin.injection.At("HEAD"), method = "onTake", cancellable = true)
    public void onTake(Player p_39790_, ItemStack p_39791_, CallbackInfo ci) {
        if (p_39791_.getOrCreateTag().getBoolean("modifier_refresh")){
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied0","");
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");
            p_39791_.getOrCreateTag().putBoolean("modifier_refresh", false);
            p_39791_.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);
            if (p_39790_.level.isClientSide)return;
            List<String> curios = CuriosUtil.getSlotsFromItemstack(p_39791_);
            if (curios.isEmpty()) ModifierHandle.CommonEvent.RandomEntry(p_39791_, p_39791_.getOrCreateTag().getInt("modifier_refresh_rarity"), p_39791_.getOrCreateTag().getInt("modifier_refresh_add"), p_39791_.getOrCreateTag().getString("wash_item"));
            else   RandomEntryCurios(p_39791_, p_39791_.getOrCreateTag().getInt("modifier_refresh_rarity"), p_39791_.getOrCreateTag().getInt("modifier_refresh_add"),p_39791_.getOrCreateTag().getString("wash_item"));

        }
    }

    @Inject(at = @org.spongepowered.asm.mixin.injection.At("HEAD"), method = "createResult", cancellable = true)
    public void createResult(CallbackInfo ci) {

        for (WashingMaterials washingMaterials : ModifierHandle.materialsList){
            if (washingMaterials.item.equals(this.inputSlots.getItem(1).getItem())){
                if (this.inputSlots.getItem(0).getOrCreateTag().getInt("exmodifier_armor_modifier_applied")>0) {
                    ci.cancel();
                    ItemStack input = this.inputSlots.getItem(0).copy();
                    ModifierHandle.CommonEvent.clearEntry(input);
//                    input.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
//                    input.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");
                    input.getOrCreateTag().putString("exmodifier_armor_modifier_applied0","UNKNOWN");
                    input.getOrCreateTag().putBoolean("modifier_refresh", true);
                    input.getOrCreateTag().putInt("modifier_refresh_rarity", washingMaterials.rarity);
                    input.getOrCreateTag().putString("wash_item", washingMaterials.ItemId);
                    input.getOrCreateTag().putInt("modifier_refresh_add", washingMaterials.additionEntry);
                    this.resultSlots.setItem(0, input);

                }
                break;
            }
        }
    }

}
