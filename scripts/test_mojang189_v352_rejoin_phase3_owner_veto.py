"""V352: strict real Minecraft phase-3 rejoin owner veto regression.

Checks source integration only. It NEVER certifies real B controller hit,
new original UUID, native hurt, or dedicated server B-only damage.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9" / "src" / "main" / "java" / (
    "dev/trexzo/custommc/platform/v1_8_9")
ACCEPT = (BASE / "Minecraft189RejoinTargetAcceptance.java").read_text(
    encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(
    encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalRejoinPhaseThreeV352(unittest.TestCase):
    def test_only_native_b_owner_survives_after_actual_a_rejoin(self):
        self.assertIn("sourceARejoinedWithBPresent", ACCEPT)
        self.assertIn("observedPhase == 3 && sourceARejoinedWithBPresent", ACCEPT)
        self.assertIn("observedPhase == 4", ACCEPT)
        self.assertIn("sourceReason", ACCEPT)
        self.assertIn("e != null && e.a >= 0 && e.b >= 0", ACCEPT)
        self.assertIn("survivorUuid.equals(e.ids.at(e.b))", ACCEPT)
        self.assertIn("survivorUuid.equals(aura.lockedTargetUuid())", ACCEPT)
        self.assertIn("sourceRayB=", ACCEPT)
        self.assertIn("phase=", ACCEPT)
        self.assertNotIn("CUSTOMMC_189_REJOIN_PHASE3_ACCEPTANCE_PASS", ACCEPT)

    def test_veto_precedes_any_automatic_melee_and_crashing_oracle(self):
        action = BRIDGE.split(
            "public static synchronized void autoClick(", 1)[1].split(
            "public static synchronized void publishTick(", 1)[0]
        assert action.index(
            "vetoTransientUnownedPhaseFourSyntheticClick(") < action.index(
                "host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)")
        assert action.index(
            "vetoTransientUnownedPhaseFourSyntheticClick(") < action.index(
                "Minecraft189RejoinTargetAcceptance.verifyOwnedClick(")
        self.assertIn("host.enterSyntheticCombatCycle()", action)
        self.assertIn("host.leaveSyntheticCombatCycle(combatCycleToken)", action)
        self.assertIn("host.mayDispatchSyntheticMelee(originalSlot)", action)
        self.assertIn("minecraft.customMcAttackCrosshairEntity()", action)

    def test_server_and_world_acceptance_remains_real(self):
        self.assertIn("wait_for_new_original_source_marker(", HARNESS)
        self.assertIn("REJOIN_UUID, deadline,", HARNESS)
        self.assertIn("wait_for(host, host_log, REJOIN_B_RETAINED, deadline,", HARNESS)
        self.assertIn("wait_for_real_score(server,server_log,\"CITargetB\"", HARNESS)
        self.assertIn("wait_for_real_score(server,server_log,\"CITarget\"", HARNESS)
        self.assertIn("verifyRealControllerB(", ACCEPT)
        self.assertIn("requireRejoinedControllerEntityUuid", ACCEPT)
        self.assertIn("originalUuid.equals(argumentUuid)", ACCEPT)
        self.assertIn("survivorUuid.equals(argumentUuid)", ACCEPT)


if __name__ == "__main__":
    unittest.main()
