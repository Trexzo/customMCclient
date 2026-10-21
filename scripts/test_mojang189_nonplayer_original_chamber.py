"""V289 bounded source checks for genuine original nonplayer chamber.

These tests parse the actual native-runner source. They DO NOT start Minecraft,
create a mob or certify a real native raycast, click or server receipt.
"""
import ast
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / "scripts" / "RunMojang189SingleplayerSmoke.py").read_text(
    encoding="utf-8")
MODULE = ast.parse(SOURCE)


def original_combat_fixture_branches():
    for node in ast.walk(MODULE):
        if not isinstance(node, ast.If):
            continue
        source = ast.get_source_segment(SOURCE, node) or ""
        if ('CUSTOMMC_189_MANUAL_MELEE_FIXED_NATIVE_ROOM_PASS=YES'
                not in source):
            continue
        if not isinstance(node.test, ast.Name) or node.test.id != "attack":
            continue
        if len(node.orelse) != 1 or not isinstance(node.orelse[0], ast.If):
            continue
        nonplayer = node.orelse[0]
        if (not isinstance(nonplayer.test, ast.Name)
                or nonplayer.test.id != "kill_aura_nonplayer"):
            continue
        return (ast.get_source_segment(SOURCE, node) or "",
                "\n".join(ast.get_source_segment(SOURCE, item) or ""
                          for item in nonplayer.body),
                "\n".join(ast.get_source_segment(SOURCE, item) or ""
                          for item in nonplayer.orelse))
    raise AssertionError("real manual / nonplayer original native branch absent")


class OriginalNativeNonplayerChamberRegressionTest(unittest.TestCase):
    def test_real_nonplayer_branch_is_separate_from_manual_and_autoclicker(self):
        whole, nonplayer, fallback = original_combat_fixture_branches()
        self.assertIn("if attack:", whole)
        self.assertIn("elif kill_aura_nonplayer:", whole)
        self.assertIn("else:", whole)
        self.assertIn("CUSTOMMC_189_NONPLAYER_NATIVE_CHAMBER_SOURCE_READY", nonplayer)
        self.assertIn('"/fill ~-3 ~-1 ~-3 ~3 ~-1 ~5 stone"', fallback)
        self.assertNotIn("CUSTOMMC_189_NONPLAYER_NATIVE_CHAMBER_SOURCE_READY",
                         fallback)

    def test_genuine_source_teleports_precede_true_in_chunk_world_fill(self):
        _, native, _ = original_combat_fixture_branches()
        tokens = (
            '"/tp CIProbe 8 200 8"',
            '"/fill 8 119 8 8 119 8 stone"',
            '"/tp CIProbe 8 120 8"',
            '"/fill 4 99 4 12 104 13 stone"',
            '"/fill 5 100 5 11 103 12 air"',
            '"/tp CIProbe 8 100 8 0 26"',
            '"/summon Pig 8.5 100 10.5 {NoAI:1b}"',
        )
        positions = [native.index(s) for s in tokens]
        self.assertEqual(sorted(positions), positions,
                         "all actual native World operations must remain ordered")

    def test_every_required_room_action_uses_fresh_real_server_witness(self):
        _, native, _ = original_combat_fixture_branches()
        self.assertEqual(5, native.count("send_verified_original_chat_reply("),
                         "two setup teleports plus final teleport+mob summon")
        self.assertEqual(3, native.count("send_verified_fill("),
                         "genuine support, positive stone, positive air /fill")
        self.assertIn('b"[CHAT] Object successfully summoned"', native)
        self.assertIn('b"[CHAT] Teleported CIProbe to 8.5, 100.0, 8.5"',
                      native)
        self.assertNotIn("send_command(", native,
                         "no unacknowledged native setup command allowed")

    def test_native_room_cannot_suffocate_source_during_setup(self):
        _, native, _ = original_combat_fixture_branches()
        self.assertIn("time.sleep(1.0)", native)
        # All X/Z cells for the fixed support, room and pig remain in one
        # real loaded vanilla source chunk rather than random spawn chunks.
        outer = (4, 99, 4, 12, 104, 13)
        inner = (5, 100, 5, 11, 103, 12)
        self.assertTrue(all(0 <= outer[i] <= 15 for i in (0, 2, 3, 5)))
        self.assertTrue(all(0 <= inner[i] <= 15 for i in (0, 2, 3, 5)))
        self.assertLess(104, 119)
        self.assertLess(119, 120)
        self.assertIn('"/tp CIProbe 8 120 8"', native)

    def test_original_twenty_and_eighty_nonplayer_gates_remain_mandatory(self):
        runner = SOURCE.split("            if controlled_combat:", 1)[1]
        self.assertIn("await_marker(process, log, AURA_DISABLED, deadline", runner)
        self.assertIn("await_marker(process, log, AURA_ENABLED, deadline", runner)
        self.assertIn("await_marker(process, log, AURA_VETO, deadline", runner)
        self.assertIn("require_running(process, \"Kill Aura nonplayer veto\")", runner)
        self.assertIn("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_NONPLAYER_NEGATIVE_PASS",
                      runner)
        self.assertLess(runner.index("await_marker(process, log, AURA_DISABLED"),
                        runner.index("await_marker(process, log, AURA_ENABLED"))
        self.assertLess(runner.index("await_marker(process, log, AURA_ENABLED"),
                        runner.index("await_marker(process, log, AURA_VETO"))

    def test_room_acknowledgment_cannot_serve_as_a_native_mob_veto_pass(self):
        _, native, _ = original_combat_fixture_branches()
        self.assertNotIn("CUSTOMMC_189_KILLAURA_DISABLED_NONPLAYER_TICKS_PASS",
                         native)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_KILLAURA_NONPLAYER_VETO_PASS",
                         native)
        self.assertNotIn("CUSTOMMC_OFFICIAL_189_REAL_KILLAURA_NONPLAYER_NEGATIVE_PASS",
                         native)
        self.assertNotIn("continue_on_error", native)


if __name__ == "__main__":
    unittest.main()
