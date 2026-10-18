"""Offline *policy* checks for real native distant-player fixture.

The source negative (80 original tick callbacks and server zero damage) is
verified only in full pinned Mojang Minecraft 1.8.9 GUI/dedicated CI.
"""
import tempfile
import unittest
from pathlib import Path

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_spawn_fixture,
    actual_server_spawn_range_negative_fixture,
)


class OriginalFarPlayerSourceFixtureTest(unittest.TestCase):
    def fixture(self, raw):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-server.log"
            log.write_bytes(raw)
            return (actual_server_spawn_fixture(log),
                    actual_server_spawn_range_negative_fixture(log))

    def test_positive_z_chunk_end_uses_negative_seven_without_crossing(self):
        normal, far = self.fixture(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at "
            b"(243.5, 4.0, 444.5)\n")
        self.assertEqual(normal, (
            "tp CIHost 243 5 444 180 0",
            "tp CITarget 243 5 440 0 0",
        ))
        self.assertEqual(far, (
            "tp CIHost 243 5 444 180 0",
            "tp CITarget 243 5 437 0 0",
        ))

    def test_negative_z_chunk_start_uses_positive_seven_and_correct_yaw(self):
        normal, far = self.fixture(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at "
            b"(-36.5, 4.0, -47.5)\n")
        self.assertEqual(normal, (
            "tp CIHost -37 5 -48 0 0",
            "tp CITarget -37 5 -44 180 0",
        ))
        self.assertEqual(far, (
            "tp CIHost -37 5 -48 0 0",
            "tp CITarget -37 5 -41 180 0",
        ))

    def test_same_native_loaded_chunk_and_real_seven_block_separation(self):
        for z in range(-36, -19):
            raw = ("CIHost[/127.0.0.1:42110] logged in with entity id "
                   "5 at (35.5, 4.0, {}.5)\n".format(z)).encode()
            with self.subTest(z=z):
                _, far = self.fixture(raw)
                host, target = far
                a, b = host.split(), target.split()
                ha, ta = int(a[4]), int(b[4])
                self.assertEqual(abs(ta-ha), 7)
                self.assertEqual(ha // 16, ta // 16,
                                 "original World chunk must be loaded")
                self.assertEqual(a[2:4], b[2:4])
                self.assertNotEqual(a[5], b[5],
                                    "original players face each other")

    def test_genuine_server_login_is_required_and_never_guessed(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "server.log"
            log.write_bytes(b"CIHost joined the game\n")
            with self.assertRaisesRegex(AssertionError, "spawn unknown"):
                actual_server_spawn_range_negative_fixture(log)

    def test_original_negative_source_gates_and_positive_pvp_unchanged(self):
        script = (Path(__file__).parent /
                  "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        start = script.rfind("        if args.range_veto:\n")
        self.assertGreaterEqual(start, 0, "actual original far-player branch missing")
        segment = script[start:start+3200]
        self.assertIn("actual_server_spawn_range_negative_fixture(", segment)
        self.assertIn("wait_for(host, host_log, RANGE_VETO", segment)
        self.assertIn('"scoreboard players test CITarget taken 0 0"', segment)
        self.assertIn("REAL_PLAYER_OUT_OF_RANGE_SERVER_NO_DAMAGE_PASS", segment)
        # This isolated fixture may not weaken the positive original
        # 2-block PvP test nor the unrelated 4-block initial-target case.
        self.assertIn("def actual_server_spawn_positive_pvp_fixture", script)
        self.assertIn("def actual_server_spawn_fixture", script)
