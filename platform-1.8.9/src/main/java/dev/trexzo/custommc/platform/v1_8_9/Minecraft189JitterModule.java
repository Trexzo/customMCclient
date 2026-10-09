package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class Minecraft189JitterModule
        implements Module {
    public static final String ID =
            "combat.jitter";
    public static final String YAW_SETTING_ID =
            "combat.jitter.yawDegrees";
    public static final String PITCH_SETTING_ID =
            "combat.jitter.pitchDegrees";
    public static final String YAW_ENABLED_SETTING_ID =
            "combat.jitter.yawEnabled";
    public static final String PITCH_ENABLED_SETTING_ID =
            "combat.jitter.pitchEnabled";
    public static final String INTERVAL_SETTING_ID =
            "combat.jitter.intervalTicks";
    public static final String GROUND_ONLY_SETTING_ID =
            "combat.jitter.groundOnly";
    public static final String PAUSE_SNEAKING_SETTING_ID =
            "combat.jitter.pauseWhileSneaking";
    public static final String REQUIRE_HOLD_SETTING_ID =
            "combat.jitter.requireHold";
    public static final String PAUSE_RIGHT_CLICKING_SETTING_ID =
            "combat.jitter.pauseWhileRightClicking";
    public static final String VARIABLE_STRENGTH_SETTING_ID =
            "combat.jitter.variableStrength";
    public static final String STRENGTH_VARIATION_SETTING_ID =
            "combat.jitter.strengthVariationPercent";
    public static final String RANDOM_INTERVAL_SETTING_ID =
            "combat.jitter.randomInterval";
    public static final String INTERVAL_VARIATION_SETTING_ID =
            "combat.jitter.intervalVariationTicks";
    public static final int DEFAULT_INTERVAL_VARIATION_TICKS = 2;
    public static final int MAXIMUM_INTERVAL_VARIATION_TICKS = 5;
    public static final int DEFAULT_STRENGTH_VARIATION_PERCENT = 35;
    public static final double DEFAULT_DEGREES =
            0.50D;
    public static final double MINIMUM_DEGREES =
            0.0D;
    public static final double MAXIMUM_DEGREES =
            5.0D;
    public static final int DEFAULT_INTERVAL_TICKS =
            1;
    public static final int MINIMUM_INTERVAL_TICKS =
            1;
    public static final int MAXIMUM_INTERVAL_TICKS =
            10;

    private final Setting<Double> yawDegrees =
            new Setting<Double>(
                    YAW_SETTING_ID,
                    DEFAULT_DEGREES,
                    Minecraft189JitterModule::validDegrees,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> pitchDegrees =
            new Setting<Double>(
                    PITCH_SETTING_ID,
                    DEFAULT_DEGREES,
                    Minecraft189JitterModule::validDegrees,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> yawEnabled =
            new Setting<Boolean>(
                    YAW_ENABLED_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pitchEnabled =
            new Setting<Boolean>(
                    PITCH_ENABLED_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> intervalTicks =
            new Setting<Integer>(
                    INTERVAL_SETTING_ID,
                    DEFAULT_INTERVAL_TICKS,
                    value -> value != null
                            && value >= MINIMUM_INTERVAL_TICKS
                            && value <= MAXIMUM_INTERVAL_TICKS,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> requireHold =
            new Setting<Boolean>(
                    REQUIRE_HOLD_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> pauseWhileRightClicking =
            new Setting<Boolean>(
                    PAUSE_RIGHT_CLICKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(
                    GROUND_ONLY_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_SNEAKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    private final Setting<Boolean> variableStrength =
            new Setting<Boolean>(
                    VARIABLE_STRENGTH_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> strengthVariationPercent =
            new Setting<Integer>(
                    STRENGTH_VARIATION_SETTING_ID,
                    DEFAULT_STRENGTH_VARIATION_PERCENT,
                    value -> value != null && value >= 0 && value <= 100,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> randomInterval =
            new Setting<Boolean>(
                    RANDOM_INTERVAL_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> intervalVariationTicks =
            new Setting<Integer>(
                    INTERVAL_VARIATION_SETTING_ID,
                    DEFAULT_INTERVAL_VARIATION_TICKS,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_INTERVAL_VARIATION_TICKS,
                    SettingCodecs.INTEGER);

    private final DoubleSupplier randomUnit;
    private boolean enabled;
    private boolean positivePhase = true;
    private boolean previousVariableStrength;
    private boolean previousRandomInterval;
    private int previousIntervalVariationTicks;
    private int previousIntervalTicks;
    private float pairedYawDelta;
    private float pairedPitchDelta;
    private int ticksUntilNext;

    public Minecraft189JitterModule() {
        this(() -> ThreadLocalRandom.current().nextDouble());
    }

    // An injected sequence makes variable-strength behavior reproducible in
    // regression tests without coupling the runtime to a fixed RNG seed.
    Minecraft189JitterModule(final DoubleSupplier randomUnit) {
        this.randomUnit = Objects.requireNonNull(randomUnit, "randomUnit");
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> yawDegreesSetting() {
        return yawDegrees;
    }

    public Setting<Double> pitchDegreesSetting() {
        return pitchDegrees;
    }

    public Setting<Boolean> yawEnabledSetting() {
        return yawEnabled;
    }

    public Setting<Boolean> pitchEnabledSetting() {
        return pitchEnabled;
    }

    public Setting<Integer> intervalTicksSetting() {
        return intervalTicks;
    }

    public Setting<Boolean> requireHoldSetting() {
        return requireHold;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> pauseWhileRightClickingSetting() {
        return pauseWhileRightClicking;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Boolean> variableStrengthSetting() {
        return variableStrength;
    }

    public Setting<Integer> strengthVariationPercentSetting() {
        return strengthVariationPercent;
    }

    public Setting<Boolean> randomIntervalSetting() {
        return randomInterval;
    }

    public Setting<Integer> intervalVariationTicksSetting() {
        return intervalVariationTicks;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetCadence();
        previousVariableStrength = variableStrength.get().booleanValue();
        previousRandomInterval = randomInterval.get().booleanValue();
        previousIntervalVariationTicks = intervalVariationTicks.get().intValue();
        previousIntervalTicks = intervalTicks.get().intValue();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetCadence();
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean leftButtonHeld) {
        // Existing direct callers have no player-movement authority.
        return apply(player, rotation, leftButtonHeld, null);
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean leftButtonHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        // Older callers do not supply a physical right-button snapshot,
        // and therefore fail closed when the optional guard is enabled.
        return apply(player, rotation, leftButtonHeld, movement, null);
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean leftButtonHeld,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final Boolean rightButtonHeld) {
        final boolean yawAxisEnabled =
                yawEnabled.get().booleanValue();
        final boolean pitchAxisEnabled =
                pitchEnabled.get().booleanValue();
        final boolean varying = variableStrength.get().booleanValue();
        final boolean randomTiming = randomInterval.get().booleanValue();
        final int variationTicks = intervalVariationTicks.get().intValue();
        final int configuredInterval = intervalTicks.get().intValue();
        if (varying != previousVariableStrength
                || randomTiming != previousRandomInterval
                || variationTicks != previousIntervalVariationTicks
                || configuredInterval != previousIntervalTicks) {
            resetCadence();
            previousVariableStrength = varying;
            previousRandomInterval = randomTiming;
            previousIntervalVariationTicks = variationTicks;
            previousIntervalTicks = configuredInterval;
        }
        if (!enabled
                || player == null
                || rotation == null
                || !rotation.available()
                || (!yawAxisEnabled
                        && !pitchAxisEnabled)
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)
                || (pauseWhileRightClicking.get().booleanValue()
                        && (rightButtonHeld == null
                                || rightButtonHeld.booleanValue()))
                || ((groundOnly.get().booleanValue()
                        || pauseWhileSneaking.get().booleanValue())
                        && (movement == null || !movement.available()
                                || (groundOnly.get().booleanValue()
                                        && !movement.onGround())
                                || (pauseWhileSneaking.get().booleanValue()
                                        && movement.sneaking())))) {
            resetCadence();
            return false;
        }

        if (ticksUntilNext > 0) {
            ticksUntilNext--;
            return false;
        }

        ticksUntilNext =
                sampledInterval(configuredInterval, randomTiming, variationTicks) - 1;

        final boolean outwardStroke = positivePhase;
        final double direction = outwardStroke ? 1.0D : -1.0D;
        positivePhase = !positivePhase;

        final float currentYaw = rotation.yaw();
        final float currentPitch = rotation.pitch();
        final double yawDelta = !varying
                ? yawDegrees.get().doubleValue()
                : outwardStroke
                        ? sampledDegrees(yawDegrees.get().doubleValue())
                        : pairedYawDelta;
        final double pitchDelta = !varying
                ? pitchDegrees.get().doubleValue()
                : outwardStroke
                        ? sampledDegrees(pitchDegrees.get().doubleValue())
                        : pairedPitchDelta;
        final float targetYaw =
                yawAxisEnabled
                        ? (float) (currentYaw + direction * yawDelta)
                        : currentYaw;
        final float targetPitch =
                pitchAxisEnabled
                        ? clampPitch(
                                (float) (currentPitch + direction * pitchDelta))
                        : currentPitch;

        if (varying && outwardStroke) {
            // Pair the exact applied float deltas, including pitch clipping.
            // A variable outward stroke and its return cannot accumulate
            // unbounded random-walk offsets in a stable player snapshot.
            pairedYawDelta = yawAxisEnabled ? targetYaw - currentYaw : 0.0F;
            pairedPitchDelta = pitchAxisEnabled ? targetPitch - currentPitch : 0.0F;
        }

        boolean changed = false;
        if (Float.compare(
                currentYaw,
                targetYaw) != 0) {
            player.customMcSetRotationYaw(
                    targetYaw);
            changed = true;
        }
        if (Float.compare(
                currentPitch,
                targetPitch) != 0) {
            player.customMcSetRotationPitch(
                    targetPitch);
            changed = true;
        }
        return changed;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetCadence() {
        positivePhase = true;
        ticksUntilNext = 0;
        pairedYawDelta = 0.0F;
        pairedPitchDelta = 0.0F;
    }

    private int sampledInterval(
            final int baseInterval,
            final boolean randomTiming,
            final int variationTicks) {
        if (!randomTiming || variationTicks == 0) {
            return baseInterval;
        }
        final double candidate = randomUnit.getAsDouble();
        // Invalid injection is neutral; runtime RNG always samples [0, 1).
        final double sample = Double.isFinite(candidate)
                && candidate >= 0.0D && candidate < 1.0D
                ? candidate : 0.5D;
        final int variation = (int) Math.floor(
                sample * (variationTicks * 2 + 1)) - variationTicks;
        return Math.max(MINIMUM_INTERVAL_TICKS,
                Math.min(MAXIMUM_INTERVAL_TICKS, baseInterval + variation));
    }

    private double sampledDegrees(final double configuredDegrees) {
        // Zero variation is exactly the original configured amplitude.
        final int variation = strengthVariationPercent.get().intValue();
        if (variation == 0 || configuredDegrees == 0.0D) {
            return configuredDegrees;
        }
        final double candidate = randomUnit.getAsDouble();
        // Fail closed for a bad test source; the production RNG is [0, 1).
        final double sample =
                Double.isFinite(candidate) && candidate >= 0.0D
                        && candidate < 1.0D ? candidate : 0.0D;
        return configuredDegrees * (1.0D - variation / 100.0D * sample);
    }

    private static float clampPitch(
            final float pitch) {
        if (pitch < -90.0F) {
            return -90.0F;
        }
        if (pitch > 90.0F) {
            return 90.0F;
        }
        return pitch;
    }

    private static boolean validDegrees(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_DEGREES
                && value.doubleValue() <= MAXIMUM_DEGREES;
    }
}
