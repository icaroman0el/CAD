package cad.regulusxv.cad.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class CadBeamRenderer {
    private static final List<ActiveBeam> BEAMS = new ArrayList<>();
    private static final double CORE_RADIUS = 0.06D;
    private static final double GLOW_RADIUS = 0.17D;

    private CadBeamRenderer() {
    }

    public static void addBeam(Vec3 start, Vec3 end, int durationTicks) {
        BEAMS.add(new ActiveBeam(start, end, Math.max(1, durationTicks), Math.max(1, durationTicks)));
    }

    public static void tick() {
        Iterator<ActiveBeam> iterator = BEAMS.iterator();
        while (iterator.hasNext()) {
            ActiveBeam beam = iterator.next();
            beam.remainingTicks--;
            if (beam.remainingTicks <= 0) {
                iterator.remove();
            }
        }
    }

    public static void render(RenderLevelStageEvent event) {
        if (BEAMS.isEmpty() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();

        for (ActiveBeam beam : BEAMS) {
            renderBeam(matrix, consumer, beam, camera);
        }

        poseStack.popPose();
        bufferSource.endBatch(RenderType.lightning());
    }

    private static void renderBeam(Matrix4f matrix, VertexConsumer consumer, ActiveBeam beam, Vec3 camera) {
        Vec3 direction = beam.end.subtract(beam.start);
        double length = direction.length();
        if (length <= 0.01D) {
            return;
        }

        Vec3 forward = direction.scale(1.0D / length);
        Vec3 midpoint = beam.start.add(direction.scale(0.5D));
        Vec3 view = camera.subtract(midpoint).normalize();
        Vec3 side = forward.cross(view);
        if (side.lengthSqr() < 1.0E-4D) {
            side = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        }
        if (side.lengthSqr() < 1.0E-4D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }

        side = side.normalize();
        Vec3 vertical = forward.cross(side).normalize();
        float fade = Math.min(1.0F, beam.remainingTicks / (float) beam.durationTicks);
        float pulse = 0.75F + 0.25F * (float) Math.sin((beam.durationTicks - beam.remainingTicks) * 0.9F);

        renderCrossBeam(matrix, consumer, beam.start, beam.end, side, GLOW_RADIUS, 0.55F, 0.0F, 1.0F, 0.18F * fade);
        renderCrossBeam(matrix, consumer, beam.start, beam.end, vertical, GLOW_RADIUS, 0.55F, 0.0F, 1.0F, 0.14F * fade);
        renderCrossBeam(matrix, consumer, beam.start, beam.end, side, CORE_RADIUS, 0.98F, 0.62F, 1.0F, 0.70F * fade * pulse);
        renderCrossBeam(matrix, consumer, beam.start, beam.end, vertical, CORE_RADIUS, 0.98F, 0.62F, 1.0F, 0.55F * fade * pulse);
    }

    private static void renderCrossBeam(
            Matrix4f matrix,
            VertexConsumer consumer,
            Vec3 start,
            Vec3 end,
            Vec3 axis,
            double radius,
            float red,
            float green,
            float blue,
            float alpha) {
        Vec3 offset = axis.scale(radius);
        addVertex(matrix, consumer, start.add(offset), red, green, blue, alpha);
        addVertex(matrix, consumer, start.subtract(offset), red, green, blue, alpha);
        addVertex(matrix, consumer, end.subtract(offset), red, green, blue, alpha);
        addVertex(matrix, consumer, end.add(offset), red, green, blue, alpha);
    }

    private static void addVertex(Matrix4f matrix, VertexConsumer consumer, Vec3 pos, float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).setColor(red, green, blue, alpha);
    }

    private static final class ActiveBeam {
        private final Vec3 start;
        private final Vec3 end;
        private final int durationTicks;
        private int remainingTicks;

        private ActiveBeam(Vec3 start, Vec3 end, int durationTicks, int remainingTicks) {
            this.start = start;
            this.end = end;
            this.durationTicks = durationTicks;
            this.remainingTicks = remainingTicks;
        }
    }
}
