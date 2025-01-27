package net.exmo.exmodifier.content.slot.block.enitty;

import io.netty.buffer.Unpooled;

import net.exmo.exmodifier.content.slot.menu.EmbeddedMenu;
import net.exmo.exmodifier.init.RegisterOther;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class EmbeddedEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(7, ItemStack.EMPTY);
	private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
	public int time;
	public float flip;
	public float oFlip;
	public float flipT;
	public float flipA;
	public float open;
	public float oOpen;
	public float rot;
	public float oRot;
	public float tRot;
	private static final RandomSource RANDOM = RandomSource.create();
	private Component name;
	public EmbeddedEntity(BlockPos position, BlockState state) {
		super(RegisterOther.BlockEntityAbout.EmbeddedTableEntity.get(), position, state);
	}
	public static void bookAnimationTick(Level p_155504_, BlockPos p_155505_, BlockState p_155506_, EmbeddedEntity p_155507_) {
		p_155507_.oOpen = p_155507_.open;
		p_155507_.oRot = p_155507_.rot;
		Player player = p_155504_.getNearestPlayer((double)p_155505_.getX() + 0.5D, (double)p_155505_.getY() + 0.5D, (double)p_155505_.getZ() + 0.5D, 3.0D, false);
		if (player != null) {
			double d0 = player.getX() - ((double)p_155505_.getX() + 0.5D);
			double d1 = player.getZ() - ((double)p_155505_.getZ() + 0.5D);
			p_155507_.tRot = (float) Mth.atan2(d1, d0);
			p_155507_.open += 0.1F;
			if (p_155507_.open < 0.5F || RANDOM.nextInt(40) == 0) {
				float f1 = p_155507_.flipT;

				do {
					p_155507_.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
				} while(f1 == p_155507_.flipT);
			}
		} else {
			p_155507_.tRot += 0.02F;
			p_155507_.open -= 0.1F;
		}

		while(p_155507_.rot >= (float)Math.PI) {
			p_155507_.rot -= ((float)Math.PI * 2F);
		}

		while(p_155507_.rot < -(float)Math.PI) {
			p_155507_.rot += ((float)Math.PI * 2F);
		}

		while(p_155507_.tRot >= (float)Math.PI) {
			p_155507_.tRot -= ((float)Math.PI * 2F);
		}

		while(p_155507_.tRot < -(float)Math.PI) {
			p_155507_.tRot += ((float)Math.PI * 2F);
		}

		float f2;
		for(f2 = p_155507_.tRot - p_155507_.rot; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F)) {
		}

		while(f2 < -(float)Math.PI) {
			f2 += ((float)Math.PI * 2F);
		}

		p_155507_.rot += f2 * 0.4F;
		p_155507_.open = Mth.clamp(p_155507_.open, 0.0F, 1.0F);
		++p_155507_.time;
		p_155507_.oFlip = p_155507_.flip;
		float f = (p_155507_.flipT - p_155507_.flip) * 0.4F;
		float f3 = 0.2F;
		f = Mth.clamp(f, -0.2F, 0.2F);
		p_155507_.flipA += (f - p_155507_.flipA) * 0.9F;
		p_155507_.flip += p_155507_.flipA;
	}
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		if (!this.tryLoadLootTable(compound))
			this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.stacks);
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		if (!this.trySaveLootTable(compound)) {
			ContainerHelper.saveAllItems(compound, this.stacks);
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}

	@Override
	public int getContainerSize() {
		return stacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.stacks)
			if (!itemstack.isEmpty())
				return false;
		return true;
	}

	@Override
	public Component getDefaultName() {
		return Component.literal("activator");
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new EmbeddedMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("活化器");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.stacks;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> stacks) {
		this.stacks = stacks;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {

		return true;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return IntStream.range(0, this.getContainerSize()).toArray();
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(index, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		if (index == 1)
			return false;
		if (index == 2)
			return false;
		if (index == 4)
			return false;
		if (index == 5)
			return false;
		if (index == 6)
			return false;
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER)
			return handlers[facing.ordinal()].cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		for (LazyOptional<? extends IItemHandler> handler : handlers)
			handler.invalidate();
	}
}
