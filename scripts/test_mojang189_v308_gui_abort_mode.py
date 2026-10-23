"""V308 offline control-plane regression for original three-stone E abort.

This is NOT original Minecraft runtime evidence. The full GUI/X11 + vanilla
server step remains mandatory and independently checks real source and blocks.
"""
import argparse
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    configure_original_scaffold_gui_negative,
)


class OriginalScaffoldGuiAbortModeTest(unittest.TestCase):
    def test_negative_mode_reuses_genuine_three_stone_fixture_only(self):
        args = argparse.Namespace(
            scaffold_placement="production-repeat-three-menu-after-first",
            pvp=False)
        self.assertIs(configure_original_scaffold_gui_negative(args), args)
        self.assertTrue(args.scaffold_gui_abort)
        self.assertEqual("production-repeat-three", args.scaffold_placement)
        self.assertFalse(args.pvp)

    def test_positive_and_default_modes_stay_independent(self):
        for mode in (None, "enabled", "menu-open",
                     "production-repeat-three", "production-repeat-four",
                     "production-repeat-three-history",
                     "production-repeat-two-keyboard-walk"):
            with self.subTest(mode=mode):
                args = argparse.Namespace(scaffold_placement=mode)
                configure_original_scaffold_gui_negative(args)
                self.assertFalse(args.scaffold_gui_abort)
                self.assertEqual(mode, args.scaffold_placement)

    def test_unreviewed_value_cannot_activate_negative_property(self):
        for mode in ("production-repeat-three-menu-after-first ",
                     "production-repeat-three-menu-after-first-extra", "",
                     "production-repeat-three-menu"):
            with self.subTest(mode=mode):
                args = argparse.Namespace(scaffold_placement=mode)
                configure_original_scaffold_gui_negative(args)
                self.assertFalse(args.scaffold_gui_abort)
                self.assertEqual(mode, args.scaffold_placement)


if __name__ == "__main__":
    unittest.main()
