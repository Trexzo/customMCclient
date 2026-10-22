"""V300 original 1.8.9 V16 World transition source-contract regression.

All tests inspect the original Python scenario and its strict native gates.
They are not a synthetic substitute for actual Minecraft GUI/server.
"""
import ast
from pathlib import Path
from unittest import TestCase

import RunMojang189SingleplayerSmoke as runner


class OriginalNativeV16WorldTransitionsTest(TestCase):
    @classmethod
    def setUpClass(cls):
        cls.full = Path(runner.__file__).read_text(encoding="utf-8")
        a = cls.full.index("            if native_scaffold_world:")
        b = cls.full.index("            if native_scaffold_tick:", a)
        cls.source = cls.full[a:b]

    def test_initial_original_high_teleport_requires_real_specific_server_ack(self):
        self.assertIn('"/tp CIProbe 0 120 0 0 0"',self.source)
        self.assertIn('b"[CHAT] Teleported CIProbe to 0.5, 120.0, 0.5"',
                      self.source)
        self.assertIn("send_verified_original_chat_reply(", self.source)
        self.assertNotIn('send_command(process, log, window, "/tp 0.5 120',self.source)

    def test_original_support_cell_is_source_reset_before_changed_stone(self):
        self.assertIn('"/fill 0 99 0 0 99 0 air"',self.source)
        self.assertIn('"/fill 0 99 0 0 99 0 stone"',self.source)
        self.assertLess(self.source.index('"/fill 0 99 0 0 99 0 air"'),
                        self.source.index('"/fill 0 99 0 0 99 0 stone"'))
        self.assertIn("send_verified_fill(process, log, window,",self.source)

    def test_original_adjacent_air_comes_from_independent_source_receipt(self):
        self.assertIn('"/fill 1 99 0 1 99 0 air"',self.source)
        self.assertIn('"missing fresh vanilla V16 adjacent air setup"',self.source)
        self.assertNotIn('"/setblock 1 99 0 air"',self.source)

    def test_real_source_reseat_requires_absolute_player_reply_after_stone(self):
        self.assertIn('"/tp CIProbe 0 100 0 0 0"',self.source)
        self.assertIn('b"[CHAT] Teleported CIProbe to 0.5, 100.0, 0.5"',self.source)
        self.assertLess(self.source.index('"/fill 0 99 0 0 99 0 stone"'),
                        self.source.index('"/tp CIProbe 0 100 0 0 0"'))

    def test_actual_server_testforblock_after_reseat_not_a_native_marker(self):
        self.assertIn('"/testforblock 0 99 0 stone"',self.source)
        self.assertIn('b"[CHAT] Successfully found the block at 0, 99, 0."',
                      self.source)
        self.assertLess(self.source.index('"/tp CIProbe 0 100 0 0 0"'),
                        self.source.index('"/testforblock 0 99 0 stone"'))
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_SUPPORT_GROUNDED=YES",
                         self.source[:self.source.index("while time.monotonic()")])

    def test_source_native_grounding_streak_is_still_original_and_strict(self):
        for token in ("native_safe_walk_positions(",
                      "len(actual) >= 3",
                      "all(", "abs(y - 100.0) < 0.2",
                      "raise AssertionError(\"vanilla player lacks sustained stone support"):
            self.assertIn(token,self.source)
        self.assertLess(self.source.index('"/testforblock 0 99 0 stone"'),
                        self.source.index("native_safe_walk_positions("))
        self.assertIn("since=v16_post_teleport_native_cursor",self.source)
        self.assertLess(self.source.index("v16_post_teleport_native_cursor ="),
                        self.source.index("native_safe_walk_positions("))
        self.assertIn("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_SUPPORT_GROUNDED=YES",self.source)

    def test_adjacent_native_air_stone_air_all_have_distinct_required_witnesses(self):
        for token in (
            "CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_AIR_PASS=YES",
            "CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_SOLID_PASS=YES",
            "CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_REVERTED_AIR_PASS=YES",
        ):
            self.assertIn(token,self.source)
        first=self.source.index("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_AIR_PASS=YES")
        second=self.source.index("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_SOLID_PASS=YES")
        third=self.source.index("CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_WORLD_REVERTED_AIR_PASS=YES")
        self.assertLess(first,second)
        self.assertLess(second,third)

    def test_source_stone_and_air_transitions_still_require_positive_changed_fill(self):
        self.assertIn('"/fill 1 99 0 1 99 0 stone"',self.source)
        self.assertIn('"/fill 1 99 0 1 99 0 air"',self.source)
        # Original mandatory support STONE, target STONE and target AIR
        # must each be individually changed in the true vanilla World.
        self.assertEqual(3,self.source.count("send_verified_fill(process, log, window,"))
        self.assertEqual(2,self.source.count("send_verified_original_air_preclear("))
        self.assertEqual(3,self.source.count("send_verified_original_chat_reply("))

    def test_no_fake_source_world_pass_or_packet_mutation_from_fixture(self):
        self.assertIn("await_marker(process, log,",self.source)
        self.assertIn('CUSTOMMC_OFFICIAL_189_NATIVE_SCAFFOLD_REAL_WORLD_TRANSITIONS_PASS=YES',
                      self.source)
        for unsafe in ("setBlockState(", "onPlayerRightClick(", "ItemStack(",
                       "sendPacket(", "skipNativeTest", "continue_on_error"):
            with self.subTest(unsafe=unsafe):
                self.assertNotIn(unsafe,self.source)


if __name__ == "__main__":
    import unittest
    unittest.main()
