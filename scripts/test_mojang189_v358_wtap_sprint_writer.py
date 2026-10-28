"""V358 original EntityPlayerSP WTap/AutoSprint single-writer contract.

Source-only checks cannot certify genuine Mojang native sprint or damage.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
HOST = (BASE / "Minecraft189HostRuntime.java").read_text(encoding="utf-8")
LEASE = (BASE / "Minecraft189SprintHostSourceLease.java").read_text(encoding="utf-8")
ARB = (BASE / "Minecraft189SprintWriteArbiter.java").read_text(encoding="utf-8")
WTAP = (BASE / "Minecraft189WTapModule.java").read_text(encoding="utf-8")
AUTO = (BASE / "Minecraft189AutoSprintModule.java").read_text(encoding="utf-8")
KEEP = (BASE / "Minecraft189KeepSprintModule.java").read_text(encoding="utf-8")
BRIDGE = (BASE / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")


class OriginalNativeSprintWriteV358(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.sprint = HOST.split("    void playerSprintControl(\n", 1)[1].split(
            "    void playerSneakControl(\n", 1)[0]

    def test_first_real_writer_claim_blocks_opposite_owner(self):
        self.assertIn("enum Owner { W_TAP, AUTO_SPRINT }", ARB)
        self.assertIn("writer != owner", ARB)
        self.assertIn("writer = owner;", ARB)
        self.assertIn("if (claim(owner, token))", ARB)
        self.assertIn("original.customMcSetSprinting(value);", ARB)
        self.assertIn("token != generation", ARB)
        self.assertIn("++generation;", ARB)
        self.assertNotIn("customMcAttackCrosshairEntity", ARB)
        self.assertNotIn("sendPacket", ARB)

    def test_native_host_preserves_wtap_precedence_and_autosprint_yield(self):
        host = self.sprint
        self.assertIn("sprintHostLease.enter(player, originalGuiFocus)", host)
        self.assertIn("sprintWriteArbiter.enter();", host)
        self.assertIn("sprintWriteArbiter.exit();", host)
        self.assertLess(host.index("sprintWriteArbiter.exit();"),
                        host.index("sprintHostLease.exit();"))
        self.assertIn("sprintWriteArbiter.guardedPlayer(guardedPlayer,", host)
        self.assertIn("Minecraft189SprintWriteArbiter.Owner.W_TAP", host)
        self.assertIn("Minecraft189SprintWriteArbiter.Owner.AUTO_SPRINT", host)
        self.assertIn("featureCatalog.wTap().apply(", host)
        self.assertIn("wTapControl, movement, attackHeld,", host)
        self.assertIn("wTapReportedWrite || sprintWriteArbiter.ownedBy(", host)
        self.assertIn("sprintHostSourceStillOwned()", host)
        self.assertIn("autoSprintControl,", host)
        self.assertLess(host.index("featureCatalog.wTap().apply("),
                        host.index("featureCatalog.autoSprint()"))
        self.assertIn("clickGuiRuntime.coreRuntime().model().snapshot().open()", host)

    def test_previous_native_wrapper_does_not_become_current_source(self):
        self.assertIn("private long generation;", LEASE)
        self.assertIn("final long token = generation;", LEASE)
        self.assertIn("token == generation && original == player", LEASE)
        self.assertIn("stillOwned(currentGuiFocused.getAsBoolean())", LEASE)
        self.assertIn("player.customMcSetSprinting(sprinting);", LEASE)
        self.assertIn("if (player != original || (!guiAtEntry && guiFocused))", LEASE)

    def test_module_lifecycle_and_manual_sprint_policy_unchanged(self):
        self.assertIn("nativeSprintWriteInProgress", WTAP)
        self.assertIn("nativeSprintWriteOwned(originalEpoch, originalPolicy)", WTAP)
        self.assertIn("previousLeftButtonHeld = leftButtonHeld;", WTAP)
        self.assertIn("player.customMcSetSprinting(false);", WTAP)
        self.assertIn("player.customMcSetSprinting(true);", AUTO)
        self.assertIn("currentNativeClickGuiOpen", AUTO)
        self.assertIn("wTapOwnsSprint", KEEP)
        self.assertIn("!wTapOwnsSprint", KEEP)
        self.assertIn("shouldKeepSprintAfterSyntheticClick", HOST)
        self.assertIn("restoreSprintAfterSyntheticClick", HOST)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();", BRIDGE)


if __name__ == "__main__":
    unittest.main()
