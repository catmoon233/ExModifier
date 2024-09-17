package net.exmo.exmodifier.mixins;

import com.google.gson.internal.bind.JsonTreeReader;
import net.exmo.exmodifier.Exmodifier;
import net.exmo.exmodifier.config;
import net.exmo.exmodifier.content.modifier.ModifierEntry;
import net.exmo.exmodifier.content.modifier.ModifierHandle;
import net.exmo.exmodifier.content.modifier.WashingMaterials;
import net.exmo.exmodifier.events.ExRefreshEvent;
import net.exmo.exmodifier.util.CuriosUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
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

@Mixin(SmithingMenu.class)
public abstract class SmiMixin extends ItemCombinerMenu {


    @Shadow protected abstract void shrinkStackInSlot(int p_40271_);
    @Inject(at = @At("HEAD"), method = "canMoveIntoInputSlots", cancellable = true)
    public void canMoveIntoInputSlots(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
       // Exmodifier.LOGGER.debug("Checking if item is washing material");
        if (inputSlots.getItem(2).isEmpty()) {

            for (WashingMaterials washingMaterials : ModifierHandle.materialsList) {

                if (washingMaterials.item.equals(itemStack.getItem())) {
                    cir.setReturnValue(true);

                    this.inputSlots.setItem(2, itemStack.copy());
                    itemStack.shrink(itemStack.getCount());
                    //       Exmodifier.LOGGER.debug("Checking if item is washing material -> true");
                    return;
                }
            }
        }
        if (inputSlots.getItem(1).isEmpty()) {
            this.inputSlots.setItem(1, itemStack.copy());
            itemStack.shrink(itemStack.getCount());
            cir.setReturnValue(true);
        }
    }
@Inject(at = @At("HEAD"), method = "findSlotMatchingIngredient", cancellable = true)
private static void findSlotMatchingIngredient(SmithingRecipe p_266790_, ItemStack p_266818_, CallbackInfoReturnable<Optional<Integer>> cir) {

        for (WashingMaterials washingMaterials : ModifierHandle.materialsList) {

            if (washingMaterials.item.equals(p_266818_.getItem())) {
                cir.setReturnValue(Optional.of(2));
                return;
            }
        }
        cir.setReturnValue(Optional.of(1));
        return;
    }
    public SmiMixin(@Nullable MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
        super(p_39773_, p_39774_, p_39775_, p_39776_);
    }
    @Inject(at = @At("HEAD"), method = "mayPickup", cancellable = true)
    public void mayPickup(Player p_39792_, boolean p_39793_, CallbackInfoReturnable<Boolean> cir) {
        if (this.resultSlots.getItem(0).getOrCreateTag().getBoolean("modifier_refresh")){
            cir.setReturnValue(true);
        }
    }
    @Inject(at = @At("HEAD"), method = "onTake", cancellable = true)
    public void onTake(Player p_39790_, ItemStack p_39791_, CallbackInfo ci) {
        if (p_39791_.getOrCreateTag().getBoolean("modifier_refresh")){
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied0","");
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
//            p_39791_.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");
            p_39791_.getOrCreateTag().putBoolean("modifier_refresh", false);
            p_39791_.getOrCreateTag().putInt("exmodifier_armor_modifier_applied", 0);
            if (p_39790_.level().isClientSide)return;
            List<String> curios = CuriosUtil.getSlotsFromItemstack(p_39791_);
            MinecraftForge.EVENT_BUS.post(new ExRefreshEvent(p_39790_, p_39791_.getOrCreateTag().getInt("modifier_refresh_add"), p_39791_.getOrCreateTag().getInt("modifier_refresh_rarity"), p_39791_.getOrCreateTag().getString("wash_item")));
            if (curios.isEmpty()) ModifierHandle.CommonEvent.RandomEntry(p_39791_, p_39791_.getOrCreateTag().getInt("modifier_refresh_rarity"), p_39791_.getOrCreateTag().getInt("modifier_refresh_add"), p_39791_.getOrCreateTag().getString("wash_item"));
            else   RandomEntryCurios(p_39791_, p_39791_.getOrCreateTag().getInt("modifier_refresh_rarity"), p_39791_.getOrCreateTag().getInt("modifier_refresh_add"),p_39791_.getOrCreateTag().getString("wash_item"));

        }
    }

    @Inject(at = @At("HEAD"), method = "createResult", cancellable = true)
    public void createResult(CallbackInfo ci) {

        for (WashingMaterials washingMaterials : ModifierHandle.materialsList){
            if (washingMaterials.item.equals(this.inputSlots.getItem(2).getItem())) {
                if (this.inputSlots.getItem(1).getOrCreateTag().getInt("exmodifier_armor_modifier_applied") > 0 || config.refresh_time == 0) {
                    if (washingMaterials.OnlyTypes.isEmpty() || washingMaterials.OnlyTypes.contains(ModifierEntry.Type.ALL) || washingMaterials.OnlyTypes.contains(ModifierEntry.getType(this.inputSlots.getItem(0)))) {

                        if (washingMaterials.OnlyItems == null || washingMaterials.OnlyItems.contains(ForgeRegistries.ITEMS.getKey(this.inputSlots.getItem(1).getItem()).toString())) {
                            if (washingMaterials.OnlyTags == null || washingMaterials.containTag(this.inputSlots.getItem(1))) {
                                ci.cancel();
                                ItemStack input = this.inputSlots.getItem(1).copy();
                                ModifierHandle.CommonEvent.clearEntry(input);
//                    input.getOrCreateTag().putString("exmodifier_armor_modifier_applied1","");
//                    input.getOrCreateTag().putString("exmodifier_armor_modifier_applied2","");
                                input.getOrCreateTag().putString("exmodifier_armor_modifier_applied0", "UNKNOWN");
                                input.getOrCreateTag().putBoolean("modifier_refresh", true);
                                if (washingMaterials.MinRandomTime * washingMaterials.MaxRandomTime == 0) {
                                    input.getOrCreateTag().putInt("modifier_refresh_rarity", washingMaterials.rarity);
                                } else {
                                    Random random = new Random();
                                    input.getOrCreateTag().putInt("modifier_refresh_rarity", washingMaterials.rarity + random.nextInt(washingMaterials.MaxRandomTime - washingMaterials.MinRandomTime) + washingMaterials.MinRandomTime);
                                }
                                input.getOrCreateTag().putString("wash_item", washingMaterials.ItemId);
                                input.getOrCreateTag().putInt("modifier_refresh_add", washingMaterials.additionEntry);
                                this.resultSlots.setItem(0, input);

                            }

                            break;
                        }
                    }
                }
            }
        }
    }

}
