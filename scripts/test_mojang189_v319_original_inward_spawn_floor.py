"""V319: genuine Mojang spawn near ±1024 re-anchors real rejoin test /tp.

Original dedicated server #40098773646 attempt 1 logged genuine
CIHost x=-1022.5: integer login x=-1023 would require a physical
5x7 stone floor through x=-1025, outside max-world-size=1024.
No game/World data is forged: /tp + /fill + /testforblock remain native.
"""
from pathlib import Path
import tempfile
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_three_client_rejoin_fixture,
    actual_server_rejoin_three_player_floor_fixture,
    write_server_properties,
)


def native_login(x, z, y=4.0):
    return (f"CIHost[/127.0.0.1:41108] logged in with entity id 40 "
            f"at ({x + 0.5}, {y}, {z + 0.5})\n").encode("ascii")


def commands(x, z, y=4.0):
    with tempfile.TemporaryDirectory() as root:
        logfile = Path(root) / "dedicated.log"
        logfile.write_bytes(native_login(x, z, y))
        return actual_server_three_client_rejoin_fixture(logfile)


class OriginalBorderInwardRejoin(unittest.TestCase):
    def test_exact_real_original_failed_negative_1022_point5_source(self):
        host, a, b = commands(-1023, -877)
        self.assertEqual(host, "tp CIHost -1020 5 -877 180 0" if False
                         else "tp CIHost -1020 5 -877 0 0")
        self.assertEqual(a, "tp CITarget -1020 5 -875 180 0")
        self.assertEqual(b, "tp CITargetB -1019 5 -875 180 0")
        floor, witnesses = actual_server_rejoin_three_player_floor_fixture(
            host, a, b)
        self.assertEqual(floor, "fill -1022 4 -880 -1018 4 -874 stone")
        self.assertEqual(witnesses, ((-1020, 4, -877), (-1020, 4, -875),
                                     (-1019, 4, -875)))
        self.assertEqual(len(set(witnesses)), 3)

    def test_max_negative_original_native_x_near_border(self):
        host, a, b = commands(-1023, 400)
        self.assertEqual(int(host.split()[2]), -1020)
        self.assertEqual(int(a.split()[2]), -1020)
        self.assertEqual(int(b.split()[2]), -1019)
        fill, witnesses = actual_server_rejoin_three_player_floor_fixture(
            host, a, b)
        self.assertEqual(int(fill.split()[1]), -1022)
        self.assertTrue(all(abs(w[0]) < 1024 for w in witnesses))

    def test_max_positive_original_native_x_near_border(self):
        host, a, b = commands(1023, 400)
        self.assertEqual(int(host.split()[2]), 1020)
        self.assertEqual(int(a.split()[2]), 1020)
        fill, witnesses = actual_server_rejoin_three_player_floor_fixture(
            host, a, b)
        self.assertEqual(int(fill.split()[4]), 1022)
        self.assertTrue(all(abs(w[0]) < 1024 for w in witnesses))

    def test_positive_z_native_border_is_clamped_inward(self):
        host, a, b = commands(250, 1023)
        self.assertEqual(int(host.split()[4]), 1020)
        self.assertEqual(int(a.split()[4]), 1018)
        fill, witnesses = actual_server_rejoin_three_player_floor_fixture(
            host, a, b)
        self.assertEqual(int(fill.split()[6]), 1023)
        self.assertTrue(all(abs(w[2]) < 1024 for w in witnesses))

    def test_negative_z_native_border_is_clamped_inward(self):
        host, a, b = commands(250, -1023)
        self.assertEqual(int(host.split()[4]), -1020)
        self.assertEqual(int(a.split()[4]), -1018)
        fill, witnesses = actual_server_rejoin_three_player_floor_fixture(
            host, a, b)
        self.assertEqual(int(fill.split()[3]), -1023)
        self.assertTrue(all(abs(w[2]) < 1024 for w in witnesses))

    def test_both_near_border_axes_remain_inside_actual_server(self):
        for x in (-1023, 1023):
            for z in (-1023, 1023):
                with self.subTest(x=x, z=z):
                    host, a, b = commands(x, z)
                    fill, witness = (
                        actual_server_rejoin_three_player_floor_fixture(
                            host, a, b))
                    pieces = fill.split()
                    self.assertTrue(all(abs(int(pieces[i])) < 1024
                                        for i in (1, 3, 4, 6)))
                    self.assertEqual(len(witness), 3)
                    self.assertEqual(len(set(witness)), 3)

    def test_unaffected_original_source_keeps_exact_commands(self):
        host, a, b = commands(-350, 1013)
        self.assertEqual(host, "tp CIHost -350 5 1013 0 0")
        self.assertEqual(a, "tp CITarget -350 5 1015 180 0")
        self.assertEqual(b, "tp CITargetB -349 5 1015 180 0")
        self.assertEqual(
            actual_server_rejoin_three_player_floor_fixture(host, a, b)[0],
            "fill -352 4 1010 -348 4 1016 stone")

    def test_true_outside_original_login_fails_without_fabricating_tps(self):
        for x, z in ((-1024, 0), (1024, 0),
                     (0, -1024), (0, 1024)):
            with self.subTest(x=x, z=z):
                with self.assertRaisesRegex(AssertionError,
                                            "outside 1024 server world"):
                    commands(x, z)

    def test_direct_illegal_floor_source_is_still_rejected(self):
        for x, z in ((-1023, 400), (1023, 400),
                     (400, -1023), (400, 1023)):
            with self.subTest(x=x, z=z):
                # Neither source a nor source b is fabricated. The old
                # 5x7 floor guard must still fail closed on raw out-of-border
                # original coordinates; only genuine full fixture uses TP.
                raw_host = f"tp CIHost {x} 5 {z} 0 0"
                raw_a = f"tp CITarget {x} 5 {z + 2} 180 0"
                raw_b = f"tp CITargetB {x + 1} 5 {z + 2} 180 0"
                with self.assertRaisesRegex(AssertionError,
                                            "outside bounded flat vanilla world"):
                    actual_server_rejoin_three_player_floor_fixture(
                        raw_host, raw_a, raw_b)

    def test_original_geometry_all_three_real_players_and_source_chunk(self):
        for x,z in ((-1023,-877),(1023,1023),(-100,1013)):
            with self.subTest(x=x,z=z):
                host,a,b=commands(x,z)
                hs,as_,bs=[c.split() for c in (host,a,b)]
                hx,hy,hz=(int(v) for v in hs[2:5])
                ax,ay,az=(int(v) for v in as_[2:5])
                bx,by,bz=(int(v) for v in bs[2:5])
                self.assertEqual((hy,ay,by),(5,5,5))
                self.assertEqual(ax,hx)
                self.assertEqual(abs(az-hz),2)
                self.assertEqual((az,bz),(bz,bz))
                self.assertEqual(abs(bx-hx),1)
                self.assertEqual(hx//16,bx//16)
                self.assertEqual(hz//16,az//16)
                self.assertLessEqual(abs(hx-x),3)
                self.assertLessEqual(abs(hz-z),3)

    def test_original_test_server_size_and_local_only_are_unchanged(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.properties"
            write_server_properties(path, 30003, max_players=3)
            source = path.read_text("ascii")
            self.assertIn("server-ip=127.0.0.1\n", source)
            self.assertIn("max-world-size=1024\n", source)
            self.assertIn("online-mode=false\n", source)

    def test_native_spatial_fixture_cannot_supply_gameplay_pass_marker(self):
        host,a,b=commands(-1023,-877)
        fill, witnesses=actual_server_rejoin_three_player_floor_fixture(
            host,a,b)
        for datum in (host,a,b,fill,str(witnesses)):
            self.assertNotIn("PASS=YES",datum)
            self.assertNotIn("attackEntity",datum)


if __name__ == "__main__":
    unittest.main()
