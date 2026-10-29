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
    private final Minecraft189WorldEntityUuidState worldEntityUuidState =
            new Minecraft189WorldEntityUuidState();
    private final Minecraft189WorldEntityVisibilityState worldEntityVisibilityState =
            new Minecraft189WorldEntityVisibilityState();
    private Minecraft189WorldEntityVisibilityAccess tickVisibilityWorld;
    // Exact loaded-world object used for UUID parity, never a stored entity index.
    private Minecraft189WorldEntityUuidAccess tickUuidWorld;
    // The genuine source UUID that earned a synthetic TriggerBot click
    // must remain the SAME UUID at actual PlayerControllerMP dispatch.
    private final Minecraft189TriggerDispatchUuidLease triggerDispatchLease =
            new Minecraft189TriggerDispatchUuidLease();
    // One UUID-specific vanilla clickMouse may be admitted into the actual
    // PlayerControllerMP argument only by its original scheduler owner.
    private final Minecraft189AutoClickerControllerLease
            autoClickerControllerLease =
            new Minecraft189AutoClickerControllerLease();
    private long triggerDispatchAdmissionSerial;
    private final Minecraft189CriticalsEvidence criticalsEvidence =
            new Minecraft189CriticalsEvidence();
    // Only one exact native player, module policy and scheduler callback
    // may authorize a final original-game critical attack.
    private long criticalsSourceGeneration;
    private long criticalsNativeAdmissionSerial = -1L;
    private long criticalsNativeSourceGeneration;
    private long criticalsNativeLifecycleEpoch;
    private double criticalsNativeMinimumFall;
    private boolean criticalsNativeRequireDescending;
    private boolean criticalsNativeActive;
    private Minecraft189PlayerInventoryAccess criticalsNativePlayer;
    // One original player owns the whole motion host chain, across
    // independently guarded Movement module callbacks.
    private final Minecraft189MotionHostSourceLease motionHostLease =
            new Minecraft189MotionHostSourceLease();
    // A native EntityPlayer.jump() callback can recursively dispatch
    // another jump MODULE while the first source jump is in flight.
    private final Minecraft189JumpHostSourceLease jumpHostLease =
            new Minecraft189JumpHostSourceLease();
    // Genuine EntityPlayerSP ground, sneak, sprint are three separate
    // native getters. Never publish a mixed or stale player snapshot.
    private final Minecraft189MovementSnapshotSourceLease movementSnapshotLease =
            new Minecraft189MovementSnapshotSourceLease();
    // Health and maximum health are two original-player native getters.
    // Do not publish values combined across source replacement/reentry.
    private final Minecraft189HealthHostSourceLease healthHostLease =
            new Minecraft189HealthHostSourceLease();
    // Four native armor flags and four original armor ItemStack references
    // must belong to ONE source player before any HUD state is published.
    private final Minecraft189ArmorHostSourceLease armorHostLease =
            new Minecraft189ArmorHostSourceLease();
    // The mapped array getter plus each real potion ID/duration/amplifier
    // and name callback must share one exact original local-player owner.
    private final Minecraft189PotionEffectsHostSourceLease potionEffectsHostLease =
            new Minecraft189PotionEffectsHostSourceLease();
    // Ping and hotbar callbacks may reenter with a different native player.
    // The local inventory control cannot outlive a revoked original owner.
    private final Minecraft189PingHostSourceLease pingHostLease =
            new Minecraft189PingHostSourceLease();
    private final Minecraft189HotbarHostSourceLease hotbarHostLease =
            new Minecraft189HotbarHostSourceLease();
    // Original World time and the two original weather getters can
    // synchronously reenter with another World or a null unload source.
    private final Minecraft189WorldTimeHostSourceLease worldTimeHostLease =
            new Minecraft189WorldTimeHostSourceLease();
    private final Minecraft189WorldWeatherHostSourceLease worldWeatherHostLease =
            new Minecraft189WorldWeatherHostSourceLease();
    // All five original held-item getter calls belong to one local player.
    // Keep the downstream item-use owner null until all return successfully.
    private final Minecraft189HeldItemHostSourceLease heldItemHostLease =
            new Minecraft189HeldItemHostSourceLease();
    // One original ServerData source for the mapped multiplayer address.
    // Reentrant logout/server-switch must not publish the old address.
    private final Minecraft189ServerAddressHostSourceLease serverAddressHostLease =
            new Minecraft189ServerAddressHostSourceLease();
    // Hurt time authorizes local JumpReset and DamageBoost. A native
    // getter that changes player may not publish old damage evidence.
    private final Minecraft189HurtTimeHostSourceLease hurtTimeHostLease =
            new Minecraft189HurtTimeHostSourceLease();
    // World/dimension transitions must never publish a prior player's
    // dimension after synchronous native getter source replacement.
    private final Minecraft189DimensionHostSourceLease dimensionHostLease =
            new Minecraft189DimensionHostSourceLease();
    // NoFall and Criticals must share the exact original player's
    // mapped fallDistance getter/setter native callback stack.
    private final Minecraft189FallHostSourceLease fallHostLease =
            new Minecraft189FallHostSourceLease();
    // Separate original FoodStats and experience getters may synchronously
    // reenter a different local player during the same host tick.
    private final Minecraft189HungerHostSourceLease hungerHostLease =
            new Minecraft189HungerHostSourceLease();
    private final Minecraft189ExperienceHostSourceLease experienceHostLease =
            new Minecraft189ExperienceHostSourceLease();
    // WTap and AutoSprint are separate native setter owners. Their host
    // routing must share one original sprint source and UI focus lease.
    private final Minecraft189SprintHostSourceLease sprintHostLease =
            new Minecraft189SprintHostSourceLease();
    // The WTap and AutoSprint modules must not write opposite sprint
    // values from one old native movement snapshot.
    private final Minecraft189SprintWriteArbiter sprintWriteArbiter =
            new Minecraft189SprintWriteArbiter();
    // Three genuine EntityPlayerSP XYZ getter calls must belong to
    // ONE original local player before publishing position or speed.
    private final Minecraft189PositionHostSourceLease positionHostLease =
            new Minecraft189PositionHostSourceLease();
    // One original local-player rotation source across mapped yaw/pitch
    // reads and all cross-module original yaw/pitch setters.
    private final Minecraft189RotationHostSourceLease rotationHostLease =
            new Minecraft189RotationHostSourceLease();
    // Within the already-validated original player callback, exactly one
    // rotation module may make native yaw/pitch setter attempts.
    private final Minecraft189RotationAxisArbiter rotationAxisArbiter =
            new Minecraft189RotationAxisArbiter();
    // Original loadedEntityList visibility, UUID and XYZ snapshots
    // must not borrow a different world during a native getter.
    private final Minecraft189WorldEntityHostSourceLease worldEntityHostLease =
            new Minecraft189WorldEntityHostSourceLease();
    // World kind/combat getters must belong to the SAME original world
    // that supplied the current loadedEntityList XYZ snapshot.
    private Object tickOriginalEntityWorld;
    private final Minecraft189WorldEntityDownstreamSourceLease worldEntityDownstreamLease =
            new Minecraft189WorldEntityDownstreamSourceLease();
    // Per-host-tick reference only; cleared before every new position sample.
    private Minecraft189PlayerSprintControl tickSprintControl;
    // An original synthetic click may restore Sprint only for its exact
    // source player and the GUI policy that admitted that same click.
    private Minecraft189PlayerSprintControl pendingKeepSprintSource;
    private boolean pendingKeepSprintPlayerHit;
    private boolean pendingKeepSprintForward;
    private boolean pendingKeepSprintRequireHit;
    private boolean pendingKeepSprintPauseSneak;
    private long pendingKeepSprintLifecycleEpoch;
    private long pendingKeepSprintAdmissionSerial;
    private final Minecraft189KeepSprintRestoreLease keepSprintRestoreLease =
            new Minecraft189KeepSprintRestoreLease();
    // Admission serial forbids a nested source callback from minting or
    // resurrecting an older approval in the same original click stack.
    private long keepSprintAdmissionSerial;
    private Minecraft189InventoryHotbarControl tickHotbarControl;
    private Minecraft189PlayerInventoryAccess tickHotbarPlayer;
    private boolean autoWeaponOwnedClick;
    // Unlike tickHotbarControl, this is the inventory that actually acquired
    // the in-flight melee click lease. A reentrant original playerHotbarSlot
    // callback or respawn must not redirect its synchronous finally.
    private Minecraft189InventoryHotbarControl combatSlotLeaseInventory;
    private Minecraft189PlayerInventoryAccess combatSlotLeasePlayer;
    private int combatSlotLeaseOriginal = -1;
    private int combatSlotLeaseSelected = -1;
    // One original InventoryPlayer.currentItem may have only one temporary
    // combat owner. Reserve even during native getters/setters: these may
    // reenter the host before a successful selected-slot lease is published.
    private boolean combatHotbarSelecting;
    private Minecraft189PlayerHeldItemAccess tickHeldItemAccess;
    // A new original player-position tick, held-item callback or shutdown
    // changes this generation even if an A-B-A native getter restores the
    // very same player reference before its caller resumes.
    private long heldItemOwnerSerial;
    // These leases outlive changes to per-tick player references. A
    // synchronous native callback may replace the local player or
    // inventory while another original item-use action owns its slot.
    private Minecraft189InventoryHotbarControl autoRodLeaseInventory;
    private int autoRodLeaseOriginal = -1;
    private Minecraft189InventoryHotbarControl autoPotLeaseInventory;
    private int autoPotLeaseOriginal = -1;
    // V147: an item ranked from genuine source HP must not be used after
    // damage/healing or player replacement changes that ranking. Lease
    // owns the exact original EntityPlayer and its pre-selection HP witness.
    private Minecraft189PlayerHealthAccess autoPotHealthLeaseOwner;
    private float autoPotHealthLeaseHealth = Float.NaN;
    private float autoPotHealthLeaseMaximum = Float.NaN;
    private boolean autoPotHealthLeaseConserving;
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
    private final Minecraft189CombatCycleArbiter combatCycle =
            new Minecraft189CombatCycleArbiter();
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

    /** Read-only native loaded entity UUID evidence for official acceptance. */
    public Minecraft189WorldEntityUuidState worldEntityUuidState() {
        requireOpen();
        return worldEntityUuidState;
    }

    /** Read-only native visibility evidence for opt-in acceptance. */
    public Minecraft189WorldEntityVisibilityState worldEntityVisibilityState() {
        requireOpen();
        return worldEntityVisibilityState;
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
        if (!positionHostLease.enter(player)) {
            // A native XYZ getter can reenter this host synchronously.
            // A different original player revokes the outer read; it
            // must clear the shared source rather than mix coordinates.
            return;
        }
        try {
            // Clear both halves on every original host tick. No stale
            // fall/motion evidence can authorize automatic attacks.
            criticalsEvidence.reset();
            ++criticalsSourceGeneration;
            tickSprintControl = null;
            pendingKeepSprintSource = null;
            keepSprintAdmissionSerial++;
            tickHotbarControl = null;
            tickHotbarPlayer = null;
            tickHeldItemAccess = null;
            heldItemOwnerSerial++;
            nearestPlayerTargetState.clear();
            if (player == null) {
                clearOriginalPositionEvidence();
                return;
            }
            final double x = player.customMcPositionX();
            if (!positionHostLease.stillOwned()) {
                clearOriginalPositionEvidence();
                return;
            }
            final double y = player.customMcPositionY();
            if (!positionHostLease.stillOwned()) {
                clearOriginalPositionEvidence();
                return;
            }
            final double z = player.customMcPositionZ();
            if (!positionHostLease.stillOwned()) {
                clearOriginalPositionEvidence();
                return;
            }
            // Publish a complete, finite original XYZ observation
            // before sampling the genuine horizontal speed delta.
            playerPositionState.update(x, y, z);
            movementSpeedTracker.sample(x, z);
        } catch (RuntimeException | Error nativeFailure) {
            // A partially read native position is no usable evidence.
            clearOriginalPositionEvidence();
            throw nativeFailure;
        } finally {
            positionHostLease.exit();
        }
    }

    private void clearOriginalPositionEvidence() {
        targetRotationState.clear();
        playerPositionState.clear();
        movementSpeedTracker.clear();
    }

    private boolean rotationHostSourceStillOwned() {
        return rotationHostLease.stillOwned(
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    private void discardRotationSourceEvidence() {
        playerRotationState.clear();
        playerMovementState.clear();
        aimAssistRangeTargetState.clear();
        aimAssistRangeRotationState.clear();
        aimAssistCandidateRotationState.clear();
    }

    void playerRotation(
            final Minecraft189PlayerRotationAccess player) {
        requireOpen();
        final boolean originalGuiFocus =
                clickGuiRuntime.coreRuntime().model().snapshot().open();
        if (!rotationHostLease.enter(player, originalGuiFocus)) {
            return;
        }
        rotationAxisArbiter.enter();
        try {
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

            final float actualYaw = player.customMcRotationYaw();
            if (!rotationHostSourceStillOwned()) {
                discardRotationSourceEvidence();
                return;
            }
            final float actualPitch = player.customMcRotationPitch();
            if (!rotationHostSourceStillOwned()) {
                discardRotationSourceEvidence();
                return;
            }
            playerRotationState.update(actualYaw, actualPitch);
            final Minecraft189PlayerRotationControl control =
                    rotationHostLease.guardedControl(player,
                            () -> clickGuiRuntime.coreRuntime()
                                    .model().snapshot().open(),
                            Boolean.getBoolean(
                                    "custommc.acceptance.reportKillAuraFovVeto")
                                ? Minecraft189RuntimeBridge
                                    ::acceptanceFovOriginalYawSetterSucceeded
                                : null);
            final Minecraft189PlayerRotationState.Snapshot rotation =
                    playerRotationState.snapshot();
            final Minecraft189PlayerRotationControl spinControl =
                    rotationAxisArbiter.guardedControl(control,
                            Minecraft189RotationAxisArbiter.Owner.SPIN);
            final Minecraft189PlayerRotationControl auraControl =
                    rotationAxisArbiter.guardedControl(control,
                            Minecraft189RotationAxisArbiter.Owner.KILL_AURA);
            final Minecraft189PlayerRotationControl assistControl =
                    rotationAxisArbiter.guardedControl(control,
                            Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST);
            final Minecraft189PlayerRotationControl jitterControl =
                    rotationAxisArbiter.guardedControl(control,
                            Minecraft189RotationAxisArbiter.Owner.JITTER);

            // Refresh ground authority from the exact player being rotated,
            // not a movement snapshot published later in the host tick.
            final Minecraft189PlayerMovementState.Snapshot rotationMovement;
            if (player instanceof Minecraft189PlayerMovementStateAccess) {
                final Minecraft189PlayerMovementStateAccess movementPlayer =
                        (Minecraft189PlayerMovementStateAccess) player;
                final boolean actualGround = movementPlayer.customMcOnGround();
                if (!rotationHostSourceStillOwned()) {
                    discardRotationSourceEvidence();
                    return;
                }
                final boolean actualSneaking = movementPlayer.customMcSneaking();
                if (!rotationHostSourceStillOwned()) {
                    discardRotationSourceEvidence();
                    return;
                }
                final boolean actualSprinting = movementPlayer.customMcSprinting();
                if (!rotationHostSourceStillOwned()) {
                    discardRotationSourceEvidence();
                    return;
                }
                playerMovementState.update(
                        actualGround, actualSneaking, actualSprinting);
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
            final boolean spinOwnsRotation = featureCatalog.spin()
                    .apply(
                            spinControl,
                            rotation,
                            leftButtonHeld,
                            playerMovementState.snapshot());
            if (!rotationHostSourceStillOwned()) {
                discardRotationSourceEvidence();
                return;
            }
            if (spinOwnsRotation || rotationAxisArbiter.ownedBy(
                    Minecraft189RotationAxisArbiter.Owner.SPIN)) {
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
                final Minecraft189TargetRotationState.Snapshot auraTarget =
                        targetRotationState.snapshot();
                final boolean recordOriginalFovAim =
                        Boolean.getBoolean(
                                "custommc.acceptance.reportKillAuraFovVeto");
                final int beforeOriginalAuraYawWrites =
                        recordOriginalFovAim
                            ? Minecraft189RuntimeBridge
                                .acceptanceFovOriginalYawWriteCount()
                            : -1;
                final boolean originalAuraAimReturnedTrue =
                        aura.aim(auraControl, rotation, auraTarget,
                                leftButtonHeld, playerMovementState.snapshot());
                if (!rotationHostSourceStillOwned()) {
                    discardRotationSourceEvidence();
                    return;
                }
                // Opt-in FOV *acceptance* provenance only. Record already-
                // read original player yaw, target and unmodified aim result.
                // No second native player getter, new setter or target select.
                if (recordOriginalFovAim) {
                    Minecraft189RuntimeBridge.acceptanceFovObserveAuraAim(
                            rotation.available(), rotation.yaw(),
                            auraTarget.available(), auraTarget.entityIndex(),
                            auraTarget.yaw(), originalAuraAimReturnedTrue);
                    Minecraft189RuntimeBridge
                            .acceptanceFovAttributeOriginalYawWritesToAura(
                                beforeOriginalAuraYawWrites);
                }
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
                if (!rotationHostSourceStillOwned()) {
                    discardRotationSourceEvidence();
                    return;
                }
                aimAssistRangeRotationState.update(
                        local,
                        aimAssistRangeTargetState.snapshot());
                assistTarget = aimAssistRangeRotationState.snapshot();
            }
            final boolean assistOwnsRotation = assist.apply(
                            assistControl,
                            rotation,
                            assistTarget,
                            leftButtonHeld,
                            inputState.keyPressed(
                                    LegacyKeyboardCodes.W),
                            rotationMovement);
            if (!rotationHostSourceStillOwned()) {
                discardRotationSourceEvidence();
                return;
            }
            if (assistOwnsRotation || rotationAxisArbiter.ownedBy(
                    Minecraft189RotationAxisArbiter.Owner.AIM_ASSIST)) {
                featureCatalog.jitter()
                        .apply(
                                null,
                                null,
                                false);
                return;
            }
            featureCatalog.jitter()
                    .apply(
                            jitterControl,
                            rotation,
                            leftButtonHeld,
                            playerMovementState.snapshot(),
                            Boolean.valueOf(inputState.pointerPressed(
                                    Minecraft189ClickRateTracker.RIGHT_BUTTON)));
            if (!rotationHostSourceStillOwned()) {
                discardRotationSourceEvidence();
            }
        } catch (RuntimeException | Error nativeFailure) {
            // Invalid/partial original yaw/pitch or ground/sneak/sprint
            // reads must not leak the previous player's source evidence.
            discardRotationSourceEvidence();
            throw nativeFailure;
        } finally {
            // Invalidate module-specific wrappers before the original
            // player source can be used by the next native callback.
            rotationAxisArbiter.exit();
            rotationHostLease.exit();
        }
    }

    void playerDimension(
            final Minecraft189PlayerDimensionAccess player) {
        requireOpen();
        if (!dimensionHostLease.enter(player)) {
            return;
        }
        try {
            if (player == null) {
                playerDimensionState.clear();
                return;
            }
            final int nativeDimension = player.customMcDimension();
            if (!dimensionHostLease.stillOwned()) {
                playerDimensionState.clear();
                return;
            }
            // Preserve all actual native dimension IDs, including
            // negative Nether and modded IDs; do not infer from posY.
            playerDimensionState.update(nativeDimension);
        } catch (RuntimeException | Error nativeFailure) {
            playerDimensionState.clear();
            throw nativeFailure;
        } finally {
            dimensionHostLease.exit();
        }
    }

    void playerMovementState(
            final Minecraft189PlayerMovementStateAccess player) {
        requireOpen();
        // A mapped ground/sneak/sprint getter may synchronously reenter
        // this host with the same or a different original player.
        if (!movementSnapshotLease.enter(player)) {
            return;
        }
        try {
            if (player == null) {
                playerMovementState.clear();
                return;
            }
            final boolean ground = player.customMcOnGround();
            if (!movementSnapshotLease.stillOwned()) {
                playerMovementState.clear();
                return;
            }
            final boolean sneaking = player.customMcSneaking();
            if (!movementSnapshotLease.stillOwned()) {
                playerMovementState.clear();
                return;
            }
            final boolean sprinting = player.customMcSprinting();
            if (!movementSnapshotLease.stillOwned()) {
                playerMovementState.clear();
                return;
            }
            // Publish all THREE actual original-player observations as a
            // single atomic snapshot, never an old/new-player mixture.
            playerMovementState.update(ground, sneaking, sprinting);
        } catch (RuntimeException | Error nativeFailure) {
            // A partial genuine original getter is not valid evidence.
            // Do not leave the former grounded/sprint/sneak state visible.
            playerMovementState.clear();
            throw nativeFailure;
        } finally {
            movementSnapshotLease.exit();
        }
    }

    private boolean sprintHostSourceStillOwned() {
        return sprintHostLease.stillOwned(
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void playerSprintControl(
            final Minecraft189PlayerSprintControl player) {
        requireOpen();
        final boolean originalGuiFocus =
                clickGuiRuntime.coreRuntime().model().snapshot().open();
        if (!sprintHostLease.enter(player, originalGuiFocus)) {
            return;
        }
        // A nested original sprint callback invalidates the KeepSprint
        // post-attack source sample, even for the same player reference.
        keepSprintRestoreLease.revoke();
        sprintWriteArbiter.enter();
        final Minecraft189PlayerSprintControl guardedPlayer =
                sprintHostLease.guardedPlayer(player,
                        () -> clickGuiRuntime.coreRuntime()
                                .model().snapshot().open());
        final Minecraft189PlayerSprintControl wTapControl =
                sprintWriteArbiter.guardedPlayer(guardedPlayer,
                        Minecraft189SprintWriteArbiter.Owner.W_TAP);
        final Minecraft189PlayerSprintControl autoSprintControl =
                sprintWriteArbiter.guardedPlayer(guardedPlayer,
                        Minecraft189SprintWriteArbiter.Owner.AUTO_SPRINT);
        try {
            // The identity recorded for a later KeepSprint restore must
            // remain the ORIGINAL player, never this guarded adapter.
            // Nested different-player setter callbacks cannot replace it.
            if (tickSprintControl != player) {
                pendingKeepSprintSource = null;
                keepSprintAdmissionSerial++;
            }
            tickSprintControl = player;
            if (guardedPlayer == null) {
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
            if (originalGuiFocus) {
                featureCatalog.wTap().suspendForGui(attackHeld);
            } else {
                final boolean wTapReportedWrite = featureCatalog.wTap().apply(
                        wTapControl, movement, attackHeld,
                        inputState.keyPressed(LegacyKeyboardCodes.W),
                        nearestPlayerTargetState.snapshot());
                // Real setSprinting(false) can reenter WTap OFF->ON and
                // cause apply() to return false after writing. Such a
                // setter still OWNS this callback; AutoSprint must not
                // replay an opposite setSprinting(true) on old evidence.
                if (wTapReportedWrite || sprintWriteArbiter.ownedBy(
                        Minecraft189SprintWriteArbiter.Owner.W_TAP))
                    return;
            }
            // A genuine original WTap setter can synchronously reenter
            // this host. AutoSprint must never consume the old snapshot
            // after another native player or GUI focus is observed.
            if (!sprintHostSourceStillOwned()) {
                return;
            }
            featureCatalog.autoSprint()
                    .apply(
                            autoSprintControl,
                            movement,
                            inputState.keyPressed(LegacyKeyboardCodes.W),
                            inputState.keyPressed(LegacyKeyboardCodes.W)
                                    || inputState.keyPressed(LegacyKeyboardCodes.A)
                                    || inputState.keyPressed(LegacyKeyboardCodes.S)
                                    || inputState.keyPressed(LegacyKeyboardCodes.D),
                            clickGuiRuntime.coreRuntime().model().snapshot().open());
        } finally {
            // Native setSprinting may apply real vanilla state then throw.
            // Never invent a sprint rollback or leave the host guard held.
            sprintWriteArbiter.exit();
            sprintHostLease.exit();
        }
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
                                || inputState.keyPressed(LegacyKeyboardCodes.D),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    float adjustNoSlowMovement(
            final float slowedValue) {
        requireOpen();
        return featureCatalog.noSlow()
                .adjustSlowedMovement(
                        slowedValue,
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    double adjustVelocityHorizontal(
            final double before,
            final double after) {
        requireOpen();
        return featureCatalog.velocity()
                .adjustHorizontal(
                        before,
                        after,
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    double adjustVelocityVertical(
            final double before,
            final double after) {
        requireOpen();
        return featureCatalog.velocity()
                .adjustVertical(
                        before,
                        after,
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    private boolean jumpHostSourceStillOwned() {
        return jumpHostLease.stillOwned(
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void playerJumpControl(
            final Minecraft189PlayerJumpControl player) {
        requireOpen();
        final boolean originalGuiFocus =
                clickGuiRuntime.coreRuntime().model().snapshot().open();
        if (!jumpHostLease.enter(player, originalGuiFocus)) {
            return;
        }
        final Minecraft189PlayerJumpControl guardedPlayer =
                jumpHostLease.guardedPlayer(player,
                        () -> clickGuiRuntime.coreRuntime()
                                .model().snapshot().open());
        try {
            if (guardedPlayer == null) {
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
            final boolean actualClickGuiOpen =
                    clickGuiRuntime.coreRuntime().model().snapshot().open();
            final boolean movementJumpSuspended =
                    actualClickGuiOpen
                            || featureCatalog.freeze()
                                    .active()
                            || featureCatalog.flight()
                                    .active();
            // Combat Jump Reset consumes only a fresh mapped local hurt edge.
            // Never execute a second synthetic jump in the same callback.
            // Existing movement jump modules retain priority while active.
            if (featureCatalog.jumpReset().apply(
                    guardedPlayer, movement, playerHurtTimeState.snapshot(),
                    inputState.keyPressed(LegacyKeyboardCodes.W),
                    movementJumpSuspended || longJumpActive || highJumpActive
                            || lowHopActive || bunnyHopActive
                            || featureCatalog.airJump().active())) {
                return;
            }
            if (!jumpHostSourceStillOwned()) return;
            featureCatalog.longJump()
                    .applyJump(
                            guardedPlayer,
                            movement,
                            movementJumpSuspended);
            if (!jumpHostSourceStillOwned()) return;
            featureCatalog.highJump()
                    .applyJump(
                            guardedPlayer,
                            movement,
                            movementJumpSuspended
                                    || longJumpActive);
            if (!jumpHostSourceStillOwned()) return;
            featureCatalog.lowHop()
                    .applyJump(
                            guardedPlayer,
                            movement,
                            movementJumpSuspended
                                    || longJumpActive
                                    || highJumpActive);
            if (!jumpHostSourceStillOwned()) return;
            featureCatalog.bunnyHop()
                    .applyJump(
                            guardedPlayer,
                            movement,
                            movementJumpSuspended
                                    || longJumpActive
                                    || highJumpActive
                                    || lowHopActive,
                            actualClickGuiOpen);
            if (!jumpHostSourceStillOwned()) return;
            if (!longJumpActive
                    && !highJumpActive
                    && !lowHopActive
                    && !bunnyHopActive) {
                featureCatalog.autoJump()
                        .apply(
                                guardedPlayer,
                                movement,
                                inputState.keyPressed(LegacyKeyboardCodes.W),
                                inputState.keyPressed(LegacyKeyboardCodes.W)
                                        || inputState.keyPressed(LegacyKeyboardCodes.A)
                                        || inputState.keyPressed(LegacyKeyboardCodes.S)
                                        || inputState.keyPressed(LegacyKeyboardCodes.D),
                                actualClickGuiOpen);
            }
            if (!jumpHostSourceStillOwned()) return;
            featureCatalog.airJump()
                    .apply(
                            guardedPlayer,
                            movement,
                            actualClickGuiOpen);
        } finally {
            // Never fake a rollback of vanilla EntityPlayer.jump(),
            // even when a mapped jump has already updated real Y.
            jumpHostLease.exit();
        }
    }

    void playerStepControl(
            final Minecraft189PlayerStepControl player) {
        requireOpen();
        featureCatalog.step()
                .apply(
                        player,
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void playerFallDistanceControl(
            final Minecraft189PlayerFallDistanceControl player) {
        requireOpen();
        if (!fallHostLease.enter(player)) {
            return;
        }
        final Minecraft189PlayerFallDistanceControl guardedPlayer =
                fallHostLease.guardedPlayer(player,
                        () -> clickGuiRuntime.coreRuntime()
                                .model().snapshot().open());
        try {
            featureCatalog.noFall()
                    .apply(guardedPlayer, playerMovementState.snapshot(),
                            clickGuiRuntime.coreRuntime().model().snapshot().open());
            if (!fallHostLease.stillOwned()) {
                // One original getter/setter may have reentered with a
                // different player. No stale Criticals fall evidence.
                criticalsEvidence.fall(Float.NaN);
                return;
            }
            if (guardedPlayer == null) {
                criticalsEvidence.fall(Float.NaN);
                return;
            }
            final float nativeFall = guardedPlayer.customMcFallDistance();
            if (!fallHostLease.stillOwned()) {
                criticalsEvidence.fall(Float.NaN);
                return;
            }
            criticalsEvidence.fall(nativeFall);
        } catch (RuntimeException | Error nativeFailure) {
            // Never fabricate a rollback for a real NoFall native
            // setter that has already changed physical fallDistance.
            // Its partial write remains real, but its Criticals
            // observation is unavailable on this failed host tick.
            criticalsEvidence.fall(Float.NaN);
            throw nativeFailure;
        } finally {
            fallHostLease.exit();
        }
    }

    void playerWebControl(
            final Minecraft189PlayerWebControl player) {
        requireOpen();
        featureCatalog.noWeb()
                .apply(
                        player,
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void playerNoClipControl(
            final Minecraft189PlayerNoClipControl player) {
        requireOpen();
        featureCatalog.noClip()
                .apply(
                        player,
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    private boolean motionHostSourceStillOwned() {
        return motionHostLease.stillOwned(
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void playerMotionControl(
            final Minecraft189PlayerMotionControl player) {
        requireOpen();
        final boolean originalGuiFocus =
                clickGuiRuntime.coreRuntime().model().snapshot().open();
        if (!motionHostLease.enter(player, originalGuiFocus)) {
            return;
        }
        final Minecraft189PlayerMotionControl guardedPlayer =
                motionHostLease.guardedPlayer(player,
                        () -> clickGuiRuntime.coreRuntime()
                                .model().snapshot().open());
        try {
            final Minecraft189PlayerHurtTimeState.Snapshot hurtTime =
                    playerHurtTimeState.snapshot();
            final boolean freezeActive =
                    featureCatalog.freeze()
                            .active();
            featureCatalog.freeze()
                    .apply(
                            guardedPlayer,
                            clickGuiRuntime.coreRuntime().model().snapshot().open());
            if (!motionHostSourceStillOwned()) return;
            if (freezeActive) {
                featureCatalog.damageBoost()
                        .apply(guardedPlayer, hurtTime, true);
                return;
            }

            final Minecraft189PlayerRotationState.Snapshot rotation =
                    playerRotationState.snapshot();
            final Minecraft189PlayerMovementState.Snapshot movement =
                    playerMovementState.snapshot();
            final boolean actualClickGuiOpen =
                    clickGuiRuntime.coreRuntime().model().snapshot().open();
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
            // CustomMC GUI focus is a common priority veto for keyboard-driven
            // movement. Do not only suspend Flight: lower-priority policies
            // would otherwise overwrite native motion with stale held W/A/S/D.
            // Feed the existing suspended inputs so one-shot pending impulses
            // are consumed without a delayed replay when the GUI closes.
            final boolean longJumpOwnsHorizontal =
                    featureCatalog.longJump().applyMotion(
                            guardedPlayer, rotation,
                            flightActive || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.highJump().applyMotion(
                    guardedPlayer, flightActive || longJumpActive || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.lowHop().applyMotion(
                    guardedPlayer, flightActive || longJumpActive || highJumpActive
                            || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            final boolean bunnyHopOwnsHorizontal =
                    featureCatalog.bunnyHop().applyMotion(
                            guardedPlayer, rotation,
                            flightActive || longJumpActive || highJumpActive
                                    || lowHopActive || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            final boolean movementSpeedOwnsHorizontal =
                    featureCatalog.movementSpeed().apply(
                            guardedPlayer, movement, rotation,
                            flightActive || longJumpOwnsHorizontal
                                    || bunnyHopOwnsHorizontal || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            final boolean airSpeedOwnsHorizontal =
                    featureCatalog.airSpeed().apply(
                            guardedPlayer, movement, rotation,
                            flightActive || longJumpOwnsHorizontal
                                    || bunnyHopOwnsHorizontal || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.flight().apply(
                    guardedPlayer, rotation, movement, actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            final boolean horizontalOwnerActive = flightActive
                    || longJumpOwnsHorizontal || bunnyHopOwnsHorizontal
                    || movementSpeedOwnsHorizontal || airSpeedOwnsHorizontal;
            if (!motionHostSourceStillOwned()) return;
            final boolean targetStrafeOwnsHorizontal = featureCatalog.targetStrafe()
                    .apply(guardedPlayer, playerPositionState.snapshot(),
                            nearestPlayerTargetState.snapshot(), movement,
                            inputState.keyPressed(LegacyKeyboardCodes.W),
                            horizontalOwnerActive || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.strafe().apply(
                    guardedPlayer, rotation,
                    horizontalOwnerActive || targetStrafeOwnsHorizontal
                            || actualClickGuiOpen, movement);
            if (!motionHostSourceStillOwned()) return;
            final boolean reverseStepOwnsVertical =
                    featureCatalog.reverseStep()
                            .apply(
                                    guardedPlayer,
                                    movement,
                                    flightActive
                                            || noGravityActive,
                                    inputState.keyPressed(LegacyKeyboardCodes.W)
                                            || inputState.keyPressed(LegacyKeyboardCodes.A)
                                            || inputState.keyPressed(LegacyKeyboardCodes.S)
                                            || inputState.keyPressed(LegacyKeyboardCodes.D),
                                    clickGuiRuntime.coreRuntime().model().snapshot().open());
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.noGravity()
                    .apply(
                            guardedPlayer,
                            movement,
                            flightActive,
                            jumpHeld,
                            clickGuiRuntime.coreRuntime().model().snapshot().open());
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.fastFall()
                    .apply(
                            guardedPlayer,
                            movement,
                            flightActive
                                    || noGravityActive
                                    || reverseStepOwnsVertical,
                            clickGuiRuntime.coreRuntime().model().snapshot().open());
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.glide()
                    .apply(
                            guardedPlayer,
                            movement,
                            flightActive
                                    || noGravityActive
                                    || reverseStepOwnsVertical
                                    || fastFallActive,
                            clickGuiRuntime.coreRuntime().model().snapshot().open());
            if (!motionHostSourceStillOwned()) return;
            featureCatalog.damageBoost()
                    .apply(guardedPlayer, hurtTime, flightActive || actualClickGuiOpen);
            if (!motionHostSourceStillOwned()) return;
            if (guardedPlayer != null) {
                // Genuine mapped Y read can itself reenter the host.
                // Do not publish old Criticals source evidence after a
                // nested different original player or newly focused UI.
                final double observedMotionY = guardedPlayer.customMcMotionY();
                if (motionHostSourceStillOwned()) {
                    criticalsEvidence.motion(observedMotionY);
                }
            }
        } finally {
            // A genuine mapped setter may write velocity and throw.
            // Release only the host lease, not source player motion.
            motionHostLease.exit();
        }
    }

    void playerHealth(
            final Minecraft189PlayerHealthAccess player) {
        requireOpen();
        if (!healthHostLease.enter(player)) return;
        try {
            if (player == null) {
                playerHealthState.clear();
                return;
            }
            final float originalHealth = player.customMcHealth();
            if (!healthHostLease.stillOwned()) {
                playerHealthState.clear();
                return;
            }
            final float originalMaxHealth = player.customMcMaxHealth();
            if (!healthHostLease.stillOwned()) {
                playerHealthState.clear();
                return;
            }
            playerHealthState.update(originalHealth, originalMaxHealth);
        } catch (RuntimeException | Error nativeFailure) {
            // Getter failure or invalid original value must not retain
            // previously valid local-player health evidence.
            playerHealthState.clear();
            throw nativeFailure;
        } finally {
            healthHostLease.exit();
        }
    }

    void playerHurtTime(
            final Minecraft189PlayerHurtTimeAccess player) {
        requireOpen();
        if (!hurtTimeHostLease.enter(player)) {
            return;
        }
        try {
            if (player == null) {
                playerHurtTimeState.clear();
                return;
            }
            final int nativeHurtTime = player.customMcHurtTime();
            if (!hurtTimeHostLease.stillOwned()) {
                playerHurtTimeState.clear();
                return;
            }
            // Do not allow an invalid native negative tick to retain
            // an earlier real damage edge for JumpReset or DamageBoost.
            playerHurtTimeState.update(nativeHurtTime);
        } catch (RuntimeException | Error nativeFailure) {
            playerHurtTimeState.clear();
            throw nativeFailure;
        } finally {
            hurtTimeHostLease.exit();
        }
    }

    void playerArmor(
            final Minecraft189PlayerArmorAccess player) {
        requireOpen();
        if (!armorHostLease.enter(player)) return;
        try {
            if (player == null) {
                playerArmorState.clear();
                return;
            }
            // Each of the eight original mapped getter callbacks can
            // synchronously reenter with a different local player/world.
            // Never accept a partially mixed exact-reference source tick.
            final boolean boots = player.customMcArmorBoots();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            final boolean leggings = player.customMcArmorLeggings();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            final boolean chestplate = player.customMcArmorChestplate();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            final boolean helmet = player.customMcArmorHelmet();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            final Minecraft189ItemStackAccess bootsItem =
                    player.customMcArmorBootsItem();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            final Minecraft189ItemStackAccess leggingsItem =
                    player.customMcArmorLeggingsItem();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            final Minecraft189ItemStackAccess chestplateItem =
                    player.customMcArmorChestplateItem();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            final Minecraft189ItemStackAccess helmetItem =
                    player.customMcArmorHelmetItem();
            if (!armorHostLease.stillOwned()) {
                playerArmorState.clear();
                return;
            }
            // ItemStack damage/maxDamage getters execute inside update.
            // Its publication is now transactional. Check the same source
            // again after every actual durability getter has returned.
            playerArmorState.update(boots, leggings, chestplate, helmet,
                    bootsItem, leggingsItem, chestplateItem, helmetItem);
            if (!armorHostLease.stillOwned()) playerArmorState.clear();
        } catch (RuntimeException | Error nativeFailure) {
            playerArmorState.clear();
            throw nativeFailure;
        } finally {
            armorHostLease.exit();
        }
    }

    void playerHunger(
            final Minecraft189PlayerHungerAccess player) {
        requireOpen();
        if (!hungerHostLease.enter(player)) return;
        try {
            if (player == null) {
                playerHungerState.clear();
                return;
            }
            final int originalFood = player.customMcFoodLevel();
            if (!hungerHostLease.stillOwned()) {
                playerHungerState.clear();
                return;
            }
            final float originalSaturation = player.customMcSaturationLevel();
            if (!hungerHostLease.stillOwned()) {
                playerHungerState.clear();
                return;
            }
            playerHungerState.update(originalFood, originalSaturation);
        } catch (RuntimeException | Error nativeFailure) {
            // A partial native FoodStats sample must not preserve stale
            // hunger evidence after a getter or genuine value error.
            playerHungerState.clear();
            throw nativeFailure;
        } finally {
            hungerHostLease.exit();
        }
    }

    void playerPotionEffects(
            final Minecraft189PlayerPotionEffectsAccess player) {
        requireOpen();
        if (!potionEffectsHostLease.enter(player)) return;
        try {
            if (player == null) {
                playerPotionEffectsState.clear();
                return;
            }
            // Native array retrieval can reenter and replace the source.
            final Minecraft189PotionEffectAccess[] originalEffects =
                    player.customMcPotionEffects();
            if (!potionEffectsHostLease.stillOwned()) {
                playerPotionEffectsState.clear();
                return;
            }
            // Snapshot calls four original getters PER native effect.
            // State itself computes all immutable effect records before
            // publication. A changed source mid-iteration is discarded.
            playerPotionEffectsState.update(originalEffects);
            if (!potionEffectsHostLease.stillOwned())
                playerPotionEffectsState.clear();
        } catch (RuntimeException | Error nativeFailure) {
            // An inaccessible effect, invalid mapped potion value, null
            // source array or getter failure cannot retain stale HUD data.
            playerPotionEffectsState.clear();
            throw nativeFailure;
        } finally {
            potionEffectsHostLease.exit();
        }
    }

    void playerExperience(
            final Minecraft189PlayerExperienceAccess player) {
        requireOpen();
        if (!experienceHostLease.enter(player)) return;
        try {
            if (player == null) {
                playerExperienceState.clear();
                return;
            }
            final int level = player.customMcExperienceLevel();
            if (!experienceHostLease.stillOwned()) {
                playerExperienceState.clear();
                return;
            }
            final int total = player.customMcExperienceTotal();
            if (!experienceHostLease.stillOwned()) {
                playerExperienceState.clear();
                return;
            }
            final float progress = player.customMcExperienceProgress();
            if (!experienceHostLease.stillOwned()) {
                playerExperienceState.clear();
                return;
            }
            final int barCap = player.customMcExperienceBarCap();
            if (!experienceHostLease.stillOwned()) {
                playerExperienceState.clear();
                return;
            }
            playerExperienceState.update(level, total, progress, barCap);
        } catch (RuntimeException | Error nativeFailure) {
            playerExperienceState.clear();
            throw nativeFailure;
        } finally {
            experienceHostLease.exit();
        }
    }

    void playerPing(
            final Minecraft189PlayerPingAccess player) {
        requireOpen();
        if (!pingHostLease.enter(player)) return;
        try {
            if (player == null) {
                playerPingState.clear();
                return;
            }
            final int milliseconds = player.customMcPingMilliseconds();
            if (!pingHostLease.stillOwned() || milliseconds < 0) {
                playerPingState.clear();
                return;
            }
            playerPingState.update(milliseconds);
        } catch (RuntimeException | Error nativeFailure) {
            // A failing original connection getter cannot preserve an old
            // HUD ping measured for a different player/session.
            playerPingState.clear();
            throw nativeFailure;
        } finally {
            pingHostLease.exit();
        }
    }

    void playerHotbarSlot(
            final Minecraft189PlayerInventoryAccess player) {
        requireOpen();
        if (!hotbarHostLease.enter(player)) return;
        try {
            // Even an A-B-A native callback revokes the old Criticals
            // source generation when the same Java player returns.
            ++criticalsSourceGeneration;
            // Neither the selected HUD slot nor the mutable combat hotbar
            // control may survive a changed/null original player source.
            tickHotbarControl = null;
            tickHotbarPlayer = null;
            if (player == null) {
                hotbarSlotState.clear();
                return;
            }
            final Minecraft189InventoryHotbarAccess inventory =
                    player.customMcInventory();
            if (!hotbarHostLease.stillOwned() || inventory == null) {
                hotbarSlotState.clear();
                return;
            }
            final int slot = inventory.customMcSelectedHotbarSlot();
            if (!hotbarHostLease.stillOwned()
                    || slot < 0
                    || slot >= Minecraft189HotbarSlotState.SLOT_COUNT) {
                hotbarSlotState.clear();
                return;
            }
            // Publish original inventory ownership ONLY after the two
            // mapped getter calls have completed without source revocation.
            tickHotbarPlayer = player;
            if (inventory instanceof Minecraft189InventoryHotbarControl)
                tickHotbarControl = (Minecraft189InventoryHotbarControl) inventory;
            hotbarSlotState.update(slot);
        } catch (RuntimeException | Error nativeFailure) {
            tickHotbarControl = null;
            tickHotbarPlayer = null;
            hotbarSlotState.clear();
            throw nativeFailure;
        } finally {
            hotbarHostLease.exit();
        }
    }

    void worldTime(
            final Minecraft189WorldTimeAccess world) {
        requireOpen();
        if (!worldTimeHostLease.enter(world)) return;
        try {
            if (world == null) {
                worldTimeState.clear();
                return;
            }
            final long originalTime = world.customMcWorldTime();
            if (!worldTimeHostLease.stillOwned()) {
                worldTimeState.clear();
                return;
            }
            worldTimeState.update(originalTime);
        } catch (RuntimeException | Error nativeFailure) {
            worldTimeState.clear();
            throw nativeFailure;
        } finally {
            worldTimeHostLease.exit();
        }
    }

    void worldWeather(
            final Minecraft189WorldWeatherAccess world) {
        requireOpen();
        if (!worldWeatherHostLease.enter(world)) return;
        try {
            if (world == null) {
                worldWeatherState.clear();
                return;
            }
            final boolean originalRaining = world.customMcRaining();
            if (!worldWeatherHostLease.stillOwned()) {
                worldWeatherState.clear();
                return;
            }
            final boolean originalThundering = world.customMcThundering();
            if (!worldWeatherHostLease.stillOwned()) {
                worldWeatherState.clear();
                return;
            }
            // Both real world booleans must belong to the same source.
            worldWeatherState.update(originalRaining, originalThundering);
        } catch (RuntimeException | Error nativeFailure) {
            worldWeatherState.clear();
            throw nativeFailure;
        } finally {
            worldWeatherHostLease.exit();
        }
    }

    void worldEntityPositions(
            final Minecraft189WorldEntityPositionsAccess world) {
        requireOpen();
        if (!worldEntityHostLease.enter(world)) {
            // A nested callback cannot publish positions for a new
            // world while an older native list getter owns the stack.
            return;
        }
        try {
            // Native loadedEntityList source evidence belongs to only
            // one original world per host sample.
            worldEntityCombatState.clear();
            worldEntityKindState.clear();
            tickOriginalEntityWorld = null;
            nearestPlayerTargetState.clear();
            targetRotationState.clear();
            worldEntityUuidState.clear();
            worldEntityVisibilityState.clear();
            tickVisibilityWorld = world instanceof Minecraft189WorldEntityVisibilityAccess
                    ? (Minecraft189WorldEntityVisibilityAccess) world : null;

            if ((featureCatalog.wallCheck().active()
                    || Boolean.getBoolean("custommc.acceptance.reportKillAuraWallLifecycle"))
                    && tickVisibilityWorld != null) {
                try {
                    final int[] originalVisibility =
                            tickVisibilityWorld.customMcLoadedEntityVisibility();
                    if (!worldEntityHostLease.stillOwned()) {
                        clearRevokedWorldEntitySource();
                        return;
                    }
                    worldEntityVisibilityState.update(originalVisibility);
                } catch (RuntimeException invalidEvidence) {
                    if (!worldEntityHostLease.stillOwned()) {
                        clearRevokedWorldEntitySource();
                        return;
                    }
                    worldEntityVisibilityState.clear();
                }
            }
            final Minecraft189WorldEntityUuidAccess identityWorld =
                    world instanceof Minecraft189WorldEntityUuidAccess
                            ? (Minecraft189WorldEntityUuidAccess) world : null;
            if (tickUuidWorld != identityWorld) {
                featureCatalog.killAura().rememberSelectedTarget(
                        null, "original-world-identity-changed");
                featureCatalog.hitSelect().suspendForSource();
            }
            tickUuidWorld = identityWorld;
            if (world == null) {
                clearRevokedWorldEntitySource();
                return;
            }
            if (identityRequired() && identityWorld != null) {
                try {
                    final java.util.UUID[] originalIds =
                            identityWorld.customMcLoadedEntityUuids();
                    if (!worldEntityHostLease.stillOwned()) {
                        clearRevokedWorldEntitySource();
                        return;
                    }
                    worldEntityUuidState.update(originalIds);
                } catch (RuntimeException invalidEvidence) {
                    if (!worldEntityHostLease.stillOwned()) {
                        clearRevokedWorldEntitySource();
                        return;
                    }
                    worldEntityUuidState.clear();
                }
            }
            final double[] originalPositions =
                    world.customMcLoadedEntityPositions();
            if (!worldEntityHostLease.stillOwned()) {
                clearRevokedWorldEntitySource();
                return;
            }
            if (originalPositions == null) {
                clearRevokedWorldEntitySource();
                return;
            }
            // A malformed original position list cannot preserve
            // previously published UUID or LOS for another list.
            try {
                worldEntityPositionState.update(originalPositions);
                tickOriginalEntityWorld = world;
            } catch (RuntimeException invalidPositions) {
                clearRevokedWorldEntitySource();
                throw invalidPositions;
            }
        } catch (RuntimeException | Error nativeFailure) {
            // Genuine getter failures must never leave a usable stale
            // entity position alongside partly sampled UUID/LOS state.
            clearRevokedWorldEntitySource();
            throw nativeFailure;
        } finally {
            worldEntityHostLease.exit();
        }
    }

    private void clearRevokedWorldEntitySource() {
        tickOriginalEntityWorld = null;
        worldEntityKindState.clear();
        worldEntityPositionState.clear();
        worldEntityUuidState.clear();
        worldEntityVisibilityState.clear();
        worldEntityCombatState.clear();
        nearestPlayerTargetState.clear();
        targetRotationState.clear();
        tickUuidWorld = null;
        tickVisibilityWorld = null;
        featureCatalog.killAura().rememberSelectedTarget(
                null, "original-world-source-revoked");
        featureCatalog.hitSelect().suspendForSource();
    }

    void worldEntityKinds(
            final Minecraft189WorldEntityKindsAccess world) {
        requireOpen();
        if (!worldEntityDownstreamLease.enter(world)) {
            // A mapped World getter can invoke the whole host recursively.
            // Nested same source isn't a new tick; nested different/null
            // source permanently revokes this still-open native sample.
            return;
        }
        try {
            if (world == null || world != tickOriginalEntityWorld
                    || !worldEntityPositionState.snapshot().available()) {
                clearRevokedWorldEntitySource();
                return;
            }
            final int[] actualKinds = world.customMcLoadedEntityKinds();
            if (!worldEntityDownstreamSourceStillOwned(world)) {
                clearRevokedWorldEntitySource();
                return;
            }
            if (actualKinds == null
                    || actualKinds.length != worldEntityPositionState.snapshot().entityCount()) {
                clearRevokedWorldEntitySource();
                return;
            }
            worldEntityKindState.update(actualKinds);
            nearestPlayerTargetState.update(
                    playerPositionState.snapshot(),
                    worldEntityPositionState.snapshot(),
                    worldEntityKindState.snapshot());
            if (!worldEntityDownstreamSourceStillOwned(world)) {
                clearRevokedWorldEntitySource();
                return;
            }
            targetRotationState.update(
                    playerPositionState.snapshot(),
                    nearestPlayerTargetState.snapshot());
            if (!worldEntityDownstreamSourceStillOwned(world)) {
                clearRevokedWorldEntitySource();
            }
        } catch (RuntimeException | Error invalidNativeKinds) {
            clearRevokedWorldEntitySource();
            throw invalidNativeKinds;
        } finally {
            worldEntityDownstreamLease.exit();
        }
    }

    void worldEntityCombat(
            final Minecraft189WorldEntityCombatAccess world) {
        requireOpen();
        if (!worldEntityDownstreamLease.enter(world)) {
            return;
        }
        try {
            if (world == null || world != tickOriginalEntityWorld
                    || !worldEntityPositionState.snapshot().available()
                    || !worldEntityKindState.snapshot().available()) {
                clearRevokedWorldEntitySource();
                return;
            }
            final Minecraft189KillAuraModule aura = featureCatalog.killAura();
            // Existing pre-combat identity/LOS gate remains mandatory.
            // The same World can reorder loadedEntityList inside the
            // mapped combat getter, so pre-check alone is insufficient.
            if (!originalWorldCombatEvidenceStillConsistent(world)) {
                rejectInconsistentOriginalCombatSource(world);
                return;
            }
            final int[] actualCombat = world.customMcLoadedEntityCombatStates();
            if (!worldEntityDownstreamSourceStillOwned(world)) {
                clearRevokedWorldEntitySource();
                return;
            }
            // Revalidate the exact ORIGINAL UUID ORDER and LOS after
            // the genuine combat native getter returns. This is the
            // final acceptance boundary before any combat publication.
            if (!originalWorldCombatEvidenceStillConsistent(world)) {
                rejectInconsistentOriginalCombatSource(world);
                return;
            }
            if (actualCombat == null
                    || actualCombat.length != worldEntityPositionState.snapshot().entityCount()
                    || actualCombat.length != worldEntityKindState.snapshot().entityCount()) {
                // Kinds previously populated nearest/aim evidence BEFORE
                // this genuine combat getter. A missing or mismatched
                // combat list invalidates that positional target too.
                rejectInconsistentOriginalCombatSource(world);
                return;
            }
            try {
                worldEntityCombatState.update(actualCombat);
            } catch (IllegalArgumentException malformed) {
                // A genuine native list can be correctly sized yet
                // contain invalid hurt/team/network bits. Retaining a
                // kind-only nearest target would allow stale targeting.
                rejectInconsistentOriginalCombatSource(world);
                return;
            }
            // Preserve the original nearest *living* player selection
            // without permitting an older World callback to publish it.
            if (featureCatalog.killAura().active()) {
                Minecraft189KillAuraTargetSelector.select(
                        featureCatalog.killAura(),
                        playerPositionState.snapshot(),
                        playerRotationState.snapshot(),
                        worldEntityPositionState.snapshot(),
                        worldEntityKindState.snapshot(),
                        worldEntityCombatState.snapshot(),
                        featureCatalog.antiBot(), featureCatalog.teamGuard(),
                        featureCatalog.friendGuard(),
                        worldEntityUuidState.snapshot(),
                        featureCatalog.wallCheck(), worldEntityVisibilityState.snapshot(),
                        nearestPlayerTargetState);
                if (!worldEntityDownstreamSourceStillOwned(world)) {
                    clearRevokedWorldEntitySource();
                    return;
                }
                targetRotationState.update(
                        playerPositionState.snapshot(),
                        nearestPlayerTargetState.snapshot());
                if (!worldEntityDownstreamSourceStillOwned(world)) {
                    clearRevokedWorldEntitySource();
                }
            }
        } catch (RuntimeException | Error genuineNativeFailure) {
            clearRevokedWorldEntitySource();
            throw genuineNativeFailure;
        } finally {
            worldEntityDownstreamLease.exit();
        }
    }

    /**
     * Source-consistent native loadedEntityList evidence. Evaluated both
     * BEFORE and AFTER the original combat getter, so an unchanged World
     * object cannot silently swap its UUID order or native LOS during
     * the mapped combat snapshot.
     *
     * No new UUID or LOS getter is invoked when the corresponding
     * original policy does not require that evidence.
     */
    private boolean originalWorldCombatEvidenceStillConsistent(
            final Minecraft189WorldEntityCombatAccess world) {
        if (identityRequired()) {
            final Minecraft189WorldEntityUuidState.Snapshot ids =
                    worldEntityUuidState.snapshot();
            boolean consistent = false;
            if (tickUuidWorld != null && tickUuidWorld == world
                    && ids.available()
                    && ids.entityCount() == worldEntityPositionState.snapshot().entityCount()
                    && ids.entityCount() == worldEntityKindState.snapshot().entityCount()) {
                try {
                    consistent = ids.matches(tickUuidWorld.customMcLoadedEntityUuids());
                } catch (RuntimeException invalidEvidence) {
                    consistent = false;
                }
            }
            if (!worldEntityDownstreamSourceStillOwned(world) || !consistent) {
                return false;
            }
        }
        if (featureCatalog.wallCheck().active()) {
            final Minecraft189WorldEntityVisibilityState.Snapshot visibility =
                    worldEntityVisibilityState.snapshot();
            boolean consistent = false;
            if (tickVisibilityWorld != null && tickVisibilityWorld == world
                    && visibility.available()
                    && visibility.entityCount() == worldEntityPositionState.snapshot().entityCount()
                    && visibility.entityCount() == worldEntityKindState.snapshot().entityCount()) {
                try {
                    consistent = visibility.matches(
                            tickVisibilityWorld.customMcLoadedEntityVisibility());
                } catch (RuntimeException invalidEvidence) {
                    consistent = false;
                }
            }
            if (!worldEntityDownstreamSourceStillOwned(world) || !consistent) {
                return false;
            }
        }
        return worldEntityDownstreamSourceStillOwned(world);
    }

    private void rejectInconsistentOriginalCombatSource(
            final Minecraft189WorldEntityCombatAccess world) {
        if (!worldEntityDownstreamSourceStillOwned(world)) {
            clearRevokedWorldEntitySource();
            return;
        }
        worldEntityCombatState.clear();
        nearestPlayerTargetState.clear();
        targetRotationState.clear();
        featureCatalog.killAura().rememberSelectedTarget(
                null, "original-world-combat-inconsistent");
    }

    private boolean worldEntityDownstreamSourceStillOwned(final Object world) {
        return worldEntityDownstreamLease.stillOwned()
                && world != null
                && tickOriginalEntityWorld == world
                && worldEntityPositionState.snapshot().available();
    }

    void serverAddress(
            final Minecraft189ServerDataAccess serverData) {
        requireOpen();
        if (!serverAddressHostLease.enter(serverData)) return;
        try {
            if (serverData == null) {
                serverAddressState.clear();
                return;
            }
            final String address = serverData.customMcServerAddress();
            if (!serverAddressHostLease.stillOwned()
                    || address == null || address.trim().isEmpty()) {
                serverAddressState.clear();
                return;
            }
            // Preserve the original string normalization and validation;
            // only an unchanged real ServerData may publish its address.
            serverAddressState.update(address);
        } catch (RuntimeException | Error nativeFailure) {
            serverAddressState.clear();
            throw nativeFailure;
        } finally {
            serverAddressHostLease.exit();
        }
    }

    void playerHeldItem(
            final Minecraft189PlayerHeldItemAccess player) {
        requireOpen();
        if (!heldItemHostLease.enter(player)) return;
        try {
            // This reference authorizes real item-use and sword checks.
            // A fresh original callback also revokes any in-flight
            // AutoBlock sword read, even if the player is later restored.
            tickHeldItemAccess = null;
            heldItemOwnerSerial++;
            ++criticalsSourceGeneration;
            if (player == null) {
                heldItemState.clear();
                return;
            }
            final Minecraft189ItemStackAccess held =
                    player.customMcHeldItem();
            if (!heldItemHostLease.stillOwned()) {
                heldItemState.clear();
                return;
            }
            if (held == null) {
                // Vanilla empty hand is a VALID original inventory owner:
                // AutoRod/AutoPot can select another genuine hotbar item.
                // The HUD has no held ItemStack, but this verified player
                // must still authorize downstream native selection.
                heldItemState.clear();
                tickHeldItemAccess = player;
                return;
            }
            final String name = held.customMcDisplayName();
            if (!heldItemHostLease.stillOwned()) {
                heldItemState.clear();
                return;
            }
            final int quantity = held.customMcStackSize();
            if (!heldItemHostLease.stillOwned()) {
                heldItemState.clear();
                return;
            }
            final int damage = held.customMcItemDamage();
            if (!heldItemHostLease.stillOwned()) {
                heldItemState.clear();
                return;
            }
            final int maximum = held.customMcMaxDamage();
            if (!heldItemHostLease.stillOwned()) {
                heldItemState.clear();
                return;
            }
            // Existing state validates genuine name, quantity and damage
            // before committing one immutable readback. Never publish a
            // mutable native held-item owner on an invalid original item.
            heldItemState.update(name, quantity, damage, maximum);
            if (!heldItemHostLease.stillOwned()) {
                heldItemState.clear();
                return;
            }
            tickHeldItemAccess = player;
        } catch (RuntimeException | Error nativeFailure) {
            tickHeldItemAccess = null;
            heldItemState.clear();
            throw nativeFailure;
        } finally {
            heldItemHostLease.exit();
        }
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
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void playerControllerBreakControl(
            final Minecraft189BlockHitDelayControl controller) {
        requireOpen();
        featureCatalog.fastBreak()
                .apply(
                        controller,
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.LEFT_BUTTON),
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void playerControllerMiningControl(
            final Minecraft189BlockMiningControl controller) {
        requireOpen();
        featureCatalog.speedMine()
                .apply(
                        controller,
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.LEFT_BUTTON),
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    void timerSpeedControl(
            final Minecraft189TimerSpeedControl timer) {
        requireOpen();
        featureCatalog.timerSpeed()
                .apply(
                        timer,
                        playerMovementState.snapshot(),
                        clickGuiRuntime.coreRuntime().model().snapshot().open());
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

    // All mapped EntityRenderer.getMouseOver sites share the same actual
    // GUI input owner. While UI-focused, preserve the caller's exact
    // vanilla raycast branch, distance and source entity border.
    float raycastBlockDistance(final float nativeDistance) {
        requireOpen();
        return featureCatalog.reach().raycastBlockDistance(
                nativeDistance,
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    boolean raycastExtendedBranch(final boolean nativeExtended) {
        requireOpen();
        return featureCatalog.reach().raycastExtendedBranch(
                nativeExtended,
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    double raycastExtendedDistance(final double nativeDistance) {
        requireOpen();
        return featureCatalog.reach().raycastExtendedDistance(
                nativeDistance,
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    float raycastHitboxBorder(
            final boolean playerCandidate, final float nativeBorder) {
        requireOpen();
        return featureCatalog.hitbox().adjustNativeBorder(
                playerCandidate, nativeBorder,
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    /** Source-mapped held item identity; no display-name matching. */
    private boolean holdingVerifiedSword() {
        final Minecraft189PlayerHeldItemAccess player = tickHeldItemAccess;
        if (player == null || !heldItemHostLease.enter(player)) return false;
        final long sourceSerial = heldItemOwnerSerial;
        try {
            // A real sword check reads TWO fresh native mapped values,
            // either of which may synchronously reenter the original
            // HeldItem callback or a new local-player position tick.
            final Minecraft189ItemStackAccess held =
                    player.customMcHeldItem();
            if (!swordSourceStillOwned(player, sourceSerial)) {
                revokeSwordSourceIfCurrent(player, sourceSerial);
                return false;
            }
            if (held == null) {
                // Legitimate vanilla empty hand keeps original player
                // inventory ownership for AutoRod/AutoPot.
                heldItemState.clear();
                return false;
            }
            final boolean actualSword = held.customMcIsSword();
            if (!swordSourceStillOwned(player, sourceSerial)) {
                revokeSwordSourceIfCurrent(player, sourceSerial);
                return false;
            }
            return actualSword;
        } catch (RuntimeException | Error nativeFailure) {
            revokeSwordSourceIfCurrent(player, sourceSerial);
            throw nativeFailure;
        } finally {
            heldItemHostLease.exit();
        }
    }

    private boolean swordSourceStillOwned(
            final Minecraft189PlayerHeldItemAccess original,
            final long sourceSerial) {
        return heldItemHostLease.stillOwned()
                && tickHeldItemAccess == original
                && heldItemOwnerSerial == sourceSerial;
    }

    private void revokeSwordSourceIfCurrent(
            final Minecraft189PlayerHeldItemAccess original,
            final long sourceSerial) {
        // Never destroy a NEW original-player snapshot published during
        // an older native getter. Only revoke the actual stale owner.
        if (tickHeldItemAccess == original
                && heldItemOwnerSerial == sourceSerial) {
            tickHeldItemAccess = null;
            heldItemOwnerSerial++;
            heldItemState.clear();
        }
    }

    /** One non-reentrant native combat callback may own one primary lane. */
    long enterSyntheticCombatCycle() {
        requireOpen();
        return combatCycle.enter();
    }

    boolean claimSyntheticCombatPrimary(final long token,
            final Minecraft189CombatCycleArbiter.Primary primary) {
        requireOpen();
        return combatCycle.claimPrimary(token, primary);
    }

    boolean claimSyntheticCombatReblock(final long token) {
        requireOpen();
        return combatCycle.claimReblock(token);
    }

    void leaveSyntheticCombatCycle(final long token) {
        // No native controller authorization survives an action callback,
        // including an early return, original click exception or GUI veto.
        autoClickerControllerLease.clear();
        combatCycle.leave(token);
    }

    boolean shouldReleaseAutoBlock(final boolean playerHit,
            final int hitIndex) {
        requireOpen();
        return featureCatalog.autoBlock().shouldStop(
                clickGuiRuntime.coreRuntime().model().snapshot().open(),
                inputState.pointerPressed(Minecraft189ClickRateTracker.RIGHT_BUTTON),
                holdingVerifiedSword(),
                playerHit && worldEntityCombatState.snapshot().alive(hitIndex));
    }

    boolean releaseAutoBlockBeforeAction() {
        requireOpen();
        return featureCatalog.autoBlock().releaseForAction(
                inputState.pointerPressed(Minecraft189ClickRateTracker.RIGHT_BUTTON));
    }

    boolean mayStartAutoBlock(final boolean playerHit, final int hitIndex,
            final int previousCombatSlot, final boolean itemAlreadyInUse) {
        requireOpen();
        return featureCatalog.autoBlock().mayStart(
                holdingVerifiedSword(),
                playerHit && worldEntityCombatState.snapshot().alive(hitIndex),
                clickGuiRuntime.coreRuntime().model().snapshot().open(),
                inputState.pointerPressed(Minecraft189ClickRateTracker.RIGHT_BUTTON),
                previousCombatSlot >= 0,
                itemAlreadyInUse);
    }

    long beginAutoBlockNativeUse() {
        requireOpen();
        // V340: existing mayStart checks the original sword, but this
        // reservation must not invoke an additional native HeldItem getter.
        // Keep original callback ordering; bind the actual current owner
        // serial at reservation and revalidate it AFTER right-click returns.
        return featureCatalog.autoBlock().beginNativeUse(
                heldItemOwnerSerial);
    }

    /**
     * V360: pre-vanilla-rightClickMouse authority for this exact original
     * sword reservation. Both mapped native getter calls in
     * holdingVerifiedSword() may reenter playerHeldItem(), so require the
     * SAME held-item owner generation from before and after those reads.
     *
     * No new right click, controller call or packet is created here.
     */
    boolean mayDispatchAutoBlockNativeUse(final long useToken) {
        requireOpen();
        if (useToken == 0L) return false;
        final long ownerBeforeRead = heldItemOwnerSerial;
        final boolean nativeSwordStillHeld = holdingVerifiedSword();
        return nativeSwordStillHeld
                && ownerBeforeRead == heldItemOwnerSerial
                && featureCatalog.autoBlock().permitsNativeUseDispatch(
                        useToken, nativeSwordStillHeld,
                        clickGuiRuntime.coreRuntime().model().snapshot().open(),
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.RIGHT_BUTTON),
                        heldItemOwnerSerial);
    }

    /** Final cleanup: never send native item release or revoke newer lease. */
    void cancelAutoBlockNativeUse(final long useToken) {
        featureCatalog.autoBlock().cancelPendingNativeUse(useToken);
    }

    void recordAutoBlockStart(final long useToken) {
        requireOpen();
        // The native isUsingItem result was already observed in the bridge.
        // This second source check may itself trigger a NEW HeldItem callback.
        // Never allow a sword of the same TYPE from a newer callback to
        // commit an older synthetic use-item lease.
        final boolean currentSword = holdingVerifiedSword();
        featureCatalog.autoBlock().completeNativeUse(
                useToken, currentSword,
                clickGuiRuntime.coreRuntime().model().snapshot().open(),
                inputState.pointerPressed(
                        Minecraft189ClickRateTracker.RIGHT_BUTTON),
                heldItemOwnerSerial);
    }

    private boolean identityRequired() {
        return Boolean.getBoolean("custommc.acceptance.reportKillAuraRejoin")
                || Boolean.getBoolean("custommc.acceptance.reportKillAuraLockedTarget")
                || Boolean.getBoolean("custommc.acceptance.reportKillAuraSimultaneous")
                || Boolean.getBoolean("custommc.acceptance.reportKillAuraMultiTarget")
                // M422 native outgoing UUID guard needs the exact-world UUID
                // evidence for EVERY active Aura-owned attack, even when
                // sticky target locking is disabled (default local PvP).
                // Without it, an unlocked Aura would fail closed forever.
                || featureCatalog.killAura().active()
                // TriggerBot also needs the exact current loaded-player UUID;
                // an index alone can be reused across unload/rejoin events.
                || featureCatalog.triggerBot().active()
                // The isolated ORIGINAL game negative acceptance must read
                // actual native UUIDs while TriggerBot itself is still OFF.
                // No ordinary client runtime enables this property.
                || Boolean.getBoolean(
                        "custommc.acceptance.reportTriggerBotRealPvp")
                || featureCatalog.friendGuard().requiresIdentity()
                || featureCatalog.wallCheck().active();
    }

    /**
     * Final target-specific UUID veto for synthetic Combat operations.
     * Current world list is rechecked against the captured identity sequence.
     */
    private boolean friendTargetPermits(final int candidateIndex) {
        final Minecraft189FriendGuardModule friends = featureCatalog.friendGuard();
        if (!friends.requiresIdentity()) return true;
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                worldEntityUuidState.snapshot();
        if (tickUuidWorld == null || !ids.available()
                || ids.entityCount() != worldEntityPositionState.snapshot().entityCount()
                || ids.entityCount() != worldEntityKindState.snapshot().entityCount()
                || ids.entityCount() != worldEntityCombatState.snapshot().entityCount()) {
            return false;
        }
        try {
            return ids.matches(tickUuidWorld.customMcLoadedEntityUuids())
                    && friends.permits(candidateIndex, ids);
        } catch (RuntimeException invalidEvidence) {
            return false;
        }
    }

    /** Native current-tick LOS evidence and exact UUID/list parity. */
    private boolean wallTargetPermits(final int index) {
        final Minecraft189WallCheckModule wall = featureCatalog.wallCheck();
        if (!wall.active()) return true;
        final Minecraft189WorldEntityVisibilityState.Snapshot evidence =
                worldEntityVisibilityState.snapshot();
        final Minecraft189WorldEntityUuidState.Snapshot ids =
                worldEntityUuidState.snapshot();
        if (!evidence.available() || !ids.available()
                || tickVisibilityWorld == null || tickUuidWorld == null
                || !wall.permits(index, evidence)
                || evidence.entityCount() != ids.entityCount()
                || evidence.entityCount() != worldEntityPositionState.snapshot().entityCount()
                || evidence.entityCount() != worldEntityKindState.snapshot().entityCount()
                || evidence.entityCount() != worldEntityCombatState.snapshot().entityCount()) {
            return false;
        }
        try {
            final int[] current = tickVisibilityWorld.customMcLoadedEntityVisibility();
            return current != null && index >= 0 && index < current.length
                    && current.length == evidence.entityCount()
                    && current[index] == 1
                    && ids.matches(tickUuidWorld.customMcLoadedEntityUuids());
        } catch (RuntimeException invalidEvidence) {
            return false;
        }
    }

    /** Eligibility captured immediately before the native synthetic click. */
    boolean shouldAutoPot(final boolean crosshairPlayer,
            final int crosshairPlayerIndex) {
        requireOpen();
        final Minecraft189AutoPotModule pot = featureCatalog.autoPot();
        if (clickGuiRuntime.coreRuntime().model().snapshot().open()
                || tickHotbarControl == null) {
            pot.suspend();
            return false;
        }
        final Minecraft189WorldEntityCombatState.Snapshot combat =
                worldEntityCombatState.snapshot();
        final boolean verifiedTarget = crosshairPlayer
                && crosshairPlayerIndex >= 0
                && combat.alive(crosshairPlayerIndex)
                && featureCatalog.antiBot().permits(crosshairPlayerIndex, combat)
                && featureCatalog.teamGuard().permits(crosshairPlayerIndex, combat)
                && friendTargetPermits(crosshairPlayerIndex)
                && wallTargetPermits(crosshairPlayerIndex);
        return pot.shouldUse(playerHealthState.snapshot(), verifiedTarget,
                inputState.pointerPressed(Minecraft189ClickRateTracker.RIGHT_BUTTON),
                false, true);
    }

    /**
     * The transformed InventoryPlayer and source held ItemStack must be
     * supplied by the SAME genuine local player object in this tick.
     * Merely matching item type in a different respawned source player
     * is never proof that this inventory owns the native use action.
     */
    private boolean sameLocalHotbarAndHeldItemOwner() {
        return tickHotbarPlayer != null && tickHeldItemAccess != null
                && tickHotbarPlayer == (Object) tickHeldItemAccess;
    }

    private boolean combatHotbarBusy() {
        return combatHotbarSelecting || autoPotLeaseInventory != null
                || autoRodLeaseInventory != null
                || combatSlotLeaseInventory != null;
    }

    private boolean mayUseRegenerationPotionWithoutWaste() {
        if (!featureCatalog.autoPot().avoidActiveRegenerationSetting().get())
            return true;
        // No potion-effects snapshot from a previous tick, respawn or
        // another player can be borrowed for this native right-click.
        return tickHotbarControl != null
                && tickHotbarPlayer != null
                && Minecraft189AutoPotRegenerationGuard.mayUse(
                        tickHotbarControl, tickHotbarPlayer);
    }

    private boolean autoPotConservationEnabled() {
        final Minecraft189AutoPotModule pot = featureCatalog.autoPot();
        return pot.autoDetectSlotSetting().get().booleanValue()
                && pot.preferImmediateHealingSetting().get().booleanValue()
                && pot.conserveInstantHealingIISetting().get().booleanValue();
    }

    private boolean sourceAutoPotHealthLeaseValid() {
        if (autoPotConservationEnabled() != autoPotHealthLeaseConserving)
            return false; // Changing the ranking setting revokes the lease.
        if (!autoPotHealthLeaseConserving)
            return autoPotHealthLeaseOwner == null;
        return autoPotHealthLeaseOwner != null
                && tickHotbarPlayer == autoPotHealthLeaseOwner
                && tickHeldItemAccess == autoPotHealthLeaseOwner
                && autoPotHealthLeaseOwner.customMcMaxHealth()
                        == autoPotHealthLeaseMaximum
                && autoPotHealthLeaseOwner.customMcHealth()
                        == autoPotHealthLeaseHealth
                && tickHotbarPlayer == autoPotHealthLeaseOwner;
    }

    int selectAutoPotSlot() {
        requireOpen();
        if (combatHotbarBusy() || !sameLocalHotbarAndHeldItemOwner()
                || clickGuiRuntime.coreRuntime().model().snapshot().open())
            return -1;
        final Minecraft189InventoryHotbarControl inventory = tickHotbarControl;
        final Minecraft189PlayerInventoryAccess owner = tickHotbarPlayer;
        final Minecraft189PlayerHeldItemAccess held = tickHeldItemAccess;
        final boolean conserve = autoPotConservationEnabled();
        final Minecraft189PlayerHealthAccess healthOwner =
                conserve && owner instanceof Minecraft189PlayerHealthAccess
                    && held == owner
                        ? (Minecraft189PlayerHealthAccess) owner : null;
        if (conserve && healthOwner == null) return -1;
        final float observedMaximum = conserve
                ? healthOwner.customMcMaxHealth() : Float.NaN;
        final float observedHealth = conserve
                ? healthOwner.customMcHealth() : Float.NaN;
        if (conserve && (!Float.isFinite(observedMaximum)
                || !Float.isFinite(observedHealth)
                || observedMaximum <= 0F || observedHealth <= 0F
                || observedHealth >= observedMaximum))
            return -1;
        combatHotbarSelecting = true;
        try {
            final int prior = Minecraft189VerifiedCombatItemSlot.selectPotion(
                    featureCatalog.autoPot(), inventory, held);
            if (prior < 0) return -1;
            if (inventory != tickHotbarControl || owner != tickHotbarPlayer
                    || held != tickHeldItemAccess
                    || conserve != autoPotConservationEnabled()
                    || (conserve
                        && (healthOwner.customMcMaxHealth() != observedMaximum
                            || healthOwner.customMcHealth() != observedHealth))
                    || !sameLocalHotbarAndHeldItemOwner()
                    || !mayUseRegenerationPotionWithoutWaste()
                    || clickGuiRuntime.coreRuntime().model().snapshot().open()) {
                featureCatalog.autoPot().restoreSlot(inventory, prior);
                return -1;
            }
            autoPotLeaseInventory = inventory;
            autoPotLeaseOriginal = prior;
            autoPotHealthLeaseConserving = conserve;
            autoPotHealthLeaseOwner = healthOwner;
            autoPotHealthLeaseHealth = observedHealth;
            autoPotHealthLeaseMaximum = observedMaximum;
            return prior;
        } finally {
            combatHotbarSelecting = false;
        }
    }

    boolean mayExecuteAutoPotUse(final int originalSlot) {
        requireOpen();
        return !clickGuiRuntime.coreRuntime().model().snapshot().open()
                && sourceAutoPotHealthLeaseValid()
                && mayUseRegenerationPotionWithoutWaste()
                && sameLocalHotbarAndHeldItemOwner()
                && autoPotLeaseInventory != null
                && tickHotbarControl == autoPotLeaseInventory
                && Minecraft189VerifiedCombatItemSlot.stillOwnsPotionAtUse(
                        featureCatalog.autoPot(), autoPotLeaseInventory,
                        tickHeldItemAccess, originalSlot)
                && sourceAutoPotHealthLeaseValid();
    }

    /**
     * Final production-only original potion authorization. Read-only V132
     * native /give source-potion discovery still calls mayExecuteAutoPotUse
     * without demanding low health. A real synthetic rightClickMouse must
     * additionally witness its EXACT original player currently low on HP
     * and no physical RMB takeover.
     *
     * The mapped health getters and full item validation can synchronously
     * reenter source/GUI/policy callbacks; bracket both with identity and
     * unchanged native health readbacks, never dispatch on a borrowed
     * health HUD snapshot or another respawned EntityPlayer.
     */
    boolean mayDispatchAutoPotNativeUse(final int originalSlot) {
        requireOpen();
        if (!mayExecuteAutoPotUse(originalSlot)
                || inputState.pointerPressed(
                        Minecraft189ClickRateTracker.RIGHT_BUTTON))
            return false;
        final Minecraft189PlayerInventoryAccess originalPlayer =
                tickHotbarPlayer;
        final Minecraft189PlayerHeldItemAccess originalHeld =
                tickHeldItemAccess;
        final Minecraft189InventoryHotbarControl inventory =
                autoPotLeaseInventory;
        if (originalPlayer == null || originalPlayer != (Object) originalHeld
                || !(originalPlayer instanceof Minecraft189PlayerHealthAccess)
                || inventory == null || tickHotbarControl != inventory
                || autoPotLeaseOriginal != originalSlot)
            return false;
        final Minecraft189PlayerHealthAccess live =
                (Minecraft189PlayerHealthAccess) originalPlayer;
        final float maximum = live.customMcMaxHealth();
        if (tickHotbarPlayer != originalPlayer
                || tickHeldItemAccess != originalHeld
                || tickHotbarControl != inventory
                || autoPotLeaseInventory != inventory)
            return false;
        final float health = live.customMcHealth();
        if (tickHotbarPlayer != originalPlayer
                || tickHeldItemAccess != originalHeld
                || tickHotbarControl != inventory
                || autoPotLeaseInventory != inventory
                || !featureCatalog.autoPot().permitsLiveNativeHealth(
                        health, maximum))
            return false;
        // Native potion ItemStack / currentItem validation occurs AFTER
        // health getters; they can change the item, GUI or policy. Read the
        // current health again after item verification as well, so later
        // reentrant native item reads cannot borrow an old low-HP witness.
        if (!mayExecuteAutoPotUse(originalSlot)) return false;
        final float finalMaximum = live.customMcMaxHealth();
        final float finalHealth = live.customMcHealth();
        return finalMaximum == maximum && finalHealth == health
                && tickHotbarPlayer == originalPlayer
                && tickHeldItemAccess == originalHeld
                && tickHotbarControl == inventory
                && autoPotLeaseInventory == inventory
                && autoPotLeaseOriginal == originalSlot
                && !inputState.pointerPressed(
                        Minecraft189ClickRateTracker.RIGHT_BUTTON)
                && !clickGuiRuntime.coreRuntime().model().snapshot().open()
                && featureCatalog.autoPot().permitsLiveNativeHealth(
                        finalHealth, finalMaximum);
    }

    /**
     * The verified native potion owns currentItem first. Opt-in pitch
     * adjustment is subordinate to the SAME real local player lease, not
     * a cached look vector or target-player rotation.
     */
    Minecraft189AutoPotPitchLease beginAutoPotDownwardPitch(
            final int originalSlot) {
        requireOpen();
        if (!featureCatalog.autoPot().aimDownForSplashSetting().get())
            return null; // Legacy native orientation unchanged.
        if (!mayExecuteAutoPotUse(originalSlot))
            return Minecraft189AutoPotPitchLease.begin(null);
        return Minecraft189AutoPotPitchLease.begin(
                tickHotbarPlayer instanceof Minecraft189PlayerRotationControl
                        ? (Minecraft189PlayerRotationControl) tickHotbarPlayer
                        : null);
    }

    boolean mayExecuteAutoPotAtPitch(
            final Minecraft189AutoPotPitchLease lease,
            final int originalSlot) {
        requireOpen();
        if (!mayExecuteAutoPotUse(originalSlot)) return false;
        if (lease == null)
            return !featureCatalog.autoPot().aimDownForSplashSetting().get();
        return featureCatalog.autoPot().aimDownForSplashSetting().get()
                && tickHotbarPlayer instanceof Minecraft189PlayerRotationControl
                && lease.mayThrow(
                        (Minecraft189PlayerRotationControl) tickHotbarPlayer)
                && mayExecuteAutoPotUse(originalSlot);
    }

    void restoreAutoPotSlot(final int originalSlot) {
        requireOpen();
        if (autoPotLeaseInventory == null || originalSlot < 0
                || autoPotLeaseOriginal != originalSlot) return;
        final Minecraft189InventoryHotbarControl owned = autoPotLeaseInventory;
        autoPotLeaseInventory = null;
        autoPotLeaseOriginal = -1;
        autoPotHealthLeaseOwner = null;
        autoPotHealthLeaseHealth = Float.NaN;
        autoPotHealthLeaseMaximum = Float.NaN;
        autoPotHealthLeaseConserving = false;
        featureCatalog.autoPot().restoreSlot(owned, originalSlot);
    }

    boolean commitAutoPotUseAttempt(final int originalSlot) {
        requireOpen();
        return autoPotLeaseInventory != null
                && tickHotbarControl == autoPotLeaseInventory
                && autoPotLeaseOriginal == originalSlot
                && featureCatalog.autoPot().commitCompletedOriginalUse(
                        autoPotLeaseInventory, originalSlot);
    }

    private boolean mayCastWithoutReelingActiveHook() {
        if (!featureCatalog.autoRod().preserveActiveHookSetting().get())
            return true;
        return tickHotbarPlayer instanceof Minecraft189PlayerFishingHookAccess
                && !((Minecraft189PlayerFishingHookAccess) tickHotbarPlayer)
                        .customMcHasFishingHook();
    }

    boolean shouldAutoRod(final boolean confirmedPlayer,
            final int verifiedPlayerIndex) {
        requireOpen();
        final Minecraft189AutoRodModule rod = featureCatalog.autoRod();
        if (clickGuiRuntime.coreRuntime().model().snapshot().open()
                || tickHotbarControl == null
                || !mayCastWithoutReelingActiveHook() || !confirmedPlayer
                || verifiedPlayerIndex < 0
                || !worldEntityCombatState.snapshot().alive(verifiedPlayerIndex)
                || !featureCatalog.antiBot().permits(
                        verifiedPlayerIndex, worldEntityCombatState.snapshot())
                || !featureCatalog.teamGuard().permits(
                        verifiedPlayerIndex, worldEntityCombatState.snapshot())
                || !friendTargetPermits(verifiedPlayerIndex)
                || !wallTargetPermits(verifiedPlayerIndex)
                || (featureCatalog.killAura().active()
                    && (!targetRotationState.snapshot().available()
                        || targetRotationState.snapshot().entityIndex()
                                != verifiedPlayerIndex))) {
            rod.suspend();
            return false;
        }
        return rod.shouldUse(true,
                inputState.pointerPressed(Minecraft189ClickRateTracker.LEFT_BUTTON),
                inputState.pointerPressed(Minecraft189ClickRateTracker.RIGHT_BUTTON),
                false, true);
    }

    int selectAutoRodSlot() {
        requireOpen();
        if (combatHotbarBusy() || !sameLocalHotbarAndHeldItemOwner()
                || !mayCastWithoutReelingActiveHook()
                || clickGuiRuntime.coreRuntime().model().snapshot().open())
            return -1;
        final Minecraft189InventoryHotbarControl inventory = tickHotbarControl;
        final Minecraft189PlayerInventoryAccess owner = tickHotbarPlayer;
        final Minecraft189PlayerHeldItemAccess held = tickHeldItemAccess;
        combatHotbarSelecting = true;
        try {
            final int prior = Minecraft189VerifiedCombatItemSlot.selectRod(
                    featureCatalog.autoRod(), inventory, held);
            if (prior < 0) return -1;
            if (inventory != tickHotbarControl || owner != tickHotbarPlayer
                    || held != tickHeldItemAccess
                    || !sameLocalHotbarAndHeldItemOwner()
                    || !mayCastWithoutReelingActiveHook()
                    || clickGuiRuntime.coreRuntime().model().snapshot().open()) {
                featureCatalog.autoRod().restoreSlot(inventory, prior);
                return -1;
            }
            autoRodLeaseInventory = inventory;
            autoRodLeaseOriginal = prior;
            return prior;
        } finally {
            combatHotbarSelecting = false;
        }
    }

    boolean mayExecuteAutoRodUse(final int originalSlot) {
        requireOpen();
        // V361: read-only original rod identity / live source verification.
        // This method is also used by the genuine Mojang V132 /give
        // discovery acceptance without physical LMB. Native dispatch must
        // additionally call mayDispatchAutoRodNativeUse(), which requires
        // real input and rechecks ownership before rightClickMouse.
        final Minecraft189InventoryHotbarControl inventory =
                autoRodLeaseInventory;
        final Minecraft189PlayerInventoryAccess originalPlayer =
                tickHotbarPlayer;
        final Minecraft189PlayerHeldItemAccess originalHeld =
                tickHeldItemAccess;
        final long originalHeldGeneration = heldItemOwnerSerial;
        final Minecraft189AutoRodModule rod = featureCatalog.autoRod();
        if (originalSlot < 0 || originalSlot > 8
                || inventory == null || autoRodLeaseOriginal != originalSlot
                || tickHotbarControl != inventory
                || originalPlayer == null || originalHeld == null
                || originalPlayer != (Object) originalHeld
                || clickGuiRuntime.coreRuntime().model().snapshot().open())
            return false;

        // Original fishEntity and original ItemStack reads can synchronously
        // reenter playerHeldItem/playerHotbarSlot. Keep the exact source
        // generation and both original owner references across ALL reads.
        if (!mayCastWithoutReelingActiveHook()
                || heldItemOwnerSerial != originalHeldGeneration
                || tickHotbarControl != inventory
                || tickHotbarPlayer != originalPlayer
                || tickHeldItemAccess != originalHeld)
            return false;
        if (!Minecraft189VerifiedCombatItemSlot.stillOwnsRodAtUse(
                rod, inventory, originalHeld, originalSlot))
            return false;
        return heldItemOwnerSerial == originalHeldGeneration
                && tickHotbarControl == inventory
                && tickHotbarPlayer == originalPlayer
                && tickHeldItemAccess == originalHeld
                && autoRodLeaseInventory == inventory
                && autoRodLeaseOriginal == originalSlot
                && !clickGuiRuntime.coreRuntime().model().snapshot().open();
    }

    /**
     * Actual rightClickMouse dispatch, NOT read-only source rod discovery.
     * An original-game V132 /give ItemStack acceptance must be able to
     * verify its native rod with no LMB pressed; such a verification does
     * not authorize a synthetic click. Only this final production gate
     * requires the current physical LMB/RMB policy, as a live input fact.
     */
    boolean mayDispatchAutoRodNativeUse(final int originalSlot) {
        requireOpen();
        final Minecraft189AutoRodModule rod = featureCatalog.autoRod();
        if (!mayExecuteAutoRodUse(originalSlot))
            return false;
        return (!rod.requireHoldSetting().get()
                    || inputState.pointerPressed(
                            Minecraft189ClickRateTracker.LEFT_BUTTON))
                && (!rod.pauseRightSetting().get()
                    || !inputState.pointerPressed(
                            Minecraft189ClickRateTracker.RIGHT_BUTTON))
                && !clickGuiRuntime.coreRuntime().model().snapshot().open()
                && mayExecuteAutoRodUse(originalSlot)
                && (!rod.requireHoldSetting().get()
                    || inputState.pointerPressed(
                            Minecraft189ClickRateTracker.LEFT_BUTTON))
                && (!rod.pauseRightSetting().get()
                    || !inputState.pointerPressed(
                            Minecraft189ClickRateTracker.RIGHT_BUTTON));
    }

    void restoreAutoRodSlot(final int originalSlot) {
        requireOpen();
        if (autoRodLeaseInventory == null || originalSlot < 0
                || autoRodLeaseOriginal != originalSlot) return;
        final Minecraft189InventoryHotbarControl owned = autoRodLeaseInventory;
        autoRodLeaseInventory = null;
        autoRodLeaseOriginal = -1;
        featureCatalog.autoRod().restoreSlot(owned, originalSlot);
    }

    boolean commitAutoRodUseAttempt(final int originalSlot) {
        requireOpen();
        return autoRodLeaseInventory != null
                && tickHotbarControl == autoRodLeaseInventory
                && autoRodLeaseOriginal == originalSlot
                && featureCatalog.autoRod().commitCompletedOriginalUse(
                        autoRodLeaseInventory, originalSlot);
    }

    int selectCombatSlotBeforeSyntheticClick(final boolean playerHit) {
        requireOpen();
        // A nested native combat callback must never replace the original
        // captured inventory or module owner before the first finally.
        if (combatHotbarBusy()) return -1;
        final Minecraft189InventoryHotbarControl source = tickHotbarControl;
        final Minecraft189PlayerInventoryAccess owner = tickHotbarPlayer;
        final boolean weaponOwner = featureCatalog.autoWeapon().active();
        final boolean guiOpen =
                clickGuiRuntime.coreRuntime().model().snapshot().open();
        combatHotbarSelecting = true;
        try {
            // Explicit sword-ranking module wins over fixed CombatSlot.
            // Unknown verified sword content never falls through blindly.
            final int prior = weaponOwner
                    ? featureCatalog.autoWeapon().select(source, playerHit, guiOpen)
                    : featureCatalog.combatSlot().select(source, playerHit, guiOpen);
            if (prior < 0) return -1;
            if (source != tickHotbarControl || owner != tickHotbarPlayer
                    || guiOpen != clickGuiRuntime.coreRuntime().model()
                            .snapshot().open()) {
                if (weaponOwner)
                    featureCatalog.autoWeapon().restore(source, prior);
                else
                    featureCatalog.combatSlot().restore(source, prior);
                return -1;
            }
            // Capturing a temporary slot does not by itself authorize the
            // eventual original attackEntity/clickMouse dispatch. Retain
            // the native selected-slot evidence for the final boundary.
            final int selected = source.customMcSelectedHotbarSlot();
            if (selected < 0 || selected > 8 || selected == prior
                    || source != tickHotbarControl || owner != tickHotbarPlayer
                    || guiOpen != clickGuiRuntime.coreRuntime().model()
                            .snapshot().open()) {
                if (weaponOwner)
                    featureCatalog.autoWeapon().restore(source, prior);
                else
                    featureCatalog.combatSlot().restore(source, prior);
                return -1;
            }
            combatSlotLeaseInventory = source;
            combatSlotLeasePlayer = owner;
            combatSlotLeaseOriginal = prior;
            combatSlotLeaseSelected = selected;
            autoWeaponOwnedClick = weaponOwner;
            return prior;
        } finally {
            combatHotbarSelecting = false;
        }
    }

    /**
     * Gate the *actual* original Mojang click/attackEntity dispatch, not
     * just a preceding synthetic hotbar switch. A nested click which
     * failed to acquire the only available lease cannot still attack
     * with its predecessor's currently selected rod, potion or sword.
     */
    boolean mayDispatchSyntheticMelee(final int originalSlot) {
        requireOpen();
        if (clickGuiRuntime.coreRuntime().model().snapshot().open())
            return false;
        if (originalSlot < 0) {
            // No switch was needed: vanilla click semantics remain, but
            // they must not run while another source action owns currentItem.
            return !combatHotbarBusy();
        }
        if (combatHotbarSelecting || combatSlotLeaseInventory == null
                || combatSlotLeaseOriginal != originalSlot
                || combatSlotLeasePlayer != tickHotbarPlayer
                || combatSlotLeaseInventory != tickHotbarControl
                || combatSlotLeaseSelected < 0
                || !(autoWeaponOwnedClick
                        ? featureCatalog.autoWeapon().active()
                        : featureCatalog.combatSlot().active()))
            return false;
        final boolean selected =
                combatSlotLeaseInventory.customMcSelectedHotbarSlot()
                        == combatSlotLeaseSelected;
        if (!selected) return false;
        // AutoWeapon is selected by *mapped native sword class*, not just
        // an inventory index. Vanilla ItemStacks can change in-place after
        // selection and before actual original controller dispatch.
        if (autoWeaponOwnedClick
                && !featureCatalog.autoWeapon().ownsVerifiedSwordAtDispatch(
                        combatSlotLeaseInventory, originalSlot))
            return false;
        // Unlike AutoWeapon's typed ItemSword, fixed CombatSlot's original
        // slot authorization is a live GUI policy. Do not attack using an
        // old selected currentItem after preferredSlot/requireHit edits or
        // disable->enable, even when the same original slot remains set.
        if (!autoWeaponOwnedClick
                && !featureCatalog.combatSlot().ownsVerifiedSlotAtDispatch(
                        combatSlotLeaseInventory, originalSlot))
            return false;
        return combatSlotLeasePlayer == tickHotbarPlayer
                && combatSlotLeaseInventory == tickHotbarControl
                && combatSlotLeaseInventory.customMcSelectedHotbarSlot()
                        == combatSlotLeaseSelected
                && !clickGuiRuntime.coreRuntime().model().snapshot().open();
    }

    void restoreCombatSlotAfterSyntheticClick(final int originalSlot) {
        requireOpen();
        // A nested failed selection returns -1. Its finally must not release
        // the outer original player's valid lease.
        if (originalSlot < 0 || combatSlotLeaseInventory == null
                || originalSlot != combatSlotLeaseOriginal) return;
        final Minecraft189InventoryHotbarControl captured =
                combatSlotLeaseInventory;
        final boolean weaponOwner = autoWeaponOwnedClick;
        combatSlotLeaseInventory = null;
        combatSlotLeasePlayer = null;
        combatSlotLeaseOriginal = -1;
        combatSlotLeaseSelected = -1;
        autoWeaponOwnedClick = false;
        if (weaponOwner) {
            featureCatalog.autoWeapon().restore(captured, originalSlot);
        } else {
            featureCatalog.combatSlot().restore(captured, originalSlot);
        }
    }

    boolean shouldKeepSprintAfterSyntheticClick(final boolean playerHit) {
        requireOpen();
        keepSprintRestoreLease.revoke();
        // A rejected click must never leave permission for a later
        // unrelated source click or a new local player.
        pendingKeepSprintSource = null;
        final long admission = ++keepSprintAdmissionSerial;
        final Minecraft189KeepSprintModule module = featureCatalog.keepSprint();
        final long lifetime = module.lifecycleEpoch();
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
        final boolean admitted = module.shouldRestore(
                measured.snapshot(), state.customMcSprinting(),
                inputState.keyPressed(LegacyKeyboardCodes.W), playerHit,
                featureCatalog.wTap().active(),
                clickGuiRuntime.coreRuntime().model().snapshot().open());
        if (admission != keepSprintAdmissionSerial
                || module.lifecycleEpoch() != lifetime) {
            // A nested original callback (or module disable/re-enable)
            // invalidates BOTH approvals; no older frame may resurrect it.
            pendingKeepSprintSource = null;
            return false;
        }
        if (admitted && tickSprintControl == current) {
            pendingKeepSprintSource = current;
            pendingKeepSprintLifecycleEpoch = lifetime;
            pendingKeepSprintAdmissionSerial = admission;
            pendingKeepSprintPlayerHit = playerHit;
            pendingKeepSprintForward =
                    module.requireForwardSetting().get().booleanValue();
            pendingKeepSprintRequireHit =
                    module.requirePlayerHitSetting().get().booleanValue();
            pendingKeepSprintPauseSneak =
                    module.pauseWhileSneakingSetting().get().booleanValue();
        }
        return admitted && pendingKeepSprintSource == current
                && pendingKeepSprintLifecycleEpoch == module.lifecycleEpoch()
                && admission == keepSprintAdmissionSerial;
    }

    void restoreSprintAfterSyntheticClick() {
        requireOpen();
        // Consume the exact one-click grant BEFORE any original getter.
        // Even a throwing or reentrant player cannot replay this grant.
        final Minecraft189PlayerSprintControl owner = pendingKeepSprintSource;
        final long admission = pendingKeepSprintAdmissionSerial;
        final long lifetime = pendingKeepSprintLifecycleEpoch;
        final boolean originalPlayerHit = pendingKeepSprintPlayerHit;
        final boolean forwardPolicy = pendingKeepSprintForward;
        final boolean hitPolicy = pendingKeepSprintRequireHit;
        final boolean sneakPolicy = pendingKeepSprintPauseSneak;
        pendingKeepSprintSource = null;
        if (owner == null || owner != tickSprintControl
                || !(owner instanceof Minecraft189PlayerMovementStateAccess)
                || admission != keepSprintAdmissionSerial
                || !keepSprintRestoreLease.enter(owner)) {
            return;
        }
        try {
            final Minecraft189KeepSprintModule module =
                    featureCatalog.keepSprint();
            if (!keepSprintRestoreStillOwned(owner, module, admission,
                    lifetime, forwardPolicy, hitPolicy, sneakPolicy))
                return;

            final Minecraft189PlayerMovementStateAccess state =
                    (Minecraft189PlayerMovementStateAccess) owner;
            // Each of these is a real original Minecraft getter. A getter
            // may reenter a different native player callback, open a GUI,
            // change KeepSprint policy, or enable WTap. Revalidate AFTER
            // every read before using the resulting source evidence.
            final boolean ground = state.customMcOnGround();
            if (!keepSprintRestoreStillOwned(owner, module, admission,
                    lifetime, forwardPolicy, hitPolicy, sneakPolicy))
                return;
            final boolean sneaking = state.customMcSneaking();
            if (!keepSprintRestoreStillOwned(owner, module, admission,
                    lifetime, forwardPolicy, hitPolicy, sneakPolicy))
                return;
            final boolean sprintingNow = state.customMcSprinting();
            if (!keepSprintRestoreStillOwned(owner, module, admission,
                    lifetime, forwardPolicy, hitPolicy, sneakPolicy))
                return;
            // No native setter is necessary if the original vanilla
            // attack did not actually cancel sprint. In particular this
            // avoids a redundant true-write that could reenter AutoSprint.
            if (sprintingNow) return;

            final Minecraft189PlayerMovementState measured =
                    new Minecraft189PlayerMovementState();
            measured.update(ground, sneaking, sprintingNow);
            if (!module.shouldRestore(
                    measured.snapshot(), true,
                    inputState.keyPressed(LegacyKeyboardCodes.W),
                    originalPlayerHit, featureCatalog.wTap().active(),
                    clickGuiRuntime.coreRuntime().model().snapshot().open()))
                return;

            // The last original setter belongs only to this live grant,
            // source player, GUI and module generation. Never restore
            // after a nested callback or when WTap has taken authority.
            if (!keepSprintRestoreStillOwned(owner, module, admission,
                    lifetime, forwardPolicy, hitPolicy, sneakPolicy))
                return;
            owner.customMcSetSprinting(true);
        } finally {
            keepSprintRestoreLease.exit();
        }
    }

    private boolean keepSprintRestoreStillOwned(
            final Minecraft189PlayerSprintControl original,
            final Minecraft189KeepSprintModule module,
            final long admission,
            final long lifecycle,
            final boolean forwardPolicy,
            final boolean hitPolicy,
            final boolean sneakPolicy) {
        return keepSprintRestoreLease.stillOwned(original)
                && original == tickSprintControl
                && keepSprintAdmissionSerial == admission
                && module.lifecycleEpoch() == lifecycle
                && module.active()
                && !featureCatalog.wTap().active()
                && !clickGuiRuntime.coreRuntime().model().snapshot().open()
                && forwardPolicy == module.requireForwardSetting().get()
                && hitPolicy == module.requirePlayerHitSetting().get()
                && sneakPolicy == module.pauseWhileSneakingSetting().get();
    }

    /**
     * A real vanilla GuiScreen takes priority over synthetic combat even
     * when the independent CustomMC ClickGUI is closed. Drop every pending
     * scheduler/Trigger UUID grant and WTap reset, while retaining module
     * enabled settings and physical mouse-button ownership.
     *
     * Called from the existing tokenized combat callback. Its finally
     * releases the combat cycle; this method never forges controller hits.
     */
    void suspendSyntheticCombatForOriginalGui() {
        requireOpen();
        ++triggerDispatchAdmissionSerial;
        criticalsNativeAdmissionSerial = -1L;
        criticalsNativePlayer = null;
        triggerDispatchLease.clear();
        autoClickerControllerLease.clear();
        featureCatalog.autoClicker().suspendForGui();
        featureCatalog.triggerBot().suspend();
        featureCatalog.killAura().suspend();
        featureCatalog.hitSelect().suspendForGui();
        featureCatalog.wTap().suspendForGui(
                inputState.pointerPressed(
                        Minecraft189ClickRateTracker.LEFT_BUTTON));
        pendingKeepSprintSource = null;
        ++keepSprintAdmissionSerial;
        keepSprintRestoreLease.revoke();
    }

    boolean shouldAutoClick() {
        return shouldAutoClickInternal(false, -1, null, false);
    }

    boolean shouldAutoClick(final boolean crosshairPlayerHit) {
        // Unindexed legacy test API; never reached by the original
        // Minecraft autoClick callback, which supplies its ray index.
        return shouldAutoClickInternal(crosshairPlayerHit, -1, null, false);
    }

    boolean shouldAutoClick(final boolean crosshairPlayerHit,
            final int crosshairPlayerIndex) {
        return shouldAutoClick(crosshairPlayerHit, crosshairPlayerIndex, null);
    }

    boolean needsHitboxRangeEvidence() {
        requireOpen();
        return featureCatalog.attackRange().needsNativeHitbox();
    }

    boolean shouldAutoClick(final boolean crosshairPlayerHit,
            final int crosshairPlayerIndex, final double[] nativeHitbox) {
        return shouldAutoClickInternal(crosshairPlayerHit, crosshairPlayerIndex,
                nativeHitbox, true);
    }

    private boolean shouldAutoClickInternal(final boolean crosshairPlayerHit,
            final int crosshairPlayerIndex, final double[] nativeHitbox,
            final boolean requireOriginalTargetUuid) {
        requireOpen();
        // Nested external mapped World getters may reenter this scheduler.
        // An older outer callback cannot revive its stale prior admission.
        final long admissionSerial = ++triggerDispatchAdmissionSerial;
        criticalsNativeAdmissionSerial = -1L;
        criticalsNativePlayer = null;
        triggerDispatchLease.clear();
        autoClickerControllerLease.clear();
        // No automatic attack may cross the native ClickGUI focus boundary.
        // The same rule applies to hold and trigger modes; pending phase
        // is cancelled so closing the GUI cannot replay banked clicks.
        if (clickGuiRuntime.coreRuntime().model().snapshot().open()) {
            featureCatalog.autoClicker().suspendForGui();
            featureCatalog.triggerBot().suspend();
            featureCatalog.killAura().suspend();
            featureCatalog.hitSelect().suspendForGui();
            return false;
        }
        final boolean confirmedHit = crosshairPlayerHit;
        final Minecraft189HitSelectModule hitSelect =
                featureCatalog.hitSelect();
        // Observe EVERY original indexed native scheduler callback, not
        // only clicks scheduled by Aura, TriggerBot or AutoClicker. This
        // allows the true source hurt-time rise and recovery to be seen
        // without manufacturing or banking an extra attack.
        if (requireOriginalTargetUuid && hitSelect.active()
                && hitSelect.requireObservedRecoverySetting().get()) {
            hitSelect.observeOriginalOwner(
                    confirmedHit ? crosshairPlayerIndex : -1,
                    worldEntityUuidState.snapshot(),
                    worldEntityKindState.snapshot(),
                    worldEntityCombatState.snapshot());
        }
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
        java.util.UUID verifiedTriggerUuid = null;
        java.util.UUID verifiedAutoClickerUuid = null;
        long verifiedAutoClickerEpoch = -1L;
        boolean nativeAutoClickerScheduled = false;
        boolean nativeAutoClickerTriggerMode = false;
        long verifiedTriggerEpoch = -1L;
        if (aura.active()) {
            trigger.suspend();
            featureCatalog.autoClicker().suspendForGui();
            // No ray-confirmed player means NO click is possible. Let the
            // existing Aura.shouldClick(false, ...) drop CPS credit without
            // deleting the genuine UUID lock during aim convergence.
            // V44 had evaluated the geometry with index=-1 on every empty
            // ray, and incorrectly invoked suspend(), clearing a valid B
            // lock after original server A disconnected.
            //
            // For EVERY actual ray-confirmed player, check independent
            // source-native position + yaw before Aura click scheduling.
            // A rejected real candidate loses CPS credit, not its separately
            // validated UUID lock. Unknown native evidence fails closed.
            if (confirmedHit && !Minecraft189KillAuraNativeFovGate.permits(
                    crosshairPlayerIndex, aura.fovSetting().get(),
                    playerRotationState.snapshot(),
                    playerPositionState.snapshot(),
                    worldEntityPositionState.snapshot(),
                    worldEntityKindState.snapshot())) {
                aura.vetoSyntheticClick();
                return false;
            }
            click = aura.shouldClick(
                    confirmedHit, crosshairPlayerIndex,
                    playerRotationState.snapshot(),
                    targetRotationState.snapshot(), attackHeld, rightHeld,
                    playerMovementState.snapshot(), featureCatalog.spin().active());
        } else if (trigger.active()) {
            aura.suspend();
            featureCatalog.autoClicker().suspendForGui();
            // Runtime source evidence, NOT a cached index or a synthetic
            // guess: bind TriggerBot's confirmation/click credit to the
            // current ORIGINAL World loaded UUID at this exact crosshair.
            // Snapshot this activation and click policy BEFORE querying
            // the external mapped original World loaded-entity UUIDs.
            final long uuidReadTicket = confirmedHit
                    ? trigger.beginNativeWorldUuidRead() : 0;
            java.util.UUID nativeTarget = null;
            final Minecraft189WorldEntityUuidState.Snapshot identities =
                    worldEntityUuidState.snapshot();
            if (confirmedHit && crosshairPlayerIndex >= 0
                    && tickUuidWorld != null && identities.available()
                    && identities.entityCount() ==
                            worldEntityKindState.snapshot().entityCount()
                    && identities.entityCount() ==
                            worldEntityCombatState.snapshot().entityCount()
                    && crosshairPlayerIndex < identities.entityCount()) {
                try {
                    if (identities.matches(tickUuidWorld.customMcLoadedEntityUuids()))
                        nativeTarget = identities.at(crosshairPlayerIndex);
                } catch (RuntimeException staleNativeWorld) {
                    nativeTarget = null;
                }
            }
            if (confirmedHit && !trigger.completeNativeWorldUuidRead(
                    uuidReadTicket)) {
                // This original World getter may have OFF->ON reentered
                // or edited the GUI policy. Revoke its stale source and
                // all scheduler credit rather than clicking from it.
                trigger.suspend();
                return false;
            }
            if (clickGuiRuntime.coreRuntime().model().snapshot().open()) {
                // The native getter may itself open the ClickGUI.
                trigger.suspend();
                return false;
            }
            // Do not use pre-getter keyboard/pointer state after native
            // World read: manual user input may have changed concurrently.
            click = trigger.shouldClick(
                    confirmedHit,
                    inputState.pointerPressed(
                            Minecraft189ClickRateTracker.LEFT_BUTTON),
                    inputState.pointerPressed(
                            Minecraft189ClickRateTracker.RIGHT_BUTTON),
                    playerMovementState.snapshot(), nativeTarget);
            if (click) {
                verifiedTriggerUuid = nativeTarget;
                verifiedTriggerEpoch = trigger.nativeWorldOwnerEpoch();
            }
        } else {
            aura.suspend();
            trigger.suspend();
            final Minecraft189AutoClickerModule clicker =
                    featureCatalog.autoClicker();
            final boolean uuidBoundTrigger = requireOriginalTargetUuid
                    && clicker.triggerModeSetting().get().booleanValue();
            nativeAutoClickerScheduled = requireOriginalTargetUuid;
            nativeAutoClickerTriggerMode = uuidBoundTrigger;
            if (uuidBoundTrigger) {
                final long ownerEpoch = clicker.nativeOwnerEpoch();
                // The same indexed ray must match a fresh ORIGINAL World
                // loaded UUID list, including a verified player-kind owner.
                final Minecraft189WorldEntityUuidState.Snapshot ids =
                        worldEntityUuidState.snapshot();
                final Minecraft189WorldEntityKindState.Snapshot kinds =
                        worldEntityKindState.snapshot();
                final Minecraft189WorldEntityCombatState.Snapshot targets =
                        worldEntityCombatState.snapshot();
                if (confirmedHit && crosshairPlayerIndex >= 0
                        && tickUuidWorld != null && ids.available()
                        && kinds.available() && targets.available()
                        && ids.entityCount() == kinds.entityCount()
                        && ids.entityCount() == targets.entityCount()
                        && crosshairPlayerIndex < ids.entityCount()
                        && kinds.player(crosshairPlayerIndex)
                        && !kinds.localPlayer(crosshairPlayerIndex)) {
                    try {
                        if (ids.matches(tickUuidWorld.customMcLoadedEntityUuids()))
                            verifiedAutoClickerUuid = ids.at(crosshairPlayerIndex);
                    } catch (RuntimeException invalidNativeWorld) {
                        verifiedAutoClickerUuid = null;
                    }
                }
                if (ownerEpoch != clicker.nativeOwnerEpoch()
                        || !clicker.active()
                        || !clicker.triggerModeSetting().get().booleanValue()
                        || aura.active() || trigger.active()
                        || admissionSerial != triggerDispatchAdmissionSerial
                        || clickGuiRuntime.coreRuntime().model().snapshot().open()) {
                    clicker.suspendForGui();
                    return false;
                }
                // Physical input may have changed inside the original getter.
                click = clicker.shouldClick(
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.LEFT_BUTTON),
                        inputState.keyPressed(LegacyKeyboardCodes.W),
                        inputState.pointerPressed(
                                Minecraft189ClickRateTracker.RIGHT_BUTTON),
                        nearestPlayerTargetState.snapshot(),
                        playerMovementState.snapshot(), confirmedHit,
                        verifiedAutoClickerUuid);
            } else if (requireOriginalTargetUuid) {
                // The indexed production path NEVER supplies the legacy
                // test-only sentinel UUID. If trigger mode is enabled
                // mid-callback, a null source owner must deny the click.
                click = clicker.shouldClick(
                        attackHeld, inputState.keyPressed(LegacyKeyboardCodes.W),
                        rightHeld, nearestPlayerTargetState.snapshot(),
                        playerMovementState.snapshot(), confirmedHit, null);
            } else {
                click = clicker.shouldClick(
                        attackHeld, inputState.keyPressed(LegacyKeyboardCodes.W),
                        rightHeld, nearestPlayerTargetState.snapshot(),
                        playerMovementState.snapshot(), confirmedHit);
            }
            if (click && requireOriginalTargetUuid)
                verifiedAutoClickerEpoch = clicker.nativeOwnerEpoch();
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
            if (aura.lockTargetSetting().get()) {
                final Minecraft189WorldEntityUuidState.Snapshot ids =
                        worldEntityUuidState.snapshot();
                final java.util.UUID selectedUuid = aura.lockedTargetUuid();
                if (!ids.available() || selectedUuid == null
                        || !selectedUuid.equals(ids.at(selected.entityIndex()))
                        || !selectedUuid.equals(ids.at(crosshairPlayerIndex))
                        || tickUuidWorld == null) return false;
                try {
                    if (!ids.matches(tickUuidWorld.customMcLoadedEntityUuids()))
                        return false;
                } catch (RuntimeException invalidEvidence) {
                    return false;
                }
            }
        }
        // A verified tab-list miss is a veto for every synthetic click
        // owner. A real manually-triggered vanilla click remains untouched.
        if (!featureCatalog.antiBot().permits(crosshairPlayerIndex, combat)
                || !featureCatalog.teamGuard().permits(crosshairPlayerIndex, combat)
                || !friendTargetPermits(crosshairPlayerIndex)
                || !wallTargetPermits(crosshairPlayerIndex)) {
            return false;
        }
        if (!featureCatalog.attackRange().permits(
                crosshairPlayerIndex, playerPositionState.snapshot(),
                worldEntityPositionState.snapshot(),
                worldEntityKindState.snapshot(), nativeHitbox)) return false;
        // HitSelect's optional recovery latch is strictly owned by the
        // ORIGINAL current remote UUID, not a cached entity index. Legacy
        // default mode retains the same index/hurt-time behavior.
        java.util.UUID verifiedHitSelectUuid = null;
        long verifiedHitSelectEpoch = -1L;
        int verifiedHitSelectMin = -1;
        int verifiedHitSelectMax = -1;
        if (requireOriginalTargetUuid && hitSelect.active()
                && hitSelect.requireObservedRecoverySetting().get()) {
            verifiedHitSelectEpoch = hitSelect.nativeOwnerEpoch();
            verifiedHitSelectMin =
                    hitSelect.minTargetHurtTicksSetting().get();
            verifiedHitSelectMax =
                    hitSelect.maxTargetHurtTicksSetting().get();
            final Minecraft189WorldEntityUuidState.Snapshot currentIds =
                    worldEntityUuidState.snapshot();
            final Minecraft189WorldEntityKindState.Snapshot currentKinds =
                    worldEntityKindState.snapshot();
            if (tickUuidWorld == null || crosshairPlayerIndex < 0
                    || !currentIds.available() || !currentKinds.available()
                    || currentIds.entityCount() != combat.entityCount()
                    || !hitSelect.permitsOriginalOwner(
                            crosshairPlayerIndex,
                            currentIds.at(crosshairPlayerIndex),
                            currentIds, currentKinds, combat))
                return false;
            try {
                if (!currentIds.matches(tickUuidWorld.customMcLoadedEntityUuids()))
                    return false;
            } catch (RuntimeException invalidNativeWorld) {
                return false;
            }
            if (!hitSelect.active()
                    || !hitSelect.requireObservedRecoverySetting().get()
                    || hitSelect.nativeOwnerEpoch() != verifiedHitSelectEpoch
                    || hitSelect.minTargetHurtTicksSetting().get()
                            != verifiedHitSelectMin
                    || hitSelect.maxTargetHurtTicksSetting().get()
                            != verifiedHitSelectMax)
                return false;
            verifiedHitSelectUuid = currentIds.at(crosshairPlayerIndex);
        } else if (!hitSelect.permits(crosshairPlayerIndex, combat)) {
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
        if (nativeAutoClickerScheduled
                && (verifiedAutoClickerEpoch < 0
                    || verifiedAutoClickerEpoch
                        != featureCatalog.autoClicker().nativeOwnerEpoch()
                    || nativeAutoClickerTriggerMode
                        != featureCatalog.autoClicker().triggerModeSetting()
                                .get().booleanValue()
                    || !featureCatalog.autoClicker().active()))
            return false;
        if (verifiedAutoClickerUuid != null) {
            final Minecraft189WorldEntityUuidState.Snapshot ids =
                    worldEntityUuidState.snapshot();
            if (!requireOriginalTargetUuid
                    || !featureCatalog.autoClicker().active()
                    || !featureCatalog.autoClicker().triggerModeSetting()
                            .get().booleanValue()
                    || aura.active() || trigger.active()
                    || !ids.available() || crosshairPlayerIndex < 0
                    || crosshairPlayerIndex >= ids.entityCount()
                    || !verifiedAutoClickerUuid.equals(ids.at(crosshairPlayerIndex))
                    || tickUuidWorld == null)
                return false;
            try {
                if (!ids.matches(tickUuidWorld.customMcLoadedEntityUuids()))
                    return false;
            } catch (RuntimeException changedNativeWorld) {
                return false;
            }
        }
        if (admissionSerial != triggerDispatchAdmissionSerial
                || clickGuiRuntime.coreRuntime().model().snapshot().open())
            return false;
        if (verifiedTriggerUuid != null) {
            // Require the ORIGINAL source UUID and module activation that
            // actually earned confirmation, not just the reused index of
            // the later native controller call.
            final Minecraft189WorldEntityUuidState.Snapshot ids =
                    worldEntityUuidState.snapshot();
            if (!trigger.active() || aura.active()
                    || verifiedTriggerEpoch != trigger.nativeWorldOwnerEpoch()
                    || !ids.available() || crosshairPlayerIndex < 0
                    || crosshairPlayerIndex >= ids.entityCount()
                    || !verifiedTriggerUuid.equals(
                            ids.at(crosshairPlayerIndex)))
                return false;
            triggerDispatchLease.arm(
                    verifiedTriggerUuid, verifiedTriggerEpoch);
        }
        if (verifiedAutoClickerUuid != null) {
            autoClickerControllerLease.arm(
                    verifiedAutoClickerUuid, verifiedAutoClickerEpoch);
        }
        // Consume the optional UUID-specific recovery state only after
        // the full cross-module synthetic-click admission has passed.
        // A failed Criticals, range, guard, GUI or owner-epoch gate
        // cannot spend a recovery cycle or queue a delayed click.
        if (verifiedHitSelectUuid != null) {
            // The original World getter or later guard callbacks may
            // synchronously OFF->ON this module, replace its World, or
            // edit its recovery window. Never consume a stale owner.
            if (!hitSelect.active()
                    || !hitSelect.requireObservedRecoverySetting().get()
                    || hitSelect.nativeOwnerEpoch() != verifiedHitSelectEpoch
                    || hitSelect.minTargetHurtTicksSetting().get()
                            != verifiedHitSelectMin
                    || hitSelect.maxTargetHurtTicksSetting().get()
                            != verifiedHitSelectMax)
                return false;
            hitSelect.noteScheduledOriginalClick(verifiedHitSelectUuid);
        }
        // Capture only after all HitSelect/UUID/range/GUI admission gates.
        final Minecraft189CriticalsModule nativeCriticals =
                featureCatalog.criticals();
        criticalsNativeActive = nativeCriticals.active();
        criticalsNativeLifecycleEpoch = nativeCriticals.lifecycleEpoch();
        criticalsNativeMinimumFall =
                nativeCriticals.minimumFallDistanceSetting().get();
        criticalsNativeRequireDescending =
                nativeCriticals.requireDescendingSetting().get();
        criticalsNativeSourceGeneration = criticalsSourceGeneration;
        criticalsNativePlayer = tickHotbarPlayer;
        criticalsNativeAdmissionSerial = admissionSerial;
        clickRateTracker.recordPress(
                Minecraft189ClickRateTracker.LEFT_BUTTON);
        return true;
    }

    /**
     * Final genuine local-player Criticals gate, shared by native Aura,
     * TriggerBot and AutoClicker. HitSelect's original UUID/hurt recovery
     * remains independent. No jump, packet, fake fallDistance or controller.
     */
    boolean mayDispatchOriginalCriticals() {
        requireOpen();
        final Minecraft189CriticalsModule module = featureCatalog.criticals();
        if (criticalsNativeAdmissionSerial < 0L
                || criticalsNativeAdmissionSerial != triggerDispatchAdmissionSerial
                || module.active() != criticalsNativeActive
                || module.lifecycleEpoch() != criticalsNativeLifecycleEpoch
                || module.minimumFallDistanceSetting().get()
                        != criticalsNativeMinimumFall
                || module.requireDescendingSetting().get()
                        != criticalsNativeRequireDescending) return false;
        if (!module.active()) return true;

        final Minecraft189PlayerInventoryAccess original = criticalsNativePlayer;
        final long generation = criticalsNativeSourceGeneration;
        if (!criticalNativeSourceStillOwned(original, generation)
                || !(original instanceof Minecraft189PlayerMovementStateAccess)
                || !(original instanceof Minecraft189PlayerFallDistanceControl)
                || !(original instanceof Minecraft189PlayerMotionControl))
            return false;
        final Minecraft189PlayerMovementStateAccess movement =
                (Minecraft189PlayerMovementStateAccess) original;
        final Minecraft189PlayerFallDistanceControl fall =
                (Minecraft189PlayerFallDistanceControl) original;
        final Minecraft189PlayerMotionControl motion =
                (Minecraft189PlayerMotionControl) original;
        // All six values come from genuine mapped getters. A callback
        // reentry or an in-place movement transition rejects the older
        // synthetic click; the second read must agree with the first.
        final boolean ground = movement.customMcOnGround();
        if (!criticalNativeSourceStillOwned(original, generation)) return false;
        final float distance = fall.customMcFallDistance();
        if (!criticalNativeSourceStillOwned(original, generation)) return false;
        final double motionY = motion.customMcMotionY();
        if (!criticalNativeSourceStillOwned(original, generation)) return false;
        final boolean groundAgain = movement.customMcOnGround();
        if (!criticalNativeSourceStillOwned(original, generation)) return false;
        final float distanceAgain = fall.customMcFallDistance();
        if (!criticalNativeSourceStillOwned(original, generation)) return false;
        final double motionAgain = motion.customMcMotionY();
        if (!criticalNativeSourceStillOwned(original, generation)
                || ground != groundAgain || distance != distanceAgain
                || motionY != motionAgain) return false;
        return module.permitsOriginalNativeFall(groundAgain,
                distanceAgain, motionAgain,
                featureCatalog.flight().active()
                        || featureCatalog.freeze().active()
                        || featureCatalog.noFall().active()
                        || featureCatalog.noGravity().active()
                        || featureCatalog.noClip().active())
                && criticalNativeSourceStillOwned(original, generation)
                && module.active() == criticalsNativeActive
                && module.lifecycleEpoch() == criticalsNativeLifecycleEpoch
                && module.minimumFallDistanceSetting().get()
                        == criticalsNativeMinimumFall
                && module.requireDescendingSetting().get()
                        == criticalsNativeRequireDescending
                && !clickGuiRuntime.coreRuntime().model().snapshot().open();
    }

    private boolean criticalNativeSourceStillOwned(
            final Minecraft189PlayerInventoryAccess original,
            final long generation) {
        return original != null && criticalsSourceGeneration == generation
                && criticalsNativePlayer == original
                && tickHotbarPlayer == original
                && tickHeldItemAccess == original
                && criticalsNativeAdmissionSerial
                        == triggerDispatchAdmissionSerial
                && !clickGuiRuntime.coreRuntime().model().snapshot().open();
    }

    /**
     * Last gate BEFORE the original real player controller attack. The
     * current original source UUID must equal the one that earned native
     * TriggerBot crosshair confirmation; spend it exactly once even if
     * the native player list index or module activation has changed.
     */
    boolean hasPendingAutoClickerControllerIntent() {
        requireOpen();
        return autoClickerControllerLease.pending();
    }

    /** Native indexed trigger only: spend the UUID just before vanilla
     * clickMouse, then enforce the same owner at the real controller.
     * A failed attempt is consumed without authorizing later replay.
     */
    Minecraft189AutoClickerControllerLease.Intent
            consumeAutoClickerControllerIntent(final java.util.UUID sourceUuid) {
        requireOpen();
        final Minecraft189AutoClickerModule clicker =
                featureCatalog.autoClicker();
        return autoClickerControllerLease.consume(
                sourceUuid, clicker.nativeOwnerEpoch(),
                clicker.active()
                        && clicker.triggerModeSetting().get().booleanValue(),
                featureCatalog.killAura().active()
                        || featureCatalog.triggerBot().active(),
                clickGuiRuntime.coreRuntime().model().snapshot().open());
    }

    boolean consumeTriggerDispatchUuid(final java.util.UUID sourceUuid) {
        requireOpen();
        return triggerDispatchLease.consume(
                sourceUuid,
                featureCatalog.triggerBot().nativeWorldOwnerEpoch(),
                featureCatalog.triggerBot().active()
                        && !featureCatalog.killAura().active()
                        && !featureCatalog.autoClicker().active(),
                clickGuiRuntime.coreRuntime().model().snapshot().open());
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
        // Revoke in-flight action-cycle tokens before any native-source
        // lease, module, hotbar or UI teardown. A stale finally cannot
        // release a newer generation created by another callback.
        combatCycle.revoke();
        autoClickerControllerLease.clear();
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
        pendingKeepSprintSource = null;
        keepSprintAdmissionSerial++;
        keepSprintRestoreLease.revoke();
        tickHotbarControl = null;
        tickHotbarPlayer = null;
        tickHeldItemAccess = null;
        heldItemOwnerSerial++;
        featureCatalog.autoBlock().forgetForShutdown();
        worldEntityKindState.clear();
        worldEntityCombatState.clear();
        worldEntityUuidState.clear();
        worldEntityVisibilityState.clear();
        tickVisibilityWorld = null;
        tickUuidWorld = null;
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
