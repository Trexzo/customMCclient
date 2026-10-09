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
    public static final String GROUND_ONLY_SETTING_ID =
            "combat.velocity.groundOnly";
    public static final String AIRBORNE_ONLY_SETTING_ID =
            "combat.velocity.airborneOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "combat.velocity.pauseWhileSneaking";
    public static final String AIRBORNE_OVERRIDE_SETTING_ID =
            "combat.velocity.airborneOverride";
    public static final String AIRBORNE_HORIZONTAL_SETTING_ID =
            "combat.velocity.airborneHorizontalPercent";
    public static final String AIRBORNE_VERTICAL_SETTING_ID =
            "combat.velocity.airborneVerticalPercent";
    public static final int DEFAULT_PERCENT = 0;
    public static final int MINIMUM_PERCENT = 0;
    public static final int MAXIMUM_PERCENT = 200;

    private final Setting<Integer> horizontalPercent =
            new Setting<Integer>(
                    HORIZONTAL_SETTING_ID,
                    DEFAULT_PERCENT,
                    Minecraft189VelocityModule::validPercent,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> verticalPercent =
            new Setting<Integer>(
                    VERTICAL_SETTING_ID,
                    DEFAULT_PERCENT,
                    Minecraft189VelocityModule::validPercent,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> onlyWhileSprinting =
            new Setting<Boolean>(
                    ONLY_WHILE_SPRINTING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> groundOnly = new Setting<Boolean>(
            GROUND_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> airborneOnly = new Setting<Boolean>(
            AIRBORNE_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking = new Setting<Boolean>(
            PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);

    private final Setting<Boolean> airborneOverride =
            new Setting<Boolean>(
                    AIRBORNE_OVERRIDE_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> airborneHorizontalPercent =
            new Setting<Integer>(
                    AIRBORNE_HORIZONTAL_SETTING_ID,
                    DEFAULT_PERCENT,
                    Minecraft189VelocityModule::validPercent,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> airborneVerticalPercent =
            new Setting<Integer>(
                    AIRBORNE_VERTICAL_SETTING_ID,
                    DEFAULT_PERCENT,
                    Minecraft189VelocityModule::validPercent,
                    SettingCodecs.INTEGER);

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

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> airborneOnlySetting() {
        return airborneOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Boolean> airborneOverrideSetting() {
        return airborneOverride;
    }

    public Setting<Integer> airborneHorizontalPercentSetting() {
        return airborneHorizontalPercent;
    }

    public Setting<Integer> airborneVerticalPercentSetting() {
        return airborneVerticalPercent;
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
                        ? (useAirbornePercent(movement)
                                ? airborneHorizontalPercent.get().intValue()
                                : horizontalPercent.get().intValue())
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
                        ? (useAirbornePercent(movement)
                                ? airborneVerticalPercent.get().intValue()
                                : verticalPercent.get().intValue())
                        : 100);
    }

    private boolean shouldScale(
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled) {
            return false;
        }
        // Opt-in movement authority guards both knockback axes. Missing
        // or stale state returns vanilla delta, never a scaled guess.
        final boolean ground = groundOnly.get().booleanValue();
        final boolean airborne = airborneOnly.get().booleanValue();
        final boolean pauseSneak = pauseWhileSneaking.get().booleanValue();
        final boolean sprint = onlyWhileSprinting.get().booleanValue();
        // Contradictory Ground Only + Airborne Only settings cannot qualify.
        // Neither setting silently overrides the other.
        if (ground && airborne) {
            return false;
        }
        if (!ground && !airborne && !pauseSneak && !sprint) {
            return true;
        }
        if (movement == null || !movement.available()) {
            return false;
        }
        return (!ground || movement.onGround())
                && (!airborne || !movement.onGround())
                && (!pauseSneak || !movement.sneaking())
                && (!sprint || movement.sprinting());
    }

    private boolean useAirbornePercent(
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return airborneOverride.get().booleanValue()
                && movement != null
                && movement.available()
                && !movement.onGround();
    }

    synchronized boolean active() {
        return enabled;
    }

    private static boolean validPercent(final Integer value) {
        return value != null
                && value >= MINIMUM_PERCENT
                && value <= MAXIMUM_PERCENT;
    }

    private static double adjust(
            final double before,
            final double after,
            final int percent) {
        // Exact endpoints preserve signed zero, vanilla 100%, and avoid
        // zero times an overflowing motion delta.
        if (percent == 100) {
            return after;
        }
        if (percent == 0) {
            return Double.isFinite(before) ? before : after;
        }
        if (!Double.isFinite(before) || !Double.isFinite(after)) {
            // The incoming update is authoritative when motion is invalid.
            return after;
        }
        final double fraction = percent / 100.0D;
        double scaled = before + (after - before) * fraction;
        if (!Double.isFinite(scaled) && percent < 100) {
            // A finite weighted interpolation can remain representable
            // even when the raw difference overflows.
            scaled = before * (1.0D - fraction) + after * fraction;
        }
        // Never fabricate NaN/Infinity when amplifying extreme finite
        // input. Retain incoming vanilla motion if scaling overflows.
        return Double.isFinite(scaled) ? scaled : after;
    }
}
