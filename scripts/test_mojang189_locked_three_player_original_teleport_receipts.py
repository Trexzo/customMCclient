"""V292 source-only unit regressions for genuine 1.8.9 server tp receipts.

Never a substitute for a real Minecraft GUI, three genuine clients,
World convergence, 20 native eligible A callbacks, or server PvP damage.
"""
import ast
from pathlib import Path
import unittest

import RunMojang189LocalhostTwoPlayerSmoke as smoke


class LockedNativeOriginalTeleportReceiptTests(unittest.TestCase):
    def test_exact_original_three_player_server_reply(self):
        self.assertEqual(
            smoke.original_locked_tp_server_reply(
                "tp CIHost 399 11 -340 0 0"),
            b"Teleported CIHost to 399.5, 11.0, -339.5")
        self.assertEqual(
            smoke.original_locked_tp_server_reply(
                "tp CITarget 399 11 -338 180 0"),
            b"Teleported CITarget to 399.5, 11.0, -337.5")
        self.assertEqual(
            smoke.original_locked_tp_server_reply(
                "tp CITargetB 397 11 -338 180 0"),
            b"Teleported CITargetB to 397.5, 11.0, -337.5")

    def test_negative_grid_cell_uses_vanilla_half_block_center(self):
        self.assertEqual(
            smoke.original_locked_tp_server_reply(
                "tp CIHost -1 70 -16 0 0"),
            b"Teleported CIHost to -0.5, 70.0, -15.5")

    def test_wrong_user_and_nonlocked_client_never_pass(self):
        for command in (
            "tp Stranger 399 11 -340 0 0",
            "tp CIHost2 399 11 -340 0 0",
            "tp CIHost 399 11 -340",
            "say Teleported CIHost to 399.5, 11.0, -339.5",
        ):
            with self.subTest(command=command):
                with self.assertRaises(AssertionError):
                    smoke.original_locked_tp_server_reply(command)

    def test_stale_or_partial_world_coordinates_never_match_fresh_receipt(self):
        expected = smoke.original_locked_tp_server_reply(
            "tp CIHost 399 11 -340 0 0")
        self.assertNotEqual(
            smoke.original_locked_tp_server_reply(
                "tp CIHost 399 11 -339 0 0"), expected)
        self.assertNotEqual(
            smoke.original_locked_tp_server_reply(
                "tp CITarget 399 11 -340 0 0"), expected)
        self.assertNotEqual(expected, b"Teleported CIHost")

    def test_nonintegral_or_offworld_source_is_strictly_rejected(self):
        for command in (
            "tp CIHost 399.25 11 -340 0 0",
            "tp CIHost X 11 -340 0 0",
            "tp CIHost 399 0 -340 0 0",
            "tp CIHost 399 256 -340 0 0",
            "tp CIHost 29999991 11 -340 0 0",
            "tp CIHost 399 11 -340 0 100",
        ):
            with self.subTest(command=command):
                with self.assertRaises(AssertionError):
                    smoke.original_locked_tp_server_reply(command)

    def test_locked_original_fixture_uses_three_acknowledged_real_commands(self):
        runner = Path(smoke.__file__).read_text(encoding="utf-8")
        tree = ast.parse(runner)
        matches = [
            node for node in ast.walk(tree) if isinstance(node, ast.If)
            and isinstance(node.test, ast.Attribute)
            and node.test.attr == "locked_target"
            and isinstance(node.test.value, ast.Name)
            and node.test.value.id == "args"
            and "for original_tp in (b_outer, host_tp, a_near):"
                in (ast.get_source_segment(runner, node) or "")
        ]
        self.assertEqual(1, len(matches),
                         "select the actual original three-client A/B "
                         "fixture rather than unrelated locked-target checks")
        native = ast.get_source_segment(runner, matches[0])
        self.assertIn("for original_tp in (b_outer, host_tp, a_near):",
                      native)
        self.assertIn("expected=original_locked_tp_server_reply(original_tp)",
                      native)
        self.assertNotIn("send_server_command(server, server_log, b_outer)",
                         native)
        self.assertNotIn("send_server_command(server, server_log, host_tp)",
                         native)
        self.assertNotIn("send_server_command(server, server_log, a_near)",
                         native)

    def test_original_twenty_then_ten_then_fifteen_then_real_damage_remain(self):
        native = Path(smoke.__file__).read_text(encoding="utf-8")
        start = native.index("        if args.locked_target:")
        end = native.index("        if args.simultaneous:", start)
        block = native[start:end]
        checkpoints = (
            "wait_for(host, host_log, LOCK_READY, deadline,",
            "wait_for(host, host_log, LOCK_ARMED, deadline,",
            "wait_for(host, host_log, LOCK_RETAINED, deadline,",
            "wait_for(host, host_log, LOCK_HIT_A, deadline,",
            'wait_for_real_score(server, server_log, "CITarget", deadline)',
            "wait_for(host, host_log, LOCK_RELEASE, deadline,",
            "wait_for(host, host_log, LOCK_HIT_B, deadline,",
        )
        offsets = [block.index(token) for token in checkpoints]
        self.assertEqual(sorted(offsets), offsets)
        self.assertNotIn("continue_on_error", block)

    def test_acknowledgment_is_not_an_original_mojang_uuid_pass_marker(self):
        witness = smoke.original_locked_tp_server_reply(
            "tp CIHost 399 11 -340 0 0")
        self.assertNotIn(b"PASS=YES", witness)
        self.assertNotIn(b"LOCK_TWO_ELIGIBLE_INITIAL_A_PASS", witness)


if __name__ == "__main__":
    unittest.main()
