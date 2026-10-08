package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Objects;

public final class Minecraft189AutoSneakModule
        implements Module {
    public static final String ID =
            "movement.autoSneak";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.autoSneak.groundOnly";
    public static final String PAUSE_SPRINTING_SETTING_ID =
            "movement.autoSneak.pauseSprinting";

    private final Setting<Boolean> groundOnly = new Setting<Boolean>(
            GROUND_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseSprinting = new Setting<Boolean>(
            PAUSE_SPRINTING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);

    private boolean enabled;

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> pauseSprintingSetting() {
        return pauseSprinting;
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
            final Minecraft189PlayerSneakControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        Objects.requireNonNull(
                movement,
                "movement");
        if (!enabled
                || player == null
                || !movement.available()
                || movement.sneaking()
                || (groundOnly.get().booleanValue() && !movement.onGround())
                || (pauseSprinting.get().booleanValue() && movement.sprinting())) {
            return;
        }
        player.customMcSetSneaking(
                true);
    }

    synchronized boolean active() {
        return enabled;
    }
}
