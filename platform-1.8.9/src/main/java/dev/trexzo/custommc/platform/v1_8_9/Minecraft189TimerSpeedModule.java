package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189TimerSpeedModule
        implements Module {
    public static final String ID =
            "player.timer";
    public static final String SPEED_SETTING_ID =
            "player.timer.speedPercent";
    public static final String AIRBORNE_OVERRIDE_SETTING_ID =
            "player.timer.airborneOverride";
    public static final String AIRBORNE_SPEED_SETTING_ID =
            "player.timer.airborneSpeedPercent";
    public static final String SMOOTH_TRANSITION_SETTING_ID =
            "player.timer.smoothTransition";
    public static final String SPRINT_ONLY_SETTING_ID =
            "player.timer.sprintOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "player.timer.pauseWhileSneaking";
    public static final String GROUND_ONLY_SETTING_ID =
            "player.timer.groundOnly";
    public static final String PAUSE_GUI_SETTING_ID =
            "player.timer.pauseWhileClickGuiOpen";
    public static final String TRANSITION_STEP_SETTING_ID =
            "player.timer.transitionStepPercent";
    public static final int DEFAULT_TRANSITION_STEP_PERCENT = 25;
    public static final int MINIMUM_TRANSITION_STEP_PERCENT = 5;
    public static final int MAXIMUM_TRANSITION_STEP_PERCENT = 100;
    public static final int MINIMUM_SPEED_PERCENT = 10;
    public static final int MAXIMUM_SPEED_PERCENT = 300;

    private final Setting<Integer> speedPercent =
            new Setting<Integer>(
                    SPEED_SETTING_ID,
                    100,
                    value -> value != null
                            && value >= MINIMUM_SPEED_PERCENT
                            && value <= MAXIMUM_SPEED_PERCENT,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> airborneOverride =
            new Setting<Boolean>(
                    AIRBORNE_OVERRIDE_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> airborneSpeedPercent =
            new Setting<Integer>(
                    AIRBORNE_SPEED_SETTING_ID,
                    100,
                    value -> value != null
                            && value >= MINIMUM_SPEED_PERCENT
                            && value <= MAXIMUM_SPEED_PERCENT,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> smoothTransition =
            new Setting<Boolean>(
                    SMOOTH_TRANSITION_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> transitionStepPercent =
            new Setting<Integer>(
                    TRANSITION_STEP_SETTING_ID,
                    DEFAULT_TRANSITION_STEP_PERCENT,
                    value -> value != null
                            && value >= MINIMUM_TRANSITION_STEP_PERCENT
                            && value <= MAXIMUM_TRANSITION_STEP_PERCENT,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> sprintOnly = new Setting<Boolean>(
            SPRINT_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking = new Setting<Boolean>(
            PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    // Default-OFF rules read the existing native player and actual
    // ClickGUI model each tick. No synthetic world position is inferred.
    private final Setting<Boolean> groundOnly = new Setting<Boolean>(
            GROUND_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseClickGui = new Setting<Boolean>(
            PAUSE_GUI_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // The mapped game Timer getter may reenter module lifecycle/GUI policy.
    private long nativeReadEpoch;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> speedPercentSetting() {
        return speedPercent;
    }

    public Setting<Boolean> airborneOverrideSetting() {
        return airborneOverride;
    }

    public Setting<Integer> airborneSpeedPercentSetting() {
        return airborneSpeedPercent;
    }

    public Setting<Boolean> smoothTransitionSetting() {
        return smoothTransition;
    }

    public Setting<Boolean> sprintOnlySetting() {
        return sprintOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Integer> transitionStepPercentSetting() {
        return transitionStepPercent;
    }

    public Setting<Boolean> groundOnlySetting() { return groundOnly; }
    public Setting<Boolean> pauseClickGuiSetting() { return pauseClickGui; }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        nativeReadEpoch++;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
    }

    synchronized void apply(
            final Minecraft189TimerSpeedControl timer) {
        apply(timer, null);
    }

    synchronized void apply(
            final Minecraft189TimerSpeedControl timer,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        apply(timer, movement, false);
    }

    synchronized void apply(
            final Minecraft189TimerSpeedControl timer,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean actualClickGuiOpen) {
        if (timer == null) {
            return;
        }

        final long epoch = nativeReadEpoch;
        // Persisted timer settings determine both the target and whether
        // the REAL game clock is permitted to remain accelerated. Capture
        // ALL policy before the external mapped Timer read.
        final int configuredSpeed = speedPercent.get().intValue();
        final boolean airOverride = airborneOverride.get().booleanValue();
        final int airSpeed = airborneSpeedPercent.get().intValue();
        final boolean smoothing = smoothTransition.get().booleanValue();
        final int transitionStep = transitionStepPercent.get().intValue();
        final boolean configuredGuiPause = pauseClickGui.get().booleanValue();
        final boolean requireSprint = sprintOnly.get().booleanValue();
        final boolean pauseSneak = pauseWhileSneaking.get().booleanValue();
        final boolean requireGround = groundOnly.get().booleanValue();
        final boolean pauseForGui = configuredGuiPause && actualClickGuiOpen;
        final boolean permitted = enabled && !pauseForGui
                && ((!requireSprint && !pauseSneak && !requireGround)
                || (movement != null && movement.available()
                    && (!requireSprint || movement.sprinting())
                    && (!pauseSneak || !movement.sneaking())
                    && (!requireGround || movement.onGround())));
        final boolean airborne = permitted
                && airOverride
                && movement != null
                && movement.available()
                && !movement.onGround();
        final int configuredPercent = airborne
                ? airSpeed : configuredSpeed;
        // A failed mapped movement gate must undo an earlier timer boost
        // immediately, not keep an obsolete accelerated world clock.
        final float target = permitted ? configuredPercent / 100.0F : 1.0F;
        final float current = timer.customMcTimerSpeed();
        if (!nativeReadOwned(epoch, configuredSpeed, airOverride, airSpeed,
                smoothing, transitionStep, configuredGuiPause, requireSprint,
                pauseSneak, requireGround)) {
            // A *disabled* Timer must release its persistent native world
            // clock to exactly 1x, even if the external read disabled it.
            // OFF->ON is a NEW owner and receives no old callback writes.
            // A live GUI policy edit also cannot spend the old timer read.
            if (!enabled && (!Float.isFinite(current)
                    || Math.abs(current - 1.0F) > 0.000001F)) {
                timer.customMcSetTimerSpeed(1.0F);
            }
            return;
        }
        // Disabling Timer restores 1x instantly even if smoothing was
        // active; valid enabled owners use current genuine native state.
        final float next = permitted && smoothing
                ? stepToward(current, target, transitionStep)
                : target;
        if (!Float.isFinite(current)
                || Math.abs(current - next) > 0.000001F) {
            timer.customMcSetTimerSpeed(next);
        }
    }

    private boolean nativeReadOwned(final long epoch,
            final int configuredSpeed, final boolean airOverride,
            final int airSpeed, final boolean smoothing, final int transitionStep,
            final boolean configuredGuiPause, final boolean requireSprint,
            final boolean pauseSneak, final boolean requireGround) {
        // A disabled call is still authorized to restore the true 1x clock
        // if its own epoch/policy stayed unchanged across source getter.
        return nativeReadEpoch == epoch
                && speedPercent.get().intValue() == configuredSpeed
                && airborneOverride.get().booleanValue() == airOverride
                && airborneSpeedPercent.get().intValue() == airSpeed
                && smoothTransition.get().booleanValue() == smoothing
                && transitionStepPercent.get().intValue() == transitionStep
                && pauseClickGui.get().booleanValue() == configuredGuiPause
                && sprintOnly.get().booleanValue() == requireSprint
                && pauseWhileSneaking.get().booleanValue() == pauseSneak
                && groundOnly.get().booleanValue() == requireGround;
    }

    static float stepToward(
            final float current,
            final float target,
            final int stepPercent) {
        if (!Float.isFinite(current) || !Float.isFinite(target)
                || stepPercent < MINIMUM_TRANSITION_STEP_PERCENT
                || stepPercent > MAXIMUM_TRANSITION_STEP_PERCENT) {
            return target;
        }
        final float maxDelta = stepPercent / 100.0F;
        final float difference = target - current;
        if (!Float.isFinite(difference)
                || Math.abs(difference) <= maxDelta + 0.000001F) {
            return target;
        }
        return current + Math.copySign(maxDelta, difference);
    }

    synchronized boolean active() {
        return enabled;
    }
}
