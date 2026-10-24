"""V311: genuine original-client B source witness, strictly after server proof."""
import tempfile
from pathlib import Path
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    v311_original_b_priority_source,
    v311_original_b_failure_evidence,
)


PREFIX = b"CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG "


def original(path=b"observe", a=b"3.25", b=b"2.125", gap=b"1.125",
             ai=b"3", bi=b"7"):
    return (PREFIX + b"path=" + path + b" aIndex=" + ai
            + b" bIndex=" + bi + b" aDistance=" + a
            + b" bDistance=" + b + b" gapBCloser=" + gap + b"\n")


class OriginalNativeBSourceAuditTests(unittest.TestCase):
    def evaluate(self, content, offset=0):
        with tempfile.TemporaryDirectory() as root:
            logfile = Path(root) / "original-host.log"
            logfile.write_bytes(content)
            return v311_original_b_priority_source(logfile, offset)

    def rejects(self, content, offset=0):
        with self.assertRaises(AssertionError):
            self.evaluate(content, offset)

    def test_exact_original_observe_source_is_retained(self):
        marker = original()
        self.assertEqual(self.evaluate(b"unrelated native log\n" + marker),
                         marker.decode("ascii").strip())

    def test_native_click_transition_keeps_distinct_source_label(self):
        marker = original(path=b"original-click")
        self.assertEqual(self.evaluate(marker), marker.decode("ascii").strip())

    def test_cursor_must_exclude_all_stale_prior_source(self):
        previous = original(a=b"3.2", b=b"2.1", gap=b"1.1")
        fresh = original(path=b"original-click")
        self.assertEqual(self.evaluate(previous + b"other\n" + fresh,
                                       len(previous) + len(b"other\n")),
                         fresh.decode("ascii").strip())

    def test_no_fresh_original_world_read_fails_closed(self):
        self.rejects(b"nothing from source\n")
        self.rejects(original(), len(original()))
        self.rejects(original(), len(original()) + 1)

    def test_duplicate_original_switch_witnesses_are_not_silently_chosen(self):
        self.rejects(original() + original(path=b"original-click"))

    def test_prefix_spoof_is_not_accepted_as_original_stdout(self):
        self.rejects(b"[fake] " + original())
        self.rejects(b"  " + original())

    def test_negative_or_zero_native_b_priority_is_not_real_switch(self):
        self.rejects(original(a=b"2.0", b=b"3.0", gap=b"-1.0"))
        self.rejects(original(a=b"2.0", b=b"2.0", gap=b"0.0"))

    def test_native_margin_must_exceed_point_twelve(self):
        self.rejects(original(a=b"2.12", b=b"2.0", gap=b"0.12"))
        self.rejects(original(a=b"2.119", b=b"2.0", gap=b"0.119"))

    def test_original_source_must_be_inside_actual_attack_reach(self):
        self.rejects(original(a=b"3.50001", b=b"2", gap=b"1.50001"))
        self.rejects(original(a=b"2", b=b"0.5", gap=b"1.5"))

    def test_same_or_invalid_native_player_index_rejected(self):
        self.rejects(original(ai=b"7", bi=b"7"))
        self.rejects(original(ai=b"-1"))

    def test_nan_infinity_and_malformed_native_floats_rejected(self):
        self.rejects(original(a=b"NaN"))
        self.rejects(original(gap=b"Infinity"))
        self.rejects(original(b=b"not-a-number"))

    def test_gap_must_match_same_native_distance_arithmetic(self):
        self.rejects(original(gap=b"1.125001"))

    def test_no_fake_pass_marker_synthesized(self):
        source = self.evaluate(original())
        self.assertTrue(source.endswith("gapBCloser=1.125"))
        self.assertNotIn("PASS=YES", source)
        self.assertNotIn("SERVER_DAMAGE", source)

    def test_actual_original_early_b_source_surfaces_at_hard_server_failure(self):
        # This helper is used only inside an except: server B=0 still raises.
        with tempfile.TemporaryDirectory() as root:
            logfile = Path(root) / "original-host.log"
            source = original(a=b"3.1", b=b"2.3", gap=b"0.8")
            logfile.write_bytes(b"original A owned click\n" + source)
            seen = v311_original_b_failure_evidence(logfile, 0)
            self.assertEqual(seen, source.decode("ascii").strip())
            self.assertNotIn("PASS=YES", seen)

    def test_no_original_source_never_converts_early_server_failure_to_pass(self):
        with tempfile.TemporaryDirectory() as root:
            logfile = Path(root) / "original-host.log"
            logfile.write_bytes(b"original stdout has no priority record\n")
            seen = v311_original_b_failure_evidence(logfile, 0)
            self.assertIn("_SOURCE_UNAVAILABLE_DIAG", seen)
            self.assertNotIn("PASS=YES", seen)

    def test_invalid_source_in_early_failure_is_reported_without_authority(self):
        with tempfile.TemporaryDirectory() as root:
            logfile = Path(root) / "original-host.log"
            logfile.write_bytes(original(gap=b"12345"))
            seen = v311_original_b_failure_evidence(logfile, 0)
            self.assertIn("source", seen.lower())
            self.assertIn("_SOURCE_UNAVAILABLE_DIAG", seen)
            self.assertNotIn("_PASS", seen)

    def test_invalid_offset_type_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            logfile = Path(root) / "original-host.log"
            logfile.write_bytes(original())
            with self.assertRaises(AssertionError):
                v311_original_b_priority_source(logfile, "0")


if __name__ == "__main__":
    unittest.main()
