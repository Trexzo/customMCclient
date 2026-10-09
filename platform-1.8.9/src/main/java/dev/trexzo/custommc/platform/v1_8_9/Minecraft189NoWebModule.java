package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoWebModule
        implements Module {
    public static final String ID =
            "movement.noWeb";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.noWeb.groundOnly";
    public static final String REQUIRE_SNEAKING_SETTING_ID =
            "movement.noWeb.requireSneaking";

    private final Setting<Boolean> groundOnly = new Setting<Boolean>(
            GROUND_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireSneaking = new Setting<Boolean>(
            REQUIRE_SNEAKING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> requireSneakingSetting() {
        return requireSneaking;
    }

    private boolean enabled;

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
            final Minecraft189PlayerWebControl player) {
        apply(player, null);
    }

    synchronized void apply(
            final Minecraft189PlayerWebControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled || player == null) {
            return;
        }
        final boolean ground = groundOnly.get().booleanValue();
        final boolean sneak = requireSneaking.get().booleanValue();
        if ((ground || sneak)
                && (movement == null || !movement.available()
                        || (ground && !movement.onGround())
                        || (sneak && !movement.sneaking()))) {
            // Existing local web flag is left intact when a configured
            // mapped-state condition is not confirmed.
            return;
        }
        if (player.customMcInWeb()) {
            player.customMcSetInWeb(false);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
