"""V320: diagnose genuine CITargetB=10 without inventing B attack authority.

Mojang original run 40105144305 attempt 1: A=10 and B=10, A-owned
native click PASS, but no original B-priority source witness. Hurt-time
can indicate actual original client damage state, not its attacker/cause.
"""
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    v320_original_b_damage_source_window as source_window,
)

A = (
    b"CUSTOMMC_189_SIMULTANEOUS_FIRST_A_CLICK_SOURCE_DIAG "
    b"nativeWorldComplete=true aIndex=2 bIndex=1 aDistance=2.0 "
    b"bDistance=2.82 aHurtTime=0 bHurtTime=0 "
    b"aSelected=true bSelected=false\n"
)
B = (
    b"CUSTOMMC_189_SIMULTANEOUS_B_HURT_NATIVE_SOURCE_DIAG "
    b"phase=1 aIndex=2 bIndex=1 aDistance=2.4 bDistance=2.1 "
    b"aHurtTime=8 bHurtTime=9 aSelected=true bSelected=false\n"
)
JAVA = (Path(__file__).parents[1] / "platform-1.8.9" / "src"
        / "main" / "java" / "dev" / "trexzo" / "custommc"
        / "platform" / "v1_8_9"
        / "Minecraft189SimultaneousTargetAcceptance.java")
SCRIPT = Path(__file__).with_name("RunMojang189LocalhostTwoPlayerSmoke.py")


class V320OriginalNativeBSource(unittest.TestCase):
    def test_two_genuine_complete_lines_from_current_original_phase(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(b"source tick\n"+A+B)
            r=source_window(p,len(b"source tick\n"),p.stat().st_size)
            self.assertEqual(r,(A[:-1].decode(),B[:-1].decode()))
            self.assertNotIn("PASS=YES",repr(r))

    def test_no_b_hurt_source_does_not_invent_b_click(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(A)
            self.assertEqual(source_window(p,0,p.stat().st_size),
                             (A[:-1].decode(),))

    def test_actual_old_v308_historical_marker_does_not_synthesize_distances(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(
                b"CUSTOMMC_189_SIMULTANEOUS_NATIVE_UUID_B_PRIORITY_SWITCH_PASS=YES\n")
            self.assertEqual(source_window(p,0,p.stat().st_size),())

    def test_stale_original_a_click_excluded_by_phase_start_cursor(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(A+b"other current source\n"+B)
            start=len(A)
            self.assertEqual(source_window(p,start,p.stat().st_size),
                             (B[:-1].decode(),))

    def test_future_appended_original_hurt_marker_cannot_backfill(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            original=b"real phase A\n"
            p.write_bytes(original+B)
            self.assertEqual(source_window(p,0,len(original)),())
            self.assertEqual(source_window(p,0,p.stat().st_size),
                             (B[:-1].decode(),))

    def test_partial_source_cursor_excludes_partial_first_line(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(A+B)
            start=17
            self.assertEqual(source_window(p,start,p.stat().st_size),
                             (B[:-1].decode(),))

    def test_unfinished_original_jvm_line_is_not_counted(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(A+B[:-1])
            self.assertEqual(source_window(p,0,p.stat().st_size),
                             (A[:-1].decode(),))

    def test_duplicate_original_native_marker_fails_as_diagnostic(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(A+A)
            with self.assertRaisesRegex(AssertionError,"duplicate"):
                source_window(p,0,p.stat().st_size)

    def test_numeric_invalid_or_oversized_and_future_bounds_fail(self):
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original-jvm.log"
            p.write_bytes(A)
            pairs=((-1,1),(9,8),(0,len(A)+1),(0,2_097_153),
                   (False,5),(0,True),(0,5.5),("1",10))
            for start,end in pairs:
                with self.subTest(start=start,end=end):
                    with self.assertRaises(AssertionError):
                        source_window(p,start,end)

    def test_untrusted_symlink_original_source_never_read(self):
        with tempfile.TemporaryDirectory() as root:
            base=Path(root); p=base/"original.log";p.write_bytes(A)
            alias=base/"alias.log"
            try:
                alias.symlink_to(p)
            except (OSError, NotImplementedError):
                self.skipTest("symlink creation unavailable")
            with self.assertRaisesRegex(AssertionError,"unavailable"):
                source_window(alias,0,len(A))

    def test_original_jvm_b_hurt_is_observed_before_priority_not_authorized(self):
        s=JAVA.read_text("utf-8")
        self.assertIn("CUSTOMMC_189_SIMULTANEOUS_B_HURT_NATIVE_SOURCE_DIAG",s)
        self.assertIn("phase < 2 && evidence != null",s)
        self.assertIn("evidence.combat.hurtTime(evidence.bIndex) > 0",s)
        self.assertIn("bHurtSourceReported = true",s)
        self.assertIn("CUSTOMMC_189_SIMULTANEOUS_FIRST_A_CLICK_SOURCE_DIAG",s)
        self.assertIn("source.combat.hurtTime(source.bIndex)",s)
        self.assertIn("CUSTOMMC_189_SIMULTANEOUS_UUID_A_OWNED_ATTACK_PASS=YES",s)
        self.assertIn("CUSTOMMC_189_SIMULTANEOUS_B_PRIORITY_NATIVE_SOURCE_DIAG",s)
        self.assertIn("switched == null || !switched.bNearest",s)
        self.assertNotIn("CUSTOMMC_189_SIMULTANEOUS_B_HURT_NATIVE_SOURCE_DIAG_PASS",s)

    def test_real_b_zero_server_gate_bare_rethrow_still_mandatory(self):
        s=SCRIPT.read_text("utf-8")
        start=s.index("        if args.simultaneous:")
        end=s.index("        if args.multi_target:",start)
        section=s[start:end]
        a=section.index("scoreboard players test CITargetB taken 0 0",
                        section.index("SIM_HIT_A"))
        b=section.index("v320_original_b_damage_source_window(")
        c=section.index("v311_original_b_failure_evidence(")
        d=section.index("send_server_command(server, server_log, b_near)")
        self.assertLess(a,b)
        self.assertLess(b,c)
        self.assertLess(c,d)
        self.assertIn("raise\n",section)
        self.assertIn("expected=b\"score 0 is in range 0 to 0\"",section)
        self.assertIn("CUSTOMMC_189_SIMULTANEOUS_A_DAMAGED_B_STILL_ZERO_PASS",section)

    def test_original_hurt_telemetry_is_no_substitute_for_true_pvp_causality(self):
        # The real native combat snapshot knows hurtTime but not whether
        # damage was PvP, wall/environment or which attacker caused it.
        # Only independent server B=0 remains the negative acceptance.
        s=SCRIPT.read_text("utf-8")
        section=s[s.index("def v320_original_b_damage_source_window("):
                  s.index("def v311_original_b_failure_evidence(")]
        self.assertNotIn("PASS=YES",section)
        self.assertNotIn("send_server_command(",section)
        self.assertNotIn("attackEntity",section)
        self.assertIn("return tuple(found)",section)


if __name__=="__main__":
    unittest.main()
