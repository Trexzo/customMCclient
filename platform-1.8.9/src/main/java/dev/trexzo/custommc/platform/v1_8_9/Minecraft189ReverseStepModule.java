package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189ReverseStepModule
        implements Module {
    public static final String ID =
            "movement.reverseStep";
    public static final String SPEED_SETTING_ID =
            "movement.reverseStep.speed";
    public static final String DELAY_TICKS_SETTING_ID =
            "movement.reverseStep.delayTicks";
    public static final String REQUIRE_MOVEMENT_SETTING_ID =
            "movement.reverseStep.requireMovement";
    public static final String REQUIRE_SNEAK_SETTING_ID =
            "movement.reverseStep.requireSneaking";
    public static final int MINIMUM_DELAY_TICKS = 0;
    public static final int MAXIMUM_DELAY_TICKS = 10;
    public static final double DEFAULT_SPEED =
            0.50D;
    public static final double MINIMUM_SPEED =
            0.05D;
    public static final double MAXIMUM_SPEED =
            1.50D;

    private final Setting<Double> speed =
            new Setting<Double>(
                    SPEED_SETTING_ID,
                    DEFAULT_SPEED,
                    Minecraft189ReverseStepModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Integer> delayTicks =
            new Setting<Integer>(
                    DELAY_TICKS_SETTING_ID,
                    0,
                    value -> value != null
                            && value >= MINIMUM_DELAY_TICKS
                            && value <= MAXIMUM_DELAY_TICKS,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> requireSneaking =
            new Setting<Boolean>(
                    REQUIRE_SNEAK_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireMovement =
            new Setting<Boolean>(
                    REQUIRE_MOVEMENT_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    private boolean previousAvailable;
    private boolean previousOnGround;
    // -1 means there is no armed ground-to-air transition. A value of
    // zero fires on this callback; positive values count skipped airborne
    // callbacks after the actual departure.
    private int pendingDelay = -1;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> speedSetting() {
        return speed;
    }

    public Setting<Integer> delayTicksSetting() {
        return delayTicks;
    }

    public Setting<Boolean> requireSneakingSetting() {
        return requireSneaking;
    }

    public Setting<Boolean> requireMovementSetting() {
        return requireMovement;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetTransition();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetTransition();
    }

    synchronized boolean apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        // Older callers have no input authority. Under the opt-in
        // movement gate they conservatively reject the transition.
        return apply(player, movement, suspended, false);
    }

    synchronized boolean apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended,
            final boolean movementHeld) {
        if (movement == null || !movement.available()) {
            resetTransition();
            return false;
        }

        final boolean currentOnGround = movement.onGround();
        final boolean transitionedOffGround = previousAvailable
                && previousOnGround && !currentOnGround;
        previousAvailable = true;
        previousOnGround = currentOnGround;

        if (currentOnGround) {
            // Landing clears the unconsumed transition immediately.
            pendingDelay = -1;
            return false;
        }
        if (!enabled || suspended || player == null
                || (requireSneaking.get().booleanValue()
                        && !movement.sneaking())
                || (requireMovement.get().booleanValue()
                        && !movementHeld)) {
            // No catch-up after higher-priority ownership, state loss or
            // releasing sneak during a pending delayed departure.
            pendingDelay = -1;
            return false;
        }
        if (transitionedOffGround) {
            pendingDelay = delayTicks.get().intValue();
        }
        if (pendingDelay < 0) {
            return false;
        }
        if (pendingDelay > 0) {
            pendingDelay--;
            return false;
        }
        // Consume exactly once per real grounded -> airborne transition,
        // including when the player is rising or already descending faster.
        pendingDelay = -1;
        final double currentMotionY = player.customMcMotionY();
        if (!Double.isFinite(currentMotionY) || currentMotionY > 0.0D) {
            return false;
        }

        final double targetMotionY = -speed.get().doubleValue();
        if (currentMotionY > targetMotionY) {
            player.customMcSetMotionY(targetMotionY);
            return true;
        }
        return false;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetTransition() {
        previousAvailable = false;
        previousOnGround = false;
        pendingDelay = -1;
    }

    private static boolean validSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_SPEED
                && value.doubleValue() <= MAXIMUM_SPEED;
    }
}
