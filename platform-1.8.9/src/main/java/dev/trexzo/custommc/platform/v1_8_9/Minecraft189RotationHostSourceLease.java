package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Pins one exact original EntityPlayerSP rotation source across actual
 * yaw/pitch and ground/sneak/sprint reads and all rotation module setters.
 */
final class Minecraft189RotationHostSourceLease {
    private boolean dispatching;
    private Minecraft189PlayerRotationAccess source;
    private boolean guiAtEntry;
    private boolean revoked;

    boolean enter(final Minecraft189PlayerRotationAccess player,
            final boolean guiFocused) {
        if (dispatching) {
            if (player != source || (!guiAtEntry && guiFocused)) {
                revoked = true;
            }
            return false;
        }
        dispatching = true;
        source = player;
        guiAtEntry = guiFocused;
        revoked = false;
        return true;
    }

    boolean stillOwned(final boolean guiFocused) {
        if (!dispatching) return false;
        if (!guiAtEntry && guiFocused) revoked = true;
        return !revoked;
    }

    Minecraft189PlayerRotationControl guardedControl(
            final Minecraft189PlayerRotationAccess player,
            final BooleanSupplier currentGuiFocused) {
        if (!(player instanceof Minecraft189PlayerRotationControl)) {
            return null;
        }
        Objects.requireNonNull(currentGuiFocused, "currentGuiFocused");
        final Minecraft189PlayerRotationControl nativeSource =
                (Minecraft189PlayerRotationControl) player;
        return new Minecraft189PlayerRotationControl() {
            @Override public float customMcRotationYaw() {
                return nativeSource.customMcRotationYaw();
            }
            @Override public float customMcRotationPitch() {
                return nativeSource.customMcRotationPitch();
            }
            @Override public void customMcSetRotationYaw(final float yaw) {
                if (stillOwned(currentGuiFocused.getAsBoolean())) {
                    nativeSource.customMcSetRotationYaw(yaw);
                }
            }
            @Override public void customMcSetRotationPitch(final float pitch) {
                if (stillOwned(currentGuiFocused.getAsBoolean())) {
                    nativeSource.customMcSetRotationPitch(pitch);
                }
            }
        };
    }

    void exit() {
        source = null;
        guiAtEntry = false;
        revoked = false;
        dispatching = false;
    }
}
