package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189StepModule
        implements Module {
    public static final String ID =
            "movement.step";
    public static final String HEIGHT_SETTING_ID =
            "movement.step.heightPercent";
    public static final int DEFAULT_HEIGHT_PERCENT = 100;
    public static final float VANILLA_STEP_HEIGHT = 0.6F;

    private final Setting<Integer> heightPercent =
            new Setting<Integer>(
                    HEIGHT_SETTING_ID,
                    DEFAULT_HEIGHT_PERCENT,
                    value -> value >= 60
                            && value <= 250,
                    SettingCodecs.INTEGER);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> heightPercentSetting() {
        return heightPercent;
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
            final Minecraft189PlayerStepControl player) {
        if (player == null) {
            return;
        }

        final float target =
                enabled
                        ? heightPercent.get().intValue() / 100.0F
                        : VANILLA_STEP_HEIGHT;
        if (Math.abs(
                player.customMcStepHeight()
                        - target) > 0.000001F) {
            player.customMcSetStepHeight(
                    target);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
