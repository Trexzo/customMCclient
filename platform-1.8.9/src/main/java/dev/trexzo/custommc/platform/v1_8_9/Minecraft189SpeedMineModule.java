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

    private final Setting<Integer> progressPercent =
            new Setting<Integer>(
                    PROGRESS_SETTING_ID,
                    70,
                    value -> value >= 0
                            && value <= 100,
                    SettingCodecs.INTEGER);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> progressPercentSetting() {
        return progressPercent;
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
        if (current >= 0.0F
                && current < minimum) {
            controller.customMcSetBlockDamageProgress(
                    minimum);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
