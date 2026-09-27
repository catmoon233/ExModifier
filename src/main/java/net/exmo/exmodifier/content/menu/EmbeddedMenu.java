package net.exmo.exmodifier.content.menu;

import net.exmo.exmodifier.content.ExModifierContent;
import net.exmo.exmodifier.core.runtime.ModifierService;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class EmbeddedMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final SimpleContainer inputs = new SimpleContainer(2);

    public EmbeddedMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public EmbeddedMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ExModifierContent.EMBEDDED_MENU.get(), id);
        this.access = access;
        addSlot(new Slot(inputs, 0, 27, 47));
        addSlot(new Slot(inputs, 1, 76, 47));
        addPlayerSlots(inventory);
    }

    public boolean tryApply(Player player) {
        ItemStack gear = inputs.getItem(0);
        ItemStack extra = inputs.getItem(1);
        if (gear.isEmpty() || extra.isEmpty()) return false;
        if (ModifierService.embedBook(gear, extra)) {
            broadcastChanges();
            return true;
        }
        if (ModifierService.unlockSlot(player, gear, extra)) {
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) return tryApply(player);
        return false;
    }

    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ExModifierContent.EMBEDDED_TABLE.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, inputs));
    }
}
