"""V315: actual 1.8.9 server spawn at Z=1013 within max-world-size=1024.

Run 40086218858 attempt 2 logged the real original player at
(-349.5,4.0,1013.5). V312 accepted only |source anchor|<=1000 despite
the dedicated server's configured maximum 1024, rejecting before the
first independent real stone/UUID witness. This module tests geometry,
not synthetic proof of native gameplay.
"""
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_rejoin_three_player_floor_fixture,
    actual_server_three_client_rejoin_fixture,
    actual_server_three_client_locked_fixture,
    original_locked_tp_server_reply,
    write_server_properties,
)


def server_source(x, z):
    return (
        "CIHost[/127.0.0.1:52226] logged in with entity id 26 "
        f"at ({x + 0.5}, 4.0, {z + 0.5})\n"
    ).encode("ascii")


def commands(x, z, y=5):
    return (
        f"tp CIHost {x} {y} {z} 0 0",
        f"tp CITarget {x} {y} {z + 2} 180 0",
        f"tp CITargetB {x + 1} {y} {z + 2} 180 0",
    )


class Original1024BorderTests(unittest.TestCase):
    def test_exact_actual_genuine_run_z_1013_generates_real_floor(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-dedicated.log"
            log.write_bytes(server_source(-350, 1013))
            host, a_near, b_outer = actual_server_three_client_rejoin_fixture(log)
            self.assertEqual(host, "tp CIHost -350 5 1013 0 0")
            self.assertEqual(a_near, "tp CITarget -350 5 1015 180 0")
            self.assertEqual(b_outer, "tp CITargetB -349 5 1015 180 0")
            fill, witnesses = actual_server_rejoin_three_player_floor_fixture(
                host, a_near, b_outer)
            self.assertEqual(fill, "fill -352 4 1010 -348 4 1016 stone")
            self.assertEqual(witnesses, ((-350, 4, 1013),
                                         (-350, 4, 1015),
                                         (-349, 4, 1015)))
            self.assertEqual(len(set(witnesses)), 3)

    def test_positive_native_border_z_1020_includes_last_stone_1023(self):
        fill, witnesses = actual_server_rejoin_three_player_floor_fixture(
            *commands(40, 1020))
        self.assertEqual(fill, "fill 38 4 1017 42 4 1023 stone")
        self.assertEqual(len(set(witnesses)), 3)

    def test_negative_native_border_z_negative_1020_stays_inside_server(self):
        fill, _ = actual_server_rejoin_three_player_floor_fixture(
            *commands(40, -1020))
        self.assertEqual(fill, "fill 38 4 -1023 42 4 -1017 stone")

    def test_source_x_plus_and_minus_1020_keeps_real_floor_inside(self):
        for x in (-1020, 1020):
            with self.subTest(x=x):
                fill, witnesses = actual_server_rejoin_three_player_floor_fixture(
                    *commands(x, 40))
                self.assertIn(str(x), fill)
                self.assertTrue(all(abs(point[0]) < 1024
                                    for point in witnesses))

    def test_source_beyond_last_safe_floor_cell_still_fails_closed(self):
        for x, z in ((1021, 0), (-1021, 0),
                     (0, 1021), (0, -1021)):
            with self.subTest(x=x, z=z):
                with self.assertRaisesRegex(AssertionError,
                                            "bounded flat vanilla world"):
                    actual_server_rejoin_three_player_floor_fixture(
                        *commands(x, z))

    def test_near_positive_border_far_a_moves_inward_12_without_weakening(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-dedicated.log"
            log.write_bytes(server_source(1013, 440))
            host, near, _, _, far = actual_server_three_client_locked_fixture(log)
            self.assertEqual(host.split()[2], "1013")
            self.assertEqual(int(far.split()[2]) - int(near.split()[2]), -12)
            self.assertEqual(far, "tp CITarget 1001 5 442 180 0")
            self.assertEqual(original_locked_tp_server_reply(far),
                             b"Teleported CITarget to 1001.5, 5.0, 442.5")

    def test_near_negative_border_far_a_moves_positive_12(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-dedicated.log"
            log.write_bytes(server_source(-1013, 440))
            _, near, _, _, far = actual_server_three_client_locked_fixture(log)
            self.assertEqual(int(far.split()[2]) - int(near.split()[2]), 12)
            self.assertEqual(far, "tp CITarget -1001 5 442 180 0")

    def test_far_a_invalid_source_beyond_real_1024_border_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "original-dedicated.log"
            log.write_bytes(server_source(1021, 440))
            with self.assertRaisesRegex(AssertionError,
                                        "outside server world"):
                actual_server_three_client_locked_fixture(log)

    def test_loopback_original_server_border_remains_1024(self):
        with tempfile.TemporaryDirectory() as root:
            p = Path(root) / "server.properties"
            write_server_properties(p, 34511, max_players=3)
            properties = p.read_text("ascii")
            self.assertIn("server-ip=127.0.0.1\n", properties)
            self.assertIn("online-mode=false\n", properties)
            self.assertIn("max-world-size=1024\n", properties)
            self.assertIn("enable-rcon=false\n", properties)

    def test_no_native_success_or_controller_proof_from_server_geometry(self):
        fill, witness = actual_server_rejoin_three_player_floor_fixture(
            *commands(-350, 1013))
        self.assertTrue(fill.startswith("fill "))
        self.assertEqual(len(witness), 3)
        self.assertNotIn("PASS=YES", repr((fill, witness)))


if __name__ == "__main__":
    unittest.main()
