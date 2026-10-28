package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verified target identity is part of trigger-mode click scheduling. */
final class Minecraft189AutoClickerNativeTargetUuidTest {
    private static final UUID PLAYER_A =
            UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID PLAYER_B =
            UUID.fromString("20000000-0000-4000-8000-000000000002");

    private static boolean nativeTrigger(
            final Minecraft189AutoClickerModule clicks, final UUID target) {
        return clicks.shouldClick(false, false, false, null, null,
                true, target);
    }

    @Test
    void newUuidCannotInheritConfirmedFramesOrTwentyCpsCredit() {
        final Minecraft189AutoClickerModule clicks =
                new Minecraft189AutoClickerModule();
        clicks.triggerModeSetting().set(Boolean.TRUE);
        clicks.triggerConfirmTicksSetting().set(3);
        clicks.minCpsSetting().set(20);
        clicks.maxCpsSetting().set(20);
        clicks.onEnable();
        try {
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
            // Same entity index in the original World may now mean B.
            assertFalse(nativeTrigger(clicks, PLAYER_B));
            assertFalse(nativeTrigger(clicks, PLAYER_B));
            assertTrue(nativeTrigger(clicks, PLAYER_B));
            // An A->B->A index reuse is not permission to reuse A's phase.
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
            // Missing genuine native UUID clears the whole confirmation.
            assertFalse(nativeTrigger(clicks, null));
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
            assertFalse(clicks.shouldClick(false, false, false,
                    null, null, false, PLAYER_A));
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
        } finally {
            clicks.onDisable();
        }
    }

    @Test
    void targetSwitchDropsFractionalCpsAndGuiOrLifecycleRevokesOwner() {
        final Minecraft189AutoClickerModule clicks =
                new Minecraft189AutoClickerModule();
        clicks.triggerModeSetting().set(Boolean.TRUE);
        clicks.triggerConfirmTicksSetting().set(1);
        clicks.minCpsSetting().set(10);
        clicks.maxCpsSetting().set(10);
        clicks.onEnable();
        try {
            final long firstEpoch = clicks.nativeOwnerEpoch();
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertFalse(nativeTrigger(clicks, PLAYER_B));
            assertTrue(nativeTrigger(clicks, PLAYER_B));
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            clicks.suspendForGui();
            assertTrue(clicks.active());
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
            clicks.onDisable();
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            clicks.onEnable();
            assertTrue(clicks.nativeOwnerEpoch() > firstEpoch);
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
            // Disabling trigger mode restores ordinary physical-hold gating.
            clicks.triggerModeSetting().set(Boolean.FALSE);
            assertFalse(clicks.shouldClick(false, false, false,
                    null, null, true, PLAYER_A));
            // Reenabling trigger mode must not reuse earlier owner credit.
            clicks.triggerModeSetting().set(Boolean.TRUE);
            assertFalse(nativeTrigger(clicks, PLAYER_A));
            assertTrue(nativeTrigger(clicks, PLAYER_A));
            assertEquals(10, clicks.minCpsSetting().get().intValue());
        } finally {
            clicks.onDisable();
        }
    }

    @Test
    void nonTriggerPhysicalHoldKeepsItsOriginalCadence() {
        final Minecraft189AutoClickerModule clicks =
                new Minecraft189AutoClickerModule();
        clicks.minCpsSetting().set(10);
        clicks.maxCpsSetting().set(10);
        clicks.onEnable();
        try {
            assertFalse(clicks.shouldClick(true, false, false,
                    null, null, false, null));
            assertTrue(clicks.shouldClick(true, false, false,
                    null, null, false, null));
            assertFalse(clicks.shouldClick(false, false, false,
                    null, null, false, null));
            assertFalse(clicks.shouldClick(true, false, false,
                    null, null, false, null));
            assertTrue(clicks.shouldClick(true, false, false,
                    null, null, false, null));
        } finally {
            clicks.onDisable();
        }
    }
}
