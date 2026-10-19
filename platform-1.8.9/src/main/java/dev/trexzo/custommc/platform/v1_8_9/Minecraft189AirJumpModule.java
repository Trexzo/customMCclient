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
    // Prevent a reentrant original jump() from rewriting the new key-edge owner.
    private long nativeJumpEpoch;
    private boolean spaceWasPressed;
    // Real mapped EntityPlayer.jump() may reenter this same original
    // host stack before its outer finally observes the Space edge.
    // Keep this guard across OFF/ON so a nested new lifetime cannot
    // borrow a second jump from a still-active original callback.
    private boolean nativeJumpDispatching;

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
        nativeJumpEpoch++;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeJumpEpoch++;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        apply(player, movement, false);
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean actualClickGuiOpen) {
        Objects.requireNonNull(
                movement,
                "movement");
        if (nativeJumpDispatching) {
            // A reentrant callback is not a new physical input tick;
            // never overwrite the new owner's sampled source Space.
            return;
        }

        final boolean spacePressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
        final long ownerEpoch = nativeJumpEpoch;
        try {
            // The GUI consumes its own Space press. Keep observing the
            // physical key in finally so closing the menu with Space still
            // held cannot synthesize a fresh airborne jump edge.
            if (!enabled
                    || actualClickGuiOpen
                    || player == null
                    || !movement.available()
                    || movement.onGround()
                    || !spacePressed
                    || spaceWasPressed) {
                return;
            }

            nativeJumpDispatching = true;
            try {
                player.customMcJump();
            } finally {
                // The native jump may update the original Y before
                // throwing. Release only reentrant ownership, not Y.
                nativeJumpDispatching = false;
            }
        } finally {
            // The original native jump() callback can restart AirJump.
            // onEnable/Disable already re-sample the real Space state for
            // that fresh owner: old callbacks must not clobber its edge.
            if (ownerEpoch == nativeJumpEpoch) {
                spaceWasPressed = inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
            }
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
