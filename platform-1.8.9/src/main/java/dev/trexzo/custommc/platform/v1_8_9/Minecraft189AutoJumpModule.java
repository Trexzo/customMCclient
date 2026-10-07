package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Objects;

public final class Minecraft189AutoJumpModule
        implements Module {
    public static final String ID =
            "movement.autoJump";
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "movement.autoJump.requireForward";

    private final Setting<Boolean> requireForward =
            new Setting<Boolean>(
                    REQUIRE_FORWARD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;
    private boolean armed = true;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        armed = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        armed = true;
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld) {
        Objects.requireNonNull(
                movement,
                "movement");
        if (!enabled
                || player == null
                || !movement.available()) {
            return;
        }

        if (!movement.onGround()) {
            armed = true;
            return;
        }

        if ((requireForward.get().booleanValue()
                && !forwardHeld)
                || !armed) {
            return;
        }

        player.customMcJump();
        armed = false;
    }

    synchronized boolean active() {
        return enabled;
    }
}
