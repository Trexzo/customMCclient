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
    private boolean enabling;
    private boolean disabling;
    // The original GameSettings getter/setter may reenter onDisable.
    private long nativeSettingsEpoch;
    private float originalFov;
    private float lastInjectedFov;

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
        if (active || enabling || disabling) {
            throw new IllegalStateException("FOV changer already active or transitioning");
        }
        enabling = true;
        final long ownerEpoch = ++nativeSettingsEpoch;
        float captured = Float.NaN;
        boolean attemptedOverride = false;
        RuntimeException failure = null;
        try {
            // Genuine Minecraft GameSettings FOV is the only baseline.
            // A synchronous disable during this getter cancels the owner
            // BEFORE any override or tick subscription is installed.
            captured = settings.fovSetting();
            requireFinite(captured, "current FOV");
            if (ownerEpoch != nativeSettingsEpoch) return;
            originalFov = captured;
            final float desired = targetFov.get().floatValue();
            lastInjectedFov = desired;
            attemptedOverride = true;
            settings.fovSetting(desired);
            // A real original setter can reenter onDisable. Do not let
            // that cancelled transaction subsequently register a live
            // tick callback or mark itself active.
            if (ownerEpoch != nativeSettingsEpoch) return;
            tickSubscription = events.subscribe(
                    Minecraft189Hooks.TickEvent.class,
                    event -> applyConfiguredFov());
            if (ownerEpoch != nativeSettingsEpoch) return;
            active = true;
        } catch (RuntimeException originalFailure) {
            failure = originalFailure;
            throw originalFailure;
        } finally {
            try {
                if (!active) {
                    if (tickSubscription != null) {
                        tickSubscription.close();
                        tickSubscription = null;
                    }
                    if (attemptedOverride
                            && Float.compare(captured, lastInjectedFov) != 0
                            && Float.compare(settings.fovSetting(), lastInjectedFov) == 0) {
                        // Failed or cancelled native setters may mutate
                        // before returning/throwing. Restore only the
                        // exact value written by THIS transaction.
                        settings.fovSetting(captured);
                    }
                }
            } catch (RuntimeException rollbackFailure) {
                if (failure != null) failure.addSuppressed(rollbackFailure);
                else throw rollbackFailure;
            } finally {
                enabling = false;
            }
        }
    }

    @Override
    public synchronized void onDisable() {
        if (disabling) return;
        ++nativeSettingsEpoch;
        if (!active) return;
        disabling = true;
        RuntimeException failure = null;
        try {
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
                // A user's own later GameSettings FOV change must survive
                // disable. The module restores ONLY its current injected
                // value, not an unrelated native preference.
                final float current = settings.fovSetting();
                if (Float.compare(current, lastInjectedFov) == 0
                        && Float.compare(originalFov, lastInjectedFov) != 0) {
                    settings.fovSetting(originalFov);
                }
            } catch (RuntimeException restoreFailure) {
                if (failure == null) failure = restoreFailure;
                else failure.addSuppressed(restoreFailure);
            }
        } finally {
            active = false;
            disabling = false;
        }
        if (failure != null) throw failure;
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
        // Track the newest exact module-owned FOV BEFORE native write:
        // a synchronous onDisable inside GameSettings setter must be
        // able to restore this specific value immediately.
        lastInjectedFov = targetFov.get().floatValue();
        settings.fovSetting(lastInjectedFov);
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
