"""V316: bounded REAL original native log before deliberate server B teleport.

Diagnostics never confer attack authorization, B zero proof or native PASS.
"""
import ast
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    v316_preteleport_original_b_priority_marker as snapshot,
)


MARKER = (
    b"CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG "
    b"path=observe aIndex=1 bIndex=2 "
    b"aDistance=2.222 bDistance=1.980 gapBCloser=0.242\n"
)


class V316OriginalBPreteleportTimeline(unittest.TestCase):
    def test_no_original_native_b_marker_before_tp_is_absent_not_pass(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "original-host.log"
            payload = b"actual vanilla runTick\nCUSTOMMC_OTHER_SOURCE=YES\n"
            log.write_bytes(payload)
            result = snapshot(log, 0, len(payload))
            self.assertIn("completeMarker=ABSENT", result)
            self.assertNotIn("PASS=YES", result)

    def test_one_exact_original_marker_before_tp_is_diagnostic_only(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            payload = b"native callback\n" + MARKER
            log.write_bytes(payload)
            result = snapshot(log, 0, len(payload))
            self.assertIn("completeMarker=PRESENT", result)
            self.assertIn("path=observe aIndex=1 bIndex=2", result)
            self.assertIn("gapBCloser=0.242", result)
            self.assertNotIn("PASS=YES", result)

    def test_later_after_tp_original_source_cannot_backfill_before_tp(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            before = b"original A damage; B still score zero\n"
            log.write_bytes(before + MARKER)
            self.assertIn("completeMarker=ABSENT",
                          snapshot(log, 0, len(before)))
            self.assertIn("completeMarker=PRESENT",
                          snapshot(log, 0, len(before + MARKER)))

    def test_stale_original_marker_before_scenario_cursor_is_excluded(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            stale = MARKER
            newer = b"next original source phase not yet prioritized\n"
            log.write_bytes(stale + newer)
            self.assertIn("completeMarker=ABSENT",
                          snapshot(log, len(stale), len(stale + newer)))

    def test_incomplete_original_java_stdout_line_has_no_authority(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            unfinished = MARKER.rstrip(b"\n")
            log.write_bytes(b"full original callback\n" + unfinished)
            result = snapshot(log, 0, log.stat().st_size)
            self.assertIn("completeMarker=ABSENT", result)

    def test_duplicate_original_native_priority_markers_are_invalid(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            log.write_bytes(MARKER + MARKER)
            with self.assertRaisesRegex(AssertionError, "duplicate"):
                snapshot(log, 0, log.stat().st_size)

    def test_prefixed_original_source_cannot_impersonate_exact_marker(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            log.write_bytes(b"spoof-prefix " + MARKER)
            with self.assertRaisesRegex(AssertionError, "malformed"):
                snapshot(log, 0, log.stat().st_size)

    def test_malformed_real_b_priority_values_fail_closed(self):
        for marker in (
            MARKER.replace(b"gapBCloser=0.242", b"gapBCloser=0.120"),
            MARKER.replace(b"aDistance=2.222", b"aDistance=5.222"),
            MARKER.replace(b"bIndex=2", b"bIndex=1"),
            MARKER.replace(b"path=observe", b"path=not-an-original-path"),
            MARKER.replace(b"bDistance=1.980", b"bDistance=NaN"),
            MARKER.replace(b"gapBCloser=0.242", b"gapBCloser=0.900"),
        ):
            with self.subTest(marker=marker):
                with tempfile.TemporaryDirectory() as d:
                    log = Path(d) / "host.log"
                    log.write_bytes(marker)
                    with self.assertRaises(AssertionError):
                        snapshot(log, 0, log.stat().st_size)

    def test_invalid_or_future_source_cursor_cannot_read_original_log(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            log.write_bytes(MARKER)
            pairs = ((-1, 1), (5, 4), (0, 2_097_153),
                     (0, len(MARKER) + 1), (False, 5),
                     (0, True), ("0", 5), (0, 3.0))
            for start, end in pairs:
                with self.subTest(start=start, end=end):
                    with self.assertRaises(AssertionError):
                        snapshot(log, start, end)

    def test_current_original_marker_must_be_complete_within_cursor(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "host.log"
            log.write_bytes(b"first source\n" + MARKER)
            offset = len(b"first source\n")
            self.assertIn("completeMarker=ABSENT",
                          snapshot(log, offset, len(b"first source\n") + 12))
            self.assertIn("completeMarker=PRESENT",
                          snapshot(log, offset, log.stat().st_size))

    def test_actual_native_gate_still_first_checks_real_b_zero(self):
        code = Path(__file__).with_name(
            "RunMojang189LocalhostTwoPlayerSmoke.py"
        ).read_text("utf-8")
        start = code.index("        if args.simultaneous:\n")
        until = code.index("        if args.multi_target:\n", start)
        body = code[start:until]
        positions = [
            body.index('"scoreboard players test CITargetB taken 0 0"'),
            body.index("CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS"),
            # V317 adds an earlier diagnostic ONLY on genuine B>0 failure.
            # This pre-teleport source read is the positive B=0 path, after
            # the original score PASS; both still occur before the real B /tp.
            body.index("v316_preteleport_original_b_priority_marker(",
                       body.index("CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS")),
            body.index("send_server_command(server, server_log, b_near)"),
            body.index("wait_for(host, host_log, SIM_HIT_B"),
        ]
        self.assertEqual(positions, sorted(positions))
        # Original strict negative exception and native rethrow unchanged.
        self.assertIn("except AssertionError:", body)
        self.assertIn("v311_original_b_failure_evidence(", body)
        tree = ast.parse(code)
        self.assertGreater(len([node for node in ast.walk(tree)
                                if isinstance(node, ast.Raise)
                                and node.exc is None]), 0)

    def test_no_synthetic_pass_or_game_mutation_from_source_slice(self):
        code = Path(__file__).with_name(
            "RunMojang189LocalhostTwoPlayerSmoke.py"
        ).read_text("utf-8")
        start = code.index(
            "def v316_preteleport_original_b_priority_marker(")
        # V320 adds an independent following helper before V311. Scope
        # this assertion to the V316 helper, not subsequent diagnostics.
        end = code.index("def v320_original_b_damage_source_window(", start)
        helper = code[start:end]
        self.assertNotIn("PASS=YES", helper)
        self.assertNotIn("send_server_command(", helper)
        self.assertNotIn("attackEntity", helper)
        self.assertNotIn("override", helper.lower())
        self.assertIn("stream.read(source_end - source_start)", helper)


if __name__ == "__main__":
    unittest.main()
