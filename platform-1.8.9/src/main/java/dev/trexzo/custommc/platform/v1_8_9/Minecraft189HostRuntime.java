package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiThemeProvider;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

public final class Minecraft189HostRuntime
        implements AutoCloseable {
    private final Minecraft189Platform platform;
    private final Minecraft189ClickGuiRuntime clickGuiRuntime;
    private final Minecraft189ClickGuiToggleController clickGuiToggleController;
    private final ServiceRegistry.Registration clickGuiToggleRegistration;
    private final Minecraft189InputState inputState;
    private final Minecraft189FrameRateTracker frameRateTracker;
    private final Minecraft189ClickRateTracker clickRateTracker;
    private final Minecraft189PlayerPositionState playerPositionState;
    private final Minecraft189PlayerRotationState playerRotationState;
    private final Minecraft189PlayerDimensionState playerDimensionState;
    private final Minecraft189PlayerMovementState playerMovementState;
    private final Minecraft189PlayerHealthState playerHealthState;
    private final Minecraft189PlayerHurtTimeState playerHurtTimeState;
    private final Minecraft189PlayerArmorState playerArmorState;
    private final Minecraft189PlayerHungerState playerHungerState;
    private final Minecraft189PlayerPotionEffectsState playerPotionEffectsState;
    private final Minecraft189PlayerExperienceState playerExperienceState;
    private final Minecraft189PlayerPingState playerPingState;
    private final Minecraft189HotbarSlotState hotbarSlotState;
    private final Minecraft189WorldTimeState worldTimeState;
    private final Minecraft189WorldWeatherState worldWeatherState;
    private final Minecraft189WorldEntityPositionState worldEntityPositionState;
    private final Minecraft189WorldEntityKindState worldEntityKindState;
    private final Minecraft189WorldEntityCombatState worldEntityCombatState =
            new Minecraft189WorldEntityCombatState();
    private final Minecraft189CriticalsEvidence criticalsEvidence =
            new Minecraft189CriticalsEvidence();
    // Per-host-tick reference only; cleared before every new position sample.
    private Minecraft189PlayerSprintControl tickSprintControl;
    private Minecraft189InventoryHotbarControl tickHotbarControl;
    private final Minecraft189NearestPlayerTargetState nearestPlayerTargetState;
    private final Minecraft189TargetRotationState targetRotationState;
    // Range-only Aim Assist targeting does not replace general nearest-player state.
    private final Minecraft189NearestPlayerTargetState aimAssistRangeTargetState =
            new Minecraft189NearestPlayerTargetState();
    private final Minecraft189TargetRotationState aimAssistRangeRotationState =
            new Minecraft189TargetRotationState();
    private final Minecraft189TargetRotationState aimAssistCandidateRotationState =
            new Minecraft189TargetRotationState();
    private final Minecraft189ServerAddressState serverAddressState;
    private final Minecraft189HeldItemState heldItemState;
    private final Minecraft189MovementSpeedTracker movementSpeedTracker;
    private final Minecraft189FeatureCatalog featureCatalog;
    private final Minecraft189Hooks renderHooks;
    private final Minecraft189HostInputBridge inputBridge;
    private boolean closed;

    private Minecraft189HostRuntime(
            final Minecraft189Platform platform,
            final Minecraft189ClickGuiRuntime clickGuiRuntime,
            final Minecraft189ClickGuiToggleController clickGuiToggleController,
            final ServiceRegistry.Registration clickGuiToggleRegistration,
            final Minecraft189InputState inputState,
            final Minecraft189FrameRateTracker frameRateTracker,
            final Minecraft189ClickRateTracker clickRateTracker,
            final Minecraft189PlayerPositionState playerPositionState,
            final Minecraft189PlayerRotationState playerRotationState,
            final Minecraft189PlayerDimensionState playerDimensionState,
            final Minecraft189PlayerMovementState playerMovementState,
            final Minecraft189PlayerHealthState playerHealthState,
            final Minecraft189PlayerHurtTimeState playerHurtTimeState,
            final Minecraft189PlayerArmorState playerArmorState,
            final Minecraft189PlayerHungerState playerHungerState,
            final Minecraft189PlayerPotionEffectsState playerPotionEffectsState,
            final Minecraft189PlayerExperienceState playerExperienceState,
            final Minecraft189PlayerPingState playerPingState,
            final Minecraft189HotbarSlotState hotbarSlotState,
            final Minecraft189WorldTimeState worldTimeState,
            final Minecraft189WorldWeatherState worldWeatherState,
            final Minecraft189WorldEntityPositionState worldEntityPositionState,
            final Minecraft189WorldEntityKindState worldEntityKindState,
            final Minecraft189NearestPlayerTargetState nearestPlayerTargetState,
            final Minecraft189TargetRotationState targetRotationState,
            final Minecraft189ServerAddressState serverAddressState,
            final Minecraft189HeldItemState heldItemState,
            final Minecraft189MovementSpeedTracker movementSpeedTracker,
            final Minecraft189FeatureCatalog featureCatalog,
            final Minecraft189Hooks renderHooks,
            final Minecraft189HostInputBridge inputBridge) {
        this.platform = platform;
        this.clickGuiRuntime = clickGuiRuntime;
        this.clickGuiToggleController = clickGuiToggleController;
        this.clickGuiToggleRegistration = clickGuiToggleRegistration;
        this.inputState = inputState;
        this.frameRateTracker = frameRateTracker;
        this.clickRateTracker = clickRateTracker;
        this.playerPositionState = playerPositionState;
        this.playerRotationState = playerRotationState;
        this.playerDimensionState = playerDimensionState;
        this.playerMovementState = playerMovementState;
        this.playerHealthState = playerHealthState;
        this.playerHurtTimeState = playerHurtTimeState;
        this.playerArmorState = playerArmorState;
        this.playerHungerState = playerHungerState;
        this.playerPotionEffectsState = playerPotionEffectsState;
        this.playerExperienceState = playerExperienceState;
        this.playerPingState = playerPingState;
        this.hotbarSlotState = hotbarSlotState;
        this.worldTimeState = worldTimeState;
        this.worldWeatherState = worldWeatherState;
        this.worldEntityPositionState = worldEntityPositionState;
        this.worldEntityKindState = worldEntityKindState;
        this.nearestPlayerTargetState = nearestPlayerTargetState;
        this.targetRotationState = targetRotationState;
        this.serverAddressState = serverAddressState;
        this.heldItemState = heldItemState;
        this.movementSpeedTracker = movementSpeedTracker;
        this.featureCatalog = featureCatalog;
        this.renderHooks = renderHooks;
        this.inputBridge = inputBridge;
    }

    public static Minecraft189HostRuntime install(
            final Minecraft189Platform platform,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final ModuleKeybindAssignments moduleKeybindAssignments,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final LegacyUiHostCallbacks hostCallbacks) {
        return install(
                platform,
                modulePresentations,
                moduleCategories,
                moduleSettings,
                moduleKeybinds,
                moduleKeybindAssignments,
                settings,
                settingPresentations,
                UiThemes::darkDefault,
                hostCallbacks);
    }

    public static Minecraft189HostRuntime install(
            final Minecraft189Platform platform,
            final ModulePresentationRegistry modulePresentations,
            final ModuleCategoryRegistry moduleCategories,
            final ModuleSettingRegistry moduleSettings,
            final ModuleKeybindRegistry moduleKeybinds,
            final ModuleKeybindAssignments moduleKeybindAssignments,
            final SettingRegistry settings,
            final SettingPresentationRegistry settingPresentations,
            final UiThemeProvider themeProvider,
            final LegacyUiHostCallbacks hostCallbacks) {
        Objects.requireNonNull(
                platform,
                "platform");
        Objects.requireNonNull(
                hostCallbacks,
                "hostCallbacks");

        final Minecraft189ClickGuiRuntime clickGuiRuntime =
                Minecraft189ClickGuiRuntime.install(
                        platform,
                        modulePresentations,
                        moduleCategories,
                        moduleSettings,
                        moduleKeybinds,
                        moduleKeybindAssignments,
                        settings,
                        settingPresentations,
                        themeProvider,
                        hostCallbacks);

        ServiceRegistry.Registration toggleRegistration = null;
        Minecraft189FeatureCatalog featureCatalog = null;
        final Minecraft189InputState inputState =
                new Minecraft189InputState();
        final Minecraft189FrameRateTracker frameRateTracker =
                new Minecraft189FrameRateTracker();
        final Minecraft189ClickRateTracker clickRateTracker =
                new Minecraft189ClickRateTracker();
        final Minecraft189PlayerPositionState playerPositionState =
                new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState playerRotationState =
                new Minecraft189PlayerRotationState();
        final Minecraft189PlayerDimensionState playerDimensionState =
                new Minecraft189PlayerDimensionState();
        final Minecraft189PlayerMovementState playerMovementState =
                new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHealthState playerHealthState =
                new Minecraft189PlayerHealthState();
        final Minecraft189PlayerHurtTimeState playerHurtTimeState =
                new Minecraft189PlayerHurtTimeState();
        final Minecraft189PlayerArmorState playerArmorState =
                new Minecraft189PlayerArmorState();
        final Minecraft189PlayerHungerState playerHungerState =
                new Minecraft189PlayerHungerState();
        final Minecraft189PlayerPotionEffectsState playerPotionEffectsState =
                new Minecraft189PlayerPotionEffectsState();
        final Minecraft189PlayerExperienceState playerExperienceState =
                new Minecraft189PlayerExperienceState();
        final Minecraft189PlayerPingState playerPingState =
                new Minecraft189PlayerPingState();
        final Minecraft189HotbarSlotState hotbarSlotState =
                new Minecraft189HotbarSlotState();
        final Minecraft189WorldTimeState worldTimeState =
                new Minecraft189WorldTimeState();
        final Minecraft189WorldWeatherState worldWeatherState =
                new Minecraft189WorldWeatherState();
        final Minecraft189WorldEntityPositionState worldEntityPositionState =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState worldEntityKindState =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearestPlayerTargetState =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState targetRotationState =
                new Minecraft189TargetRotationState();
        final Minecraft189ServerAddressState serverAddressState =
                new Minecraft189ServerAddressState();
        final Minecraft189HeldItemState heldItemState =
                new Minecraft189HeldItemState();
        final Minecraft189MovementSpeedTracker movementSpeedTracker =
                new Minecraft189MovementSpeedTracker();
        try {
            final ServiceRegistry services =
                    platform.requireContext()
                            .services();
            final Minecraft189ClickGuiToggleController toggleController =
                    new Minecraft189ClickGuiToggleController(
                            services.require(
                                    ClickGuiModel.class));
            toggleRegistration =
                    services.registerManaged(
                            Minecraft189ClickGuiToggleController.class,
                            toggleController);

            featureCatalog =
                    Minecraft189FeatureCatalog.install(
                            platform.requireContext()
                                    .modules(),
                            platform.requireContext()
                                    .moduleController(),
                            modulePresentations,
                            moduleCategories,
                            moduleSettings,
                            settings,
                            settingPresentations,
                            inputState,
                            frameRateTracker,
                            clickRateTracker,
                            playerPositionState,
                            playerRotationState,
                            playerDimensionState,
                            playerMovementState,
                            playerHealthState,
                            playerHurtTimeState,
                            playerArmorState,
                            playerHungerState,
                            playerPotionEffectsState,
                            playerExperienceState,
                            playerPingState,
                            hotbarSlotState,
                            worldTimeState,
                            worldWeatherState,
                            serverAddressState,
                            heldItemState,
                            movementSpeedTracker,
                            nearestPlayerTargetState,
                            targetRotationState,
                            worldEntityPositionState,
                            worldEntityKindState,
                            services.require(
                                    RenderPipeline.class),
                            hostCallbacks);

            return new Minecraft189HostRuntime(
                    platform,
                    clickGuiRuntime,
                    toggleController,
                    toggleRegistration,
                    inputState,
                    frameRateTracker,
                    clickRateTracker,
                    playerPositionState,
                    playerRotationState,
                    playerDimensionState,
                    playerMovementState,
                    playerHealthState,
                    playerHurtTimeState,
                    playerArmorState,
                    playerHungerState,
                    playerPotionEffectsState,
                    playerExperienceState,
                    playerPingState,
                    hotbarSlotState,
                    worldTimeState,
                    worldWeatherState,
                    worldEntityPositionState,
                    worldEntityKindState,
                    nearestPlayerTargetState,
                    targetRotationState,
                    serverAddressState,
                    heldItemState,
                    movementSpeedTracker,
                    featureCatalog,
                    new Minecraft189Hooks(platform),
                    new Minecraft189HostInputBridge(
                            platform,
                            hostCallbacks));
        } catch (RuntimeException failure) {
            if (featureCatalog != null) {
                try {
                    featureCatalog.close();
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(
                            cleanupFailure);
                }
            }
            if (toggleRegistration != null) {
                toggleRegistration.close();
            }
            clickGuiRuntime.close();
            throw failure;
        }
    }

    public Minecraft189ClickGuiRuntime clickGuiRuntime() {
        requireOpen();
        return clickGuiRuntime;
    }

    public Minecraft189ClickGuiToggleController clickGuiToggleController() {
        requireOpen();
        return clickGuiToggleController;
    }

    public Minecraft189FeatureCatalog featureCatalog() {
        requireOpen();
        return featureCatalog;
    }

    public Minecraft189InputState inputState() {
        requireOpen();
        return inputState;
    }

    public Minecraft189FrameRateTracker frameRateTracker() {
        requireOpen();
        return frameRateTracker;
    }

    public Minecraft189ClickRateTracker clickRateTracker() {
        requireOpen();
        return clickRateTracker;
    }

    public Minecraft189PlayerPositionState playerPositionState() {
        requireOpen();
        return playerPositionState;
    }

    public Minecraft189PlayerRotationState playerRotationState() {
        requireOpen();
        return playerRotationState;
    }

    public Minecraft189PlayerDimensionState playerDimensionState() {
        requireOpen();
        return playerDimensionState;
    }

    public Minecraft189PlayerMovementState playerMovementState() {
        requireOpen();
        return playerMovementState;
    }

    public Minecraft189PlayerHealthState playerHealthState() {
        requireOpen();
        return playerHealthState;
    }

    public Minecraft189PlayerHurtTimeState playerHurtTimeState() {
        requireOpen();
        return playerHurtTimeState;
    }

    public Minecraft189PlayerArmorState playerArmorState() {
        requireOpen();
        return playerArmorState;
    }

    public Minecraft189PlayerHungerState playerHungerState() {
        requireOpen();
        return playerHungerState;
    }

    public Minecraft189PlayerPotionEffectsState playerPotionEffectsState() {
        requireOpen();
        return playerPotionEffectsState;
    }

    public Minecraft189PlayerExperienceState playerExperienceState() {
        requireOpen();
        return playerExperienceState;
    }

    public Minecraft189PlayerPingState playerPingState() {
        requireOpen();
        return playerPingState;
    }

    public Minecraft189HotbarSlotState hotbarSlotState() {
        requireOpen();
        return hotbarSlotState;
    }

    public Minecraft189WorldTimeState worldTimeState() {
        requireOpen();
        return worldTimeState;
    }

    public Minecraft189WorldWeatherState worldWeatherState() {
        requireOpen();
        return worldWeatherState;
    }

    public Minecraft189WorldEntityPositionState worldEntityPositionState() {
        requireOpen();
        return worldEntityPositionState;
    }

    public Minecraft189WorldEntityKindState worldEntityKindState() {
        requireOpen();
        return worldEntityKindState;
    }

    public Minecraft189WorldEntityCombatState worldEntityCombatState() {
        requireOpen();
        return worldEntityCombatState;
    }

    public Minecraft189NearestPlayerTargetState nearestPlayerTargetState() {
        requireOpen();
        return nearestPlayerTargetState;
    }

    public Minecraft189TargetRotationState targetRotationState() {
        requireOpen();
        return targetRotationState;
    }

    public Minecraft189ServerAddressState serverAddressState() {
        requireOpen();
        return serverAddressState;
    }

    public Minecraft189HeldItemState heldItemState() {
        requireOpen();
        return heldItemState;
    }

    public Minecraft189MovementSpeedTracker movementSpeedTracker() {
        requireOpen();
        return movementSpeedTracker;
    }

    void playerPosition(
            final Minecraft189PlayerPositionAccess player) {
        requireOpen();
        // Clear both halves on every host tick. No stale fall/motion
        // evidence can authorize an automatic attack after world changes.
        criticalsEvidence.reset();
        tickSprintControl = null;
        tickHotbarControl = null;
        nearestPlayerTargetState.clear();
        if (player == null) {
            targetRotationState.clear();
            playerPositionState.clear();
            movementSpeedTracker.clear();
            return;
        }
        final double x =
                player.customMcPositionX();
        final double y =
                player.customMcPositionY();
        final double z =
                player.customMcPositionZ();
        playerPositionState.update(
                x,
                y,
                z);
        movementSpeedTracker.sample(
                x,
                z);
    }

    void playerRotation(
            final Minecraft189PlayerRotationAccess player) {
        requireOpen();
        if (player == null) {
            playerRotationState.clear();
            aimAssistRangeTargetState.clear();
            aimAssistRangeRotationState.clear();
            aimAssistCandidateRotationState.clear();
            featureCatalog.spin()
                    .apply(
                            null,
                            null,
                            false);
            featureCatalog.jitter()
                    .apply(null, null, false);
            featureCatalog.killAura().suspend();
            return;
        }

        playerRotationState.update(
                player.customMcRotationYaw(),
                player.customMcRotationPitch());
        final Minecraft189PlayerRotationControl control =
                player instanceof Minecraft189PlayerRotationControl
                        ? (Minecraft189PlayerRotationControl) player
                        : null;
        final Minecraft189PlayerRotationState.Snapshot rotation =
                playerRotationState.snapshot();

        // Refresh ground authority from the exact player being rotated,
        // not a movement snapshot published later in the host tick.
        final Minecraft189PlayerMovementState.Snapshot rotationMovement;
        if (player instanceof Minecraft189PlayerMovementStateAccess) {
            final Minecraft189PlayerMovementStateAccess movementPlayer =
                    (Minecraft189PlayerMovementStateAccess) player;
            playerMovementState.update(
                    movementPlayer.customMcOnGround(),
                    movementPlayer.customMcSneaking(),
                    movementPlayer.customMcSprinting());
            rotationMovement = playerMovementState.snapshot();
        } else {
            rotationMovement = null;
        }

        final boolean leftButtonHeld =
                inputState.pointerPressed(
                        Minecraft189ClickRateTracker.LEFT_BUTTON);
        // The GUI owns pointer/keyboard focus: rotation automation must
        // release ownership and discard any pending correction cadence.
        if (clickGuiRuntime.coreRuntime().model().snapshot().open()) {
            featureCatalog.spin().apply(null, null, false);
            featureCatalog.aimAssist().apply(null, null, null, false, false);
            featureCatalog.jitter().apply(null, null, false);
            featureCatalog.killAura().suspend();
            return;
        }
        if (featureCatalog.spin()
                .apply(
                        control,
                        rotation,
                        leftButtonHeld,
                        playerMovementState.snapshot())) {
            featureCatalog.jitter()
                    .apply(
                            null,
                            null,
                            false);
            return;
        }
        // Aura exclusively owns the rotation lane when enabled.
        // Spin retains its established, explicit precedence.
        final Minecraft189KillAuraModule aura = featureCatalog.killAura();
        if (aura.active()) {
            aura.aim(control, rotation, targetRotationState.snapshot(),
                    leftButtonHeld, playerMovementState.snapshot());
            featureCatalog.jitter().apply(null, null, false);
            return;
        }
        final Minecraft189AimAssistModule assist =
                featureCatalog.aimAssist();
        Minecraft189TargetRotationState.Snapshot assistTarget =
                targetRotationState.snapshot();
        final boolean limitedFov =
                (assist.yawEnabledSetting().get().booleanValue()
                        && assist.maxFovSetting().get().doubleValue() < 180.0D)
                || (assist.pitchEnabledSetting().get().booleanValue()
                        && assist.maxPitchFovSetting().get().doubleValue() < 180.0D);
        final boolean crosshairPriority =
                assist.prioritizeCrosshairSetting().get().booleanValue();
        if (assist.active()
                && (assist.minDistanceSetting().get().doubleValue() > 0.0D
                        || limitedFov || crosshairPriority)) {
            final Minecraft189PlayerPositionState.Snapshot local =
                    playerPositionState.snapshot();
            final Minecraft189NearestPlayerTargetState.CandidateFilter filter;
            if (limitedFov) {
                filter = candidate -> {
                    aimAssistCandidateRotationState.update(local, candidate);
                    return assist.targetWithinFov(
                            rotation,
                            aimAssistCandidateRotationState.snapshot());
                };
            } else {
                filter = null;
            }
            final Minecraft189NearestPlayerTargetState.CandidateScore score;
            if (crosshairPriority) {
                score = candidate -> {
                    aimAssistCandidateRotationState.update(local, candidate);
                    return assist.targetAngularErrorSquared(
                            rotation,
                            aimAssistCandidateRotationState.snapshot());
                };
            } else {
                score = null;
            }
            aimAssistRangeTargetState.update(
                    local,
                    worldEntityPositionState.snapshot(),
                    worldEntityKindState.snapshot(),
                    assist.minDistanceSetting().get().doubleValue(),
                    assist.maxDistanceSetting().get().doubleValue(),
                    filter,
                    score);
            aimAssistRangeRotationState.update(
                    local,
                    aimAssistRangeTargetState.snapshot());
            assistTarget = aimAssistRangeRotationState.snapshot();
        }
        if (assist.apply(
                        control,
                        rotation,
                        assistTarget,
                        leftButtonHeld,
                        inputState.keyPressed(
                                LegacyKeyboardCodes.W),
                        rotationMovement)) {
            featureCatalog.jitter()
                    .apply(
                            null,
                            null,
                            false);
            return;
        }
        featureCatalog.jitter()
                .apply(
                        control,
                        rotation,
                        leftButtonHeld,
                        playerMovementState.snapshot(),
                        Boolean.valueOf(inputState.pointerPressed(
                                Minecraft189ClickRateTracker.RIGHT_BUTTON)));
    }

    void playerDimension(
            final Minecraft189PlayerDimensionAccess player) {
        requireOpen();
        if (player == null) {
            playerDimensionState.clear();
            return;
        }
        playerDimensionState.update(
                player.customMcDimension());
    }

    void playerMovementState(
            final Minecraft189PlayerMovementStateAccess player) {
        requireOpen();
        if (player == null) {
            playerMovementState.clear();
            return;
        }
        playerMovementState.update(
                player.customMcOnGround(),
                player.customMcSneaking(),
                player.customMcSprinting());
    }

    void playerSprintControl(
            final Minecraft189PlayerSprintControl player) {
        requireOpen();
        tickSprintControl = player;
        if (player == null) {
            featureCatalog.wTap()
                    .apply(
                            null,
                            null,
                            false,
                            false);
            return;
        }
        final Minecraft189PlayerMovementState.Snapshot movement =
                playerMovementState.snapshot();
        final boolean attackHeld = inputState.pointerPressed(
                Minecraft189ClickRateTracker.LEFT_BUTTON);
        if (clickGuiRuntime.coreRuntime().model().snapshot().open()) {
            featureCatalog.wTap().suspendForGui(attackHeld);
        } else if (featureCatalog.wTap().apply(
                player, movement, attackHeld,
                inputState.keyPressed(LegacyKeyboardCodes.W),
                nearestPlayerTargetState.snapshot())) {
            return;
        }
        featureCatalog.autoSprint()
                .apply(
                        player,
                        movement,
                        inputState.keyPressed(LegacyKeyboardCodes.W),
                        inputState.keyPressed(LegacyKeyboardCodes.W)
                                || inputState.keyPressed(LegacyKeyboardCodes.A)
                                || inputState.keyPressed(LegacyKeyboardCodes.S)
                                || inputState.keyPressed(LegacyKeyboardCodes.D));
    }

    void playerSneakControl(
            final Minecraft189PlayerSneakControl player) {
        requireOpen();
        if (player == null) {
            return;
        }
        featureCatalog.autoSneak()
                .apply(
                        player,
                        playerMovementState.snapshot(),
                        inputState.keyPressed(LegacyKeyboardCodes.W),
                        inputState.keyPressed(LegacyKeyboardCodes.W)
                                || inputState.keyPressed(LegacyKeyboardCodes.A)
                                || inputState.keyPressed(LegacyKeyboardCodes.S)
                                || inputState.keyPressed(LegacyKeyboardCodes.D));
    }

    float adjustNoSlowMovement(
            final float slowedValue) {
        requireOpen();
        return featureCatalog.noSlow()
                .adjustSlowedMovement(
                        slowedValue,
                        playerMovementState.snapshot());
    }

    double adjustVelocityHorizontal(
            final double before,
            final double after) {
        requireOpen();
        return featureCatalog.velocity()
                .adjustHorizontal(
                        before,
                        after,
                        playerMovementState.snapshot());
    }

    double adjustVelocityVertical(
            final double before,
            final double after) {
        requireOpen();
        return featureCatalog.velocity()
                .adjustVertical(
                        before,
                        after,
                        playerMovementState.snapshot());
    }

    void playerJumpControl(
            final Minecraft189PlayerJumpControl player) {
        requireOpen();
        if (player == null) {
            featureCatalog.jumpReset().apply(null, null, null, false, true);
            return;
        }
        final Minecraft189PlayerMovementState.Snapshot movement =
                playerMovementState.snapshot();
        final boolean longJumpActive =
                featureCatalog.longJump()
                        .active();
        final boolean highJumpActive =
                featureCatalog.highJump()
                        .active();
        final boolean lowHopActive =
                featureCatalog.lowHop()
                        .active();
        final boolean bunnyHopActive =
                featureCatalog.bunnyHop()
                        .active();
        final boolean movementJumpSuspended =
                featureCatalog.freeze()
                        .active()
                        || featureCatalog.flight()
                                .active();
        // Combat Jump Reset consumes only a fresh mapped local hurt edge.
        // Never execute a second synthetic jump in the same callback.
        // Existing movement jump modules retain priority while active.
        if (featureCatalog.jumpReset().apply(
                player, movement, playerHurtTimeState.snapshot(),
                inputState.keyPressed(LegacyKeyboardCodes.W),
                movementJumpSuspended || longJumpActive || highJumpActive
                        || lowHopActive || bunnyHopActive
                        || featureCatalog.airJump().active()
                        || clickGuiRuntime.coreRuntime().model().snapshot().open())) {
            return;
        }
        featureCatalog.longJump()
                .applyJump(
                        player,
                        movement,
                        movementJumpSuspended);
        featureCatalog.highJump()
                .applyJump(
                        player,
                        movement,
                        movementJumpSuspended
                                || longJumpActive);
        featureCatalog.lowHop()
                .applyJump(
                        player,
                        movement,
                        movementJumpSuspended
                                || longJumpActive
                                || highJumpActive);
        featureCatalog.bunnyHop()
                .applyJump(
                        player,
                        movement,
                        movementJumpSuspended
                                || longJumpActive
                                || highJumpActive
                                || lowHopActive);
        if (!longJumpActive
                && !highJumpActive
                && !lowHopActive
                && !bunnyHopActive) {
            featureCatalog.autoJump()
                    .apply(
                            player,
                            movement,
                            inputState.keyPressed(LegacyKeyboardCodes.W),
                            inputState.keyPressed(LegacyKeyboardCodes.W)
                                    || inputState.keyPressed(LegacyKeyboardCodes.A)
                                    || inputState.keyPressed(LegacyKeyboardCodes.S)
                                    || inputState.keyPressed(LegacyKeyboardCodes.D));
        }
        featureCatalog.airJump()
                .apply(
                        player,
                        movement);
    }

    void playerStepControl(
            final Minecraft189PlayerStepControl player) {
        requireOpen();
        featureCatalog.step()
                .apply(
                        player,
                        playerMovementState.snapshot());
    }

    void playerFallDistanceControl(
            final Minecraft189PlayerFallDistanceControl player) {
        requireOpen();
        featureCatalog.noFall()
                .apply(player, playerMovementState.snapshot());
        if (player != null) {
            criticalsEvidence.fall(player.customMcFallDistance());
        }
    }

    void playerWebControl(
            final Minecraft189PlayerWebControl player) {
        requireOpen();
        featureCatalog.noWeb()
                .apply(
                        player,
                        playerMovementState.snapshot());
    }

    void playerNoClipControl(
            final Minecraft189PlayerNoClipControl player) {
        requireOpen();
        featureCatalog.noClip()
                .apply(
                        player,
                        playerMovementState.snapshot());
    }

    void playerMotionControl(
            final Minecraft189PlayerMotionControl player) {
        requireOpen();
        final Minecraft189PlayerHurtTimeState.Snapshot hurtTime =
                playerHurtTimeState.snapshot();
        final boolean freezeActive =
                featureCatalog.freeze()
                        .active();
        featureCatalog.freeze()
                .apply(
                        player);
        if (freezeActive) {
            featureCatalog.damageBoost()
                    .apply(player, hurtTime, true);
            return;
        }

        final Minecraft189PlayerRotationState.Snapshot rotation =
                playerRotationState.snapshot();
        final Minecraft189PlayerMovementState.Snapshot movement =
                playerMovementState.snapshot();
        final boolean flightActive =
                featureCatalog.flight()
                        .active();
        final boolean fastFallActive =
                featureCatalog.fastFall()
                        .active();
        final boolean jumpHeld =
                inputState.keyPressed(LegacyKeyboardCodes.SPACE);
        final boolean noGravityActive =
                featureCatalog.noGravity()
                        .ownsVertical(jumpHeld);
        final boolean longJumpActive =
                featureCatalog.longJump()
                        .active();
        final boolean highJumpActive =
                featureCatalog.highJump()
                        .active();
        final boolean lowHopActive =
                featureCatalog.lowHop()
                        .active();
        final boolean longJumpOwnsHorizontal =
                featureCatalog.longJump()
                        .applyMotion(
                                player,
                                rotation,
                                flightActive);
        featureCatalog.highJump()
                .applyMotion(
                        player,
                        flightActive
                                || longJumpActive);
        featureCatalog.lowHop()
                .applyMotion(
                        player,
                        flightActive
                                || longJumpActive
                                || highJumpActive);
        final boolean bunnyHopOwnsHorizontal =
                featureCatalog.bunnyHop()
                        .applyMotion(
                                player,
                                rotation,
                                flightActive
                                        || longJumpActive
                                        || highJumpActive
                                        || lowHopActive);
        final boolean movementSpeedOwnsHorizontal =
                featureCatalog.movementSpeed()
                        .apply(
                                player,
                                movement,
                                rotation,
                                flightActive
                                        || longJumpOwnsHorizontal
                                        || bunnyHopOwnsHorizontal);
        final boolean airSpeedOwnsHorizontal =
                featureCatalog.airSpeed()
                        .apply(
                                player,
                                movement,
                                rotation,
                                flightActive
                                        || longJumpOwnsHorizontal
                                        || bunnyHopOwnsHorizontal);
        featureCatalog.flight()
                .apply(
                        player,
                        rotation,
                        movement);
        final boolean horizontalOwnerActive = flightActive
                || longJumpOwnsHorizontal || bunnyHopOwnsHorizontal
                || movementSpeedOwnsHorizontal || airSpeedOwnsHorizontal;
        final boolean targetStrafeOwnsHorizontal = featureCatalog.targetStrafe()
                .apply(player, playerPositionState.snapshot(),
                        nearestPlayerTargetState.snapshot(), movement,
                        inputState.keyPressed(LegacyKeyboardCodes.W),
                        horizontalOwnerActive);
        featureCatalog.strafe()
                .apply(player, rotation,
                        horizontalOwnerActive || targetStrafeOwnsHorizontal,
                        movement);
        final boolean reverseStepOwnsVertical =
                featureCatalog.reverseStep()
                        .apply(
                                player,
                                movement,
                                flightActive
                                        || noGravityActive,
                                inputState.keyPressed(LegacyKeyboardCodes.W)
                                        || inputState.keyPressed(LegacyKeyboardCodes.A)
                                        || inputState.keyPressed(LegacyKeyboardCodes.S)
                                        || inputState.keyPressed(LegacyKeyboardCodes.D));
        featureCatalog.noGravity()
                .apply(
                        player,
                        movement,
                        flightActive,
                        jumpHeld);
        featureCatalog.fastFall()
                .apply(
                        player,
                        movement,
                        flightActive
                                || noGravityActive
                                || reverseStepOwnsVertical);
        featureCatalog.glide()
                .apply(
                        player,
                        movement,
                        flightActive
                                || noGravityActive
                                || reverseStepOwnsVertical
                                || fastFallActive);
        featureCatalog.damageBoost()
                .apply(player, hurtTime, flightActive);
        if (player != null) {
            // Read the final mapped motion after movement policies apply.
            criticalsEvidence.motion(player.customMcMotionY());
        }
    }

    void playerHealth(
            final Minecraft189PlayerHealthAccess player) {
        requireOpen();
        if (player == null) {
            playerHealthState.clear();
            return;
        }
        playerHealthState.update(
                player.customMcHealth(),
                player.customMcMaxHealth());
    }

    void playerHurtTime(
            final Minecraft189PlayerHurtTimeAccess player) {
        requireOpen();
        if (player == null) {
            playerHurtTimeState.clear();
            return;
        }
        playerHurtTimeState.update(
                player.customMcHurtTime());
    }

    void playerArmor(
            final Minecraft189PlayerArmorAccess player) {
        requireOpen();
        if (player == null) {
            playerArmorState.clear();
            return;
        }
        playerArmorState.update(
                player.customMcArmorBoots(),
                player.customMcArmorLeggings(),
                player.customMcArmorChestplate(),
                player.customMcArmorHelmet(),
                player.customMcArmorBootsItem(),
                player.customMcArmorLeggingsItem(),
                player.customMcArmorChestplateItem(),
                player.customMcArmorHelmetItem());
    }

    void playerHunger(
            final Minecraft189PlayerHungerAccess player) {
        requireOpen();
        if (player == null) {
            playerHungerState.clear();
            return;
        }
        playerHungerState.update(
                player.customMcFoodLevel(),
                player.customMcSaturationLevel());
    }

    void playerPotionEffects(
            final Minecraft189PlayerPotionEffectsAccess player) {
        requireOpen();
        if (player == null) {
            playerPotionEffectsState.clear();
            return;
        }
        playerPotionEffectsState.update(
                player.customMcPotionEffects());
    }

    void playerExperience(
            final Minecraft189PlayerExperienceAccess player) {
        requireOpen();
        if (player == null) {
            playerExperienceState.clear();
            return;
        }
        playerExperienceState.update(
                player.customMcExperienceLevel(),
                player.customMcExperienceTotal(),
                player.customMcExperienceProgress(),
                player.customMcExperienceBarCap());
    }

    void playerPing(
            final Minecraft189PlayerPingAccess player) {
        requireOpen();
        if (player == null) {
            playerPingState.clear();
            return;
        }
        final int milliseconds =
                player.customMcPingMilliseconds();
        if (milliseconds < 0) {
            playerPingState.clear();
            return;
        }
        playerPingState.update(
                milliseconds);
    }

    void playerHotbarSlot(
            final Minecraft189PlayerInventoryAccess player) {
        requireOpen();
        tickHotbarControl = null;
        if (player == null) {
            hotbarSlotState.clear();
            return;
        }
        final Minecraft189InventoryHotbarAccess inventory =
                player.customMcInventory();
        if (inventory instanceof Minecraft189InventoryHotbarControl) {
            tickHotbarControl = (Minecraft189InventoryHotbarControl) inventory;
        }
        if (inventory == null) {
            hotbarSlotState.clear();
            return;
        }
        final int slot =
                inventory.customMcSelectedHotbarSlot();
        if (slot < 0
                || slot >= Minecraft189HotbarSlotState.SLOT_COUNT) {
            hotbarSlotState.clear();
            return;
        }
        hotbarSlotState.update(
                slot);
    }

    void worldTime(
            final Minecraft189WorldTimeAccess world) {
        requireOpen();
        if (world == null) {
            worldTimeState.clear();
            return;
        }
        worldTimeState.update(
                world.customMcWorldTime());
    }

    void worldWeather(
            final Minecraft189WorldWeatherAccess world) {
        requireOpen();
        if (world == null) {
            worldWeatherState.clear();
            return;
        }
        worldWeatherState.update(
                world.customMcRaining(),
                world.customMcThundering());
    }

    void worldEntityPositions(
            final Minecraft189WorldEntityPositionsAccess world) {
        requireOpen();
        // Begin a new entity-list snapshot; never pair old combat evidence
        // with positions that may have reordered across world ticks.
        worldEntityCombatState.clear();
        nearestPlayerTargetState.clear();
        targetRotationState.clear();
        if (world == null) {
            worldEntityPositionState.clear();
            return;
        }
        final double[] packedPositions =
                world.customMcLoadedEntityPositions();
        if (packedPositions == null) {
            worldEntityPositionState.clear();
            return;
        }
        worldEntityPositionState.update(
                packedPositions);
    }

    void worldEntityKinds(
            final Minecraft189WorldEntityKindsAccess world) {
        requireOpen();
        if (world == null) {
            worldEntityKindState.clear();
            nearestPlayerTargetState.clear();
            targetRotationState.clear();
            return;
        }
        final int[] kinds =
                world.customMcLoadedEntityKinds();
        if (kinds == null) {
            worldEntityKindState.clear();
            nearestPlayerTargetState.clear();
            targetRotationState.clear();
            return;
        }
        worldEntityKindState.update(
                kinds);
        nearestPlayerTargetState.update(
                playerPositionState.snapshot(),
                worldEntityPositionState.snapshot(),
                worldEntityKindState.snapshot());
        targetRotationState.update(
                playerPositionState.snapshot(),
                nearestPlayerTargetState.snapshot());
    }

    void worldEntityCombat(
            final Minecraft189WorldEntityCombatAccess world) {
        requireOpen();
        if (world == null || !worldEntityPositionState.snapshot().available()
                || !worldEntityKindState.snapshot().available()) {
            worldEntityCombatState.clear();
            return;
        }
        final int[] data = world.customMcLoadedEntityCombatStates();
        if (data == null || data.length != worldEntityPositionState.snapshot().entityCount()
                || data.length != worldEntityKindState.snapshot().entityCount()) {
            worldEntityCombatState.clear();
            return;
        }
        try {
            worldEntityCombatState.update(data);
        } catch (IllegalArgumentException malformed) {
            worldEntityCombatState.clear();
            return;
        }
        // Aura should rotate toward the nearest verified *living* player,
        // rather than repeatedly selecting a nearer dead player. This does
        // not change the normal nearest-target semantics for other modules.
        if (featureCatalog.killAura().active()) {
            Minecraft189KillAuraTargetSelector.select(
                    featureCatalog.killAura(),
                    playerPositionState.snapshot(),
                    playerRotationState.snapshot(),
                    worldEntityPositionState.snapshot(),
                    worldEntityKindState.snapshot(),
                    worldEntityCombatState.snapshot(),
                    featureCatalog.antiBot(),
                    nearestPlayerTargetState);
            targetRotationState.update(
                    playerPositionState.snapshot(),
                    nearestPlayerTargetState.snapshot());
        }
    }

    void serverAddress(
            final Minecraft189ServerDataAccess serverData) {
        requireOpen();
        if (serverData == null) {
            serverAddressState.clear();
            return;
        }
        final String address =
                serverData.customMcServerAddress();
        if (address == null
                || address.trim().isEmpty()) {
            serverAddressState.clear();
            return;
        }
        serverAddressState.update(
                address);
    }

    void playerHeldItem(
            final Minecraft189PlayerHeldItemAccess player) {
        requireOpen();
        if (player == null) {
            heldItemState.clear();
            return;
        }
        final Minecraft189ItemStackAccess held =
                player.customMcHeldItem();
        if (held == null) {
            heldItemState.clear();
            return;
        }
        heldItemState.update(
                held.customMcDisplayName(),
                held.customMcStackSize(),
                held.customMcItemDamage(),
                held.customMcMaxDamage());
    }

    void frameStarted(
            final long frameIndex,
            final float partialTicks) {
        requireOpen();
        frameRateTracker.frameStarted();
    }

    int rightClickDelay(
            final int currentDelay) {
        requireOpen();
        return featureCatalog.fastPlace()
                .apply(
                        currentDelay,
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.RIGHT_BUTTON),
                        playerMovementState.snapshot());
    }

    void playerControllerBreakControl(
            final Minecraft189BlockHitDelayControl controller) {
        requireOpen();
        featureCatalog.fastBreak()
                .apply(
                        controller,
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.LEFT_BUTTON),
                        playerMovementState.snapshot());
    }

    void playerControllerMiningControl(
            final Minecraft189BlockMiningControl controller) {
        requireOpen();
        featureCatalog.speedMine()
                .apply(
                        controller,
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.LEFT_BUTTON),
                        playerMovementState.snapshot());
    }

    void timerSpeedControl(
            final Minecraft189TimerSpeedControl timer) {
        requireOpen();
        featureCatalog.timerSpeed()
                .apply(
                        timer,
                        playerMovementState.snapshot());
    }

    int leftClickCounter(
            final int currentCounter) {
        requireOpen();
        if (clickGuiRuntime.coreRuntime().model().snapshot().open()) {
            return currentCounter;
        }
        return featureCatalog.noHitDelay()
                .apply(
                        currentCounter,
                        playerMovementState.snapshot(),
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.LEFT_BUTTON));
    }

    /** Eligibility captured immediately before the native synthetic click. */
    int selectCombatSlotBeforeSyntheticClick(final boolean playerHit) {
        requireOpen();
        return featureCatalog.combatSlot().select(tickHotbarControl, playerHit,
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void restoreCombatSlotAfterSyntheticClick(final int originalSlot) {
        requireOpen();
        featureCatalog.combatSlot().restore(tickHotbarControl, originalSlot);
    }

    boolean shouldKeepSprintAfterSyntheticClick(final boolean playerHit) {
        requireOpen();
        final Minecraft189PlayerSprintControl current = tickSprintControl;
        if (!(current instanceof Minecraft189PlayerMovementStateAccess)) {
            return false;
        }
        final Minecraft189PlayerMovementStateAccess state =
                (Minecraft189PlayerMovementStateAccess) current;
        final Minecraft189PlayerMovementState measured =
                new Minecraft189PlayerMovementState();
        measured.update(state.customMcOnGround(), state.customMcSneaking(),
                state.customMcSprinting());
        return featureCatalog.keepSprint().shouldRestore(
                measured.snapshot(), state.customMcSprinting(),
                inputState.keyPressed(LegacyKeyboardCodes.W), playerHit,
                featureCatalog.wTap().active(),
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void restoreSprintAfterSyntheticClick() {
        requireOpen();
        // Called synchronously after the mapped vanilla click. Preserve
        // WTap's higher-priority sprint-reset behavior if it is active.
        if (tickSprintControl != null && featureCatalog.keepSprint().active()
                && !featureCatalog.wTap().active()
                && !clickGuiRuntime.coreRuntime().model().snapshot().open()) {
            tickSprintControl.customMcSetSprinting(true);
        }
    }

    boolean shouldAutoClick() {
        return shouldAutoClick(false, -1);
    }

    boolean shouldAutoClick(final boolean crosshairPlayerHit) {
        return shouldAutoClick(crosshairPlayerHit, -1);
    }

    boolean shouldAutoClick(final boolean crosshairPlayerHit,
            final int crosshairPlayerIndex) {
        requireOpen();
        // No automatic attack may cross the native ClickGUI focus boundary.
        // The same rule applies to hold and trigger modes; pending phase
        // is cancelled so closing the GUI cannot replay banked clicks.
        if (clickGuiRuntime.coreRuntime().model().snapshot().open()) {
            featureCatalog.autoClicker().suspendForGui();
            featureCatalog.triggerBot().suspend();
            featureCatalog.killAura().suspend();
            return false;
        }
        final boolean confirmedHit = crosshairPlayerHit;
        // Exactly one Combat module can own an automatic attack callback.
        // Dedicated Trigger Bot has precedence; legacy Auto Clicker trigger
        // mode remains for backward-compatible persisted profiles.
        final Minecraft189TriggerBotModule trigger = featureCatalog.triggerBot();
        final Minecraft189KillAuraModule aura = featureCatalog.killAura();
        final boolean attackHeld = inputState.pointerPressed(
                Minecraft189ClickRateTracker.LEFT_BUTTON);
        final boolean rightHeld = inputState.pointerPressed(
                Minecraft189ClickRateTracker.RIGHT_BUTTON);
        final boolean click;
        if (aura.active()) {
            trigger.suspend();
            featureCatalog.autoClicker().suspendForGui();
            click = aura.shouldClick(
                    confirmedHit, crosshairPlayerIndex,
                    playerRotationState.snapshot(),
                    targetRotationState.snapshot(), attackHeld, rightHeld,
                    playerMovementState.snapshot(), featureCatalog.spin().active());
        } else if (trigger.active()) {
            aura.suspend();
            featureCatalog.autoClicker().suspendForGui();
            click = trigger.shouldClick(
                    confirmedHit, attackHeld, rightHeld,
                    playerMovementState.snapshot());
        } else {
            aura.suspend();
            trigger.suspend();
            click = featureCatalog.autoClicker().shouldClick(
                    attackHeld, inputState.keyPressed(LegacyKeyboardCodes.W),
                    rightHeld, nearestPlayerTargetState.snapshot(),
                    playerMovementState.snapshot(), confirmedHit);
        }
        // One shared final veto applies to every automatic click owner.
        // It never alters genuine vanilla mouse presses. A rejected click
        // consumes scheduler credit rather than banking a late burst.
        if (!click) return false;
        final Minecraft189WorldEntityCombatState.Snapshot combat =
                worldEntityCombatState.snapshot();
        if (aura.active()) {
            final Minecraft189TargetRotationState.Snapshot selected =
                    targetRotationState.snapshot();
            if (!selected.available()
                    || !combat.alive(selected.entityIndex())) {
                return false;
            }
        }
        // A verified tab-list miss is a veto for every synthetic click
        // owner. A real manually-triggered vanilla click remains untouched.
        if (!featureCatalog.antiBot().permits(crosshairPlayerIndex, combat)) {
            return false;
        }
        if (!featureCatalog.hitSelect().permits(
                crosshairPlayerIndex, combat)) {
            return false;
        }
        if (!featureCatalog.criticals().permits(
                playerMovementState.snapshot(), criticalsEvidence.snapshot(),
                featureCatalog.flight().active()
                        || featureCatalog.freeze().active()
                        || featureCatalog.noFall().active()
                        || featureCatalog.noGravity().active()
                        || featureCatalog.noClip().active())) {
            return false;
        }
        clickRateTracker.recordPress(
                Minecraft189ClickRateTracker.LEFT_BUTTON);
        return true;
    }

    public void publishTick(
            final long tickIndex) {
        requireOpen();
        renderHooks.publishTick(tickIndex);
    }

    public void renderWorld(
            final long frameIndex,
            final float partialTicks) {
        requireOpen();
        renderHooks.renderWorld(
                frameIndex,
                partialTicks);
    }

    public void renderWorldOverlay(
            final long frameIndex,
            final float partialTicks) {
        requireOpen();
        renderHooks.renderWorldOverlay(
                frameIndex,
                partialTicks);
    }

    public void renderHud(
            final long frameIndex,
            final float partialTicks) {
        requireOpen();
        renderHooks.renderHud(
                frameIndex,
                partialTicks);
    }

    public void renderPostProcess(
            final long frameIndex,
            final float partialTicks) {
        requireOpen();
        renderHooks.renderPostProcess(
                frameIndex,
                partialTicks);
    }

    public boolean pointerButton(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyButton,
            final boolean pressed) {
        requireOpen();
        final boolean wasPressed =
                inputState.pointerPressed(
                        legacyButton);
        inputState.pointerButton(
                legacyButton,
                pressed);
        if (pressed
                && !wasPressed
                && (legacyButton
                == Minecraft189ClickRateTracker.LEFT_BUTTON
                || legacyButton
                == Minecraft189ClickRateTracker.RIGHT_BUTTON)) {
            clickRateTracker.recordPress(
                    legacyButton);
        }
        return inputBridge.pointerButton(
                pixelX,
                pixelYFromBottom,
                legacyButton,
                pressed);
    }

    public boolean scroll(
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyWheelDelta) {
        requireOpen();
        return inputBridge.scroll(
                pixelX,
                pixelYFromBottom,
                legacyWheelDelta);
    }

    public boolean key(
            final int legacyKeyCode,
            final char character,
            final boolean pressed,
            final boolean repeat,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        requireOpen();
        inputState.key(
                legacyKeyCode,
                pressed);
        return inputBridge.key(
                legacyKeyCode,
                character,
                pressed,
                repeat,
                shift,
                control,
                alt);
    }

    public synchronized boolean closed() {
        return closed;
    }

    public Minecraft189Platform platform() {
        return platform;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        RuntimeException failure = null;
        inputState.clear();
        frameRateTracker.clear();
        clickRateTracker.clear();
        playerPositionState.clear();
        playerRotationState.clear();
        playerDimensionState.clear();
        playerMovementState.clear();
        playerHealthState.clear();
        playerHurtTimeState.clear();
        playerArmorState.clear();
        playerHungerState.clear();
        playerPotionEffectsState.clear();
        playerExperienceState.clear();
        playerPingState.clear();
        hotbarSlotState.clear();
        worldTimeState.clear();
        worldWeatherState.clear();
        worldEntityPositionState.clear();
        criticalsEvidence.reset();
        tickSprintControl = null;
        tickHotbarControl = null;
        worldEntityKindState.clear();
        worldEntityCombatState.clear();
        nearestPlayerTargetState.clear();
        targetRotationState.clear();
        serverAddressState.clear();
        heldItemState.clear();
        movementSpeedTracker.clear();
        try {
            featureCatalog.close();
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }
        try {
            clickGuiToggleRegistration.close();
        } catch (RuntimeException closeFailure) {
            if (failure == null) {
                failure = closeFailure;
            } else {
                failure.addSuppressed(
                        closeFailure);
            }
        }
        try {
            clickGuiRuntime.close();
        } catch (RuntimeException closeFailure) {
            if (failure == null) {
                failure = closeFailure;
            } else {
                failure.addSuppressed(
                        closeFailure);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private synchronized void requireOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 host runtime is closed");
        }
    }
}
