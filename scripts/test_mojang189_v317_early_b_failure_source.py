"""V317: the original B=0 failure now retains pre-teleport native source.

This is read-only diagnostic coverage, not a shortcut past Minecraft's
original scoreboard, UUID, camera, ray or attack-owner checks.
"""
import ast
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    v316_preteleport_original_b_priority_marker as read_original_timeline,
)

SOURCE = Path(__file__).with_name("RunMojang189LocalhostTwoPlayerSmoke.py")


def strict_original_gate():
    source = SOURCE.read_text(encoding="utf-8")
    tree = ast.parse(source)
    # Find the genuine 3-client simultaneous acceptance branch, not
    # another historical offline source or a generated PASS fixture.
    candidates = [node for node in ast.walk(tree)
                  if isinstance(node, ast.Try) and any(
                      isinstance(statement, ast.Expr)
                      and isinstance(statement.value, ast.Call)
                      and any(isinstance(sub, ast.Constant)
                              and sub.value == "scoreboard players test CITargetB taken 0 0"
                              for sub in ast.walk(statement))
                      for statement in node.body)]
    if len(candidates) != 1:
        raise AssertionError("not exactly one original native B=0 server gate")
    return source, candidates[0]


class V317GenuineEarlyBFailureSourceTests(unittest.TestCase):
    def test_exact_strict_b_score_negative_remains_before_diagnostic(self):
        source, gate = strict_original_gate()
        before = ast.get_source_segment(source, gate.body[0])
        self.assertIn("send_server_command(server, server_log", before)
        self.assertIn("scoreboard players test CITargetB taken 0 0", before)
        self.assertIn('expected=b"score 0 is in range 0 to 0"', before)

    def test_original_b_zero_failure_is_still_an_assertion_not_pass(self):
        source, gate = strict_original_gate()
        self.assertEqual(len(gate.handlers), 1)
        handler = gate.handlers[0]
        self.assertIsInstance(handler.type, ast.Name)
        self.assertEqual(handler.type.id, "AssertionError")
        self.assertTrue(any(isinstance(n, ast.Raise) and n.exc is None
                            for n in ast.walk(handler)))
        self.assertFalse(any(isinstance(n, ast.Return)
                             for n in ast.walk(handler)))

    def test_early_b_failure_captures_actual_host_log_cursor(self):
        source, gate = strict_original_gate()
        body = ast.get_source_segment(source, gate.handlers[0])
        self.assertIn("v317_bad_b_end = host_log.stat().st_size", body)
        self.assertIn("v316_preteleport_original_b_priority_marker(", body)
        self.assertIn("host_log, v311_original_b_source_start,", body)
        self.assertIn("v317_bad_b_end)", body)
        self.assertIn("CUSTOMMC_189_SIMULTANEOUS_EARLY_B_DAMAGE_SOURCE_DIAG", body)

    def test_negative_diagnostic_read_failure_cannot_mask_original_failure(self):
        source, gate = strict_original_gate()
        handler = gate.handlers[0]
        inner_tries = [n for n in ast.walk(handler)
                       if isinstance(n, ast.Try)]
        # V320 adds a SECOND read-only diagnostic in this same original
        # B=0 rejection branch. Both exception handlers must remain bounded
        # and MUST NOT turn the server scoreboard failure into a success.
        self.assertEqual(len(inner_tries), 2)
        for diagnostic in inner_tries:
            self.assertEqual(len(diagnostic.handlers), 1)
            self.assertEqual(
                ast.get_source_segment(
                    source, diagnostic.handlers[0].type),
                "(AssertionError, OSError, UnicodeError)")
        self.assertTrue(any(
            isinstance(node, ast.Raise) and node.exc is None
            for node in handler.body))
        block = ast.get_source_segment(source, handler)
        self.assertIn("EARLY_B_SOURCE_UNAVAILABLE_DIAG", block)
        self.assertIn("type(error).__name__", block)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_SIMULTANEOUS_BOTH_SERVER_DAMAGE_PASS", block)

    def test_previous_native_negative_diagnostic_still_retained(self):
        source, gate = strict_original_gate()
        lines = ast.get_source_segment(source, gate.handlers[0])
        first = lines.index("v316_preteleport_original_b_priority_marker")
        previous = lines.index("v311_original_b_failure_evidence")
        self.assertLess(first, previous)
        self.assertTrue(lines.rstrip().endswith("raise"))

    def test_genuine_v308_old_switch_log_without_same_read_is_absent(self):
        # A real old V308 log had the switch marker but was BEFORE V310
        # introduced the source distance log; NEVER invent a new metric.
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "host.log"
            payload = (
                b"CUSTOMMC_189_SIMULTANEOUS_UUID_A_OWNED_ATTACK_PASS=YES\n"
                b"CUSTOMMC_189_SIMULTANEOUS_NATIVE_UUID_B_PRIORITY_SWITCH_PASS=YES\n"
                b"CUSTOMMC_189_SIMULTANEOUS_UUID_B_OWNED_ATTACK_PASS=YES\n"
            )
            log.write_bytes(payload)
            result = read_original_timeline(log, 0, len(payload))
            self.assertIn("completeMarker=ABSENT", result)
            self.assertNotIn("aDistance=", result)
            self.assertNotIn("PASS=YES", result)

    def test_real_early_original_distance_if_present_only_describes_source(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "host.log"
            source = (
                b"CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG "
                b"path=original-click aIndex=1 bIndex=2 "
                b"aDistance=2.402 bDistance=1.980 gapBCloser=0.422\n")
            log.write_bytes(source)
            observed = read_original_timeline(log, 0, len(source))
            self.assertIn("completeMarker=PRESENT", observed)
            self.assertIn("aDistance=2.402", observed)
            self.assertNotIn("SERVER_DAMAGE_PASS", observed)
            self.assertNotIn("PASS=YES", observed)

    def test_original_native_b_teleport_is_after_both_gate_paths(self):
        source = SOURCE.read_text("utf-8")
        section = source[source.index("        if args.simultaneous:\n"):
                         source.index("        if args.multi_target:\n",
                                      source.index("        if args.simultaneous:\n"))]
        self.assertLess(
            section.index('"scoreboard players test CITargetB taken 0 0"'),
            section.index("EARLY_B_DAMAGE_SOURCE_DIAG"))
        self.assertLess(section.index("EARLY_B_DAMAGE_SOURCE_DIAG"),
                        section.index("CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS"))
        self.assertLess(
            section.index("CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS"),
            section.index("send_server_command(server, server_log, b_near)"))


if __name__ == "__main__":
    unittest.main()
