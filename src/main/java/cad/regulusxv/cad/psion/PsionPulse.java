package cad.regulusxv.cad.psion;

import cad.regulusxv.cad.CAD;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;

public final class PsionPulse {
    public static final int COST = 15;
    public static final int COOLDOWN_TICKS = 24;
    public static final double RANGE = 14.0D;
    public static final double RADIUS = 0.85D;
    public static final float DAMAGE = 6.0F;

    private PsionPulse() {
    }

    public static void cast(ServerPlayer player) {
        if (!hasCadEquipped(player)) {
            player.displayClientMessage(Component.literal("Equip a CAD to channel Psions"), true);
            return;
        }

        if (!PsionData.isUnlocked(player)) {
            player.displayClientMessage(Component.literal("Unlock Psions first"), true);
            return;
        }

        if (player.getCooldowns().isOnCooldown(CAD.CAD_BASIC.get())) {
            return;
        }

        if (!player.getAbilities().instabuild && !PsionData.consume(player, COST)) {
            player.displayClientMessage(Component.literal("Not enough Psions"), true);
            return;
        }

        player.getCooldowns().addCooldown(CAD.CAD_BASIC.get(), COOLDOWN_TICKS);
        player.swing(InteractionHand.MAIN_HAND, true);

        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 end = findBeamEnd(level, player, start, direction);
        double length = start.distanceTo(end);

        spawnBeam(level, start, direction, length);
        damageTargets(level, player, start, direction, length);
        player.displayClientMessage(PsionData.status(player), true);
    }

    private static Vec3 findBeamEnd(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 direction) {
        Vec3 fullReach = start.add(direction.scale(RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(start, fullReach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() == HitResult.Type.MISS) {
            return fullReach;
        }

        return blockHit.getLocation();
    }

    private static void damageTargets(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 direction, double length) {
        AABB beamBounds = new AABB(start, start.add(direction.scale(length))).inflate(RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, beamBounds, entity -> isValidTarget(player, entity));

        targets.stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .filter(entity -> isInsideBeam(entity, start, direction, length))
                .forEach(entity -> entity.hurt(level.damageSources().magic(), DAMAGE));
    }

    private static boolean isValidTarget(Player player, LivingEntity entity) {
        return entity != player && entity.isAlive() && !entity.isSpectator();
    }

    private static boolean isInsideBeam(LivingEntity entity, Vec3 start, Vec3 direction, double length) {
        Vec3 center = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        Vec3 relative = center.subtract(start);
        double alongBeam = relative.dot(direction);
        if (alongBeam < 0.0D || alongBeam > length) {
            return false;
        }

        double distanceFromBeam = relative.subtract(direction.scale(alongBeam)).length();
        return distanceFromBeam <= RADIUS + entity.getBbWidth() * 0.5D;
    }

    private static void spawnBeam(ServerLevel level, Vec3 start, Vec3 direction, double length) {
        for (double distance = 0.0D; distance <= length; distance += 0.35D) {
            Vec3 point = start.add(direction.scale(distance));
            level.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z, 3, 0.045D, 0.045D, 0.045D, 0.01D);

            if (((int) (distance * 10.0D)) % 7 == 0) {
                level.sendParticles(ParticleTypes.PORTAL, point.x, point.y, point.z, 2, 0.08D, 0.08D, 0.08D, 0.02D);
            }
        }
    }

    private static boolean hasCadEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(CAD.CAD_BASIC.get()))
                .isPresent();
    }
}
