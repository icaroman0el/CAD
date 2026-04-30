package cad.regulusxv.cad.client.renderer;

import cad.regulusxv.cad.CAD;
import cad.regulusxv.cad.block.entity.CadCalibrationTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CadCalibrationTableRenderer implements BlockEntityRenderer<CadCalibrationTableBlockEntity> {
    public CadCalibrationTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CadCalibrationTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = CAD.CAD_BASIC.get().getDefaultInstance();
        long gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
        float rotation = (gameTime + partialTick) * 2.0f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.92, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.scale(0.45f, 0.45f, 0.45f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, blockEntity.getLevel(), 0);
        poseStack.popPose();
    }
}
