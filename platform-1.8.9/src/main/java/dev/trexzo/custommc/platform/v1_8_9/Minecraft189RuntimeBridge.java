package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Minecraft189RuntimeBridge {
    private static Minecraft189BootstrapRuntime activeRuntime;
    private static int graphicalAcceptanceFrames;
    private static boolean graphicalAcceptanceReported;
    private static boolean acceptancePlayerObserved;
    private static boolean acceptanceWorldCombatObserved;
    private static int acceptanceWorldTicks;
    private static boolean acceptanceWorldReported;
    private static String acceptanceLastScreen;
    private static int acceptanceDisabledTargetTicks;
    private static boolean acceptanceAutoClickerEnabled;

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
        graphicalAcceptanceFrames = 0;
        graphicalAcceptanceReported = false;
        acceptancePlayerObserved = false;
        acceptanceWorldCombatObserved = false;
        acceptanceWorldTicks = 0;
        acceptanceWorldReported = false;
        acceptanceLastScreen = null;
        Minecraft189VanillaMeleeAcceptance.reset();
        acceptanceDisabledTargetTicks = 0;
        acceptanceAutoClickerEnabled = false;
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
                    try {
                        minecraft.customMcClickMouse();
                    } finally {
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
