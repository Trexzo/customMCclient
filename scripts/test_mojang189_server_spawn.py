"""Offline regression checks for the genuine-server spawn-chunk test fixture."""
import tempfile
from pathlib import Path
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_spawn_fixture,
    actual_server_spawn_fov_fixture,
)


class SpawnChunkFixtureTest(unittest.TestCase):
    def check(self, raw, expected_host, expected_target):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(raw)
            result = actual_server_spawn_fixture(path)
            self.assertEqual((expected_host, expected_target), result)
            return result

    def test_spawn_near_high_z_chunk_end_uses_negative_four(self):
        self.check(
            b"[Server thread/INFO]: CIHost[/127.0.0.1:48866] logged in with entity id 48 at (243.5, 4.0, 444.5)\n",
            "tp CIHost 243 5 444 180 0",
            "tp CITarget 243 5 440 0 0"
        )

    def test_spawn_near_low_z_chunk_start_uses_positive_four(self):
        self.check(
            b"CIHost[/127.0.0.1:40000] logged in with entity id 2 at (-36.5, 4.0, -47.5)\n",
            "tp CIHost -37 5 -48 0 0",
            "tp CITarget -37 5 -44 180 0"
        )

    def test_missing_real_server_login_fails_closed(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_text(
                "CIHost joined the game\nTeleported CIHost to 0.5, 5.0, 0.5\n"
            )
            with self.assertRaisesRegex(AssertionError, "spawn unknown"):
                actual_server_spawn_fixture(path)


class RealFovSpawnChunkFixtureTest(unittest.TestCase):
    def check(self, raw, host_command, target_command):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(raw)
            self.assertEqual(
                actual_server_spawn_fov_fixture(path),
                (host_command, target_command)
            )

    def test_forward_target_is_actually_behind_180_yaw(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            "tp CIHost 243 5 444 180 0",
            "tp CITarget 243 5 446 0 0",
        )

    def test_backward_target_is_actually_behind_zero_yaw(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 447.5)\n",
            "tp CIHost 243 5 447 0 0",
            "tp CITarget 243 5 445 180 0",
        )

    def test_fov_requires_real_mojang_server_spawn(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(b"CIHost joined the game\n")
            with self.assertRaisesRegex(AssertionError, "host spawn unknown"):
                actual_server_spawn_fov_fixture(path)


if __name__ == "__main__":
    unittest.main()
