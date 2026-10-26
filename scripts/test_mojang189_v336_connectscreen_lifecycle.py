"""V336 genuine Minecraft 1.8.9 native GUI lifecycle evidence only."""
from pathlib import Path
import importlib.util
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "scripts"
PLATFORM = (ROOT / "platform-1.8.9" / "src" / "main" / "java" /
            "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9")
BRIDGE = (PLATFORM / "Minecraft189RuntimeBridge.java").read_text(
    encoding="utf-8"
)
HARNESS = (SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text(
    encoding="utf-8"
)


def source_harness():
    sys.path.insert(0, str(SCRIPTS))
    spec = importlib.util.spec_from_file_location(
        "v336_original_mojang_source_harness",
        SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py"
    )
    obj = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(obj)
    return obj


class V336OriginalConnectScreenLifecycle(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.harness = source_harness()

    def test_exact_original_connectscreen_clear_and_reopen_are_preserved(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / "CIHost-original.log"
            screens = [
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=awz",
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=axs",
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=(in-game/no GUI)",
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=awz",
            ]
            source.write_bytes(("\n".join(screens) + "\n"
                                + "unrelated original logs\n" * 1100
                                ).encode("ascii"))
            self.assertNotIn(
                b"CUSTOMMC_189_ACCEPTANCE_SCREEN=awz",
                self.harness.log_tail(source, 6000)
            )
            self.assertEqual(
                screens,
                self.harness.v336_original_source_screen_timeline(source)
            )

    def test_missing_unmatched_or_invalid_source_never_creates_history(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "CIHost-original.log"
            source.write_bytes(
                b"not a native screen\n"
                b"prefixed CUSTOMMC_189_ACCEPTANCE_SCREEN=awz\n"
            )
            self.assertEqual([], self.harness.v336_original_source_screen_timeline(source))
            self.assertEqual([], self.harness.v336_original_source_screen_timeline(root / "absent"))
            with self.assertRaises(ValueError):
                self.harness.v336_original_source_screen_timeline(source, 0)
            with self.assertRaises(ValueError):
                self.harness.v336_original_source_screen_timeline(source, True)

    def test_transition_output_is_bounded_even_when_original_game_repeatedly_changes(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / "CIHost-original.log"
            states = [
                "CUSTOMMC_189_ACCEPTANCE_SCREEN=" + (
                    "awz" if i % 2 == 0 else "(in-game/no GUI)"
                ) for i in range(30)
            ]
            source.write_text("\n".join(states) + "\n", encoding="utf-8")
            observed = self.harness.v336_original_source_screen_timeline(source)
            self.assertEqual(states[:5] + states[-5:], observed)
            self.assertEqual([], self.harness.v336_original_source_screen_timeline(source, 1))

    def test_original_game_tick_gate_never_modifies_input_or_authorizes_pvp(self):
        section = BRIDGE.split(
            "public static synchronized void gameTick()", 1
        )[1].split("private static void reportAcceptanceScreen()", 1)[0]
        self.assertIn('custommc.acceptance.requireAutoClickerExactRemoteUuid', section)
        self.assertIn('custommc.acceptance.reportKillAuraRejoin', section)
        self.assertIn("reportAcceptanceScreen();", section)
        self.assertIn("activeRuntime.hostInstalled()", section)
        self.assertIn("if (!Boolean.getBoolean", section)
        for prohibited in ("displayGuiScreen(", "setIngameFocus(", "attackEntity(",
                           "leftClickCounter =", "customMcClickMouse()"):
            self.assertNotIn(prohibited, section)
        checks = HARNESS.split("def run(", 1)[1].split(
            "    except BaseException:", 1)[0]
        self.assertNotIn("v336_original_source_screen_timeline(", checks)
        for required in (
            "AUTO_NATIVE_UNSCOPED, deadline",
            "AUTO_NATIVE_EXACT_REMOTE, deadline",
            "AUTO_HURT if args.autoclicker_pvp",
            "scoreboard players test CITarget taken 1 999999",
        ):
            self.assertIn(required, HARNESS)


if __name__ == "__main__":
    unittest.main()
