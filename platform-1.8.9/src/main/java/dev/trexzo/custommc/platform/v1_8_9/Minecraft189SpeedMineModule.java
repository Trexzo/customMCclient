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
    public static final String REQUIRE_ATTACK_HELD_SETTING_ID =
            "player.speedMine.requireAttackHeld";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "player.speedMine.pauseWhileSneaking";

    public static final String GROUND_ONLY_SETTING_ID =
            "player.speedMine.groundOnly";
    public static final String AIRBORNE_OVERRIDE_SETTING_ID =
            "player.speedMine.airborneOverride";
    public static final String AIRBORNE_PROGRESS_SETTING_ID =
            "player.speedMine.airborneProgressPercent";

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

    private final Setting<Boolean> requireAttackHeld =
            new Setting<Boolean>(
                    REQUIRE_ATTACK_HELD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(
                    GROUND_ONLY_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> airborneOverride = new Setting<Boolean>(
            AIRBORNE_OVERRIDE_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> airborneProgressPercent = new Setting<Integer>(
            AIRBORNE_PROGRESS_SETTING_ID, 70,
            value -> value != null && value >= 0 && value <= 100,
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

    public Setting<Boolean> requireAttackHeldSetting() {
        return requireAttackHeld;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> airborneOverrideSetting() {
        return airborneOverride;
    }

    public Setting<Integer> airborneProgressPercentSetting() {
        return airborneProgressPercent;
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
        apply(controller, false, null);
    }

    synchronized void apply(
            final Minecraft189BlockMiningControl controller,
            final boolean attackHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled
                || controller == null
                || !controller.customMcIsHittingBlock()
                || (requireAttackHeld.get().booleanValue()
                        && !attackHeld)
                || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null
                        || !movement.available()
                        || movement.sneaking()))
                || (groundOnly.get().booleanValue()
                        && (movement == null
                        || !movement.available()
                        || !movement.onGround()))) {
            return;
        }

        final float current =
                controller.customMcBlockDamageProgress();
        // Select the alternate cap only from confirmed mapped airborne
        // movement. Missing movement falls back to the original minimum.
        // Ground Only has already vetoed all airborne writes above.
        final boolean airborne = airborneOverride.get().booleanValue()
                && movement != null && movement.available()
                && !movement.onGround();
        final float minimum =
                (airborne ? airborneProgressPercent.get().intValue()
                        : progressPercent.get().intValue()) / 100.0F;
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
