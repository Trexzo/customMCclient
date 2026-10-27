"""V345: original gameTick GuiScreen forensics, never attack acceptance.

Check only the verified source log lines and strict separation between
read-only diagnostics and native-controller UUID/hurt/server success.
"""
from pathlib import Path
import importlib.util
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "scripts"
SRC = (ROOT / "platform-1.8.9" / "src" / "main" / "java"
       / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
BRIDGE = (SRC / "Minecraft189RuntimeBridge.java").read_text(encoding="utf-8")
HARNESS = (SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8")


def load_harness():
    sys.path.insert(0, str(SCRIPTS))
    spec = importlib.util.spec_from_file_location(
        "custommc_v345_original_gui_harness",
        SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class V345OriginalGuiTransitionEvidence(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.harness = load_harness()

    def test_actual_original_connectscreen_clear_and_reopen_lines_are_preserved(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            expected = [
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=awz",
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=axs",
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)",
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=awz",
            ]
            log.write_bytes(
                ("\n".join(expected) + "\n" + "not-a-source-event\n" * 1300
                 ).encode("utf-8"))
            self.assertNotIn(b"CUSTOMMC_189_ACCEPTANCE_SCREEN=awz",
                             self.harness.log_tail(log, 6000))
            self.assertEqual(expected,
                             self.harness.v345_original_screen_timeline(log))

    def test_missing_ambiguous_and_partial_lines_never_fabricate_history(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            log = root / "host.log"
            log.write_bytes(b"some CLIENT screen=awz\n"
                            b"prefix CUSTOMMC_189_ACCEPTANCE_SCREEN=awz\n"
                            b"CUSTOMMC_189_ACCEPTANCE_SCREEN=awz")
            self.assertEqual([], self.harness.v345_original_screen_timeline(log))
            self.assertEqual([], self.harness.v345_original_screen_timeline(
                root / "absent"))
            for invalid in (0, -1, True, 16777217):
                with self.assertRaises(ValueError):
                    self.harness.v345_original_screen_timeline(log, invalid)

    def test_first_five_and_last_five_are_bounded_without_false_pass(self):
        with tempfile.TemporaryDirectory() as folder:
            log = Path(folder) / "host.log"
            states = ["CUSTOMMC_189_ACCEPTANCE_SCREEN=" +
                      ("awz" if i % 2 == 0 else "(in-game/no GUI)")
                      for i in range(25)]
            log.write_text("\n".join(states) + "\n", encoding="utf-8")
            self.assertEqual(states[:5] + states[-5:],
                             self.harness.v345_original_screen_timeline(log))
            self.assertEqual([], self.harness.v345_original_screen_timeline(
                log, 1))

    def test_source_original_tick_does_not_override_vanilla_gui_or_controller(self):
        original = BRIDGE.split(
            "public static synchronized void gameTick()", 1)[1].split(
            "private static void reportAcceptanceScreen()", 1)[0]
        for flag in ("custommc.acceptance.reportAutoClicker",
                     "custommc.acceptance.reportKillAuraRejoin",
                     "custommc.acceptance.reportTriggerBotRealPvp"):
            self.assertIn(flag, original)
        self.assertIn("reportAcceptanceScreen();", original)
        self.assertIn("activeRuntime.hostInstalled()", original)
        for forbidden in ("displayGuiScreen(", "setIngameFocus(",
                          "attackEntity(", "leftClickCounter =",
                          "customMcClickMouse()"):
            self.assertNotIn(forbidden, original)

    def test_failure_only_forensics_cannot_satisfy_real_server_success(self):
        positive = HARNESS.split("def run(", 1)[1].split(
            "    except BaseException:", 1)[0]
        self.assertNotIn("v345_original_screen_timeline(", positive)
        failure = HARNESS.split("    except BaseException:", 1)[1].split(
            "    finally:", 1)[0]
        self.assertIn('if name == "host":', failure)
        self.assertIn("v345_original_screen_timeline(log)", failure)
        for mandatory in (
            "wait_for(host, host_log, CONTROLLER_ATTACK,",
            "TRIGGER_HURT if args.triggerbot_pvp else AURA_HURT,",
            "wait_for(host, host_log, REJOIN_B_HIT, deadline,",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(mandatory, HARNESS)
        self.assertNotIn("V345_GUI_PASS", HARNESS)


if __name__ == "__main__":
    unittest.main()
