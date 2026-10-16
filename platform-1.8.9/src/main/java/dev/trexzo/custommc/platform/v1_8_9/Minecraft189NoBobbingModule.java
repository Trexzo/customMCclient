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
        if (active) {
            throw new IllegalStateException(
                    "no-bobbing already active");
        }

        originalViewBobbing = settings.viewBobbing();
        try {
            settings.viewBobbing(false);
        } catch (RuntimeException failure) {
            // Original setter may have applied false before throwing.
            // Restore only our own override, never a concurrent user edit.
            try {
                if (!settings.viewBobbing() && originalViewBobbing)
                    settings.viewBobbing(originalViewBobbing);
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
        active = true;
    }

    @Override
    public synchronized void onDisable() {
        if (!active) {
            return;
        }

        try {
            final boolean current = settings.viewBobbing();
            // If the user changed bobbing while the module was active,
            // the module may not clobber that latest native preference.
            if (!current && originalViewBobbing)
                settings.viewBobbing(originalViewBobbing);
        } finally {
            active = false;
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
