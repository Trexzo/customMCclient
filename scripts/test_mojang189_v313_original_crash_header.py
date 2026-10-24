"""V313: read only real local original Minecraft crash report headers.

A real vanilla 1.8.9 StackOverflowError can have thousands of frames; the
current singleplayer failure tail contains none of the original Throwable.
"""
from pathlib import Path
import os
import tempfile
import unittest

from RunMojang189SingleplayerSmoke import v313_original_crash_report_header


class V313OriginalCrashEvidenceTests(unittest.TestCase):
    def test_missing_original_game_report_is_not_success_or_error(self):
        with tempfile.TemporaryDirectory() as home:
            self.assertIsNone(v313_original_crash_report_header(home))

    def test_empty_original_crash_report_is_no_evidence(self):
        with tempfile.TemporaryDirectory() as home:
            p = Path(home) / "crash-reports"
            p.mkdir()
            (p / "crash-01.txt").write_bytes(b"")
            self.assertIsNone(v313_original_crash_report_header(home))

    def test_real_server_crash_head_preserves_first_throwable(self):
        with tempfile.TemporaryDirectory() as home:
            reports = Path(home) / "crash-reports"
            reports.mkdir()
            p = reports / "crash-2026-10-24_10.37.27-server.txt"
            p.write_bytes(
                b"---- Minecraft Crash Report ----\n"
                b"Description: Exception ticking world\n\n"
                b"java.lang.StackOverflowError\n"
                + b"\tat adm.c(SourceFile:394)\n" * 1000)
            head = v313_original_crash_report_header(home)
            self.assertTrue(head.startswith("file=crash-2026"))
            self.assertIn("java.lang.StackOverflowError", head)
            self.assertIn("Description: Exception ticking world", head)
            self.assertLessEqual(len(head.encode("utf-8")), 6100)
            self.assertNotIn("PASS=YES", head)

    def test_latest_original_server_crash_wins_without_other_game_files(self):
        with tempfile.TemporaryDirectory() as home:
            reports = Path(home) / "crash-reports"
            reports.mkdir()
            old = reports / "crash-old.txt"
            new = reports / "crash-new.txt"
            old.write_bytes(b"old genuine error")
            new.write_bytes(b"new genuine error")
            os.utime(old, ns=(1000000000, 1000000000))
            os.utime(new, ns=(2000000000, 2000000000))
            head = v313_original_crash_report_header(home)
            self.assertIn("new genuine error", head)
            self.assertNotIn("old genuine error", head)

    def test_unrelated_log_is_not_mojang_crash_proof(self):
        with tempfile.TemporaryDirectory() as home:
            reports = Path(home) / "crash-reports"
            reports.mkdir()
            (reports / "spoofed.txt").write_bytes(b"fake error")
            (reports / "crash-ignored.json").write_bytes(b"fake error")
            self.assertIsNone(v313_original_crash_report_header(home))

    def test_symlink_outside_game_directory_is_never_read(self):
        with tempfile.TemporaryDirectory() as home:
            reports = Path(home) / "crash-reports"
            reports.mkdir()
            outside = Path(home) / "outside.txt"
            outside.write_bytes(b"outside-sentinel")
            (reports / "crash-link.txt").symlink_to(outside)
            self.assertIsNone(v313_original_crash_report_header(home))

    def test_header_is_bounded_even_with_unlimited_native_stack(self):
        with tempfile.TemporaryDirectory() as home:
            reports = Path(home) / "crash-reports"
            reports.mkdir()
            (reports / "crash-large.txt").write_bytes(b"A" * 500000)
            head = v313_original_crash_report_header(home, 128)
            self.assertTrue(head.endswith("A" * 128))
            self.assertLessEqual(len(head), 200)

    def test_bad_header_limits_cannot_dump_unbounded_native_log(self):
        with tempfile.TemporaryDirectory() as home:
            for length in (0, 1, 127, 6001, -1, None, "all"):
                with self.subTest(length=length):
                    with self.assertRaises(ValueError):
                        v313_original_crash_report_header(home, length)

    def test_crash_diagnostic_has_no_success_or_attack_authority(self):
        with tempfile.TemporaryDirectory() as home:
            reports = Path(home) / "crash-reports"
            reports.mkdir()
            (reports / "crash-latest.txt").write_bytes(
                b"java.lang.StackOverflowError\n")
            evidence = v313_original_crash_report_header(home)
            self.assertIn("StackOverflowError", evidence)
            self.assertNotIn("CUSTOMMC_OFFICIAL_189_LIVE_WORLD_TICKS_PASS", evidence)


if __name__ == "__main__":
    unittest.main()
