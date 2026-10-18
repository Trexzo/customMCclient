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
    private boolean enabling;
    private boolean disabling;
    private long nativeSettingsEpoch;
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
        if (active || enabling || disabling) {
            throw new IllegalStateException("fullbright already active or transitioning");
        }
        enabling = true;
        final long ownerEpoch = ++nativeSettingsEpoch;
        float captured = Float.NaN;
        boolean attemptedOverride = false;
        RuntimeException failure = null;
        try {
            // The mapped gamma getter may reenter onDisable. In that case
            // this cancelled owner must never write a fullbright override.
            captured = settings.gammaSetting();
            requireFinite(captured, "current gamma");
            if (ownerEpoch != nativeSettingsEpoch) return;
            originalGamma = captured;
            attemptedOverride = true;
            settings.gammaSetting(FULLBRIGHT_GAMMA);
            // The mapped setter may also reenter onDisable. No previous
            // activation can become active after that cancellation.
            if (ownerEpoch == nativeSettingsEpoch) active = true;
        } catch (RuntimeException originalFailure) {
            failure = originalFailure;
            throw originalFailure;
        } finally {
            try {
                if (!active && attemptedOverride
                        && Float.compare(captured, FULLBRIGHT_GAMMA) != 0
                        && Float.compare(settings.gammaSetting(), FULLBRIGHT_GAMMA) == 0) {
                    // Original setter may mutate before throw or cancellation.
                    // Only roll back our own exact injected value; preserve
                    // any native user Gamma edit made by the callback.
                    settings.gammaSetting(captured);
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
        // Even if enable is only halfway through a mapped getter/setter,
        // disabling revokes its source owner before it can commit.
        nativeSettingsEpoch++;
        if (!active) return;
        disabling = true;
        try {
            final float current = settings.gammaSetting();
            if (Float.compare(current, FULLBRIGHT_GAMMA) == 0
                    && Float.compare(originalGamma, FULLBRIGHT_GAMMA) != 0) {
                settings.gammaSetting(originalGamma);
            }
        } finally {
            active = false;
            disabling = false;
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
