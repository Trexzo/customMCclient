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
    private static boolean firstOwnedClick;
    private static boolean secondOwnedClick;

    private Minecraft189SimultaneousTargetAcceptance() {}

    static void reset() {
        firstUuid = null;
        secondUuid = null;
        phase = 0;
        disabledTicks = 0;
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
        // Do not cache a candidate index: a native loadedEntityList may be
        // rebuilt between observing and attempting the very next attack.
    }

    static void verifyOwnedClick(final Minecraft189HostRuntime host,
            final boolean playerHit, final int playerIndex) {
        // The other genuine client can temporarily disappear from the
        // loaded list or move one tick before its new teleport packet.
        // That cannot authorize ANY attack: verify the clicked owner against
        // fresh native UUID, living player, actual crosshair, production
        // selection/rotation and range ON THIS EXACT CALLBACK.
        //
        // Both-player eligibility and priority are established separately
        // by consecutive observe() gates; an incomplete transition snapshot
        // can never advance a milestone or certify a server hit.
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        final Minecraft189WorldEntityCombatState.Snapshot combat =
                host.worldEntityCombatState().snapshot();
        final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                host.nearestPlayerTargetState().snapshot();
        final Minecraft189TargetRotationState.Snapshot rotation =
                host.targetRotationState().snapshot();
        if (!host.featureCatalog().killAura().active()
                || !playerHit || playerIndex < 0
                || !ids.available() || playerIndex >= ids.entityCount()
                || !kinds.available() || playerIndex >= kinds.entityCount()
                || !kinds.player(playerIndex) || kinds.localPlayer(playerIndex)
                || !kinds.living(playerIndex)
                || !combat.available() || playerIndex >= combat.entityCount()
                || !combat.alive(playerIndex)
                || !nearest.found() || nearest.entityIndex() != playerIndex
                || !rotation.available() || rotation.entityIndex() != playerIndex
                || !Double.isFinite(rotation.distance())
                || rotation.distance() > 3.5D) {
            throw new IllegalStateException(
                    "simultaneous native attack owner invalid"
                    + " phase=" + phase + " hit=" + playerHit
                    + " index=" + playerIndex
                    + " uuid=" + ids.at(playerIndex)
                    + " nearest=" + nearest.entityIndex()
                    + " selected=" + rotation.entityIndex());
        }
        final UUID current = ids.at(playerIndex);
        if (phase == 1 && firstOwnedClick && secondUuid.equals(current)) {
            // Real client action selection can advance between the world
            // snapshot callback and the attack callback. Recognize B ONLY
            // when the CURRENT full native world still establishes BOTH
            // eligible living player UUIDs and B is nearer/selected.
            final StringBuilder sameRead = new StringBuilder();
            final Evidence switched = read(host, sameRead);
            if (switched == null || !switched.bNearest
                    || playerIndex != switched.bIndex) {
                throw new IllegalStateException(
                        "simultaneous early B click without both-eligible "
                        + "live native priority evidence"
                        + " rejectedSameRead=" + m276FailureAtOriginalRead(
                                sameRead.toString(), playerIndex,
                                switched == null ? -1 : switched.bIndex)
                        // This separate late source read can differ from the
                        // REJECTING same-read witness above. It is diagnostic
                        // only, not a second chance to authorize the click.
                        + m274EarlyBClickSourceDiagnostics(host, playerIndex));
            }
            phase = 2;
            System.out.println(
                    "CUSTOMMC_189_SIMULTANEOUS_NATIVE_UUID_B_PRIORITY_SWITCH_PASS=YES");
            System.out.flush();
        }
        if (phase == 1 && firstUuid.equals(current)) {
            if (!firstOwnedClick) {
                firstOwnedClick = true;
                System.out.println(
                        "CUSTOMMC_189_SIMULTANEOUS_UUID_A_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (phase == 2 && firstOwnedClick
                && secondUuid.equals(current)) {
            if (!secondOwnedClick) {
                secondOwnedClick = true;
                phase = 3;
                System.out.println(
                        "CUSTOMMC_189_SIMULTANEOUS_UUID_B_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (phase == 2 && firstOwnedClick
                && firstUuid.equals(current)) {
            // A genuine knockback or late movement can move the nearest
            // target back from B to A before B's required second server
            // hit. This is *not* proof B was hit. Permit only the fresh
            // source world with BOTH real alive UUIDs still eligible and
            // source nearest/rotation explicitly selecting A.
            final Evidence renewed = read(host);
            if (!phaseTwoFirstReturnPermitted(phase, firstOwnedClick,
                    current, firstUuid, playerIndex,
                    renewed == null ? -1 : renewed.aIndex,
                    renewed != null && renewed.aNearest
                            && renewed.currentValid(playerIndex))) {
                throw new IllegalStateException(
                        "simultaneous first UUID reprioritized without "
                        + "both-live-native-owner evidence phase=" + phase
                        + " uuid=" + current + " index=" + playerIndex);
            }
            // Intentionally no phase change, no B PASS marker. The
            // dedicated server still MUST prove a separate damage event
            // to B before this acceptance may finish.
        } else if (phase >= 3
                && (firstUuid.equals(current) || secondUuid.equals(current))) {
            // After proving both official server-owned attacks, real
            // knockback may legitimately change the nearest eligible player.
        } else {
            throw new IllegalStateException(
                    "simultaneous attack crossed source-native UUID/phase="
                    + phase + " uuid=" + current
                    + m280UnexpectedPhaseDiagnostic(phase, firstOwnedClick,
                            current, firstUuid, secondUuid, playerIndex,
                            ids.entityCount(), kinds.entityCount(),
                            combat.entityCount(), nearest.entityIndex(),
                            rotation.entityIndex(),
                            kinds.player(playerIndex), combat.alive(playerIndex)));
        }
    }

    /**
     * V280 exact first-rejecting UUID/phase and selected target witness.
     * Consumes only the native snapshots ALREADY read and accepted for
     * index validity in verifyOwnedClick. No second host/native read, no
     * mutation of phase, ownership, or acceptance policy.
     */
    static String m280UnexpectedPhaseDiagnostic(final int rejectingPhase,
            final boolean firstOwned,
            final UUID clickedUuid, final UUID expectedA,
            final UUID expectedB, final int clickedIndex,
            final int uuidCount, final int kindCount, final int combatCount,
            final int nearestIndex, final int rotationIndex,
            final boolean clickedPlayer, final boolean clickedAlive) {
        final String reason;
        if (rejectingPhase == 1 && !firstOwned
                && expectedB != null && expectedB.equals(clickedUuid)) {
            reason = "B-click-before-first-A-owned-attack";
        } else if (rejectingPhase == 2 && !firstOwned) {
            reason = "phase-two-without-A-owned-attack";
        } else if (expectedA != null && expectedA.equals(clickedUuid)) {
            reason = "A-click-outside-allowed-phase";
        } else if (expectedB != null && expectedB.equals(clickedUuid)) {
            reason = "B-click-outside-allowed-phase";
        } else {
            reason = "non-A-B-native-UUID";
        }
        return " sameReadReason=" + reason
                + " sameReadPhase=" + rejectingPhase
                + " sameReadFirstOwnedClick=" + firstOwned
                + " sameReadClickedIndex=" + clickedIndex
                + " sameReadClickedUuid=" + clickedUuid
                + " sameReadExpectedA=" + expectedA
                + " sameReadExpectedB=" + expectedB
                + " sameReadUuidCount=" + uuidCount
                + " sameReadKindCount=" + kindCount
                + " sameReadCombatCount=" + combatCount
                + " sameReadNearest=" + nearestIndex
                + " sameReadRotation=" + rotationIndex
                + " sameReadClickedPlayer=" + clickedPlayer
                + " sameReadClickedAlive=" + clickedAlive;
    }

    /**
     * Same-rejecting-read reason retains its provenance even when the later
     * V274 diagnostic source reads a different World position/target.
     */
    static String m276FailureAtOriginalRead(
            final String originalReadReason, final int clickedIndex,
            final int originalReadBIndex) {
        if (originalReadReason == null || originalReadReason.isEmpty())
            return "original-rejection-diagnostic-missing";
        final String accepted =
                "original-both-eligible-B-priority-present-on-diagnostic-read";
        if (!originalReadReason.startsWith(accepted))
            return originalReadReason;
        if (clickedIndex < 0 || clickedIndex != originalReadBIndex)
            return "original-B-index-changed-before-click"
                    + " clicked=" + clickedIndex
                    + " originalB=" + originalReadBIndex
                    + " " + originalReadReason;
        return "original-reject-with-valid-source-needs-investigation"
                + " " + originalReadReason;
    }

    /**
     * V274 crash-only classification of the actual late native B switch.
     * The existing BOTH-eligible rule never consumes this output; it only
     * makes the original fail-closed crash explain which source condition
     * was absent at an independent read of the actual game World.
     */
    static String m274PriorityReason(
            final boolean aligned, final int remotePlayers,
            final int aIndex, final int bIndex,
            final boolean aAlive, final boolean bAlive,
            final double aDistance, final double bDistance,
            final boolean bNearest, final boolean bRotation) {
        if (!aligned) return "original-world-snapshots-unavailable-or-misaligned";
        if (remotePlayers != 2) return "original-remote-player-count-not-two";
        if (aIndex < 0) return "original-A-UUID-not-loaded";
        if (bIndex < 0) return "original-B-UUID-not-loaded";
        if (!aAlive) return "original-A-not-living-and-alive";
        if (!bAlive) return "original-B-not-living-and-alive";
        if (!Double.isFinite(aDistance) || aDistance < 1D
                || aDistance > 3.5D) return "original-A-outside-native-reach";
        if (!Double.isFinite(bDistance) || bDistance < 1D
                || bDistance > 3.5D) return "original-B-outside-native-reach";
        if (!(bDistance + .12D < aDistance))
            return "original-B-not-0.12-closer-than-A";
        if (!bNearest) return "original-nearest-target-not-B";
        if (!bRotation) return "original-selected-rotation-not-B";
        return "original-both-eligible-B-priority-present-on-diagnostic-read";
    }

    private static String m274EarlyBClickSourceDiagnostics(
            final Minecraft189HostRuntime host, final int clickIndex) {
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        final Minecraft189WorldEntityCombatState.Snapshot combat =
                host.worldEntityCombatState().snapshot();
        final Minecraft189WorldEntityPositionState.Snapshot positions =
                host.worldEntityPositionState().snapshot();
        final Minecraft189PlayerPositionState.Snapshot local =
                host.playerPositionState().snapshot();
        final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                host.nearestPlayerTargetState().snapshot();
        final Minecraft189TargetRotationState.Snapshot rotation =
                host.targetRotationState().snapshot();
        final boolean aligned = ids.available() && kinds.available()
                && combat.available() && positions.available()
                && local.available()
                && ids.entityCount() == kinds.entityCount()
                && ids.entityCount() == combat.entityCount()
                && ids.entityCount() == positions.entityCount();
        int ai = -1, bi = -1, remotes = 0;
        boolean aliveA = false, aliveB = false;
        double da = Double.NaN, db = Double.NaN;
        if (aligned) {
            for (int i = 0; i < ids.entityCount(); i++) {
                if (!kinds.player(i) || kinds.localPlayer(i)) continue;
                remotes++;
                if (firstUuid.equals(ids.at(i))) {
                    ai = i;
                    aliveA = kinds.living(i) && combat.alive(i);
                }
                if (secondUuid.equals(ids.at(i))) {
                    bi = i;
                    aliveB = kinds.living(i) && combat.alive(i);
                }
            }
            if (ai >= 0) {
                da = Math.sqrt(Math.pow(positions.x(ai)-local.x(),2)
                        + Math.pow(positions.y(ai)-local.y(),2)
                        + Math.pow(positions.z(ai)-local.z(),2));
            }
            if (bi >= 0) {
                db = Math.sqrt(Math.pow(positions.x(bi)-local.x(),2)
                        + Math.pow(positions.y(bi)-local.y(),2)
                        + Math.pow(positions.z(bi)-local.z(),2));
            }
        }
        final boolean nearestB = bi >= 0 && nearest.found()
                && nearest.entityIndex() == bi;
        final boolean rotationB = bi >= 0 && rotation.available()
                && rotation.entityIndex() == bi;
        return " secondReadReason=" + m274PriorityReason(
                    aligned, remotes, ai, bi, aliveA, aliveB,
                    da, db, nearestB, rotationB)
                + " secondReadUuidCount=" + ids.entityCount()
                + " secondReadKindsCount=" + kinds.entityCount()
                + " secondReadCombatCount=" + combat.entityCount()
                + " secondReadPositionsCount=" + positions.entityCount()
                + " secondReadRemotePlayers=" + remotes
                + " secondReadAIndex=" + ai + " secondReadBIndex=" + bi
                + " secondReadAAlive=" + aliveA + " secondReadBAlive=" + aliveB
                + " secondReadA3D=" + da + " secondReadB3D=" + db
                + " secondReadNearest=" + nearest.entityIndex()
                + " secondReadRotation=" + rotation.entityIndex()
                + " clickIndex=" + clickIndex;
    }

    /**
     * Exact native acceptance, never a production target-selection bypass.
     * The caller MUST first verify original crosshair UUID/kind/combat,
     * source nearest/rotation and attack range, then re-read BOTH actual
     * live candidate UUIDs through Evidence. This predicate cannot emit
     * B's owned-attack marker or advance the witness from phase 2.
     */
    static boolean phaseTwoFirstReturnPermitted(final int phase,
            final boolean firstOwnedClick, final UUID clickedUuid,
            final UUID firstUuid, final int clickedIndex,
            final int revalidatedAIndex,
            final boolean freshBothEligibleANearest) {
        return phase == 2 && firstOwnedClick
                && clickedUuid != null && clickedUuid.equals(firstUuid)
                && clickedIndex >= 0 && clickedIndex == revalidatedAIndex
                && freshBothEligibleANearest;
    }

    private static Evidence read(final Minecraft189HostRuntime host) {
        return read(host, null);
    }

    /**
     * When requested, attach the decision reason from EXACTLY the native
     * snapshots used by the rejecting original two-player read. The same
     * source values and selection gates still decide the actual attack.
     */
    private static Evidence read(final Minecraft189HostRuntime host,
            final StringBuilder sameReadReason) {
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
                || ids.entityCount() != positions.entityCount()) {
            if (sameReadReason != null) {
                sameReadReason.append("original-world-snapshots-unavailable-or-misaligned")
                        .append(" uuidCount=").append(ids.entityCount())
                        .append(" kindsCount=").append(kinds.entityCount())
                        .append(" combatCount=").append(combat.entityCount())
                        .append(" positionsCount=").append(positions.entityCount())
                        .append(" localAvailable=").append(local.available());
            }
            return null;
        }
        int a = -1, b = -1, remotes = 0;
        int originalAIndex = -1, originalBIndex = -1;
        boolean originalAAlive = false, originalBAlive = false;
        for (int index = 0; index < kinds.entityCount(); index++) {
            if (!kinds.player(index) || kinds.localPlayer(index)) continue;
            remotes++;
            final boolean alive = kinds.living(index) && combat.alive(index);
            if (firstUuid.equals(ids.at(index))) {
                originalAIndex = index;
                originalAAlive = alive;
                if (alive) a = index;
            }
            if (secondUuid.equals(ids.at(index))) {
                originalBIndex = index;
                originalBAlive = alive;
                if (alive) b = index;
            }
        }
        if (remotes != 2 || a < 0 || b < 0 || a == b) {
            if (sameReadReason != null) {
                sameReadReason.append(m274PriorityReason(
                        true, remotes, originalAIndex, originalBIndex,
                        originalAAlive, originalBAlive, Double.NaN, Double.NaN,
                        false, false))
                        .append(" remoteCount=").append(remotes)
                        .append(" originalAIndex=").append(originalAIndex)
                        .append(" originalBIndex=").append(originalBIndex)
                        .append(" AAlive=").append(originalAAlive)
                        .append(" BAlive=").append(originalBAlive);
            }
            return null;
        }
        final double adx = positions.x(a) - local.x();
        final double ady = positions.y(a) - local.y();
        final double adz = positions.z(a) - local.z();
        final double bdx = positions.x(b) - local.x();
        final double bdy = positions.y(b) - local.y();
        final double bdz = positions.z(b) - local.z();
        final double adist = Math.sqrt(adx * adx + ady * ady + adz * adz);
        final double bdist = Math.sqrt(bdx * bdx + bdy * bdy + bdz * bdz);
        if (!Double.isFinite(adist) || !Double.isFinite(bdist)
                || adist < 1.0D || adist > 3.5D
                || bdist < 1.0D || bdist > 3.5D) {
            if (sameReadReason != null) {
                sameReadReason.append(m274PriorityReason(
                        true, remotes, a, b, true, true, adist, bdist,
                        false, false))
                        .append(" originalA3D=").append(adist)
                        .append(" originalB3D=").append(bdist);
            }
            return null;
        }
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
        if (sameReadReason != null) {
            sameReadReason.append(m274PriorityReason(
                    true, remotes, a, b, true, true, adist, bdist,
                    nearest.found() && nearest.entityIndex() == b,
                    rotation.available() && rotation.entityIndex() == b))
                    .append(" originalA3D=").append(adist)
                    .append(" originalB3D=").append(bdist)
                    .append(" originalNearest=").append(nearest.entityIndex())
                    .append(" originalRotation=").append(rotation.entityIndex())
                    .append(" originalBIndex=").append(b);
        }
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
