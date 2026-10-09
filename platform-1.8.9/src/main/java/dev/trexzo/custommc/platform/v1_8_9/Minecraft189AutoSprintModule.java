package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Objects;

public final class Minecraft189AutoSprintModule
        implements Module {
    public static final String ID =
            "movement.autoSprint";
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "movement.autoSprint.requireForward";
    public static final String REQUIRE_MOVEMENT_SETTING_ID =
            "movement.autoSprint.requireMovement";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.autoSprint.groundOnly";

    private final Setting<Boolean> requireForward =
            new Setting<Boolean>(
                    REQUIRE_FORWARD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> requireMovement =
            new Setting<Boolean>(
                    REQUIRE_MOVEMENT_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(
                    GROUND_ONLY_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
    }

    public Setting<Boolean> requireMovementSetting() {
        return requireMovement;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
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
            final Minecraft189PlayerSprintControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld) {
        // Original callers know the state of W, not A/S/D. Treat W as
        // the only confirmed motion input in the compatibility overload.
        apply(player, movement, forwardHeld, forwardHeld);
    }

    synchronized void apply(
            final Minecraft189PlayerSprintControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld,
            final boolean anyMovementHeld) {
        Objects.requireNonNull(
                movement,
                "movement");
        if (!enabled
                || player == null
                || !movement.available()
                || (requireForward.get().booleanValue()
                        && !forwardHeld)
                || (requireMovement.get().booleanValue()
                        && !anyMovementHeld)
                || (groundOnly.get().booleanValue()
                        && !movement.onGround())
                || movement.sneaking()
                || movement.sprinting()) {
            return;
        }
        player.customMcSetSprinting(
                true);
    }

    synchronized boolean active() {
        return enabled;
    }
}
