package dev.trexzo.custommc.platform.v1_8_9;

/**
 * An independent source-native FOV guard immediately before automated Aura
 * click admission. Revalidates the actual current World entity index/position
 * against the local original player yaw; a stale or mismatched selected
 * rotation snapshot may NEVER authorize a behind-the-player attack.
 *
 * This does not alter vanilla manual clicks, target selection, or server
 * packets. Missing, inconsistent or non-finite source data fails closed.
 */
final class Minecraft189KillAuraNativeFovGate {
    private Minecraft189KillAuraNativeFovGate() { }

    static boolean permits(
            final int targetIndex, final double maxYawDegrees,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds) {
        if (rotation == null || local == null || positions == null || kinds == null
                || !rotation.available() || !local.available()
                || !positions.available() || !kinds.available()
                || positions.entityCount() != kinds.entityCount()
                || targetIndex < 0 || targetIndex >= positions.entityCount()
                || !kinds.player(targetIndex) || !kinds.living(targetIndex)
                || kinds.localPlayer(targetIndex)
                || !Double.isFinite(maxYawDegrees)
                || maxYawDegrees < 1.0D || maxYawDegrees > 180.0D) {
            return false;
        }
        final double dx = positions.x(targetIndex) - local.x();
        final double dz = positions.z(targetIndex) - local.z();
        final double horizontal = Math.hypot(dx, dz);
        if (!Double.isFinite(horizontal) || horizontal <= 0.0D
                || !Float.isFinite(rotation.yaw())) return false;
        final double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0D;
        double yawDifference = (targetYaw - rotation.yaw()) % 360.0D;
        if (yawDifference < -180.0D) yawDifference += 360.0D;
        if (yawDifference >= 180.0D) yawDifference -= 360.0D;
        return Double.isFinite(yawDifference)
                && Math.abs(yawDifference) <= maxYawDegrees;
    }
}
