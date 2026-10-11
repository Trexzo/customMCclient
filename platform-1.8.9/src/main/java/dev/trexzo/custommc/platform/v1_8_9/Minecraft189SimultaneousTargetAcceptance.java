package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * Opt-in, exact-native Mojang 1.8.9 simultaneous eligible-player witness.
 * Never a production target selector. Attack authority always comes from the
 * current live UUID/kind/combat/position/nearest/rotation and crosshair.
 */
final class Minecraft189SimultaneousTargetAcceptance {
    private static UUID firstUuid;
    private static UUID secondUuid;
    private static int phase;
    private static int disabledTicks;
    private static int stableAAfterHitTicks;
    private static boolean stableAAfterHitReported;
    private static boolean firstOwnedClick;
    private static boolean secondOwnedClick;

    private Minecraft189SimultaneousTargetAcceptance() {}

    static void reset() {
        firstUuid = null;
        secondUuid = null;
        phase = 0;
        disabledTicks = 0;
        stableAAfterHitTicks = 0;
        stableAAfterHitReported = false;
        firstOwnedClick = false;
        secondOwnedClick = false;
    }

    static void observe(final Minecraft189HostRuntime host) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraSimultaneous"))
            return;
        final Evidence evidence = read(host);
        if (phase == 0) {
            if (host.featureCatalog().killAura().active())
                throw new IllegalStateException("simultaneous Aura pre-enabled");
            disabledTicks = evidence != null && evidence.aNearest
                    ? disabledTicks + 1 : 0;
            if (disabledTicks >= 20) {
                host.featureCatalog()
                        .enableKillAuraForOfficialSimultaneousAcceptance();
                phase = 1;
                System.out.println(
                        "CUSTOMMC_189_SIMULTANEOUS_TWO_NATIVE_ELIGIBLE_PLAYERS_PASS=20");
                System.out.println(
                        "CUSTOMMC_189_SIMULTANEOUS_AURA_NORMAL_ENABLE_PASS=YES");
                System.out.flush();
            }
        } else if (phase == 1 && firstOwnedClick
                && evidence != null && evidence.bNearest) {
            phase = 2;
            System.out.println(
                    "CUSTOMMC_189_SIMULTANEOUS_NATIVE_UUID_B_PRIORITY_SWITCH_PASS=YES");
            System.out.flush();
        }
        if (phase == 1 && firstOwnedClick && !stableAAfterHitReported) {
            stableAAfterHitTicks = evidence != null && evidence.aNearest
                    ? stableAAfterHitTicks + 1 : 0;
            if (stableAAfterHitTicks >= 20) {
                stableAAfterHitReported = true;
                System.out.println(
                        "CUSTOMMC_189_SIMULTANEOUS_A_STILL_NEAREST_WITH_B_ELIGIBLE_PASS=20");
                System.out.flush();
            }
        }
        // Do not cache a candidate index: a native loadedEntityList may be
        // rebuilt between observing and attempting the very next attack.
    }

    static void verifyOwnedClick(final Minecraft189HostRuntime host,
            final boolean playerHit, final int playerIndex) {
        final Evidence evidence = read(host);
        if (!host.featureCatalog().killAura().active()
                || !playerHit || evidence == null
                || !evidence.currentValid(playerIndex)) {
            throw new IllegalStateException(
                    "simultaneous click lacks current living native UUID owner"
                    + " phase=" + phase + " hit=" + playerHit
                    + " hitIndex=" + playerIndex
                    + " evidence=" + (evidence != null));
        }
        if (phase == 1 && evidence.aNearest && playerIndex == evidence.aIndex) {
            if (!firstOwnedClick) {
                firstOwnedClick = true;
                System.out.println(
                        "CUSTOMMC_189_SIMULTANEOUS_UUID_A_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (phase == 2 && firstOwnedClick
                && evidence.bNearest && playerIndex == evidence.bIndex) {
            if (!secondOwnedClick) {
                secondOwnedClick = true;
                phase = 3;
                System.out.println(
                        "CUSTOMMC_189_SIMULTANEOUS_UUID_B_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (phase >= 3
                && ((evidence.aNearest && playerIndex == evidence.aIndex)
                    || (evidence.bNearest && playerIndex == evidence.bIndex))) {
            // After both proved attacks, ordinary nearest arbitration may
            // change as real hurt/knockback moves living native players.
        } else {
            throw new IllegalStateException(
                    "simultaneous attack crossed UUID/priority phase=" + phase
                    + " index=" + playerIndex
                    + " aNear=" + evidence.aNearest
                    + " bNear=" + evidence.bNearest);
        }
    }

    private static Evidence read(final Minecraft189HostRuntime host) {
        if (firstUuid == null) {
            firstUuid = UUID.fromString(
                    System.getProperty("custommc.acceptance.targetAUuid"));
            secondUuid = UUID.fromString(
                    System.getProperty("custommc.acceptance.targetBUuid"));
            if (firstUuid.equals(secondUuid))
                throw new IllegalStateException("duplicate real player UUID");
        }
        final Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        final Minecraft189WorldEntityCombatState.Snapshot combat =
                host.worldEntityCombatState().snapshot();
        final Minecraft189WorldEntityPositionState.Snapshot positions =
                host.worldEntityPositionState().snapshot();
        final Minecraft189PlayerPositionState.Snapshot local =
                host.playerPositionState().snapshot();
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                host.nearestPlayerTargetState().snapshot();
        final Minecraft189TargetRotationState.Snapshot rotation =
                host.targetRotationState().snapshot();
        if (!ids.available() || !kinds.available() || !combat.available()
                || !positions.available() || !local.available()
                || ids.entityCount() != kinds.entityCount()
                || ids.entityCount() != combat.entityCount()
                || ids.entityCount() != positions.entityCount()) return null;
        int a = -1, b = -1, remotes = 0;
        for (int index = 0; index < kinds.entityCount(); index++) {
            if (!kinds.player(index) || kinds.localPlayer(index)) continue;
            remotes++;
            if (kinds.living(index) && combat.alive(index)) {
                if (firstUuid.equals(ids.at(index))) a = index;
                if (secondUuid.equals(ids.at(index))) b = index;
            }
        }
        if (remotes != 2 || a < 0 || b < 0 || a == b) return null;
        final double adx = positions.x(a) - local.x();
        final double ady = positions.y(a) - local.y();
        final double adz = positions.z(a) - local.z();
        final double bdx = positions.x(b) - local.x();
        final double bdy = positions.y(b) - local.y();
        final double bdz = positions.z(b) - local.z();
        final double adist = Math.sqrt(adx * adx + ady * ady + adz * adz);
        final double bdist = Math.sqrt(bdx * bdx + bdy * bdy + bdz * bdz);
        if (!Double.isFinite(adist) || !Double.isFinite(bdist)
                || adist < 1.5D || adist > 3.25D
                || bdist < 1.5D || bdist > 3.25D) return null;
        // Require BOTH living players to remain eligible by actual distance,
        // not one eligible while another is safely far away (M412's scope).
        final boolean aPriority = adist + 0.12D < bdist;
        final boolean bPriority = bdist + 0.12D < adist;
        final boolean aSelected = aPriority && nearest.found()
                && nearest.entityIndex() == a && rotation.available()
                && rotation.entityIndex() == a;
        final boolean bSelected = bPriority && nearest.found()
                && nearest.entityIndex() == b && rotation.available()
                && rotation.entityIndex() == b;
        return new Evidence(a, b, aSelected, bSelected, nearest, rotation,
                kinds, combat, ids);
    }

    private static final class Evidence {
        private final int aIndex, bIndex;
        private final boolean aNearest, bNearest;
        private final Minecraft189NearestPlayerTargetState.Snapshot nearest;
        private final Minecraft189TargetRotationState.Snapshot rotation;
        private final Minecraft189WorldEntityKindState.Snapshot kinds;
        private final Minecraft189WorldEntityCombatState.Snapshot combat;
        private final Minecraft189WorldEntityUuidState.Snapshot ids;

        private Evidence(final int a, final int b, final boolean aNearest,
                final boolean bNearest,
                final Minecraft189NearestPlayerTargetState.Snapshot nearest,
                final Minecraft189TargetRotationState.Snapshot rotation,
                final Minecraft189WorldEntityKindState.Snapshot kinds,
                final Minecraft189WorldEntityCombatState.Snapshot combat,
                final Minecraft189WorldEntityUuidState.Snapshot ids) {
            this.aIndex = a;
            this.bIndex = b;
            this.aNearest = aNearest;
            this.bNearest = bNearest;
            this.nearest = nearest;
            this.rotation = rotation;
            this.kinds = kinds;
            this.combat = combat;
            this.ids = ids;
        }

        private boolean currentValid(final int index) {
            return index >= 0 && index < ids.entityCount()
                    && ((index == aIndex && aNearest
                            && firstUuid.equals(ids.at(index)))
                        || (index == bIndex && bNearest
                            && secondUuid.equals(ids.at(index))))
                    && kinds.player(index) && !kinds.localPlayer(index)
                    && kinds.living(index) && combat.alive(index)
                    && nearest.found() && nearest.entityIndex() == index
                    && rotation.available() && rotation.entityIndex() == index
                    && Double.isFinite(rotation.distance())
                    && rotation.distance() <= 3.5D;
        }
    }
}
