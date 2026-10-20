"""Guard that all original Mojang witness edits actually trigger real binary CI.

This is discovered by the existing offline asset-integrity test step. It
checks the pull_request paths filter, not merely the existence of a workflow.
"""
from pathlib import Path
import fnmatch
import unittest


ROOT = Path(__file__).resolve().parent.parent
WORKFLOW = ROOT / ".github" / "workflows" / "minecraft-189-real-client.yml"
SRC = "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform/v1_8_9/"
TEST = "platform-1.8.9/src/test/java/dev/trexzo/custommc/platform/v1_8_9/"


def pr_paths():
    text = WORKFLOW.read_text(encoding="utf-8")
    # Extract ONLY the actual pull_request filter; a documentation mention
    # or a different event is not sufficient proof of a triggered workflow.
    start = text.index("  pull_request:\n")
    end = text.index("  workflow_dispatch:", start)
    block = text[start:end]
    if "    paths:\n" not in block:
        raise AssertionError("Mojang workflow lacks a PR paths filter")
    return [
        line.strip().removeprefix("- ").strip("'\"")
        for line in block.splitlines()
        if line.strip().startswith("- '")
    ]


def triggered(filename):
    return any(fnmatch.fnmatchcase(filename, pattern) for pattern in pr_paths())


class OriginalWitnessPullRequestPathTests(unittest.TestCase):
    def test_actual_simultaneous_uuid_acceptance_changes_trigger_binary(self):
        self.assertTrue(triggered(SRC + "Minecraft189SimultaneousTargetAcceptance.java"))
        self.assertTrue(triggered(SRC + "Minecraft189OriginalCombatItemDiscoveryAcceptance.java"))
        self.assertTrue(triggered(SRC + "Minecraft189RejoinTargetAcceptance.java"))
        self.assertTrue(triggered(SRC + "Minecraft189VanillaMeleeAcceptance.java"))

    def test_acceptance_unit_and_regression_change_triggers_binary(self):
        self.assertTrue(triggered(TEST + "Minecraft189AnotherAcceptanceTest.java"))
        self.assertTrue(triggered(TEST + "Minecraft189SimultaneousPriorityRevalidationTest.java"))
        self.assertTrue(triggered("scripts/test_mojang189_workflow_acceptance_paths.py"))

    def test_native_business_logic_still_requires_binary(self):
        self.assertTrue(triggered(SRC + "Minecraft189AutoRodModule.java"))
        self.assertTrue(triggered(SRC + "Minecraft189AutoPotFeature.java"))
        self.assertTrue(triggered(SRC + "Minecraft189HostRuntime.java"))

    def test_original_position_speed_hud_changes_also_trigger_native_binary(self):
        # The real X/Z tracker is NOT named *Module.java, so an earlier
        # module-only workflow path filter would run Foundation alone.
        self.assertTrue(triggered(
            SRC + "Minecraft189MovementSpeedTracker.java"))
        self.assertTrue(triggered(
            TEST + "Minecraft189MovementSpeedTrackerFiniteTest.java"))

    def test_all_native_source_lease_impl_changes_trigger_original_binary(self):
        # A previously missing paths filter let a source-only refactor
        # pass Foundation without even scheduling native Mojang CI.
        for name in (
            "Minecraft189ExactSourceLease.java",
            "Minecraft189MovementSnapshotSourceLease.java",
            "Minecraft189PositionHostSourceLease.java",
            "Minecraft189WorldEntityHostSourceLease.java",
            "Minecraft189WorldEntityDownstreamSourceLease.java",
            "Minecraft189MotionHostSourceLease.java",
            "Minecraft189JumpHostSourceLease.java",
            "Minecraft189SprintHostSourceLease.java",
            "Minecraft189RotationHostSourceLease.java",
        ):
            with self.subTest(name=name):
                self.assertTrue(triggered(SRC + name))

    def test_native_source_lease_test_edits_also_trigger_original_binary(self):
        for name in (
            "Minecraft189ExactSourceLeaseConsolidationTest.java",
            "Minecraft189MotionHostSourceLeaseTest.java",
            "Minecraft189JumpHostSourceLeaseTest.java",
            "Minecraft189WorldEntityDownstreamSourceLeaseTest.java",
        ):
            with self.subTest(name=name):
                self.assertTrue(triggered(TEST + name))

    def test_real_two_player_and_rod_proofs_remain_mandatory(self):
        text = WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("Verify two SIMULTANEOUS real eligible UUID targets arbitrate and take separate damage", text)
        self.assertIn("V132 original Mojang fishing rod and potion discovery", text)
        self.assertIn("--native-rod-potion-discovery", text)


if __name__ == "__main__":
    unittest.main()
