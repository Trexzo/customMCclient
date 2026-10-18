"""Offline regression checks for the genuine-server spawn-chunk test fixture."""
import tempfile
from pathlib import Path
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_spawn_fixture,
    actual_server_spawn_fov_fixture,
    actual_server_spawn_wall_fixture,
    actual_server_three_client_target_switch_fixture,
    actual_server_three_client_simultaneous_fixture,
    actual_server_three_client_locked_fixture,
    actual_server_three_client_rejoin_fixture,
    actual_server_rejoined_a_controller_fixture,
    official_offline_uuid,
    actual_server_spawn_positive_pvp_fixture,
    actual_server_clear_wall_command,
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


class RealWallSpawnChunkFixtureTest(unittest.TestCase):
    def check(self, raw, host, target, fill):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "server.log"
            log.write_bytes(raw)
            self.assertEqual(
                actual_server_spawn_wall_fixture(log), (host, target, fill)
            )

    def test_forward_two_block_server_wall_in_same_chunk(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            "tp CIHost 243 5 444 0 0",
            "tp CITarget 243 5 446 180 0",
            "fill 242 5 445 244 8 445 stone",
        )

    def test_backward_two_block_server_wall_near_chunk_edge(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-36.5, 4.0, -33.5)\n",
            "tp CIHost -37 5 -34 180 0",
            "tp CITarget -37 5 -36 0 0",
            "fill -38 5 -35 -36 8 -35 stone",
        )

    def test_wall_fixture_requires_native_mojang_login(self):
        with tempfile.TemporaryDirectory() as root:
            log = Path(root) / "server.log"
            log.write_bytes(b"CIHost joined the game\n")
            with self.assertRaisesRegex(AssertionError, "wall host spawn unknown"):
                actual_server_spawn_wall_fixture(log)


class WallRemovalCommandTest(unittest.TestCase):
    def test_forward_wall_removal_reuses_exact_coordinates(self):
        self.assertEqual(
            actual_server_clear_wall_command("fill 242 5 445 244 8 445 stone"),
            "fill 242 5 445 244 8 445 air",
        )

    def test_negative_coords_remain_identical(self):
        self.assertEqual(
            actual_server_clear_wall_command("fill -38 5 -35 -36 8 -35 stone"),
            "fill -38 5 -35 -36 8 -35 air",
        )

    def test_malformed_or_non_stone_command_fails_closed(self):
        for command in ("fill 0 0 0 1 1 1 air",
                        "fill 0 0 0 1 1 stone",
                        "fill 0 0 NaN 1 1 1 stone"):
            with self.subTest(command=command):
                with self.assertRaises(AssertionError):
                    actual_server_clear_wall_command(command)


class PositivePvpSpawnChunkFixtureTest(unittest.TestCase):
    def check(self, raw, host, target):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(raw)
            self.assertEqual(actual_server_spawn_positive_pvp_fixture(path),
                             (host, target))

    def test_positive_pvp_follows_real_positive_z_spawn(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            "tp CIHost 243 5 444 0 0",
            "tp CITarget 243 5 446 180 0",
        )

    def test_positive_pvp_follows_negative_chunk_edge(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-36.5, 4.0, -33.5)\n",
            "tp CIHost -37 5 -34 180 0",
            "tp CITarget -37 5 -36 0 0",
        )

    def test_positive_pvp_requires_real_server_login(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(b"CIHost joined the game\n")
            with self.assertRaisesRegex(AssertionError, "wall host spawn unknown"):
                actual_server_spawn_positive_pvp_fixture(path)


class RealThreePlayerFixtureTest(unittest.TestCase):
    def test_two_native_candidates_and_position_switch(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(
                b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n")
            self.assertEqual(actual_server_three_client_target_switch_fixture(path), (
                "tp CIHost 243 5 444 0 0",
                "tp CITarget 243 5 446 180 0",
                "tp CITargetB 248 5 446 180 0",
                "tp CITarget 248 5 446 180 0",
                "tp CITargetB 243 5 446 180 0",
            ))

    def test_three_player_fixture_fails_without_native_login(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(b"CITargetB joined the game\n")
            with self.assertRaises(AssertionError):
                actual_server_three_client_target_switch_fixture(path)

    def test_server_offline_names_derive_distinct_uuid_v3(self):
        import uuid
        a = official_offline_uuid("CITarget")
        b = official_offline_uuid("CITargetB")
        self.assertNotEqual(a, b)
        self.assertEqual(uuid.UUID(a).version, 3)
        self.assertEqual(uuid.UUID(b).version, 3)


class RealSimultaneousTargetFixtureTest(unittest.TestCase):
    def check(self, raw, expected):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(raw)
            self.assertEqual(
                actual_server_three_client_simultaneous_fixture(path),
                expected,
            )

    def test_both_eligible_positive_direction_same_spawn_chunk(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            ("tp CIHost 243 5 444 0 0",
             "tp CITarget 243 5 446 180 0",
             "tp CITargetB 245 5 446 180 0",
             "tp CITargetB 244 5 445 180 0"),
        )

    def test_both_eligible_negative_direction_near_x_chunk_edge(self):
        self.check(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n",
            ("tp CIHost -34 5 -34 180 0",
             "tp CITarget -34 5 -36 0 0",
             "tp CITargetB -36 5 -36 180 0",
             "tp CITargetB -35 5 -35 180 0"),
        )

    def test_real_server_login_required_for_simultaneous_fixture(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(b"CITargetB joined the game\n")
            with self.assertRaisesRegex(AssertionError, "wall host spawn unknown"):
                actual_server_three_client_simultaneous_fixture(path)


class RealLockedTargetFixtureTest(unittest.TestCase):
    def fixture(self, raw, expected):
        with tempfile.TemporaryDirectory() as directory:
            log = Path(directory) / "server.log"
            log.write_bytes(raw)
            self.assertEqual(actual_server_three_client_locked_fixture(log), expected)

    def test_native_forward_spawn_sticky_then_range_release(self):
        self.fixture(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            ("tp CIHost 243 5 444 0 0",
             "tp CITarget 243 5 446 180 0",
             "tp CITargetB 245 5 446 180 0",
             "tp CITargetB 244 5 445 180 0",
             "tp CITarget 248 5 446 180 0"),
        )

    def test_native_reverse_spawn_sticky_then_range_release(self):
        self.fixture(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n",
            ("tp CIHost -34 5 -34 180 0",
             "tp CITarget -34 5 -36 0 0",
             "tp CITargetB -36 5 -36 180 0",
             "tp CITargetB -35 5 -35 180 0",
             "tp CITarget -29 5 -36 180 0"),
        )

    def test_real_login_missing_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            log = Path(directory) / "server.log"
            log.write_bytes(b"CITargetB joined the game\n")
            with self.assertRaises(AssertionError):
                actual_server_three_client_locked_fixture(log)


class OriginalM419RejoinedNativeDistanceFixtureTest(unittest.TestCase):
    def test_real_positive_and_negative_sources_move_only_rejoined_a(self):
        import math
        for login, expected in (
            (b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at "
             b"(243.5, 4.0, 444.5)\n", "tp CITarget 243.05 5 445.85 180 0"),
            (b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at "
             b"(-33.5, 4.0, -33.5)\n", "tp CITarget -33.05 5 -34.85 0 0"),
        ):
            with tempfile.TemporaryDirectory() as root:
                path = Path(root) / "server.log"
                path.write_bytes(login)
                host, a, b = actual_server_three_client_rejoin_fixture(path)
                chosen = actual_server_rejoined_a_controller_fixture(host, a, b)
                self.assertEqual(chosen, expected)
                hx, hy, hz = map(int, host.split()[2:5])
                ax = float(chosen.split()[2])
                ay = int(chosen.split()[3])
                az = float(chosen.split()[4])
                bx, by, bz = map(int, b.split()[2:5])
                self.assertEqual(ay, hy)
                self.assertAlmostEqual(abs(ax-(hx+.5)), .45)
                self.assertAlmostEqual(
                    (ax-(hx+.5))*(bx-hx), -.45,
                    msg="real A must be opposite true B's lateral ray side")
                self.assertEqual(hx//16, math.floor(ax/16))
                self.assertTrue(chosen.split()[4].endswith(".85"),
                                "original fractional vanilla teleport must not re-center")
                self.assertAlmostEqual(abs(az-(hz+.5)), 1.35,
                                       places=6,
                                       msg="actual Minecraft /tp centers integer host")
                self.assertEqual(abs((bz+.5)-(hz+.5)), 2)
                self.assertEqual(abs(bx-hx), 1)
                self.assertEqual(hz//16, math.floor(az/16))
                self.assertEqual(math.floor(az/16), bz//16)
                real_a = math.hypot(ax-(hx+.5), az-(hz+.5))
                real_b = math.hypot(bx-hx, bz-hz)
                self.assertGreater(real_b,
                                   math.hypot(real_a, .9)+.12,
                                   "true original A upward hop must leave B closer")
                self.assertGreaterEqual(real_a-.3, 1.0,
                                        "native A min reach survives drift")
                self.assertNotEqual((ax, az), (bx+.5, bz+.5))
                source_b_ray_x = (bx-hx)*(
                    (abs(az-(hz+.5))-.3)/abs(bz-hz))
                self.assertGreater(abs(source_b_ray_x-(ax-(hx+.5))), .3,
                                   "true 0.6-wide original A must miss native B ray")

    def test_original_v172_real_ray_hit_a_despite_selected_locked_b_regression(self):
        # V172 exact original phase4 observed A index6 under the genuine
        # mapped Mojang objectMouseOver while *selected* B was index1.
        # It must not ever be counted as B controller or server B damage.
        original_ray_hit_index = 6
        original_locked_b_index = 1
        self.assertNotEqual(original_ray_hit_index, original_locked_b_index)
        self.assertFalse(original_ray_hit_index == original_locked_b_index)
        # Positive/reverse real 0.6-wide AABB support: the independent
        # dedicated vanilla coordinate fixture now places A on the
        # opposite lateral side from the ray toward live B.
        for source_x, side, z_direction in (
            (243, 1, 1), (-34, -1, -1)):
            host_x, host_z = source_x + .5, 444.5
            a_x = host_x - side*.45
            a_z = host_z + z_direction*1.35
            b_x = host_x + side
            b_z = host_z + z_direction*2
            # Every sample through the player's Z-hitbox cannot overlap
            # the actual ray's X with this *intended* static fixture.
            for dz in (-.3, 0., .3):
                z_at = a_z + z_direction*dz
                ray_x = host_x+(b_x-host_x)*(
                    (z_at-host_z)/(b_z-host_z))
                self.assertGreater(abs(ray_x-a_x), .3)
            self.assertGreater(
                math.hypot(b_x-host_x,b_z-host_z)
                - math.hypot(a_x-host_x,a_z-host_z), .12)

    def test_real_original_survival_floor_uses_fractional_x_and_z(self):
        script = (Path(__file__).parent /
                  "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        self.assertIn("ax, ay, az=(float(v) for v in ap[2:5])", script)
        self.assertIn("fx, fz=math.floor(ax), math.floor(az)", script)
        self.assertIn("send_server_command(server,server_log,a_rejoined)", script)
        self.assertIn('expected=b"4 blocks filled"', script)

    def test_integer_teleport_centering_was_actual_cause_of_both_bounds(self):
        import math
        # Genuine old integer /tp into a 1-block-forward source cell
        # centers it at only one block from Minecraft host (not 1.5).
        host_z = -136.5
        old_integer_world_z = -135.5
        actual_previous = old_integer_world_z - .34
        self.assertLess(math.hypot(-.43, actual_previous-host_z), 1.0,
                        "observed V159 genuine old A source x/z failed da>=1")
        new_explicit_decimal_z = -135.15
        self.assertAlmostEqual(new_explicit_decimal_z-host_z, 1.35)
        self.assertGreater(math.hypot(-.43,
                                     new_explicit_decimal_z-.34-host_z), 1.0)
        # Historical true 1.5-block fixture had only 0.098 source margin
        # after actual host/player drift in V170. The new real server TP
        # moves A a further .15 toward its logged host, but native 3D
        # bounds remain STRICT and every full real source gate still runs.
        self.assertLess(math.hypot(1.35,.89)+.12, math.sqrt(5.0))
        self.assertGreater(math.hypot(2,.89)+.12, math.sqrt(5.0))

    def test_original_v170_true_source_crash_margin_and_fractional_counterfactual(self):
        import math
        host_x, host_z = -1.20, -1.09
        source_a_x, source_a_z = -0.50, -3.31
        source_b_x, source_b_z = -1.50, -3.50
        observed_a = math.hypot(source_a_x-host_x, source_a_z-host_z)
        observed_b = math.hypot(source_b_x-host_x, source_b_z-host_z)
        self.assertLess(observed_b-observed_a, .12,
                        "actual V170 crash report must fail source closer gate")
        # A 0.15 genuine server spawn shift towards the host preserves
        # the same observed lateral drift yet leaves the original .12
        # distance margin. This is a fixture counterfactual, NOT real
        # original runtime evidence or an authority to accept any click.
        shifted_a_z = source_a_z + .15
        shifted_a = math.hypot(source_a_x-host_x, shifted_a_z-host_z)
        self.assertGreater(observed_b-shifted_a, .12)
        self.assertGreaterEqual(shifted_a, 1.0)
        self.assertLessEqual(shifted_a, 3.25)
        self.assertLessEqual(observed_b, 3.5)

    def test_original_v159_too_close_lower_boundary_remains_positive_with_135(self):
        import math
        # Real original V159 host, A coordinates from crash report.
        hx, hz = -413.50, -136.50
        ax, az = -413.93, -135.84
        original_da = math.hypot(ax-hx, az-hz)
        self.assertLess(original_da, 1.0)
        # The old 1-block vanilla /tp was 0.35 blocks closer than the
        # new dedicated vanilla fraction. Keep 3D native min unchanged.
        shifted_da = math.hypot(ax-hx, az-hz+.35)
        self.assertGreaterEqual(shifted_da, 1.0)

    def test_reject_wrong_uuid_names_source_positions_or_overlap(self):
        valid = ("tp CIHost 243 5 444 0 0",
                 "tp CITarget 243 5 446 180 0",
                 "tp CITargetB 244 5 446 180 0")
        rejects = [
            ("tp Stranger 243 5 444 0 0", valid[1], valid[2]),
            (valid[0], "tp CIHost 243 5 446 180 0", valid[2]),
            (valid[0], valid[1], "tp CITargetB 243 5 446 180 0"),
            (valid[0], valid[1], "tp CITargetB 244 5 445 180 0"),
            (valid[0], "tp CITarget 243 7 446 180 0", valid[2]),
            (valid[0], "tp CITarget 243 5 445 180 0", valid[2]),
        ]
        for h, a, b in rejects:
            with self.subTest(host=h, a=a, b=b):
                with self.assertRaises(AssertionError):
                    actual_server_rejoined_a_controller_fixture(h, a, b)

    def test_original_m419_runtime_rejoin_and_native_controller_stay_mandatory(self):
        script = (Path(__file__).parent /
                  "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        self.assertIn("if args.require_rejoined_controller_b else a_near", script)
        self.assertIn("send_server_command(server, server_log, a_rejoined)", script)
        self.assertIn("ap=a_rejoined.split()", script)
        self.assertIn("send_server_command(server,server_log,a_rejoined)", script)
        for required in ("REJOIN_B_RETAINED", "REJOIN_WRONG_UUID_VETO",
                         "REJOIN_REAL_CONTROLLER_B",
                         "REJOIN_CONTROLLER_ARGUMENT_UUID_B",
                         'wait_for_real_score(server,server_log,"CITargetB",deadline)',
                         "CUSTOMMC_OFFICIAL_189_REJOIN_CONTROLLER_B_SERVER_DAMAGE_PASS"):
            self.assertIn(required, script)


class RealReconnectNativeIdentityFixtureTest(unittest.TestCase):
    def test_real_forward_spawn_keeps_survivor_in_loaded_chunk(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(
                b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n")
            self.assertEqual(actual_server_three_client_rejoin_fixture(path), (
                "tp CIHost 243 5 444 0 0",
                "tp CITarget 243 5 446 180 0",
                "tp CITargetB 244 5 446 180 0",
            ))

    def test_real_reverse_spawn_keeps_survivor_in_loaded_chunk(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(
                b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n")
            self.assertEqual(actual_server_three_client_rejoin_fixture(path), (
                "tp CIHost -34 5 -34 180 0",
                "tp CITarget -34 5 -36 0 0",
                "tp CITargetB -35 5 -36 180 0",
            ))

    def test_native_survivor_has_source_ray_margin_and_distinct_hitbox(self):
        import math
        for raw in (
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n",
        ):
            with tempfile.TemporaryDirectory() as root:
                log = Path(root) / "server.log"
                log.write_bytes(raw)
                host, a, b = actual_server_three_client_rejoin_fixture(log)
                hx, hy, hz = map(int, host.split()[2:5])
                ax, ay, az = map(int, a.split()[2:5])
                bx, by, bz = map(int, b.split()[2:5])
                self.assertEqual(hy, ay)
                self.assertEqual(ay, by)
                self.assertEqual(ax, hx)
                self.assertEqual(abs(az-hz), 2)
                self.assertEqual(abs(ax-bx), 1,
                                 "original player A and B hitboxes stay distinct")
                self.assertEqual(bz, az)
                self.assertEqual(math.hypot(ax-hx, az-hz), 2)
                self.assertEqual(math.hypot(bx-hx, bz-hz), math.sqrt(5))
                self.assertLess(math.hypot(bx-hx, bz-hz), 3)
                self.assertNotEqual((ax, az), (bx, bz))
                self.assertEqual(hx // 16, bx // 16)
                self.assertEqual(hz // 16, bz // 16)

    def test_server_spawn_unknown_cannot_fake_rejoin_fixture(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "server.log"
            path.write_bytes(b"CITarget left the game\nCITarget joined the game\n")
            with self.assertRaises(AssertionError):
                actual_server_three_client_rejoin_fixture(path)


if __name__ == "__main__":
    unittest.main()
