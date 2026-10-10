package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import java.util.concurrent.ThreadLocalRandom;

/** Bounded rotation + confirmed vanilla crosshair-player attack automation. */
public final class Minecraft189KillAuraModule implements Module {
    public static final String ID = "combat.killAura";
    public static final String MIN_CPS = ID + ".minCps";
    public static final String MAX_CPS = ID + ".maxCps";
    public static final String RANGE = ID + ".range";
    public static final String FOV = ID + ".fov";
    public static final String ANGULAR_STEP = ID + ".angularStep";
    public static final String REQUIRE_HOLD = ID + ".requireAttackHeld";
    public static final String PAUSE_SNEAK = ID + ".pauseWhileSneaking";
    public static final String PAUSE_RIGHT = ID + ".pauseWhileRightClicking";
    public static final String PRIORITIZE_CROSSHAIR = ID + ".prioritizeCrosshair";
    public static final String SWITCH_HURT_TARGETS = ID + ".switchHurtTargets";
    public static final String MAX_SWITCH_HURT_TICKS = ID + ".maxSwitchHurtTicks";
    public static final String LOCK_TARGET = ID + ".lockTargetWhileEligible";
    private static final double MAX_AIM_ERROR = 8.0D;

    private final Setting<Integer> minCps = new Setting<Integer>(
            MIN_CPS, 8, x -> x != null && x >= 1 && x <= 20, SettingCodecs.INTEGER);
    private final Setting<Integer> maxCps = new Setting<Integer>(
            MAX_CPS, 12, x -> x != null && x >= 1 && x <= 20, SettingCodecs.INTEGER);
    private final Setting<Double> range = new Setting<Double>(
            RANGE, 3.5D, x -> x != null && Double.isFinite(x)
                    && x >= 0.5D && x <= 6.0D, SettingCodecs.DOUBLE);
    private final Setting<Double> fov = new Setting<Double>(
            FOV, 90.0D, x -> x != null && Double.isFinite(x)
                    && x >= 1.0D && x <= 180.0D, SettingCodecs.DOUBLE);
    private final Setting<Double> angularStep = new Setting<Double>(
            ANGULAR_STEP, 10.0D, x -> x != null && Double.isFinite(x)
                    && x >= 0.5D && x <= 180.0D, SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireHold = new Setting<Boolean>(
            REQUIRE_HOLD, Boolean.FALSE, x -> x != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseSneak = new Setting<Boolean>(
            PAUSE_SNEAK, Boolean.TRUE, x -> x != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseRight = new Setting<Boolean>(
            PAUSE_RIGHT, Boolean.TRUE, x -> x != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> prioritizeCrosshair = new Setting<Boolean>(
            PRIORITIZE_CROSSHAIR, Boolean.FALSE, x -> x != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> switchHurt = new Setting<Boolean>(
            SWITCH_HURT_TARGETS, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> maxSwitchHurtTicks = new Setting<Integer>(
            MAX_SWITCH_HURT_TICKS, 1,
            value -> value != null && value >= 0 && value <= 20,
            SettingCodecs.INTEGER);
    private final Setting<Boolean> lockTarget = new Setting<Boolean>(
            LOCK_TARGET, Boolean.FALSE, value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    private int lockedTargetIndex = -1;
    private int phase;
    private int sampledCps;
    private int lastMin = -1;
    private int lastMax = -1;
    private int lastEntityIndex = -1;

    @Override
    public String id() { return ID; }
    public Setting<Integer> minCpsSetting() { return minCps; }
    public Setting<Integer> maxCpsSetting() { return maxCps; }
    public Setting<Double> rangeSetting() { return range; }
    public Setting<Double> fovSetting() { return fov; }
    public Setting<Double> angularStepSetting() { return angularStep; }
    public Setting<Boolean> requireHoldSetting() { return requireHold; }
    public Setting<Boolean> pauseSneakSetting() { return pauseSneak; }
    public Setting<Boolean> pauseRightSetting() { return pauseRight; }
    public Setting<Boolean> prioritizeCrosshairSetting() { return prioritizeCrosshair; }
    public Setting<Boolean> switchHurtTargetsSetting() { return switchHurt; }
    public Setting<Integer> maxSwitchHurtTicksSetting() { return maxSwitchHurtTicks; }
    public Setting<Boolean> lockTargetSetting() { return lockTarget; }
    /** Index is retained only while this module is active and lock is enabled. */
    synchronized int lockedTargetIndex() {
        return enabled && lockTarget.get() ? lockedTargetIndex : -1;
    }
    synchronized void rememberSelectedTarget(final int index) {
        lockedTargetIndex = enabled && lockTarget.get() && index >= 0 ? index : -1;
    }
    synchronized boolean active() { return enabled; }

    @Override
    public synchronized void onEnable() { enabled = true; clear(); }
    @Override
    public synchronized void onDisable() { enabled = false; clear(); }
    synchronized void suspend() { clear(); }

    synchronized boolean aim(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final boolean attackHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (player == null || !eligible(target, attackHeld, movement)
                || rotation == null || !rotation.available()) {
            clear();
            return false;
        }
        final double dyaw = wrap(target.yaw() - rotation.yaw());
        final double dpitch = target.pitch() - rotation.pitch();
        if (!Double.isFinite(dyaw) || !Double.isFinite(dpitch)
                || Math.abs(dyaw) > fov.get() || Math.abs(dpitch) > fov.get()) {
            clear();
            return false;
        }
        if (lastEntityIndex != target.entityIndex()) {
            // Switching the confirmed attack owner resets CPS credit, but
            // must not discard a freshly source-validated sticky selection.
            resetSchedule();
            lastEntityIndex = target.entityIndex();
        }
        final double step = angularStep.get();
        final float nextYaw = (float) wrap(rotation.yaw() + clamp(dyaw, -step, step));
        final float nextPitch = (float) clamp(
                rotation.pitch() + clamp(dpitch, -step, step), -90.0D, 90.0D);
        if (Float.compare(nextYaw, rotation.yaw()) != 0)
            player.customMcSetRotationYaw(nextYaw);
        if (Float.compare(nextPitch, rotation.pitch()) != 0)
            player.customMcSetRotationPitch(nextPitch);
        return true;
    }

    synchronized boolean shouldClick(
            final boolean verifiedCrosshairPlayer,
            final int verifiedPlayerIndex,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final boolean attackHeld,
            final boolean rightHeld,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        if (!verifiedCrosshairPlayer || suspended
                || (pauseRight.get() && rightHeld)
                || !eligible(target, attackHeld, movement)
                || rotation == null || !rotation.available()
                || target.entityIndex() != lastEntityIndex
                || verifiedPlayerIndex != target.entityIndex()
                || Math.abs(wrap(target.yaw() - rotation.yaw())) > MAX_AIM_ERROR
                || Math.abs(target.pitch() - rotation.pitch()) > MAX_AIM_ERROR) {
            // Losing a confirmed ray hit drops click credit but does not
            // forget a still-eligible rotation target in this host tick.
            resetSchedule();
            return false;
        }
        final int min = minCps.get();
        final int max = maxCps.get();
        if (min != lastMin || max != lastMax) {
            phase = 0;
            sampledCps = 0;
            lastMin = min;
            lastMax = max;
        }
        if (sampledCps == 0) sampledCps = nextCps(min, max);
        phase += sampledCps;
        if (phase < 20) return false;
        phase -= 20;
        sampledCps = nextCps(min, max);
        return true;
    }

    private boolean eligible(
            final Minecraft189TargetRotationState.Snapshot target,
            final boolean held,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return enabled && target != null && target.available()
                && target.entityIndex() >= 0
                && Double.isFinite(target.distance())
                && target.distance() <= range.get()
                && (!requireHold.get() || held)
                && (!pauseSneak.get()
                    || (movement != null && movement.available() && !movement.sneaking()));
    }

    private static double wrap(final double angle) {
        double value = angle % 360.0D;
        if (value < -180.0D) value += 360.0D;
        if (value >= 180.0D) value -= 360.0D;
        return value;
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(val, max));
    }

    private int nextCps(final int a, final int b) {
        final int min = Math.min(a, b);
        final int max = Math.max(a, b);
        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void clear() {
        resetSchedule();
        lastEntityIndex = -1;
        lockedTargetIndex = -1;
    }

    private void resetSchedule() {
        phase = 0;
        sampledCps = 0;
        lastMin = -1;
        lastMax = -1;
    }
}
