"""V355 original Minecraft KillAura UUID-lock transition provenance.

An opt-in diagnostic cannot replace real Mojang 1.8.9 entity/UUID
selection, original PlayerControllerMP, or server-authoritative damage.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
AURA = (BASE / "Minecraft189KillAuraModule.java").read_text(encoding="utf-8")
OWNER = (BASE / "Minecraft189RejoinTargetAcceptance.java").read_text(encoding="utf-8")
SELECTOR = (BASE / "Minecraft189KillAuraTargetSelector.java").read_text(
    encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
SCRIPT = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class NativeOwnerTraceV355(unittest.TestCase):
    def test_opt_in_phase_bounded_diagnostic_only(self):
        self.assertIn("custommc.acceptance.reportKillAuraRejoin", OWNER)
        self.assertIn("phase < 2 || phase > 4", OWNER)
        self.assertIn("java.util.Objects.equals(previous, next)", OWNER)
        self.assertIn("survivorUuid.equals(previous)", OWNER)
        self.assertIn("survivorUuid.equals(next)", OWNER)
        self.assertIn("originalUuid.equals(next)", OWNER)
        self.assertIn("v355LockTransitionReports > 12", OWNER)
        self.assertIn("CUSTOMMC_189_REJOIN_NATIVE_LOCK_TRANSITION_DIAG", OWNER)
        self.assertNotIn("CUSTOMMC_189_REJOIN_NATIVE_LOCK_TRANSITION_PASS", OWNER)

    def test_production_owner_assignment_is_identical_without_trace(self):
        self.assertIn("lockedTargetUuid = enabled && lockTarget.get() ? uuid : null;",
                      AURA)
        self.assertIn("traceNativeOwnerTransition(", AURA)
        self.assertIn('clear("host-suspend-gui-or-world")', AURA)
        self.assertIn('clear("invalid-original-player-rotation")', AURA)
        self.assertIn("resetSchedule();", AURA)
        self.assertIn("lastEntityIndex = -1;", AURA)
        self.assertIn("verifiedLockRetainable(", SELECTOR)
        self.assertIn("combat.alive(ownedIndex)", SELECTOR)
        self.assertIn("angularScore(local, rotation, candidate, fov)", SELECTOR)

    def test_distinguish_native_world_and_selection_revoke_origins(self):
        for marker in (
            '"original-world-identity-changed"',
            '"original-world-source-revoked"',
            '"original-world-combat-inconsistent"',
        ):
            self.assertIn(marker, HOST)
        for marker in (
            '"selector-invalid-original-state"',
            '"selector-invalid-original-uuid"',
            '"selector-missing-visibility"',
            '"selector-verified-existing-uuid"',
            '"selector-live-candidate-or-absent"',
        ):
            self.assertIn(marker, SELECTOR)

    def test_original_game_and_server_positive_oracles_still_required(self):
        self.assertIn("wait_for_new_original_source_marker(", SCRIPT)
        self.assertIn("REJOIN_UUID, deadline,", SCRIPT)
        self.assertIn("REJOIN_B_RETAINED, deadline,", SCRIPT)
        self.assertIn("wait_for_real_score(", SCRIPT)
        self.assertIn("requireRejoinedControllerEntityUuid", OWNER)
        self.assertIn("verifyRealControllerB(", OWNER)
        self.assertIn("vetoTransientUnownedPhaseFourSyntheticClick(", OWNER)


if __name__ == "__main__":
    unittest.main()
