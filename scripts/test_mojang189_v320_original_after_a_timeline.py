"""V320: original JVM post-A source trace on genuine premature B damage.

A vanilla scoreboard damageTaken=10 is authority for B damage, NOT proof
of who/what inflicted it. This suite never turns source observations into
attack admission or server-owned damage PASS.
"""
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    v320_original_after_a_b_timeline as inspect_original,
)

RUNNER = Path(__file__).with_name("RunMojang189LocalhostTwoPlayerSmoke.py")
JAVA = (Path(__file__).parents[1] / "platform-1.8.9" / "src" / "main"
        / "java" / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"
        / "Minecraft189SimultaneousTargetAcceptance.java")

PREFIX = b"CUSTOMMC_189_SIMULTANEOUS_POST_A_ORIGINAL_TICK_SOURCE_DIAG "


def sample(tick=5, eligible=True, a=2.235, b=2.828):
    return (
        PREFIX
        + f"phase=1 ticksAfterA={tick} "
          f"bothEligible={str(eligible).lower()} "
          f"Aindex={1 if eligible else -1} "
          f"Bindex={2 if eligible else -1} "
          f"A3D={a if eligible else 'NaN'} "
          f"B3D={b if eligible else 'NaN'} "
          f"sourceANearest={str(eligible).lower()} sourceBNearest=false "
          "sameNativeReadReason=original-source-A-priority\n"
          .encode("ascii")
    )


class V320OriginalAfterASource(unittest.TestCase):
    def audit(self, raw, start=0, end=None):
        with tempfile.TemporaryDirectory() as root:
            p = Path(root) / "genuine-native-stdout.log"
            p.write_bytes(raw)
            return inspect_original(p, start, len(raw) if end is None else end)

    def test_real_original_a_then_b_server_error_with_no_tick_sample(self):
        raw = (b"CUSTOMMC_189_SIMULTANEOUS_UUID_A_OWNED_ATTACK_PASS=YES\n"
               b"real server later B score 10\n")
        result = self.audit(raw)
        self.assertIn("count=0", result)
        self.assertNotIn("PASS=YES", result)

    def test_first_original_five_tick_source_prints_genuine_distances(self):
        result = self.audit(b"native-A-owned\n" + sample())
        self.assertIn("count=1", result)
        self.assertIn("A3D=2.235", result)
        self.assertIn("B3D=2.828", result)
        self.assertIn("sourceBNearest=false", result)
        self.assertNotIn("PASS=YES", result)

    def test_three_consecutive_same_native_read_snapshots(self):
        raw = sample(5) + sample(10, a=2.400, b=2.120) + sample(15)
        result = self.audit(raw)
        self.assertIn("count=3", result)
        self.assertLess(result.index("ticksAfterA=5 "),
                        result.index("ticksAfterA=10 "))
        self.assertLess(result.index("ticksAfterA=10 "),
                        result.index("ticksAfterA=15 "))

    def test_finite_original_a_cannot_be_assumed_when_native_read_missing(self):
        result = self.audit(sample(5, eligible=False))
        self.assertIn("bothEligible=false", result)
        self.assertIn("Aindex=-1 Bindex=-1", result)
        self.assertIn("A3D=NaN B3D=NaN", result)
        self.assertNotIn("PASS=YES", result)

    def test_samples_after_failed_b_score_cursor_are_never_backfilled(self):
        early = b"native source before B failure\n"
        raw = early + sample(5)
        self.assertIn("count=0", self.audit(raw, end=len(early)))
        self.assertIn("count=1", self.audit(raw))

    def test_native_marker_before_stage_cursor_is_not_current_run(self):
        stale = sample(5)
        new = b"start new authentic server stage\n"
        self.assertIn("count=0", self.audit(stale + new, start=len(stale)))

    def test_truncated_last_native_stdout_line_never_counts(self):
        raw = b"header\n" + sample(5).rstrip(b"\n")
        self.assertIn("count=0", self.audit(raw))

    def test_midline_start_cursor_discards_partial_source(self):
        prefix = sample(5)
        raw = prefix + sample(10)
        # Only the complete second line remains, so do not accidentally
        # certify the full timeline from this incomplete cursor.
        with self.assertRaisesRegex(AssertionError, "tick"):
            self.audit(raw, start=20)

    def test_fake_prefixed_marker_cannot_supply_a_native_read(self):
        result = self.audit(b"spoof-prefix " + sample())
        self.assertIn("count=0", result)

    def test_source_phase_and_tick_cadence_cannot_be_forged(self):
        for forged in (
            sample(4),
            sample(105),
            sample(5).replace(b"phase=1", b"phase=2"),
            sample(5).replace(b"bothEligible=true", b"bothEligible=maybe"),
            sample(5).replace(b"sourceBNearest=false",
                              b"sourceBNearest=unknown"),
        ):
            with self.subTest(raw=forged):
                with self.assertRaisesRegex(AssertionError, "tick"):
                    self.audit(forged)

    def test_original_log_rejects_invalid_and_future_byte_cursors(self):
        data = sample(5)
        for start, end in ((-1, 0), (4, 3), (0, len(data) + 1),
                           (0, 2097153), (False, 6), ("0", 5),
                           (0, True), (0, 1.3)):
            with self.subTest(start=start, end=end):
                with self.assertRaises(AssertionError):
                    self.audit(data, start, end)

    def test_unbounded_fake_source_line_count_fails_closed(self):
        raw = b"".join(sample(n) for n in range(5, 110, 5))
        with self.assertRaisesRegex(AssertionError, "count invalid"):
            self.audit(raw)

    def test_original_a_click_precedes_native_tick_samples(self):
        java = JAVA.read_text("utf-8")
        self.assertIn("v320OriginalTicksAfterA", java)
        self.assertIn("phase == 1 && firstOwnedClick", java)
        self.assertIn("++v320OriginalTicksAfterA % 5 == 0", java)
        self.assertIn("final Evidence evidence = read(host, v320NativeReason)",
                      java)
        self.assertIn("CUSTOMMC_189_SIMULTANEOUS_POST_A_ORIGINAL_TICK_SOURCE_DIAG",
                      java)
        self.assertIn("reportV310OriginalSource(\"observe\", evidence)", java)
        self.assertIn("reportV310OriginalSource(\"original-click\", switched)", java)
        self.assertIn("simultaneous early B click without both-eligible", java)

    def test_failed_original_b_score_remains_a_hard_native_gate(self):
        runner = RUNNER.read_text("utf-8")
        start = runner.index("        if args.simultaneous:\n")
        end = runner.index("        if args.multi_target:\n", start)
        body = runner[start:end]
        self.assertIn('"scoreboard players test CITargetB taken 0 0"', body)
        self.assertIn("v317_bad_b_end = host_log.stat().st_size", body)
        self.assertIn("v320_original_after_a_b_timeline(", body)
        self.assertIn("v311_original_b_failure_evidence(", body)
        self.assertLess(body.index("v320_original_after_a_b_timeline("),
                        body.index("v311_original_b_failure_evidence("))
        self.assertIn("                raise\n", body)
        self.assertIn("send_server_command(server, server_log, b_near)", body)
        self.assertNotIn("return True", body[body.index(
            '"scoreboard players test CITargetB taken 0 0"'):
            body.index("send_server_command(server, server_log, b_near)")])

    def test_native_source_only_never_changes_gameplay_or_supplies_pass(self):
        java = JAVA.read_text("utf-8")
        start = java.index("final boolean v320Due")
        end = java.index("if (phase == 0)", start)
        diagnostic = java[start:end]
        for forbidden in ("PASS=YES", "attackEntity(", "teleport(",
                          "setRotation(", "setTarget(", "enableKillAura"):
            self.assertNotIn(forbidden, diagnostic)
        self.assertIn("System.out.println(", diagnostic)


if __name__ == "__main__":
    unittest.main()
