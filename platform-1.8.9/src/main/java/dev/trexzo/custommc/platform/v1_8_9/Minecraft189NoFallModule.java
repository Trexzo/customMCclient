package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoFallModule
        implements Module {
    public static final String ID =
            "movement.noFall";
    public static final String AIRBORNE_ONLY_SETTING_ID =
            "movement.noFall.airborneOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.noFall.pauseWhileSneaking";
    public static final String THRESHOLD_SETTING_ID =
            "movement.noFall.threshold";
    public static final double DEFAULT_THRESHOLD = 0.0D;
    public static final double MINIMUM_THRESHOLD = 0.0D;
    public static final double MAXIMUM_THRESHOLD = 10.0D;

    private final Setting<Double> threshold =
            new Setting<Double>(
                    THRESHOLD_SETTING_ID,
                    DEFAULT_THRESHOLD,
                    value -> value != null
                            && !Double.isNaN(value.doubleValue())
                            && !Double.isInfinite(value.doubleValue())
                            && value.doubleValue() >= MINIMUM_THRESHOLD
                            && value.doubleValue() <= MAXIMUM_THRESHOLD,
                    SettingCodecs.DOUBLE);

    private final Setting<Boolean> airborneOnly =
            new Setting<Boolean>(
                    AIRBORNE_ONLY_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    private boolean enabled;
    // Invalidates callbacks that read a native source owner before restart.
    private long nativeReadEpoch;
    // The original EntityPlayer.fallDistance mapped getter/setter may
    // synchronously reenter the host before the native float changes.
    private boolean nativeFallDispatching;
    private Minecraft189PlayerFallDistanceControl nativeFallPlayer;
    private boolean nativeFallRevoked;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> thresholdSetting() {
        return threshold;
    }

    public Setting<Boolean> airborneOnlySetting() {
        return airborneOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

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
            final Minecraft189PlayerFallDistanceControl player) {
        apply(player, null);
    }

    synchronized void apply(
            final Minecraft189PlayerFallDistanceControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        apply(player, movement, false);
    }

    synchronized void apply(
            final Minecraft189PlayerFallDistanceControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean actualClickGuiOpen) {
        // A nested real host callback must never authorize a second zero
        // from one native getter/setter stack. Changed source authority
        // (different player, GUI, missing movement, ground/sneak veto)
        // additionally invalidates the outer original fall read.
        if (nativeFallDispatching) {
            final boolean requiresAirborne =
                    airborneOnly.get().booleanValue();
            final boolean pauseSneak =
                    pauseWhileSneaking.get().booleanValue();
            if (!enabled || player != nativeFallPlayer || actualClickGuiOpen
                    || ((requiresAirborne || pauseSneak)
                        && (movement == null || !movement.available()
                            || (requiresAirborne && movement.onGround())
                            || (pauseSneak && movement.sneaking())))) {
                nativeFallRevoked = true;
            }
            return;
        }
        // Preserve the original positive fallDistance while the GUI
        // owns focus. Never overwrite native fall evidence on a menu
        // callback, even though NoFall remains enabled.
        if (!enabled || actualClickGuiOpen || player == null) {
            return;
        }
        final boolean requireAirborne = airborneOnly.get().booleanValue();
        final boolean pauseSneak = pauseWhileSneaking.get().booleanValue();
        if ((requireAirborne || pauseSneak)
                && (movement == null || !movement.available()
                || (requireAirborne && movement.onGround())
                || (pauseSneak && movement.sneaking()))) {
            // Opt-in restrictions require mapped state. No fabricated
            // airborne or sneak state, and no write on failed authority.
            return;
        }

        nativeFallDispatching = true;
        nativeFallPlayer = player;
        nativeFallRevoked = false;
        try {
            // Snapshot the exact enabled lifetime and policy before invoking
            // the original player's native getter. It may reenter this module
            // or change GUI settings, so an old callback must not zero a new
            // lifetime's real fall-distance source.
            final long approvedEpoch = nativeReadEpoch;
            final double configured = threshold.get().doubleValue();
            final float distance = player.customMcFallDistance();
            if (nativeFallRevoked) return;
            // Only a finite, positive ORIGINAL falling distance is evidence
            // for a falling-damage reduction. Negative sentinel/transition
            // values and nonfinite corrupted readbacks are not a valid fall;
            // never overwrite those native source states with synthetic zero.
            if (enabled
                    && nativeReadEpoch == approvedEpoch
                    && threshold.get().doubleValue() == configured
                    && airborneOnly.get().booleanValue() == requireAirborne
                    && pauseWhileSneaking.get().booleanValue() == pauseSneak
                    && Float.isFinite(distance)
                    && distance > 0.0F
                    && (double) distance > configured + 0.000001D) {
                player.customMcSetFallDistance(0.0F);
            }
        } finally {
            // A native distance setter may write and then throw. Never
            // synthesize a restoration of actual vanilla falling evidence.
            nativeFallPlayer = null;
            nativeFallRevoked = false;
            nativeFallDispatching = false;
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
