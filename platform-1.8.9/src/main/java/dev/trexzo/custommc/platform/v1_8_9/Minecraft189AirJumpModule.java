package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189AirJumpModule
        implements Module {
    public static final String ID =
            "movement.airJump";

    private final Minecraft189InputState inputState;
    private boolean enabled;
    private boolean spaceWasPressed;

    Minecraft189AirJumpModule(
            final Minecraft189InputState inputState) {
        this.inputState =
                Objects.requireNonNull(
                        inputState,
                        "inputState");
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        Objects.requireNonNull(
                movement,
                "movement");

        final boolean spacePressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
        try {
            if (!enabled
                    || player == null
                    || !movement.available()
                    || movement.onGround()
                    || !spacePressed
                    || spaceWasPressed) {
                return;
            }

            player.customMcJump();
        } finally {
            spaceWasPressed = spacePressed;
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
