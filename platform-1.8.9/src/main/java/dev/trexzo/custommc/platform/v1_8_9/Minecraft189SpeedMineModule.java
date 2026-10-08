package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189SpeedMineModule
        implements Module {
    public static final String ID =
            "player.speedMine";
    public static final String PROGRESS_SETTING_ID =
            "player.speedMine.progressPercent";
    public static final String PROGRESSIVE_SETTING_ID =
            "player.speedMine.progressive";
    public static final String STEP_PERCENT_SETTING_ID =
            "player.speedMine.stepPercent";

    private final Setting<Integer> progressPercent =
            new Setting<Integer>(
                    PROGRESS_SETTING_ID,
                    70,
                    value -> value >= 0
                            && value <= 100,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> progressive =
            new Setting<Boolean>(
                    PROGRESSIVE_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> stepPercent =
            new Setting<Integer>(
                    STEP_PERCENT_SETTING_ID,
                    10,
                    value -> value != null && value >= 1 && value <= 50,
                    SettingCodecs.INTEGER);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> progressPercentSetting() {
        return progressPercent;
    }

    public Setting<Boolean> progressiveSetting() {
        return progressive;
    }

    public Setting<Integer> stepPercentSetting() {
        return stepPercent;
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
            final Minecraft189BlockMiningControl controller) {
        if (!enabled
                || controller == null
                || !controller.customMcIsHittingBlock()) {
            return;
        }

        final float current =
                controller.customMcBlockDamageProgress();
        final float minimum =
                progressPercent.get().intValue()
                        / 100.0F;
        if (!Float.isFinite(current) || current < 0.0F
                || current >= minimum) {
            return;
        }

        // The legacy instant minimum remains the default. Progressive mode
        // advances only the locally mapped active break-progress field and
        // never writes above the configured minimum or above full progress.
        final float next = progressive.get().booleanValue()
                ? Math.min(minimum,
                        current + stepPercent.get().intValue() / 100.0F)
                : minimum;
        if (next > current) {
            controller.customMcSetBlockDamageProgress(next);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
