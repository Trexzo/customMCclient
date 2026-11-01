package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** V367: module precedence and fail-closed original callback reentry. */
final class Minecraft189CombatLaneAdmissionTest {
    private static Minecraft189CombatLaneAdmission admission(
            final boolean aura, final boolean trigger, final boolean clicker,
            final long a, final long t, final long c) {
        return Minecraft189CombatLaneAdmission.capture(
                aura, trigger, clicker, a, t, c);
    }

    @Test
    void preservesAllEightExistingModulePrecedenceCombinations() {
        for (int mask = 0; mask < 8; ++mask) {
            final boolean aura = (mask & 1) != 0;
            final boolean trigger = (mask & 2) != 0;
            final boolean clicker = (mask & 4) != 0;
            final Minecraft189CombatLaneAdmission.Owner owner =
                    aura ? Minecraft189CombatLaneAdmission.Owner.AURA
                            : trigger
                            ? Minecraft189CombatLaneAdmission.Owner.TRIGGER_BOT
                            : Minecraft189CombatLaneAdmission.Owner.AUTO_CLICKER;
            assertEquals(owner,
                    admission(aura, trigger, clicker, 11, 22, 33).owner(),
                    "persisted combinations must keep their original owner");
        }
    }

    @Test
    void sameLaneAndEpochSurvivesOrdinarySteadyCallbacks() {
        for (int mask = 0; mask < 8; ++mask) {
            final boolean a = (mask & 1) != 0;
            final boolean t = (mask & 2) != 0;
            final boolean c = (mask & 4) != 0;
            final Minecraft189CombatLaneAdmission first =
                    admission(a, t, c, 7, 13, 19);
            assertTrue(first.matches(admission(a, t, c, 7, 13, 19)));
            assertFalse(first.matches(null));
        }
    }

    @Test
    void rejectsSwitchToHigherPriorityModuleAndBackBeforeNativeDispatch() {
        final Minecraft189CombatLaneAdmission triggerOwner =
                admission(false, true, true, 10, 20, 30);
        assertFalse(triggerOwner.matches(
                admission(true, true, true, 11, 20, 30)));
        // Aura turned ON then OFF during a reentrant World callback.
        assertFalse(triggerOwner.matches(
                admission(false, true, true, 12, 20, 30)));
        final Minecraft189CombatLaneAdmission clickerOwner =
                admission(false, false, true, 10, 20, 30);
        assertFalse(clickerOwner.matches(
                admission(false, true, true, 10, 21, 30)));
        assertFalse(clickerOwner.matches(
                admission(false, false, true, 10, 22, 30)));
    }

    @Test
    void everyOwnerRejectsItsOwnDisableEnableAbaEpoch() {
        assertFalse(admission(true, true, true, 100, 200, 300).matches(
                admission(true, true, true, 102, 200, 300)));
        assertFalse(admission(false, true, true, 100, 200, 300).matches(
                admission(false, true, true, 100, 202, 300)));
        assertFalse(admission(false, false, true, 100, 200, 300).matches(
                admission(false, false, true, 100, 200, 302)));
    }

    @Test
    void lowerPriorityLifecycleChangeCannotBorrowAnAlreadyEarnedAttack() {
        final Minecraft189CombatLaneAdmission auraOwner =
                admission(true, false, false, 1, 4, 7);
        assertFalse(auraOwner.matches(admission(true, true, false, 1, 5, 7)));
        assertFalse(auraOwner.matches(admission(true, false, false, 1, 6, 7)));
        assertFalse(auraOwner.matches(admission(true, false, true, 1, 4, 8)));
        assertFalse(auraOwner.matches(admission(true, false, false, 1, 4, 9)));
    }

    @Test
    void originalGuiSuspensionKeepsAuraLifecycleUntilActualDisable() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final long initial = aura.lifecycleEpoch();
        aura.onEnable();
        assertEquals(initial + 1L, aura.lifecycleEpoch());
        aura.suspendForGui();
        assertEquals(initial + 1L, aura.lifecycleEpoch(),
                "a vanilla pause preserves verified UUID lock eligibility");
        aura.onDisable();
        assertEquals(initial + 2L, aura.lifecycleEpoch());
        aura.onEnable();
        assertEquals(initial + 3L, aura.lifecycleEpoch());
        assertFalse(admission(true, false, false, initial + 1L, 0, 0)
                .matches(admission(true, false, false,
                        aura.lifecycleEpoch(), 0, 0)),
                "real disable-enable must revoke an older Aura admission");
        aura.onDisable();
    }

    @Test
    void triggerAndClickerNativeEpochsRevokeInFlightCallbacks() {
        final Minecraft189TriggerBotModule trigger =
                new Minecraft189TriggerBotModule();
        final Minecraft189AutoClickerModule clicker =
                new Minecraft189AutoClickerModule();
        trigger.onEnable();
        clicker.onEnable();
        final long t = trigger.nativeWorldOwnerEpoch();
        final long c = clicker.nativeOwnerEpoch();
        final Minecraft189CombatLaneAdmission chosen =
                admission(false, true, true, 10, t, c);
        trigger.suspend();
        assertFalse(chosen.matches(admission(false, true, true, 10,
                trigger.nativeWorldOwnerEpoch(), c)));
        clicker.onDisable();
        clicker.onEnable();
        assertFalse(chosen.matches(admission(false, true, true, 10,
                t, clicker.nativeOwnerEpoch())));
        trigger.onDisable();
        clicker.onDisable();
    }
}
