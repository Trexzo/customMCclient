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
    private final Minecraft189PlayerArmorState playerArmorState;
    private final Minecraft189PlayerHungerState playerHungerState;
    private final Minecraft189PlayerPotionEffectsState playerPotionEffectsState;
    private final Minecraft189PlayerExperienceState playerExperienceState;
    private final Minecraft189PlayerPingState playerPingState;
    private final Minecraft189HotbarSlotState hotbarSlotState;
    private final Minecraft189WorldTimeState worldTimeState;
    private final Minecraft189WorldWeatherState worldWeatherState;
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
            final Minecraft189PlayerArmorState playerArmorState,
            final Minecraft189PlayerHungerState playerHungerState,
            final Minecraft189PlayerPotionEffectsState playerPotionEffectsState,
            final Minecraft189PlayerExperienceState playerExperienceState,
            final Minecraft189PlayerPingState playerPingState,
            final Minecraft189HotbarSlotState hotbarSlotState,
            final Minecraft189WorldTimeState worldTimeState,
            final Minecraft189WorldWeatherState worldWeatherState,
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
        this.playerArmorState = playerArmorState;
        this.playerHungerState = playerHungerState;
        this.playerPotionEffectsState = playerPotionEffectsState;
        this.playerExperienceState = playerExperienceState;
        this.playerPingState = playerPingState;
        this.hotbarSlotState = hotbarSlotState;
        this.worldTimeState = worldTimeState;
        this.worldWeatherState = worldWeatherState;
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
        if (player == null) {
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
            return;
        }
        playerRotationState.update(
                player.customMcRotationYaw());
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
        if (player == null) {
            return;
        }
        featureCatalog.autoSprint()
                .apply(
                        player,
                        playerMovementState.snapshot());
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
        if (player == null) {
            hotbarSlotState.clear();
            return;
        }
        final Minecraft189InventoryHotbarAccess inventory =
                player.customMcInventory();
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
        playerArmorState.clear();
        playerHungerState.clear();
        playerPotionEffectsState.clear();
        playerExperienceState.clear();
        playerPingState.clear();
        hotbarSlotState.clear();
        worldTimeState.clear();
        worldWeatherState.clear();
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
