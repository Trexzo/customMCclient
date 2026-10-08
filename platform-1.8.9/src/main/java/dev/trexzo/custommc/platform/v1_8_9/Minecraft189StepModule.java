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
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.step.groundOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.step.pauseWhileSneaking";
    public static final int DEFAULT_HEIGHT_PERCENT = 100;
    public static final float VANILLA_STEP_HEIGHT = 0.6F;

    private final Setting<Integer> heightPercent =
            new Setting<Integer>(
                    HEIGHT_SETTING_ID,
                    DEFAULT_HEIGHT_PERCENT,
                    value -> value >= 60
                            && value <= 250,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(
                    GROUND_ONLY_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> heightPercentSetting() {
        return heightPercent;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
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
        apply(player, null);
    }

    synchronized void apply(
            final Minecraft189PlayerStepControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (player == null) {
            return;
        }

        final boolean groundGate = groundOnly.get().booleanValue();
        final boolean sneakGate = pauseWhileSneaking.get().booleanValue();
        final boolean permitted =
                enabled && ((!groundGate && !sneakGate)
                        || (movement != null && movement.available()
                        && (!groundGate || movement.onGround())
                        && (!sneakGate || !movement.sneaking())));
        // Restoring vanilla on failed authority is essential: simply
        // skipping writes would retain a stale boosted step height while
        // airborne, sneaking, disconnected or disabled.
        final float target = permitted
                ? heightPercent.get().intValue() / 100.0F
                : VANILLA_STEP_HEIGHT;
        final float current = player.customMcStepHeight();
        if (!Float.isFinite(current)
                || Math.abs(current - target) > 0.000001F) {
            player.customMcSetStepHeight(target);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
