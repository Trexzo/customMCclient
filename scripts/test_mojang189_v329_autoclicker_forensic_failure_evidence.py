"""V329 CI-only failure evidence must survive a clipped 6000-byte JVM log tail."""
from pathlib import Path
import importlib.util
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "scripts"


def load_harness():
    sys.path.insert(0, str(SCRIPTS))
    spec = importlib.util.spec_from_file_location(
        "v329_real_original_game_harness",
        SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py")
    harness = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(harness)
    return harness


class V329ClippedTailProvenanceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.harness = load_harness()

    def test_one_shot_native_counter_survives_truncated_failure_tail(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            exact = b"CUSTOMMC_189_AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_BLOCKED=10000\n"
            entry = b"CUSTOMMC_189_V329_SYNTHETIC_CLICK_ENTRY exactRemoteProperty=true vanillaMeleeProperty=true nativeInputPresent=true\n"
            guard = b"CUSTOMMC_189_V329_SYNTHETIC_GUARD_CALLBACK exactRemoteProperty=true sourceCounter=10000\n"
            data = (entry + guard + exact + (b"ARMED original native ray\n" * 1200))
            log.write_bytes(data)
            self.assertNotIn(exact, self.harness.log_tail(log, 6000))
            preserved = self.harness.v329_original_autoclicker_failure_evidence(log)
            self.assertEqual(3, len(preserved))
            self.assertTrue(any("sourceCounter=10000" in line for line in preserved))
            self.assertTrue(any("BLOCKED=10000" in line for line in preserved))
            self.assertTrue(any("exactRemoteProperty=true" in line for line in preserved))

    def test_no_markers_are_invented_and_repeated_lines_are_bounded(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            marker = b"CUSTOMMC_189_V329_FIRST_GUARD_CALLBACK synthetic=false exactRemoteProperty=true sourceCounter=8\n"
            log.write_bytes(marker * 500)
            preserved = self.harness.v329_original_autoclicker_failure_evidence(log)
            self.assertEqual(1, len(preserved))
            self.assertIn("synthetic=false", preserved[0])
            log.write_bytes(b"not a native call\n")
            self.assertEqual([], self.harness.v329_original_autoclicker_failure_evidence(log))

    def test_untrusted_path_and_invalid_limit_do_not_create_evidence(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            log = root / "host.log"
            log.write_bytes(b"CUSTOMMC_189_V329_SYNTHETIC_GUARD_CALLBACK sourceCounter=0\n")
            self.assertEqual([], self.harness.v329_original_autoclicker_failure_evidence(
                root / "missing.log"))
            with self.assertRaises(ValueError):
                self.harness.v329_original_autoclicker_failure_evidence(log, 0)
            with self.assertRaises(ValueError):
                self.harness.v329_original_autoclicker_failure_evidence(log, True)
            # Read cap is a hard bound, never an inferred missing native hook.
            self.assertEqual([], self.harness.v329_original_autoclicker_failure_evidence(
                log, 1))
            link = root / "alias.log"
            try:
                link.symlink_to(log)
            except (OSError, NotImplementedError):
                pass
            else:
                self.assertEqual([], self.harness.v329_original_autoclicker_failure_evidence(link))


if __name__ == "__main__":
    unittest.main()
