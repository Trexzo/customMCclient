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
    // Real controller hit/progress native getters may restart this module.
    private long nativeMiningEpoch;
    // Mapped PlayerControllerMP hit/progress methods may reenter the host.
    // Pin a single original controller across all native mining accesses.
    private boolean nativeMiningDispatching;
    private Minecraft189BlockMiningControl nativeMiningOwner;
    private boolean nativeMiningRevoked;

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
        nativeMiningEpoch++;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeMiningEpoch++;
    }

    synchronized void apply(
            final Minecraft189BlockMiningControl controller) {
        apply(controller, false, null);
    }

    synchronized void apply(
            final Minecraft189BlockMiningControl controller,
            final boolean attackHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        apply(controller, attackHeld, movement, false);
    }

    synchronized void apply(
            final Minecraft189BlockMiningControl controller,
            final boolean attackHeld,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean actualClickGuiOpen) {
        if (nativeMiningDispatching) {
            // A different original controller/world or actual UI/opt-in
            // gate loss revokes the still-open native hit/progress owner.
            if (!enabled || controller != nativeMiningOwner
                    || actualClickGuiOpen
                    || (requireAttackHeld.get().booleanValue() && !attackHeld)
                    || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null || !movement.available()
                            || movement.sneaking()))
                    || (groundOnly.get().booleanValue()
                        && (movement == null || !movement.available()
                            || !movement.onGround()))) {
                nativeMiningRevoked = true;
            }
            return;
        }
        // A stale original hit-block flag may remain set across menu focus.
        // UI ownership must never continue artificial break progress.
        if (actualClickGuiOpen) return;
        if (!enabled || controller == null) return;
        nativeMiningDispatching = true;
        nativeMiningOwner = controller;
        nativeMiningRevoked = false;
        try {
            final long epoch = nativeMiningEpoch;
            final int configuredMinimum = progressPercent.get().intValue();
            final boolean progressiveNow = progressive.get().booleanValue();
            final int configuredStep = stepPercent.get().intValue();
            final boolean requiresAttack = requireAttackHeld.get().booleanValue();
            final boolean pausesSneaking = pauseWhileSneaking.get().booleanValue();
            final boolean needsGround = groundOnly.get().booleanValue();
            final boolean airborne = airborneOverride.get().booleanValue();
            final int airMinimum = airborneProgressPercent.get().intValue();
            if ((requiresAttack && !attackHeld)
                    || (pausesSneaking && (movement == null
                        || !movement.available() || movement.sneaking()))
                    || (needsGround && (movement == null
                        || !movement.available() || !movement.onGround()))) return;

            // Both getters are MAPPED ORIGINAL PlayerControllerMP accesses.
            // Native callbacks can synchronously change module epoch, GUI
            // profile or mining state. Do not even read progress after an
            // unowned "isHittingBlock" result.
            final boolean hitting = controller.customMcIsHittingBlock();
            if (!nativeReadOwned(epoch, configuredMinimum, progressiveNow,
                    configuredStep, requiresAttack, pausesSneaking, needsGround,
                    airborne, airMinimum) || !hitting) return;
            final float current = controller.customMcBlockDamageProgress();
            if (!nativeReadOwned(epoch, configuredMinimum, progressiveNow,
                    configuredStep, requiresAttack, pausesSneaking, needsGround,
                    airborne, airMinimum)) return;

            // Select airborne progress only from actual mapped movement state,
            // not a guessed block target or missing native World snapshot.
            final boolean sourceAirborne = airborne
                    && movement != null && movement.available()
                    && !movement.onGround();
            final float minimum = (sourceAirborne
                    ? airMinimum : configuredMinimum) / 100.0F;
            if (!Float.isFinite(current) || current < 0.0F
                    || current >= minimum) return;

            final float next = progressiveNow
                    ? Math.min(minimum, current + configuredStep / 100.0F)
                    : minimum;
            if (next <= current) return;

            // Original mining can end DURING the progress getter. A second
            // actual source hit-state read immediately before setter prevents
            // committing old block progress when no native mining remains.
            if (!controller.customMcIsHittingBlock()
                    || !nativeReadOwned(epoch, configuredMinimum, progressiveNow,
                        configuredStep, requiresAttack, pausesSneaking, needsGround,
                        airborne, airMinimum)) return;
            controller.customMcSetBlockDamageProgress(next);
        } finally {
            // Mapped setter may write real original progress then throw.
            // Release guard, never fake a native block-progress rollback.
            nativeMiningOwner = null;
            nativeMiningRevoked = false;
            nativeMiningDispatching = false;
        }
    }

    private boolean nativeReadOwned(final long epoch,
            final int configuredMinimum, final boolean progressiveNow,
            final int configuredStep, final boolean requiresAttack,
            final boolean pausesSneaking, final boolean needsGround,
            final boolean airborne, final int airMinimum) {
        return enabled && !nativeMiningRevoked && nativeMiningEpoch == epoch
                && progressPercent.get().intValue() == configuredMinimum
                && progressive.get().booleanValue() == progressiveNow
                && stepPercent.get().intValue() == configuredStep
                && requireAttackHeld.get().booleanValue() == requiresAttack
                && pauseWhileSneaking.get().booleanValue() == pausesSneaking
                && groundOnly.get().booleanValue() == needsGround
                && airborneOverride.get().booleanValue() == airborne
                && airborneProgressPercent.get().intValue() == airMinimum;
    }

    synchronized boolean active() {
        return enabled;
    }
}
