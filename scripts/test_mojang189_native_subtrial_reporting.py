"""V277 native original subtrial result observability, never a PASS gate.

The only authoritative source is an actual pinned Mojang 1.8.9 workflow job.
These source-level checks cannot replace original 78/78 validation.
"""
from pathlib import Path
import re
import unittest

WORKFLOW = (Path(__file__).resolve().parent.parent
            / ".github" / "workflows" / "minecraft-189-real-client.yml")

CASES = (
    ("AUTOCLICKER_REAL_GAME_TRIAL", "autoclicker"),
    ("M424_REMOTE_PAIR_SCOPE_REAL_PVP_TRIAL", "m424-remote-pair"),
    ("M423_UNLOCKED_SCOPE_REAL_PVP_TRIAL", "m423-unlocked-scope"),
    ("M407_UPSTREAM_REAL_PVP_FULL_TRIAL", "m407-upstream-pvp"),
)


def native_subtrial_report_from_job_log(raw):
    """Parse only *printed* native outcome lines, excluding workflow source.

    Unknown or missing reports must remain UNKNOWN; never infer first try
    from an overall green GitHub Actions job result.
    """
    if not isinstance(raw, str):
        raise TypeError("expected GitHub job log text")
    expected = {label for _marker, label in CASES}
    report = {}
    regex = re.compile(
        r"(?:^|\s)CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT="
        r"([a-z0-9-]+):(first|retried)$")
    for line in raw.splitlines():
        match = regex.search(line.strip())
        if match is None:
            continue
        label, outcome = match.groups()
        if label not in expected:
            raise ValueError("unknown native subtrial report: " + label)
        if label in report:
            raise ValueError("duplicate native subtrial report: " + label)
        report[label] = outcome
    return report


class OriginalMojangNativeSubtrialReportTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = WORKFLOW.read_text(encoding="utf-8")

    def trial_block(self, marker):
        start = self.source.index('echo "' + marker + '=$attempt"')
        finish = self.source.index('test "$accepted" -eq 1', start)
        end = self.source.index('\n      - name:', finish)
        return self.source[start:end]

    def test_four_genuine_trial_loops_still_bounded_and_strict(self):
        for marker, label in CASES:
            with self.subTest(label=label):
                block = self.trial_block(marker)
                marker_at = self.source.index('echo "' + marker + '=$attempt"')
                step_at = self.source.rfind("      - name:", 0, marker_at)
                loop_at = self.source.rfind("for attempt in 1 2; do",
                                            step_at, marker_at)
                self.assertGreater(loop_at, step_at,
                                   "native trial must be in this exact step")
                self.assertIn('test "$accepted" -eq 1', block)
                self.assertIn('accepted=1', block)
                self.assertIn('break', block)

    def test_reviewed_v308_and_v309_native_steps_are_both_required(self):
        # Certifed V307 had 67 named steps. The two new independently
        # verified original-game NEGATIVE stages must remain distinct.
        names = re.findall(r"^      - name: (.*)$", self.source,
                           flags=re.MULTILINE)
        reviewed = [n for n in names if n.startswith(("V308 ", "V309 "))]
        # The S22 visual proof adds two *distinct* genuine Mojang CI
        # steps without changing the original 69 certified stages.
        visual = [n for n in names if n.startswith("S22 ")]
        self.assertEqual([
            "S22 capture original Mojang ClickGUI visual evidence",
            "S22 archive original Minecraft GUI screenshots (even on failure)",
        ], visual)
        original = [n for n in names if not n.startswith("S22 ")]
        self.assertEqual(69, len(original))
        self.assertEqual(67, len(original) - len(reviewed))
        self.assertEqual([
            "V308 original E after first three-mode Scaffold stone permanently revokes live quota",
            "V309 original hotbar 2 to 1 after first Scaffold stone revokes live quota",
        ], reviewed)
        self.assertEqual(1, self.source.count(
            "--scaffold-placement production-repeat-three-menu-after-first"))
        self.assertEqual(1, self.source.count(
            "--scaffold-placement production-repeat-three-hotbar-after-first"))
        self.assertIn("V54 certify production opt-in three original Scaffold", self.source)
        self.assertIn("V59 certify four genuine production Scaffold", self.source)

    def test_each_true_trial_emits_one_of_two_machine_readable_outcomes(self):
        for marker, label in CASES:
            with self.subTest(label=label):
                block=self.trial_block(marker)
                self.assertIn('if [ "$attempt" -eq 1 ]; then',block)
                self.assertIn(
                    'CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT='+label+':first',
                    block)
                self.assertIn(
                    'CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT='+label+':retried',
                    block)
                self.assertLess(block.index('test "$accepted" -eq 1'),
                                block.index('CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT='))
                self.assertIn('::warning::Original Mojang',block)

    def test_success_reports_not_used_as_synthetic_native_game_witness(self):
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_NATIVE_SUBTRIAL_PASS=YES",
                         self.source)
        self.assertEqual(8,self.source.count(
            "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT="))
        self.assertEqual(4,self.source.count('test "$accepted" -eq 1'))

    def test_only_actual_emitted_lines_are_parsed_from_original_log(self):
        log=('2026-10-21T13:12:00Z echo "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=autoclicker:first"\n'
             '2026-10-21T13:12:01Z CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=autoclicker:first\n'
             '2026-10-21T13:12:02Z CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=m424-remote-pair:retried\n')
        self.assertEqual({
            "autoclicker":"first",
            "m424-remote-pair":"retried"
        },native_subtrial_report_from_job_log(log))

    def test_missing_outcomes_never_infer_first_attempt_cleanliness(self):
        self.assertEqual({},native_subtrial_report_from_job_log(
            "workflow successful 78/78"))
        self.assertEqual({"autoclicker":"first"},
                         native_subtrial_report_from_job_log(
                             "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=autoclicker:first"))

    def test_duplicate_or_unknown_results_are_not_trusted(self):
        with self.assertRaisesRegex(ValueError,"duplicate"):
            native_subtrial_report_from_job_log(
                "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=autoclicker:first\n"
                "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=autoclicker:retried")
        with self.assertRaisesRegex(ValueError,"unknown"):
            native_subtrial_report_from_job_log(
                "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=unexpected:first")

    def test_malformed_labels_and_false_pass_never_count(self):
        self.assertEqual({},native_subtrial_report_from_job_log(
            "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=autoclicker:PASS\n"
            'echo "CUSTOMMC_189_NATIVE_SUBTRIAL_REPORT=autoclicker:first"\n'))
        with self.assertRaises(TypeError):
            native_subtrial_report_from_job_log(None)


if __name__ == "__main__":
    unittest.main()
