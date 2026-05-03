package cad.regulusxv.cad.client.renderer;

import cad.regulusxv.cad.CAD;
import cad.regulusxv.cad.block.entity.CadCalibrationTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CadCalibrationTableRenderer implements BlockEntityRenderer<CadCalibrationTableBlockEntity, CadCalibrationTableRenderer.RenderState> {
    private final ItemModelResolver itemModelResolver;

    public CadCalibrationTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            CadCalibrationTableBlockEntity blockEntity,
            RenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        long gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
        state.rotation = (gameTime + partialTicks) * 2.0f;
        this.itemModelResolver.updateForTopItem(state.cad, CAD.CAD_BASIC.get().getDefaultInstance(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.92, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.scale(0.45f, 0.45f, 0.45f);
        state.cad.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class RenderState extends BlockEntityRenderState {
        private final ItemStackRenderState cad = new ItemStackRenderState();
        private float rotation;
    }
}
