"""V349: only the pre-target original host Minecraft window authorizes XTest.

Multiple other real Minecraft windows may exist later. LWJGL2 often omits
_NET_WM_PID. Never guess from '--pid', window ordering, or active-window focus.
"""
from pathlib import Path
import importlib.util
from unittest import TestCase
from unittest.mock import Mock, patch
import sys
import tempfile
import time
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


def harness():
    scripts = ROOT / "scripts"
    sys.path.insert(0, str(scripts))
    spec = importlib.util.spec_from_file_location(
        "v349_original_host_x11", scripts / "RunMojang189LocalhostTwoPlayerSmoke.py")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


class V349OriginalHostWindowOwner(TestCase):
    @classmethod
    def setUpClass(cls):
        cls.mod = harness()

    def test_unique_pretarget_original_host_window_is_captured(self):
        host = Mock(pid=1234)
        found = Mock(returncode=0, stdout="901\n")
        title = Mock(returncode=0, stdout="Minecraft 1.8.9")
        with patch.object(self.mod, "check_process"), \
             patch.object(self.mod.subprocess, "run",
                          side_effect=[found, title]) as run:
            self.assertEqual("901", self.mod.identify_original_host_window_before_target(host))
            self.assertIn("--onlyvisible", run.call_args_list[0].args[0])
            self.assertNotIn("--pid", run.call_args_list[0].args[0])
            self.assertEqual(["xdotool", "getwindowname", "901"],
                             run.call_args_list[1].args[0])

    def test_ambiguous_pretarget_window_fails_without_arbitrary_selection(self):
        host = Mock(pid=5678)
        ambiguous = Mock(returncode=0, stdout="901\n902\n")
        with patch.object(self.mod, "check_process"), \
             patch.object(self.mod.subprocess, "run",
                          return_value=ambiguous) as run:
            with self.assertRaisesRegex(AssertionError, "exactly one"):
                self.mod.identify_original_host_window_before_target(host)
            run.assert_called_once()

    def test_pretarget_window_must_still_be_visible_and_keep_native_title(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            log.write_bytes(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axp\n")
            host = Mock(pid=5678)
            host.poll.return_value = None
            found = Mock(returncode=0, stdout=b"901\n902\n")
            renamed = Mock(returncode=0, stdout=b"unrelated application")
            with patch.object(self.mod.subprocess, "run",
                              side_effect=[found, renamed]), \
                 patch.object(self.mod, "send_real_host_key") as key:
                with self.assertRaisesRegex(AssertionError, "title invalid"):
                    self.mod.v348_resume_confirmed_original_pause(
                        host, log, time.monotonic() + 5.0, "901")
                key.assert_not_called()

    def test_strict_original_native_acceptance_unchanged_and_no_pid_fallback(self):
        run = SOURCE.split("def run(", 1)[1].split("    except BaseException:", 1)[0]
        capture = run.index(
            "host_window = identify_original_host_window_before_target(host)",
            run.index("host_cmd = client_command("))
        launch = run.index('with target_log.open("wb")')
        self.assertLess(capture, launch)
        # V366 R7 permits precisely two additional calls: one in sticky-A,
        # one in simultaneous PvP. The two rejoin calls remain unchanged.
        self.assertEqual(4, run.count(
            "v348_resume_confirmed_original_pause("))
        self.assertEqual(2, run.split("if args.locked_target:", 1)[0].count(
            "v348_resume_confirmed_original_pause("))
        sticky = run.split("if args.locked_target:", 1)[1].split(
            "if args.simultaneous:", 1)[0]
        self.assertEqual(1, sticky.count("v348_resume_confirmed_original_pause("))
        simultaneous = run.split("if args.simultaneous:", 1)[1].split(
            "if args.multi_target:", 1)[0]
        self.assertEqual(1, simultaneous.count("v348_resume_confirmed_original_pause("))
        self.assertIn(
            "or args.locked_target or args.simultaneous):",
            run)
        self.assertIn("CUSTOMMC_189_V366_LOCK_PREATTACK_ORIGINAL_GUI_CLEAR_PASS=YES",
                      sticky)
        method = SOURCE.split(
            "def v348_resume_confirmed_original_pause(", 1)[1].split(
            "def restore_real_attacker_focus(", 1)[0]
        self.assertIn("original_host_window", method)
        self.assertIn('if screen != "axp":', method)
        self.assertIn("v348_latest_original_gui_screen(host_log)", method)
        self.assertIn('send_real_host_key(host, window, "Escape")', method)
        self.assertNotIn('"--pid"', method)
        self.assertNotIn("leftClickCounter =", method)
        for mandatory in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(mandatory, SOURCE)


if __name__ == "__main__":
    unittest.main()
