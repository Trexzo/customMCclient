"""V331: original 1.8.9 focus recovery must be a *release*, before arms.

These wiring tests never confer real controller, hurt or dedicated-server PASS.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8"
)


class V331RealNativePrearmReleaseTests(unittest.TestCase):
    def test_x11_release_is_only_in_host_active_prearm_branch(self):
        focus = HARNESS.split(
            "def restore_real_attacker_focus(", 1
        )[1].split("\ndef ", 1)[0]
        grab = focus.index('"click", "--window", window, "1"')
        isolated = focus.index('if keep_host_active:', grab)
        release = focus.index('"xdotool", "mouseup", "1"', isolated)
        marker = focus.index("AUTOCLICKER_PREARM_REAL_MOUSE_RELEASE=YES", release)
        wait = focus.index("time.sleep(2.0)", marker)
        return_focus = focus.index("if keep_host_active:", wait)
        self.assertLess(grab, isolated)
        self.assertLess(isolated, release)
        self.assertLess(release, marker)
        self.assertLess(marker, wait)
        self.assertLess(wait, return_focus)
        self.assertEqual(1, focus.count('"xdotool", "mouseup", "1"'))
        self.assertNotIn('"xdotool", "mousedown"', focus)
        self.assertNotIn('"xdotool", "key"', focus)

    def test_native_controller_and_authoritative_damage_remain_mandatory(self):
        segment = HARNESS.split("if args.autoclicker_pvp:", 1)[1]
        self.assertIn("AUTO_NATIVE_UNSCOPED, deadline", HARNESS)
        self.assertIn("AUTO_NATIVE_EXACT_REMOTE, deadline", HARNESS)
        self.assertIn("AUTO_HURT if args.autoclicker_pvp", HARNESS)
        self.assertIn("scoreboard players test CITarget taken 1 999999", HARNESS)
        self.assertIn("keep_host_active=args.autoclicker_pvp", HARNESS)
        self.assertNotIn("AUTOCLICKER_PREARM_REAL_MOUSE_RELEASE=YES", segment[:500])
        self.assertNotIn("leftClickCounter =", HARNESS)
        self.assertNotIn("attackEntity(", HARNESS)

    def test_scope_other_modes_are_unchanged(self):
        focus = HARNESS.split(
            "def restore_real_attacker_focus(", 1
        )[1].split("\ndef ", 1)[0]
        self.assertEqual(2, focus.count("if keep_host_active:"))
        self.assertIn('["xdotool", "windowactivate", "--sync", window]', focus)
        self.assertIn("CUSTOMMC_189_AUTOCLICKER_REAL_HOST_X11_FOCUS_PASS=YES", focus)


if __name__ == "__main__":
    unittest.main()
