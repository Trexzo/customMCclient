"""V309 strict non-native regression for real hotbar-veto scenario dispatch.

Mocked X11 transport here does NOT certify game behavior. Original real
client/server CI remains mandatory; checks original selected slot and air.
"""
import argparse
import subprocess
import unittest
from unittest.mock import Mock, patch

from RunMojang189LocalhostTwoPlayerSmoke import (
    configure_original_scaffold_gui_negative,
    configure_original_scaffold_hotbar_negative,
    send_real_host_key,
)


class OriginalHotbarAbortFixtureTest(unittest.TestCase):
    def test_exact_hotbar_mode_maps_only_to_original_production_three(self):
        args = argparse.Namespace(
            scaffold_placement="production-repeat-three-hotbar-after-first")
        configure_original_scaffold_gui_negative(args)
        configure_original_scaffold_hotbar_negative(args)
        self.assertTrue(args.scaffold_hotbar_abort)
        self.assertFalse(args.scaffold_gui_abort)
        self.assertEqual("production-repeat-three", args.scaffold_placement)

    def test_gui_abort_and_unrelated_positive_modes_remain_isolated(self):
        for mode in (None, "enabled", "production-repeat-three",
                     "production-repeat-three-menu-after-first",
                     "production-repeat-four", "production-repeat-three-history",
                     "production-repeat-three-hotbar-after-first-extra", ""):
            with self.subTest(mode=mode):
                args = argparse.Namespace(scaffold_placement=mode)
                configure_original_scaffold_gui_negative(args)
                configure_original_scaffold_hotbar_negative(args)
                self.assertFalse(args.scaffold_hotbar_abort)
                self.assertEqual(
                    mode == "production-repeat-three-menu-after-first",
                    args.scaffold_gui_abort)

    def test_original_host_slot_two_uses_x11_keyboard_not_native_item_write(self):
        host = Mock()
        with patch("RunMojang189LocalhostTwoPlayerSmoke.check_process") as check, \
             patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run") as run:
            run.return_value = subprocess.CompletedProcess(
                args=["xdotool", "getwindowname", "731"], returncode=0,
                stdout="Minecraft 1.8.9")
            send_real_host_key(host, "731", "2")
            check.assert_called_once_with(host, "host X11 key")
            self.assertEqual([
                ["xdotool", "getwindowname", "731"],
                ["xdotool", "windowactivate", "--sync", "731"],
                ["xdotool", "key", "--clearmodifiers", "2"],
            ], [call.args[0] for call in run.call_args_list])

    def test_unreviewed_key_never_reaches_original_host(self):
        host = Mock()
        with patch("RunMojang189LocalhostTwoPlayerSmoke.check_process") as check, \
             patch("RunMojang189LocalhostTwoPlayerSmoke.subprocess.run") as run:
            for key in ("3", "F7", "Return", "2;echo", "", "BackSpace"):
                with self.subTest(key=key):
                    with self.assertRaises(ValueError):
                        send_real_host_key(host, "731", key)
            check.assert_not_called()
            run.assert_not_called()


if __name__ == "__main__":
    unittest.main()
