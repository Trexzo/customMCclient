package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

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
        select(aura, local, rotation, positions, kinds, combat, null, null, target);
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
        select(aura, local, rotation, positions, kinds, combat, antiBot, null, null, target);
    }

    static void select(
            final Minecraft189KillAuraModule aura,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189AntiBotModule antiBot,
            final Minecraft189TeamGuardModule teamGuard,
            final Minecraft189NearestPlayerTargetState target) {
        select(aura, local, rotation, positions, kinds, combat,
                antiBot, teamGuard, null, null, target);
    }

    static void select(
            final Minecraft189KillAuraModule aura,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189AntiBotModule antiBot,
            final Minecraft189TeamGuardModule teamGuard,
            final Minecraft189WorldEntityUuidState.Snapshot identities,
            final Minecraft189NearestPlayerTargetState target) {
        select(aura, local, rotation, positions, kinds, combat,
                antiBot, teamGuard, null, identities, null, null, target);
    }

    static void select(
            final Minecraft189KillAuraModule aura,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189AntiBotModule antiBot,
            final Minecraft189TeamGuardModule teamGuard,
            final Minecraft189FriendGuardModule friendGuard,
            final Minecraft189WorldEntityUuidState.Snapshot identities,
            final Minecraft189NearestPlayerTargetState target) {
        select(aura, local, rotation, positions, kinds, combat,
                antiBot, teamGuard, friendGuard, identities, null, null, target);
    }

    static void select(
            final Minecraft189KillAuraModule aura,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189AntiBotModule antiBot,
            final Minecraft189TeamGuardModule teamGuard,
            final Minecraft189FriendGuardModule friendGuard,
            final Minecraft189WorldEntityUuidState.Snapshot identities,
            final Minecraft189WallCheckModule wallCheck,
            final Minecraft189WorldEntityVisibilityState.Snapshot visibility,
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
            if (aura != null) aura.rememberSelectedTarget(null);
            return;
        }

        final boolean lockEnabled = aura.active() && aura.lockTargetSetting().get();
        if ((lockEnabled || (friendGuard != null && friendGuard.requiresIdentity()))
                && (identities == null || !identities.available()
                    || identities.entityCount() != positions.entityCount())) {
            target.clear();
            aura.rememberSelectedTarget(null);
            return;
        }
        if (wallCheck != null && wallCheck.active()
                && (visibility == null || !visibility.available()
                    || visibility.entityCount() != positions.entityCount())) {
            target.clear();
            aura.rememberSelectedTarget(null);
            return;
        }
        final UUID lockedUuid = aura.lockedTargetUuid();
        final double range = aura.rangeSetting().get();
        final double fov = aura.fovSetting().get();
        // A rejoined/closer remote player must not steal an existing
        // UUID-locked survivor merely because its last yaw temporarily
        // falls outside the configured FOV (or it is still hurt).
        // Keep the identity only while its ORIGINAL source World record
        // remains alive, in range and permitted by every safety policy.
        // FOV/hurt remain mandatory before publishing any aim target.
        final boolean pinnedOwner = lockEnabled && lockedUuid != null
                && verifiedLockRetainable(
                        lockedUuid, local, positions, kinds, combat,
                        identities, range, antiBot, teamGuard,
                        friendGuard, wallCheck, visibility);
        final boolean crosshair = aura.prioritizeCrosshairSetting().get();
        final boolean skipHurt = aura.switchHurtTargetsSetting().get();
        final int acceptedHurtTime = aura.maxSwitchHurtTicksSetting().get();
        target.update(local, positions, kinds, 0.0D, range,
                candidate -> (!pinnedOwner || lockedUuid.equals(
                                    identities.at(candidate.entityIndex())))
                        && combat.alive(candidate.entityIndex())
                        && (!skipHurt
                                || combat.hurtTime(candidate.entityIndex()) <= acceptedHurtTime)
                        && (antiBot == null
                                || antiBot.permits(candidate.entityIndex(), combat))
                        && (teamGuard == null
                                || teamGuard.permits(candidate.entityIndex(), combat))
                        && (friendGuard == null
                                || friendGuard.permits(candidate.entityIndex(), identities))
                        && (wallCheck == null
                                || wallCheck.permits(candidate.entityIndex(), visibility))
                        && Double.isFinite(
                            angularScore(local, rotation, candidate, fov)),
                candidate -> lockedUuid != null
                        && lockedUuid.equals(identities.at(candidate.entityIndex()))
                        ? 0.0D
                        : (crosshair
                                ? angularScore(local, rotation, candidate, fov)
                                : candidate.distanceSquared()));
        // Revalidate each tick against the current living/range/FOV/AntiBot
        // filters; never reuse a stale index without reselecting it.
        final Minecraft189NearestPlayerTargetState.Snapshot selected =
                target.snapshot();
        // A temporarily out-of-FOV/hurt owner is not a selectable target:
        // target.update published found=false, so neither aim nor melee is
        // authorized. Its UUID may remain reserved for a later *fresh*
        // native tick. An unloaded/dead/out-of-range/disallowed owner is
        // released and the normal eligible-candidate scan may proceed.
        aura.rememberSelectedTarget(
                selected.found() && lockEnabled
                        ? identities.at(selected.entityIndex())
                        : (pinnedOwner ? lockedUuid : null));
    }

    private static boolean verifiedLockRetainable(
            final UUID lockedUuid,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189WorldEntityPositionState.Snapshot positions,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189WorldEntityUuidState.Snapshot identities,
            final double range,
            final Minecraft189AntiBotModule antiBot,
            final Minecraft189TeamGuardModule teamGuard,
            final Minecraft189FriendGuardModule friendGuard,
            final Minecraft189WallCheckModule wallCheck,
            final Minecraft189WorldEntityVisibilityState.Snapshot visibility) {
        if (lockedUuid == null || identities == null || !identities.available()
                || identities.entityCount() != positions.entityCount())
            return false;
        int ownedIndex = -1;
        for (int i = 0; i < identities.entityCount(); i++) {
            if (!lockedUuid.equals(identities.at(i))) continue;
            // Duplicate loaded UUID identities must never select one
            // arbitrary entity/list index as the native attack owner.
            if (ownedIndex >= 0) return false;
            ownedIndex = i;
        }
        if (ownedIndex < 0 || !kinds.player(ownedIndex)
                || kinds.localPlayer(ownedIndex)
                || !kinds.living(ownedIndex) || !combat.alive(ownedIndex))
            return false;
        final double dx = positions.x(ownedIndex) - local.x();
        final double dy = positions.y(ownedIndex) - local.y();
        final double dz = positions.z(ownedIndex) - local.z();
        final double distanceSquared = dx*dx + dy*dy + dz*dz;
        if (!Double.isFinite(distanceSquared)
                || distanceSquared > range*range)
            return false;
        return (antiBot == null || antiBot.permits(ownedIndex, combat))
                && (teamGuard == null || teamGuard.permits(ownedIndex, combat))
                && (friendGuard == null
                        || friendGuard.permits(ownedIndex, identities))
                && (wallCheck == null
                        || wallCheck.permits(ownedIndex, visibility));
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
