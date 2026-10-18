package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

/**
 * Local-player orbit motion around an existing certified nearest-player
 * snapshot. This does not assert collision visibility or server acceptance.
 */
public final class Minecraft189TargetStrafeModule implements Module {
    public static final String ID = "movement.targetStrafe";
    public static final String SPEED_SETTING_ID =
            "movement.targetStrafe.speed";
    public static final String RADIUS_SETTING_ID =
            "movement.targetStrafe.radius";
    public static final String MAX_DISTANCE_SETTING_ID =
            "movement.targetStrafe.maxDistance";
    public static final String CLOCKWISE_SETTING_ID =
            "movement.targetStrafe.clockwise";
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "movement.targetStrafe.requireForward";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.targetStrafe.groundOnly";
    private final Setting<Double> speed = new Setting<Double>(
            SPEED_SETTING_ID, 0.28D,
            value -> value != null && Double.isFinite(value) && value >= 0.05D && value <= 1.0D, SettingCodecs.DOUBLE);
    private final Setting<Double> radius = new Setting<Double>(
            RADIUS_SETTING_ID, 3.0D,
            value -> value != null && Double.isFinite(value) && value >= 1.0D && value <= 6.0D, SettingCodecs.DOUBLE);
    private final Setting<Double> maxDistance = new Setting<Double>(
            MAX_DISTANCE_SETTING_ID, 6.0D,
            value -> value != null && Double.isFinite(value) && value >= 2.0D && value <= 12.0D, SettingCodecs.DOUBLE);
    private final Setting<Boolean> clockwise = new Setting<Boolean>(
            CLOCKWISE_SETTING_ID, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireForward = new Setting<Boolean>(
            REQUIRE_FORWARD_SETTING_ID, Boolean.TRUE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> groundOnly = new Setting<Boolean>(
            GROUND_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Local-player native X/Z getters may revoke this activation.
    private long nativeReadEpoch;

    @Override public String id() { return ID; }
    public Setting<Double> speedSetting() { return speed; }
    public Setting<Double> radiusSetting() { return radius; }
    public Setting<Double> maxDistanceSetting() { return maxDistance; }
    public Setting<Boolean> clockwiseSetting() { return clockwise; }
    public Setting<Boolean> requireForwardSetting() { return requireForward; }
    public Setting<Boolean> groundOnlySetting() { return groundOnly; }

    @Override public synchronized void onEnable() {
        enabled = true;
        nativeReadEpoch++;
    }
    @Override public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
    }
    synchronized boolean active() { return enabled; }

    synchronized boolean apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerPositionState.Snapshot local,
            final Minecraft189NearestPlayerTargetState.Snapshot target,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld,
            final boolean suspended) {
        if (!enabled || suspended || player == null
                || local == null || !local.available()
                || target == null || !target.available() || !target.found()
                || (requireForward.get().booleanValue() && !forwardHeld)
                || (groundOnly.get().booleanValue()
                        && (movement == null || !movement.available()
                                || !movement.onGround()))
                || !Double.isFinite(target.distance())
                || target.distance() > maxDistance.get().doubleValue()) {
            return false;
        }
        final double dx = local.x() - target.x();
        final double dz = local.z() - target.z();
        final double distance = Math.hypot(dx, dz);
        // Never manufacture a motion vector at a coincident or invalid
        // target; no guessed attack range, ray cast or collision path.
        if (!Double.isFinite(distance) || distance < 0.000001D) {
            return false;
        }
        final long epoch = nativeReadEpoch;
        final double configuredSpeed = speed.get().doubleValue();
        final double configuredRadius = radius.get().doubleValue();
        final double configuredMaxDistance = maxDistance.get().doubleValue();
        final boolean configuredClockwise = clockwise.get().booleanValue();
        final boolean needsForward = requireForward.get().booleanValue();
        final boolean needsGround = groundOnly.get().booleanValue();
        final double rx = dx / distance;
        final double rz = dz / distance;
        final double turn = configuredClockwise ? 1.0D : -1.0D;
        final double correction = Math.max(-0.70D, Math.min(0.70D,
                (distance - configuredRadius) / configuredRadius));
        final double vx = -rz * turn - rx * correction;
        final double vz = rx * turn - rz * correction;
        final double norm = Math.hypot(vx, vz);
        if (!Double.isFinite(norm) || norm < 0.000001D) {
            return false;
        }
        // Canonicalize signed and tiny floating-point zero so a tangent
        // aligned to one axis never produces an unnecessary -0.0 write.
        final double rawX = vx / norm * configuredSpeed;
        final double rawZ = vz / norm * configuredSpeed;
        final double nextX = Math.abs(rawX) < 1.0E-12D ? 0.0D : rawX;
        final double nextZ = Math.abs(rawZ) < 1.0E-12D ? 0.0D : rawZ;
        final double currentX = player.customMcMotionX();
        if (!nativeReadOwned(epoch, configuredSpeed, configuredRadius,
                configuredMaxDistance, configuredClockwise,
                needsForward, needsGround, movement, forwardHeld)) return false;
        final double currentZ = player.customMcMotionZ();
        if (!nativeReadOwned(epoch, configuredSpeed, configuredRadius,
                configuredMaxDistance, configuredClockwise,
                needsForward, needsGround, movement, forwardHeld)) return false;
        if (Double.compare(currentX, nextX) != 0) {
            player.customMcSetMotionX(nextX);
            if (!nativeReadOwned(epoch, configuredSpeed, configuredRadius,
                    configuredMaxDistance, configuredClockwise,
                    needsForward, needsGround, movement, forwardHeld)) return false;
        }
        if (Double.compare(currentZ, nextZ) != 0) {
            player.customMcSetMotionZ(nextZ);
        }
        // Eligible no-op frames still own horizontal motion, preventing
        // Strafe from overwriting the orbit with yaw-relative input.
        return true;
    }

    private boolean nativeReadOwned(final long epoch,
            final double configuredSpeed, final double configuredRadius,
            final double configuredMaxDistance, final boolean configuredClockwise,
            final boolean needsForward, final boolean needsGround,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld) {
        return enabled && nativeReadEpoch == epoch
                && speed.get().doubleValue() == configuredSpeed
                && radius.get().doubleValue() == configuredRadius
                && maxDistance.get().doubleValue() == configuredMaxDistance
                && clockwise.get().booleanValue() == configuredClockwise
                && requireForward.get().booleanValue() == needsForward
                && groundOnly.get().booleanValue() == needsGround
                && (!needsForward || forwardHeld)
                && (!needsGround || (movement != null
                    && movement.available() && movement.onGround()));
    }
}
