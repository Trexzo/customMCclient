"""Pure regression tests for source-original host X11 identification.

No X11 server, Mojang binary, credentials or gameplay window required.
"""
import subprocess
import tempfile
import time
import unittest
from pathlib import Path
from unittest.mock import Mock, patch

from RunMojang189LocalhostTwoPlayerSmoke import (
    identify_original_host_window_before_target,
    send_real_host_key,
    original_hud_source_screen,
    require_original_hud_clear_screen,
)


class OriginalHostWindowFixtureTest(unittest.TestCase):
    @staticmethod
    def host():
        proc = Mock()
        proc.poll.return_value = None
        return proc

    @staticmethod
    def xdotool(cmd, **kwargs):
        if cmd[1] == "search":
            return subprocess.CompletedProcess(cmd, 0, "751\n", "")
        if cmd[1] == "getwindowname":
            if cmd[2] != "751":
                return subprocess.CompletedProcess(cmd, 1, "", "wrong window")
            return subprocess.CompletedProcess(cmd, 0, "Minecraft 1.8.9\n", "")
        return subprocess.CompletedProcess(cmd, 0, "", "")

    @patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run")
    def test_original_host_captured_before_any_second_client(self, run):
        run.side_effect = self.xdotool
        original = identify_original_host_window_before_target(self.host())
        self.assertEqual("751", original)
        self.assertEqual(["xdotool", "search", "--onlyvisible", "--name",
                          "Minecraft"], run.call_args_list[0].args[0])
        self.assertFalse(any(
            call.args[0][1] == "getwindowpid" for call in run.call_args_list),
            "legacy LWJGL may not expose an X11 PID")

    @patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run")
    def test_two_windows_never_fall_back_to_first_or_last(self, run):
        def duplicate(cmd, **kwargs):
            return subprocess.CompletedProcess(cmd, 0, "751\n752\n", "")
        run.side_effect = duplicate
        with self.assertRaisesRegex(AssertionError, "exactly one"):
            identify_original_host_window_before_target(self.host())
        self.assertEqual(1, run.call_count)

    @patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run")
    def test_zero_windows_and_wrong_title_fail_closed(self, run):
        run.return_value = subprocess.CompletedProcess([], 1, "", "")
        with self.assertRaisesRegex(AssertionError, "exactly one"):
            identify_original_host_window_before_target(self.host())
        def wrong_title(cmd, **kwargs):
            if cmd[1] == "search":
                return subprocess.CompletedProcess(cmd, 0, "751\n", "")
            return subprocess.CompletedProcess(cmd, 0, "Other Client", "")
        run.side_effect = wrong_title
        with self.assertRaisesRegex(AssertionError, "Minecraft title"):
            identify_original_host_window_before_target(self.host())

    @patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run")
    def test_keys_only_reach_captured_host_window(self, run):
        run.side_effect = self.xdotool
        host = self.host()
        captured = identify_original_host_window_before_target(host)
        send_real_host_key(host, captured, "1")
        send_real_host_key(host, captured, "e")
        send_real_host_key(host, captured, "Escape")
        keys = [call.args[0] for call in run.call_args_list
                if call.args[0][1] == "key"]
        self.assertEqual([["xdotool", "key", "--clearmodifiers", "1"],
                          ["xdotool", "key", "--clearmodifiers", "e"],
                          ["xdotool", "key", "--clearmodifiers", "Escape"]], keys)
        activations = [call.args[0] for call in run.call_args_list
                       if call.args[0][1] == "windowactivate"]
        self.assertEqual(["751", "751", "751"], [x[-1] for x in activations])
        with self.assertRaises(ValueError):
            send_real_host_key(host, captured, "F3")
        with self.assertRaisesRegex(AssertionError, "not identified"):
            send_real_host_key(host, None, "e")

    @patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run")
    def test_lost_window_cannot_be_replaced_by_a_different_client(self, run):
        run.return_value = subprocess.CompletedProcess([], 1, "", "")
        with self.assertRaisesRegex(AssertionError, "no longer exists"):
            send_real_host_key(self.host(), "751", "e")
        self.assertFalse(any(c.args[0][1] == "key"
                             for c in run.call_args_list))

    def test_hud_visual_source_clear_does_not_send_escape(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "native.log"
            path.write_bytes(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)\n")
            self.assertEqual(b"(in-game/no GUI)",
                             original_hud_source_screen(path))
            with patch("RunMojang189LocalhostTwoPlayerSmoke.send_real_host_key") as key:
                require_original_hud_clear_screen(
                    self.host(), path, "751", time.monotonic() + 1)
                key.assert_not_called()

    def test_hud_visual_real_pause_requires_fresh_original_clear_after_escape(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "native.log"
            path.write_bytes(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axp\n")
            def original_escape(_host, window, key):
                self.assertEqual(("751", "Escape"), (window, key))
                with path.open("ab") as native:
                    native.write(
                        b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)\n")
            with patch("RunMojang189LocalhostTwoPlayerSmoke.send_real_host_key",
                       side_effect=original_escape) as send:
                require_original_hud_clear_screen(
                    self.host(), path, "751", time.monotonic() + 2)
                send.assert_called_once()

    def test_hud_visual_unknown_native_gui_cannot_be_dismissed_or_certified(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "native.log"
            path.write_bytes(b"no original screen evidence\n")
            with patch("RunMojang189LocalhostTwoPlayerSmoke.send_real_host_key") as key:
                with self.assertRaisesRegex(AssertionError, "unverified"):
                    require_original_hud_clear_screen(
                        self.host(), path, "751", time.monotonic() + 1)
                path.write_bytes(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axv\n")
                with self.assertRaisesRegex(AssertionError, "unverified"):
                    require_original_hud_clear_screen(
                        self.host(), path, "751", time.monotonic() + 1)
                key.assert_not_called()

    def test_hud_visual_stale_pause_never_counts_as_post_input_clear(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "native.log"
            path.write_bytes(
                b"CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)\n"
                b"CUSTOMMC_189_ACCEPTANCE_SCREEN=axp\n")
            with patch("RunMojang189LocalhostTwoPlayerSmoke.send_real_host_key") as key:
                with self.assertRaisesRegex(AssertionError, "fresh source"):
                    require_original_hud_clear_screen(
                        self.host(), path, "751", time.monotonic() + .01)
                key.assert_called_once()


if __name__ == "__main__":
    unittest.main()
