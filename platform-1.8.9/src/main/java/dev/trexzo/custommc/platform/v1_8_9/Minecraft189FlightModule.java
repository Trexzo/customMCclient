package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189FlightModule
        implements Module {
    public static final String ID =
            "movement.flight";
    public static final double ASCEND_MOTION_Y =
            0.30D;
    public static final double DESCEND_MOTION_Y =
            -0.30D;
    public static final double HOVER_MOTION_Y =
            0.0D;

    private final Minecraft189InputState inputState;
    private boolean enabled;

    Minecraft189FlightModule(
            final Minecraft189InputState inputState) {
        this.inputState =
                Objects.requireNonNull(
                        inputState,
                        "inputState");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player) {
        if (!enabled || player == null) {
            return;
        }

        final boolean ascend =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
        final boolean descend =
                inputState.keyPressed(
                        LegacyKeyboardCodes.LEFT_SHIFT)
                        || inputState.keyPressed(
                                LegacyKeyboardCodes.RIGHT_SHIFT);

        final double targetMotionY;
        if (ascend == descend) {
            targetMotionY =
                    HOVER_MOTION_Y;
        } else if (ascend) {
            targetMotionY =
                    ASCEND_MOTION_Y;
        } else {
            targetMotionY =
                    DESCEND_MOTION_Y;
        }

        if (Double.compare(
                player.customMcMotionY(),
                targetMotionY) != 0) {
            player.customMcSetMotionY(
                    targetMotionY);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
