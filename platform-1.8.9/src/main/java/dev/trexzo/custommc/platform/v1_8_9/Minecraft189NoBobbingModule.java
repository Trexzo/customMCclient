package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;

import java.util.Objects;

public final class Minecraft189NoBobbingModule
        implements Module {
    public static final String ID =
            "render.no-bobbing";

    private final Minecraft189GuiSettingsAccess settings;
    private boolean active;
    private boolean enabling;
    private boolean disabling;
    private long nativeSettingsEpoch;
    private boolean originalViewBobbing;

    public Minecraft189NoBobbingModule(
            final Minecraft189GuiSettingsAccess settings) {
        this.settings =
                Objects.requireNonNull(
                        settings,
                        "settings");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        if (active || enabling || disabling) {
            throw new IllegalStateException("no-bobbing already active or transitioning");
        }
        enabling = true;
        final long ownerEpoch = ++nativeSettingsEpoch;
        boolean captured = false;
        boolean hasBaseline = false;
        boolean attemptedOverride = false;
        RuntimeException failure = null;
        try {
            captured = settings.viewBobbing();
            hasBaseline = true;
            if (ownerEpoch != nativeSettingsEpoch) return;
            originalViewBobbing = captured;
            attemptedOverride = true;
            settings.viewBobbing(false);
            if (ownerEpoch == nativeSettingsEpoch) active = true;
        } catch (RuntimeException originalFailure) {
            failure = originalFailure;
            throw originalFailure;
        } finally {
            try {
                if (!active && hasBaseline && attemptedOverride
                        && captured && !settings.viewBobbing()) {
                    // Only restore the false override written by the old
                    // transaction. A concurrent native true is user-owned.
                    settings.viewBobbing(true);
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
        nativeSettingsEpoch++;
        if (!active) return;
        disabling = true;
        try {
            final boolean current = settings.viewBobbing();
            if (!current && originalViewBobbing) settings.viewBobbing(true);
        } finally {
            active = false;
            disabling = false;
        }
    }

    public synchronized boolean active() {
        return active;
    }

    public synchronized boolean originalViewBobbing() {
        if (!active) {
            throw new IllegalStateException(
                    "no-bobbing has no active captured value");
        }
        return originalViewBobbing;
    }
}
