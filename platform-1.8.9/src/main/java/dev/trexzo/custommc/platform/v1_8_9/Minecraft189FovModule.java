package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;

import java.util.Objects;

public final class Minecraft189FovModule
        implements Module {
    public static final String ID =
            "render.fov";
    public static final String VALUE_SETTING_ID =
            "render.fov.value";

    private final Minecraft189GuiSettingsAccess settings;
    private final EventBus events;
    private final Setting<Integer> targetFov =
            new Setting<Integer>(
                    VALUE_SETTING_ID,
                    110,
                    value -> value >= 30
                            && value <= 179,
                    SettingCodecs.INTEGER);

    private EventBus.Subscription tickSubscription;
    private boolean active;
    private float originalFov;

    public Minecraft189FovModule(
            final Minecraft189GuiSettingsAccess settings,
            final EventBus events) {
        this.settings =
                Objects.requireNonNull(
                        settings,
                        "settings");
        this.events =
                Objects.requireNonNull(
                        events,
                        "events");
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> targetFovSetting() {
        return targetFov;
    }

    @Override
    public synchronized void onEnable() {
        if (active) {
            throw new IllegalStateException(
                    "FOV changer already active");
        }

        final float current =
                settings.fovSetting();
        requireFinite(
                current,
                "current FOV");
        originalFov = current;

        settings.fovSetting(
                targetFov.get().floatValue());
        try {
            tickSubscription =
                    events.subscribe(
                            Minecraft189Hooks.TickEvent.class,
                            event -> applyConfiguredFov());
            active = true;
        } catch (RuntimeException failure) {
            settings.fovSetting(
                    originalFov);
            throw failure;
        }
    }

    @Override
    public synchronized void onDisable() {
        if (!active) {
            return;
        }

        RuntimeException failure = null;
        if (tickSubscription != null) {
            try {
                tickSubscription.close();
            } catch (RuntimeException closeFailure) {
                failure = closeFailure;
            } finally {
                tickSubscription = null;
            }
        }

        try {
            settings.fovSetting(
                    originalFov);
        } catch (RuntimeException restoreFailure) {
            if (failure == null) {
                failure = restoreFailure;
            } else {
                failure.addSuppressed(
                        restoreFailure);
            }
        } finally {
            active = false;
        }

        if (failure != null) {
            throw failure;
        }
    }

    public synchronized boolean active() {
        return active;
    }

    public synchronized float originalFov() {
        if (!active) {
            throw new IllegalStateException(
                    "FOV changer has no active captured FOV");
        }
        return originalFov;
    }

    private synchronized void applyConfiguredFov() {
        if (!active) {
            return;
        }
        settings.fovSetting(
                targetFov.get().floatValue());
    }

    private static void requireFinite(
            final float value,
            final String name) {
        if (Float.isNaN(value)
                || Float.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }
}
