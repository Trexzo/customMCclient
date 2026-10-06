package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;

import java.util.Objects;

public final class Minecraft189FullbrightModule
        implements Module {
    public static final String ID =
            "render.fullbright";
    private static final float FULLBRIGHT_GAMMA =
            16.0F;

    private final Minecraft189GuiSettingsAccess settings;
    private boolean active;
    private float originalGamma;

    public Minecraft189FullbrightModule(
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
                    "fullbright already active");
        }

        final float current =
                settings.gammaSetting();
        requireFinite(
                current,
                "current gamma");

        originalGamma = current;
        settings.gammaSetting(
                FULLBRIGHT_GAMMA);
        active = true;
    }

    @Override
    public synchronized void onDisable() {
        if (!active) {
            return;
        }

        try {
            settings.gammaSetting(
                    originalGamma);
        } finally {
            active = false;
        }
    }

    public synchronized boolean active() {
        return active;
    }

    public synchronized float originalGamma() {
        if (!active) {
            throw new IllegalStateException(
                    "fullbright has no active captured gamma");
        }
        return originalGamma;
    }

    static float fullbrightGamma() {
        return FULLBRIGHT_GAMMA;
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
