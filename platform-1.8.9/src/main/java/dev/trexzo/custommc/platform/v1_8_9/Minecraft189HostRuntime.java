package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiThemeProvider;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;

import java.util.Objects;

public final class Minecraft189HostRuntime
        implements AutoCloseable {
    private final Minecraft189Platform platform;
    private final Minecraft189ClickGuiRuntime clickGuiRuntime;
    private final Minecraft189Hooks renderHooks;
    private final Minecraft189HostInputBridge inputBridge;
    private boolean closed;

    private Minecraft189HostRuntime(
            final Minecraft189Platform platform,
            final Minecraft189ClickGuiRuntime clickGuiRuntime,
            final Minecraft189Hooks renderHooks,
            final Minecraft189HostInputBridge inputBridge) {
        this.platform = platform;
        this.clickGuiRuntime = clickGuiRuntime;
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

        return new Minecraft189HostRuntime(
                platform,
                clickGuiRuntime,
                new Minecraft189Hooks(platform),
                new Minecraft189HostInputBridge(
                        platform,
                        hostCallbacks));
    }

    public Minecraft189ClickGuiRuntime clickGuiRuntime() {
        requireOpen();
        return clickGuiRuntime;
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
        clickGuiRuntime.close();
    }

    private synchronized void requireOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "minecraft 1.8.9 host runtime is closed");
        }
    }
}
