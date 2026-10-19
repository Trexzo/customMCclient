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
    // A real mapped Y getter can synchronously revoke a prior departure.
    private long nativeReadEpoch;
    private boolean previousAvailable;
    private boolean previousOnGround;
    // -1 means there is no armed ground-to-air transition. A value of
    // zero fires on this callback; positive values count skipped airborne
    // callbacks after the actual departure.
    private int pendingDelay = -1;
    // A positive delayed original ground->air contact belongs to the
    // source GUI policy present at THAT departure, not new mid-air edits.
    private double departureSpeed;
    private int departureDelay;
    private boolean departureRequiresSneak;
    private boolean departureRequiresMovement;
    // Genuine EntityPlayer.motionY setter can synchronously reenter the
    // movement host before it returns; never recursively spend an edge.
    private boolean nativeYWriteDispatching;

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
        nativeReadEpoch++;
        resetTransition();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
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
        return apply(player, movement, suspended, movementHeld, false);
    }

    synchronized boolean apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended,
            final boolean movementHeld,
            final boolean actualClickGuiOpen) {
        if (nativeYWriteDispatching) {
            // This call is nested inside the real mapped Y setter.
            // The outer original departure was already spent; do not
            // update transition history or fabricate another velocity.
            return false;
        }
        // A source-native GUI focus change revokes the full historical
        // ground-to-air lease, not merely the pending delayed write. No
        // departure observed before/in GUI may replay on menu close.
        if (actualClickGuiOpen) {
            resetTransition();
            return false;
        }
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
            departureSpeed = speed.get().doubleValue();
            departureDelay = pendingDelay;
            departureRequiresSneak = requireSneaking.get().booleanValue();
            departureRequiresMovement = requireMovement.get().booleanValue();
        }
        if (pendingDelay < 0) {
            return false;
        }
        if (departureDelay > 0
                && (Double.compare(departureSpeed,
                        speed.get().doubleValue()) != 0
                    || departureDelay != delayTicks.get().intValue()
                    || departureRequiresSneak
                            != requireSneaking.get().booleanValue()
                    || departureRequiresMovement
                            != requireMovement.get().booleanValue())) {
            // An unspent delayed native departure cannot be reinterpreted
            // under an edited GUI velocity or input admission policy.
            // Demand a NEW actual grounded->airborne contact.
            pendingDelay = -1;
            return false;
        }
        if (pendingDelay > 0) {
            pendingDelay--;
            return false;
        }
        // Consume exactly once per genuine ground->air transition BEFORE
        // reading external player state: an old source callback never
        // passes the pending delay credit into a new module activation.
        pendingDelay = -1;
        final long epoch = nativeReadEpoch;
        final double configuredSpeed = speed.get().doubleValue();
        final int configuredDelay = delayTicks.get().intValue();
        final boolean requiresSneak = requireSneaking.get().booleanValue();
        final boolean requiresMovement = requireMovement.get().booleanValue();
        final double currentMotionY = player.customMcMotionY();
        if (!enabled || nativeReadEpoch != epoch
                || speed.get().doubleValue() != configuredSpeed
                || delayTicks.get().intValue() != configuredDelay
                || requireSneaking.get().booleanValue() != requiresSneak
                || requireMovement.get().booleanValue() != requiresMovement
                || (requiresSneak && !movement.sneaking())
                || (requiresMovement && !movementHeld)) {
            // No synthetic delayed replay after a reentrant GUI edit.
            return false;
        }
        if (!Double.isFinite(currentMotionY) || currentMotionY > 0.0D) {
            return false;
        }
        final double targetMotionY = -configuredSpeed;
        if (currentMotionY > targetMotionY) {
            nativeYWriteDispatching = true;
            try {
                player.customMcSetMotionY(targetMotionY);
                // Original setter may disable/re-enable ReverseStep or
                // edit speed/gates before returning. It already physically
                // wrote Y, but cannot report success to a NEW owner.
                return enabled && nativeReadEpoch == epoch
                        && Double.compare(
                                speed.get().doubleValue(),
                                configuredSpeed) == 0
                        && delayTicks.get().intValue() == configuredDelay
                        && requireSneaking.get().booleanValue()
                                == requiresSneak
                        && requireMovement.get().booleanValue()
                                == requiresMovement;
            } finally {
                nativeYWriteDispatching = false;
            }
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
