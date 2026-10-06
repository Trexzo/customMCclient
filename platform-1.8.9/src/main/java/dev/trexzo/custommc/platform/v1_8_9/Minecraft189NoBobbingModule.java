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

        originalViewBobbing =
                settings.viewBobbing();
        settings.viewBobbing(false);
        active = true;
    }

    @Override
    public synchronized void onDisable() {
        if (!active) {
            return;
        }

        try {
            settings.viewBobbing(
                    originalViewBobbing);
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
