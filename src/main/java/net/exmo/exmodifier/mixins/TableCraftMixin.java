package net.exmo.exmodifier.mixins;

import net.exmo.exmodifier.content.client.TableCraftData;
import net.exmo.exmodifier.content.helper.ItemLevelHelper;
import net.exmo.exmodifier.content.helper.ModifierEntryHelper;
import net.exmo.exmodifier.network.ExModifiervaV;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.exmo.exmodifier.content.client.TableCraftData._player;
import static net.exmo.exmodifier.content.client.TableCraftData.itemStackSet;

@Mixin(CraftingMenu.class)
public abstract class TableCraftMixin {

    @Shadow @Final private Player player;

    @Shadow @Final private ResultContainer resultSlots;



    @Inject(at = @At("TAIL"),method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V")
    private  void init(int p_39356_, Inventory p_39357_, ContainerLevelAccess p_39358_, CallbackInfo ci){
        exmodifier$clearIndex(player);
    }
    @Inject(at = @At("HEAD"),method = "removed")
    private  void initM(Player p_39389_, CallbackInfo ci){
        exmodifier$clearIndex(p_39389_);
    }

    @Unique
    private static void exmodifier$clearIndex(Player p_39389_) {
        if (p_39389_ == null) return;
        p_39389_.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(
                e -> {
                    e.craftIndex = -2;
                    e.syncPlayerVariables(p_39389_);
                }
        );
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getServer()Lnet/minecraft/server/MinecraftServer;",shift = At.Shift.BEFORE),method = "slotChangedCraftingGrid", remap = false)
    private static void slotChangedCraftingGrid(AbstractContainerMenu p_150547_, Level p_150548_, Player p_150549_, CraftingContainer p_150550_, ResultContainer p_150551_, CallbackInfo ci){
        if (p_150549_ instanceof ServerPlayer) {
            _player = (ServerPlayer) p_150549_;
        }
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setRemoteSlot(ILnet/minecraft/world/item/ItemStack;)V"),method = "slotChangedCraftingGrid")
    private static void slotChangedCraftingGrid$1(AbstractContainerMenu instance, int p_150405_, ItemStack p_150406_){
        if (p_150406_.isEmpty()){
            exmodifier$clearIndex(_player);
        }

    }
    @Redirect( at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"),method = "slotChangedCraftingGrid", remap = false)
    private static ItemStack assemble(CraftingRecipe instance, Container container, RegistryAccess registryAccess) {
        CraftingContainer container1 = (CraftingContainer) container;
        boolean flag = false;
        boolean found = false;
        itemStackSet.clear();
         for (int i = 0; i < container1.getContainerSize(); i++){
             ItemStack itemStack = container1.getItem(i);
           if (itemStack!=null&&!itemStack.isEmpty() && itemStack.hasTag() &&( ModifierEntryHelper.of(itemStack).ValidModifierEntry() || ItemLevelHelper.of(itemStack).ValidItemLevelNbt())){
               int finalI = i+1;
               itemStackSet.put(finalI,itemStack);
               found = true;
               if (!TableCraftData.changeIndex) {

                   _player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY).ifPresent(e ->{
                       e.craftIndex = finalI;
                       e.syncPlayerVariables(_player);
                   });
               }else flag = true;
           }
         }
        if (flag) TableCraftData.changeIndex = false;
        if (!found) exmodifier$clearIndex(_player);
        ItemStack assemble = instance.assemble(container1, registryAccess);
        int craftIndex = _player.getCapability(ExModifiervaV.PLAYER_VARIABLES_CAPABILITY, null).orElse(null).craftIndex;
        ItemStack itemStack = itemStackSet.get(craftIndex);
        if (itemStack!=null&& !itemStack.isEmpty() && itemStack.hasTag()) {
            ModifierEntryHelper.of(assemble).copyOtherHelper(ModifierEntryHelper.of(itemStack));
            ItemLevelHelper.of(assemble).copyOtherHelper(ItemLevelHelper.of(itemStack));
        }
         return assemble;
    }

}
