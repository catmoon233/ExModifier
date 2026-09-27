package net.exmo.exmodifier.content.block.entity;

import net.exmo.exmodifier.content.ExModifierContent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Animated book block entity for the refresh table (mirrors the enchanting-table book). */
public class RefreshTableEntity extends BlockEntity {
    private static final RandomSource RANDOM = RandomSource.create();

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

    public RefreshTableEntity(BlockPos pos, BlockState state) {
        super(ExModifierContent.REFRESH_TABLE_ENTITY.get(), pos, state);
    }

    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state, RefreshTableEntity entity) {
        entity.oOpen = entity.open;
        entity.oRot = entity.rot;
        Player player = level.getNearestPlayer((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, 3.0D, false);
        if (player != null) {
            double dx = player.getX() - ((double) pos.getX() + 0.5D);
            double dz = player.getZ() - ((double) pos.getZ() + 0.5D);
            entity.tRot = (float) Mth.atan2(dz, dx);
            entity.open += 0.1F;
            if (entity.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float f1 = entity.flipT;
                do {
                    entity.flipT += (float) (RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f1 == entity.flipT);
            }
        } else {
            entity.tRot += 0.02F;
            entity.open -= 0.1F;
        }
        while (entity.rot >= (float) Math.PI) entity.rot -= ((float) Math.PI * 2F);
        while (entity.rot < -(float) Math.PI) entity.rot += ((float) Math.PI * 2F);
        while (entity.tRot >= (float) Math.PI) entity.tRot -= ((float) Math.PI * 2F);
        while (entity.tRot < -(float) Math.PI) entity.tRot += ((float) Math.PI * 2F);
        float f2;
        for (f2 = entity.tRot - entity.rot; f2 >= (float) Math.PI; f2 -= ((float) Math.PI * 2F)) {}
        while (f2 < -(float) Math.PI) f2 += ((float) Math.PI * 2F);
        entity.rot += f2 * 0.4F;
        entity.open = Mth.clamp(entity.open, 0.0F, 1.0F);
        entity.time++;
        entity.oFlip = entity.flip;
        float f = (entity.flipT - entity.flip) * 0.4F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        entity.flipA += (f - entity.flipA) * 0.9F;
        entity.flip += entity.flipA;
    }
}
