package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public final class Minecraft189RuntimeBridge {
    private static Minecraft189BootstrapRuntime activeRuntime;
    // True production-owned, synchronous game-thread scope. Manual clickMouse
    // and Auto Clicker attacks do not set it; every Aura call clears in finally.
    private static final ThreadLocal<UUID> auraDispatchUuid =
            new ThreadLocal<UUID>();
    // Separate original-controller dispatch ownership for TriggerBot.
    private static final ThreadLocal<UUID> triggerDispatchUuid =
            new ThreadLocal<UUID>();
    // Unlike manual/ordinary hold clicks, Auto Clicker trigger mode
    // carries its source-UUID/epoch into the real controller argument.
    private static final ThreadLocal<Minecraft189AutoClickerControllerLease.Intent>
            autoClickerControllerIntent =
            new ThreadLocal<Minecraft189AutoClickerControllerLease.Intent>();
    // M423 acceptance witness only; the ordinary production gate below
    // remains unchanged and active for both locked and unlocked Aura.
    private static boolean acceptanceUnlockedOwnerVetoReported;
    private static boolean acceptanceUnlockedOwnerFreshReported;
    private static UUID acceptanceUnlockedRemoteUuid;
    private static UUID acceptanceUnlockedLocalUuid;
    // M424 acceptance witness: both are genuine remote player UUIDs.
    private static boolean acceptanceRemotePairVetoReported;
    private static boolean acceptanceRemotePairFreshReported;
    private static UUID acceptanceRemotePairSelectedUuid;
    private static UUID acceptanceRemotePairOtherUuid;
    // M425 opt-in genuine manual melee witness, never production behavior.
    private static boolean acceptanceManualUnscopedNativeReported;
    // M426 isolated Auto Clicker controller proof; no production behavior.
    private static boolean acceptanceAutoClickerUnscopedNativeReported;
    private static int nativeTimerOriginalBaselineCallbacks;
    private static int nativeTimerOriginalBoostCallbacks;
    private static boolean nativeTimerOriginalEnabled;
    private static boolean nativeTimerOriginalFinished;
    private static int graphicalAcceptanceFrames;
    private static boolean graphicalAcceptanceReported;
    private static boolean acceptancePlayerObserved;
    private static boolean acceptanceWorldCombatObserved;
    private static int acceptanceWorldTicks;
    private static boolean acceptanceWorldReported;
    private static String acceptanceLastScreen;
    private static int acceptanceDisabledTargetTicks;
    private static boolean acceptanceAutoClickerEnabled;
    private static int acceptanceTriggerDisabledTargetTicks;
    private static boolean acceptanceTriggerEnabled;
    private static boolean acceptanceTriggerOwnedClickReported;
    private static Object killAuraNonPlayerTarget;
    private static int killAuraDisabledTargetTicks;
    private static int killAuraEnabledVetoTicks;
    // Strict original player identity continuity is tracked separately
    // from raw raycast streak, which cannot certify a single native mob.
    private static int killAuraNonPlayerMaxDisabledIdentityStreak;
    private static int killAuraNonPlayerMaxEnabledIdentityStreak;
    private static int killAuraNonPlayerSourceDiagCallbacks;
    private static boolean killAuraNonPlayerEnabled;
    private static boolean killAuraNonPlayerReported;
    private static int acceptanceRemotePlayerKindTicks;
    private static boolean acceptanceRemotePlayerKindFresh;
    private static boolean acceptanceRemotePlayerKindReported;
    private static int acceptanceAuraPlayerTargetTicks;
    private static int acceptanceAuraPlayerIndex = -1;
    private static boolean acceptanceAuraPlayerEnabled;
    private static boolean acceptanceAuraAttackReported;
    private static int acceptanceAuraFarTargetIndex = -1;
    private static int acceptanceAuraFarDisabledTicks;
    private static int acceptanceAuraFarEnabledTicks;
    private static boolean acceptanceAuraFarEnabled;
    private static boolean acceptanceAuraFarReported;
    private static int acceptanceAuraFarEvidenceChecks;
    private static int acceptanceAuraWallIndex = -1;
    private static int acceptanceAuraWallDisabledTicks;
    private static int acceptanceAuraWallEnabledTicks;
    private static boolean acceptanceAuraWallCheckArmed;
    private static boolean acceptanceAuraWallEnabled;
    private static boolean acceptanceAuraWallReported;
    private static int acceptanceWallLifecyclePhase;
    private static int acceptanceWallLifecycleDisabledSelectionTicks;
    private static int acceptanceWallLifecycleReenabledVetoTicks;
    private static int acceptanceWallLifecycleSourceEvidenceTicks;
    private static boolean acceptanceAuraWallRecoveryVisibleReported;
    private static boolean acceptanceAuraWallRecoveryClickReported;
    private static boolean acceptanceAuraWallRecoveryControllerReported;
    private static int acceptanceAuraFovTargetIndex = -1;
    private static int acceptanceAuraFovDisabledTicks;
    private static int acceptanceAuraFovEnabledTicks;
    private static int acceptanceAuraFovSourceDiagTicks;
    // Read-only original FOV acceptance provenance. Never used for target,
    // admission, rotation or any original source/World write.
    private static float acceptanceAuraFovLastRearYaw = Float.NaN;
    private static int acceptanceAuraFovLastRearTick = -1;
    private static int acceptanceAuraFovLastRearIndex = -1;
    private static final Minecraft189OriginalFovAuraAimTrace
            acceptanceAuraFovAimTrace =
            new Minecraft189OriginalFovAuraAimTrace();
    private static final Minecraft189OriginalFovGuardedYawWriteTrace
            acceptanceFovNativeYawWrites =
            new Minecraft189OriginalFovGuardedYawWriteTrace();
    private static boolean acceptanceAuraFovEnabled;
    private static boolean acceptanceAuraFovReported;

    /**
     * V284 diagnostic classification ONLY, never a click gate. Compares
     * two observed native source yaw values without modifying either.
     * A large difference demonstrates a rotation happened, NOT why.
     */
    static String m284FovYawShiftCause(final float lastRearYaw,
            final float currentYaw, final boolean hadRearWitness) {
        if (!hadRearWitness || !Float.isFinite(lastRearYaw)
                || !Float.isFinite(currentYaw))
            return "original-native-rear-yaw-provenance-unavailable";
        double change = (currentYaw - lastRearYaw) % 360.0D;
        if (change < -180.0D) change += 360.0D;
        if (change >= 180.0D) change -= 360.0D;
        if (Math.abs(change) >= 120.0D)
            return "original-native-yaw-changed-120-plus";
        if (Math.abs(change) > 0.5D)
            return "original-native-yaw-changed-under-120";
        return "original-native-yaw-stable";
    }

    private Minecraft189RuntimeBridge() {
    }

    /**
     * Opt-in original setter evidence. Invoked ONLY after the original
     * native yaw setter returns successfully through its existing lease.
     * No native getter or setter is invoked from these diagnostics.
     */
    static void acceptanceFovOriginalYawSetterSucceeded(final float yaw) {
        if (Boolean.getBoolean("custommc.acceptance.reportKillAuraFovVeto"))
            acceptanceFovNativeYawWrites.afterOriginalNativeSetter(yaw);
    }

    static int acceptanceFovOriginalYawWriteCount() {
        return acceptanceFovNativeYawWrites.completedWrites();
    }

    static void acceptanceFovAttributeOriginalYawWritesToAura(
            final int beforeOriginalAuraAim) {
        if (Boolean.getBoolean("custommc.acceptance.reportKillAuraFovVeto"))
            acceptanceFovNativeYawWrites.afterAuraAim(beforeOriginalAuraAim);
    }

    /**
     * Called only after the original player rotation lease remains owned,
     * observing the UNMODIFIED Aura aim return and pre-aim source snapshots.
     * The caller and recorder never change the original aim decision.
     */
    static void acceptanceFovObserveAuraAim(
            final boolean sourceYawAvailable, final float preAimYaw,
            final boolean selectedAvailable, final int selectedIndex,
            final float selectedYaw, final boolean aimReturnedTrue) {
        if (!Boolean.getBoolean("custommc.acceptance.reportKillAuraFovVeto"))
            return;
        acceptanceAuraFovAimTrace.observe(sourceYawAvailable, preAimYaw,
                selectedAvailable, selectedIndex, selectedYaw, aimReturnedTrue);
    }

    /**
     * Strict ORIGINAL offline-acceptance window for the actual vanilla
     * dedicated-server seven-block range-negative fixture.
     *
     * V165 moved its real remote player from four to seven blocks to avoid
     * native drift into Aura's configured 3.5 reach, but this M189 source
     * observer had continued requiring squared 3.8..4.8. It therefore
     * *always* rejected real 7.0 (49.0) and could never collect the
     * mandatory disabled-20 / enabled-80 source callbacks.
     *
     * This is ONLY a test-fixture source eligibility predicate. It does not
     * alter actual Aura range, target selection, packets, hits or credits.
     */
    static boolean originalSevenBlockRangeFixtureDistance(final double squared) {
        return Double.isFinite(squared)
                && squared >= 42.25D // original 6.5 blocks: true 7 +/- 0.5
                && squared <= 56.25D; // original 7.5 blocks
    }

    /**
     * Read-only classification of why a positive original Wall Check
     * lifecycle source witness cannot advance. These reasons NEVER replace
     * the real original 30 consecutive callbacks or grant attack permission.
     * Kept pure to test both absent data and genuine selected-target veto.
     */
    static String originalWallLifecycleSourceReason(
            final int targetIndex, final boolean availableKinds,
            final boolean availableCombat, final boolean availableVisibility,
            final int kindsCount, final int combatCount, final int visibilityCount,
            final boolean player, final boolean living,
            final boolean localPlayer, final boolean alive,
            final boolean occluded, final boolean auraEnabled,
            final boolean wallCheckEnabled,
            final boolean nearestFound, final boolean rotationAvailable) {
        if (targetIndex < 0) return "no-original-target-index";
        if (!availableKinds || !availableCombat || !availableVisibility)
            return "native-world-snapshot-unavailable";
        if (targetIndex >= kindsCount || targetIndex >= combatCount
                || targetIndex >= visibilityCount)
            return "source-index-outside-original-world";
        if (!player || !living || localPlayer || !alive)
            return "source-entity-no-longer-remote-living-player";
        if (!occluded) return "original-source-line-of-sight-no-longer-occluded";
        if (!auraEnabled) return "source-aura-disabled";
        if (!wallCheckEnabled) return "source-wall-check-disabled";
        if (nearestFound || rotationAvailable)
            return "stale-original-target-still-selected";
        return "real-occluded-and-both-target-snapshots-cleared";
    }

    private static String originalWallLifecycleReason(
            final int idx,
            final Minecraft189WorldEntityKindState.Snapshot kinds,
            final Minecraft189WorldEntityCombatState.Snapshot combat,
            final Minecraft189WorldEntityVisibilityState.Snapshot los,
            final Minecraft189NearestPlayerTargetState.Snapshot nearest,
            final Minecraft189TargetRotationState.Snapshot target,
            final Minecraft189HostRuntime host) {
        final boolean inBounds = idx >= 0
                && kinds.available() && combat.available() && los.available()
                && idx < kinds.entityCount() && idx < combat.entityCount()
                && idx < los.entityCount();
        return originalWallLifecycleSourceReason(
                idx, kinds.available(), combat.available(), los.available(),
                kinds.entityCount(), combat.entityCount(), los.entityCount(),
                inBounds && kinds.player(idx),
                inBounds && kinds.living(idx),
                inBounds && kinds.localPlayer(idx),
                inBounds && combat.alive(idx),
                inBounds && los.occluded(idx),
                host.featureCatalog().killAura().active(),
                host.featureCatalog().wallCheck().active(),
                nearest.found(), target.available());
    }

    static synchronized Registration install(
            final Minecraft189BootstrapRuntime runtime) {
        final Minecraft189BootstrapRuntime next =
                Objects.requireNonNull(
                        runtime,
                        "runtime");
        if (activeRuntime != null) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 runtime bridge is already active");
        }
        activeRuntime = next;
        auraDispatchUuid.remove();
        triggerDispatchUuid.remove();
        acceptanceUnlockedOwnerVetoReported = false;
        acceptanceUnlockedOwnerFreshReported = false;
        acceptanceUnlockedRemoteUuid = null;
        acceptanceUnlockedLocalUuid = null;
        acceptanceRemotePairVetoReported = false;
        acceptanceRemotePairFreshReported = false;
        acceptanceRemotePairSelectedUuid = null;
        acceptanceRemotePairOtherUuid = null;
        acceptanceManualUnscopedNativeReported = false;
        acceptanceAutoClickerUnscopedNativeReported = false;
        nativeTimerOriginalBaselineCallbacks = 0;
        nativeTimerOriginalBoostCallbacks = 0;
        nativeTimerOriginalEnabled = false;
        nativeTimerOriginalFinished = false;
        graphicalAcceptanceFrames = 0;
        graphicalAcceptanceReported = false;
        acceptancePlayerObserved = false;
        acceptanceWorldCombatObserved = false;
        acceptanceWorldTicks = 0;
        acceptanceWorldReported = false;
        acceptanceLastScreen = null;
        Minecraft189VanillaMeleeAcceptance.reset();
        Minecraft189MultiTargetAcceptance.reset();
        Minecraft189SimultaneousTargetAcceptance.reset();
        Minecraft189LockedTargetAcceptance.reset();
        Minecraft189RejoinTargetAcceptance.reset();
        originalScaffoldRayTraceEvidence.reset();
        Minecraft189OriginalScaffoldNativeRayReturnTrace.reset();
        acceptanceDisabledTargetTicks = 0;
        acceptanceAutoClickerEnabled = false;
        acceptanceTriggerDisabledTargetTicks = 0;
        acceptanceTriggerEnabled = false;
        acceptanceTriggerOwnedClickReported = false;
        killAuraNonPlayerTarget = null;
        killAuraDisabledTargetTicks = 0;
        killAuraEnabledVetoTicks = 0;
        killAuraNonPlayerMaxDisabledIdentityStreak = 0;
        killAuraNonPlayerMaxEnabledIdentityStreak = 0;
        killAuraNonPlayerSourceDiagCallbacks = 0;
        killAuraNonPlayerEnabled = false;
        killAuraNonPlayerReported = false;
        acceptanceRemotePlayerKindTicks = 0;
        acceptanceRemotePlayerKindFresh = false;
        acceptanceRemotePlayerKindReported = false;
        acceptanceAuraPlayerTargetTicks = 0;
        acceptanceAuraPlayerIndex = -1;
        acceptanceAuraPlayerEnabled = false;
        acceptanceAuraAttackReported = false;
        acceptanceAuraFarTargetIndex = -1;
        acceptanceAuraFarDisabledTicks = 0;
        acceptanceAuraFarEnabledTicks = 0;
        acceptanceAuraFarEnabled = false;
        acceptanceAuraFarReported = false;
        acceptanceAuraFarEvidenceChecks = 0;
        acceptanceAuraWallIndex = -1;
        acceptanceAuraWallDisabledTicks = 0;
        acceptanceAuraWallEnabledTicks = 0;
        acceptanceAuraWallCheckArmed = false;
        acceptanceAuraWallEnabled = false;
        acceptanceAuraWallReported = false;
        acceptanceWallLifecyclePhase = 0;
        acceptanceWallLifecycleDisabledSelectionTicks = 0;
        acceptanceWallLifecycleReenabledVetoTicks = 0;
        acceptanceWallLifecycleSourceEvidenceTicks = 0;
        acceptanceAuraWallRecoveryVisibleReported = false;
        acceptanceAuraWallRecoveryClickReported = false;
        acceptanceAuraWallRecoveryControllerReported = false;
        acceptanceAuraFovTargetIndex = -1;
        acceptanceAuraFovDisabledTicks = 0;
        acceptanceAuraFovEnabledTicks = 0;
        acceptanceAuraFovSourceDiagTicks = 0;
        acceptanceAuraFovLastRearYaw = Float.NaN;
        acceptanceAuraFovLastRearTick = -1;
        acceptanceAuraFovLastRearIndex = -1;
        acceptanceAuraFovAimTrace.reset();
        acceptanceFovNativeYawWrites.reset();
        acceptanceAuraFovEnabled = false;
        acceptanceAuraFovReported = false;
        scaffoldOriginalContextReported = false;
        scaffoldOriginalCandidateReported = false;
        scaffoldOriginalCandidateDiagTicks = 0;
        scaffoldOriginalPlacementReadback = null;
        scaffoldOriginalPlacementReadbackTicks = 0;
        scaffoldOriginalPlacementReadbackEpoch = 0L;
        scaffoldOriginalPlacementActivation = 0L;
        scaffoldOriginalSourceEpoch.invalidate();
        scaffoldOriginalPlacementReadbackReported = false;
        scaffoldOriginalPlacementNativeDiagTicks = 0;
        Minecraft189ScaffoldNativePlacementAttempt.resetOriginalV29Trace();
        scaffoldAcceptanceEnableIssued = false;
        scaffoldAcceptancePendingDiagTicks = 0;
        scaffoldMenuPrearmHeartbeatTicks = 0;
        scaffoldRepeatTick = 0L;
        scaffoldRepeatProofStarted = false;
        scaffoldRepeatPositiveAttempts = 0;
        scaffoldRepeatPositiveReadbacks = 0;
        scaffoldProductionReceiptReported = 0;
        scaffoldProductionCompletionReported = false;
        scaffoldThreeGuiAbortObserved = false;
        scaffoldThreeGuiResumeClearTicks = 0;
        scaffoldThreeHotbarAbortObserved = false;
        scaffoldThreeHotbarAbortActivation = 0L;
        scaffoldThreeHotbarResumeTicks = 0;
        scaffoldKeyboardWalkDiagnosticsTicks = 0;
        scaffoldMenuNegativeClearStability.reset();
        scaffoldDisabledProofTicks = 0;
        scaffoldDisabledProofReported = false;
        return new Registration(next);
    }

    /**
     * Invoked solely after the genuine Entity.moveEntity isSneaking call.
     * Vanilla's own onGround/player/collision tests remain authoritative.
     * Remote Entities never implement the local player marker.
     */
    private static boolean acceptanceRealSafeWalkReported;

    public static synchronized boolean nativeSafeWalkSneaking(
            final boolean originalSneaking, final Object nativeEntity) {
        if (originalSneaking || activeRuntime == null)
            return originalSneaking;
        final boolean adjusted = activeRuntime.nativeSafeWalkSneaking(
                originalSneaking, nativeEntity);
        // Original Entity.moveEntity invoked the exact vanilla edge branch
        // with a source-mapped EntityPlayerSP, module ON, and no user sneak.
        if (adjusted && !acceptanceRealSafeWalkReported
                && Boolean.getBoolean("custommc.acceptance.requireNativeSafeWalk")
                && nativeEntity instanceof Minecraft189LocalPlayerSafeWalkMarker) {
            acceptanceRealSafeWalkReported = true;
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_NATIVE_LOCAL_SAFEWALK_BRANCH_PASS=YES");
            System.out.flush();
        }
        return adjusted;
    }

    public static synchronized boolean active() {
        return activeRuntime != null
                && !activeRuntime.closed();
    }

    public static synchronized boolean hostInstalled() {
        return activeRuntime != null
                && activeRuntime.hostInstalled();
    }

    /**
     * Explicit CI-only stop after the real transformed Minecraft main hook.
     * Normal launches never set this property and continue unchanged.
     */
    static final String MAIN_ENTRY_ACCEPTANCE_PROPERTY =
            "custommc.acceptance.stopAtMinecraftMain";

    public static synchronized void targetMainEntered() {
        requireRuntime()
                .markTargetMainEntered();
        if (Boolean.getBoolean(MAIN_ENTRY_ACCEPTANCE_PROPERTY)) {
            throw new IllegalStateException(
                    "CUSTOMMC_OFFICIAL_189_MAIN_ENTRY_PROBE_REACHED");
        }
    }

    public static synchronized Minecraft189HostRuntime installHost(
            final LegacyUiHostCallbacks hostCallbacks) {
        return requireRuntime()
                .installHost(
                        Objects.requireNonNull(
                                hostCallbacks,
                                "hostCallbacks"));
    }

    // V18: original-game acceptance ONLY. Three independent source-native
    // callbacks must publish inside the same explicit tick-owned window.
    private static final Minecraft189ScaffoldNativeTickParity scaffoldTickParity =
            new Minecraft189ScaffoldNativeTickParity();
    private static boolean scaffoldTickParityReported;

    private static void reportScaffoldTickParity() {
        if (scaffoldTickParityReported
                || !Boolean.getBoolean(
                        "custommc.acceptance.requireNativeScaffoldTickParity"))
            return;
        final Minecraft189ScaffoldNativeTickParity.Witness witness =
                scaffoldTickParity.consume();
        if (witness == null) return;
        scaffoldTickParityReported = true;
        System.out.println(
                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_TICK_PARITY_PASS=YES"
                + " tick=" + witness.tick
                + " selectedSlot=" + witness.selectedSlot
                + " originalStackSize=" + witness.originalStackSize
                + " floor=" + witness.playerX + "," + witness.supportY + ","
                + witness.playerZ);
        System.out.flush();
    }

    // V23 original-game acceptance only. A source-original Minecraft object
    // is passed directly from its runTick RETURN, never reconstructed from
    // a cached singleton, source position estimate or fake mouse-over hit.
    private static boolean scaffoldOriginalContextReported;
    private static boolean scaffoldOriginalCandidateReported;
    private static int scaffoldOriginalCandidateDiagTicks;
    private static final Minecraft189ScaffoldSourceEpoch scaffoldOriginalSourceEpoch =
            new Minecraft189ScaffoldSourceEpoch();
    private static final Minecraft189ScaffoldActivationSource scaffoldOriginalActivationSource =
            new Minecraft189ScaffoldActivationSource();
    private static long scaffoldOriginalActivationLease;
    private static long scaffoldOriginalPlacementReadbackEpoch;
    private static long scaffoldOriginalPlacementActivation;
    private static Minecraft189ScaffoldNativePlacementAttempt.Result
            scaffoldOriginalPlacementReadback;
    private static int scaffoldOriginalPlacementReadbackTicks;
    private static boolean scaffoldOriginalPlacementReadbackReported;
    private static int scaffoldOriginalPlacementNativeDiagTicks;
    private static int scaffoldDisabledProofTicks;
    private static boolean scaffoldDisabledProofReported;
    private static boolean scaffoldAcceptanceEnableIssued;
    private static int scaffoldAcceptancePendingDiagTicks;
    private static int scaffoldMenuPrearmHeartbeatTicks;
    // V37: acceptance-only original-Mojang two-block proof. Never active
    // during ordinary gameplay or the certified V35 single-block fixture.
    private static long scaffoldRepeatTick;
    private static boolean scaffoldRepeatProofStarted;
    private static int scaffoldRepeatPositiveAttempts;
    private static int scaffoldRepeatPositiveReadbacks;
    private static int scaffoldProductionReceiptReported;
    private static boolean scaffoldProductionCompletionReported;
    private static boolean scaffoldProductionHistoricalVetoReported;
    private static int scaffoldKeyboardWalkDiagnosticsTicks;
    // V308: acceptance-only observations for real E after one receipted
    // original controller placement. No persistent native game references.
    private static boolean scaffoldThreeGuiAbortObserved;
    private static int scaffoldThreeGuiResumeClearTicks;
    // V309: original-game acceptance-only hotbar switch witness. No native
    // objects survive callbacks and no inventory/controller write is made.
    private static boolean scaffoldThreeHotbarAbortObserved;
    private static long scaffoldThreeHotbarAbortActivation;
    private static int scaffoldThreeHotbarResumeTicks;
    private static final Minecraft189ScaffoldGuiClearStability
            scaffoldMenuNegativeClearStability = new Minecraft189ScaffoldGuiClearStability();

    /**
     * Original-game CI controls when to enable the real module through its
     * normal controller. This prevents initial world-loading/pause screens
     * from consuming an arm before the vanilla fixture supplies ItemBlock.
     * Never used in production without the explicit acceptance property.
     */
    private static boolean originalScaffoldAcceptanceMayArm(
            final Object minecraft, final boolean guiNegative) {
        if (!(minecraft instanceof Minecraft189ScaffoldNativeContextAccess)) {
            if (guiNegative) scaffoldMenuNegativeClearStability.reset();
            return false;
        }
        final Minecraft189ScaffoldScreenGate.Verdict screen =
                Minecraft189ScaffoldScreenGate.inspect(minecraft);
        try {
            final Minecraft189ScaffoldNativeContextAccess original =
                    (Minecraft189ScaffoldNativeContextAccess)minecraft;
            if (guiNegative) {
                final Object[] nativeSource = original.customMcScaffoldNativeContext();
                if (nativeSource == null || nativeSource.length != 3
                        || !(nativeSource[0] instanceof Minecraft189LocalPlayerSafeWalkMarker)
                        || !(nativeSource[0] instanceof Minecraft189PlayerPositionAccess)
                        || !(nativeSource[1] instanceof Minecraft189ScaffoldWorldReadAccess)) {
                    scaffoldMenuNegativeClearStability.reset();
                    return false;
                }
                // V49: never count a startup creative/empty pre-grant
                // World as ready. The original server fixture must already
                // have provided EIGHT real selected hotbar ItemBlocks. V48
                // proved V35 could otherwise enable in the earlier startup
                // World, then revoke before the harness' actual E keypress.
                if (!Minecraft189ScaffoldSelectedHotbar.hasGenuineSelectedBlockCount(
                        nativeSource[0], 8)) {
                    scaffoldMenuNegativeClearStability.reset();
                    return false;
                }
                // Initial World/login/pause screens may briefly report CLEAR
                // just before another vanilla menu opens. Count 60 truly
                // consecutive original World/Player+selected-eight+GUI-clear
                // ticks BEFORE the one controller enable. A later OPEN
                // still revokes through the original production V35 veto.
                return scaffoldMenuNegativeClearStability.observe(
                        minecraft, nativeSource[0], nativeSource[1],
                        screen == Minecraft189ScaffoldScreenGate.Verdict.CLEAR);
            }
            if (screen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR)
                return false;
            final Minecraft189ScaffoldNativeContext.Witness witness =
                    Minecraft189ScaffoldNativeContext.capture(original);
            if (witness == null
                    || !(witness.player instanceof Minecraft189PlayerInventoryAccess))
                return false;
            final Minecraft189InventoryHotbarAccess inventory =
                    ((Minecraft189PlayerInventoryAccess)witness.player)
                            .customMcInventory();
            if (!(inventory instanceof Minecraft189InventoryHotbarItemsAccess))
                return false;
            final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                    Minecraft189ScaffoldSelectedHotbar.from(
                            (Minecraft189InventoryHotbarItemsAccess)inventory);
            // The real fixture grants EIGHT stone after changing to Survival.
            // A startup creative inventory or empty pre-grant hotbar cannot
            // arm the controller and then be revoked by an incidental GUI.
            return selected != null && selected.sourceStackSize == 8;
        } catch (RuntimeException sourceUnavailable) {
            if (guiNegative) scaffoldMenuNegativeClearStability.reset();
            return false;
        }
    }


    public static synchronized void originalScaffoldContextAtTick(final Object minecraft) {
        if (activeRuntime == null || activeRuntime.closed()) {
            scaffoldOriginalPlacementReadback = null;
            scaffoldOriginalPlacementReadbackEpoch = 0L;
            scaffoldOriginalPlacementActivation = 0L;
            scaffoldOriginalSourceEpoch.invalidate();
            scaffoldOriginalActivationSource.reset();
            scaffoldOriginalActivationLease = 0L;
            return;
        }
        // Track exact original Minecraft/World/player identities only while
        // the opt-in placement proof is active or a receipt is outstanding.
        final Minecraft189ScaffoldModule scaffold = activeRuntime.scaffoldModule();
        // V308: after genuine inventory E revoked a single-receipt three-click
        // activation, observe 120 real GUI-clear runTick callbacks. The server
        // separately confirms exactly one stone, seven items, and two AIR cells.
        // This witness never grants placement or rewrites player/World state.
        if (Boolean.getBoolean(
                "custommc.acceptance.requireProductionScaffoldThreeGuiAbort")
                && scaffoldThreeGuiAbortObserved) {
            if (scaffold == null || scaffold.active())
                throw new IllegalStateException(
                        "V308 three-placement activation restarted after GUI veto");
            boolean nativeClear = false;
            if (minecraft instanceof Minecraft189ScaffoldNativeContextAccess
                    && Minecraft189ScaffoldScreenGate.inspect(minecraft)
                            == Minecraft189ScaffoldScreenGate.Verdict.CLEAR) {
                try {
                    final Object[] nativeSource =
                            ((Minecraft189ScaffoldNativeContextAccess)minecraft)
                                    .customMcScaffoldNativeContext();
                    nativeClear = nativeSource != null
                            && nativeSource.length == 3
                            && nativeSource[0] instanceof Minecraft189PlayerPositionAccess
                            && nativeSource[1] != null
                            && Math.abs(((Minecraft189PlayerPositionAccess)
                                    nativeSource[0]).customMcPositionY() - 100.0D)
                                    < 0.15D
                            && Minecraft189ScaffoldSelectedHotbar
                                    .hasGenuineSelectedBlockCount(nativeSource[0], 7);
                } catch (RuntimeException unavailableNativeSource) {
                    nativeClear = false;
                }
            }
            if (nativeClear) {
                if (scaffoldThreeGuiResumeClearTicks < 120
                        && ++scaffoldThreeGuiResumeClearTicks % 60 == 0) {
                    System.out.println(
                            "CUSTOMMC_189_SCAFFOLD_THREE_GUI_RESUMED_DISABLED_TICKS="
                            + scaffoldThreeGuiResumeClearTicks);
                    System.out.flush();
                }
            } else {
                scaffoldThreeGuiResumeClearTicks = 0;
            }
        }
        // V309: after a physical 2->1 hotbar selection revoked the live
        // three-step sequence, its original activation must stay STOPPED.
        // Require 120 original GUI-clear runTicks with exactly seven native
        // selected ItemBlocks and real player Y=100. Server checks remain
        // independent of this non-authorizing read-only witness.
        if (Boolean.getBoolean(
                "custommc.acceptance.requireProductionScaffoldThreeHotbarAbort")
                && scaffoldThreeHotbarAbortObserved) {
            if (scaffold == null || !scaffold.active()
                    || !scaffold.productionRepeatEnabledForActivation()
                    || !scaffold.repeatProofStopped()
                    || scaffold.activation() != scaffoldThreeHotbarAbortActivation)
                throw new IllegalStateException(
                        "V309 original hotbar veto resumed or changed activation");
            boolean actualSource = false;
            if (minecraft instanceof Minecraft189ScaffoldNativeContextAccess
                    && Minecraft189ScaffoldScreenGate.inspect(minecraft)
                            == Minecraft189ScaffoldScreenGate.Verdict.CLEAR) {
                try {
                    final Object[] nativeSource =
                            ((Minecraft189ScaffoldNativeContextAccess)minecraft)
                                    .customMcScaffoldNativeContext();
                    actualSource = nativeSource != null
                            && nativeSource.length == 3
                            && nativeSource[0] instanceof Minecraft189PlayerPositionAccess
                            && nativeSource[1] != null
                            && Math.abs(((Minecraft189PlayerPositionAccess)
                                    nativeSource[0]).customMcPositionY() - 100.0D)
                                    < 0.15D
                            && Minecraft189ScaffoldSelectedHotbar
                                    .hasGenuineSelectedBlockCount(nativeSource[0], 7);
                } catch (RuntimeException unavailableOriginalSource) {
                    actualSource = false;
                }
            }
            if (actualSource) {
                if (scaffoldThreeHotbarResumeTicks < 120
                        && ++scaffoldThreeHotbarResumeTicks % 60 == 0) {
                    System.out.println(
                            "CUSTOMMC_189_SCAFFOLD_THREE_HOTBAR_RESUMED_VETO_TICKS="
                            + scaffoldThreeHotbarResumeTicks);
                    System.out.flush();
                }
            } else {
                scaffoldThreeHotbarResumeTicks = 0;
            }
        }
        // V48 CI-only: record that the original Mojang runTick hook really
        // executes, even when the positive readiness path NEVER enters.
        // Source-only evidence, no original action, no selector/module write.
        if (Boolean.getBoolean("custommc.acceptance.requireOriginalScaffoldMenuVeto")
                && (++scaffoldMenuPrearmHeartbeatTicks == 1
                    || scaffoldMenuPrearmHeartbeatTicks % 20 == 0)) {
            // CI observation only. Unlike the positive-placement candidate,
            // GUI prearm needs no cursor hit, but it MUST prove the actual
            // selected eight-block native Survival grant.
            boolean originalEightGranted = false;
            if (minecraft instanceof Minecraft189ScaffoldNativeContextAccess) {
                final Object[] originalSource =
                        ((Minecraft189ScaffoldNativeContextAccess)minecraft)
                                .customMcScaffoldNativeContext();
                originalEightGranted = originalSource != null
                        && originalSource.length == 3
                        && Minecraft189ScaffoldSelectedHotbar
                                .hasGenuineSelectedBlockCount(originalSource[0], 8);
            }
            System.out.println("CUSTOMMC_189_SCAFFOLD_MENU_PREARM_HEARTBEAT"
                    + " tick=" + scaffoldMenuPrearmHeartbeatTicks
                    + " modulePresent=" + (scaffold != null)
                    + " moduleArmed=" + (scaffold != null && scaffold.armed())
                    + " acceptanceEnableIssued=" + scaffoldAcceptanceEnableIssued
                    + " stableGuiClearTicks="
                    + scaffoldMenuNegativeClearStability.witnessedClearTicks()
                    + " nativeSelectedEightGrant=" + originalEightGranted
                    + " originalScreen="
                    + Minecraft189ScaffoldScreenGate.inspect(minecraft)
                    + " source="
                    + Minecraft189ScaffoldOriginalCandidateProbe.diagnose(minecraft));
            System.out.flush();
        }
        final boolean twoBlockOriginalProof = Boolean.getBoolean(
                "custommc.acceptance.requireOriginalScaffoldTwoBlockProof");
        final boolean productionTwoBlockProof = Boolean.getBoolean(
                "custommc.acceptance.requireProductionScaffoldTwoBlockProof");
        final boolean productionThreeBlockProof = Boolean.getBoolean(
                "custommc.acceptance.requireProductionScaffoldThreeBlockProof");
        final boolean productionFourBlockProof = Boolean.getBoolean(
                "custommc.acceptance.requireProductionScaffoldFourBlockProof");
        final boolean productionFiveBlockProof = Boolean.getBoolean(
                "custommc.acceptance.requireProductionScaffoldFiveBlockProof");
        final boolean productionSixBlockProof = Boolean.getBoolean(
                "custommc.acceptance.requireProductionScaffoldSixBlockProof");
        if ((productionTwoBlockProof ? 1 : 0)
                + (productionThreeBlockProof ? 1 : 0)
                + (productionFourBlockProof ? 1 : 0)
                + (productionFiveBlockProof ? 1 : 0)
                + (productionSixBlockProof ? 1 : 0) > 1)
            throw new IllegalStateException("conflicting original Scaffold proof quotas");
        final boolean productionRepeatProof = productionTwoBlockProof
                || productionThreeBlockProof || productionFourBlockProof
                || productionFiveBlockProof || productionSixBlockProof;
        final boolean needsAcceptanceEnable = scaffold != null
                && !scaffoldAcceptanceEnableIssued
                && (Boolean.getBoolean(
                        "custommc.acceptance.requireOriginalScaffoldPlacement")
                        || twoBlockOriginalProof || productionRepeatProof);
        final boolean acceptanceReady = needsAcceptanceEnable
                && originalScaffoldAcceptanceMayArm(minecraft,
                        Boolean.getBoolean(
                                "custommc.acceptance.requireOriginalScaffoldMenuVeto"));
        if (needsAcceptanceEnable && !acceptanceReady
                && ++scaffoldAcceptancePendingDiagTicks % 100 == 0) {
            // Strictly observational, bounded original-source diagnostics.
            // Never alter the source cursor, hotbar, controller or module.
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAIT"
                    + " screen=" + Minecraft189ScaffoldScreenGate.inspect(minecraft)
                    + " source=" + Minecraft189ScaffoldOriginalCandidateProbe
                            .diagnose(minecraft));
            System.out.flush();
        }
        if (acceptanceReady) {
            // One grant per boot/runtime, never re-arm after a veto, World
            // switch or controller failure. The ordinary production module
            // is disabled by default and user toggles remain unchanged.
            if (productionRepeatProof && !twoBlockOriginalProof) {
                // CI-only configuration of exactly ONE real production
                // opt-in setting before ModuleController grants a new
                // activation. Never transfer control to test-only clicks.
                if (productionSixBlockProof)
                    scaffold.sixPlacementsSetting().set(Boolean.TRUE);
                else if (productionFiveBlockProof)
                    scaffold.fivePlacementsSetting().set(Boolean.TRUE);
                else if (productionFourBlockProof)
                    scaffold.fourPlacementsSetting().set(Boolean.TRUE);
                else if (productionThreeBlockProof)
                    scaffold.threePlacementsSetting().set(Boolean.TRUE);
                else
                    scaffold.twoPlacementsSetting().set(Boolean.TRUE);
                if (Boolean.getBoolean(
                        "custommc.acceptance.requireProductionScaffoldKeyboardWalkProof"))
                    scaffold.adjacentEdgeTargetsSetting().set(Boolean.TRUE);
            }
            activeRuntime.moduleController().enable(Minecraft189ScaffoldModule.ID);
            scaffoldAcceptanceEnableIssued = true;
            if (productionRepeatProof
                    && scaffold.productionRepeatEnabledForActivation()) {
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_MODULE_ARMED=YES"
                        + " activation=" + scaffold.activation());
                System.out.flush();
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireOriginalScaffoldMenuVeto")) {
                System.out.println(
                    "CUSTOMMC_OFFICIAL_189_SCAFFOLD_GUI_ARMED_IN_WORLD=YES");
                System.out.flush();
            }
        }
        final long moduleActivation = scaffold == null ? 0L : scaffold.activation();
        // A module armed before any World is loaded waits for its FIRST valid
        // World/player. After that, losing or changing either revokes the
        // entire activation, even if there has been no placement attempt.
        if (moduleActivation != scaffoldOriginalActivationLease) {
            scaffoldOriginalActivationSource.reset();
            scaffoldOriginalActivationLease = moduleActivation;
        }
        if (moduleActivation > 0L
                && scaffoldOriginalActivationSource.observe(minecraft)
                        == Minecraft189ScaffoldActivationSource.Verdict.REVOKED) {
            activeRuntime.moduleController().disable(Minecraft189ScaffoldModule.ID);
            scaffoldOriginalPlacementReadback = null;
            scaffoldOriginalPlacementReadbackEpoch = 0L;
            scaffoldOriginalPlacementActivation = 0L;
            scaffoldOriginalPlacementReadbackTicks = 0;
            scaffoldOriginalSourceEpoch.invalidate();
            return;
        }
        // A native menu/inventory screen must never admit a controller
        // placement or complete a pending receipt. The source field is the
        // exact original 'ave' -> 'axu' GUI owner verified by SHA-pinned
        // Mojang ASM preflight, not a guessed reflective singleton.
        // UNKNOWN is a veto too. Cancel through ModuleController so the
        // old activation cannot silently resume when a menu closes.
        final Minecraft189ScaffoldScreenGate.Verdict originalScreen =
                moduleActivation > 0L && scaffoldOriginalActivationSource.witnessed()
                ? Minecraft189ScaffoldScreenGate.inspect(minecraft)
                : Minecraft189ScaffoldScreenGate.Verdict.CLEAR;
        if (originalScreen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR) {
            // V308: a REAL E inventory opened after exactly one received
            // production click must cancel the remaining live activation.
            // Ordinary production game logic remains the original GUI veto.
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireProductionScaffoldThreeGuiAbort")
                    && scaffold != null
                    && scaffold.productionRepeatEnabledForActivation()
                    && scaffold.productionRepeatCountForActivation() == 3
                    && scaffold.productionPositiveReceipts() == 1
                    && scaffold.productionRepeatAttemptedCount() == 1) {
                scaffoldThreeGuiAbortObserved = true;
                scaffoldThreeGuiResumeClearTicks = 0;
                System.out.println(
                        "CUSTOMMC_189_SCAFFOLD_THREE_REAL_GUI_ABORT_AFTER_ONE_PASS=YES"
                        + " state=" + originalScreen);
                System.out.flush();
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireOriginalScaffoldMenuVeto")) {
                System.out.println(
                    "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_GUI_VETO=YES state="
                    + originalScreen);
                System.out.flush();
            }
            activeRuntime.moduleController().disable(Minecraft189ScaffoldModule.ID);
            scaffoldOriginalPlacementReadback = null;
            scaffoldOriginalPlacementReadbackEpoch = 0L;
            scaffoldOriginalPlacementActivation = 0L;
            scaffoldOriginalPlacementReadbackTicks = 0;
            scaffoldOriginalSourceEpoch.invalidate();
            return;
        }
        // Only the SHA-pinned ORIGINAL Mojang CI two-block test can use
        // V36's isolated repeat transaction. It is not exposed in ClickGUI
        // and regular gameplay still permits just one click per enable.
        // This is a single original runTick callback, no retained World,
        // player, cursor, ItemStack or controller references.
        if (twoBlockOriginalProof && scaffold != null && moduleActivation > 0L) {
            if (scaffoldRepeatTick == Long.MAX_VALUE) {
                activeRuntime.moduleController().disable(Minecraft189ScaffoldModule.ID);
                return;
            }
            final long repeatTick = ++scaffoldRepeatTick;
            if (!scaffoldRepeatProofStarted && scaffold.armed()
                    && scaffold.beginRepeatProof(minecraft, repeatTick, 2, 4)) {
                scaffoldRepeatProofStarted = true;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_REPEAT_MODULE_ARMED=YES"
                        + " activation=" + moduleActivation);
                System.out.flush();
            }
            if (scaffoldRepeatProofStarted && !scaffold.repeatProofStopped()) {
                if (scaffold.repeatProofReceipt(minecraft, repeatTick)) {
                    scaffoldRepeatPositiveReadbacks++;
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_REPEAT_CLIENT_READBACK_PASS=YES"
                            + " step=" + scaffoldRepeatPositiveReadbacks);
                    System.out.flush();
                }
                if (!scaffold.repeatProofStopped()) {
                    final Minecraft189ScaffoldNativePlacementAttempt.Result step =
                            scaffold.repeatProofAttempt(minecraft, repeatTick);
                    if (step != null) {
                        scaffoldRepeatPositiveAttempts++;
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_REPEAT_ORIGINAL_CONTROLLER_ATTEMPT=YES"
                                + " step=" + scaffoldRepeatPositiveAttempts
                                + " accepted=" + step.originalControllerAccepted
                                + " target=" + step.targetX + "," + step.targetY
                                + "," + step.targetZ
                                + " selectedBefore=" + step.originalStackSize);
                        System.out.flush();
                    }
                }
            }
            // A single accepted original WorldClient receipt is not yet
            // evidence of the second click. Bound the diagnostic volume
            // while this explicit Mojang-only proof is waiting, including
            // after a fail-shut stop, to expose the precise original ray /
            // selected ItemStack / source-cadence blocker. Observation only.
            if (scaffoldRepeatProofStarted
                    && scaffoldRepeatPositiveReadbacks == 1
                    && scaffoldRepeatPositiveAttempts == 1
                    && repeatTick % 80L == 0L) {
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_REPEAT_SECOND_STEP_DIAG"
                        + " screen=" + Minecraft189ScaffoldScreenGate.inspect(minecraft)
                        + " sequence=" + scaffold.repeatProofDiagnostics(minecraft)
                        + " native=" + Minecraft189ScaffoldOriginalCandidateProbe
                                .diagnose(minecraft));
                System.out.flush();
            }
            if (scaffoldRepeatProofStarted && scaffold.repeatProofStopped()
                    && scaffoldRepeatPositiveReadbacks == 2) {
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_REPEAT_TWO_CLIENT_RECEIPTS_PASS=YES"
                        + " attempts=" + scaffoldRepeatPositiveAttempts);
                System.out.flush();
                // Completion is reported exactly once; no rearming.
                scaffoldRepeatPositiveReadbacks = 3;
            }
        }
        // V42: regular, GUI-clear original runTick path. The setting is
        // OFF by default, is sampled only at module onEnable, and never runs
        // in V37's explicit acceptance-only repeat proof. Prevent the V35
        // one-shot path below from racing the module-owned transaction.
        // Native World/player/session and screen revocation already ran.
        if (!twoBlockOriginalProof && scaffold != null
                && scaffold.productionRepeatEnabledForActivation()) {
            final Minecraft189ScaffoldNativePlacementAttempt.Result step =
                    scaffold.productionRepeatAtOriginalTick(minecraft, true);
            if (productionRepeatProof) {
                // V309: exact original selected slot AFTER production code
                // rejected its own originally pinned ItemBlock slot lease.
                // Physical X11 key '2' is sent only by the CI host, never
                // through a test inventory setter or injected click.
                if (Boolean.getBoolean(
                        "custommc.acceptance.requireProductionScaffoldThreeHotbarAbort")
                        && !scaffoldThreeHotbarAbortObserved
                        && scaffold.productionRepeatCountForActivation() == 3
                        && scaffold.productionPositiveReceipts() == 1
                        && scaffold.repeatProofStopped()
                        && scaffold.productionRepeatStarted()
                        && minecraft instanceof Minecraft189ScaffoldNativeContextAccess) {
                    final Object[] nativeSource =
                            ((Minecraft189ScaffoldNativeContextAccess)minecraft)
                                    .customMcScaffoldNativeContext();
                    if (nativeSource != null && nativeSource.length == 3
                            && nativeSource[0] instanceof Minecraft189PlayerInventoryAccess) {
                        final Minecraft189InventoryHotbarAccess inventory =
                                ((Minecraft189PlayerInventoryAccess)nativeSource[0])
                                        .customMcInventory();
                        if (inventory != null
                                && inventory.customMcSelectedHotbarSlot() == 1) {
                            scaffoldThreeHotbarAbortObserved = true;
                            scaffoldThreeHotbarAbortActivation = scaffold.activation();
                            scaffoldThreeHotbarResumeTicks = 0;
                            System.out.println(
                                "CUSTOMMC_189_SCAFFOLD_THREE_REAL_HOTBAR_ABORT_AFTER_ONE_PASS=YES slot=1");
                            System.out.flush();
                        }
                    }
                }
                // V58: independent CI-only observation of a native candidate
                // rejected SPECIFICALLY because this module activation
                // already receipted its coordinates. No source is fabricated.
                if (productionThreeBlockProof
                        && Boolean.getBoolean(
                            "custommc.acceptance.requireProductionScaffoldHistoryVeto")
                        && !scaffoldProductionHistoricalVetoReported
                        && scaffold.productionHistoricalTargetVetoObserved()) {
                    scaffoldProductionHistoricalVetoReported = true;
                    System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_HISTORY_REUSE_VETO=YES");
                    System.out.flush();
                }
                final int receipts = scaffold.productionPositiveReceipts();
                while (scaffoldProductionReceiptReported < receipts) {
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_CLIENT_READBACK_PASS=YES"
                            + " step=" + ++scaffoldProductionReceiptReported);
                    System.out.flush();
                }
                if (step != null) {
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_ORIGINAL_CONTROLLER_ATTEMPT=YES"
                            + " step=" + scaffold.productionRepeatAttemptedCount()
                            + " accepted=" + step.originalControllerAccepted
                            + " target=" + step.targetX + "," + step.targetY
                            + "," + step.targetZ
                            + " selectedBefore=" + step.originalStackSize);
                    System.out.flush();
                    if (Boolean.getBoolean(
                            "custommc.acceptance.requireProductionScaffoldKeyboardWalkProof")) {
                        // V60 source-native read-only witness at the actual
                        // original controller tick. A marker may never be
                        // inferred from server teleport commands or expected
                        // placement geometry. Source data missing = no proof.
                        Object player = null;
                        if (minecraft instanceof Minecraft189ScaffoldNativeContextAccess) {
                            final Object[] nativeValues =
                                    ((Minecraft189ScaffoldNativeContextAccess)minecraft)
                                            .customMcScaffoldNativeContext();
                            if (nativeValues != null && nativeValues.length == 3)
                                player = nativeValues[0];
                        }
                        if (!(player instanceof Minecraft189PlayerPositionAccess))
                            throw new IllegalStateException(
                                    "V60 original native local player unavailable at controller");
                        final Minecraft189PlayerPositionAccess nativePlayer =
                                (Minecraft189PlayerPositionAccess)player;
                        final double px = nativePlayer.customMcPositionX();
                        final double py = nativePlayer.customMcPositionY();
                        final double pz = nativePlayer.customMcPositionZ();
                        if (!Double.isFinite(px) || !Double.isFinite(py)
                                || !Double.isFinite(pz))
                            throw new IllegalStateException(
                                    "V60 original controller player position nonfinite");
                        final boolean originalGrounded =
                                player instanceof Minecraft189PlayerMovementStateAccess
                                && ((Minecraft189PlayerMovementStateAccess)player)
                                        .customMcOnGround();
                        final boolean originalSneaking =
                                player instanceof Minecraft189PlayerMovementStateAccess
                                && ((Minecraft189PlayerMovementStateAccess)player)
                                        .customMcSneaking();
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_ORIGINAL_SOURCE_POSITION"
                                + " step=" + scaffold.productionRepeatAttemptedCount()
                                + " x=" + px + " y=" + py + " z=" + pz
                                + " onGround=" + originalGrounded
                                + " sneaking=" + originalSneaking);
                        System.out.flush();
                    }
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.requireProductionScaffoldKeyboardWalkProof")
                        && Minecraft189ScaffoldKeyboardWalkDiagnosticsGate
                                .waitingForOriginalNextController(
                                        receipts,
                                        scaffold.productionRepeatAttemptedCount(),
                                        productionSixBlockProof ? 6
                                        : productionFiveBlockProof ? 5
                                        : productionFourBlockProof ? 4
                                        : productionThreeBlockProof ? 3 : 2)
                        && ++scaffoldKeyboardWalkDiagnosticsTicks % 40 == 0) {
                    // V262: native read-only heartbeat before original
                    // controller #2, #5, and #6; never grant placement.
                    String position = "unavailable";
                    try {
                        if (minecraft instanceof Minecraft189ScaffoldNativeContextAccess) {
                            final Object[] nativeValues =
                                    ((Minecraft189ScaffoldNativeContextAccess)minecraft)
                                            .customMcScaffoldNativeContext();
                            if (nativeValues != null && nativeValues.length == 3
                                    && nativeValues[0] instanceof Minecraft189PlayerPositionAccess) {
                                final Minecraft189PlayerPositionAccess p =
                                        (Minecraft189PlayerPositionAccess)nativeValues[0];
                                position = p.customMcPositionX() + ","
                                        + p.customMcPositionY() + ","
                                        + p.customMcPositionZ();
                            }
                        }
                    } catch (RuntimeException unavailableNativeSource) {
                        position = "read-failed";
                    }
                    final Minecraft189PlayerRotationState.Snapshot nativeRotation =
                            activeRuntime != null && activeRuntime.hostInstalled()
                            ? activeRuntime.requireHostRuntime()
                                    .playerRotationState().snapshot()
                            : null;
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_KEYBOARD_WALK_NATIVE_DIAG"
                            + " positiveReceipts=" + receipts
                            + " originalAttempts="
                            + scaffold.productionRepeatAttemptedCount()
                            + " expectedNextStep=" + (receipts + 1)
                            + " nativePosition=" + position
                            + " originalYaw=" + (nativeRotation != null
                                    && nativeRotation.available()
                                    ? nativeRotation.yaw() : Float.NaN)
                            + " originalPitch=" + (nativeRotation != null
                                    && nativeRotation.available()
                                    ? nativeRotation.pitch() : Float.NaN)
                            + " source=" + Minecraft189ScaffoldOriginalCandidateProbe
                                    .diagnose(minecraft)
                            + " receiptSequence=" + scaffold.repeatProofDiagnostics(minecraft));
                    System.out.flush();
                }
                final int expectedCount = productionSixBlockProof ? 6
                        : productionFiveBlockProof ? 5
                        : productionFourBlockProof ? 4
                        : productionThreeBlockProof ? 3 : 2;
                if (!scaffoldProductionCompletionReported
                        && receipts == expectedCount
                        && scaffold.repeatProofStopped()
                        && scaffold.productionRepeatAttemptedCount() == expectedCount) {
                    scaffoldProductionCompletionReported = true;
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_PRODUCTION_"
                            + (productionSixBlockProof ? "SIX"
                                : productionFiveBlockProof ? "FIVE"
                                : productionFourBlockProof ? "FOUR"
                                : productionThreeBlockProof ? "THREE" : "TWO")
                            + "_CLIENT_RECEIPTS_PASS=YES attempts="
                            + expectedCount);
                    System.out.flush();
                }
            }
            return;
        }
        final boolean moduleArmed = scaffold != null && scaffold.armed();
        final boolean receiptPending = scaffoldOriginalPlacementReadback != null;
        final boolean candidateRequested = moduleArmed;
        final long sourceEpoch = receiptPending || candidateRequested
                ? scaffoldOriginalSourceEpoch.observe(minecraft) : 0L;
        if (activeRuntime != null && !scaffoldDisabledProofReported
                && !Boolean.getBoolean(
                        "custommc.acceptance.requireOriginalScaffoldPlacement")
                && Boolean.getBoolean(
                        "custommc.acceptance.requireOriginalScaffoldDisabledControl")
                && minecraft instanceof Minecraft189ScaffoldNativeContextAccess) {
            final Minecraft189ScaffoldNativeContext.Witness disabledSource =
                    Minecraft189ScaffoldNativeContext.capture(
                            (Minecraft189ScaffoldNativeContextAccess)minecraft);
            if (disabledSource != null) {
                final Minecraft189InventoryHotbarAccess inventory =
                        ((Minecraft189PlayerInventoryAccess)disabledSource.player)
                                .customMcInventory();
                final Minecraft189ScaffoldSelectedHotbar.Evidence selected =
                        inventory instanceof Minecraft189InventoryHotbarItemsAccess
                        ? Minecraft189ScaffoldSelectedHotbar.from(
                                (Minecraft189InventoryHotbarItemsAccess)inventory)
                        : null;
                // Source original player feet/World, not a hardcoded
                // integrated-server coordinate. Dedicated server worlds
                // generate their spawn at different absolute coordinates.
                final int targetX =
                        (int)Math.floor(disabledSource.player.customMcPositionX());
                final int targetY =
                        (int)Math.floor(disabledSource.player.customMcPositionY()) - 1;
                final int targetZ =
                        (int)Math.floor(disabledSource.player.customMcPositionZ());
                if (selected != null && selected.sourceStackSize == 8
                        && disabledSource.world.customMcScaffoldCellCode(
                                targetX,targetY,targetZ)
                                == Minecraft189ScaffoldWorldReadAccess.AIR
                        && disabledSource.world.customMcScaffoldCellCode(
                                targetX-1,targetY,targetZ)
                                == Minecraft189ScaffoldWorldReadAccess.FULL_SOLID) {
                    if (++scaffoldDisabledProofTicks >= 60) {
                        scaffoldDisabledProofReported = true;
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_DISABLED_SURVIVAL_WORLD_AIR_STACK_UNCHANGED_PASS=YES");
                        System.out.flush();
                    }
                } else {
                    scaffoldDisabledProofTicks = 0;
                }
            }
        }
        if (scaffoldOriginalPlacementReadback != null
                && !scaffoldOriginalPlacementReadbackReported) {
            if (moduleActivation == 0L
                    || moduleActivation != scaffoldOriginalPlacementActivation
                    || sourceEpoch == 0L
                    || sourceEpoch != scaffoldOriginalPlacementReadbackEpoch) {
                // A new World/player or disconnected original client cannot
                // complete a receipt from a prior source generation.
                scaffoldOriginalPlacementReadback = null;
                scaffoldOriginalPlacementReadbackEpoch = 0L;
                scaffoldOriginalPlacementActivation = 0L;
                scaffoldOriginalPlacementReadbackTicks = 0;
            } else if (scaffoldOriginalSourceEpoch.confirmsSameSourceReadback(
                    minecraft, scaffoldOriginalPlacementReadbackEpoch,
                    scaffoldOriginalPlacementReadback)) {
                scaffoldOriginalPlacementReadbackReported = true;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CLIENT_SURVIVAL_READBACK_PASS=YES"
                        + " target=" + scaffoldOriginalPlacementReadback.targetX
                        + "," + scaffoldOriginalPlacementReadback.targetY
                        + "," + scaffoldOriginalPlacementReadback.targetZ
                        + " stackBefore="
                        + scaffoldOriginalPlacementReadback.originalStackSize
                        + " stackAfter="
                        + (scaffoldOriginalPlacementReadback.originalStackSize - 1));
                System.out.flush();
                scaffoldOriginalPlacementReadback = null;
                scaffoldOriginalPlacementReadbackEpoch = 0L;
            } else if (++scaffoldOriginalPlacementReadbackTicks >= 120) {
                // A positive client controller return is not enough:
                // missing original target block/decrement is a failure.
                scaffoldOriginalPlacementReadback = null;
                scaffoldOriginalPlacementReadbackEpoch = 0L;
            }
        }
        // V28 deliberately acceptance-only until the vanilla multiplayer
        // server persistence and survival stack negative tests are certified.
        // Source callback runs on original Minecraft.runTick and never
        // creates C08 itself or rewrites the original cursor.
        if (moduleArmed && scaffoldOriginalPlacementReadback == null
                && sourceEpoch > 0L) {
            final Minecraft189ScaffoldNativePlacementAttempt.Result attempted =
                    scaffold.attemptOnce(minecraft);
            if (attempted != null) {
                if (Boolean.getBoolean(
                        "custommc.acceptance.requireOriginalScaffoldPlacement")) {
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_MODULE_OWNER_PASS=YES"
                            + " activation=" + moduleActivation);
                    System.out.flush();
                }
                if (attempted.originalControllerAccepted
                        && scaffold.activation() == moduleActivation
                        && scaffoldOriginalSourceEpoch.observe(minecraft) == sourceEpoch) {
                    scaffoldOriginalPlacementReadback = attempted;
                    scaffoldOriginalPlacementReadbackEpoch = sourceEpoch;
                    scaffoldOriginalPlacementActivation = moduleActivation;
                    scaffoldOriginalPlacementReadbackTicks = 0;
                    scaffoldOriginalPlacementReadbackReported = false;
                }
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_ATTEMPT=YES"
                        + " accepted=" + attempted.originalControllerAccepted
                        + " target=" + attempted.targetX + ","
                        + attempted.targetY + "," + attempted.targetZ
                        + " support=" + attempted.supportX + ","
                        + attempted.supportY + "," + attempted.supportZ
                        + " selectedBefore=" + attempted.originalStackSize);
                System.out.flush();
            }
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.requireOriginalScaffoldPlacement")
                && (++scaffoldOriginalPlacementNativeDiagTicks == 1
                    || scaffoldOriginalPlacementNativeDiagTicks % 40 == 0)) {
            // V294: source-owned frame + exact branch refusals from the
            // already-executed real attempt. No independent World/cursor/
            // controller access or additional attempt to place.
            final Minecraft189OriginalScaffoldPlacementAttemptTrace.Snapshot v294 =
                    Minecraft189ScaffoldNativePlacementAttempt.originalV29Trace();
            System.out.println(
                    "CUSTOMMC_189_ORIGINAL_SCAFFOLD_V29_REFUSAL_DIAGNOSTIC"
                    + " callbacks=" + scaffoldOriginalPlacementNativeDiagTicks
                    + " enableIssued=" + scaffoldAcceptanceEnableIssued
                    + " moduleActivation=" + moduleActivation
                    + " moduleArmed=" + moduleArmed
                    + " originalEpoch=" + sourceEpoch
                    + " receiptPending=" + (scaffoldOriginalPlacementReadback != null)
                    + " attemptedCalls=" + v294.originalCalls
                    + " refused=" + v294.refused
                    + " completedControllerCalls=" + v294.completedControllerCalls
                    + " controllerAccepted=" + v294.controllerAccepted
                    + " lastAttemptDecision=" + v294.lastReason
                    + " refusalCounts=" + v294.reasons);
            System.out.flush();
        }
        if (activeRuntime != null && !scaffoldOriginalCandidateReported
                && Boolean.getBoolean(
                        "custommc.acceptance.requireOriginalScaffoldCandidate")) {
            final Minecraft189ScaffoldOriginalCandidateProbe.Observation candidate =
                    Minecraft189ScaffoldOriginalCandidateProbe.observe(minecraft);
            if (candidate == null) {
                // Acceptance-only bounded original Minecraft diagnostics.
                // Never changes cursor, player, original World or module state.
                if (++scaffoldOriginalCandidateDiagTicks % 60 == 0) {
                    System.out.println("CUSTOMMC_V26_SOURCE_REJECTION="
                            + Minecraft189ScaffoldOriginalCandidateProbe.diagnose(minecraft));
                    System.out.flush();
                }
            }
            if (candidate != null) {
                scaffoldOriginalCandidateReported = true;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_NATIVE_CANDIDATE_PASS=YES"
                        + " target=" + candidate.targetX + ","
                        + candidate.targetY + "," + candidate.targetZ
                        + " support=" + candidate.supportX + ","
                        + candidate.supportY + "," + candidate.supportZ
                        + " face=" + candidate.face
                        + " sourceEyeY=" + candidate.nativeEyeY
                        + " conservativeReach=" + candidate.conservativeReach);
                System.out.flush();
            }
        }
        if (scaffoldOriginalContextReported || activeRuntime == null
                || !Boolean.getBoolean(
                        "custommc.acceptance.requireOriginalScaffoldContext"))
            return;
        if (!(minecraft instanceof Minecraft189ScaffoldNativeContextAccess))
            return;
        final Minecraft189ScaffoldNativeContext.Witness witness =
                Minecraft189ScaffoldNativeContext.capture(
                        (Minecraft189ScaffoldNativeContextAccess)minecraft);
        if (witness == null) return;
        // V24: if explicitly requested, the same original player must also
        // provide its source-native dynamic wn/aS()F eye height. No constant.
        final boolean requireNativeEye = Boolean.getBoolean(
                "custommc.acceptance.requireOriginalScaffoldEye");
        final double sourceEyeY = requireNativeEye
                ? Minecraft189ScaffoldSourceEye.currentEyeY(witness.player)
                : Double.NaN;
        if (requireNativeEye && !Double.isFinite(sourceEyeY)) return;
        final boolean requireNativeReach = Boolean.getBoolean(
                "custommc.acceptance.requireOriginalScaffoldReach");
        final double conservativeReach = requireNativeReach
                ? Minecraft189ScaffoldSourceReach.conservativeReach(minecraft)
                : Double.NaN;
        if (requireNativeReach && !Double.isFinite(conservativeReach)) return;
        // A valid reference triple and selected genuine ItemBlock do not
        // establish a clicked BLOCK or a legal Scaffold candidate. The
        // original current ray may be MISS/ENTITY. No placement occurs.
        scaffoldOriginalContextReported = true;
        if (requireNativeEye) {
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_PLAYER_EYE_PASS=YES"
                    + " eyeY=" + sourceEyeY);
        }
        if (requireNativeReach) {
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_REACH_PASS=YES"
                    + " conservativeReach=" + conservativeReach);
        }
        System.out.println(
                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_TICK_CONTEXT_PASS=YES");
        System.out.flush();
    }

    public static synchronized void gameTick() {
        if (activeRuntime != null) {
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireNativeScaffoldTickParity")) {
                scaffoldTickParity.beginTick();
            }
            Minecraft189VanillaMeleeAcceptance.tick();
            activeRuntime.publishGameTick();
            if (Boolean.getBoolean("custommc.acceptance.reportRemotePlayer")
                    && !acceptanceRemotePlayerKindReported
                    && activeRuntime.hostInstalled()) {
                if (acceptanceRemotePlayerKindFresh) {
                    acceptanceRemotePlayerKindTicks++;
                    if (acceptanceRemotePlayerKindTicks >= 20) {
                        acceptanceRemotePlayerKindReported = true;
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_REMOTE_PLAYER_KIND_20_TICKS_PASS=YES");
                        System.out.flush();
                    }
                } else {
                    acceptanceRemotePlayerKindTicks = 0;
                }
                acceptanceRemotePlayerKindFresh = false;
            }
            // This checkpoint requires repeated genuine game ticks with both
            // loaded player and world combat evidence; menu frames alone
            // cannot reach it. Normal launches never enable this property.
            if (Boolean.getBoolean("custommc.acceptance.reportLiveWorld")
                    && activeRuntime.hostInstalled()) {
                reportAcceptanceScreen();
            }
            // V345: gameTick-only forensic for source-original GUI changes
            // during genuine combat acceptance. ConnectScreen 'awz' may
            // remain OPEN after World/UUID data arrives; only native source
            // transitions can establish whether it clears or reopens.
            // Pure read-only diagnostics: never modify input, GUI/counter,
            // controller dispatch, UUID or damage acceptance criteria.
            if (!Boolean.getBoolean("custommc.acceptance.reportLiveWorld")
                    && activeRuntime.hostInstalled()
                    && (Boolean.getBoolean("custommc.acceptance.reportAutoClicker")
                        || Boolean.getBoolean(
                                "custommc.acceptance.requireAutoClickerExactRemoteUuid")
                        || Boolean.getBoolean(
                                "custommc.acceptance.reportKillAuraRejoin")
                        || Boolean.getBoolean(
                                "custommc.acceptance.reportTriggerBotRealPvp"))) {
                reportAcceptanceScreen();
            }
            if (Boolean.getBoolean("custommc.acceptance.reportLiveWorld")
                    && !acceptanceWorldReported
                    && activeRuntime.hostInstalled()) {
                if (acceptancePlayerObserved
                        && acceptanceWorldCombatObserved) {
                    acceptanceWorldTicks++;
                    if (acceptanceWorldTicks >= 20) {
                        acceptanceWorldReported = true;
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_LIVE_WORLD_TICKS_PASS=YES");
                        System.out.flush();
                    }
                } else {
                    acceptanceWorldTicks = 0;
                }
                acceptancePlayerObserved = false;
                acceptanceWorldCombatObserved = false;
            }
        }
    }

    /**
     * Test-only GUI state tracing for diagnosing real X11 menu navigation.
     * The target class is resolved from the game's owned context loader;
     * there are no hardcoded parent-owned vanilla class identities.
     */
    private static void reportAcceptanceScreen() {
        try {
            final ClassLoader loader = Thread.currentThread()
                    .getContextClassLoader();
            final Class<?> minecraft = Class.forName("ave", false, loader);
            final Method singleton = minecraft.getDeclaredMethod("A");
            singleton.setAccessible(true);
            final Object instance = singleton.invoke(null);
            if (instance == null) {
                return;
            }
            for (Field field : minecraft.getDeclaredFields()) {
                if (!"axu".equals(field.getType().getName())) {
                    continue;
                }
                field.setAccessible(true);
                final Object screen = field.get(instance);
                final String screenType = screen == null
                        ? "(in-game/no GUI)"
                        : screen.getClass().getName();
                if (!screenType.equals(acceptanceLastScreen)) {
                    acceptanceLastScreen = screenType;
                    System.out.println("CUSTOMMC_189_ACCEPTANCE_SCREEN=" + screenType);
                    System.out.flush();
                }
                return;
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Optional diagnostics must not break normal Minecraft ticks.
        }
    }

    // CI-only, bounded samples of the actual transformed local player.
    // Strictly observational: never touches vanilla collision or controls.
    private static int acceptanceSafeWalkPositionTicks;

    /**
     * Test-only original Mojang FOV geometry. The real two-player source
     * fixture uses identical X/Y and two blocks of Z. Server /tp ACK does
     * not mean that the client's World has consumed that teleport.
     * This guard never affects production FOV, Aura or vanilla clicks.
     */
    static boolean originalFovNegativeFixtureSourceAligned(
            final double dx, final double dy, final double dz) {
        return Double.isFinite(dx) && Double.isFinite(dy)
                && Double.isFinite(dz)
                && Math.abs(dx) <= 0.20D
                && Math.abs(dy) <= 0.30D
                && Math.abs(Math.abs(dz) - 2.0D) <= 0.25D;
    }

    public static synchronized void playerPosition(
            final Minecraft189PlayerPositionAccess player) {
        if (Boolean.getBoolean("custommc.acceptance.reportLiveWorld")) {
            acceptancePlayerObserved = player != null
                    && Double.isFinite(player.customMcPositionX())
                    && Double.isFinite(player.customMcPositionY())
                    && Double.isFinite(player.customMcPositionZ());
            if (player == null) {
                acceptanceWorldTicks = 0;
            }
        }
        if (Boolean.getBoolean("custommc.acceptance.reportNativeSafeWalkPosition")
                && player instanceof Minecraft189LocalPlayerSafeWalkMarker
                && acceptanceSafeWalkPositionTicks++ < 900
                && acceptanceSafeWalkPositionTicks % 3 == 0
                && Double.isFinite(player.customMcPositionX())
                && Double.isFinite(player.customMcPositionY())
                && Double.isFinite(player.customMcPositionZ())) {
            System.out.println("CUSTOMMC_189_NATIVE_LOCAL_POS=x="
                    + player.customMcPositionX() + " y="
                    + player.customMcPositionY() + " z="
                    + player.customMcPositionZ());
            System.out.flush();
        }
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerPosition(player);
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.requireNativeScaffoldTickParity")) {
            scaffoldTickParity.observePlayer(player);
            reportScaffoldTickParity();
        }
    }

    public static synchronized void playerRotation(
            final Minecraft189PlayerRotationAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerRotation(player);
        }
    }

    public static synchronized void playerDimension(
            final Minecraft189PlayerDimensionAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerDimension(player);
        }
    }

    public static synchronized void playerMovementState(
            final Minecraft189PlayerMovementStateAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerMovementState(player);
        }
    }

    public static synchronized void playerSprintControl(
            final Minecraft189PlayerSprintControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerSprintControl(player);
        }
    }

    public static synchronized void playerSneakControl(
            final Minecraft189PlayerSneakControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerSneakControl(player);
        }
    }

    public static synchronized float adjustNoSlowMovement(
            final float slowedValue) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host == null
                ? slowedValue
                : host.adjustNoSlowMovement(
                        slowedValue);
    }

    /**
     * V339 native EntityLivingBase.knockBack owner fence: the transformed
     * method is inherited by *all* living entities, but the private user's
     * Velocity feature must never scale other players' or mobs' motion.
     * Exact obfuscated original EntityPlayerSP (bew) is the only admitted
     * local class in Minecraft 1.8.9. Do not infer from names, team, target
     * UUID, or the attacker's identity. No packet or motion write occurs
     * in this guard; the caller returns the actual vanilla result.
     */
    static boolean velocityOriginalLocalOwner(final Object owner) {
        return owner != null
                && Minecraft189Mappings.ENTITY_PLAYER_SP
                        .obfuscatedInternalName()
                        .equals(owner.getClass().getName());
    }

    public static synchronized double adjustVelocityHorizontalOriginalOwner(
            final Object originalKnockbackOwner,
            final double before,
            final double after) {
        return !velocityOriginalLocalOwner(originalKnockbackOwner)
                ? after : adjustVelocityHorizontal(before, after);
    }

    public static synchronized double adjustVelocityVerticalOriginalOwner(
            final Object originalKnockbackOwner,
            final double before,
            final double after) {
        return !velocityOriginalLocalOwner(originalKnockbackOwner)
                ? after : adjustVelocityVertical(before, after);
    }

    public static synchronized double adjustVelocityHorizontal(
            final double before,
            final double after) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host == null
                ? after
                : host.adjustVelocityHorizontal(
                        before,
                        after);
    }

    public static synchronized double adjustVelocityVertical(
            final double before,
            final double after) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host == null
                ? after
                : host.adjustVelocityVertical(
                        before,
                        after);
    }

    public static synchronized void playerJumpControl(
            final Minecraft189PlayerJumpControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerJumpControl(player);
        }
    }

    public static synchronized void playerStepControl(
            final Minecraft189PlayerStepControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerStepControl(player);
            // Opt-in official Mojang 1.8.9 client evidence only. The
            // readback is the actual transformed Entity stepHeight field,
            // after the production source-native adapter callback.
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireNativeStepGuiFocus")
                    && player != null) {
                Minecraft189OriginalClickGuiStepAcceptance.observe(host, player);
            }
        }
    }

    public static synchronized void playerFallDistanceControl(
            final Minecraft189PlayerFallDistanceControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerFallDistanceControl(player);
        }
    }

    public static synchronized void playerWebControl(
            final Minecraft189PlayerWebControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerWebControl(player);
        }
    }

    public static synchronized void playerNoClipControl(
            final Minecraft189PlayerNoClipControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerNoClipControl(player);
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireNativeNoClipGuiFocus")
                    && player != null) {
                Minecraft189OriginalClickGuiNoClipAcceptance.observe(host, player);
            }
        }
    }

    public static synchronized void playerMotionControl(
            final Minecraft189PlayerMotionControl player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerMotionControl(player);
        }
    }

    public static synchronized void playerHealth(
            final Minecraft189PlayerHealthAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHealth(player);
        }
    }

    public static synchronized void playerHurtTime(
            final Minecraft189PlayerHurtTimeAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHurtTime(player);
        }
    }

    public static synchronized void playerArmor(
            final Minecraft189PlayerArmorAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerArmor(player);
        }
    }

    public static synchronized void playerHunger(
            final Minecraft189PlayerHungerAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHunger(player);
        }
    }

    public static synchronized void playerPotionEffects(
            final Minecraft189PlayerPotionEffectsAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerPotionEffects(player);
        }
    }

    public static synchronized void playerExperience(
            final Minecraft189PlayerExperienceAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerExperience(player);
        }
    }

    public static synchronized void playerPing(
            final Minecraft189PlayerPingAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerPing(player);
        }
    }

    public static synchronized void playerHotbarSlot(
            final Minecraft189PlayerInventoryAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHotbarSlot(player);
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireNativeCombatSlotGuiFocus")) {
                Minecraft189OriginalCombatSlotAcceptance.observe(host, player);
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireNativeRodPotionDiscovery")) {
                Minecraft189OriginalCombatItemDiscoveryAcceptance.observe(host, player);
            }
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.requireNativeScaffoldTickParity")) {
            scaffoldTickParity.observeInventory(player);
            reportScaffoldTickParity();
        }
    }

    public static synchronized void worldTime(
            final Minecraft189WorldTimeAccess world) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.worldTime(world);
        }
    }

    public static synchronized void worldWeather(
            final Minecraft189WorldWeatherAccess world) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.worldWeather(world);
        }
    }

    // V16 acceptance only: observe one exact original World cell as the
    // integrated vanilla server changes it air -> stone -> air. This never
    // changes a block, touches packets or enables in regular gameplay.
    private static int acceptanceScaffoldWorldCellPhase;
    private static int acceptanceScaffoldWorldProbeCount;
    private static int acceptanceScaffoldWorldLastCode = -999;
    private static int acceptanceScaffoldWorldDiagTick;
    private static boolean acceptanceScaffoldFarChunkVetoReported;

    // V19 original-game-only authoritative raytrace proof. No clicked block.
    private static boolean scaffoldNativeRayTraceReported;
    private static boolean scaffoldNativeRayTraceNegativeReported;
    private static final Minecraft189OriginalScaffoldRayTraceProvenance
            originalScaffoldRayTraceEvidence =
            new Minecraft189OriginalScaffoldRayTraceProvenance();

    public static synchronized void worldEntityPositions(
            final Minecraft189WorldEntityPositionsAccess world) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.worldEntityPositions(world);
        }
        final boolean observeOriginalScaffoldRay =
                !scaffoldNativeRayTraceReported
                && Boolean.getBoolean(
                    "custommc.acceptance.requireNativeScaffoldRayTrace");
        final boolean originalCellSource =
                world instanceof Minecraft189ScaffoldWorldReadAccess;
        final boolean originalRaySource =
                world instanceof Minecraft189ScaffoldWorldRayTraceAccess;
        if (observeOriginalScaffoldRay)
            originalScaffoldRayTraceEvidence.observeInterfaces(
                    originalCellSource, originalRaySource);
        if (observeOriginalScaffoldRay
                && originalCellSource && originalRaySource) {
            final Minecraft189ScaffoldWorldReadAccess cells =
                    (Minecraft189ScaffoldWorldReadAccess)world;
            final Minecraft189ScaffoldWorldRayTraceAccess trace =
                    (Minecraft189ScaffoldWorldRayTraceAccess)world;
            try {
                // Genuinely loaded original vanilla stone support is first
                // required, then use source World.rayTraceBlocks from above
                // through its UP face. The ray endpoint is 0.5 inside stone
                // to avoid an endpoint exactly at an ambiguous voxel border.
                final int originalCell =
                        cells.customMcScaffoldCellCode(0, 99, 0);
                originalScaffoldRayTraceEvidence.observeCell(originalCell,
                        Minecraft189ScaffoldWorldReadAccess.FULL_SOLID);
                if (originalCell
                        == Minecraft189ScaffoldWorldReadAccess.FULL_SOLID) {
                    final boolean correct=trace.customMcScaffoldRayTraceSupport(
                            0.5D,102.0D,0.5D,0.5D,99.5D,0.5D,
                            0,99,0,1);
                    final boolean wrongFace=trace.customMcScaffoldRayTraceSupport(
                            0.5D,102.0D,0.5D,0.5D,99.5D,0.5D,
                            0,99,0,2);
                    final boolean wrongBlock=trace.customMcScaffoldRayTraceSupport(
                            0.5D,102.0D,0.5D,0.5D,99.5D,0.5D,
                            1,99,0,1);
                    // Observe results of those SAME three original calls.
                    // No additional ray or World lookup, ever.
                    originalScaffoldRayTraceEvidence.observeRayResults(
                            correct, wrongFace, wrongBlock);
                    if (wrongFace || wrongBlock)
                        throw new IllegalStateException(
                                "original Scaffold raytrace accepted wrong face/BlockPos");
                    if (!scaffoldNativeRayTraceNegativeReported) {
                        scaffoldNativeRayTraceNegativeReported=true;
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_RAYTRACE_WRONG_FACE_BLOCK_VETO_PASS=YES");
                    }
                    if (correct) {
                        scaffoldNativeRayTraceReported=true;
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_SCAFFOLD_NATIVE_WORLD_UP_FACE_RAYTRACE_PASS=YES");
                    }
                    System.out.flush();
                }
            } catch (RuntimeException sourceRayUnavailable) {
                throw new IllegalStateException(
                        "original Scaffold World raytrace source failure",
                        sourceRayUnavailable);
            }
        }
        if (observeOriginalScaffoldRay) {
            final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot
                    originalRayEvidence =
                    originalScaffoldRayTraceEvidence.snapshot();
            final Minecraft189OriginalScaffoldNativeRayReturnTrace.Snapshot
                    originalReturn =
                    Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot();
            if (originalRayEvidence.sourceCallbacks == 1
                    || originalRayEvidence.sourceCallbacks % 100 == 0
                    || scaffoldNativeRayTraceReported) {
                System.out.println(
                        "CUSTOMMC_189_ORIGINAL_SCAFFOLD_RAY_SOURCE_DIAGNOSTIC"
                        + " sourceCallbacks=" + originalRayEvidence.sourceCallbacks
                        + " missingInterfaces=" + originalRayEvidence.missingInterfaces
                        + " checkedCells=" + originalRayEvidence.worldCellChecks
                        + " fullSolidCells=" + originalRayEvidence.fullSolidCells
                        + " otherCells=" + originalRayEvidence.unavailableOrOtherCells
                        + " rayBatches=" + originalRayEvidence.correctFaceRayCalls
                        + " correctFaceHits=" + originalRayEvidence.correctFaceHits
                        + " originalExpectedRayReturnCalls=" + originalReturn.expectedUpCalls
                        + " originalRayNullHit=" + originalReturn.miss
                        + " originalRayNonBlock=" + originalReturn.wrongType
                        + " originalRayNullFace=" + originalReturn.missingFace
                        + " originalRayWrongFace=" + originalReturn.wrongFace
                        + " originalRayWrongBlockPos=" + originalReturn.wrongBlock
                        + " originalRayExactMatch=" + originalReturn.exactMatch
                        + " lastNativeReturnReason=" + originalReturn.lastCode
                        + " wrongFaceHits=" + originalRayEvidence.wrongFaceHits
                        + " wrongBlockHits=" + originalRayEvidence.wrongBlockHits
                        + " lastCellCode=" + originalRayEvidence.lastCellCode
                        + " lastDecision=" + originalRayEvidence.lastDecision
                        + " originalUpFaceReported=" + scaffoldNativeRayTraceReported);
                System.out.flush();
            }
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.requireNativeScaffoldTickParity")) {
            scaffoldTickParity.observeWorld(world);
            reportScaffoldTickParity();
        }
        if (Boolean.getBoolean("custommc.acceptance.requireNativeScaffoldWorldCell")
                && acceptanceScaffoldWorldProbeCount == 0) {
            System.out.println("CUSTOMMC_SCAFFOLD_WORLD_DIAG_CALLBACK=YES host="
                    + (host != null) + " transformed="
                    + (world instanceof Minecraft189ScaffoldWorldReadAccess));
            System.out.flush();
            acceptanceScaffoldWorldProbeCount++;
        }
        if (host != null
                && acceptanceScaffoldWorldCellPhase < 3
                && Boolean.getBoolean("custommc.acceptance.requireNativeScaffoldWorldCell")
                && world instanceof Minecraft189ScaffoldWorldReadAccess) {
            final Minecraft189ScaffoldWorldReadAccess source =
                    (Minecraft189ScaffoldWorldReadAccess)world;
            final int cell;
            try {
                // Adjacent cell (1,99,0) beside source-server stone
                // support at (0,99,0). This keeps the actual player's
                // chunk loaded and their feet stably on ground during
                // independent vanilla air -> stone -> air transitions.
                cell = source.customMcScaffoldCellCode(1, 99, 0);
            } catch (RuntimeException nativeWorldUnavailable) {
                return;
            }
            // Native original-world read diagnostics are bounded and
            // acceptance-only. Compare a real server-set floor, the target
            // neighbor and air above player, without changing the test.
            if (acceptanceScaffoldWorldCellPhase == 0
                    && ++acceptanceScaffoldWorldDiagTick % 100 == 0
                    && acceptanceScaffoldWorldDiagTick <= 3000) {
                try {
                    final int floor = source.customMcScaffoldCellCode(0, 99, 0);
                    final int above = source.customMcScaffoldCellCode(0, 101, 0);
                    final int targetBranch =
                            source.customMcScaffoldCellTrace(1, 99, 0);
                    final int floorBranch =
                            source.customMcScaffoldCellTrace(0, 99, 0);
                    final int aboveBranch =
                            source.customMcScaffoldCellTrace(0, 101, 0);
                    System.out.println("CUSTOMMC_SCAFFOLD_WORLD_DIAG_TRIPLE=target:"
                            + cell + ",floor:" + floor + ",above:" + above
                            + ",branchTarget:" + targetBranch
                            + ",branchFloor:" + floorBranch
                            + ",branchAbove:" + aboveBranch
                            + ",tick:" + acceptanceScaffoldWorldDiagTick
                            + ",type:" + world.getClass().getName());
                    System.out.flush();
                } catch (RuntimeException diagnosticUnavailable) {
                    System.out.println("CUSTOMMC_SCAFFOLD_WORLD_DIAG_ERROR="
                            + diagnosticUnavailable.getClass().getName());
                    System.out.flush();
                }
            }
            if (cell != acceptanceScaffoldWorldLastCode
                    && acceptanceScaffoldWorldProbeCount < 12) {
                acceptanceScaffoldWorldLastCode = cell;
                acceptanceScaffoldWorldProbeCount++;
                System.out.println("CUSTOMMC_SCAFFOLD_WORLD_DIAG_NATIVE_CELL=" + cell
                        + " phase=" + acceptanceScaffoldWorldCellPhase);
                System.out.flush();
            }
            if (acceptanceScaffoldWorldCellPhase == 0
                    && cell == Minecraft189ScaffoldWorldReadAccess.AIR) {
                // A separate genuine unloaded distant chunk MUST remain
                // UNKNOWN even when vanilla allowEmpty is true. Never
                // create a fake permissive air result from unloaded space.
                final int far = source.customMcScaffoldCellCode(
                        1048576, 99, 1048576);
                if (far != Minecraft189ScaffoldWorldReadAccess.UNKNOWN)
                    throw new IllegalStateException(
                            "Scaffold source World leaked unloaded far chunk code="
                                    + far);
                if (!acceptanceScaffoldFarChunkVetoReported) {
                    acceptanceScaffoldFarChunkVetoReported = true;
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_UNLOADED_CHUNK_VETO_PASS=YES");
                    System.out.flush();
                }
                acceptanceScaffoldWorldCellPhase = 1;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_AIR_PASS=YES");
            } else if (acceptanceScaffoldWorldCellPhase == 1
                    && cell == Minecraft189ScaffoldWorldReadAccess.FULL_SOLID) {
                acceptanceScaffoldWorldCellPhase = 2;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_SOLID_PASS=YES");
            } else if (acceptanceScaffoldWorldCellPhase == 2
                    && cell == Minecraft189ScaffoldWorldReadAccess.AIR) {
                acceptanceScaffoldWorldCellPhase = 3;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_REVERTED_AIR_PASS=YES");
            }
            if (acceptanceScaffoldWorldCellPhase == 3
                    || acceptanceScaffoldWorldCellPhase == 1
                    || acceptanceScaffoldWorldCellPhase == 2)
                System.out.flush();
        }
    }

    public static synchronized void worldEntityKinds(
            final Minecraft189WorldEntityKindsAccess world) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.worldEntityKinds(world);
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportRemotePlayer")
                    && !acceptanceRemotePlayerKindReported) {
                acceptanceRemotePlayerKindFresh = false;
                if (world != null) {
                    final int[] kinds = world.customMcLoadedEntityKinds();
                    if (kinds != null) {
                        for (int bits : kinds) {
                            if ((bits & (Minecraft189WorldEntityKindState.LIVING
                                    | Minecraft189WorldEntityKindState.PLAYER))
                                    == (Minecraft189WorldEntityKindState.LIVING
                                    | Minecraft189WorldEntityKindState.PLAYER)
                                    && (bits & Minecraft189WorldEntityKindState.LOCAL_PLAYER)
                                    == 0) {
                                acceptanceRemotePlayerKindFresh = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static synchronized void worldEntityCombat(
            final Minecraft189WorldEntityCombatAccess world) {
        Minecraft189VanillaMeleeAcceptance.onWorldCombat(world);
        if (Boolean.getBoolean("custommc.acceptance.reportLiveWorld")) {
            acceptanceWorldCombatObserved = world != null;
            if (world == null) {
                acceptanceWorldTicks = 0;
            }
        }
        final Minecraft189HostRuntime host = activeHost();
        if (host != null) {
            host.worldEntityCombat(world);
        }
    }

    public static synchronized void serverAddress(
            final Minecraft189ServerDataAccess serverData) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.serverAddress(serverData);
        }
    }

    public static synchronized void playerHeldItem(
            final Minecraft189PlayerHeldItemAccess player) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerHeldItem(player);
        }
    }

    public static synchronized int rightClickDelay(
            final int currentDelay) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host == null
                ? currentDelay
                : host.rightClickDelay(
                        currentDelay);
    }

    public static synchronized void playerControllerBreakControl(
            final Minecraft189BlockHitDelayControl controller) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerControllerBreakControl(
                    controller);
        }
    }

    public static synchronized void playerControllerMiningControl(
            final Minecraft189BlockMiningControl controller) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerControllerMiningControl(
                    controller);
        }
    }

    public static synchronized void timerSpeedControl(
            final Minecraft189TimerSpeedControl timer) {
        final Minecraft189HostRuntime host = activeHost();
        if (host == null || timer == null) return;
        // Production always calls exactly the existing source-mapped game
        // Timer accessor; acceptance only OBSERVES its result and changes
        // modules through their real normal ModuleController lifecycle.
        host.timerSpeedControl(timer);
        if (!Boolean.getBoolean(
                "custommc.acceptance.requireRealTimerMultiplier")
                || nativeTimerOriginalFinished
                || !host.playerMovementState().snapshot().available()) return;

        final float original = timer.customMcTimerSpeed();
        if (!Float.isFinite(original))
            throw new IllegalStateException("native Mojang Timer value nonfinite");
        if (!nativeTimerOriginalEnabled) {
            if (host.featureCatalog().timerSpeed().active()
                    || Math.abs(original - 1.0F) > .00001F)
                throw new IllegalStateException(
                        "negative disabled native Timer must remain vanilla 1x");
            if (++nativeTimerOriginalBaselineCallbacks == 20) {
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_TIMER_DISABLED_NATIVE_ONE_X_PASS=20");
                host.featureCatalog().enableTimerForOriginalAcceptance();
                nativeTimerOriginalEnabled = true;
                System.out.println(
                        "CUSTOMMC_OFFICIAL_189_TIMER_NORMAL_MODULE_ENABLE_PASS=YES");
                System.out.flush();
            }
            return;
        }
        if (!host.featureCatalog().timerSpeed().active()
                || Math.abs(original - 1.5F) > .00001F)
            throw new IllegalStateException(
                    "actual Mojang Timer did not retain the owned 1.5x multiplier");
        if (++nativeTimerOriginalBoostCallbacks >= 5) {
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_TIMER_NATIVE_FIVE_1_5_X_CALLBACKS_PASS=YES");
            host.featureCatalog().disableTimerForOriginalAcceptance();
            // Only original source-native callback may restore vanilla.
            host.timerSpeedControl(timer);
            if (Math.abs(timer.customMcTimerSpeed() - 1.0F) > .00001F
                    || host.featureCatalog().timerSpeed().active())
                throw new IllegalStateException(
                        "actual Mojang Timer did not restore vanilla after disable");
            nativeTimerOriginalFinished = true;
            System.out.println(
                    "CUSTOMMC_OFFICIAL_189_TIMER_ORIGINAL_NATIVE_RESTORE_ONE_X_PASS=YES");
            System.out.flush();
        }
    }

    public static synchronized int leftClickCounter(
            final int currentCounter) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host == null
                ? currentCounter
                : host.leftClickCounter(
                        currentCounter);
    }

    /** Called solely at exact mapped EntityRenderer.getMouseOver bytecode sites. */
    public static synchronized float raycastBlockDistance(final float vanilla) {
        final Minecraft189HostRuntime host = activeHost();
        if (host == null) return vanilla;
        final float result = host.raycastBlockDistance(vanilla);
        if (Boolean.getBoolean(
                "custommc.acceptance.requireNativeReachGuiFocus")) {
            Minecraft189OriginalClickGuiReachAcceptance.observe(
                    host, vanilla, result);
        }
        return result;
    }

    public static synchronized boolean raycastExtendedBranch(final boolean vanilla) {
        final Minecraft189HostRuntime host = activeHost();
        return host == null ? vanilla : host.raycastExtendedBranch(vanilla);
    }

    public static synchronized double raycastExtendedDistance() {
        final Minecraft189HostRuntime host = activeHost();
        return host == null ? 6.0D : host.raycastExtendedDistance(6.0D);
    }

    /**
     * Player-only local ray-hitbox inflation. Receives the native border
     * unchanged whenever Hitbox is disabled or the candidate is not a player.
     */
    public static synchronized float raycastHitboxBorder(
            final boolean playerCandidate, final float nativeBorder) {
        final Minecraft189HostRuntime host = activeHost();
        return host == null ? nativeBorder
                : host.raycastHitboxBorder(playerCandidate, nativeBorder);
    }

    /**
     * V338 final synthetic HitSelect verdict, immediately before original
     * clickMouse or PlayerControllerMP.attackEntity. Match the *earlier*
     * native ray UUID to the current indexed World identity and recompute
     * its currently published hurt window. Re-sample the actual vanilla ray
     * after hotbar/rotation/native getters: an index or target switch cannot
     * spend an older hit's permission. This never queues a rejected swing.
     *
     * Disabled HitSelect keeps the previously certified owner behavior.
     * Physical/manual vanilla mouse input is not intercepted here.
     */
    private static boolean permitsHitSelectAtNativeDispatch(
            final Minecraft189HostRuntime host,
            final Minecraft189ClickMouseControl minecraft,
            final int originalIndex,
            final UUID originalUuid) {
        final Minecraft189HitSelectModule hitSelect =
                host.featureCatalog().hitSelect();
        if (!hitSelect.active()) return true;
        if (originalUuid == null || originalIndex < 0
                || !(minecraft instanceof Minecraft189CrosshairHitAccess))
            return false;
        final Minecraft189CrosshairHitAccess originalRay =
                (Minecraft189CrosshairHitAccess) minecraft;
        if (!originalRay.customMcCrosshairPlayerHit()
                || originalRay.customMcCrosshairPlayerIndex() != originalIndex)
            return false;
        return hitSelect.permitsOriginalOwner(
                originalIndex, originalUuid,
                host.worldEntityUuidState().snapshot(),
                host.worldEntityKindState().snapshot(),
                host.worldEntityCombatState().snapshot());
    }

    /**
     * Recheck the exact original vanilla GuiScreen directly before a native
     * automated action. Source getter reentry between initial admission and
     * final controller dispatch may open a menu. Rejecting an attempted
     * action revokes scheduler credit rather than replaying it later.
     *
     * This is NOT an original PvP controller/UUID or server-damage witness.
     */
    private static boolean vetoOriginalScreenBeforeNativeCombat(
            final Minecraft189HostRuntime host,
            final Minecraft189ClickMouseControl minecraft) {
        if (Minecraft189NativeGuiCombatVeto.blocks(minecraft)) {
            host.suspendSyntheticCombatForOriginalGui();
            return true;
        }
        return false;
    }

    public static synchronized void autoClick(
            final Minecraft189ClickMouseControl minecraft) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null && minecraft != null) {
            // V341: the top-level original-game synthetic action callback
            // owns at most one primary lane. JNI/vanilla getter reentry is
            // not allowed to borrow that lane or double-spend click credit.
            final long combatCycleToken = host.enterSyntheticCombatCycle();
            if (combatCycleToken == 0L) return;
            try {
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportTriggerBotRealPvp")
                    && !acceptanceTriggerEnabled) {
                // No module-controlled click until twenty consecutive
                // ORIGINAL player-only crosshair readbacks are observed.
                // Clean original native hurt-time + live UUID must agree;
                // an arbitrary nearby player or reused list index is never
                // sufficient to arm this acceptance.
                final Minecraft189WorldEntityUuidState.Snapshot ids =
                        host.worldEntityUuidState().snapshot();
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        host.worldEntityKindState().snapshot();
                final boolean hit = minecraft instanceof Minecraft189CrosshairHitAccess
                        && ((Minecraft189CrosshairHitAccess) minecraft)
                                .customMcCrosshairPlayerHit();
                final int index = hit
                        ? ((Minecraft189CrosshairHitAccess) minecraft)
                                .customMcCrosshairPlayerIndex() : -1;
                boolean valid = hit && index >= 0 && ids.available()
                        && kinds.available() && index < ids.entityCount()
                        && ids.entityCount() == kinds.entityCount()
                        && ids.at(index) != null
                        && kinds.player(index) && !kinds.localPlayer(index)
                        && Minecraft189VanillaMeleeAcceptance
                                .cleanCrosshairEntity(minecraft)
                        && host.featureCatalog().killAura().active() == false
                        && host.featureCatalog().autoClicker().active() == false;
                if (valid) {
                    acceptanceTriggerDisabledTargetTicks++;
                } else {
                    acceptanceTriggerDisabledTargetTicks = 0;
                }
                if (acceptanceTriggerDisabledTargetTicks >= 20) {
                    if (host.featureCatalog().triggerBot().active())
                        throw new IllegalStateException("TriggerBot pre-enabled");
                    System.out.println(
                            "CUSTOMMC_189_TRIGGERBOT_DISABLED_REAL_PLAYER_TICKS_PASS=20");
                    host.featureCatalog()
                            .enableTriggerBotForOfficialRealPlayerAcceptance();
                    acceptanceTriggerEnabled = true;
                    System.out.println(
                            "CUSTOMMC_189_TRIGGERBOT_NORMAL_MODULE_ENABLE_PASS=YES");
                    System.out.flush();
                }
            }
            if (Boolean.getBoolean("custommc.acceptance.reportAutoClicker")
                    && !acceptanceAutoClickerEnabled) {
                // Negative control: with a real, alive, zero-hurt raycast
                // target, keep the ordinary module DISABLED for 20 actual
                // automatic-action callbacks before enabling it through
                // ModuleController. No external left clicks are sent.
                if (Minecraft189VanillaMeleeAcceptance.cleanCrosshairEntity(
                        minecraft)) {
                    acceptanceDisabledTargetTicks++;
                } else {
                    acceptanceDisabledTargetTicks = 0;
                }
                if (acceptanceDisabledTargetTicks >= 20) {
                    if (host.featureCatalog().autoClicker().active()) {
                        throw new IllegalStateException(
                                "negative control: Auto Clicker pre-enabled");
                    }
                    System.out.println(
                            "CUSTOMMC_189_AUTOCLICKER_DISABLED_TARGET_TICKS_PASS=20");
                    host.featureCatalog().enableAutoClickerForOfficialAcceptance();
                    acceptanceAutoClickerEnabled = true;
                    System.out.println(
                            "CUSTOMMC_189_AUTOCLICKER_NORMAL_MODULE_ENABLE_PASS=YES");
                    System.out.flush();
                }
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraNonPlayer")
                    && !killAuraNonPlayerReported) {
                final Minecraft189WorldEntityKindState.Snapshot kindState =
                        host.worldEntityKindState().snapshot();
                final Object nonPlayer = kindState.available()
                        ? Minecraft189VanillaMeleeAcceptance.raycastLivingNonPlayer(
                                minecraft, kindState.kindBits()) : null;
                // V288: read-only diagnostic of the *existing* original
                // raycast evaluation. No second vanilla or World access,
                // no independent acceptance and no additional target query.
                final Minecraft189OriginalNonPlayerRaycastTrace.Snapshot
                        observedRay = Minecraft189VanillaMeleeAcceptance
                            .originalNonPlayerRaycastDiagnostic();
                ++killAuraNonPlayerSourceDiagCallbacks;
                if (killAuraNonPlayerSourceDiagCallbacks == 1
                        || killAuraNonPlayerSourceDiagCallbacks % 40 == 0) {
                    System.out.println(
                            "CUSTOMMC_189_ORIGINAL_NONPLAYER_RAY_DIAGNOSTIC"
                            + " sourceCallbacks="
                                + killAuraNonPlayerSourceDiagCallbacks
                            + " kindSourceAvailable=" + kindState.available()
                            + " calls=" + observedRay.calls
                            + " accepted=" + observedRay.validCallbacks
                            + " rawRayStreak="
                                + observedRay.currentAcceptedRayStreak
                            + " maxRawRayStreak="
                                + observedRay.maximumAcceptedRayStreak
                            + " rejectNoRay=" + observedRay.missRayCount
                            + " rejectRayType="
                                + observedRay.wrongRayTypeCount
                            + " rejectWorldIdentity="
                                + observedRay.worldMismatchCount
                            + " rejectNotLoaded="
                                + observedRay.identityMissingCount
                            + " rejectKindIndex="
                                + observedRay.kindIndexMismatchCount
                            + " rejectInvalidKind="
                                + observedRay.invalidKindCount
                            + " rejectReflection="
                                + observedRay.reflectionUnavailableCount
                            + " rejectSourceUnavailable="
                                + observedRay.unavailableSourceCount
                            + " rejectOther=" + observedRay.otherRejections
                            + " maxDisabledSameIdentity="
                                + killAuraNonPlayerMaxDisabledIdentityStreak
                            + " maxEnabledSameIdentity="
                                + killAuraNonPlayerMaxEnabledIdentityStreak
                            + " decision=" + observedRay.decision
                            + " nativeIndex=" + observedRay.nativeIndex
                            + " nativeKindBits=" + observedRay.nativeKindBits
                            + " nativeLoadedEntities=" + observedRay.nativeEntityCount
                            + " nativeKindCount=" + observedRay.sourceKindCount
                            + " consecutiveDisabled=" + killAuraDisabledTargetTicks
                            + " consecutiveEnabled=" + killAuraEnabledVetoTicks
                            + " auraEnabled=" + killAuraNonPlayerEnabled);
                    System.out.flush();
                }
                if (nonPlayer == null
                        || (killAuraNonPlayerTarget != null
                                && nonPlayer != killAuraNonPlayerTarget)) {
                    killAuraNonPlayerTarget = null;
                    killAuraDisabledTargetTicks = 0;
                    killAuraEnabledVetoTicks = 0;
                } else {
                    killAuraNonPlayerTarget = nonPlayer;
                    if (!killAuraNonPlayerEnabled) {
                        if (host.featureCatalog().killAura().active()) {
                            throw new IllegalStateException(
                                    "Kill Aura negative fixture pre-enabled");
                        }
                        killAuraDisabledTargetTicks++;
                        if (killAuraDisabledTargetTicks
                                > killAuraNonPlayerMaxDisabledIdentityStreak)
                            killAuraNonPlayerMaxDisabledIdentityStreak =
                                    killAuraDisabledTargetTicks;
                        if (killAuraDisabledTargetTicks >= 20) {
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_DISABLED_NONPLAYER_TICKS_PASS=20");
                            host.featureCatalog()
                                    .enableKillAuraForOfficialNonPlayerAcceptance();
                            killAuraNonPlayerEnabled = true;
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_NORMAL_MODULE_ENABLE_PASS=YES");
                            System.out.flush();
                        }
                    } else {
                        if (!host.featureCatalog().killAura().active()
                                || (host.nearestPlayerTargetState().snapshot().found())
                                || host.targetRotationState().snapshot().available()) {
                            throw new IllegalStateException(
                                    "Kill Aura accepted native nonplayer target");
                        }
                        killAuraEnabledVetoTicks++;
                        if (killAuraEnabledVetoTicks
                                > killAuraNonPlayerMaxEnabledIdentityStreak)
                            killAuraNonPlayerMaxEnabledIdentityStreak =
                                    killAuraEnabledVetoTicks;
                        if (killAuraEnabledVetoTicks >= 80) {
                            killAuraNonPlayerReported = true;
                            System.out.println(
                                    "CUSTOMMC_OFFICIAL_189_KILLAURA_NONPLAYER_VETO_PASS=YES");
                            System.out.flush();
                        }
                    }
                }
            }
            if ((Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraWallVeto")
                    || Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraWallRecovery")
                    || Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraWallLifecycle"))
                    && !acceptanceAuraWallReported) {
                // Use genuine player positions + the live vanilla LOS vector.
                // 0 means native canEntityBeSeen confirmed occluded;
                // -1 means unknown and must NEVER count as wall evidence.
                if (!acceptanceAuraWallCheckArmed) {
                    host.featureCatalog()
                            .enableWallCheckForOfficialWallVetoAcceptance();
                    acceptanceAuraWallCheckArmed = true;
                }
                final Minecraft189PlayerPositionState.Snapshot local =
                        host.playerPositionState().snapshot();
                final Minecraft189PlayerRotationState.Snapshot rotation =
                        host.playerRotationState().snapshot();
                final Minecraft189WorldEntityPositionState.Snapshot positions =
                        host.worldEntityPositionState().snapshot();
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        host.worldEntityKindState().snapshot();
                final Minecraft189WorldEntityCombatState.Snapshot combat =
                        host.worldEntityCombatState().snapshot();
                final Minecraft189WorldEntityVisibilityState.Snapshot visibility =
                        host.worldEntityVisibilityState().snapshot();
                int remoteIndex = -1;
                int remoteCount = 0;
                boolean valid = local.available() && rotation.available()
                        && positions.available() && kinds.available()
                        && combat.available() && visibility.available()
                        && positions.entityCount() == kinds.entityCount()
                        && positions.entityCount() == combat.entityCount()
                        && positions.entityCount() == visibility.entityCount();
                if (valid) {
                    for (int index = 0; index < kinds.entityCount(); index++) {
                        if (kinds.player(index) && !kinds.localPlayer(index)) {
                            remoteCount++;
                            if (kinds.living(index) && combat.alive(index))
                                remoteIndex = index;
                        }
                    }
                    valid = remoteCount == 1 && remoteIndex >= 0
                            && visibility.occluded(remoteIndex);
                }
                if (valid) {
                    final double dx = positions.x(remoteIndex) - local.x();
                    final double dy = positions.y(remoteIndex) - local.y();
                    final double dz = positions.z(remoteIndex) - local.z();
                    final double horizontal = Math.hypot(dx, dz);
                    final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    final double targetYaw =
                            Math.toDegrees(Math.atan2(dz, dx)) - 90.0D;
                    double yawError = targetYaw - rotation.yaw();
                    yawError = ((yawError + 540.0D) % 360.0D) - 180.0D;
                    valid = Double.isFinite(distance)
                            && distance >= 1.5D && distance <= 3.25D
                            && Double.isFinite(horizontal) && horizontal > 0.0D
                            && Double.isFinite(yawError)
                            && Math.abs(yawError) <= 30.0D;
                }
                if (!valid || (acceptanceAuraWallIndex >= 0
                        && remoteIndex != acceptanceAuraWallIndex)) {
                    acceptanceAuraWallIndex = -1;
                    acceptanceAuraWallDisabledTicks = 0;
                    acceptanceAuraWallEnabledTicks = 0;
                } else {
                    acceptanceAuraWallIndex = remoteIndex;
                    if (!host.featureCatalog().wallCheck().active()) {
                        throw new IllegalStateException(
                                "wall-check module unexpectedly disabled");
                    }
                    if (Double.compare(host.featureCatalog().killAura()
                            .rangeSetting().get(), 3.5D) != 0
                            || Double.compare(host.featureCatalog().killAura()
                                    .fovSetting().get(), 90.0D) != 0) {
                        throw new IllegalStateException(
                                "wall acceptance requires unchanged Aura settings");
                    }
                    if (!acceptanceAuraWallEnabled) {
                        if (host.featureCatalog().killAura().active()) {
                            throw new IllegalStateException(
                                    "wall Aura was pre-enabled");
                        }
                        if (++acceptanceAuraWallDisabledTicks >= 20) {
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_WALL_DISABLED_OCCLUDED_TICKS_PASS=20");
                            host.featureCatalog()
                                    .enableKillAuraForOfficialWallVetoAcceptance();
                            acceptanceAuraWallEnabled = true;
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_WALL_MODULE_ENABLE_PASS=YES");
                            System.out.flush();
                        }
                    } else {
                        if (!host.featureCatalog().killAura().active()) {
                            throw new IllegalStateException(
                                    "wall Aura unexpectedly disabled");
                        }
                        // Require 80 consecutive native hidden-player
                        // callbacks with NO selectable attack target.
                        if (host.nearestPlayerTargetState().snapshot().found()
                                || host.targetRotationState().snapshot().available()) {
                            acceptanceAuraWallEnabledTicks = 0;
                        } else if (++acceptanceAuraWallEnabledTicks >= 80) {
                            acceptanceAuraWallReported = true;
                            System.out.println(
                                    "CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_WALL_VETO_PASS=YES");
                            System.out.flush();
                        }
                    }
                }
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraWallLifecycle")
                    && acceptanceAuraWallReported
                    && acceptanceWallLifecyclePhase < 3) {
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        host.worldEntityKindState().snapshot();
                final Minecraft189WorldEntityCombatState.Snapshot combat =
                        host.worldEntityCombatState().snapshot();
                final Minecraft189WorldEntityVisibilityState.Snapshot los =
                        host.worldEntityVisibilityState().snapshot();
                final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                        host.nearestPlayerTargetState().snapshot();
                final Minecraft189TargetRotationState.Snapshot target =
                        host.targetRotationState().snapshot();
                final int idx = acceptanceAuraWallIndex;
                // Real native occlusion remains mandatory throughout BOTH
                // module lifecycle changes. Never count unknown -1 as 0.
                final boolean stableOccluded =
                        idx >= 0 && kinds.available() && combat.available()
                        && los.available() && idx < kinds.entityCount()
                        && idx < combat.entityCount() && idx < los.entityCount()
                        && kinds.player(idx) && kinds.living(idx)
                        && !kinds.localPlayer(idx) && combat.alive(idx)
                        && los.occluded(idx)
                        && host.featureCatalog().killAura().active();
                if ((acceptanceWallLifecyclePhase == 1
                        || acceptanceWallLifecyclePhase == 2)
                        && ++acceptanceWallLifecycleSourceEvidenceTicks % 80 == 0) {
                    // V318 extends the read-only V164 source heartbeat to
                    // PHASE 1, where original #40095144475 attempt 1 never
                    // reached 20 genuine occluded selections before timeout.
                    // Both 20-off-selection and 30-on-clear gates stay strict.
                    // No target/world mutation and no acceptance shortcut.
                    System.out.println(
                            "CUSTOMMC_189_WALL_LIFECYCLE_NATIVE_SOURCE_DIAG"
                            + " reason=" + originalWallLifecycleReason(
                                    idx, kinds, combat, los, nearest, target, host)
                            + " phase=" + acceptanceWallLifecyclePhase
                            + " sourceIndex=" + idx
                            + " kindsCount=" + kinds.entityCount()
                            + " combatCount=" + combat.entityCount()
                            + " losCount=" + los.entityCount()
                            + " nativeOccluded=" + (stableOccluded)
                            + " nearestFound=" + nearest.found()
                            + " nearestIndex=" + nearest.entityIndex()
                            + " rotationAvailable=" + target.available()
                            + " rotationIndex=" + target.entityIndex()
                            + " validConsecutiveSourceClearTicks="
                                    + acceptanceWallLifecycleReenabledVetoTicks
                            + " selectedConsecutiveSourceTicks="
                                    + acceptanceWallLifecycleDisabledSelectionTicks
                            + " wallCheckNativeActive="
                                    + host.featureCatalog().wallCheck().active());
                    System.out.flush();
                }
                if (!stableOccluded) {
                    acceptanceWallLifecycleDisabledSelectionTicks = 0;
                    acceptanceWallLifecycleReenabledVetoTicks = 0;
                } else if (acceptanceWallLifecyclePhase == 0) {
                    if (!host.featureCatalog().wallCheck().active()
                            || nearest.found() || target.available()) {
                        throw new IllegalStateException(
                                "wall lifecycle started without active occlusion veto");
                    }
                    host.featureCatalog()
                            .disableWallCheckForOfficialLifecycleAcceptance();
                    acceptanceWallLifecyclePhase = 1;
                    System.out.println(
                            "CUSTOMMC_189_WALL_LIFECYCLE_NORMAL_DISABLE_PASS=YES");
                    System.out.flush();
                } else if (acceptanceWallLifecyclePhase == 1) {
                    if (host.featureCatalog().wallCheck().active()) {
                        throw new IllegalStateException(
                                "wall check reenabled too early");
                    }
                    if (nearest.found() && nearest.entityIndex() == idx
                            && target.available() && target.entityIndex() == idx) {
                        if (++acceptanceWallLifecycleDisabledSelectionTicks >= 20) {
                            System.out.println(
                                    "CUSTOMMC_189_WALL_LIFECYCLE_OCCLUDED_SELECT_WHEN_OFF_PASS=20");
                            host.featureCatalog()
                                    .reenableWallCheckForOfficialLifecycleAcceptance();
                            acceptanceWallLifecyclePhase = 2;
                            System.out.println(
                                    "CUSTOMMC_189_WALL_LIFECYCLE_NORMAL_REENABLE_PASS=YES");
                            System.out.flush();
                        }
                    } else {
                        acceptanceWallLifecycleDisabledSelectionTicks = 0;
                    }
                } else {
                    if (!host.featureCatalog().wallCheck().active()) {
                        throw new IllegalStateException(
                                "wall check was not reenabled");
                    }
                    if (nearest.found() || target.available()) {
                        // A newly re-enabled wall check must invalidate the
                        // once-selected but now occluded candidate; never
                        // accept stale target as a completed veto.
                        acceptanceWallLifecycleReenabledVetoTicks = 0;
                    } else if (++acceptanceWallLifecycleReenabledVetoTicks >= 30) {
                        acceptanceWallLifecyclePhase = 3;
                        System.out.println(
                                "CUSTOMMC_OFFICIAL_189_WALL_LIFECYCLE_STALE_TARGET_CLEARED_PASS=YES");
                        System.out.flush();
                    }
                }
            }
            if ((Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraWallRecovery")
                    || Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraWallLifecycle"))
                    && acceptanceAuraWallReported
                    && !acceptanceAuraWallRecoveryVisibleReported) {
                final Minecraft189WorldEntityVisibilityState.Snapshot visibility =
                        host.worldEntityVisibilityState().snapshot();
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        host.worldEntityKindState().snapshot();
                final Minecraft189WorldEntityCombatState.Snapshot combat =
                        host.worldEntityCombatState().snapshot();
                final Minecraft189TargetRotationState.Snapshot selected =
                        host.targetRotationState().snapshot();
                final int idx = acceptanceAuraWallIndex;
                if (host.featureCatalog().wallCheck().active()
                        && host.featureCatalog().killAura().active()
                        && idx >= 0 && kinds.available() && combat.available()
                        && visibility.available()
                        && idx < kinds.entityCount() && idx < combat.entityCount()
                        && idx < visibility.entityCount()
                        && kinds.player(idx) && kinds.living(idx)
                        && !kinds.localPlayer(idx) && combat.alive(idx)
                        && visibility.visible(idx)
                        && selected.available() && selected.entityIndex() == idx) {
                    acceptanceAuraWallRecoveryVisibleReported = true;
                    System.out.println(
                            "CUSTOMMC_189_WALL_RECOVERY_NATIVE_VISIBLE_SELECTED_PASS=YES");
                    System.out.flush();
                }
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraFovVeto")
                    && !acceptanceAuraFovReported) {
                // Once enabled, the real Aura selector correctly clears its
                // rejected target. Measure yaw from the unmodified native
                // loaded-player position instead of requiring targetRotation.
                final Minecraft189PlayerRotationState.Snapshot rotation =
                        host.playerRotationState().snapshot();
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        host.worldEntityKindState().snapshot();
                final Minecraft189PlayerPositionState.Snapshot local =
                        host.playerPositionState().snapshot();
                final Minecraft189WorldEntityPositionState.Snapshot positions =
                        host.worldEntityPositionState().snapshot();
                int remoteIndex = -1;
                int remoteCount = 0;
                boolean valid = rotation.available()
                        && kinds.available() && positions.available()
                        && local.available()
                        && kinds.entityCount() == positions.entityCount();
                if (valid) {
                    for (int index = 0; index < kinds.entityCount(); index++) {
                        if (kinds.player(index) && !kinds.localPlayer(index)) {
                            remoteCount++;
                            if (kinds.living(index))
                                remoteIndex = index;
                        }
                    }
                    valid = remoteCount == 1 && remoteIndex >= 0;
                }
                if (valid) {
                    final double dx = positions.x(remoteIndex) - local.x();
                    final double dy = positions.y(remoteIndex) - local.y();
                    final double dz = positions.z(remoteIndex) - local.z();
                    final double horizontal = Math.hypot(dx, dz);
                    final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    final double targetYaw = Math.toDegrees(Math.atan2(dz, dx))
                            - 90.0D;
                    double yawError = targetYaw - rotation.yaw();
                    yawError = ((yawError + 540.0D) % 360.0D) - 180.0D;
                    valid = Double.isFinite(distance)
                            && distance >= 1.5D && distance <= 3.25D
                            && Double.isFinite(horizontal) && horizontal > 0.0D
                            && Double.isFinite(yawError)
                            && Math.abs(yawError) >= 120.0D
                            // The original client must have consumed both
                            // genuine dedicated-server player teleports
                            // BEFORE test Aura can be enabled after 20 reads.
                            && originalFovNegativeFixtureSourceAligned(dx, dy, dz);
                }
                // V52: read-only witness for V51's intermittent failure to
                // observe 80 consecutive *real* rear-player FOV rejections.
                // A complete native callback is required; merely enabling
                // Aura or seeing the real server's teleport is insufficient.
                // Never change World, yaw, FOV, target, click or strict test.
                if (++acceptanceAuraFovSourceDiagTicks % 80 == 0) {
                    final Minecraft189NearestPlayerTargetState.Snapshot nativeTarget =
                            host.nearestPlayerTargetState().snapshot();
                    final Minecraft189TargetRotationState.Snapshot nativeRotation =
                            host.targetRotationState().snapshot();
                    // V56 diagnosis deliberately does NOT reuse the final
                    // 'valid' veto. Reusing it made every rejected ray show
                    // NaN and hid the actual cause of missing rear witnesses.
                    // The values below are READ ONLY; admission below keeps
                    // the exact same source/FOV/range constraints.
                    final boolean completeNativeSnapshots =
                            rotation.available() && kinds.available()
                            && positions.available() && local.available()
                            && kinds.entityCount() == positions.entityCount();
                    final boolean geometricSource = completeNativeSnapshots
                            && remoteIndex >= 0
                            && remoteIndex < positions.entityCount();
                    final double dx = geometricSource
                            ? positions.x(remoteIndex) - local.x() : Double.NaN;
                    final double dy = geometricSource
                            ? positions.y(remoteIndex) - local.y() : Double.NaN;
                    final double dz = geometricSource
                            ? positions.z(remoteIndex) - local.z() : Double.NaN;
                    final double actualDistance = Math.sqrt(
                            dx * dx + dy * dy + dz * dz);
                    final double actualHorizontal = Math.hypot(dx, dz);
                    final double nativeTargetYaw =
                            Math.toDegrees(Math.atan2(dz, dx)) - 90.0D;
                    final double nativeYawDelta = ((nativeTargetYaw
                            - rotation.yaw() + 540.0D) % 360.0D) - 180.0D;
                    System.out.println("CUSTOMMC_189_FOV_ORIGINAL_SOURCE_OBSERVATION"
                            + " tick=" + acceptanceAuraFovSourceDiagTicks
                            + " nativeSourceValid=" + valid
                            + " nativeSnapshotsComplete=" + completeNativeSnapshots
                            + " nativeRotationAvailable=" + rotation.available()
                            + " nativeKindsAvailable=" + kinds.available()
                            + " nativePositionsAvailable=" + positions.available()
                            + " nativeLocalPositionAvailable=" + local.available()
                            + " nativeEntityCountsMatch=" + (kinds.entityCount() == positions.entityCount())
                            + " nativeDistance=" + actualDistance
                            + " nativeHorizontal=" + actualHorizontal
                            + " geometricSource=" + geometricSource
                            + " actualRemoteIndex=" + remoteIndex
                            + " actualRemoteCount=" + remoteCount
                            + " nativePlayerYaw="
                                + (rotation.available() ? rotation.yaw() : Float.NaN)
                            + " actualTargetYaw=" + nativeTargetYaw
                            + " actualYawDelta=" + nativeYawDelta
                            + " originalFovWorldAligned="
                                + originalFovNegativeFixtureSourceAligned(dx,dy,dz)
                            + " selectedFound=" + nativeTarget.found()
                            + " selectedRotationAvailable=" + nativeRotation.available()
                            + " auraEnabled=" + acceptanceAuraFovEnabled
                            + " stableDisabledTicks=" + acceptanceAuraFovDisabledTicks
                            + " stableEnabledVetoTicks=" + acceptanceAuraFovEnabledTicks);
                    System.out.flush();
                }
                // V284 preserves the last truly original yaw at which this
                // same remote source was still verified BEHIND the camera
                // after normal module activation. This observes only the
                // already-read acceptance snapshots; it cannot permit a click.
                if (valid && acceptanceAuraFovEnabled) {
                    acceptanceAuraFovLastRearYaw = rotation.yaw();
                    acceptanceAuraFovLastRearTick =
                            acceptanceAuraFovSourceDiagTicks;
                    acceptanceAuraFovLastRearIndex = remoteIndex;
                }
                if (!valid || (acceptanceAuraFovTargetIndex >= 0
                        && remoteIndex != acceptanceAuraFovTargetIndex)) {
                    acceptanceAuraFovTargetIndex = -1;
                    acceptanceAuraFovDisabledTicks = 0;
                    acceptanceAuraFovEnabledTicks = 0;
                } else {
                    acceptanceAuraFovTargetIndex = remoteIndex;
                    if (Double.compare(host.featureCatalog().killAura()
                            .fovSetting().get(), 90.0D) != 0) {
                        throw new IllegalStateException(
                                "FOV acceptance requires unchanged 90-degree setting");
                    }
                    if (!acceptanceAuraFovEnabled) {
                        if (host.featureCatalog().killAura().active())
                            throw new IllegalStateException("FOV module pre-enabled");
                        if (++acceptanceAuraFovDisabledTicks >= 20) {
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_FOV_DISABLED_TARGET_TICKS_PASS=20");
                            host.featureCatalog()
                                    .enableKillAuraForOfficialFovVetoAcceptance();
                            acceptanceAuraFovEnabled = true;
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_FOV_MODULE_ENABLE_PASS=YES");
                            System.out.flush();
                        }
                    } else {
                        if (!host.featureCatalog().killAura().active())
                            throw new IllegalStateException(
                                    "FOV module disabled unexpectedly");
                        // An actual rejected remote player must remain absent
                        // from BOTH selected player and attack rotation state.
                        if (host.nearestPlayerTargetState().snapshot().found()
                                || host.targetRotationState().snapshot().available()) {
                            acceptanceAuraFovEnabledTicks = 0;
                        } else if (++acceptanceAuraFovEnabledTicks >= 80) {
                            acceptanceAuraFovReported = true;
                            System.out.println(
                                    "CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_FOV_VETO_PASS=YES");
                            System.out.flush();
                        }
                    }
                }
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraRangeVeto")
                    && !acceptanceAuraFarReported) {
                // Real-game fail-closed test: one genuinely server-owned
                // remote living player, ~7 real blocks from CIHost in the
                // same loaded dedicated-server chunk. The V165 native source
                // fixture uses 7 +/- 0.5; the pre-V165 3.8..4.8 check
                // rejected true 49.0 distanceSquared forever. Preserve
                // Aura's ORIGINAL configured 3.5 reach and native no-hit
                // negative; this is ONLY an observation-validity window.
                final Minecraft189PlayerPositionState.Snapshot local =
                        host.playerPositionState().snapshot();
                final Minecraft189WorldEntityPositionState.Snapshot positions =
                        host.worldEntityPositionState().snapshot();
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        host.worldEntityKindState().snapshot();
                int farIndex = -1;
                int realRemotePlayers = 0;
                double lastRemoteDistanceSquared = Double.NaN;
                if (local.available() && positions.available()
                        && kinds.available()
                        && kinds.entityCount() == positions.entityCount()) {
                    boolean ambiguous = false;
                    for (int i = 0; i < kinds.entityCount(); i++) {
                        if (!kinds.player(i) || kinds.localPlayer(i))
                            continue;
                        realRemotePlayers++;
                        final double dx = positions.x(i) - local.x();
                        final double dy = positions.y(i) - local.y();
                        final double dz = positions.z(i) - local.z();
                        final double squared = dx * dx + dy * dy + dz * dz;
                        lastRemoteDistanceSquared = squared;
                        if (farIndex >= 0 || !kinds.living(i)
                                || !originalSevenBlockRangeFixtureDistance(
                                        squared)) {
                            ambiguous = true;
                            break;
                        }
                        farIndex = i;
                    }
                    if (ambiguous) farIndex = -1;
                }
                // Bounded read-only diagnostics for *why* a real
                // server-connected player failed the far-range precondition.
                // No pass marker, fabricated entity, or range adjustment.
                if (++acceptanceAuraFarEvidenceChecks % 200 == 0) {
                    System.out.println(
                            "CUSTOMMC_189_RANGE_REAL_EVIDENCE"
                            + " local=" + local.available()
                            + " positions=" + positions.available()
                            + " kinds=" + kinds.available()
                            + " entities=" + positions.entityCount()
                            + " remotePlayers=" + realRemotePlayers
                            + " distanceSquared=" + lastRemoteDistanceSquared
                            + " validSevenBlockRange="
                                    + originalSevenBlockRangeFixtureDistance(
                                            lastRemoteDistanceSquared)
                            + " qualifyingIndex=" + farIndex
                            + " auraEnabledForAcceptance=" + acceptanceAuraFarEnabled
                            + " disabledConsecutive=" + acceptanceAuraFarDisabledTicks
                            + " enabledConsecutive=" + acceptanceAuraFarEnabledTicks);
                    System.out.flush();
                }
                if (farIndex < 0
                        || (acceptanceAuraFarTargetIndex >= 0
                        && acceptanceAuraFarTargetIndex != farIndex)) {
                    acceptanceAuraFarTargetIndex = -1;
                    acceptanceAuraFarDisabledTicks = 0;
                    acceptanceAuraFarEnabledTicks = 0;
                } else {
                    acceptanceAuraFarTargetIndex = farIndex;
                    if (!acceptanceAuraFarEnabled) {
                        if (host.featureCatalog().killAura().active())
                            throw new IllegalStateException(
                                    "out-of-range aura was pre-enabled");
                        acceptanceAuraFarDisabledTicks++;
                        if (acceptanceAuraFarDisabledTicks >= 20) {
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_RANGE_DISABLED_TICKS_PASS=20");
                            host.featureCatalog()
                                    .enableKillAuraForOfficialRangeVetoAcceptance();
                            acceptanceAuraFarEnabled = true;
                            System.out.println(
                                    "CUSTOMMC_189_KILLAURA_RANGE_MODULE_ENABLE_PASS=YES");
                            System.out.flush();
                        }
                    } else {
                        if (!host.featureCatalog().killAura().active())
                            throw new IllegalStateException(
                                    "out-of-range aura deactivated unexpectedly");
                        // The first few callbacks may still contain a
                        // pre-activation nearest snapshot; wait for live
                        // WorldCombat to recompute with Aura's range.
                        if (host.nearestPlayerTargetState().snapshot().found()
                                || host.targetRotationState().snapshot().available()) {
                            acceptanceAuraFarEnabledTicks = 0;
                        } else {
                            acceptanceAuraFarEnabledTicks++;
                            if (acceptanceAuraFarEnabledTicks >= 80) {
                                acceptanceAuraFarReported = true;
                                System.out.println(
                                        "CUSTOMMC_OFFICIAL_189_KILLAURA_REAL_PLAYER_RANGE_VETO_PASS=YES");
                                System.out.flush();
                            }
                        }
                    }
                }
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraMultiTarget")) {
                Minecraft189MultiTargetAcceptance.observe(host);
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraSimultaneous")) {
                Minecraft189SimultaneousTargetAcceptance.observe(host);
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraLockedTarget")) {
                Minecraft189LockedTargetAcceptance.observe(host);
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraRejoin")) {
                Minecraft189RejoinTargetAcceptance.observe(host);
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraLocalPvp")) {
                final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                        host.nearestPlayerTargetState().snapshot();
                final Minecraft189TargetRotationState.Snapshot target =
                        host.targetRotationState().snapshot();
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        host.worldEntityKindState().snapshot();
                final int index = target.available() ? target.entityIndex() : -1;
                final boolean genuinePlayer = nearest.found()
                        && target.available() && index >= 0
                        && nearest.entityIndex() == index
                        && kinds.available()
                        && kinds.player(index) && !kinds.localPlayer(index)
                        && target.distance() <= 3.5D;
                if (!acceptanceAuraPlayerEnabled) {
                    if (host.featureCatalog().killAura().active()) {
                        throw new IllegalStateException(
                                "localhost aura pre-enabled unexpectedly");
                    }
                    if (genuinePlayer) {
                        if (index != acceptanceAuraPlayerIndex) {
                            acceptanceAuraPlayerIndex = index;
                            acceptanceAuraPlayerTargetTicks = 0;
                        }
                        acceptanceAuraPlayerTargetTicks++;
                    } else {
                        acceptanceAuraPlayerIndex = -1;
                        acceptanceAuraPlayerTargetTicks = 0;
                    }
                    if (acceptanceAuraPlayerTargetTicks >= 20) {
                        System.out.println(
                                "CUSTOMMC_189_KILLAURA_DISABLED_REAL_PLAYER_TICKS_PASS=20");
                        host.featureCatalog()
                                .enableKillAuraForOfficialLocalPvpAcceptance();
                        acceptanceAuraPlayerEnabled = true;
                        System.out.println(
                                "CUSTOMMC_189_KILLAURA_PLAYER_MODULE_ENABLE_PASS=YES");
                        System.out.flush();
                    }
                }
            }
            // Preserve all original read-only acceptance and negative
            // controls above this point; none authorize automated input.
            // Opening a vanilla GUI vetoes Pot, Rod, melee and reblock in
            // this tokenized cycle, regardless of CustomMC ClickGUI.
            if (vetoOriginalScreenBeforeNativeCombat(host, minecraft))
                return;
            final Minecraft189SwordBlockControl swordBlock =
                    minecraft instanceof Minecraft189SwordBlockControl
                    ? (Minecraft189SwordBlockControl) minecraft : null;
            // M351 verified the exact live vanilla hit-result fields.
            final boolean playerHit =
                    minecraft instanceof Minecraft189CrosshairHitAccess
                    && ((Minecraft189CrosshairHitAccess) minecraft)
                            .customMcCrosshairPlayerHit();
            final int playerIndex = playerHit
                    ? ((Minecraft189CrosshairHitAccess) minecraft)
                            .customMcCrosshairPlayerIndex()
                    : -1;
            // Preserve the exact original player identity observed at the
            // first native raycast for an optional final HitSelect check.
            // Never allow a later reuse of this World list slot to inherit
            // an earlier target's injury window.
            final UUID hitSelectInitialUuid =
                    host.featureCatalog().hitSelect().active()
                    && playerHit && playerIndex >= 0
                    ? host.worldEntityUuidState().snapshot().at(playerIndex)
                    : null;
            // V51: the real Mojang objectMouseOver is sampled before
            // synthetic action scheduling; observe the original ray while B
            // is source-UUID-locked, even if no controller click follows.
            // Test-only and read-only; original manual mouse and all gates
            // retain their existing semantics and strict verification.
            if (Boolean.getBoolean(
                    "custommc.acceptance.reportKillAuraRejoin"))
                Minecraft189RejoinTargetAcceptance
                        .observeOriginalCrosshairAtClick(
                                host, playerHit, playerIndex);
            // Expire an owned use-item latch even when no attack is due.
            // A real manual right-click takes ownership and is never released
            // by this synthetic module.
            if (swordBlock != null
                    && host.shouldReleaseAutoBlock(playerHit, playerIndex)
                    && swordBlock.customMcIsUsingItem()) {
                swordBlock.customMcStopUsingItem();
            }
            // Healing item has priority over Rod and synthetic attacks.
            // Each winning action owns the full native right-click lane.
            if (host.shouldAutoPot(playerHit, playerIndex)) {
                // Only an actual potion takes the combat action lane.
                final int originalPotionSlot = host.selectAutoPotSlot();
                if (originalPotionSlot >= 0) {
                    try {
                        if (!host.claimSyntheticCombatPrimary(
                                combatCycleToken,
                                Minecraft189CombatCycleArbiter.Primary.POTION))
                            return;
                        if (swordBlock != null && host.releaseAutoBlockBeforeAction()
                                && swordBlock.customMcIsUsingItem())
                            swordBlock.customMcStopUsingItem();
                        // AutoBlock stopUsingItem is an external mapped
                        // callback: it may change the real selected slot,
                        // held stack or ClickGUI focus. Re-verify the exact
                        // inventory lease before *any* synthetic right click.
                        // The optional downward-aim lease is taken only
                        // AFTER original AutoBlock release and native source
                        // potion revalidation. The same live EntityPlayer
                        // pitch must remain ours at actual right-click.
                        Minecraft189AutoPotSourceCast.execute(
                                host, originalPotionSlot,
                                () -> minecraft.customMcRightClickMouse(),
                                () -> !vetoOriginalScreenBeforeNativeCombat(
                                        host, minecraft));
                    } finally {
                        host.restoreAutoPotSlot(originalPotionSlot);
                    }
                    return;
                }
                // Incorrect or unknown item type: the slot is already
                // restored. Rod or ordinary click can still run.
            }
            // Rod consumes one synthetic combat action on its own tick.
            // Normal clicks are not double-issued on the same tick.
            if (host.shouldAutoRod(playerHit, playerIndex)) {
                final int originalRodSlot = host.selectAutoRodSlot();
                if (originalRodSlot >= 0) {
                    try {
                        if (!host.claimSyntheticCombatPrimary(
                                combatCycleToken,
                                Minecraft189CombatCycleArbiter.Primary.ROD))
                            return;
                        if (swordBlock != null && host.releaseAutoBlockBeforeAction()
                                && swordBlock.customMcIsUsingItem())
                            swordBlock.customMcStopUsingItem();
                        if (host.mayExecuteAutoRodUse(originalRodSlot)) {
                            if (vetoOriginalScreenBeforeNativeCombat(
                                    host, minecraft)) return;
                            minecraft.customMcRightClickMouse();
                            host.commitAutoRodUseAttempt(originalRodSlot);
                        }
                    } finally {
                        host.restoreAutoRodSlot(originalRodSlot);
                    }
                    return;
                }
                // No verified rod in the configured slot: fall through.
            }
            final double[] nativeHitbox = playerHit
                    && host.needsHitboxRangeEvidence()
                    ? ((Minecraft189CrosshairHitAccess) minecraft)
                            .customMcCrosshairHitboxBounds()
                    : null;
            // M419 acceptance only: a real native rejoin/server teleport
            // can make A temporarily selected while the mandatory B UUID
            // lock is not yet stable. Fail CLOSED before synthetic melee
            // scheduling: never send an A click, never fake B damage or
            // count this veto as a pass. Strict source-native 20 snapshots,
            // real controller UUID B and server damage remain compulsory.
            if (Minecraft189RejoinTargetAcceptance
                    .vetoTransientUnownedPhaseFourSyntheticClick(
                            host, playerHit, playerIndex)) return;
            if (host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)) {
                if (!host.claimSyntheticCombatPrimary(
                        combatCycleToken,
                        Minecraft189CombatCycleArbiter.Primary.MELEE))
                    return;
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraRangeVeto")) {
                    throw new IllegalStateException(
                            "out-of-range real player caused synthetic attack");
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraWallVeto")) {
                    throw new IllegalStateException(
                            "real occluded player caused synthetic attack");
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraWallRecovery")
                        || Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraWallLifecycle")) {
                    final Minecraft189WorldEntityVisibilityState.Snapshot los =
                            host.worldEntityVisibilityState().snapshot();
                    final Minecraft189TargetRotationState.Snapshot selected =
                            host.targetRotationState().snapshot();
                    if (!acceptanceAuraWallReported
                            || (Boolean.getBoolean(
                                    "custommc.acceptance.reportKillAuraWallLifecycle")
                                    && acceptanceWallLifecyclePhase != 3)
                            || !acceptanceAuraWallRecoveryVisibleReported
                            || !host.featureCatalog().wallCheck().active()
                            || !host.featureCatalog().killAura().active()
                            || !playerHit || playerIndex < 0
                            || playerIndex != acceptanceAuraWallIndex
                            || !los.visible(playerIndex)
                            || !selected.available()
                            || selected.entityIndex() != playerIndex) {
                        throw new IllegalStateException(
                                "wall recovery attacked without genuine visible original player");
                    }
                    if (!acceptanceAuraWallRecoveryClickReported) {
                        acceptanceAuraWallRecoveryClickReported = true;
                        System.out.println(
                                "CUSTOMMC_189_WALL_RECOVERY_OWNED_VISIBLE_ATTACK_PASS=YES");
                        System.out.flush();
                    }
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraFovVeto")) {
                    // Diagnostic witness ONLY after the strict policy has
                    // already rejected this click. Never mutate angle,
                    // selector, World, input, entity or source authority.
                    final Minecraft189PlayerRotationState.Snapshot angle =
                            host.playerRotationState().snapshot();
                    final Minecraft189PlayerPositionState.Snapshot local =
                            host.playerPositionState().snapshot();
                    final Minecraft189WorldEntityPositionState.Snapshot positions =
                            host.worldEntityPositionState().snapshot();
                    final Minecraft189WorldEntityKindState.Snapshot kinds =
                            host.worldEntityKindState().snapshot();
                    final Minecraft189NearestPlayerTargetState.Snapshot nearest =
                            host.nearestPlayerTargetState().snapshot();
                    final Minecraft189TargetRotationState.Snapshot selected =
                            host.targetRotationState().snapshot();
                    final int idx = playerIndex;
                    final boolean indexed = idx >= 0
                            && positions.available() && kinds.available()
                            && positions.entityCount() == kinds.entityCount()
                            && idx < positions.entityCount();
                    final double dx = indexed && local.available()
                            ? positions.x(idx) - local.x() : Double.NaN;
                    final double dz = indexed && local.available()
                            ? positions.z(idx) - local.z() : Double.NaN;
                    final double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0D;
                    double delta = (targetYaw - angle.yaw()) % 360.0D;
                    if (delta < -180.0D) delta += 360.0D;
                    if (delta >= 180.0D) delta -= 360.0D;
                    final Minecraft189OriginalFovAuraAimTrace.Snapshot
                            originalAim = acceptanceAuraFovAimTrace.snapshot();
                    final Minecraft189OriginalFovGuardedYawWriteTrace.Snapshot
                            nativeWrites = acceptanceFovNativeYawWrites.snapshot();
                    System.out.println(
                            "CUSTOMMC_189_NATIVE_FOV_UNEXPECTED_CLICK_EVIDENCE"
                            + " nativePlayerHit=" + playerHit
                            + " nativeIndex=" + idx
                            + " nativeIndexed=" + indexed
                            + " playerYaw=" + angle.yaw()
                            + " playerPitch=" + angle.pitch()
                            + " completedGuardedNativeYawWrites="
                                + nativeWrites.completedWrites
                            + " auraCallbackNativeYawWrites="
                                + nativeWrites.auraAttributableWrites
                            + " lastCompletedNativeYawSetter="
                                + nativeWrites.lastCompletedYaw
                            + " lastAuraNativeYawSetter="
                                + nativeWrites.lastAuraCompletedYaw
                            + " originalAimCallbacks=" + originalAim.callbacks
                            + " originalAimEligibleCallbacks="
                                + originalAim.eligibleCallbacks
                            + " lastOriginalPreAimYaw="
                                + originalAim.lastPreAimYaw
                            + " lastEligibleAimPreYaw="
                                + originalAim.lastEligiblePreAimYaw
                            + " lastEligibleAimTargetYaw="
                                + originalAim.lastEligibleSelectedYaw
                            + " lastEligibleAimTargetIndex="
                                + originalAim.lastEligibleSelectedIndex
                            + " lastVerifiedRearYaw="
                                + acceptanceAuraFovLastRearYaw
                            + " lastVerifiedRearTick="
                                + acceptanceAuraFovLastRearTick
                            + " lastVerifiedRearIndex="
                                + acceptanceAuraFovLastRearIndex
                            + " nativeYawShiftCause="
                                + m284FovYawShiftCause(
                                    acceptanceAuraFovLastRearYaw,
                                    angle.yaw(),
                                    acceptanceAuraFovLastRearTick >= 0)
                            + " localAvailable=" + local.available()
                            + " actualDx=" + dx + " actualDz=" + dz
                            + " actualTargetYaw=" + targetYaw
                            + " actualYawDelta=" + delta
                            + " configuredFov="
                            + host.featureCatalog().killAura().fovSetting().get()
                            + " independentlyPermits="
                            + Minecraft189KillAuraNativeFovGate.permits(
                                idx, host.featureCatalog().killAura().fovSetting().get(),
                                angle, local, positions, kinds)
                            + " nearestFound=" + nearest.found()
                            + " nearestIndex=" + nearest.entityIndex()
                            + " selectedAvailable=" + selected.available()
                            + " selectedIndex=" + selected.entityIndex()
                            + " selectedYaw=" + selected.yaw()
                            + " selectedDistance=" + selected.distance()
                            + " entityCount=" + positions.entityCount()
                            + " auraActive=" + host.featureCatalog().killAura().active());
                    System.out.flush();
                    throw new IllegalStateException(
                            "out-of-FOV real player caused synthetic attack");
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraMultiTarget")) {
                    Minecraft189MultiTargetAcceptance.verifyOwnedClick(
                            host, playerHit, playerIndex);
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraSimultaneous")) {
                    Minecraft189SimultaneousTargetAcceptance.verifyOwnedClick(
                            host, playerHit, playerIndex);
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraLockedTarget")) {
                    Minecraft189LockedTargetAcceptance.verifyOwnedClick(
                            host, playerHit, playerIndex);
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraRejoin")) {
                    Minecraft189RejoinTargetAcceptance.verifyOwnedClick(
                            host, playerHit, playerIndex);
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraLocalPvp")) {
                    if (!acceptanceAuraPlayerEnabled
                            || !host.featureCatalog().killAura().active()
                            || !playerHit || playerIndex < 0
                            || playerIndex != host.targetRotationState()
                                    .snapshot().entityIndex()) {
                        throw new IllegalStateException(
                                "localhost aura automatic attack failed owner/raycast");
                    }
                    if (!acceptanceAuraAttackReported) {
                        acceptanceAuraAttackReported = true;
                        System.out.println(
                                "CUSTOMMC_189_KILLAURA_REAL_PLAYER_ROTATION_GATE_PASS=YES");
                        System.out.println(
                                "CUSTOMMC_189_KILLAURA_SYNTHETIC_PLAYER_ATTACK_PASS=YES");
                        System.out.flush();
                    }
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraNonPlayer")) {
                    throw new IllegalStateException(
                            "Kill Aura nonplayer fixture attempted a synthetic attack");
                }
                if (swordBlock != null && host.releaseAutoBlockBeforeAction()
                        && swordBlock.customMcIsUsingItem())
                    swordBlock.customMcStopUsingItem();
                final boolean alreadyUsingItem = swordBlock != null
                        && swordBlock.customMcIsUsingItem();
                final boolean restoreSprint =
                        host.shouldKeepSprintAfterSyntheticClick(playerHit);
                final int originalSlot =
                        host.selectCombatSlotBeforeSyntheticClick(playerHit);
                try {
                    // Source native hotbar selection and source combat
                    // dispatch are separate callback boundaries. If a
                    // nested Rod/Pot/melee action already owns currentItem,
                    // or the original player changed during selection,
                    // the failed -1 lease must not still attack with the
                    // other module's temporary item. A clean no-switch
                    // vanilla attack remains legal with no active owner.
                    if (!host.mayDispatchSyntheticMelee(originalSlot))
                        return;
                    if (Boolean.getBoolean(
                            "custommc.acceptance.reportTriggerBotRealPvp")) {
                        if (!acceptanceTriggerEnabled
                                || !host.featureCatalog().triggerBot().active()
                                || host.featureCatalog().killAura().active()
                                || host.featureCatalog().autoClicker().active()
                                || !playerHit || playerIndex < 0)
                            throw new IllegalStateException(
                                    "triggerbot original attack escaped owner/disabled gates");
                        if (!acceptanceTriggerOwnedClickReported) {
                            acceptanceTriggerOwnedClickReported = true;
                            System.out.println(
                                    "CUSTOMMC_189_TRIGGERBOT_NATIVE_RAY_UUID_OWNED_CLICK_PASS=YES");
                            System.out.flush();
                        }
                        // Do not arm a positive native TriggerBot attack
                        // until the exact scheduled UUID is reverified at
                        // original controller dispatch below.
                    }
                    if (Boolean.getBoolean(
                            "custommc.acceptance.reportAutoClicker")) {
                        if (!acceptanceAutoClickerEnabled
                                || !host.featureCatalog().autoClicker().active()) {
                            throw new IllegalStateException(
                                    "automatic attack escaped disabled module");
                        }
                        Minecraft189VanillaMeleeAcceptance.syntheticClickStarted();
                    }
                    if (Boolean.getBoolean(
                            "custommc.acceptance.reportKillAuraLocalPvp")) {
                        Minecraft189VanillaMeleeAcceptance.syntheticAuraStarted();
                    }
                    try {
                        // A Kill Aura owned player attack is NOT a physical
                        // left-click: Minecraft.clickMouse is guarded by the
                        // focus/leftClickCounter (potentially 10000 ticks).
                        // Invoke Mojang PlayerControllerMP.attackEntity using
                        // the actual live crosshair entity instead. All
                        // native target/rotation/range gating above remains.
                        final boolean triggerOwnedPlayerAttack =
                                playerHit && playerIndex >= 0
                                && host.featureCatalog().triggerBot().active()
                                && !host.featureCatalog().killAura().active()
                                && !host.featureCatalog().autoClicker().active();
                        final boolean auraOwnedPlayerAttack =
                                playerHit && playerIndex >= 0
                                && host.featureCatalog().killAura().active()
                                && host.targetRotationState().snapshot().available()
                                && host.targetRotationState().snapshot()
                                        .entityIndex() == playerIndex;
                        if (triggerOwnedPlayerAttack) {
                            // Stale original clickMouse leftClickCounter may
                            // be 10000 after a server teleport: do not fake
                            // physical mouse input or bypass the native game
                            // controller. Bind the live original raycast
                            // UUID to its real attackEntity argument.
                            final Minecraft189WorldEntityUuidState.Snapshot ids =
                                    host.worldEntityUuidState().snapshot();
                            final Minecraft189WorldEntityKindState.Snapshot kinds =
                                    host.worldEntityKindState().snapshot();
                            if (!ids.available() || !kinds.available()
                                    || ids.entityCount() != kinds.entityCount()
                                    || playerIndex >= ids.entityCount()
                                    || !kinds.player(playerIndex)
                                    || kinds.localPlayer(playerIndex))
                                throw new IllegalStateException(
                                        "TriggerBot dispatch lacks original remote UUID");
                            final UUID sourceUuid = ids.at(playerIndex);
                            if (sourceUuid == null)
                                throw new IllegalStateException(
                                        "TriggerBot dispatch has null source UUID");
                            // The current World entity at playerIndex can
                            // differ from the source native UUID that
                            // originally earned TriggerBot CPS/confirmation.
                            // A changed A->B->A index or OFF->ON must not
                            // receive a synthetic attack. The original
                            // entity/ray is checked again in the real
                            // controller hook after this one-shot gate.
                            if (!host.consumeTriggerDispatchUuid(sourceUuid))
                                return;
                            if (Boolean.getBoolean(
                                    "custommc.acceptance.reportTriggerBotRealPvp")) {
                                Minecraft189VanillaMeleeAcceptance.syntheticTriggerStarted();
                                Minecraft189VanillaMeleeAcceptance.onNativeClick(
                                        minecraft);
                            }
                            if (!permitsHitSelectAtNativeDispatch(
                                    host, minecraft, playerIndex,
                                    hitSelectInitialUuid))
                                return;
                            if (vetoOriginalScreenBeforeNativeCombat(
                                    host, minecraft)) return;
                            triggerDispatchUuid.set(sourceUuid);
                            try {
                                minecraft.customMcAttackCrosshairEntity();
                            } finally {
                                triggerDispatchUuid.remove();
                            }
                        } else if (auraOwnedPlayerAttack) {
                            if (Boolean.getBoolean(
                                    "custommc.acceptance.reportKillAuraLocalPvp")) {
                                // Bind the exact same real entity and zero
                                // hurt-time before calling the native attack.
                                Minecraft189VanillaMeleeAcceptance.onNativeClick(
                                        minecraft);
                            }
                            // Capture the already source-mapped, native UUID
                            // at the LAST owner-gated point before dispatch.
                            // The original controller checks the ACTUAL entity
                            // argument against this synchronous intent before
                            // it can send any vanilla attack packet.
                            UUID ownerUuid = host.worldEntityUuidState()
                                    .snapshot().at(playerIndex);
                            if (Boolean.getBoolean(
                                    "custommc.acceptance.requireScopedAuraOwnerVeto"))
                                ownerUuid = Minecraft189RejoinTargetAcceptance
                                        .scopedOwnerIntentForAcceptance(ownerUuid);
                            if (Boolean.getBoolean(
                                    "custommc.acceptance.requireUnlockedAuraOwnerVeto"))
                                ownerUuid = acceptanceUnlockedOwnerForNativeProof(
                                        host, ownerUuid);
                            if (Boolean.getBoolean(
                                    "custommc.acceptance.requireRemotePairAuraOwnerVeto"))
                                ownerUuid = acceptanceRemotePairOwnerForNativeProof(
                                        host, ownerUuid);
                            if (ownerUuid != null) {
                                if (!permitsHitSelectAtNativeDispatch(
                                        host, minecraft, playerIndex,
                                        hitSelectInitialUuid))
                                    return;
                                if (vetoOriginalScreenBeforeNativeCombat(
                                        host, minecraft)) return;
                                auraDispatchUuid.set(ownerUuid);
                                try {
                                    minecraft.customMcAttackCrosshairEntity();
                                } finally {
                                    auraDispatchUuid.remove();
                                }
                            }
                        } else {
                            // Manual mouse and Auto Clicker preserve vanilla
                            // clickMouse semantics and their previous CI.
                            if (!permitsHitSelectAtNativeDispatch(
                                    host, minecraft, playerIndex,
                                    hitSelectInitialUuid))
                                return;
                            if (vetoOriginalScreenBeforeNativeCombat(
                                    host, minecraft)) return;
                            if (host.hasPendingAutoClickerControllerIntent()) {
                                // A native index may be reused after source
                                // scheduling or during synthetic slot changes.
                                // Spend exactly this target's original UUID.
                                final UUID currentSourceUuid = playerHit
                                        && playerIndex >= 0
                                        ? host.worldEntityUuidState()
                                                .snapshot().at(playerIndex)
                                        : null;
                                final Minecraft189AutoClickerControllerLease.Intent
                                        controllerIntent =
                                        host.consumeAutoClickerControllerIntent(
                                                currentSourceUuid);
                                if (controllerIntent == null) return;
                                autoClickerControllerIntent.set(controllerIntent);
                            } else if (host.featureCatalog().autoClicker().active()
                                    && host.featureCatalog().autoClicker()
                                            .triggerModeSetting().get()) {
                                // Trigger mode enabled mid-callback is not
                                // allowed to borrow an unscoped vanilla click.
                                return;
                            }
                            try {
                                // Keep precisely one original clickMouse
                                // call: physical/hold mode is unscoped;
                                // trigger mode carries an original UUID.
                                minecraft.customMcClickMouse();
                            } finally {
                                autoClickerControllerIntent.remove();
                            }
                        }
                    } finally {
                        Minecraft189VanillaMeleeAcceptance.syntheticAuraFinished();
                        Minecraft189VanillaMeleeAcceptance.syntheticTriggerFinished();
                        Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();
                    }
                    if (restoreSprint) {
                        host.restoreSprintAfterSyntheticClick();
                    }
                } finally {
                    // Never strand the player on a synthetic combat slot if
                    // vanilla clickMouse() throws during invocation.
                    host.restoreCombatSlotAfterSyntheticClick(originalSlot);
                }
                if (swordBlock != null && host.mayStartAutoBlock(
                        playerHit, playerIndex, originalSlot, alreadyUsingItem)
                        && host.claimSyntheticCombatReblock(combatCycleToken)) {
                    // Source-mapped rightClickMouse is the only way to begin
                    // vanilla sword use. Verify it actually started before
                    // tracking ownership (cooldown/no-item may refuse it).
                    final long useToken = host.beginAutoBlockNativeUse();
                    if (useToken != 0) {
                        try {
                            // A source held-item getter may open a vanilla
                            // menu AFTER melee. Never issue reblock into it.
                            if (vetoOriginalScreenBeforeNativeCombat(
                                    host, minecraft)) return;
                            minecraft.customMcRightClickMouse();
                            if (swordBlock.customMcIsUsingItem()) {
                                host.recordAutoBlockStart(useToken);
                            }
                        } finally {
                            // This exact uncommitted native use lease may
                            // otherwise survive a GUI veto, native exception,
                            // or original right-click refusing to use item.
                            // A completed block or newer reservation is not
                            // affected. Original mouse/item release stays
                            // exclusively in Minecraft and existing policy.
                            host.cancelAutoBlockNativeUse(useToken);
                        }
                    }
                }
            }
            } finally {
                // Every early return and throwing Mojang right-click or
                // controller callback returns this cycle to QUIESCENT.
                // Stale tokens can never close a newer runtime cycle.
                host.leaveSyntheticCombatCycle(combatCycleToken);
            }
        }
    }

    public static synchronized void publishTick(
            final long tickIndex) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.publishTick(tickIndex);
        }
    }

    /**
     * Optional bounded CI proof of a live LWJGL Display and repeated
     * Minecraft render callbacks. The property is absent in normal runs.
     * Never report a window or game loop from a bootstrap-only probe.
     */
    /**
     * Called only from the first instruction of vanilla ave.aw()V clickMouse.
     * This is observational, and only does work under an opt-in CI property.
     */
    public static synchronized void acceptanceVanillaClick(
            final Object minecraft) {
        Minecraft189VanillaMeleeAcceptance.onNativeClick(minecraft);
    }

    /**
     * Observation only: exact vanilla bda attackEntity(EntityPlayer, Entity)
     * reached after clickMouse's left-click cooldown/focus guard. This does
     * not generate an ATTACK packet or infer server-side damage.
     */
    /**
     * Opt-in M421 native controller dispatch veto, before any vanilla
     * outgoing attack side effects. Ordinary gameplay always returns true.
     */
    /**
     * M423 isolated opt-in negative fixture: replace one valid, genuine
     * unlocked Aura remote UUID intent with the actual *local player's*
     * source-native UUID. No new Entity/UUID/packet is fabricated.
     * The real Mojang attackEntity argument remains the remote player.
     * The production controller mismatch guard must reject it first.
     */
    private static UUID acceptanceUnlockedOwnerForNativeProof(
            final Minecraft189HostRuntime host, final UUID nativeRemoteUuid) {
        if (acceptanceUnlockedOwnerVetoReported) return nativeRemoteUuid;
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        if (!ids.available() || !kinds.available()
                || nativeRemoteUuid == null
                || ids.entityCount() != kinds.entityCount()
                || host.featureCatalog().killAura().lockTargetSetting().get())
            throw new IllegalStateException(
                    "M423 native unlocked Aura identity evidence unavailable");
        UUID local = null;
        for (int i = 0; i < ids.entityCount(); i++) {
            if (kinds.localPlayer(i)) {
                if (local != null)
                    throw new IllegalStateException(
                            "M423 ambiguous native local player");
                local = ids.at(i);
            }
        }
        if (local == null || local.equals(nativeRemoteUuid))
            throw new IllegalStateException(
                    "M423 distinct genuine native local UUID missing");
        acceptanceUnlockedRemoteUuid = nativeRemoteUuid;
        acceptanceUnlockedLocalUuid = local;
        return local;
    }

    /**
     * M424: source-native unlocked Aura is selecting remote A while a distinct
     * genuine remote B is still loaded. One opt-in negative scope substitutes
     * B's real UUID. The original controller's Entity stays A, unmodified.
     */
    private static UUID acceptanceRemotePairOwnerForNativeProof(
            final Minecraft189HostRuntime host, final UUID selected) {
        if (acceptanceRemotePairVetoReported) return selected;
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                host.worldEntityUuidState().snapshot();
        final Minecraft189WorldEntityKindState.Snapshot kinds =
                host.worldEntityKindState().snapshot();
        if (!ids.available() || !kinds.available()
                || ids.entityCount() != kinds.entityCount()
                || selected == null
                || host.featureCatalog().killAura().lockTargetSetting().get())
            throw new IllegalStateException(
                    "M424 unlocked two-remote native evidence unavailable");
        UUID other = null;
        boolean selectedPlayer = false;
        for (int i=0; i<ids.entityCount(); i++) {
            final UUID id = ids.at(i);
            if (!kinds.player(i) || kinds.localPlayer(i)) continue;
            if (selected.equals(id)) {
                selectedPlayer = true;
            } else {
                if (other != null)
                    throw new IllegalStateException(
                            "M424 ambiguous alternate remote player UUID");
                other = id;
            }
        }
        if (!selectedPlayer || other == null || other.equals(selected))
            throw new IllegalStateException(
                    "M424 two distinct source-native remote UUIDs unavailable");
        acceptanceRemotePairSelectedUuid = selected;
        acceptanceRemotePairOtherUuid = other;
        return other;
    }

    public static synchronized boolean acceptanceControllerDispatchAllowed(
            final Object controller, final Object player,
            final Object entity) {
        final UUID expected = auraDispatchUuid.get();
        final UUID expectedTrigger = triggerDispatchUuid.get();
        final Minecraft189AutoClickerControllerLease.Intent autoClickerIntent =
                autoClickerControllerIntent.get();
        if (autoClickerIntent != null) {
            final UUID actual =
                    entity instanceof Minecraft189NativeEntityUuidAccess
                    ? ((Minecraft189NativeEntityUuidAccess) entity)
                            .customMcNativeEntityUuid() : null;
            if (activeRuntime == null || !activeRuntime.hostInstalled()
                    || expected != null || expectedTrigger != null)
                return false;
            final Minecraft189HostRuntime host =
                    activeRuntime.requireHostRuntime();
            if (!autoClickerIntent.permits(actual,
                    host.featureCatalog().autoClicker().nativeOwnerEpoch(),
                    host.featureCatalog().autoClicker().active()
                            && host.featureCatalog().autoClicker()
                                    .triggerModeSetting().get().booleanValue(),
                    host.featureCatalog().killAura().active()
                            || host.featureCatalog().triggerBot().active(),
                    host.clickGuiRuntime().coreRuntime()
                            .model().snapshot().open()))
                return false;
        }
        if (expectedTrigger != null) {
            // Fail BEFORE vanilla ATTACK packet side effects if the current
            // original controller argument differs from the owned ray UUID.
            final UUID actual =
                    entity instanceof Minecraft189NativeEntityUuidAccess
                    ? ((Minecraft189NativeEntityUuidAccess) entity)
                            .customMcNativeEntityUuid() : null;
            if (!expectedTrigger.equals(actual) || expected != null
                    || activeRuntime == null || !activeRuntime.hostInstalled()
                    || !activeRuntime.requireHostRuntime().featureCatalog()
                            .triggerBot().active()
                    || activeRuntime.requireHostRuntime().featureCatalog()
                            .killAura().active())
                return false;
        }
        // A real vanilla mouse click must reach the native controller
        // WITHOUT the Kill Aura-only thread-local owner. Do not claim this
        // from the click callback alone: require the actual mapped entity.
        if (Boolean.getBoolean(
                "custommc.acceptance.requireManualUnscopedNativeController")) {
            if (expected != null
                    || activeRuntime == null || !activeRuntime.hostInstalled()
                    || activeRuntime.requireHostRuntime()
                            .featureCatalog().killAura().active()
                    || !(entity instanceof Minecraft189NativeEntityUuidAccess)
                    || ((Minecraft189NativeEntityUuidAccess) entity)
                            .customMcNativeEntityUuid() == null)
                throw new IllegalStateException(
                        "M425 native manual melee escaped unscoped controller evidence");
            if (!acceptanceManualUnscopedNativeReported) {
                acceptanceManualUnscopedNativeReported = true;
                System.out.println(
                        "CUSTOMMC_189_MANUAL_NATIVE_CONTROLLER_UNSCOPED_UUID_PASS=YES");
                System.out.flush();
            }
        }
        // The existing genuine Auto Clicker acceptance performs NO X11
        // attack presses. When its synthetic click reaches Mojang's native
        // controller, require an unscoped, source-mapped actual entity UUID.
        if (Boolean.getBoolean(
                "custommc.acceptance.requireAutoClickerUnscopedNativeController")) {
            if (expected != null
                    || activeRuntime == null || !activeRuntime.hostInstalled()
                    || !activeRuntime.requireHostRuntime()
                            .featureCatalog().autoClicker().active()
                    || activeRuntime.requireHostRuntime()
                            .featureCatalog().killAura().active()
                    || !(entity instanceof Minecraft189NativeEntityUuidAccess)
                    || ((Minecraft189NativeEntityUuidAccess) entity)
                            .customMcNativeEntityUuid() == null)
                throw new IllegalStateException(
                        "M426 native Auto Clicker escaped unscoped controller evidence");
            if (!acceptanceAutoClickerUnscopedNativeReported) {
                acceptanceAutoClickerUnscopedNativeReported = true;
                System.out.println(
                        "CUSTOMMC_189_AUTOCLICKER_NATIVE_CONTROLLER_UNSCOPED_UUID_PASS=YES");
                System.out.flush();
            }
        }
        if (expected != null) {
            // This is the real mapped outgoing method ARGUMENT, not a
            // reconstructed crosshair list index or rotation projection.
            final UUID actual =
                    entity instanceof Minecraft189NativeEntityUuidAccess
                    ? ((Minecraft189NativeEntityUuidAccess) entity)
                            .customMcNativeEntityUuid() : null;
            if (!expected.equals(actual)) {
                if (Boolean.getBoolean(
                        "custommc.acceptance.requireRemotePairAuraOwnerVeto")
                        && !acceptanceRemotePairVetoReported) {
                    if (actual == null
                            || !expected.equals(acceptanceRemotePairOtherUuid)
                            || !actual.equals(acceptanceRemotePairSelectedUuid))
                        throw new IllegalStateException(
                                "M424 unexpected real remote-pair mismatch"
                                + " expected=" + expected + " actual=" + actual);
                    acceptanceRemotePairVetoReported = true;
                    System.out.println(
                            "CUSTOMMC_189_UNLOCKED_TWO_REAL_REMOTE_UUID_MISMATCH_VETO_PASS=YES");
                    System.out.flush();
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.requireUnlockedAuraOwnerVeto")
                        && !acceptanceUnlockedOwnerVetoReported) {
                    if (!expected.equals(acceptanceUnlockedLocalUuid)
                            || actual == null
                            || !actual.equals(acceptanceUnlockedRemoteUuid))
                        throw new IllegalStateException(
                                "M423 unexpected native unlocked mismatch"
                                + " expected=" + expected + " actual=" + actual);
                    acceptanceUnlockedOwnerVetoReported = true;
                    System.out.println(
                            "CUSTOMMC_189_UNLOCKED_AURA_NATIVE_LOCAL_INTENT_REMOTE_ARGUMENT_VETO_PASS=YES");
                    System.out.flush();
                }
                if (Boolean.getBoolean(
                        "custommc.acceptance.requireScopedAuraOwnerVeto"))
                    Minecraft189RejoinTargetAcceptance
                            .verifyScopedOwnerVeto(expected, actual);
                return false; // BEFORE vanilla ATTACK packet side effects.
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireRemotePairAuraOwnerVeto")
                    && acceptanceRemotePairVetoReported
                    && !acceptanceRemotePairFreshReported) {
                if (actual == null
                        || !actual.equals(acceptanceRemotePairSelectedUuid)
                        || !expected.equals(acceptanceRemotePairSelectedUuid))
                    throw new IllegalStateException(
                            "M424 fresh unlocked remote A dispatch mismatch");
                acceptanceRemotePairFreshReported = true;
                System.out.println(
                        "CUSTOMMC_189_UNLOCKED_TWO_REAL_REMOTE_FRESH_A_DISPATCH_PASS=YES");
                System.out.flush();
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireUnlockedAuraOwnerVeto")
                    && acceptanceUnlockedOwnerVetoReported
                    && !acceptanceUnlockedOwnerFreshReported) {
                if (actual == null
                        || !actual.equals(acceptanceUnlockedRemoteUuid)
                        || !expected.equals(acceptanceUnlockedRemoteUuid))
                    throw new IllegalStateException(
                            "M423 fresh unlocked remote UUID mismatch");
                acceptanceUnlockedOwnerFreshReported = true;
                System.out.println(
                        "CUSTOMMC_189_UNLOCKED_AURA_FRESH_NATIVE_REMOTE_DISPATCH_PASS=YES");
                System.out.flush();
            }
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireScopedAuraOwnerVeto"))
                Minecraft189RejoinTargetAcceptance
                        .verifyScopedOwnerAccepted(expected, actual);
        }
        if (!Boolean.getBoolean(
                "custommc.acceptance.requireStaleDispatchVeto"))
            return true;
        if (activeRuntime == null || !activeRuntime.hostInstalled())
            throw new IllegalStateException(
                    "M421 controller missing genuine installed host");
        return Minecraft189RejoinTargetAcceptance.allowStaleDispatchAfterRealExit(
                activeRuntime.requireHostRuntime(), controller, player, entity);
    }

    public static synchronized void acceptanceControllerAttackEntity(
            final Object controller, final Object player,
            final Object entity) {
        // V320: the EXACT original vanilla controller's third (Entity)
        // argument before native attack packet effects. Opt-in for the
        // simultaneous localhost acceptance only, read-only provenance.
        // The existing controller veto and attack gates remain unchanged.
        if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraSimultaneous")) {
            Minecraft189SimultaneousTargetAcceptance
                    .observeOriginalControllerArgument(entity);
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraRangeVeto")) {
            throw new IllegalStateException(
                    "out-of-range player reached vanilla attackEntity");
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraWallVeto")) {
            throw new IllegalStateException(
                    "real occluded player reached native attackEntity");
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraWallRecovery")
                || Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraWallLifecycle")) {
            if (!acceptanceAuraWallRecoveryClickReported
                    || controller == null || player == null || entity == null) {
                throw new IllegalStateException(
                        "native attackEntity before verified wall removal and Aura attack");
            }
            if (!acceptanceAuraWallRecoveryControllerReported) {
                acceptanceAuraWallRecoveryControllerReported = true;
                System.out.println(
                        "CUSTOMMC_189_WALL_RECOVERY_NATIVE_CONTROLLER_ATTACK_PASS=YES");
                System.out.flush();
            }
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.reportKillAuraFovVeto")) {
            throw new IllegalStateException(
                    "out-of-FOV player reached vanilla attackEntity");
        }
        if (Boolean.getBoolean(
                "custommc.acceptance.requireRejoinedControllerB")
                && activeRuntime != null && activeRuntime.hostInstalled()) {
            Minecraft189RejoinTargetAcceptance.verifyRealControllerB(
                    activeRuntime.requireHostRuntime(), controller, player, entity);
        }
        Minecraft189VanillaMeleeAcceptance.onMojangControllerAttack(
                controller, player, entity);
    }

    /**
     * Records the genuine first integer operand read by vanilla clickMouse:
     * Mojang leftClickCounter, without changing its value or branch.
     */
    public static synchronized void acceptanceVanillaClickGuard(
            final int nativeLeftClickCounter) {
        Minecraft189VanillaMeleeAcceptance.onNativeClickGuard(
                nativeLeftClickCounter);
    }

    /**
     * Exact source-mapped original world pass, *after* Mojang camera setup.
     * No GL capture without explicit visual opt-in and owned render frame.
     */
    private static boolean acceptanceCameraHookReported;

    public static synchronized void worldCameraSetupCaptured(
            final int worldPass, final float partialTicks) {
        if (!acceptanceCameraHookReported
                && Boolean.getBoolean("custommc.acceptance.requireNativeWorldCamera")) {
            acceptanceCameraHookReported = true;
            System.out.println("CUSTOMMC_CAMERA_DIAG_HOOK=INVOKED pass="
                    + worldPass + " activeRuntime=" + (activeRuntime != null));
            System.out.flush();
        }
        if (activeRuntime != null) {
            activeRuntime.captureNativeWorldCamera(worldPass, partialTicks);
        }
    }

    /** Exact original EntityRenderer.mc.getRenderViewEntity() after camera setup. */
    public static synchronized void worldCameraViewEntityCaptured(
            final Object actualRenderViewEntity,
            final int worldPass, final float partialTicks) {
        if (activeRuntime != null) {
            activeRuntime.captureNativeWorldOrigin(
                    actualRenderViewEntity, worldPass, partialTicks);
        }
    }

    /**
     * Exact original EntityRenderer.mc.theWorld, after native world camera
     * and source-derived render-view origin have both been captured.
     */
    public static synchronized void worldCameraLoadedEntitiesCaptured(
            final Object actualWorld, final int pass, final float partialTicks) {
        if (activeRuntime != null)
            activeRuntime.captureNativePlayerBoxes(actualWorld, pass, partialTicks);
    }

    public static synchronized void renderFrameStarted(
            final float partialTicks) {
        if (activeRuntime != null) {
            activeRuntime.beginRenderFrame(
                    partialTicks);
            if (!graphicalAcceptanceReported
                    && Boolean.getBoolean(
                            "custommc.acceptance.reportGraphicalFrame")
                    && activeRuntime.hostInstalled()
                    && org.lwjgl.opengl.Display.isCreated()) {
                graphicalAcceptanceFrames++;
                if (graphicalAcceptanceFrames >= 3) {
                    graphicalAcceptanceReported = true;
                    System.out.println(
                            "CUSTOMMC_OFFICIAL_189_OPENGL_FRAME_PASS=YES");
                    System.out.flush();
                }
            }
        }
    }

    public static synchronized void renderHudFrame(
            final float partialTicks) {
        if (activeRuntime != null) {
            activeRuntime.renderHudFrame(
                    partialTicks);
        }
    }

    public static synchronized void renderPostProcessFrame(
            final float partialTicks) {
        if (activeRuntime != null) {
            activeRuntime.renderPostProcessFrame(
                    partialTicks);
        }
    }

    public static synchronized void renderWorld(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderWorld(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized void renderWorldOverlay(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderWorldOverlay(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized void renderHud(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderHud(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized void renderPostProcess(
            final long frameIndex,
            final float partialTicks) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.renderPostProcess(
                    frameIndex,
                    partialTicks);
        }
    }

    public static synchronized boolean pointerButton(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyButton,
            final boolean pressed) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host != null
                && host.pointerButton(
                        pixelX,
                        pixelYFromBottom,
                        legacyButton,
                        pressed);
    }

    public static synchronized boolean scroll(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyWheelDelta) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host != null
                && host.scroll(
                        pixelX,
                        pixelYFromBottom,
                        legacyWheelDelta);
    }

    public static synchronized boolean key(
            final int legacyKeyCode,
            final char character,
            final boolean pressed,
            final boolean repeat,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        final Minecraft189HostRuntime host =
                activeHost();
        return host != null
                && host.key(
                        legacyKeyCode,
                        character,
                        pressed,
                        repeat,
                        shift,
                        control,
                        alt);
    }

    private static Minecraft189BootstrapRuntime requireRuntime() {
        if (activeRuntime == null
                || activeRuntime.closed()) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 runtime bridge is not active");
        }
        return activeRuntime;
    }

    private static Minecraft189HostRuntime activeHost() {
        if (activeRuntime == null
                || !activeRuntime.hostInstalled()) {
            return null;
        }
        return activeRuntime.requireHostRuntime();
    }

    static final class Registration
            implements AutoCloseable {
        private final Minecraft189BootstrapRuntime owner;
        private boolean closed;

        private Registration(
                final Minecraft189BootstrapRuntime owner) {
            this.owner = owner;
        }

        @Override
        public void close() {
            synchronized (Minecraft189RuntimeBridge.class) {
                if (closed) {
                    return;
                }
                if (activeRuntime == owner) {
                    activeRuntime = null;
                    scaffoldOriginalPlacementReadback = null;
                    scaffoldOriginalPlacementReadbackEpoch = 0L;
                    scaffoldOriginalPlacementActivation = 0L;
                    scaffoldOriginalSourceEpoch.invalidate();
                    scaffoldOriginalActivationSource.reset();
                    scaffoldOriginalActivationLease = 0L;
                    scaffoldAcceptanceEnableIssued = false;
                    scaffoldAcceptancePendingDiagTicks = 0;
                    scaffoldMenuPrearmHeartbeatTicks = 0;
                    scaffoldRepeatTick = 0L;
                    scaffoldRepeatProofStarted = false;
                    scaffoldRepeatPositiveAttempts = 0;
                    scaffoldRepeatPositiveReadbacks = 0;
                    scaffoldProductionReceiptReported = 0;
                    scaffoldProductionCompletionReported = false;
                    scaffoldMenuNegativeClearStability.reset();
                }
                closed = true;
            }
        }

        synchronized boolean closed() {
            return closed;
        }
    }
}
