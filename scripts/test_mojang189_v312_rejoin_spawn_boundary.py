"""V312: genuine Mojang 1.8.9 negative-X spawn boundary and real stone floor.

The original 2026-10-24 CI run 40040751254 attempt 2 logged CIHost at
(-999.5,4.0,702.5). Its actual integer native fixture anchor was x=-1000,
but the old arbitrary |x|<997 guard rejected it before server floor proof.
"""
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_rejoin_three_player_floor_fixture,
    actual_server_spawn_fixture,
    actual_server_three_client_locked_fixture,
    write_server_properties,
)


def source_triplet(x, z, height=5):
    """Same player owners + near-A/diagonal-B geometry as original fixture."""
    return (
        f"tp CIHost {x} {height} {z} 0 0",
        f"tp CITarget {x} {height} {z + 2} 180 0",
        f"tp CITargetB {x + 1} {height} {z + 2} 180 0",
    )


class V312GenuineOriginalSpawnFloorTests(unittest.TestCase):
    def test_actual_ci_log_negative_999_point_5_maps_to_negative_1000(self):
        with tempfile.TemporaryDirectory() as temp:
            server = Path(temp) / "dedicated.log"
            server.write_bytes(
                b"[server thread/INFO]: CIHost[/127.0.0.1:44736] "
                b"logged in with entity id 33 at (-999.5, 4.0, 702.5)\n")
            host, _target = actual_server_spawn_fixture(server)
            self.assertEqual(host, "tp CIHost -1000 5 702 180 0")
            floor, witnesses = actual_server_rejoin_three_player_floor_fixture(
                *source_triplet(-1000, 702))
            self.assertEqual(floor, "fill -1002 4 699 -998 4 705 stone")
            self.assertEqual(witnesses,
                             ((-1000, 4, 702), (-1000, 4, 704),
                              (-999, 4, 704)))

    def test_positive_native_1000_anchor_has_35_real_floor_blocks(self):
        floor, witnesses = actual_server_rejoin_three_player_floor_fixture(
            *source_triplet(1000, 440))
        self.assertEqual(floor, "fill 998 4 437 1002 4 443 stone")
        self.assertEqual(len(set(witnesses)), 3)

    def test_negative_native_z_spawn_edge_is_not_fake_world_border(self):
        floor, witnesses = actual_server_rejoin_three_player_floor_fixture(
            *source_triplet(40, -1000))
        self.assertEqual(floor, "fill 38 4 -1003 42 4 -997 stone")
        self.assertIn((40, 4, -1000), witnesses)

    def test_positive_native_z_spawn_edge_is_not_fake_world_border(self):
        floor, _ = actual_server_rejoin_three_player_floor_fixture(
            *source_triplet(-40, 1000))
        self.assertEqual(floor, "fill -42 4 997 -38 4 1003 stone")

    def test_host_anchor_beyond_natual_spawn_bounds_fails_closed(self):
        for x, z in ((1001, 440), (-1001, 440),
                     (40, 1001), (40, -1001)):
            with self.subTest(x=x, z=z):
                with self.assertRaisesRegex(AssertionError, "bounded"):
                    actual_server_rejoin_three_player_floor_fixture(
                        *source_triplet(x, z))

    def test_local_only_real_server_border_contains_source_floor(self):
        with tempfile.TemporaryDirectory() as root:
            properties = Path(root) / "server.properties"
            write_server_properties(properties, 33171, max_players=3)
            text = properties.read_text("ascii")
            self.assertIn("server-ip=127.0.0.1\n", text)
            self.assertIn("online-mode=false\n", text)
            self.assertIn("enable-rcon=false\n", text)
            self.assertIn("max-world-size=1024\n", text)
            self.assertNotIn("max-world-size=1000\n", text)
            # Source anchor=1000; the full 5x7 stone floor extends only
            # to x=1002 / z=1003, safely inside this real test border.
            floor, _ = actual_server_rejoin_three_player_floor_fixture(
                *source_triplet(1000, 1000))
            self.assertIn("1002 4 1003 stone", floor)

    def test_real_positive_spawn_994_allows_original_far_1006(self):
        with tempfile.TemporaryDirectory() as root:
            server = Path(root) / "dedicated.log"
            server.write_bytes(
                b"CIHost[/127.0.0.1:42300] logged in with entity id 5 "
                b"at (994.5, 4.0, 444.5)\n")
            _, _, _, _, far = actual_server_three_client_locked_fixture(
                server)
            self.assertEqual(far.split()[2], "1006")

    def test_server_border_still_vetoes_far_teleport_beyond_1020(self):
        with tempfile.TemporaryDirectory() as root:
            server = Path(root) / "dedicated.log"
            server.write_bytes(
                b"CIHost[/127.0.0.1:42300] logged in with entity id 5 "
                b"at (1008.5, 4.0, 444.5)\n")
            with self.assertRaisesRegex(AssertionError,
                                        "outside server world"):
                actual_server_three_client_locked_fixture(server)

    def test_invalid_y_still_hard_fails(self):
        for height in (2, 253):
            with self.subTest(y=height):
                with self.assertRaisesRegex(AssertionError, "bounded"):
                    actual_server_rejoin_three_player_floor_fixture(
                        *source_triplet(-1000, 702, height))

    def test_invalid_genuine_owner_or_geometry_still_fails(self):
        host, a, b = source_triplet(-1000, 702)
        with self.assertRaisesRegex(AssertionError, "owners"):
            actual_server_rejoin_three_player_floor_fixture(
                host, a, b.replace("CITargetB", "ForgedPlayer"))
        with self.assertRaisesRegex(AssertionError, "geometry"):
            actual_server_rejoin_three_player_floor_fixture(
                host, a, b.replace("704", "705"))

    def test_fixture_does_not_fabricate_any_original_world_attestation(self):
        floor, witnesses = actual_server_rejoin_three_player_floor_fixture(
            *source_triplet(-1000, 702))
        self.assertTrue(floor.startswith("fill "))
        self.assertEqual(len(witnesses), 3)
        self.assertNotIn("PASS=YES", repr((floor, witnesses)))


if __name__ == "__main__":
    unittest.main()
