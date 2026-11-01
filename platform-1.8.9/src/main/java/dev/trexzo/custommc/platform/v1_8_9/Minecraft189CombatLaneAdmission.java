package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V367: immutable, source-local admission for one automatic combat lane.
 *
 * The host preserves the old precedence (Aura > TriggerBot > AutoClicker).
 * A native World getter, policy callback or other module may re-enter while
 * an attack is scheduled. Its original lane and ALL three module lifecycle
 * epochs must still agree before any scheduler credit becomes dispatchable.
 * No target index, UUID, attack packet or module preference is invented here.
 *
 * The host captures this admission AFTER suspending lower-priority schedulers,
 * so ordinary suppression never invalidates the currently selected lane.
 */
final class Minecraft189CombatLaneAdmission {
    enum Owner { AURA, TRIGGER_BOT, AUTO_CLICKER }

    private final Owner owner;
    private final boolean auraActive;
    private final boolean triggerActive;
    private final boolean autoClickerActive;
    private final long auraEpoch;
    private final long triggerEpoch;
    private final long autoClickerEpoch;

    private Minecraft189CombatLaneAdmission(
            final boolean auraActive, final boolean triggerActive,
            final boolean autoClickerActive, final long auraEpoch,
            final long triggerEpoch, final long autoClickerEpoch) {
        this.owner = owner(auraActive, triggerActive);
        this.auraActive = auraActive;
        this.triggerActive = triggerActive;
        this.autoClickerActive = autoClickerActive;
        this.auraEpoch = auraEpoch;
        this.triggerEpoch = triggerEpoch;
        this.autoClickerEpoch = autoClickerEpoch;
    }

    static Owner owner(final boolean auraActive, final boolean triggerActive) {
        if (auraActive) return Owner.AURA;
        if (triggerActive) return Owner.TRIGGER_BOT;
        return Owner.AUTO_CLICKER;
    }

    static Minecraft189CombatLaneAdmission capture(
            final boolean auraActive, final boolean triggerActive,
            final boolean autoClickerActive, final long auraEpoch,
            final long triggerEpoch, final long autoClickerEpoch) {
        return new Minecraft189CombatLaneAdmission(
                auraActive, triggerActive, autoClickerActive,
                auraEpoch, triggerEpoch, autoClickerEpoch);
    }

    Owner owner() { return owner; }

    boolean matches(final Minecraft189CombatLaneAdmission current) {
        return current != null && owner == current.owner
                && auraActive == current.auraActive
                && triggerActive == current.triggerActive
                && autoClickerActive == current.autoClickerActive
                && auraEpoch == current.auraEpoch
                && triggerEpoch == current.triggerEpoch
                && autoClickerEpoch == current.autoClickerEpoch;
    }
}
