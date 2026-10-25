"""V320: source-grounded one-shot GUI recovery for genuine Scaffold V30.

Run 40105144305 attempt 3: original host showed repeatedly
  screen=OPEN source=missing_player_world_cursor_or_selected_item,
  enableIssued=false moduleActivation=0.
The original positive-candidate-only Escape predicate was a deadlock.
Never fabricate a candidate, CLEAR screen, placement, controller or PASS.
"""
import ast
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    v320_original_enabled_scaffold_escape_evidence as decision,
)

SOURCE = Path(__file__).with_name("RunMojang189LocalhostTwoPlayerSmoke.py")
ARM = b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAIT "
OLD = ARM + b"screen=OPEN source=missing_player_world_cursor_or_selected_item\n"
POS = ARM + b"screen=OPEN source=positive_candidate_already_available\n"
CLEAR = ARM + b"screen=CLEAR source=positive_candidate_already_available\n"


class V320ActualScaffoldGuiRecovery(unittest.TestCase):
    def test_exact_real_failed_native_open_source_two_observations(self):
        self.assertTrue(decision(OLD + OLD))
        self.assertTrue(decision(
            b"original live host startup\n" + OLD + b"unrelated\n" + OLD))

    def test_single_missing_candidate_open_does_not_invoke_escape(self):
        self.assertFalse(decision(OLD))
        self.assertFalse(decision(OLD[:-1]))
        self.assertFalse(decision(b"unrelated logs\n" + OLD[:-1]))

    def test_previous_original_positive_candidate_still_may_close_gui(self):
        self.assertTrue(decision(POS))
        self.assertTrue(decision(OLD + POS))

    def test_newer_native_clear_screen_overrides_old_open(self):
        self.assertFalse(decision(OLD + OLD + CLEAR))
        self.assertFalse(decision(POS + CLEAR))
        self.assertFalse(decision(CLEAR + CLEAR))

    def test_open_observations_need_complete_literal_java_lines(self):
        truncated = OLD + OLD[:-1]
        self.assertFalse(decision(truncated))
        self.assertFalse(decision(ARM + b"screen=OPEN source=missing"))
        self.assertFalse(decision(
            b"fake-prefix " + OLD + b"fake-prefix " + OLD))
        self.assertFalse(decision(
            b"CUSTOMMC_OFFICIAL_189_SCAFFOLD_ARM_WAITCH screen=OPEN source=x\n" * 2))

    def test_never_send_second_escape_even_with_later_original_open(self):
        self.assertFalse(decision(OLD + OLD, escape_already_sent=True))
        self.assertFalse(decision(POS, escape_already_sent=True))

    def test_source_diagnostic_must_have_genuine_open_screen(self):
        x = ARM + b"screen=CLEAR source=missing_player_world_cursor_or_selected_item\n"
        self.assertFalse(decision(x + x))
        self.assertFalse(decision(OLD + x))
        self.assertTrue(decision(x + OLD + OLD))

    def test_invalid_evidence_cannot_trigger_recovery(self):
        for data, sent in ((None, False), ("screen=OPEN", False),
                           (b"", 1), (b"", None), (b"", "False")):
            with self.subTest(data=data, sent=sent):
                with self.assertRaises(ValueError):
                    decision(data, sent)

    def test_real_host_log_phase_cursor_and_bounded_read_required(self):
        s = SOURCE.read_text("utf-8")
        block = s[s.index('elif args.scaffold_placement == "enabled":'):
                  s.index('elif args.scaffold_placement == "menu-open":',
                          s.index('elif args.scaffold_placement == "enabled":'))]
        self.assertIn("enabled_gui_source_start = host_log.stat().st_size", block)
        self.assertIn("native_reader.seek(enabled_gui_source_start)", block)
        self.assertIn("native_reader.read(131072)", block)
        self.assertIn("v320_original_enabled_scaffold_escape_evidence(", block)
        self.assertIn("dismissed_enabled_gui = True", block)
        self.assertIn('send_real_host_key(host, host_window, "Escape")', block)
        self.assertNotIn("forceEnable", block)
        self.assertLess(block.index("if module in host_data:"),
                        block.index("send_real_host_key(host, host_window,"))
        self.assertLess(block.index("v320_original_enabled_scaffold_escape_evidence("),
                        block.index("send_real_host_key(host, host_window,"))

    def test_original_controller_and_server_stone_and_seven_remain_mandatory(self):
        s = SOURCE.read_text("utf-8")
        block = s[s.index('elif args.scaffold_placement == "enabled":'):
                  s.index('elif args.scaffold_placement == "menu-open":',
                          s.index('elif args.scaffold_placement == "enabled":'))]
        items = [
            'wait_for(host,host_log,module,deadline,',
            'wait_for(host,host_log,attempt,deadline,',
            'wait_for(host,host_log,receipt,deadline,',
            '"testforblock {} {} {} stone"',
            '"clear CIHost stone 0 0"',
            'b"has 7 items that match the criteria"',
            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_DEDICATED_SERVER_STONE_AND_SEVEN_ITEMS_PASS",
        ]
        for needle in items:
            self.assertIn(needle, block)
        self.assertEqual([block.index(x) for x in items],
                         sorted(block.index(x) for x in items))

    def test_helper_does_not_write_a_native_player_or_synthesize_a_pass(self):
        s = SOURCE.read_text("utf-8")
        block = s[s.index("def v320_original_enabled_scaffold_escape_evidence("):
                  s.index("def v318_original_negative_native_source(")]
        for forbidden in ("PASS=YES", "send_real_host_key(",
                          "send_server_command(", "targetRotationState",
                          "host.featureCatalog", "onPlayerRightClick"):
            self.assertNotIn(forbidden, block)
        ast.parse(s)

    def test_legacy_actual_fov_and_wall_negative_pass_gates_unchanged(self):
        s = SOURCE.read_text("utf-8")
        self.assertIn('"scoreboard players test CITarget taken 0 0"', s)
        self.assertIn('wait_for(host, host_log, WALL_LIFECYCLE_SELECTED, deadline,', s)
        self.assertIn('wait_for(host, host_log, WALL_LIFECYCLE_VETO, deadline,', s)


if __name__ == "__main__":
    unittest.main()
