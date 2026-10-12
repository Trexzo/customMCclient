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
    private static int graphicalAcceptanceFrames;
    private static boolean graphicalAcceptanceReported;
    private static boolean acceptancePlayerObserved;
    private static boolean acceptanceWorldCombatObserved;
    private static int acceptanceWorldTicks;
    private static boolean acceptanceWorldReported;
    private static String acceptanceLastScreen;
    private static int acceptanceDisabledTargetTicks;
    private static boolean acceptanceAutoClickerEnabled;
    private static Object killAuraNonPlayerTarget;
    private static int killAuraDisabledTargetTicks;
    private static int killAuraEnabledVetoTicks;
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
    private static boolean acceptanceAuraWallRecoveryVisibleReported;
    private static boolean acceptanceAuraWallRecoveryClickReported;
    private static boolean acceptanceAuraWallRecoveryControllerReported;
    private static int acceptanceAuraFovTargetIndex = -1;
    private static int acceptanceAuraFovDisabledTicks;
    private static int acceptanceAuraFovEnabledTicks;
    private static boolean acceptanceAuraFovEnabled;
    private static boolean acceptanceAuraFovReported;

    private Minecraft189RuntimeBridge() {
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
        acceptanceDisabledTargetTicks = 0;
        acceptanceAutoClickerEnabled = false;
        killAuraNonPlayerTarget = null;
        killAuraDisabledTargetTicks = 0;
        killAuraEnabledVetoTicks = 0;
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
        acceptanceAuraWallRecoveryVisibleReported = false;
        acceptanceAuraWallRecoveryClickReported = false;
        acceptanceAuraWallRecoveryControllerReported = false;
        acceptanceAuraFovTargetIndex = -1;
        acceptanceAuraFovDisabledTicks = 0;
        acceptanceAuraFovEnabledTicks = 0;
        acceptanceAuraFovEnabled = false;
        acceptanceAuraFovReported = false;
        return new Registration(next);
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

    public static synchronized void gameTick() {
        if (activeRuntime != null) {
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
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.playerPosition(player);
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

    public static synchronized void worldEntityPositions(
            final Minecraft189WorldEntityPositionsAccess world) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.worldEntityPositions(world);
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
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null) {
            host.timerSpeedControl(
                    timer);
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
        return host == null ? vanilla
                : host.featureCatalog().reach().raycastBlockDistance(vanilla);
    }

    public static synchronized boolean raycastExtendedBranch(final boolean vanilla) {
        final Minecraft189HostRuntime host = activeHost();
        return host == null ? vanilla
                : host.featureCatalog().reach().raycastExtendedBranch(vanilla);
    }

    public static synchronized double raycastExtendedDistance() {
        final Minecraft189HostRuntime host = activeHost();
        return host == null ? 6.0D
                : host.featureCatalog().reach().raycastExtendedDistance(6.0D);
    }

    /**
     * Player-only local ray-hitbox inflation. Receives the native border
     * unchanged whenever Hitbox is disabled or the candidate is not a player.
     */
    public static synchronized float raycastHitboxBorder(
            final boolean playerCandidate, final float nativeBorder) {
        final Minecraft189HostRuntime host = activeHost();
        return host == null ? nativeBorder
                : host.featureCatalog().hitbox()
                        .adjustNativeBorder(playerCandidate, nativeBorder);
    }

    public static synchronized void autoClick(
            final Minecraft189ClickMouseControl minecraft) {
        final Minecraft189HostRuntime host =
                activeHost();
        if (host != null && minecraft != null) {
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
                            && Math.abs(yawError) >= 120.0D;
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
                // nonlocal player, alive and consistently 3.8..4.8 blocks from
                // CIHost. With default Kill Aura range 3.5 this player must
                // NEVER become an automated attack target.
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
                                || !Double.isFinite(squared)
                                || squared < 14.44D || squared > 23.04D) {
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
                            + " qualifyingIndex=" + farIndex);
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
                        if (swordBlock != null && host.releaseAutoBlockBeforeAction()
                                && swordBlock.customMcIsUsingItem())
                            swordBlock.customMcStopUsingItem();
                        minecraft.customMcRightClickMouse();
                        host.commitAutoPotUseAttempt();
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
                        if (swordBlock != null && host.releaseAutoBlockBeforeAction()
                                && swordBlock.customMcIsUsingItem())
                            swordBlock.customMcStopUsingItem();
                        minecraft.customMcRightClickMouse();
                        host.commitAutoRodUseAttempt();
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
            if (host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)) {
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
                        final boolean auraOwnedPlayerAttack =
                                playerHit && playerIndex >= 0
                                && host.featureCatalog().killAura().active()
                                && host.targetRotationState().snapshot().available()
                                && host.targetRotationState().snapshot()
                                        .entityIndex() == playerIndex;
                        if (auraOwnedPlayerAttack) {
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
                            minecraft.customMcClickMouse();
                        }
                    } finally {
                        Minecraft189VanillaMeleeAcceptance.syntheticAuraFinished();
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
                        playerHit, playerIndex, originalSlot, alreadyUsingItem)) {
                    // Source-mapped rightClickMouse is the only way to begin
                    // vanilla sword use. Verify it actually started before
                    // tracking ownership (cooldown/no-item may refuse it).
                    minecraft.customMcRightClickMouse();
                    if (swordBlock.customMcIsUsingItem())
                        host.recordAutoBlockStart();
                }
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
                }
                closed = true;
            }
        }

        synchronized boolean closed() {
            return closed;
        }
    }
}
