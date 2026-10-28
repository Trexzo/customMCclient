package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * V355: diagnostics cannot change original combat owner semantics.
 * Actual original client / native controller / server hurt remain separate.
 */
final class Minecraft189KillAuraOwnerTransitionProvenanceTest {
    @Test
    void sourceReasonsAreDiagnosticOnlyAndCannotInventAnOwner() {
        final UUID originalA = UUID.fromString(
                "e3542dd1-56f3-3ab4-82cb-2c015df72d8d");
        final UUID survivorB = UUID.fromString(
                "9c6b2c0b-a653-3300-9829-2617908fae14");
        final Minecraft189KillAuraModule aura =
                new Minecraft189KillAuraModule();
        aura.lockTargetSetting().set(Boolean.TRUE);
        aura.onEnable();
        try {
            aura.rememberSelectedTarget(survivorB, "selector-verified-existing-uuid");
            assertEquals(survivorB, aura.lockedTargetUuid());
            aura.rememberSelectedTarget(originalA,
                    "selector-live-candidate-or-absent");
            assertEquals(originalA, aura.lockedTargetUuid());
            aura.rememberSelectedTarget(null,
                    "original-world-combat-inconsistent");
            assertNull(aura.lockedTargetUuid());
            aura.rememberSelectedTarget(survivorB,
                    "selector-live-candidate-or-absent");
            assertEquals(survivorB, aura.lockedTargetUuid());
            aura.suspend();
            assertNull(aura.lockedTargetUuid());
        } finally {
            aura.onDisable();
        }
        aura.rememberSelectedTarget(survivorB,
                "selector-live-candidate-or-absent");
        assertNull(aura.lockedTargetUuid());
    }

    @Test
    void lockDisabledNeverStoresAnOwnerRegardlessOfDiagnosticSource() {
        final Minecraft189KillAuraModule aura =
                new Minecraft189KillAuraModule();
        aura.onEnable();
        try {
            aura.rememberSelectedTarget(UUID.randomUUID(),
                    "selector-live-candidate-or-absent");
            assertNull(aura.lockedTargetUuid());
        } finally {
            aura.onDisable();
        }
    }
}
