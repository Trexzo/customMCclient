package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * Opt-in official Mojang 1.8.9 three-client test. The production selector,
 * module rotation, range and native attack controller remain unchanged.
 * This witness rejects stale loadedEntityList indices and wrong UUID owners.
 */
final class Minecraft189MultiTargetAcceptance {
    private static UUID firstUuid;
    private static UUID secondUuid;
    private static int stage;
    private static int disabledTicks;
    private static int chosenIndex = -1;
    private static boolean firstHit;
    private static boolean secondHit;

    private Minecraft189MultiTargetAcceptance() {}

    static void reset() {
        firstUuid = null; secondUuid = null;
        stage = 0; disabledTicks = 0;
        chosenIndex = -1; firstHit = false; secondHit = false;
    }

    static void observe(final Minecraft189HostRuntime host) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraMultiTarget")) return;
        if (firstUuid == null) {
            firstUuid = UUID.fromString(System.getProperty("custommc.acceptance.targetAUuid"));
            secondUuid = UUID.fromString(System.getProperty("custommc.acceptance.targetBUuid"));
            if (firstUuid.equals(secondUuid))
                throw new IllegalStateException("duplicate official target UUIDs");
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
        final Minecraft189TargetRotationState.Snapshot selected =
                host.targetRotationState().snapshot();
        chosenIndex = -1;
        if (!kinds.available() || !combat.available() || !positions.available()
                || !local.available() || !ids.available()
                || ids.entityCount() != kinds.entityCount()
                || ids.entityCount() != combat.entityCount()
                || ids.entityCount() != positions.entityCount()) {
            disabledTicks = 0;
            return;
        }
        int a = -1, b = -1, remoteCount = 0;
        for (int i = 0; i < kinds.entityCount(); i++) {
            if (!kinds.player(i) || kinds.localPlayer(i)) continue;
            remoteCount++;
            if (kinds.living(i) && combat.alive(i)) {
                if (firstUuid.equals(ids.at(i))) a = i;
                if (secondUuid.equals(ids.at(i))) b = i;
            }
        }
        if (remoteCount != 2 || a < 0 || b < 0 || a == b) {
            disabledTicks = 0;
            return;
        }
        final double adx = positions.x(a) - local.x();
        final double ady = positions.y(a) - local.y();
        final double adz = positions.z(a) - local.z();
        final double bdx = positions.x(b) - local.x();
        final double bdy = positions.y(b) - local.y();
        final double bdz = positions.z(b) - local.z();
        final double aDist = Math.sqrt(adx * adx + ady * ady + adz * adz);
        final double bDist = Math.sqrt(bdx * bdx + bdy * bdy + bdz * bdz);
        if (!Double.isFinite(aDist) || !Double.isFinite(bDist)) {
            disabledTicks = 0; return;
        }
        final boolean aCandidate = aDist >= 1.5D && aDist <= 3.25D
                && bDist >= 4.5D && nearest.found()
                && nearest.entityIndex() == a && selected.available()
                && selected.entityIndex() == a;
        final boolean bCandidate = bDist >= 1.5D && bDist <= 3.25D
                && aDist >= 4.5D && nearest.found()
                && nearest.entityIndex() == b && selected.available()
                && selected.entityIndex() == b;
        if (stage == 0) {
            if (host.featureCatalog().killAura().active())
                throw new IllegalStateException("multi-target Aura unexpectedly pre-enabled");
            disabledTicks = aCandidate ? disabledTicks + 1 : 0;
            if (disabledTicks >= 20) {
                host.featureCatalog().enableKillAuraForOfficialMultiTargetAcceptance();
                stage = 1;
                System.out.println("CUSTOMMC_189_MULTI_TWO_REAL_UUID_PLAYERS_PASS=YES");
                System.out.println("CUSTOMMC_189_MULTI_AURA_NORMAL_ENABLE_PASS=YES");
                System.out.flush();
            }
        } else if (stage == 1 && aCandidate) {
            chosenIndex = a;
        } else if (stage >= 1 && firstHit && bCandidate) {
            if (stage < 3) {
                stage = 3;
                System.out.println("CUSTOMMC_189_MULTI_TARGET_SWITCH_TO_UUID_B_PASS=YES");
                System.out.flush();
            }
            chosenIndex = b;
        } else if (stage >= 3 && aCandidate) {
            // Preserve this strict failure. The genuine server may have
            // delivered adjacent teleports while the graphical client's
            // World snapshots are still converging; the failing source
            // values must be visible before changing ANY acceptance gate.
            throw new IllegalStateException(
                    "stale original target became selected"
                    + " stage=" + stage
                    + " firstHit=" + firstHit
                    + " secondHit=" + secondHit
                    + " A.uuid=" + ids.at(a)
                    + " A.index=" + a
                    + " A.distance=" + aDist
                    + " A.xyz=" + positions.x(a) + ","
                    + positions.y(a) + "," + positions.z(a)
                    + " B.uuid=" + ids.at(b)
                    + " B.index=" + b
                    + " B.distance=" + bDist
                    + " B.xyz=" + positions.x(b) + ","
                    + positions.y(b) + "," + positions.z(b)
                    + " local.xyz=" + local.x() + ","
                    + local.y() + "," + local.z()
                    + " nearest.found=" + nearest.found()
                    + " nearest.index=" + nearest.entityIndex()
                    + " selected.available=" + selected.available()
                    + " selected.index=" + selected.entityIndex()
                    + " remoteCount=" + remoteCount);
        }
    }

    static void verifyOwnedClick(final Minecraft189HostRuntime host,
            final boolean playerHit, final int playerIndex) {
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final Minecraft189TargetRotationState.Snapshot selected =
                host.targetRotationState().snapshot();
        final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                host.nearestPlayerTargetState().snapshot();
        final Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        final Minecraft189WorldEntityCombatState.Snapshot combat =
                host.worldEntityCombatState().snapshot();
        // The observer's chosenIndex can be temporarily unavailable when
        // the genuine server moves TWO players between adjacent client
        // callbacks. Never treat that cached witness as attack authority:
        // revalidate current native UUID, real crosshair, living kind,
        // production selector and rotation for THIS exact callback instead.
        if (!host.featureCatalog().killAura().active()
                || !playerHit || playerIndex < 0
                || (chosenIndex >= 0 && chosenIndex != playerIndex)
                || !ids.available() || playerIndex >= ids.entityCount()
                || !kinds.available() || playerIndex >= kinds.entityCount()
                || !kinds.player(playerIndex) || kinds.localPlayer(playerIndex)
                || !kinds.living(playerIndex)
                || !combat.available() || playerIndex >= combat.entityCount()
                || !combat.alive(playerIndex)
                || !nearest.found() || nearest.entityIndex() != playerIndex
                || !selected.available() || selected.entityIndex() != playerIndex
                || !Double.isFinite(selected.distance())
                || selected.distance() > 3.5D) {
            throw new IllegalStateException(
                    "multi-target click without native owner: stage=" + stage
                    + " aura=" + host.featureCatalog().killAura().active()
                    + " playerHit=" + playerHit + " hitIndex=" + playerIndex
                    + " selectedIndex=" + chosenIndex
                    + " idsAvailable=" + ids.available()
                    + " idsCount=" + ids.entityCount()
                    + " hitUuid=" + ids.at(playerIndex)
                    + " targetAvailable=" + selected.available()
                    + " targetIndex=" + selected.entityIndex()
                    + " firstHit=" + firstHit);
        }
        final UUID current = ids.at(playerIndex);
        if (stage == 1 && firstUuid.equals(current)) {
            if (!firstHit) {
                firstHit = true;
                System.out.println("CUSTOMMC_189_MULTI_UUID_A_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (stage >= 3 && firstHit && secondUuid.equals(current)) {
            if (!secondHit) {
                secondHit = true;
                System.out.println("CUSTOMMC_189_MULTI_UUID_B_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else {
            throw new IllegalStateException("wrong UUID attacked during real target switch");
        }
    }
}
