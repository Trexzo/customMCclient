package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V367 R5: HitSelect admission retains its activation, source-lifetime
 * and real hurt-window policy until original controller dispatch.
 * The test never simulates a real controller hit or server-side damage.
 */
final class Minecraft189HitSelectPolicyAdmissionTest {
    private static Minecraft189HitSelectPolicyAdmission policy(
            final boolean enabled, final long epoch,
            final boolean recovery, final int min, final int max) {
        return Minecraft189HitSelectPolicyAdmission.capture(
                enabled, epoch, recovery, min, max);
    }

    @Test
    void steadyStateSupportsDisabledDefaultAndBothRecoveryModes() {
        for (boolean enabled : new boolean[]{false, true}) {
            for (boolean recovery : new boolean[]{false, true}) {
                final Minecraft189HitSelectPolicyAdmission expected =
                        policy(enabled, 91L, recovery, 0, 1);
                assertTrue(expected.matches(policy(enabled, 91L, recovery, 0, 1)));
                assertFalse(expected.matches(null));
            }
        }
    }

    @Test
    void rejectsDisabledEnabledDisabledAbaWithSameVisibleFlags() {
        final Minecraft189HitSelectPolicyAdmission initial =
                policy(false, 13L, false, 0, 1);
        assertFalse(initial.matches(policy(true, 14L, false, 0, 1)));
        assertFalse(initial.matches(policy(false, 15L, false, 0, 1)));
        final Minecraft189HitSelectPolicyAdmission armed =
                policy(true, 22L, true, 0, 1);
        assertFalse(armed.matches(policy(false, 23L, true, 0, 1)));
        assertFalse(armed.matches(policy(true, 24L, true, 0, 1)));
    }

    @Test
    void cannotReuseDifferentRecoveryAndHurtWindowEvenIfEpochMatches() {
        final Minecraft189HitSelectPolicyAdmission first =
                policy(true, 81L, true, 0, 1);
        assertFalse(first.matches(policy(true, 81L, false, 0, 1)));
        assertFalse(first.matches(policy(true, 81L, true, 1, 1)));
        assertFalse(first.matches(policy(true, 81L, true, 0, 2)));
        assertFalse(first.matches(policy(true, 81L, true, 2, 1)));
        assertFalse(first.matches(policy(false, 81L, true, 0, 1)));
    }

    @Test
    void originalSchedulerIntakeRejectsChangedPolicyBeforeFinalNativeController() {
        final Minecraft189HitSelectModule select =
                new Minecraft189HitSelectModule();
        select.onEnable();
        try {
            // Source intake happens before any later original World getter.
            final Minecraft189HitSelectPolicyAdmission intake = policy(
                    select.active(), select.nativeOwnerEpoch(),
                    select.requireObservedRecoverySetting().get(),
                    select.minTargetHurtTicksSetting().get(),
                    select.maxTargetHurtTicksSetting().get());

            // A callback changes HitSelect's live recovery setting; taking
            // a *new* snapshot at the end would incorrectly adopt it.
            select.requireObservedRecoverySetting().set(Boolean.TRUE);
            final Minecraft189HitSelectPolicyAdmission late = policy(
                    select.active(), select.nativeOwnerEpoch(),
                    select.requireObservedRecoverySetting().get(),
                    select.minTargetHurtTicksSetting().get(),
                    select.maxTargetHurtTicksSetting().get());
            assertTrue(late.matches(late), "late snapshot alone misses reentry");
            assertFalse(intake.matches(late),
                    "only source-intake policy may authorize this click");

            // A second original callback changes the hurt window.
            select.requireObservedRecoverySetting().set(Boolean.FALSE);
            select.minTargetHurtTicksSetting().set(1);
            assertFalse(intake.matches(policy(
                    select.active(), select.nativeOwnerEpoch(),
                    select.requireObservedRecoverySetting().get(),
                    select.minTargetHurtTicksSetting().get(),
                    select.maxTargetHurtTicksSetting().get())));
            select.minTargetHurtTicksSetting().set(0);

            // OFF->ON with the same visible settings is still a different
            // activation epoch and cannot borrow any earlier attack.
            select.onDisable();
            select.onEnable();
            assertFalse(intake.matches(policy(
                    select.active(), select.nativeOwnerEpoch(),
                    select.requireObservedRecoverySetting().get(),
                    select.minTargetHurtTicksSetting().get(),
                    select.maxTargetHurtTicksSetting().get())));
        } finally {
            select.onDisable();
        }
    }

    @Test
    void originalGuiOrWorldSuspensionRevokesRecoveryAdmission() {
        final Minecraft189HitSelectModule module =
                new Minecraft189HitSelectModule();
        module.requireObservedRecoverySetting().set(Boolean.TRUE);
        module.onEnable();
        try {
            final long original = module.nativeOwnerEpoch();
            final Minecraft189HitSelectPolicyAdmission admitted = policy(
                    module.active(), original,
                    module.requireObservedRecoverySetting().get(),
                    module.minTargetHurtTicksSetting().get(),
                    module.maxTargetHurtTicksSetting().get());
            module.suspendForGui();
            assertFalse(admitted.matches(policy(
                    module.active(), module.nativeOwnerEpoch(),
                    module.requireObservedRecoverySetting().get(),
                    module.minTargetHurtTicksSetting().get(),
                    module.maxTargetHurtTicksSetting().get())));
            final long beforeSource = module.nativeOwnerEpoch();
            module.suspendForSource();
            assertEquals(beforeSource + 1L, module.nativeOwnerEpoch());
        } finally {
            module.onDisable();
        }
    }

    @Test
    void moduleDisableEnableEpochRejectsOldExactWorldPolicy() {
        final Minecraft189HitSelectModule module =
                new Minecraft189HitSelectModule();
        module.onEnable();
        final Minecraft189HitSelectPolicyAdmission prior =
                policy(true, module.nativeOwnerEpoch(), false, 0, 1);
        module.onDisable();
        module.onEnable();
        try {
            assertFalse(prior.matches(policy(
                    module.active(), module.nativeOwnerEpoch(),
                    module.requireObservedRecoverySetting().get(),
                    module.minTargetHurtTicksSetting().get(),
                    module.maxTargetHurtTicksSetting().get())));
        } finally {
            module.onDisable();
        }
    }
}
