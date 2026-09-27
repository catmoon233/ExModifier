package net.exmo.exmodifier.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.exmo.exmodifier.content.block.entity.EmbeddedTableEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class EmbeddedTableEntityRenderer implements BlockEntityRenderer<EmbeddedTableEntity> {
    public static final Material BOOK_LOCATION = new Material(
            TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/enchanting_table_book"));
    private final BookModel bookModel;

    public EmbeddedTableEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(EmbeddedTableEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        pose.pushPose();
        pose.translate(0.5F, 0.75F, 0.5F);
        float f = (float) entity.time + partialTick;
        pose.translate(0.0F, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0F);
        float f1;
        for (f1 = entity.rot - entity.oRot; f1 >= (float) Math.PI; f1 -= ((float) Math.PI * 2F)) {}
        while (f1 < -(float) Math.PI) f1 += ((float) Math.PI * 2F);
        float f2 = entity.oRot + f1 * partialTick;
        pose.mulPose(Axis.YP.rotation(-f2));
        pose.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float f3 = Mth.lerp(partialTick, entity.oFlip, entity.flip);
        float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = Mth.lerp(partialTick, entity.oOpen, entity.open);
        this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
        VertexConsumer consumer = BOOK_LOCATION.buffer(buffer, RenderType::entitySolid);
        this.bookModel.render(pose, consumer, packedLight, packedOverlay, -1);
        pose.popPose();
    }
}
