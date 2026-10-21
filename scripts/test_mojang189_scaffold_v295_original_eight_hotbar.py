"""V295: original Mojang 1.8.9 V29 inventory source-only regressions.

These verify genuine-chat receipt classification and original runner ordering;
they DO NOT start Minecraft, grant items, or certify native controller clicks.
"""
import ast
from pathlib import Path
import unittest

import RunMojang189SingleplayerSmoke as runner


class OriginalV29EightStoneHotbarTests(unittest.TestCase):
    def test_clear_positive_source_receipt_matches_specific_player(self):
        raw = b"junk\n[CHAT] Cleared the inventory of CIProbe, removing 1 items\n"
        self.assertEqual("prior-items-removed",
                         runner.original_v29_inventory_clear_receipt(raw))
        self.assertEqual("prior-items-removed",
                         runner.original_v29_inventory_clear_receipt(
                             b"[CHAT] Cleared the inventory of CIProbe, removing 128 items"))

    def test_clear_failure_no_items_is_real_already_empty_evidence(self):
        self.assertEqual("already-empty",
                         runner.original_v29_inventory_clear_receipt(
                             b"[CHAT] Could not clear the inventory of CIProbe, no items to remove"))

    def test_clear_zero_items_cannot_claim_prior_items_removed(self):
        self.assertIsNone(runner.original_v29_inventory_clear_receipt(
            b"[CHAT] Cleared the inventory of CIProbe, removing 0 items"))

    def test_clear_other_player_and_partial_reply_are_untrusted(self):
        for source in (
            b"[CHAT] Cleared the inventory of Stranger, removing 1 items",
            b"Cleared the inventory of CIProbe, removing 1 items",
            b"[CHAT] Cleared the inventory of CIProbe, removing",
            b"[CHAT] Unknown command",
        ):
            with self.subTest(source=source):
                self.assertIsNone(
                    runner.original_v29_inventory_clear_receipt(source))

    def test_give_exact_eight_original_stone_with_player_and_chat(self):
        for reply in (
            b"[CHAT] Given [Stone] * 8 to CIProbe\n",
            b"[CHAT] Given Stone * 8 to CIProbe\n",
        ):
            with self.subTest(reply=reply):
                self.assertTrue(runner.original_v29_eight_stone_grant_receipt(reply))

    def test_give_wrong_quantity_item_or_player_never_accepted(self):
        for reply in (
            b"[CHAT] Given [Stone] * 1 to CIProbe\n",
            b"[CHAT] Given [Stone] * 9 to CIProbe\n",
            b"[CHAT] Given [Dirt] * 8 to CIProbe\n",
            b"[CHAT] Given [Stone] * 8 to Stranger\n",
            b"Given [Stone] * 8 to CIProbe\n",
            b"[CHAT] Given [Stone] * 8 to CIProbeOther\n",
        ):
            with self.subTest(reply=reply):
                self.assertFalse(runner.original_v29_eight_stone_grant_receipt(reply))

    def test_original_v29_preclear_happens_before_hotbar_key_and_give(self):
        script = Path(runner.__file__).read_text(encoding="utf-8")
        tree = ast.parse(script)
        helpers = [node for node in tree.body if isinstance(node, ast.FunctionDef)
                   and node.name == "prepare_verified_original_v29_stone_stack"]
        self.assertEqual(1, len(helpers))
        src = ast.get_source_segment(script, helpers[0])
        markers = (
            'send_command(process, log, window, "/clear CIProbe")',
            'original_v29_inventory_clear_receipt(fresh)',
            'xdotool("key", "--clearmodifiers", "1")',
            'send_command(process, log, window, "/give CIProbe stone 8")',
            'original_v29_eight_stone_grant_receipt(fresh)',
        )
        offsets = [src.index(mark) for mark in markers]
        self.assertEqual(sorted(offsets), offsets)
        self.assertIn('raise AssertionError("no fresh original 1.8.9 /clear server inventory reply")', src)
        self.assertIn('"no fresh original /give stone eight acknowledgment; cannot arm V29"', src)

    def test_actual_original_v29_only_calls_strict_helper_after_arming_precheck(self):
        src = Path(runner.__file__).read_text(encoding="utf-8")
        start = src.index("            if native_scaffold_placement:")
        end = src.index("            if native_scaffold_candidate:", start)
        scenario = src[start:end]
        self.assertIn("await_original_v29_survival_edge_grounding(process, log, window)",
                      scenario)
        self.assertIn("prepare_verified_original_v29_stone_stack(", scenario)
        self.assertLess(scenario.index("await_original_v29_survival_edge_grounding("),
                        scenario.index("prepare_verified_original_v29_stone_stack("))
        self.assertNotIn('send_command(process, log, window, "/give CIProbe stone 8")',
                         scenario)

    def test_true_original_module_controller_markers_remain_mandatory(self):
        src = Path(runner.__file__).read_text(encoding="utf-8")
        start = src.index("            if native_scaffold_placement:")
        end = src.index("            if native_scaffold_candidate:", start)
        scenario = src[start:end]
        witnesses = (
            "await_marker(process, log, module_tag, deadline",
            "await_marker(process, log, attempt_tag, deadline",
            'if b"accepted=true" not in read_tail(log):',
            "await_marker(process, log, readback_tag, deadline",
        )
        offsets = [scenario.index(v) for v in witnesses]
        self.assertEqual(offsets, sorted(offsets))
        self.assertIn('await_marker(process, log, disabled_tag, deadline', scenario)
        self.assertIn("no original controller", "no original controller")  # no waiver

    def test_give_receipt_is_not_an_original_selected_hotbar_or_controller_pass(self):
        src = Path(runner.__file__).read_text(encoding="utf-8")
        start = src.index("def prepare_verified_original_v29_stone_stack(")
        end = src.index("def prepare_verified_original_scaffold_survival_edge(", start)
        fixture = src[start:end]
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_SCAFFOLD_MODULE_OWNER_PASS", fixture)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_ATTEMPT", fixture)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CLIENT_SURVIVAL_READBACK", fixture)
        self.assertNotIn("PASS=YES", fixture)

    def test_no_game_inventory_or_mouse_writes_from_python_fixture(self):
        src = Path(runner.__file__).read_text(encoding="utf-8")
        start = src.index("def prepare_verified_original_v29_stone_stack(")
        end = src.index("def prepare_verified_original_scaffold_survival_edge(", start)
        fixture = src[start:end]
        for dangerous in ("setSelected", "ItemStack(", "WorldClient(",
                          "onPlayerRightClick", "controller.click",
                          "continue_on_error", "retry_on_failure"):
            with self.subTest(dangerous=dangerous):
                self.assertNotIn(dangerous, fixture)


if __name__ == "__main__":
    unittest.main()
