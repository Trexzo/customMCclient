package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189VelocityModule
        implements Module {
    public static final String ID =
            "combat.velocity";
    public static final String HORIZONTAL_SETTING_ID =
            "combat.velocity.horizontalPercent";
    public static final String VERTICAL_SETTING_ID =
            "combat.velocity.verticalPercent";
    public static final String ONLY_WHILE_SPRINTING_SETTING_ID =
            "combat.velocity.onlyWhileSprinting";
    public static final int DEFAULT_PERCENT = 0;
    public static final int MINIMUM_PERCENT = 0;
    public static final int MAXIMUM_PERCENT = 200;

    private final Setting<Integer> horizontalPercent =
            new Setting<Integer>(
                    HORIZONTAL_SETTING_ID,
                    DEFAULT_PERCENT,
                    value -> value >= MINIMUM_PERCENT
                            && value <= MAXIMUM_PERCENT,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> verticalPercent =
            new Setting<Integer>(
                    VERTICAL_SETTING_ID,
                    DEFAULT_PERCENT,
                    value -> value >= MINIMUM_PERCENT
                            && value <= MAXIMUM_PERCENT,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> onlyWhileSprinting =
            new Setting<Boolean>(
                    ONLY_WHILE_SPRINTING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> horizontalPercentSetting() {
        return horizontalPercent;
    }

    public Setting<Integer> verticalPercentSetting() {
        return verticalPercent;
    }

    public Setting<Boolean> onlyWhileSprintingSetting() {
        return onlyWhileSprinting;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized double adjustHorizontal(
            final double before,
            final double after) {
        return adjustHorizontal(before, after, null);
    }

    synchronized double adjustHorizontal(
            final double before,
            final double after,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return adjust(
                before,
                after,
                shouldScale(movement)
                        ? horizontalPercent.get().intValue()
                        : 100);
    }

    synchronized double adjustVertical(
            final double before,
            final double after) {
        return adjustVertical(before, after, null);
    }

    synchronized double adjustVertical(
            final double before,
            final double after,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return adjust(
                before,
                after,
                shouldScale(movement)
                        ? verticalPercent.get().intValue()
                        : 100);
    }

    private boolean shouldScale(
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return enabled
                && (!onlyWhileSprinting.get().booleanValue()
                || (movement != null && movement.available()
                && movement.sprinting()));
    }

    synchronized boolean active() {
        return enabled;
    }

    private static double adjust(
            final double before,
            final double after,
            final int percent) {
        return before
                + (after - before)
                * (percent / 100.0D);
    }
}
