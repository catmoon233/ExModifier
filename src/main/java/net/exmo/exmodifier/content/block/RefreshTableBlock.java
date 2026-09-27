package net.exmo.exmodifier.content.block;

import com.mojang.serialization.MapCodec;
import net.exmo.exmodifier.content.ExModifierContent;
import net.exmo.exmodifier.content.block.entity.RefreshTableEntity;
import net.exmo.exmodifier.content.menu.RefreshMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public final class RefreshTableBlock extends Block implements EntityBlock {
    public static final MapCodec<RefreshTableBlock> CODEC = simpleCodec(RefreshTableBlock::new);

    public RefreshTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(menu(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private MenuProvider menu(BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) ->
                new RefreshMenu(id, inventory, ContainerLevelAccess.create(player.level(), pos)),
                Component.translatable("container.exmodifier.refresh_table"));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefreshTableEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide && type == ExModifierContent.REFRESH_TABLE_ENTITY.get()) {
            @SuppressWarnings("unchecked")
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (BlockEntityTicker<RefreshTableEntity>) RefreshTableEntity::bookAnimationTick;
            return ticker;
        }
        return null;
    }
}
