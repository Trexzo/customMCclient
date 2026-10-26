"""V335: expose source-original GUI class only as failed-trial forensics."""
from pathlib import Path
import importlib.util
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "scripts"
HARNESS_SOURCE = (SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8"
)
JAVA_SOURCE = (ROOT / "platform-1.8.9" / "src" / "main" / "java" /
               "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9" /
               "Minecraft189NativeGuiCombatVeto.java").read_text(encoding="utf-8")


def original_harness():
    sys.path.insert(0, str(SCRIPTS))
    spec = importlib.util.spec_from_file_location(
        "v335_original_host_gui_harness",
        SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py"
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class V335SourceGuiClassEvidenceTest(unittest.TestCase):
    def test_first_genuine_gui_class_is_preserved_beyond_failure_tail(self):
        harness = original_harness()
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "original-host.log"
            early = (
                b"CUSTOMMC_189_V335_NATIVE_GUI_VETO_SOURCE "
                b"OPEN class=aya\n"
            )
            log.write_bytes(early + b"other unrelated source evidence\n" * 1300)
            self.assertNotIn(early, harness.log_tail(log, 6000))
            original = harness.v329_original_autoclicker_failure_evidence(log)
            self.assertEqual(1, sum("V335_NATIVE_GUI_VETO_SOURCE" in line
                                    for line in original))
            self.assertTrue(any("OPEN class=aya" in line for line in original))

    def test_no_gui_class_diagnostic_can_authorize_a_real_attack(self):
        run = HARNESS_SOURCE.split("def run(", 1)[1].split(
            "    except BaseException:", 1)[0]
        self.assertNotIn("V335_NATIVE_GUI_VETO_SOURCE", run)
        for required in (
            "AUTO_NATIVE_UNSCOPED, deadline",
            "AUTO_NATIVE_EXACT_REMOTE, deadline",
            "AUTO_HURT if args.autoclicker_pvp",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(required, HARNESS_SOURCE)
        self.assertIn("if (args.autoclicker_pvp or args.rejoin_release)",
                      HARNESS_SOURCE)

    def test_log_only_after_original_open_or_unavailable_gate(self):
        self.assertIn("Minecraft189ScaffoldScreenGate.inspect(minecraft)",
                      JAVA_SOURCE)
        self.assertIn("verdict != Minecraft189ScaffoldScreenGate.Verdict.CLEAR",
                      JAVA_SOURCE)
        self.assertIn("Minecraft189OriginalGuiClassProbe.inspect(minecraft)",
                      JAVA_SOURCE)
        self.assertIn("CUSTOMMC_189_V335_NATIVE_GUI_VETO_SOURCE",
                      JAVA_SOURCE)
        self.assertNotIn("customMcClickMouse()", JAVA_SOURCE)
        self.assertNotIn("attackEntity(", JAVA_SOURCE)
        self.assertNotIn("leftClickCounter =", JAVA_SOURCE)


if __name__ == "__main__":
    unittest.main()
