"""Real-original-game Scaffold V23 grounding fixture strict offline tests.

Mocking here tests only control flow and source-coordinate policy. Only the
official pinned original Minecraft 1.8.9 graphical test certifies behavior.
"""
from pathlib import Path
from unittest import TestCase
from unittest.mock import patch
import RunMojang189SingleplayerSmoke as smoke


class OriginalScaffoldContextRoomTests(TestCase):
    def test_real_source_commands_have_immutable_order_and_exact_server_replies(self):
        commands = []
        with patch.object(smoke, "send_verified_original_chat_reply",
                          side_effect=lambda *args: commands.append(
                              ("tp", args[3], args[4]))), patch.object(
                smoke, "send_verified_fill",
                side_effect=lambda *args: commands.append(
                    ("fill", args[3]))):
            smoke.prepare_native_scaffold_context_chamber(
                object(), Path("original-source.log"), "source-window")
        self.assertEqual(commands, [
            ("tp", "/tp CIProbe 2 120 2 0 0",
                   b"[CHAT] Teleported CIProbe to 2.5, 120.0, 2.5"),
            ("fill", "/fill 0 99 0 4 104 4 stone"),
            ("fill", "/fill 0 100 0 4 104 4 air"),
            ("tp", "/tp CIProbe 2 100 2 0 0",
                   b"[CHAT] Teleported CIProbe to 2.5, 100.0, 2.5"),
        ])

    def test_native_grounded_requires_three_source_ticks_without_motion(self):
        ok = [(2.5, 100., 2.5), (2.502, 100., 2.501),
              (2.5, 100., 2.5)]
        self.assertTrue(smoke.native_scaffold_context_grounded(ok))
        self.assertFalse(smoke.native_scaffold_context_grounded(ok[:2]))
        self.assertFalse(smoke.native_scaffold_context_grounded(
            [(0.5, 100., 0.5)] * 3),
            "old source-border geometry cannot masquerade as center")
        self.assertFalse(smoke.native_scaffold_context_grounded(
            [(2.5, 120., 2.5)] * 3),
            "real teleport alone is not grounded")
        self.assertFalse(smoke.native_scaffold_context_grounded(
            [(2.5, 100., 2.5), (2.5, 100., 2.5),
             (2.5, 100.10, 2.5)]),
            "even physically close source ticks cannot drift through ground")
        self.assertFalse(smoke.native_scaffold_context_grounded(
            [(2.5, 100., 2.5), (2.5, 100., 2.5),
             (2.70, 100., 2.5)]))
        self.assertTrue(smoke.native_scaffold_context_grounded(
            [(0, 0, 0)] + ok),
            "only the latest consecutive real native samples own readiness")

    def test_ground_and_air_cuboids_are_closed_and_in_one_source_chunk(self):
        floor = (0, 99, 0, 4, 104, 4)
        hollow = (0, 100, 0, 4, 104, 4)
        for cube in (floor, hollow):
            self.assertTrue(all(0 <= cube[i] <= 15 for i in (0, 2, 3, 5)))
            self.assertTrue(cube[0] <= 2 <= cube[3])
            self.assertTrue(cube[2] <= 2 <= cube[5])
        self.assertEqual(floor[1] + 1, hollow[1],
                         "stone at y=99 is the real floor after hollowing")
        self.assertTrue(floor[4] >= hollow[4])
        self.assertLess(floor[4], 120,
                        "first player remains above future solid fill")

    def test_full_native_scaffold_context_does_not_replace_gameplay_proof(self):
        src = Path(smoke.__file__).read_text(encoding="utf-8")
        mode = src.split("            if native_scaffold_context:", 1)[1]
        mode = mode.split("            if native_scaffold_placement:", 1)[0]
        self.assertIn("prepare_native_scaffold_context_chamber", mode)
        self.assertIn("native_scaffold_context_grounded", mode)
        self.assertIn('send_command(process, log, window, "/give CIProbe stone 8")',
                      mode)
        for marker in (
            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_PLAYER_EYE_PASS",
            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_CONTROLLER_REACH_PASS",
            "CUSTOMMC_OFFICIAL_189_SCAFFOLD_ORIGINAL_TICK_CONTEXT_PASS",
        ):
            self.assertIn(marker, mode)
        self.assertIn("await_marker(process, log, eye_tag", mode)
        self.assertIn("await_marker(process, log, reach_tag", mode)
        self.assertIn("await_marker(process, log, tag", mode)
        self.assertNotIn("continue_on_error", mode)
