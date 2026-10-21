package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * Opt-in test-only witness for real vanilla Minecraft 1.8.9 disconnect
 * and fresh same-username rejoin. Never supplies production target choice.
 */
final class Minecraft189RejoinTargetAcceptance {
    private static UUID originalUuid, survivorUuid;
    private static int phase, disabledTicks, armedTicks, aloneTicks, rejoinedTicks, releasedTicks;
    private static boolean reacquiredClicked;
    private static boolean survivorClicked;
    private static int firstOriginalIndex = -1;
    private static int nativeNonPlayerBaseline = -1;
    private static boolean nativeSentinelObserved;
    private static boolean realBControllerReported;
    private static boolean realBControllerEntityUuidReported;
    private static UUID staleControllerIntentUuid;
    private static boolean staleDispatchVetoReported, freshDispatchAllowedReported;
    private static boolean scopedOwnerVetoReported, scopedOwnerFreshReported;
    private static int nativeOwnerDiagnosticsTicks;
    private static int prelockNativeEvidenceTicks;
    private static int nativeCrosshairEvidenceTicks;
    private static int m419NativeStableSourceTicks;
    private static int m419TransientWrongOwnerVetoCount;

    private Minecraft189RejoinTargetAcceptance() {}

    static void reset() {
        originalUuid = survivorUuid = null;
        phase = disabledTicks = armedTicks = aloneTicks = rejoinedTicks = releasedTicks = 0;
        reacquiredClicked = false;
        survivorClicked = false;
        firstOriginalIndex = -1;
        nativeNonPlayerBaseline = -1;
        nativeSentinelObserved = false;
        realBControllerReported = false;
        realBControllerEntityUuidReported = false;
        staleControllerIntentUuid = null;
        staleDispatchVetoReported = freshDispatchAllowedReported = false;
        scopedOwnerVetoReported = scopedOwnerFreshReported = false;
        nativeOwnerDiagnosticsTicks = 0;
        prelockNativeEvidenceTicks = 0;
        nativeCrosshairEvidenceTicks = 0;
        m419NativeStableSourceTicks = 0;
        m419TransientWrongOwnerVetoCount = 0;
    }

    private static Evidence read(Minecraft189HostRuntime host) {
        if (originalUuid == null) {
            originalUuid = UUID.fromString(System.getProperty(
                    "custommc.acceptance.targetAUuid"));
            survivorUuid = UUID.fromString(System.getProperty(
                    "custommc.acceptance.targetBUuid"));
            if (originalUuid.equals(survivorUuid))
                throw new IllegalStateException("duplicate real player UUID identities");
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
        Minecraft189NearestPlayerTargetState.Snapshot nearest =
                host.nearestPlayerTargetState().snapshot();
        Minecraft189TargetRotationState.Snapshot rotation =
                host.targetRotationState().snapshot();
        if (!ids.available() || !kinds.available() || !combat.available()
                || !positions.available() || !local.available()
                || ids.entityCount() != kinds.entityCount()
                || ids.entityCount() != combat.entityCount()
                || ids.entityCount() != positions.entityCount()) return null;
        int a=-1,b=-1,other=0;
        for (int i=0; i<ids.entityCount(); i++) {
            if (!kinds.player(i) || kinds.localPlayer(i)) continue;
            if (!kinds.living(i) || !combat.alive(i)) return null;
            UUID id=ids.at(i);
            if (originalUuid.equals(id)) a=i;
            else if (survivorUuid.equals(id)) b=i;
            else other++;
        }
        if (other!=0 || (b<0 && !(phase>=4 && a>=0))) return null;
        double da=Double.POSITIVE_INFINITY,db;
        if (a>=0) {
            double dx=positions.x(a)-local.x(),dy=positions.y(a)-local.y(),
                    dz=positions.z(a)-local.z();
            da=Math.sqrt(dx*dx+dy*dy+dz*dz);
        }
        if (b>=0) {
            double bx=positions.x(b)-local.x(),by=positions.y(b)-local.y(),
                    bz=positions.z(b)-local.z();
            db=Math.sqrt(bx*bx+by*by+bz*bz);
        } else db=Double.POSITIVE_INFINITY;
        if ((b>=0 && !Double.isFinite(db)) || (a>=0 && !Double.isFinite(da)))
            return null;
        return new Evidence(ids,kinds,combat,nearest,rotation,a,b,da,db);
    }

    private static int sourceNonPlayerCount(final Evidence evidence) {
        if (evidence == null) return -1;
        int count = 0;
        for (int i = 0; i < evidence.ids.entityCount(); i++)
            if (!evidence.kinds.player(i)) count++;
        return count;
    }

    /** Read-only explanation of a genuine four-sentinel native World witness. */
    static String m272SentinelReadinessReason(
            final int baseline, final int observed,
            final boolean originalSnapshotValid, final boolean bOnlyLocked) {
        if (baseline < 0) return "native-baseline-unavailable";
        if (!originalSnapshotValid) return "original-source-evidence-unavailable";
        if (!bOnlyLocked) return "original-survivor-B-owner-not-locked";
        if (observed < baseline) return "native-nonplayer-count-decreased";
        if ((long)observed - baseline < 4L)
            return "four-native-nonplayers-not-yet-observed";
        return "four-native-nonplayers-observed";
    }

    /** Raw original World kind/UUID readback only: cannot authorize a hit. */
    static int m272RawNativeNonplayerCount(
            final Minecraft189WorldEntityUuidState.Snapshot ids,
            final Minecraft189WorldEntityKindState.Snapshot kinds) {
        if (ids == null || kinds == null || !ids.available()
                || !kinds.available() || ids.entityCount() != kinds.entityCount())
            return -1;
        int count = 0;
        for (int i = 0; i < ids.entityCount(); i++)
            if (!kinds.player(i)) count++;
        return count;
    }

    /**
     * V51: independent original objectMouseOver/ray witness at the EXACT
     * Mojang automatic-click boundary. A valid sticky B UUID target does
     * not imply the vanilla camera ray currently hits B. Diagnose that
     * distinction without setting rotation, selecting entities, clicking,
     * touching packets or changing a single admission rule.
     */
    /**
     * Real click-lane telemetry for both the original locked-B phase and
     * the post-rejoin M419 controller phase. Observing phase 4 is critical:
     * the original selected B UUID and source 3D distance can be valid
     * while vanilla objectMouseOver never hits a real B collision box.
     * This classifier can neither authorize nor manufacture a click.
     */
    static boolean originalCrosshairObservationDue(
            final int sourcePhase, final int sourceTickCount) {
        return (sourcePhase == 3 || sourcePhase == 4)
                && sourceTickCount > 0 && sourceTickCount % 40 == 0;
    }

    static void observeOriginalCrosshairAtClick(
            final Minecraft189HostRuntime host,
            final boolean originalPlayerHit, final int originalHitIndex) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraRejoin")
                || (phase != 3 && phase != 4)
                || !originalCrosshairObservationDue(
                        phase, ++nativeCrosshairEvidenceTicks))
            return;
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final Minecraft189TargetRotationState.Snapshot rotation =
                host.targetRotationState().snapshot();
        final Minecraft189PlayerRotationState.Snapshot localRotation =
                host.playerRotationState().snapshot();
        final Minecraft189KillAuraModule aura = host.featureCatalog().killAura();
        final boolean indexed = originalPlayerHit && originalHitIndex >= 0
                && ids.available() && originalHitIndex < ids.entityCount();
        final UUID actualRayUuid = indexed ? ids.at(originalHitIndex) : null;
        // V62 READ-ONLY: determine why a source-native exact B rotation
        // sometimes has no true original vanilla objectMouseOver hit. Match
        // B via its CURRENT original UUID, never its old entity-list index.
        // A correct target yaw alone is not proof that the genuine ray
        // intersects the target's vertical collision box.
        final Minecraft189WorldEntityPositionState.Snapshot positions =
                host.worldEntityPositionState().snapshot();
        final Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        final Minecraft189WorldEntityCombatState.Snapshot combat =
                host.worldEntityCombatState().snapshot();
        final Minecraft189PlayerPositionState.Snapshot localPosition =
                host.playerPositionState().snapshot();
        int nativeBIndex = -1;
        if (survivorUuid != null && ids.available()) {
            for (int i = 0; i < ids.entityCount(); i++) {
                if (survivorUuid.equals(ids.at(i))) {
                    nativeBIndex = i;
                    break;
                }
            }
        }
        final boolean bPositionReadable = nativeBIndex >= 0
                && positions.available() && kinds.available()
                && localPosition.available()
                && positions.entityCount() == ids.entityCount()
                && kinds.entityCount() == ids.entityCount()
                && kinds.player(nativeBIndex);
        final double sourceBHorizontal = bPositionReadable
                ? Math.hypot(positions.x(nativeBIndex) - localPosition.x(),
                        positions.z(nativeBIndex) - localPosition.z())
                : Double.NaN;
        final double sourceBEyeAboveHead = bPositionReadable
                ? (localPosition.y() + 1.62D) - (positions.y(nativeBIndex) + 1.80D)
                : Double.NaN;
        final double sourceBFeetY = bPositionReadable
                ? positions.y(nativeBIndex) : Double.NaN;
        final boolean originalBCombatAlive = bPositionReadable
                && combat.available() && combat.entityCount() == ids.entityCount()
                && combat.alive(nativeBIndex);
        System.out.println("CUSTOMMC_189_REJOIN_ORIGINAL_CROSSHAIR_AT_CLICK"
                + " phase=" + phase
                + " tick=" + nativeCrosshairEvidenceTicks
                + " realPlayerHit=" + originalPlayerHit
                + " realHitIndex=" + originalHitIndex
                + " realHitUuid=" + actualRayUuid
                + " rayIsSurvivorB="
                    + (indexed && survivorUuid != null
                        && survivorUuid.equals(actualRayUuid))
                + " nativeTargetAvailable=" + rotation.available()
                + " nativeTargetIndex=" + rotation.entityIndex()
                + " realYaw="
                    + (localRotation.available() ? localRotation.yaw() : Float.NaN)
                + " realPitch="
                    + (localRotation.available() ? localRotation.pitch() : Float.NaN)
                + " targetYaw=" + rotation.yaw()
                + " targetPitch=" + rotation.pitch()
                + " targetDistance=" + rotation.distance()
                + " BstillLocked="
                    + (survivorUuid != null
                        && survivorUuid.equals(aura.lockedTargetUuid()))
                + " BcurrentOriginalIndex=" + nativeBIndex
                + " BnativeGeometryAvailable=" + bPositionReadable
                + " BoriginalFeetY=" + sourceBFeetY
                + " BnativeHorizontalDistance=" + sourceBHorizontal
                + " originalEyeHeightAboveBHead=" + sourceBEyeAboveHead
                + " BnativeCombatAlive=" + originalBCombatAlive
                + " localSourceFeetY="
                    + (localPosition.available() ? localPosition.y() : Double.NaN));
        System.out.flush();
    }

    static void observe(Minecraft189HostRuntime host) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraRejoin"))
            return;
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        // V47: purely observational source-native evidence for intermittent
        // missing INITIAL A lock. This is measured in the disabled phase:
        // no target/rotation/FOV/World/player mutation and no "PASS" label.
        if (phase == 0 && ++prelockNativeEvidenceTicks % 100 == 0) {
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
            final Minecraft189PlayerRotationState.Snapshot yaw =
                    host.playerRotationState().snapshot();
            final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                    host.nearestPlayerTargetState().snapshot();
            final Minecraft189TargetRotationState.Snapshot selected =
                    host.targetRotationState().snapshot();
            System.out.println("CUSTOMMC_189_REJOIN_INITIAL_PRELOCK_EVIDENCE"
                    + " validEvidence=" + (e != null)
                    + " disabledPreferredTicks=" + disabledTicks
                    + " Aindex=" + (e == null ? -2 : e.a)
                    + " Bindex=" + (e == null ? -2 : e.b)
                    + " A_distance=" + (e == null ? Double.NaN : e.da)
                    + " B_distance=" + (e == null ? Double.NaN : e.db)
                    + " A_selected=" + (e != null && e.selected(e.a))
                    + " ids=" + ids.available() + "/" + ids.entityCount()
                    + " kinds=" + kinds.available() + "/" + kinds.entityCount()
                    + " combat=" + combat.available() + "/" + combat.entityCount()
                    + " positions=" + positions.available() + "/" + positions.entityCount()
                    + " local=" + local.available()
                    + " yaw=" + (yaw.available() ? yaw.yaw() : Float.NaN)
                    + " pitch=" + (yaw.available() ? yaw.pitch() : Float.NaN)
                    + " nearestFound=" + nearest.found()
                    + " nearestIndex=" + nearest.entityIndex()
                    + " rotationSelected=" + selected.available()
                    + " selectedIndex=" + selected.entityIndex()
                    + " auraActive=" + aura.active());
            System.out.flush();
        }
        // Bounded observation only. Never changes the native target, lock,
        // attack scheduling, source World, or fixture assertions.
        if (phase >= 1 && phase <= 3 && ++nativeOwnerDiagnosticsTicks % 80 == 0) {
            System.out.println("CUSTOMMC_189_REJOIN_NATIVE_OWNER_DIAG"
                    + " phase=" + phase
                    + " aIndex=" + (e == null ? -2 : e.a)
                    + " bIndex=" + (e == null ? -2 : e.b)
                    + " da=" + (e == null ? Double.NaN : e.da)
                    + " db=" + (e == null ? Double.NaN : e.db)
                    + " selectedA=" + (e != null && e.selected(e.a))
                    + " selectedB=" + (e != null && e.selected(e.b))
                    + " locked=" + aura.lockedTargetUuid()
                    // V50: source-native read-only witnesses for the exact
                    // phase-3 failure where B is correctly selected+locked
                    // but no genuine Mojang attackEntity callback follows.
                    // No targeting/scheduling/World/player state changes.
                    + " auraFov=" + aura.fovSetting().get()
                    + " playerYaw=" + (host.playerRotationState().snapshot().available()
                            ? host.playerRotationState().snapshot().yaw() : Float.NaN)
                    + " playerPitch=" + (host.playerRotationState().snapshot().available()
                            ? host.playerRotationState().snapshot().pitch() : Float.NaN)
                    + " selectedRotationAvailable="
                            + host.targetRotationState().snapshot().available()
                    + " selectedRotationIndex="
                            + host.targetRotationState().snapshot().entityIndex()
                    + " selectedRotationYaw="
                            + host.targetRotationState().snapshot().yaw()
                    + " selectedRotationDistance="
                            + host.targetRotationState().snapshot().distance()
                    + " nativeFovPermitsB=" + (e != null && e.b >= 0
                            && Minecraft189KillAuraNativeFovGate.permits(
                                    e.b, aura.fovSetting().get(),
                                    host.playerRotationState().snapshot(),
                                    host.playerPositionState().snapshot(),
                                    host.worldEntityPositionState().snapshot(),
                                    host.worldEntityKindState().snapshot()))
                    // Strict acceptance is unchanged; these fields only
                    // expose why real server-spawned stands were not seen.
                    + " sentinelBaseline=" + nativeNonPlayerBaseline
                    + " sentinelStrictCount=" + sourceNonPlayerCount(e)
                    + " sentinelRawCount=" + m272RawNativeNonplayerCount(
                            host.worldEntityUuidState().snapshot(),
                            host.worldEntityKindState().snapshot())
                    + " nativeUuidCount="
                            + host.worldEntityUuidState().snapshot().entityCount()
                    + " nativeKindsCount="
                            + host.worldEntityKindState().snapshot().entityCount()
                    + " sentinelObserved=" + nativeSentinelObserved
                    + " sentinelReason=" + m272SentinelReadinessReason(
                            nativeNonPlayerBaseline, sourceNonPlayerCount(e),
                            e != null, e != null && e.a == -1
                                    && e.bOnlySelected()
                                    && survivorUuid.equals(aura.lockedTargetUuid())));
            System.out.flush();
        }
        if (phase == 4
                && Boolean.getBoolean(
                        "custommc.acceptance.requireRejoinedControllerB")) {
            // A server-acknowledged /tp cannot prove that the ORIGINAL
            // client has consumed it. Observe 20 consecutive true native
            // World snapshots with locked, selected B and a .30 buffer
            // before the harness makes B damageable. This is telemetry,
            // NOT an attack authorization. The .12 controller rule is intact.
            final String reason = m419SourceRejection(e, aura, true);
            if (m419StableNativeSourceSample(
                    reason, e == null ? Double.NaN : e.da,
                    e == null ? Double.NaN : e.db)) {
                m419NativeStableSourceTicks++;
                if (m419NativeStableSourceTicks >= 20
                        && m419NativeStableSourceTicks % 20 == 0) {
                    System.out.println(
                            "CUSTOMMC_189_M419_NATIVE_PHASE4_SOURCE_STABLE=20"
                            + " observed=" + m419NativeStableSourceTicks
                            + " Aindex=" + e.a
                            + " Bindex=" + e.b
                            + " original3DA=" + e.da
                            + " original3DB=" + e.db
                            + " margin=" + (e.db - e.da)
                            + " nativeSelectedB=" + e.selected(e.b)
                            + " locked=" + aura.lockedTargetUuid());
                    System.out.flush();
                }
            } else {
                m419NativeStableSourceTicks = 0;
            }
            if (++nativeOwnerDiagnosticsTicks % 80 == 0) {
                System.out.println("CUSTOMMC_189_M419_WAITING_NATIVE_CONTROLLER_SOURCE"
                        + " reason=" + reason
                        + " Aindex=" + (e == null ? -2 : e.a)
                        + " Bindex=" + (e == null ? -2 : e.b)
                        + " original3DA=" + (e == null ? Double.NaN : e.da)
                        + " original3DB=" + (e == null ? Double.NaN : e.db)
                        + " nativeSelectedB=" + (e != null && e.selected(e.b))
                        + " locked=" + aura.lockedTargetUuid());
                System.out.flush();
            }
        }
        if (phase==0) {
            if (aura.active()) throw new IllegalStateException("rejoin Aura preenabled");
            disabledTicks=e!=null && e.aPreferred()
                    ? disabledTicks+1 : 0;
            if (disabledTicks>=20) {
                firstOriginalIndex=e.a;
                host.featureCatalog().enableKillAuraForOfficialRejoinAcceptance();
                phase=1;
                System.out.println("CUSTOMMC_189_REJOIN_TWO_NATIVE_UUID_PLAYERS_READY=20");
                System.out.flush();
            }
            return;
        }
        if (!aura.active() || !aura.lockTargetSetting().get())
            throw new IllegalStateException("rejoin lost actual Kill Aura lock setting");
        if (phase==1) {
            if (e!=null && e.aPreferred()
                    && originalUuid.equals(aura.lockedTargetUuid())) {
                if (++armedTicks>=10) {
                    phase=2;
                    System.out.println("CUSTOMMC_189_REJOIN_ORIGINAL_UUID_LOCK_ARMED=10");
                    System.out.flush();
                }
            } else armedTicks=0;
        } else if (phase==2) {
            // A must ACTUALLY disappear from native loadedEntityList.
            // Only the living B UUID may remain, not a stale original index.
            if (e!=null && e.a==-1 && e.bOnlySelected()
                    && survivorUuid.equals(aura.lockedTargetUuid())) {
                if (++aloneTicks>=15) {
                    phase=3;
                    nativeNonPlayerBaseline = sourceNonPlayerCount(e);
                    System.out.println("CUSTOMMC_189_REJOIN_OLD_A_UNLOADED_B_LOCKED_PASS=15");
                    System.out.flush();
                }
            } else aloneTicks=0;
        } else if (phase==3) {
            // M417: a native nonplayer addition must be OBSERVED by the
            // original host after A has genuinely left, before its fresh
            // JVM returns. Dedicated-server summon output alone is not proof.
            if (Boolean.getBoolean("custommc.acceptance.requireRejoinIndexReorder")
                    && !nativeSentinelObserved && nativeNonPlayerBaseline >= 0
                    && e!=null && e.a==-1 && e.bOnlySelected()
                    && survivorUuid.equals(aura.lockedTargetUuid())
                    && sourceNonPlayerCount(e)>=nativeNonPlayerBaseline+4) {
                nativeSentinelObserved = true;
                System.out.println(
                    "CUSTOMMC_189_REJOIN_REAL_SENTINEL_NATIVE_OBSERVED_PASS=YES"
                    + " nonplayersBefore=" + nativeNonPlayerBaseline
                    + " nonplayersAfter=" + sourceNonPlayerCount(e));
                System.out.flush();
            }
            // Original A has a new real EntityPlayer instance and source-native
            // list index. Re-acquire by UUID, never a stored index.
            if (e!=null && e.aCloserButBSelected()
                    && survivorUuid.equals(aura.lockedTargetUuid())) {
                if (++rejoinedTicks>=20) {
                    phase=4;
                    System.out.println("CUSTOMMC_189_REJOIN_ORIGINAL_UUID_REVALIDATED_PASS=YES");
                    System.out.println("CUSTOMMC_189_REJOIN_STICKY_B_RETAINED_PASS=20");
                    System.out.println("CUSTOMMC_189_REJOIN_OLD_A_INDEX="
                            + firstOriginalIndex + " NEW_A_INDEX=" + e.a);
                    if (Boolean.getBoolean(
                            "custommc.acceptance.requireRejoinIndexReorder")) {
                        // M415/M416 did not certify index reordering. This
                        // optional M417 gate must witness a DIFFERENT real
                        // current index for the SAME source-native UUID.
                        if (firstOriginalIndex < 0 || e.a < 0
                                || !nativeSentinelObserved
                                || firstOriginalIndex == e.a)
                            throw new IllegalStateException(
                                    "real rejoin did not actually reorder A index"
                                    + " original=" + firstOriginalIndex
                                    + " current=" + e.a
                                    + " nativeSentinelsObserved=" + nativeSentinelObserved
                                    + " nonplayersBefore=" + nativeNonPlayerBaseline
                                    + " nonplayersNow=" + sourceNonPlayerCount(e));
                        if (!originalUuid.equals(e.ids.at(e.a))
                                || !e.kinds.player(e.a)
                                || e.kinds.localPlayer(e.a))
                            throw new IllegalStateException(
                                    "reordered entity index has wrong native UUID");
                        System.out.println(
                            "CUSTOMMC_OFFICIAL_189_REJOIN_ACTUAL_INDEX_REORDER_PASS=YES");
                    }
                    if (Boolean.getBoolean(
                            "custommc.acceptance.requireRejoinWrongTargetVeto"))
                        proveWrongTargetProposalRejected(host, e);
                    if (Boolean.getBoolean(
                            "custommc.acceptance.requireStaleDispatchVeto")) {
                        if (!survivorUuid.equals(e.ids.at(e.b))
                                || !survivorUuid.equals(aura.lockedTargetUuid()))
                            throw new IllegalStateException(
                                    "M421 captured stale owner must be real locked B");
                        staleControllerIntentUuid = e.ids.at(e.b);
                        System.out.println(
                                "CUSTOMMC_189_REAL_B_PRE_EXIT_INTENT_CAPTURED_PASS=YES");
                    }
                    System.out.flush();
                }
            } else rejoinedTicks=0;
        } else if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraRejoinRelease")
                && phase==4) {
            // Real B player MUST disappear, not merely move away. Source-native
            // lock/selection then falls back to the fresh living A UUID.
            if (e!=null && e.b==-1 && e.aOnlySelected()
                    && originalUuid.equals(aura.lockedTargetUuid())) {
                if (++releasedTicks>=15) {
                    phase=5;
                    System.out.println(
                            "CUSTOMMC_189_REJOIN_SURVIVOR_B_UNLOADED_A_RELOCKED_PASS=15");
                    System.out.flush();
                }
            } else releasedTicks=0;
        }
    }

    /**
     * M422: a real B-locked phase-4 Aura dispatch has current native UUID
     * B. In one opt-in acceptance attempt, present the genuine *rejoined*
     * and still visible A UUID as its deliberately stale scope owner.
     * The outgoing Entity argument is never changed or forged.
     */
    static UUID scopedOwnerIntentForAcceptance(final UUID nativeOwner) {
        if (phase == 4 && !scopedOwnerVetoReported
                && survivorUuid.equals(nativeOwner))
            return originalUuid;
        return nativeOwner;
    }

    static void verifyScopedOwnerVeto(final UUID expected, final UUID actual) {
        // Other genuine crosshair/selection divergence MUST still be
        // rejected by the production controller gate without fabricating
        // a passing test marker or crashing the host.
        if (phase != 4 || scopedOwnerVetoReported
                || !originalUuid.equals(expected)
                || !survivorUuid.equals(actual)) return;
        scopedOwnerVetoReported = true;
        System.out.println(
                "CUSTOMMC_189_SCOPED_NATIVE_A_INTENT_REAL_B_DISPATCH_VETO_PASS=YES");
        System.out.flush();
    }

    static void verifyScopedOwnerAccepted(final UUID expected, final UUID actual) {
        if (phase != 4 || !scopedOwnerVetoReported
                || scopedOwnerFreshReported) return;
        if (!survivorUuid.equals(expected) || !survivorUuid.equals(actual))
            throw new IllegalStateException(
                    "M422 fresh native controller B owner mismatch");
        scopedOwnerFreshReported = true;
        System.out.println(
                "CUSTOMMC_189_SCOPED_NATIVE_FRESH_B_DISPATCH_PASS=YES");
        System.out.flush();
    }

    /**
     * M421: retain a genuine B intent while B remains locked. After real B
     * disconnect and genuine A reacquisition, exercise one deliberately
     * delayed B intent at the ORIGINAL controller's entry using the real
     * current A argument. The original vanilla method returns BEFORE any
     * attack packet. The next fresh A controller call is permitted.
     * This tests a bounded delayed-intent identity switch across ticks,
     * not every possible in-frame race or a fabricated network packet.
     */
    static boolean allowStaleDispatchAfterRealExit(
            Minecraft189HostRuntime host,
            Object controller, Object player, Object entity) {
        if (phase < 5) return true;
        Evidence e = read(host);
        Minecraft189KillAuraModule aura = host.featureCatalog().killAura();
        UUID actual = entity instanceof Minecraft189NativeEntityUuidAccess
                ? ((Minecraft189NativeEntityUuidAccess) entity)
                        .customMcNativeEntityUuid() : null;
        if (controller == null || player == null || e == null
                || e.a < 0 || e.b != -1 || !e.aOnlySelected()
                || !aura.active() || !aura.lockTargetSetting().get()
                || !originalUuid.equals(e.ids.at(e.a))
                || !originalUuid.equals(aura.lockedTargetUuid())
                || !originalUuid.equals(actual)
                || !survivorUuid.equals(staleControllerIntentUuid))
            throw new IllegalStateException(
                    "M421 real B-to-A native dispatch invalid phase=" + phase
                    + " argument=" + actual + " oldB=" + staleControllerIntentUuid);
        if (!staleDispatchVetoReported) {
            staleDispatchVetoReported = true;
            System.out.println(
                    "CUSTOMMC_189_REAL_B_EXIT_STALE_CONTROLLER_DISPATCH_VETO_PASS=YES");
            System.out.flush();
            return false;
        }
        if (!freshDispatchAllowedReported) {
            freshDispatchAllowedReported = true;
            System.out.println(
                    "CUSTOMMC_189_REAL_A_FRESH_CONTROLLER_DISPATCH_PASS=YES");
            System.out.flush();
        }
        return true;
    }

    /**
     * M419 observational classifier for the genuine original-world data at
     * the actual source controller boundary. This intentionally duplicates
     * the unchanged *strict* Evidence.aCloserButBSelected checks into
     * distinguishable failure reasons; it never grants a click.
     *
     * Keep the source 3D distance lower bound, upper bound and .12 closer
     * margin visible separately. In actual CI these failed on distinct
     * identical-acceptance branches before the caller could tell why.
     */
    static String m419RejectionReason(
            final boolean evidenceAvailable,
            final int aIndex, final int bIndex,
            final double da, final double db,
            final boolean sourceBSelected,
            final boolean aUuidMatches, final boolean bUuidMatches,
            final boolean lockedB, final boolean controllerReady) {
        if (!evidenceAvailable) return "source-evidence-unavailable";
        if (aIndex < 0) return "rejoined-A-missing";
        if (bIndex < 0) return "survivor-B-missing";
        if (!Double.isFinite(da) || da < 1.0D || da > 3.25D)
            return "rejoined-A-outside-original-[1,3.25]-reach";
        if (!Double.isFinite(db) || db < 1.0D || db > 3.5D)
            return "survivor-B-outside-original-[1,3.5]-reach";
        if (!(da + 0.12D < db))
            return "rejoined-A-not-0.12-closer-in-3D";
        if (!sourceBSelected) return "survivor-B-not-source-selected";
        if (!aUuidMatches) return "rejoined-A-original-UUID-mismatch";
        if (!bUuidMatches) return "survivor-B-original-UUID-mismatch";
        if (!lockedB) return "survivor-B-not-locked-by-KillAura";
        if (!controllerReady) return "original-controller-player-entity-null";
        return null;
    }

    /**
     * Test-only preflight observation, never native controller permission.
     * In the ORIGINAL controller path the separate .12 3D rule is enforced
     * again, regardless of this stricter .30 post-teleport timing buffer.
     */
    static boolean m419StableNativeSourceSample(
            final String rejection, final double da, final double db) {
        return rejection == null
                && Double.isFinite(da) && Double.isFinite(db)
                && da >= 1.0D && da <= 3.25D
                && db >= 1.0D && db <= 3.5D
                && db - da >= 0.30D;
    }

    private static String m419SourceRejection(
            final Evidence e, final Minecraft189KillAuraModule aura,
            final boolean controllerReady) {
        return m419RejectionReason(
                e != null, e == null ? -2 : e.a, e == null ? -2 : e.b,
                e == null ? Double.NaN : e.da,
                e == null ? Double.NaN : e.db,
                e != null && e.selected(e.b),
                e != null && e.a >= 0
                    && originalUuid.equals(e.ids.at(e.a)),
                e != null && e.b >= 0
                    && survivorUuid.equals(e.ids.at(e.b)),
                survivorUuid.equals(aura.lockedTargetUuid()),
                controllerReady);
    }

    /**
     * Original-source READ-ONLY diagnostics for a failed REAL controller
     * criterion. Logs current native local/A/B XYZ to distinguish genuine
     * vertical falling from horizontal drift; never changes admission,
     * player positions, UUID ownership, native objectMouseOver or packets.
     * These snapshots may be later than the failed Evidence read.
     */
    private static String m419NativeCoordinateDiagnostics(
            final Minecraft189HostRuntime host, final Evidence e) {
        final Minecraft189PlayerPositionState.Snapshot local =
                host.playerPositionState().snapshot();
        final Minecraft189WorldEntityPositionState.Snapshot positions =
                host.worldEntityPositionState().snapshot();
        if (e == null || local == null || positions == null
                || !local.available() || !positions.available()
                || e.a < 0 || e.b < 0
                || e.a >= positions.entityCount()
                || e.b >= positions.entityCount())
            return " originalNativeXYZ=unavailable";
        return " originalNativeHostXYZ=(" + local.x() + "," + local.y()
                + "," + local.z() + ")"
                + " originalNativeAXYZ=(" + positions.x(e.a) + ","
                + positions.y(e.a) + "," + positions.z(e.a) + ")"
                + " originalNativeBXYZ=(" + positions.x(e.b) + ","
                + positions.y(e.b) + "," + positions.z(e.b) + ")";
    }

    /**
     * A phase4 server /tp or original World update can temporarily place
     * true B outside reach while A is genuinely the native selected player.
     * The correct behavior is to issue NO synthetic melee command to A.
     * Do NOT turn this veto into a PASS: the original 20 fresh stable
     * B-locked World snapshots, strict real controller argument B UUID,
     * ray and dedicated server B-only damage remain mandatory.
     * Ordinary game/manual attacks are not intercepted here.
     */
    static boolean m419MustVetoPendingSyntheticClick(
            final boolean acceptanceEnabled, final int observedPhase,
            final String realSourceRejection, final boolean nativeRayHitsB) {
        return acceptanceEnabled && observedPhase == 4
                && (realSourceRejection != null || !nativeRayHitsB);
    }

    static boolean vetoTransientUnownedPhaseFourSyntheticClick(
            final Minecraft189HostRuntime host,
            final boolean originalPlayerHit, final int originalHitIndex) {
        final boolean acceptanceEnabled =
                Boolean.getBoolean("custommc.acceptance.reportKillAuraRejoin")
                && Boolean.getBoolean("custommc.acceptance.requireRejoinedControllerB");
        if (!acceptanceEnabled || phase != 4) return false;
        final Evidence e = read(host);
        final Minecraft189KillAuraModule aura =
                host.featureCatalog().killAura();
        final String sourceReason = m419SourceRejection(e, aura, true);
        final boolean originalRayHitsB = e != null && e.b >= 0
                && originalPlayerHit && originalHitIndex == e.b
                && survivorUuid.equals(e.ids.at(e.b));
        if (!m419MustVetoPendingSyntheticClick(
                acceptanceEnabled, phase, sourceReason, originalRayHitsB))
            return false;
        // READ ONLY and tightly bounded diagnostics; no success marker.
        // A permanently invalid World is still a genuine timed CI FAIL.
        final int samples = ++m419TransientWrongOwnerVetoCount;
        if (samples <= 4 || samples % 80 == 0) {
            System.out.println("CUSTOMMC_189_M419_TRANSIENT_WRONG_UUID_CLICK_VETO"
                    + " observed=" + samples
                    + " reason=" + sourceReason
                    + " sourceRayB=" + originalRayHitsB
                    + " nativeRayIndex=" + originalHitIndex
                    + " nativeAIndex=" + (e == null ? -2 : e.a)
                    + " nativeBIndex=" + (e == null ? -2 : e.b)
                    + " nativeA3D=" + (e == null ? Double.NaN : e.da)
                    + " nativeB3D=" + (e == null ? Double.NaN : e.db)
                    + " nativeSelectedB=" + (e != null && e.selected(e.b))
                    + " nativeLocked=" + aura.lockedTargetUuid()
                    + m419NativeCoordinateDiagnostics(host, e));
            System.out.flush();
        }
        return true;
    }

    /**
     * M419: the ORIGINAL mapped Mojang PlayerControllerMP.attackEntity
     * callback was entered in a genuine three-client session. Revalidate
     * both real UUIDs, their current native indices and sticky B ownership
     * at the actual controller boundary. No synthetic packet is injected.
     * The dedicated server independently validates fresh B damage, A zero.
     */
    static void verifyRealControllerB(
            Minecraft189HostRuntime host,
            Object controller, Object player, Object entity) {
        if (!Boolean.getBoolean(
                "custommc.acceptance.requireRejoinedControllerB")
                || phase!=4) return;
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        final String reason = m419SourceRejection(
                e, aura, controller != null && player != null && entity != null);
        if (reason != null)
            throw new IllegalStateException(
                    "M419 real outgoing controller not owned by live locked B UUID"
                    + " reason=" + reason
                    + " phase=" + phase
                    + " Aindex=" + (e == null ? -2 : e.a)
                    + " Bindex=" + (e == null ? -2 : e.b)
                    + " original3DA=" + (e == null ? Double.NaN : e.da)
                    + " original3DB=" + (e == null ? Double.NaN : e.db)
                    + " nativeSelectedB=" + (e != null && e.selected(e.b))
                    + " locked=" + aura.lockedTargetUuid()
                    + " expectedB=" + survivorUuid
                    + m419NativeCoordinateDiagnostics(host,e));
        if (Boolean.getBoolean(
                "custommc.acceptance.requireRejoinedControllerEntityUuid")) {
            if (!(entity instanceof Minecraft189NativeEntityUuidAccess))
                throw new IllegalStateException(
                        "M420 native attackEntity argument lacks mapped UUID access");
            // This is the ACTUAL third argument passed to Mojang's mapped
            // PlayerControllerMP.attackEntity, not a selected index, rotation
            // prediction, loaded-list lookup or fabricated player identity.
            UUID argumentUuid =
                    ((Minecraft189NativeEntityUuidAccess) entity)
                            .customMcNativeEntityUuid();
            if (argumentUuid == null
                    || !survivorUuid.equals(argumentUuid)
                    || originalUuid.equals(argumentUuid)
                    || !argumentUuid.equals(e.ids.at(e.b)))
                throw new IllegalStateException(
                        "M420 native attackEntity argument UUID does not equal"
                        + " current living locked B UUID argument=" + argumentUuid
                        + " locked=" + aura.lockedTargetUuid()
                        + " B=" + e.ids.at(e.b));
            if (!realBControllerEntityUuidReported) {
                realBControllerEntityUuidReported = true;
                System.out.println(
                        "CUSTOMMC_189_REJOIN_NATIVE_CONTROLLER_ENTITY_UUID_B_PASS=YES");
                System.out.flush();
            }
        }
        if (!realBControllerReported) {
            realBControllerReported=true;
            System.out.println(
                "CUSTOMMC_189_REJOIN_REAL_B_NATIVE_CONTROLLER_UUID_PASS=YES");
            System.out.flush();
        }
    }

    /**
     * M418 test-only negative proposal. A is genuinely in reach and closer,
     * but the real current selected and locked UUID is B. Send NO attack or
     * packet: exercise the same fail-closed original click ownership gate
     * against A's actual current native index and require explicit rejection.
     */
    private static void proveWrongTargetProposalRejected(
            Minecraft189HostRuntime host, Evidence e) {
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        if (!e.aCloserButBSelected() || e.a==e.b || !e.aInRange()
                || !originalUuid.equals(e.ids.at(e.a))
                || !survivorUuid.equals(e.ids.at(e.b))
                || !survivorUuid.equals(aura.lockedTargetUuid()))
            throw new IllegalStateException(
                    "wrong-target veto fixture lacks real native A/B ownership");
        try {
            verifyOwnedClick(host, true, e.a);
        } catch (IllegalStateException rejected) {
            String message=rejected.getMessage();
            if (message==null
                    || !message.startsWith("rejoin click invalid source-native owner"))
                throw rejected;
            System.out.println(
                    "CUSTOMMC_189_REJOIN_WRONG_IN_RANGE_UUID_PROPOSAL_VETO_PASS=YES");
            return;
        }
        throw new IllegalStateException(
                "real wrong UUID proposal was NOT vetoed by original click gate");
    }

    /**
     * Acceptance-only tolerance for one transient original-client state:
     * vanilla has announced A's disconnect, but its old EntityPlayer is
     * still present in loadedEntityList while the real live locked B is
     * already the selected target. An owned B click is NOT an A-absent
     * observation and must never advance the required 15-frame phase-3 gate.
     *
     * Fail closed on wrong UUID/lock, bad range or an unselected B. This is
     * deliberately never consulted by production target choice/dispatch.
     */
    static boolean phaseTwoOwnedSurvivorWhileNativeAStillLoaded(
            final int currentPhase, final boolean aStillLoaded,
            final boolean selectedSurvivor, final boolean survivorInRange,
            final UUID actual, final UUID locked, final UUID expectedSurvivor) {
        return currentPhase == 2 && aStillLoaded
                && selectedSurvivor && survivorInRange
                && actual != null && expectedSurvivor != null
                && expectedSurvivor.equals(actual)
                && expectedSurvivor.equals(locked);
    }

    static void verifyOwnedClick(Minecraft189HostRuntime host,
            boolean playerHit, int index) {
        Evidence e=read(host);
        Minecraft189KillAuraModule aura=host.featureCatalog().killAura();
        UUID id=(e!=null && index>=0 && index<e.ids.entityCount())
                ? e.ids.at(index) : null;
        if (!aura.active() || !aura.lockTargetSetting().get()
                || !playerHit || e==null || id==null
                || !e.kinds.player(index) || e.kinds.localPlayer(index)
                || !e.kinds.living(index) || !e.combat.alive(index)
                || !e.selected(index) || !Double.isFinite(e.rotation.distance())
                || e.rotation.distance()>3.5D
                || !id.equals(aura.lockedTargetUuid())) {
            throw new IllegalStateException(
                    "rejoin click invalid source-native owner phase="+phase
                    +" nativeUuid="+id+" locked="+aura.lockedTargetUuid()
                    +" index="+index);
        }
        if (phase<=2 && originalUuid.equals(id) && index==e.a
                && e.a>=0 && e.da<=3.5D) {
            // Original A is creative, never counted as genuine server damage.
        } else if (phaseTwoOwnedSurvivorWhileNativeAStillLoaded(
                phase, e.a >= 0, index == e.b && e.selected(e.b),
                e.bInRange(), id, aura.lockedTargetUuid(), survivorUuid)) {
            // Original Minecraft may retain A's entity briefly after its
            // dedicated-server disconnect. The *actual* native selected/
            // locked UUID is already B, so this is not a wrong-UUID attack.
            // Do not emit a survivor-lock PASS or advance phase here:
            // observe() still requires 15 real A-absent native snapshots.
        } else if (phase>=2 && survivorUuid.equals(id) && index==e.b
                && e.a==-1 && e.bInRange()) {
            // Selection-only creative B clicks never prove server damage.
            // Record a positive-owned click only once the required
            // 15-callback survivor-lock gate reaches phase 3.
            if (phase>=3 && !survivorClicked) {
                survivorClicked=true;
                System.out.println("CUSTOMMC_189_REJOIN_B_NATIVE_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else if (phase>=3 && phase<5 && survivorUuid.equals(id)
                && index==e.b && e.bInRange()) {
            // Fresh A rejoined; original B remains the true owner until
            // vanilla B actually leaves, without reusing any stale index.
        } else if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraRejoinRelease")
                && phase==4 && originalUuid.equals(id)
                && index==e.a && e.b==-1 && e.aOnlySelected()) {
            // A is still genuine CREATIVE while the actual B-disconnected
            // 15-callback UUID lock proof runs. This callback never certifies
            // positive damage, and cannot skip the full phase gate.
        } else if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraRejoinRelease")
                && phase>=5 && originalUuid.equals(id)
                && index==e.a && e.b==-1 && e.aInRange()) {
            if (!reacquiredClicked) {
                reacquiredClicked=true;
                phase=6;
                System.out.println(
                        "CUSTOMMC_189_REJOIN_FRESH_A_NATIVE_OWNED_ATTACK_PASS=YES");
                System.out.flush();
            }
        } else {
            throw new IllegalStateException("rejoin stale or wrong UUID attack phase="
                    +phase+" actual="+id+" locked="+aura.lockedTargetUuid()
                    +" A="+originalUuid+" B="+survivorUuid
                    +" aIndex="+e.a+" bIndex="+e.b
                    +" da="+e.da+" db="+e.db
                    +" selectedA="+e.selected(e.a)
                    +" selectedB="+e.selected(e.b));
        }
    }

    private static final class Evidence {
        final Minecraft189WorldEntityUuidState.Snapshot ids;
        final Minecraft189WorldEntityKindState.Snapshot kinds;
        final Minecraft189WorldEntityCombatState.Snapshot combat;
        final Minecraft189NearestPlayerTargetState.Snapshot nearest;
        final Minecraft189TargetRotationState.Snapshot rotation;
        final int a,b;
        final double da,db;
        Evidence(Minecraft189WorldEntityUuidState.Snapshot ids,
                Minecraft189WorldEntityKindState.Snapshot kinds,
                Minecraft189WorldEntityCombatState.Snapshot combat,
                Minecraft189NearestPlayerTargetState.Snapshot nearest,
                Minecraft189TargetRotationState.Snapshot rotation,
                int a,int b,double da,double db) {
            this.ids=ids;this.kinds=kinds;this.combat=combat;
            this.nearest=nearest;this.rotation=rotation;
            this.a=a;this.b=b;this.da=da;this.db=db;
        }
        boolean selected(int i) {
            return i>=0 && nearest.found() && nearest.entityIndex()==i
                    && rotation.available() && rotation.entityIndex()==i;
        }
        boolean bInRange() { return b>=0 && db>=1.0D && db<=3.5D; }
        boolean aInRange() { return a>=0 && da>=1.0D && da<=3.5D; }
        boolean aOnlySelected() { return b<0 && aInRange() && selected(a); }
        boolean aPreferred() {
            return a>=0 && da>=1.0D && da<=3.25D && bInRange()
                    && da+0.12D<db && selected(a);
        }
        boolean bOnlySelected() { return a<0 && bInRange() && selected(b); }
        boolean aCloserButBSelected() {
            return a>=0 && da>=1.0D && da<=3.25D && bInRange()
                    && da+0.12D<db && selected(b);
        }
    }
}
