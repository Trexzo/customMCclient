package dev.trexzo.custommc.platform.v1_8_9;
import java.util.UUID;

/** Real Mojang localhost CI only; observes production sticky UUID arbitration. */
final class Minecraft189LockedTargetAcceptance {
    private static UUID aUuid, bUuid;
    private static int phase, initialTicks, armedTicks, heldTicks;
    private static boolean aHit, bHit;
    // V366 R2: bounded, acceptance-only observations from the original
    // crosshair/scheduler callback. Never used to admit an action.
    private static int phaseThreeRayObservations, phaseThreeScheduleObservations;
    // Acceptance-only provenance copied from the SAME existing snapshots.
    // Never participates in initial eligibility, target selection, or clicks.
    private static int initialSourceObservations, maxInitialAStreak;
    private static int phase0SourceUnavailable, phase0CountMismatch;
    private static int phase0RemoteIdentityMissing, phase0NonfiniteDistance;
    private static int phase0GeometryRejected, phase0SelectionRejected;
    private static String lastInitialReason = "not-observed";
    private static String lastReadReason = "not-observed";
    private Minecraft189LockedTargetAcceptance() {}
    static void reset() {
        aUuid = bUuid = null;
        phase = initialTicks = armedTicks = heldTicks = 0;
        aHit = bHit = false;
        phaseThreeRayObservations = phaseThreeScheduleObservations = 0;
        initialSourceObservations = maxInitialAStreak = 0;
        phase0SourceUnavailable = phase0CountMismatch = 0;
        phase0RemoteIdentityMissing = phase0NonfiniteDistance = 0;
        phase0GeometryRejected = phase0SelectionRejected = 0;
        lastInitialReason = lastReadReason = "not-observed";
    }

    private static Evidence read(Minecraft189HostRuntime host) {
        if (aUuid == null) {
            aUuid = UUID.fromString(System.getProperty("custommc.acceptance.targetAUuid"));
            bUuid = UUID.fromString(System.getProperty("custommc.acceptance.targetBUuid"));
            if (aUuid.equals(bUuid)) throw new IllegalStateException("duplicate UUIDs");
        }
        Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        Minecraft189WorldEntityCombatState.Snapshot combat =
                host.worldEntityCombatState().snapshot();
        Minecraft189WorldEntityPositionState.Snapshot positions =
                host.worldEntityPositionState().snapshot();
        Minecraft189PlayerPositionState.Snapshot local =
                host.playerPositionState().snapshot();
        Minecraft189NearestPlayerTargetState.Snapshot near =
                host.nearestPlayerTargetState().snapshot();
        Minecraft189TargetRotationState.Snapshot aim =
                host.targetRotationState().snapshot();
        if (!ids.available() || !kinds.available() || !combat.available()
                || !positions.available() || !local.available()) {
            lastReadReason = "source-snapshot-unavailable";
            return null;
        }
        if (ids.entityCount() != kinds.entityCount()
                || ids.entityCount() != combat.entityCount()
                || ids.entityCount() != positions.entityCount()) {
            lastReadReason = "native-entity-array-count-mismatch";
            return null;
        }
        int a=-1, b=-1, remotes=0;
        for (int i=0; i<kinds.entityCount(); i++) {
            if (!kinds.player(i) || kinds.localPlayer(i)) continue;
            remotes++;
            if (!kinds.living(i) || !combat.alive(i)) continue;
            if (aUuid.equals(ids.at(i))) a=i;
            if (bUuid.equals(ids.at(i))) b=i;
        }
        if (remotes != 2 || a<0 || b<0 || a==b) {
            lastReadReason = "two-alive-original-UUIDs-not-found";
            return null;
        }
        double adx=positions.x(a)-local.x(), ady=positions.y(a)-local.y(),
                adz=positions.z(a)-local.z();
        double bdx=positions.x(b)-local.x(), bdy=positions.y(b)-local.y(),
                bdz=positions.z(b)-local.z();
        double da=Math.sqrt(adx*adx+ady*ady+adz*adz);
        double db=Math.sqrt(bdx*bdx+bdy*bdy+bdz*bdz);
        if (!Double.isFinite(da) || !Double.isFinite(db)) {
            lastReadReason = "source-distance-nonfinite";
            return null;
        }
        lastReadReason = "two-alive-UUIDs-and-distances-available";
        return new Evidence(ids,kinds,combat,near,aim,a,b,da,db);
    }

    static void observe(Minecraft189HostRuntime host) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraLockedTarget")) return;
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        if (phase==0) {
            if (aura.active()) throw new IllegalStateException("Aura pre-enabled");
            final boolean originalInitialA = e!=null && e.initialA();
            // Keep original 20-consecutive real-source gate unchanged.
            initialTicks=originalInitialA ? initialTicks+1 : 0;
            initialSourceObservations++;
            if (initialTicks > maxInitialAStreak)
                maxInitialAStreak = initialTicks;
            if (!originalInitialA) {
                if (e == null) {
                    switch (lastReadReason) {
                        case "source-snapshot-unavailable":
                            phase0SourceUnavailable++;
                            break;
                        case "native-entity-array-count-mismatch":
                            phase0CountMismatch++;
                            break;
                        case "source-distance-nonfinite":
                            phase0NonfiniteDistance++;
                            break;
                        default:
                            phase0RemoteIdentityMissing++;
                            break;
                    }
                } else {
                    final String rejection = m291InitialAReason(
                            e.da, e.db, e.a, e.b,
                            e.near.found(), e.near.entityIndex(),
                            e.aim.available(), e.aim.entityIndex());
                    if ("original-initial-A-not-selected".equals(rejection))
                        phase0SelectionRejected++;
                    else
                        phase0GeometryRejected++;
                }
            }
            if (initialSourceObservations == 1
                    || initialSourceObservations % 40 == 0) {
                // Read-only same-observation reason diagnostics; never
                // fabricate target selection or second source getter.
                lastInitialReason = e==null ? lastReadReason
                        : m291InitialAReason(e.da, e.db, e.a, e.b,
                            e.near.found(), e.near.entityIndex(),
                            e.aim.available(), e.aim.entityIndex());
                System.out.println(
                        "CUSTOMMC_189_LOCK_INITIAL_A_SOURCE_DIAGNOSTIC"
                        + " callbacks=" + initialSourceObservations
                        + " reason=" + lastInitialReason
                        + " sourceReason=" + lastReadReason
                        + " initialConsecutive=" + initialTicks
                        + " maxInitialConsecutive=" + maxInitialAStreak
                        + " missingSource=" + phase0SourceUnavailable
                        + " countMismatch=" + phase0CountMismatch
                        + " remoteUuidMissing=" + phase0RemoteIdentityMissing
                        + " nonfiniteDistance=" + phase0NonfiniteDistance
                        + " geometryRejected=" + phase0GeometryRejected
                        + " selectionRejected=" + phase0SelectionRejected
                        + " originalAIndex=" + (e==null ? -1 : e.a)
                        + " originalBIndex=" + (e==null ? -1 : e.b)
                        + " originalA3D=" + (e==null ? Double.NaN : e.da)
                        + " originalB3D=" + (e==null ? Double.NaN : e.db)
                        + " sourceNearest=" + (e==null ? -1 : e.near.entityIndex())
                        + " sourceAim=" + (e==null ? -1 : e.aim.entityIndex()));
                System.out.flush();
            }
            if (initialTicks>=20) {
                host.featureCatalog().enableKillAuraForOfficialLockedTargetAcceptance();
                phase=1;
                System.out.println("CUSTOMMC_189_LOCK_TWO_ELIGIBLE_INITIAL_A_PASS=20");
                System.out.flush();
            }
            return;
        }
        if (!aura.active() || !aura.lockTargetSetting().get())
            throw new IllegalStateException("sticky module disabled");
        if (phase==1) {
            // First confirm genuine native A is the live locked UUID.
            // Test A remains creative to exclude PvP knockback in this
            // selection-only phase. Server damage remains at zero.
            if (e!=null && e.initialA()
                    && aUuid.equals(aura.lockedTargetUuid())) {
                if (++armedTicks>=10) {
                    phase=2;
                    System.out.println("CUSTOMMC_189_LOCK_A_NATIVE_UUID_ARMED_PASS=10");
                    System.out.flush();
                }
            } else armedTicks=0;
        } else if (phase==2) {
            if (e!=null && e.bCloserButASelected()
                    && aUuid.equals(aura.lockedTargetUuid())) {
                if (++heldTicks>=15) {
                    phase=3;
                    System.out.println("CUSTOMMC_189_LOCK_A_RETAINED_WITH_CLOSER_B_PASS=15");
                    System.out.flush();
                }
            } else heldTicks=0;
        }
        if (phase==4 && e!=null && e.aFarBSelected()
                && bUuid.equals(aura.lockedTargetUuid())) {
            phase=5;
            System.out.println("CUSTOMMC_189_LOCK_A_INELIGIBLE_RELEASES_TO_B_PASS=YES");
            System.out.flush();
        }
    }

    /** Diagnostic only. The original renderer's ray is already published
     * by Minecraft before this hook; no new ray, UUID, packet or click.
     */
    static void observeNativeRay(Minecraft189HostRuntime host,
            final boolean originalPlayerHit, final int originalIndex) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraLockedTarget")
                || phase != 3) return;
        final int callback = ++phaseThreeRayObservations;
        if (callback != 1 && callback % 40 != 0) return;
        final Evidence e = read(host); // only existing native snapshots
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final UUID rayUuid = originalPlayerHit && ids.available()
                && originalIndex >= 0 && originalIndex < ids.entityCount()
                ? ids.at(originalIndex) : null;
        System.out.println("CUSTOMMC_189_V366_LOCK_PHASE3_NATIVE_RAY_DIAG"
                + " n=" + callback
                + " rayPlayer=" + originalPlayerHit
                + " rayIndex=" + originalIndex
                + " rayUuid=" + rayUuid
                + " selectedIndex=" + (e == null ? -1 : e.near.entityIndex())
                + " aimIndex=" + (e == null ? -1 : e.aim.entityIndex())
                + " lockedUuid=" + host.featureCatalog().killAura().lockedTargetUuid()
                + " aIndex=" + (e == null ? -1 : e.a)
                + " bIndex=" + (e == null ? -1 : e.b)
                + " a3D=" + (e == null ? Double.NaN : e.da)
                + " b3D=" + (e == null ? Double.NaN : e.db)
                + " sourceReason=" + lastReadReason);
        System.out.flush();
    }

    /** This reports the actual Aura scheduler decision; false is NOT a
     * failed controller packet, never an invented native hit.
     */
    static void observeAuraSchedule(Minecraft189HostRuntime host,
            final boolean originalPlayerHit, final int originalIndex,
            final boolean schedulerPermitted) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraLockedTarget")
                || phase != 3) return;
        final int callback = ++phaseThreeScheduleObservations;
        if (callback != 1 && callback % 40 != 0) return;
        final Minecraft189TargetRotationState.Snapshot target =
                host.targetRotationState().snapshot();
        final Minecraft189PlayerRotationState.Snapshot rotation =
                host.playerRotationState().snapshot();
        System.out.println("CUSTOMMC_189_V366_LOCK_PHASE3_SCHEDULE_DIAG"
                + " n=" + callback
                + " approved=" + schedulerPermitted
                + " rayPlayer=" + originalPlayerHit
                + " rayIndex=" + originalIndex
                + " aimAvailable=" + target.available()
                + " aimIndex=" + target.entityIndex()
                + " aimYaw=" + target.yaw()
                + " aimPitch=" + target.pitch()
                + " actualYaw=" + rotation.yaw()
                + " actualPitch=" + rotation.pitch()
                + " auraActive=" + host.featureCatalog().killAura().active()
                + " originalGuiOpen="
                + host.clickGuiRuntime().coreRuntime().model().snapshot().open());
        System.out.flush();
    }

    static void verifyOwnedClick(Minecraft189HostRuntime host,
            boolean playerHit, int index) {
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        UUID uuid=e==null || index<0 || index>=e.ids.entityCount()
                ? null : e.ids.at(index);
        if (!aura.active() || !aura.lockTargetSetting().get()
                || !playerHit || index<0 || e==null
                || index>=e.ids.entityCount()
                || !e.kinds.player(index) || e.kinds.localPlayer(index)
                || !e.kinds.living(index) || !e.combat.alive(index)
                || !e.selected(index)
                || !Double.isFinite(e.aim.distance()) || e.aim.distance()>3.5D
                || !uuid.equals(aura.lockedTargetUuid()))
            throw new IllegalStateException("no live locked owner: phase="+phase
                    +" index="+index+" uuid="+uuid+" locked="+aura.lockedTargetUuid());
        if ((phase==1 || phase==2) && aUuid.equals(uuid)) {
            // Creative A is a real eligible selectable player, but cannot
            // satisfy the later survival PvP server-damage gate.
        } else if (phase==3 && aUuid.equals(uuid)) {
            aHit=true; phase=4;
            System.out.println("CUSTOMMC_189_LOCK_A_NATIVE_UUID_OWNED_ATTACK_PASS=YES");
            System.out.flush();
        } else if (phase==4 && aUuid.equals(uuid)) {
            if (!(e.da<=3.5D && e.da>=1.0D))
                throw new IllegalStateException("stale A attack after range expiry");
        } else if ((phase==4 || phase==5) && bUuid.equals(uuid)
                && aHit && e.aFarBSelected()) {
            if (phase==4) {
                phase=5;
                System.out.println("CUSTOMMC_189_LOCK_A_INELIGIBLE_RELEASES_TO_B_PASS=YES");
            }
            if (!bHit) {
                bHit=true; phase=6;
                System.out.println("CUSTOMMC_189_LOCK_B_NATIVE_UUID_OWNED_ATTACK_PASS=YES");
            }
            System.out.flush();
        } else if (phase==6 && bUuid.equals(uuid)) {
            // Every later B click must still pass exact current native guard.
        } else throw new IllegalStateException("wrong locked target UUID phase="
                +phase+" uuid="+uuid
                + m280WrongPhaseDiagnostic(
                        phase, uuid, aUuid, bUuid,
                        aura.lockedTargetUuid(), index,
                        e == null ? -1 : e.a,
                        e == null ? -1 : e.b,
                        e == null ? Double.NaN : e.da,
                        e == null ? Double.NaN : e.db,
                        e == null ? -1 : e.near.entityIndex(),
                        e == null ? -1 : e.aim.entityIndex(),
                        e != null));
    }

    /**
     * V280 fail-closed diagnostics from the SAME source-native Evidence
     * read that rejected the current click. No additional getter, second
     * source snapshot, target selection or native game action is performed.
     */
    static String m280WrongPhaseDiagnostic(final int rejectedPhase,
            final UUID clickedUuid, final UUID expectedA,
            final UUID expectedB, final UUID sourceLockedUuid,
            final int clickedIndex, final int aIndex, final int bIndex,
            final double nativeA3D, final double nativeB3D,
            final int nearestIndex, final int rotationIndex,
            final boolean sourceAvailable) {
        final String reason;
        if (!sourceAvailable) {
            reason = "source-unavailable";
        } else if ((rejectedPhase == 1 || rejectedPhase == 2)
                && expectedB != null && expectedB.equals(clickedUuid)) {
            reason = "B-click-before-A-retention";
        } else if (rejectedPhase == 3
                && expectedB != null && expectedB.equals(clickedUuid)) {
            reason = "B-click-before-A-owned-attack";
        } else if (expectedA != null && expectedA.equals(clickedUuid)) {
            reason = "A-click-outside-allowed-phase";
        } else if (expectedB != null && expectedB.equals(clickedUuid)) {
            reason = "B-click-outside-allowed-phase";
        } else {
            reason = "unexpected-clicked-UUID";
        }
        return " sameReadReason=" + reason
                + " sameReadAvailable=" + sourceAvailable
                + " sameReadPhase=" + rejectedPhase
                + " sameReadClickedIndex=" + clickedIndex
                + " sameReadClickedUuid=" + clickedUuid
                + " sameReadExpectedA=" + expectedA
                + " sameReadExpectedB=" + expectedB
                + " sameReadLockedUuid=" + sourceLockedUuid
                + " sameReadAIndex=" + aIndex
                + " sameReadBIndex=" + bIndex
                + " sameReadA3D=" + nativeA3D
                + " sameReadB3D=" + nativeB3D
                + " sameReadNearest=" + nearestIndex
                + " sameReadRotation=" + rotationIndex;
    }

    /**
     * Pure same-source initial witness classifier. The original Evidence
     * predicate remains authoritative; this method cannot admit a target.
     */
    static String m291InitialAReason(final double a3D, final double b3D,
            final int aIndex, final int bIndex,
            final boolean nearestFound, final int nearestIndex,
            final boolean aimAvailable, final int aimIndex) {
        if (!Double.isFinite(a3D) || !Double.isFinite(b3D))
            return "original-distance-not-finite";
        if (aIndex < 0 || bIndex < 0 || aIndex == bIndex)
            return "original-UUID-source-index-unavailable";
        if (!(a3D >= 1 && a3D <= 3.25
                && b3D >= 1 && b3D <= 3.25))
            return "original-initial-A-or-B-outside-3D-bounds";
        if (!(a3D + 0.12 < b3D))
            return "original-A-not-nearer-by-source-margin";
        if (!nearestFound || nearestIndex != aIndex
                || !aimAvailable || aimIndex != aIndex)
            return "original-initial-A-not-selected";
        return "original-initial-A-witness-eligible";
    }

    private static final class Evidence {
        final Minecraft189WorldEntityUuidState.Snapshot ids;
        final Minecraft189WorldEntityKindState.Snapshot kinds;
        final Minecraft189WorldEntityCombatState.Snapshot combat;
        final Minecraft189NearestPlayerTargetState.Snapshot near;
        final Minecraft189TargetRotationState.Snapshot aim;
        final int a,b;
        final double da,db;
        Evidence(Minecraft189WorldEntityUuidState.Snapshot ids,
                Minecraft189WorldEntityKindState.Snapshot kinds,
                Minecraft189WorldEntityCombatState.Snapshot combat,
                Minecraft189NearestPlayerTargetState.Snapshot near,
                Minecraft189TargetRotationState.Snapshot aim,
                int a,int b,double da,double db) {
            this.ids=ids;this.kinds=kinds;this.combat=combat;
            this.near=near;this.aim=aim;this.a=a;this.b=b;
            this.da=da;this.db=db;
        }
        boolean selected(int i) {
            return near.found() && near.entityIndex()==i && aim.available()
                    && aim.entityIndex()==i;
        }
        boolean initialA() {
            return da>=1 && da<=3.25 && db>=1 && db<=3.25
                    && da+0.12<db && selected(a);
        }
        boolean bCloserButASelected() {
            return da>=1 && da<=3.5 && db>=1 && db<=3.5
                    && db+0.12<da && selected(a);
        }
        boolean aFarBSelected() {
            return da>4.5 && db>=1 && db<=3.5 && selected(b);
        }
    }
}
