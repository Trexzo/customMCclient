package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189WTapModule
        implements Module {
    public static final String ID =
            "combat.wTap";
    public static final String REQUIRE_GROUND_SETTING_ID =
            "combat.wTap.requireGround";

    private final Setting<Boolean> requireGround =
            new Setting<Boolean>(
                    REQUIRE_GROUND_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;
    private boolean previousLeftButtonHeld;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Boolean> requireGroundSetting() {
        return requireGround;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetInput();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetInput();
    }

    synchronized boolean apply(
            final Minecraft189PlayerSprintControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean leftButtonHeld) {
        if (!enabled
                || player == null
                || movement == null
                || !movement.available()) {
            resetInput();
            return false;
        }

        if (!leftButtonHeld) {
            previousLeftButtonHeld = false;
            return false;
        }
        if (previousLeftButtonHeld) {
            return false;
        }
        previousLeftButtonHeld = true;

        if ((requireGround.get().booleanValue()
                && !movement.onGround())
                || !movement.sprinting()) {
            return false;
        }

        player.customMcSetSprinting(
                false);
        return true;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetInput() {
        previousLeftButtonHeld = false;
    }
}
