"""V348: source-verified original pause-only XTest Escape is not PvP proof."""
from pathlib import Path
import importlib.util
from unittest import TestCase
from unittest.mock import Mock, patch
import sys
import tempfile
import time
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "scripts"
SOURCE = (SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


def harness():
    sys.path.insert(0, str(SCRIPTS))
    spec = importlib.util.spec_from_file_location(
        "v348_native_paused_host", SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py")
    obj = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(obj)
    return obj


class NativePauseV348(TestCase):
    @classmethod
    def setUpClass(cls):
        cls.mod = harness()

    def test_complete_native_source_only_last_screen_is_authoritative(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            log.write_bytes(b"junk gui text awz\n"
                            b"CUSTOMMC_189_ACCEPTANCE_SCREEN=awz\n"
                            b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)\n"
                            b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axp\n")
            self.assertEqual("axp", self.mod.v348_latest_original_gui_screen(log))
            with log.open("ab") as fd:
                fd.write(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)\n")
            self.assertEqual("(in-game/no GUI)",
                             self.mod.v348_latest_original_gui_screen(log))
            with log.open("ab") as fd:
                fd.write(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axp")
            self.assertEqual("(in-game/no GUI)",
                             self.mod.v348_latest_original_gui_screen(log))
            for invalid in (127, 0, True, 16777217):
                with self.assertRaises(ValueError):
                    self.mod.v348_latest_original_gui_screen(log, invalid)
            self.assertIsNone(self.mod.v348_latest_original_gui_screen(
                Path(folder) / "missing"))

    def test_already_clear_original_gui_never_sends_escape(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            log.write_bytes(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)\n")
            host = Mock(pid=345)
            host.poll.return_value = None
            with patch.object(self.mod, "send_real_host_key") as key:
                self.assertFalse(self.mod.v348_resume_confirmed_original_pause(
                    host, log, time.monotonic() + 1.0, "9001"))
                key.assert_not_called()

    def test_pause_requires_exact_host_window_and_fresh_clear_after_escape(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            log.write_bytes(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=awz\n"
                            b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)\n"
                            b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axp\n")
            host = Mock(pid=1234)
            host.poll.return_value = None
            # Two visible Minecraft windows are LEGAL after target launch.
            # The sole PRE-TARGET host X11 ID is what authorizes Escape.
            visible = Mock(returncode=0, stdout=b"9001\n9002\n")
            title = Mock(returncode=0, stdout=b"Minecraft 1.8.9\n")
            with patch.object(self.mod.subprocess, "run",
                              side_effect=[visible, title]) as run, \
                 patch.object(self.mod, "send_real_host_key") as key, \
                 patch.object(self.mod, "wait_for_new_original_source_marker") as proof:
                self.assertTrue(self.mod.v348_resume_confirmed_original_pause(
                    host, log, time.monotonic() + 4.0, "9001"))
                self.assertEqual(2, run.call_count)
                self.assertNotIn("--pid", run.call_args_list[0].args[0])
                self.assertEqual(
                    ["xdotool", "getwindowname", "9001"],
                    run.call_args_list[1].args[0])
                key.assert_called_once_with(host, "9001", "Escape")
                proof.assert_called_once()
                args = proof.call_args.args
                self.assertEqual(log, args[1])
                self.assertEqual(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)",
                                 args[3])
                self.assertEqual("real XTest Escape closed original axp pause", args[5])

    def test_ambiguous_native_window_must_fail_without_escape(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            log.write_bytes(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axp\n")
            host = Mock(pid=1234)
            host.poll.return_value = None
            # Multiple target windows are expected, but the captured
            # original host ID must still exist among visible windows.
            result = Mock(returncode=0, stdout=b"77\n88\n")
            with patch.object(self.mod.subprocess, "run", return_value=result), \
                 patch.object(self.mod, "send_real_host_key") as key:
                with self.assertRaisesRegex(AssertionError, "disappeared"):
                    self.mod.v348_resume_confirmed_original_pause(
                        host, log, time.monotonic() + 2.0, "9001")
                key.assert_not_called()

    def test_exact_two_rejoin_calls_and_strict_server_authority_stay(self):
        run = SOURCE.split("def run(", 1)[1].split("    except BaseException:", 1)[0]
        # V366 R6 adds one independently guarded sticky-lock recovery.
        # The previous two rejoin recoveries must remain exactly two.
        self.assertEqual(3, run.count("v348_resume_confirmed_original_pause("))
        self.assertEqual(2, run.split("if args.locked_target:", 1)[0].count(
            "v348_resume_confirmed_original_pause("))
        self.assertEqual(3, run.count("host, host_log, deadline, host_window)"))
        sticky = run.split("if args.locked_target:", 1)[1].split(
            "if args.simultaneous:", 1)[0]
        self.assertEqual(1, sticky.count("v348_resume_confirmed_original_pause("))
        self.assertIn(
            'v348_latest_original_gui_screen(host_log) != "(in-game/no GUI)"',
            sticky)
        pretarget = run.index("host_window = identify_original_host_window_before_target(host)", 
                              run.index("host_cmd = client_command("))
        self.assertLess(pretarget, run.index('with target_log.open("wb")'))
        self.assertNotIn("--pid", SOURCE.split(
            "def v348_resume_confirmed_original_pause(", 1)[1].split(
            "def restore_real_attacker_focus(", 1)[0])
        self.assertIn("if args.rejoin_target or args.rejoin_release:", run)
        self.assertIn(
            "if args.rejoin_target or args.rejoin_release or args.locked_target:",
            run)
        self.assertIn("wait_for_log_occurrences(", run)
        method = SOURCE.split("def v348_resume_confirmed_original_pause(", 1)[1].split(
            "def restore_real_attacker_focus(", 1)[0]
        self.assertIn('if screen != "axp":', method)
        self.assertIn('if v348_latest_original_gui_screen(host_log) != "axp":',
                      method)
        self.assertIn('send_real_host_key(host, window, "Escape")', method)
        self.assertIn("wait_for_new_original_source_marker(", method)
        self.assertNotIn('click", "--window"', method)
        self.assertNotIn("leftClickCounter =", method)
        for gate in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(gate, SOURCE)


if __name__ == "__main__":
    unittest.main()
