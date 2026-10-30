"""V367 opt-in read-only diagnosis of genuine native locked A no-hit phase.

Must never change real original UUID selection, controller, packet or the
unmodified sticky A and B server-damage positive/negative requirements.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9"
ACCEPT=(BASE/"Minecraft189LockedTargetAcceptance.java").read_text(
    encoding="utf-8")
HOST=(BASE/"Minecraft189HostRuntime.java").read_text(encoding="utf-8")
BRIDGE=(BASE/"Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HARNESS=(ROOT/"scripts/RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


class OriginalLockedAReadOnlyV367(unittest.TestCase):
    def test_original_lock_success_and_server_damage_still_required(self):
        for value in (
            "CUSTOMMC_189_LOCK_A_RETAINED_WITH_CLOSER_B_PASS=15",
            "CUSTOMMC_189_LOCK_A_NATIVE_UUID_OWNED_ATTACK_PASS=YES",
            "CUSTOMMC_189_LOCK_B_NATIVE_UUID_OWNED_ATTACK_PASS=YES",
            "CUSTOMMC_189_LOCK_A_INELIGIBLE_RELEASES_TO_B_PASS=YES",
            "scoreboard players test CITargetB taken 0 0",
            "wait_for_real_score(server, server_log, \"CITarget\", deadline)",
            "wait_for_real_score(server, server_log, \"CITargetB\", deadline)",
        ):
            self.assertIn(value, ACCEPT+HARNESS)

    def test_phase3_diagnostic_has_no_original_game_actions(self):
        segment=ACCEPT.split(
            "private static boolean traceAt(",1)[1].split(
            "static void verifyOwnedClick(",1)[0]
        for v in (
            "phase != 3",
            "custommc.acceptance.reportKillAuraLockedTarget",
            "sample <= 960 && sample % 80 == 0",
            "CUSTOMMC_189_V367_LOCK_A_PHASE3_SOURCE_DIAG",
            "CUSTOMMC_189_V367_LOCK_A_PHASE3_RAY_DIAG",
            "CUSTOMMC_189_V367_LOCK_A_PHASE3_SCHEDULER_DIAG",
            "e.selected(e.a)",
            "host.playerRotationState().snapshot()",
            "host.targetRotationState().snapshot()",
        ):
            self.assertIn(v,segment)
        for forbidden in (
            "customMc", "sendPacket(", "attackEntity(", "shouldClick(",
            "rememberSelectedTarget(", "setRotation", ".onEnable()",
            ".onDisable()", "controller.", "Thread.sleep",
        ):
            self.assertNotIn(forbidden,segment)
        self.assertIn("if (phase==3) tracePhaseThree(host);",ACCEPT)

    def test_original_native_ray_observation_before_scheduler(self):
        action=BRIDGE.split("public static synchronized void autoClick(",1)[1].split(
            "public static synchronized void publishTick(",1)[0]
        self.assertIn("Minecraft189LockedTargetAcceptance.traceNativeRay(",action)
        self.assertLess(action.index("traceNativeRay("),
                        action.index("host.shouldAutoClick(playerHit, playerIndex, nativeHitbox)"))
        self.assertIn("Minecraft189LockedTargetAcceptance.verifyOwnedClick(",action)
        self.assertIn("minecraft.customMcAttackCrosshairEntity();",action)

    def test_scheduler_witness_does_not_change_admission_decisions(self):
        sched=HOST.split("private boolean shouldAutoClickInternal(",1)[1].split(
            "boolean mayDispatchOriginalCriticals()",1)[0]
        self.assertIn("Minecraft189LockedTargetAcceptance.traceNativeScheduler(",sched)
        self.assertIn("if (!click) return false;",sched)
        self.assertIn("if (!featureCatalog.criticals().permits(",sched)
        self.assertIn("clickRateTracker.recordPress(",sched)
        self.assertIn("return true;",sched)
        self.assertEqual(2,sched.count("traceNativeScheduler("))
        self.assertIn("custommc.acceptance.reportKillAuraLockedTarget",sched)


if __name__=="__main__":
    unittest.main()
