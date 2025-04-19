package net.exmo.exmodifier.content.modifier.menu;

import net.exmo.exmodifier.init.RegisterOther;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class RefreshMenuPlus extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    public static ContainerLevelAccess Con(FriendlyByteBuf extraData, Level level){
        return RefreshMenu.Con(extraData, level);
    }
    protected final ContainerLevelAccess access;
    public Player player;
    public RefreshMenuPlus(int p_39008_, Inventory inventory, FriendlyByteBuf extraData) {
        super(RegisterOther.MenuAbout.REFRESH_MENU_PLUS.get(), p_39008_);
        this.access = Con(extraData, inventory.player.level());
        this.player = inventory.player;
        this.addDataSlot(this.cost);
        this.world = inventory.player.level();

    }
    private final DataSlot cost = DataSlot.standalone();
    public Level world;
    @Override
    public Map<Integer, Slot> get() {
        return Map.of();
    }


    @Override
    public void removed(Player p_38940_) {
        super.removed(p_38940_);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
