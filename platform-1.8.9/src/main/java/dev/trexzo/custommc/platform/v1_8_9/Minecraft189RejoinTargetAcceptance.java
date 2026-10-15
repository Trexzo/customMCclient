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

    /**
     * V51: independent original objectMouseOver/ray witness at the EXACT
     * Mojang automatic-click boundary. A valid sticky B UUID target does
     * not imply the vanilla camera ray currently hits B. Diagnose that
     * distinction without setting rotation, selecting entities, clicking,
     * touching packets or changing a single admission rule.
     */
    static void observeOriginalCrosshairAtClick(
            final Minecraft189HostRuntime host,
            final boolean originalPlayerHit, final int originalHitIndex) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraRejoin")
                || phase != 3 || ++nativeCrosshairEvidenceTicks % 40 != 0)
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
        System.out.println("CUSTOMMC_189_REJOIN_ORIGINAL_CROSSHAIR_AT_CLICK"
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
                        && survivorUuid.equals(aura.lockedTargetUuid())));
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
                                    host.worldEntityKindState().snapshot())));
            System.out.flush();
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
        if (e==null || !e.aCloserButBSelected() || !e.bInRange()
                || !survivorUuid.equals(e.ids.at(e.b))
                || !originalUuid.equals(e.ids.at(e.a))
                || !survivorUuid.equals(aura.lockedTargetUuid())
                || controller==null || player==null || entity==null)
            throw new IllegalStateException(
                    "M419 real outgoing controller not owned by live locked B UUID");
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
