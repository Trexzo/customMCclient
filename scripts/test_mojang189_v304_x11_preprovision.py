"""V304: guard native Mojang CI against host package setup eating gameplay time.

These source-only guards never substitute for genuine original Minecraft 1.8.9
GUI/server verification. They preserve the existing 78 real binary stages.
"""
from pathlib import Path
from unittest import TestCase

ROOT = Path(__file__).resolve().parent.parent
WORKFLOW = ROOT / ".github" / "workflows" / "minecraft-189-real-client.yml"
PREP = "      - name: Provision original X11 tools before native gameplay deadlines"
FIRST = "      - name: Run offline singleplayer with genuine player and world Combat snapshots"
NEXT = "      - name: V94 genuine Mojang Step collision field and physical X11 ClickGUI keyboard focus"


class OriginalX11ProvisioningTests(TestCase):
    @classmethod
    def setUpClass(cls):
        cls.workflow = WORKFLOW.read_text(encoding="utf-8")
        cls.prep = cls.workflow[cls.workflow.index(PREP):cls.workflow.index(FIRST)]
        cls.native = cls.workflow[cls.workflow.index(FIRST):cls.workflow.index(NEXT)]

    def test_original_x11_provisioning_precedes_strict_first_native_stage(self):
        self.assertLess(self.workflow.index(PREP), self.workflow.index(FIRST))
        self.assertIn("timeout-minutes: 12", self.prep)
        self.assertEqual(1, self.workflow.count(PREP))

    def test_all_original_x11_dependencies_are_checked_before_gameplay(self):
        for executable in ("xvfb-run", "xrandr", "xdotool", "openbox"):
            self.assertIn("! command -v " + executable, self.prep)
            self.assertIn("command -v " + executable, self.prep)

    def test_original_package_install_remains_real_and_fails_closed(self):
        self.assertIn("sudo apt-get -o Acquire::Retries=2 update", self.prep)
        self.assertIn("DEBIAN_FRONTEND=noninteractive apt-get", self.prep)
        self.assertIn("-y --no-install-recommends --no-upgrade", self.prep)
        self.assertIn("xvfb x11-xserver-utils xdotool openbox", self.prep)
        self.assertNotIn("|| true", self.prep)
        self.assertNotIn("continue-on-error", self.prep)

    def test_native_gameplay_timer_stays_four_minutes_without_host_apt(self):
        self.assertIn("timeout-minutes: 4", self.native)
        self.assertNotIn("apt-get", self.native)
        self.assertIn("command -v xdotool", self.native)
        self.assertIn("command -v openbox", self.native)
        self.assertIn("RunMojang189SingleplayerSmoke.py", self.native)
        self.assertIn("--timeout 120 --native-world-camera --native-safe-walk", self.native)

    def test_no_original_binary_paths_or_native_acceptance_markers_waived(self):
        self.assertIn("      - name: V26 prove original native edge air target and adjacent support ray candidate", self.workflow)
        self.assertIn("      - name: V19 prove original World raytrace exact support face and wrong-hit vetoes", self.workflow)
        self.assertIn("      - name: V29 prove vanilla survival controller placement and client readback", self.workflow)
        self.assertIn("      - name: Verify two SIMULTANEOUS real eligible UUID targets arbitrate and take separate damage", self.workflow)
        self.assertNotIn("continue-on-error: true", self.native)

    def test_original_workflow_still_triggers_for_workflow_and_regression_edits(self):
        pull_request = self.workflow.split("  pull_request:", 1)[1].split("  workflow_dispatch:", 1)[0]
        self.assertIn("'.github/workflows/minecraft-189-real-client.yml'", pull_request)
        self.assertIn("'scripts/test_mojang189_*.py'", pull_request)
        self.assertEqual(1, self.workflow.count("  workflow_dispatch:"))


if __name__ == "__main__":
    import unittest
    unittest.main()
