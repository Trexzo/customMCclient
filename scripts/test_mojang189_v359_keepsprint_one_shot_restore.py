"""V359 KeepSprint original-player post-melee restore safety contracts.

These inspect source only. They cannot replace genuine Minecraft 1.8.9
controller provenance, hit confirmation or vanilla-server damage tests.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
KEEP = (BASE / "Minecraft189KeepSprintModule.java").read_text(encoding="utf-8")
LEASE = (BASE / "Minecraft189KeepSprintRestoreLease.java").read_text(encoding="utf-8")
WTAP = (BASE / "Minecraft189WTapModule.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HARNESS = (ROOT / "scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalKeepSprintV359(unittest.TestCase):
    def test_original_owner_and_no_nested_restore_grant(self):
        self.assertIn("original == player", LEASE)
        self.assertIn("if (restoring)", LEASE)
        self.assertIn("revoked = true;", LEASE)
        self.assertIn("return restoring && !revoked && original == player;", LEASE)
        self.assertIn("keepSprintRestoreLease.revoke();", HOST)
        self.assertIn("keepSprintRestoreLease.exit();", HOST)
        self.assertNotIn("customMcSetSprinting", LEASE)

    def test_one_synthetic_click_proof_is_consumed_before_original_reads(self):
        x = HOST.index("    void restoreSprintAfterSyntheticClick() {")
        y = HOST.index("    private boolean keepSprintRestoreStillOwned(", x)
        scope = HOST[x:y]
        self.assertLess(scope.index("pendingKeepSprintSource = null;"),
                        scope.index("state.customMcOnGround()"))
        self.assertIn("pendingKeepSprintAdmissionSerial", scope)
        self.assertIn("keepSprintRestoreLease.enter(owner)", scope)
        self.assertIn("final boolean ground = state.customMcOnGround();", scope)
        self.assertIn("final boolean sneaking = state.customMcSneaking();", scope)
        self.assertIn("final boolean sprintingNow = state.customMcSprinting();",
                      scope)
        self.assertGreaterEqual(scope.count("keepSprintRestoreStillOwned("), 5)
        self.assertIn("if (sprintingNow) return;", scope)
        self.assertIn("owner.customMcSetSprinting(true);", scope)
        self.assertIn("finally {\n            keepSprintRestoreLease.exit();", scope)
        self.assertNotIn("setSprinting(false)", scope)

    def test_current_gui_wtap_policy_and_module_lifecycle_veto(self):
        q = HOST.split("    private boolean keepSprintRestoreStillOwned(", 1)[1]
        q = q.split("\n    /**", 1)[0]
        for term in ("original == tickSprintControl",
                     "keepSprintAdmissionSerial == admission",
                     "module.lifecycleEpoch() == lifecycle",
                     "module.active()", "!featureCatalog.wTap().active()",
                     "!clickGuiRuntime.coreRuntime()",
                     "forwardPolicy == module.requireForwardSetting().get()",
                     "hitPolicy == module.requirePlayerHitSetting().get()",
                     "sneakPolicy == module.pauseWhileSneakingSetting().get()"):
            self.assertIn(term, q)
        self.assertIn("!wTapOwnsSprint", KEEP)
        self.assertIn("nativeSprintWriteInProgress", WTAP)

    def test_no_new_click_or_controller_or_server_authority(self):
        self.assertIn("host.shouldKeepSprintAfterSyntheticClick(playerHit)", BRIDGE)
        self.assertIn("host.restoreSprintAfterSyntheticClick();", BRIDGE)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", BRIDGE)
        self.assertIn("wait_for_real_score(", HARNESS)
        self.assertIn("REJOIN_B_RETAINED, deadline,", HARNESS)
        self.assertIn("verifyRealControllerB(", (BASE /
                     "Minecraft189RejoinTargetAcceptance.java").read_text(
                         encoding="utf-8"))
        self.assertNotIn("sendPacket(", LEASE)
        self.assertNotIn("syntheticAttack(", LEASE)


if __name__ == "__main__":
    unittest.main()
