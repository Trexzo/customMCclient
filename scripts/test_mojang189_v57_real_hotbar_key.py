"""V57 real-X11 hotbar abort: exercise the actual whitelisted key sender.

Pure test: only mocks xdotool/process I/O. The separate original Minecraft
CI stage must still prove that slot 2 genuinely changed InventoryPlayer,
revoked the live repeat lease and left two real server blocks as air.
"""
import subprocess
import unittest
from unittest.mock import Mock, call, patch

from RunMojang189LocalhostTwoPlayerSmoke import send_real_host_key


class RealOriginalHotbarKeyTest(unittest.TestCase):
    def test_real_slot_two_is_forwarded_to_known_host_x11_window(self):
        host = Mock()
        with patch("RunMojang189LocalhostTwoPlayerSmoke.check_process") as check, \
             patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run") as run:
            run.return_value = subprocess.CompletedProcess(
                args=["xdotool", "getwindowname", "731"], returncode=0,
                stdout="Minecraft 1.8.9")
            send_real_host_key(host, "731", "2")
            check.assert_called_once_with(host, "host X11 key")
            self.assertEqual(3, run.call_count)
            self.assertEqual(
                ["xdotool", "getwindowname", "731"],
                run.call_args_list[0].args[0])
            self.assertEqual(
                ["xdotool", "windowactivate", "--sync", "731"],
                run.call_args_list[1].args[0])
            self.assertEqual(
                ["xdotool", "key", "--clearmodifiers", "2"],
                run.call_args_list[2].args[0])

    def test_unreviewed_keys_cannot_reach_xdotool_or_host(self):
        host = Mock()
        with patch("RunMojang189LocalhostTwoPlayerSmoke.check_process") as check, \
             patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run") as run:
            for key in ("3", "F7", "Return", "space", "", "2;echo"):
                with self.subTest(key=key):
                    with self.assertRaisesRegex(ValueError, "invalid original host"):
                        send_real_host_key(host, "731", key)
            check.assert_not_called()
            run.assert_not_called()

    def test_real_original_whitelist_retains_previous_keys(self):
        host = Mock()
        for key in ("e", "1", "Escape"):
            with self.subTest(key=key), \
                 patch("RunMojang189LocalhostTwoPlayerSmoke.check_process"), \
                 patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run") as run:
                run.return_value = subprocess.CompletedProcess(
                    args=["xdotool", "getwindowname", "731"], returncode=0,
                    stdout="Minecraft 1.8.9")
                send_real_host_key(host, "731", key)
                self.assertEqual(
                    ["xdotool", "key", "--clearmodifiers", key],
                    run.call_args_list[-1].args[0])


if __name__ == "__main__":
    unittest.main()
