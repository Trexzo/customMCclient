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
                self.assertIn('for attempt in 1 2; do', self.source[
                    self.source.rfind('          for attempt',0,
                                      self.source.index('echo "'+marker),0)
                    :self.source.index('echo "'+marker)])
                self.assertIn('test "$accepted" -eq 1', block)
                self.assertIn('accepted=1', block)
                self.assertIn('break', block)

    def test_no_extra_original_github_actions_named_step(self):
        # Previously certified workflow had exactly 67 explicit named
        # steps; source proof-only V277 must not increase the 78-step job.
        self.assertEqual(67, len(re.findall(r"^      - name:",self.source,
                                            flags=re.MULTILINE)))

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
