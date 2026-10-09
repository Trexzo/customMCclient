package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Source-mapped KillAura candidate arbitration. Other modules keep the
 * shared nearest-player semantics; only active KillAura opts into this.
 */
final class Minecraft189KillAuraTargetSelector {
    private Minecraft189KillAuraTargetSelector() {}

    static void select(
            final Minecraft189KillAuraModule aura,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189NearestPlayerTargetState target) {
        select(aura, local, rotation, positions, kinds, combat, null, target);
    }

    static void select(
            final Minecraft189KillAuraModule aura,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189AntiBotModule antiBot,
            final Minecraft189NearestPlayerTargetState target) {
        if (target == null) return;
        if (aura == null || local == null || rotation == null
                || positions == null || kinds == null || combat == null
                || !local.available() || !rotation.available()
                || !positions.available() || !kinds.available()
                || !combat.available()
                || positions.entityCount() != kinds.entityCount()
                || positions.entityCount() != combat.entityCount()) {
            target.clear();
            return;
        }

        final double range = aura.rangeSetting().get();
        final double fov = aura.fovSetting().get();
        final boolean crosshair = aura.prioritizeCrosshairSetting().get();
        final boolean skipHurt = aura.switchHurtTargetsSetting().get();
        final int acceptedHurtTime = aura.maxSwitchHurtTicksSetting().get();
        target.update(local, positions, kinds, 0.0D, range,
                candidate -> combat.alive(candidate.entityIndex())
                        && (!skipHurt
                                || combat.hurtTime(candidate.entityIndex()) <= acceptedHurtTime)
                        && (antiBot == null
                                || antiBot.permits(candidate.entityIndex(), combat))
                        && Double.isFinite(
                            angularScore(local, rotation, candidate, fov)),
                candidate -> crosshair
                        ? angularScore(local, rotation, candidate, fov)
                        : candidate.distanceSquared());
    }

    private static double angularScore(
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189NearestPlayerTargetState.Snapshot candidate,
            final double maxFov) {
        final double dx = candidate.x() - local.x();
        final double dy = candidate.y() - local.y();
        final double dz = candidate.z() - local.z();
        final double horizontal = Math.hypot(dx, dz);
        if (!(horizontal > 0.0D) || !Double.isFinite(horizontal)
                || !Double.isFinite(dy)) return Double.NaN;
        final double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0D;
        final double pitch = -Math.toDegrees(Math.atan2(dy, horizontal));
        final double deltaYaw = wrap(yaw - rotation.yaw());
        final double deltaPitch = pitch - rotation.pitch();
        if (!Double.isFinite(deltaYaw) || !Double.isFinite(deltaPitch)
                || Math.abs(deltaYaw) > maxFov
                || Math.abs(deltaPitch) > maxFov) return Double.NaN;
        return deltaYaw * deltaYaw + deltaPitch * deltaPitch;
    }

    private static double wrap(final double angle) {
        double value = angle % 360.0D;
        if (value < -180.0D) value += 360.0D;
        if (value >= 180.0D) value -= 360.0D;
        return value;
    }
}
