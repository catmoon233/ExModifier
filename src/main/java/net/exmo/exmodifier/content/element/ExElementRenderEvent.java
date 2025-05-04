package net.exmo.exmodifier.content.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ExElementRenderEvent {
    /*
    *  copy L2Hositily  ElementRenderEvent
    */
    public static Vec3 getRayTerm(Vec3 pos, float xRot, float yRot, double reach) {
        float f2 = Mth.cos(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-xRot * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-xRot * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        return pos.add((double) f6 * reach, (double) f5 * reach, (double) f7 * reach);
    }

    @Nullable
    public static EntityHitResult rayTraceEntity(Player player, double reach, Predicate<Entity> pred) {
        Level world = player.level();
        Vec3 pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 end = getRayTerm(pos, player.getXRot(), player.getYRot(), reach);
        AABB box = (new AABB(pos, end)).inflate((double) 1.0F);
        return ProjectileUtil.getEntityHitResult(world, player, pos, end, box, pred);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderNamePlate(RenderNameTagEvent event) {


        if (event.getEntity() instanceof LivingEntity le && le.getHealth() > 0) {
            LocalPlayer player = Minecraft.getInstance().player;
            double x1 = le.getX();
            double y1 = le.getY();
            double z1 = le.getZ();
            if ((Math.abs(player.getX() - x1) + Math.abs(player.getY() - y1) + Math.abs(player.getZ() - z1)) <= 25) {


                assert player != null;
                boolean needHover = le.isInvisible();
                if (needHover && rayTraceEntity(player, player.getEntityReach(), e -> e == le) == null) {
                    return;
                }

                Font.DisplayMode mode = player.hasLineOfSight(event.getEntity()) ?
                        Font.DisplayMode.SEE_THROUGH :
                        Font.DisplayMode.NORMAL;
                ExElementInstant orAskElement = ExElementEntityData.getOrAskElement(le.getUUID());
                if (orAskElement != null) {
                    renderNameTag(event, orAskElement.getDesc(), event.getPoseStack(), 0.6f, mode);
                }
            }

        }
    }

    protected static void renderNameTag(RenderNameTagEvent event, Component text, PoseStack pose, float offset, Font.DisplayMode mode) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        double d0 = dispatcher.distanceToSqr(event.getEntity());
        int max = 24;
        int light = LightTexture.FULL_BRIGHT;
        if (d0 < max * max) {

            float f = event.getEntity().getNameTagOffsetY() + offset;
            pose.pushPose();
            pose.translate(0.0F, f, 0.0F);
            pose.mulPose(dispatcher.cameraOrientation());
            pose.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = pose.last().pose();
            Font font = event.getEntityRenderer().getFont();
            float f2 = (float) (-font.width(text) / 2);
            float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int j = (int) (f1 * 255.0F) << 24;
            font.drawInBatch(text, f2, 0, -1, false, matrix4f,
                    event.getMultiBufferSource(), mode, j, light);
            pose.popPose();
        }
    }

}
