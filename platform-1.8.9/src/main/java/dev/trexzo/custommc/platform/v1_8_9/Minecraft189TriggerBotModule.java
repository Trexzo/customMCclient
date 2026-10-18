package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dedicated Combat Trigger Bot. Only the exact mapped vanilla player
 * crosshair hit may grant a click; it cannot target off-screen entities.
 */
public final class Minecraft189TriggerBotModule implements Module {
    public static final String ID = "combat.triggerBot";
    public static final String MIN_CPS = ID + ".minCps";
    public static final String MAX_CPS = ID + ".maxCps";
    public static final String CONFIRM = ID + ".confirmTicks";
    public static final String HOLD = ID + ".requireAttackHeld";
    public static final String PAUSE_RIGHT = ID + ".pauseWhileRightClicking";
    public static final String PAUSE_SNEAK = ID + ".pauseWhileSneaking";

    private final Setting<Integer> minCps = new Setting<Integer>(
            MIN_CPS, 8, v -> v != null && v >= 1 && v <= 20, SettingCodecs.INTEGER);
    private final Setting<Integer> maxCps = new Setting<Integer>(
            MAX_CPS, 12, v -> v != null && v >= 1 && v <= 20, SettingCodecs.INTEGER);
    private final Setting<Integer> confirmTicks = new Setting<Integer>(
            CONFIRM, 2, v -> v != null && v >= 1 && v <= 10, SettingCodecs.INTEGER);
    private final Setting<Boolean> requireHold = new Setting<Boolean>(
            HOLD, Boolean.FALSE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseRight = new Setting<Boolean>(
            PAUSE_RIGHT, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseSneak = new Setting<Boolean>(
            PAUSE_SNEAK, Boolean.TRUE, v -> v != null, SettingCodecs.BOOLEAN);

    private boolean enabled;
    private int lastMin = -1;
    private int lastMax = -1;
    private int lastConfirm = -1;
    private int confirmed;
    // Keep CPS and consecutive crosshair confirmation owned by ONE exact
    // source-native player UUID. Recycled loaded-list indices never confer
    // authority to a different player during a target switch.
    private UUID confirmedTargetUuid;
    private int phaseCredit;
    private int sampledCps;
    // Native World.loadedEntity UUID getters may synchronously reenter
    // feature lifecycle/ClickGUI profile. Old original World evidence
    // cannot grant confirmation to a new TriggerBot activation.
    private long nativeWorldEpoch;
    private long nextNativeUuidTicket;
    private long pendingNativeUuidTicket;
    private long pendingNativeUuidEpoch;
    private int pendingMinCps;
    private int pendingMaxCps;
    private int pendingConfirmTicks;
    private boolean pendingRequireHold;
    private boolean pendingPauseRight;
    private boolean pendingPauseSneak;

    @Override
    public String id() { return ID; }
    public Setting<Integer> minCpsSetting() { return minCps; }
    public Setting<Integer> maxCpsSetting() { return maxCps; }
    public Setting<Integer> confirmTicksSetting() { return confirmTicks; }
    public Setting<Boolean> requireHoldSetting() { return requireHold; }
    public Setting<Boolean> pauseRightSetting() { return pauseRight; }
    public Setting<Boolean> pauseSneakSetting() { return pauseSneak; }
    synchronized boolean active() { return enabled; }

    @Override
    public synchronized void onEnable() {
        ++nativeWorldEpoch;
        pendingNativeUuidTicket = 0;
        enabled = true;
        reset();
    }

    @Override
    public synchronized void onDisable() {
        ++nativeWorldEpoch;
        pendingNativeUuidTicket = 0;
        enabled = false;
        reset();
    }

    // Cancels all scheduler credit while another module or ClickGUI owns input.
    synchronized void suspend() {
        ++nativeWorldEpoch;
        pendingNativeUuidTicket = 0;
        reset();
    }

    /**
     * Capture exactly one active module / persisted click policy lifetime
     * before querying the genuine mapped World loaded-player UUID array.
     * Ticket by itself never authorizes an outgoing attack.
     */
    synchronized long beginNativeWorldUuidRead() {
        if (!enabled) return 0;
        final long token = ++nextNativeUuidTicket;
        pendingNativeUuidTicket = token;
        pendingNativeUuidEpoch = nativeWorldEpoch;
        pendingMinCps = minCps.get();
        pendingMaxCps = maxCps.get();
        pendingConfirmTicks = confirmTicks.get();
        pendingRequireHold = requireHold.get();
        pendingPauseRight = pauseRight.get();
        pendingPauseSneak = pauseSneak.get();
        return token;
    }

    /**
     * The current original World UUID read finished. Any OFF->ON,
     * suspended scheduler or GUI policy edit invalidates its old source
     * evidence before it can accumulate confirmation or CPS phase credit.
     * Completion is single-use, including for a rejected source read.
     */
    synchronized boolean completeNativeWorldUuidRead(final long token) {
        final boolean permitted = token != 0
                && token == pendingNativeUuidTicket && enabled
                && pendingNativeUuidEpoch == nativeWorldEpoch
                && pendingMinCps == minCps.get()
                && pendingMaxCps == maxCps.get()
                && pendingConfirmTicks == confirmTicks.get()
                && pendingRequireHold == requireHold.get()
                && pendingPauseRight == pauseRight.get()
                && pendingPauseSneak == pauseSneak.get();
        if (token != 0 && token == pendingNativeUuidTicket)
            pendingNativeUuidTicket = 0;
        return permitted;
    }

    // Legacy primitive-only tests/callers have no entity identity. Production
    // MUST use the overload below with the verified current-world UUID.
    private static final UUID LEGACY_TEST_TARGET = new UUID(0L, 0L);

    synchronized boolean shouldClick(
            final boolean verifiedCrosshairPlayer,
            final boolean attackHeld,
            final boolean rightHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return shouldClick(verifiedCrosshairPlayer, attackHeld, rightHeld,
                movement, LEGACY_TEST_TARGET);
    }

    synchronized boolean shouldClick(
            final boolean verifiedCrosshairPlayer,
            final boolean attackHeld,
            final boolean rightHeld,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final UUID verifiedNativePlayerUuid) {
        if (!enabled || !verifiedCrosshairPlayer
                || verifiedNativePlayerUuid == null
                || (requireHold.get() && !attackHeld)
                || (pauseRight.get() && rightHeld)
                || (pauseSneak.get() && (movement == null || !movement.available()
                        || movement.sneaking()))) {
            reset();
            return false;
        }
        final int min = minCps.get();
        final int max = maxCps.get();
        final int need = confirmTicks.get();
        if (min != lastMin || max != lastMax || need != lastConfirm) {
            reset();
            lastMin = min;
            lastMax = max;
            lastConfirm = need;
        }

        if (!verifiedNativePlayerUuid.equals(confirmedTargetUuid)) {
            // Neither a successful A crosshair confirmation nor its pending
            // sub-tick CPS credit can be replayed against newly acquired B.
            confirmed = 0;
            phaseCredit = 0;
            sampledCps = 0;
            confirmedTargetUuid = verifiedNativePlayerUuid;
        }
        confirmed = Math.min(need, confirmed + 1);
        if (confirmed < need) return false;
        if (sampledCps == 0) sampledCps = nextCps(min, max);
        phaseCredit += sampledCps;
        if (phaseCredit < 20) return false;
        phaseCredit -= 20;
        sampledCps = nextCps(min, max);
        return true;
    }

    private int nextCps(final int a, final int b) {
        final int low = Math.min(a, b);
        final int high = Math.max(a, b);
        return low == high ? low : ThreadLocalRandom.current().nextInt(low, high + 1);
    }

    private void reset() {
        lastMin = -1;
        lastMax = -1;
        lastConfirm = -1;
        confirmed = 0;
        confirmedTargetUuid = null;
        sampledCps = 0;
        phaseCredit = 0;
    }
}
