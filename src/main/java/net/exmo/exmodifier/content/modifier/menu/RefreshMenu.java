package net.exmo.exmodifier.content.modifier.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RefreshMenu extends ItemCombinerMenu {
    public RefreshMenu(@Nullable MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
        super(p_39773_, p_39774_, p_39775_, p_39776_);
    }

    @Override
    protected boolean mayPickup(Player p_39798_, boolean p_39799_) {
        return false;
    }

    @Override
    protected void onTake(Player p_150601_, ItemStack p_150602_) {

    }

    @Override
    protected boolean isValidBlock(BlockState p_39788_) {
        return false;
    }

    @Override
    public void createResult() {

    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return null;
    }
}