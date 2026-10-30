"""V366 original-world Aura UUID lease survives GUI pause, never source loss.

These are source-only guard assertions, not evidence of real native B
controller dispatch or authoritative server damage in the V47 acceptance.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
MODULE = (BASE / "Minecraft189KillAuraModule.java").read_text(encoding="utf-8")
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
SELECTOR = (BASE / "Minecraft189KillAuraTargetSelector.java").read_text(
    encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalAuraGuiUuidLeaseV366(unittest.TestCase):
    def test_gui_does_not_clear_only_source_verified_uuid(self):
        gui = MODULE.split("synchronized void suspendForGui() {", 1)[1].split(
            "\n    }", 1)[0]
        self.assertIn("resetSchedule();", gui)
        self.assertIn("lastEntityIndex = -1;", gui)
        self.assertNotIn("rememberSelectedTarget(null", gui)
        self.assertNotIn("lockedTargetUuid = null", gui)
        self.assertNotIn("customMc", gui)
        self.assertIn(
            'synchronized void suspend() { clear("host-suspend-original-owner-lost"); }',
            MODULE)
        self.assertIn('clear("module-disable")', MODULE)
        self.assertIn('clear("invalid-original-player-rotation")', MODULE)

    def test_all_gui_fences_suspend_scheduler_without_releasing_world_uuid(self):
        self.assertEqual(3, HOST.count(
            "featureCatalog.killAura().suspendForGui();"))
        self.assertIn("featureCatalog.killAura().suspend();", HOST)
        self.assertIn("featureCatalog.killAura().rememberSelectedTarget(\n"
                      '                null, "original-world-source-revoked");', HOST)
        self.assertIn('"original-world-identity-changed"', HOST)
        self.assertIn('"original-world-combat-inconsistent"', HOST)
        self.assertIn("featureCatalog.hitSelect().suspendForGui();", HOST)
        self.assertIn("featureCatalog.autoClicker().suspendForGui();", HOST)
        self.assertIn("vetoOriginalScreenBeforeNativeCombat(", BRIDGE)

    def test_real_world_selector_has_no_ghost_uuid_owner(self):
        for term in (
            "final UUID lockedUuid = aura.lockedTargetUuid();",
            "verifiedLockRetainable(",
            "lockedUuid.equals(identities.at(candidate.entityIndex()))",
            "identities.entityCount() != positions.entityCount()",
            "if (ownedIndex >= 0) return false;",
            "kinds.localPlayer(ownedIndex)",
            "!kinds.living(ownedIndex) || !combat.alive(ownedIndex)",
            "distanceSquared > range*range",
            "antiBot.permits(ownedIndex, combat)",
            "teamGuard.permits(ownedIndex, combat)",
            "friendGuard.permits(ownedIndex, identities)",
            "wallCheck.permits(ownedIndex, visibility)",
            "angularScore(local, rotation, candidate, fov)",
            '"selector-live-candidate-or-absent"',
        ):
            self.assertIn(term, SELECTOR)
        self.assertIn("if (aura != null) aura.rememberSelectedTarget(null,",
                      SELECTOR)
        self.assertIn("target.clear();", SELECTOR)

    def test_original_v47_rejoin_server_damage_oracle_unchanged(self):
        for term in (
            "CUSTOMMC_189_REJOIN_REAL_A_DISCONNECTED_PASS",
            "CUSTOMMC_189_REJOIN_B_ONLY_STABLE_NO_DAMAGE_PASS",
            "CUSTOMMC_189_REJOIN_B_DAMAGE_A_STILL_ZERO_PASS",
            "REJOIN_B_RETAINED, deadline,",
            "wait_for_real_score(",
        ):
            self.assertIn(term, HARNESS)
        self.assertIn("CUSTOMMC_189_M419_TRANSIENT_WRONG_UUID_CLICK_VETO",
                      (BASE / "Minecraft189RejoinTargetAcceptance.java").read_text(
                          encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
