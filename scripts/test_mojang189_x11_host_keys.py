"""Pure regression tests for source-original host X11 identification.

No X11 server, Mojang binary, credentials or gameplay window required.
"""
import subprocess
import unittest
from unittest.mock import Mock, patch

from RunMojang189LocalhostTwoPlayerSmoke import (
    identify_original_host_window_before_target,
    send_real_host_key,
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
            return subprocess.CompletedProcess(cmd, 0, "751\\n", "")
        if cmd[1] == "getwindowname":
            if cmd[2] != "751":
                return subprocess.CompletedProcess(cmd, 1, "", "wrong window")
            return subprocess.CompletedProcess(cmd, 0, "Minecraft 1.8.9\\n", "")
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
            return subprocess.CompletedProcess(cmd, 0, "751\\n752\\n", "")
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
                return subprocess.CompletedProcess(cmd, 0, "751\\n", "")
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
        keys = [call.args[0] for call in run.call_args_list
                if call.args[0][1] == "key"]
        self.assertEqual([["xdotool", "key", "--clearmodifiers", "1"],
                          ["xdotool", "key", "--clearmodifiers", "e"]], keys)
        activations = [call.args[0] for call in run.call_args_list
                       if call.args[0][1] == "windowactivate"]
        self.assertEqual(["751", "751"], [x[-1] for x in activations])
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


if __name__ == "__main__":
    unittest.main()
