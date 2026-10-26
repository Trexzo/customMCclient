"""Original Mojang Auto Clicker focus test: prearm Escape is not an attack.

No module/counter/UUID authority is granted by a genuine X11 keyboard event.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
HARNESS = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8"
)


class V332OriginalPrearmEscapeTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.focus = HARNESS.split(
            "def restore_real_attacker_focus(", 1
        )[1].split("\ndef ", 1)[0]

    def test_escape_reaches_only_live_active_host_before_physical_grab(self):
        focus = self.focus
        owner = focus.index('if keep_host_active:')
        key = focus.index('["xdotool", "key", "--clearmodifiers", "Escape"]')
        mouse = focus.index('["xdotool", "mousemove", "--window", window,')
        click = focus.index('["xdotool", "click", "--window", window, "1"]')
        release = focus.index('["xdotool", "mouseup", "1"]')
        sleep = focus.index('time.sleep(2.0)')
        self.assertLess(owner, key)
        self.assertLess(key, mouse)
        self.assertLess(mouse, click)
        self.assertLess(click, release)
        self.assertLess(release, sleep)
        self.assertEqual(1, focus.count('"key", "--clearmodifiers", "Escape"'))
        self.assertIn('if int(subprocess.check_output(', focus)
        self.assertIn('raise AssertionError("V332 prearm Escape lost original CIHost window")', focus)
        self.assertNotIn('"xdotool", "mousedown"', focus)

    def test_escape_marker_is_diagnostic_only(self):
        focus = self.focus
        self.assertEqual(1, focus.count("AUTOCLICKER_PREARM_REAL_GUI_ESCAPE=YES"))
        self.assertNotIn("AUTO_NATIVE_UNSCOPED", focus)
        self.assertNotIn("AUTO_NATIVE_EXACT_REMOTE", focus)
        # Genuine native controller and server-owned damage are required later.
        self.assertIn("AUTO_NATIVE_UNSCOPED, deadline", HARNESS)
        self.assertIn("AUTO_NATIVE_EXACT_REMOTE, deadline", HARNESS)
        self.assertIn("AUTO_HURT if args.autoclicker_pvp", HARNESS)
        self.assertIn("scoreboard players test CITarget taken 1 999999", HARNESS)

    def test_no_game_source_mutation_and_other_modes_do_not_get_prearm_escape(self):
        focus = self.focus
        self.assertNotIn("leftClickCounter =", focus)
        self.assertNotIn("attackEntity(", focus)
        self.assertNotIn("customMc", focus)
        self.assertEqual(3, focus.count("if keep_host_active:"))
        # The early Escape is inside the first keep_host_active branch.
        isolated = focus.index("if keep_host_active:")
        marker = focus.index("AUTOCLICKER_PREARM_REAL_GUI_ESCAPE=YES")
        self.assertLess(isolated, marker)
        self.assertLess(marker, focus.index('["xdotool", "click", "--window", window, "1"]'))


if __name__ == "__main__":
    unittest.main()
