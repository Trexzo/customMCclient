"""Offline regression checks for the genuine-server spawn-chunk test fixture."""
import tempfile
from pathlib import Path
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_spawn_fixture,
    actual_server_spawn_fov_fixture,
    actual_server_fov_ground_support_fixture,
    actual_server_spawn_wall_fixture,
    actual_server_three_client_target_switch_fixture,
    actual_server_three_client_simultaneous_fixture,
    actual_server_three_client_locked_fixture,
    actual_server_three_client_rejoin_fixture,
    actual_server_rejoined_a_controller_fixture,
    actual_server_original_rejoin_prelock_a_fixture,
    actual_server_rejoined_host_recenter_fixture,
    actual_server_rejoined_phase4_grounded_floor_fixture,
    wait_for_new_original_source_marker,
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
             "tp CITarget 255 5 446 180 0"),
        )

    def test_native_reverse_spawn_sticky_then_range_release(self):
        self.fixture(
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n",
            ("tp CIHost -34 5 -34 180 0",
             "tp CITarget -34 5 -36 0 0",
             "tp CITargetB -36 5 -36 180 0",
             "tp CITargetB -35 5 -35 180 0",
             "tp CITarget -22 5 -36 180 0"),
        )

    def test_real_login_missing_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            log = Path(directory) / "server.log"
            log.write_bytes(b"CITargetB joined the game\n")
            with self.assertRaises(AssertionError):
                actual_server_three_client_locked_fixture(log)


class OriginalM419GroundedNativeThreePlayerFixtureTest(unittest.TestCase):
    def test_actual_server_ground_floor_covers_all_original_three_positions(self):
        import math
        for login in (
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n",
        ):
            with tempfile.TemporaryDirectory() as root:
                log=Path(root)/"server.log"
                log.write_bytes(login)
                host,a,b=actual_server_three_client_rejoin_fixture(log)
                new_a=actual_server_rejoined_a_controller_fixture(host,a,b)
                cmd=actual_server_rejoined_phase4_grounded_floor_fixture(
                    host,new_a,b)
                parts=cmd.split()
                self.assertEqual(len(parts),8)
                self.assertEqual(parts[0],"fill")
                self.assertEqual(parts[7],"stone")
                lx,ly,lz,hx,hy,hz=map(int,parts[1:7])
                self.assertEqual(ly,hy)
                self.assertEqual(ly,int(host.split()[3])-1)
                host_x,host_z=int(host.split()[2]),int(host.split()[4])
                b_x,b_z=int(b.split()[2]),int(b.split()[4])
                a_x=math.floor(float(new_a.split()[2]))
                a_z=math.floor(float(new_a.split()[4]))
                for x,z in ((host_x,host_z),(b_x,b_z),
                            (a_x,a_z),(a_x+1,a_z+1)):
                    self.assertLessEqual(lx,x)
                    self.assertLessEqual(x,hx)
                    self.assertLessEqual(lz,z)
                    self.assertLessEqual(z,hz)
                self.assertGreater((hx-lx+1)*(hz-lz+1),4,
                                   "real original A-only 2x2 stone floor is insufficient")

    def test_native_m419_real_run_ground_floor_expected_actual_server_positions(self):
        cmd=actual_server_rejoined_phase4_grounded_floor_fixture(
            "tp CIHost 911 5 862 180 0",
            "tp CITarget 911.95 5 861.15 0 0",
            "tp CITargetB 910 5 860 0 0")
        self.assertEqual(cmd,"fill 907 4 857 915 4 865 stone")
        # Strict B ray and UUID geometry are unchanged from V175/V181.
        self.assertEqual(actual_server_rejoined_host_recenter_fixture(
            "tp CIHost 911 5 862 180 0",
            "tp CITarget 911.95 5 861.15 0 0",
            "tp CITargetB 910 5 860 0 0"),
            "tp CIHost 911 5 862 180 0")

    def test_invalid_original_rejoin_geometry_rejected_before_ground_floor(self):
        good_host="tp CIHost 911 5 862 180 0"
        good_b="tp CITargetB 910 5 860 0 0"
        for wrong_a in (
            "tp CITarget 911.05 5 861.15 0 0",
            "tp CITarget 911.95 7 861.15 0 0",
            "tp CITarget 911.95 5 862.15 0 0",
            "tp CITargetB 911.95 5 861.15 0 0",
        ):
            with self.assertRaises(AssertionError):
                actual_server_rejoined_phase4_grounded_floor_fixture(
                    good_host,wrong_a,good_b)

    def test_real_stone_ack_slowness_then_one_b_a_host_and_20_native_samples(self):
        src=(Path(__file__).parent /
             "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        begin=src.index("grounded_floor = actual_server_rejoined_phase4_grounded_floor_fixture")
        ack=src.index('expected=b"blocks filled"',begin)
        native_stone=src.index('"testforblock {} {} {} stone"',ack)
        slow=src.index('"effect CITargetB 2 600 5 true"',native_stone)
        b=src.index("send_server_command(server,server_log,b_outer",slow)
        a=src.index("send_server_command(server,server_log,a_rejoined",b)
        host=src.index("send_server_command(server,server_log,phase4_host",a)
        original=src.index("wait_for_new_original_source_marker(",host)
        score=src.index('"scoreboard players set CITargetB taken 0"',original)
        self.assertTrue(0<begin<ack<native_stone<slow<b<a<host<original<score)
        self.assertEqual(src[begin:host].count(
            "send_server_command(server,server_log,b_outer"),1)
        self.assertEqual(src[begin:host].count(
            "send_server_command(server,server_log,a_rejoined"),1)
        self.assertIn('expected=b"Teleported CITargetB to"',src[b:a])
        self.assertIn('expected=b"Teleported CITarget to"',src[a:host])
        self.assertIn("CUSTOMMC_189_M419_NATIVE_B_WIDE_PLATFORM_RESEAT_PASS=YES",
                      src[b:host])

    def test_true_v199_original_b_native_drift_lands_on_new_stone_not_old(self):
        import math
        original_x, original_z = -595.59375, -309.09375
        observed_y = 4.0
        tp_host="tp CIHost -598 5 -312 180 0"
        tp_a="tp CITarget -597.95 5 -310.15 0 0"
        tp_b="tp CITargetB -597 5 -310 0 0"
        # The original logged B /tp was -596.5,-309.5 (integer
        # -597,-310). Its later native position was outside 3x3 floor.
        cmd=actual_server_rejoined_phase4_grounded_floor_fixture(
            tp_host,tp_a,tp_b)
        chunks=cmd.split()
        lx,ly,lz,hx,hy,hz=map(int,chunks[1:7])
        self.assertEqual(ly,4)
        self.assertEqual(hy,4)
        self.assertEqual(int(observed_y),ly)
        self.assertTrue(lx<=math.floor(original_x)<=hx)
        self.assertTrue(lz<=math.floor(original_z)<=hz)
        # Previous narrow planned-only floor would exclude the real B.
        self.assertGreater(math.floor(original_x),max(-598,-597,-597))

    def test_exact_v196_v197_b_outside_native_reach_still_fails_closed(self):
        for da,db in (
            (1.7891839931361735,3.960637161313068),
            (1.7580159040626773,3.9382332372799493),
            (1.4231621920919626,3.228595379572981)):
            self.assertLess(da,db)
        acceptance=(Path(__file__).parent.parent /
                    "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform"
                    "/v1_8_9/Minecraft189RejoinTargetAcceptance.java").read_text()
        self.assertIn('return "survivor-B-outside-original-[1,3.5]-reach"',acceptance)
        self.assertIn("survivorUuid.equals(aura.lockedTargetUuid())",acceptance)
        self.assertIn("m419MustVetoPendingSyntheticClick(",acceptance)

    def test_original_controller_strict_three_dimensional_margin_not_weakened(self):
        acceptance=(Path(__file__).parent.parent /
                    "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform"
                    "/v1_8_9/Minecraft189RejoinTargetAcceptance.java").read_text()
        self.assertIn("if (!(da + 0.12D < db))",acceptance)
        self.assertIn("db - da >= 0.30D",acceptance)
        self.assertIn("verifyRealControllerB(",acceptance)
        self.assertIn("m419NativeCoordinateDiagnostics(host,e)",acceptance)
        self.assertIn("originalNativeHostXYZ",acceptance)


class OriginalM419SourceNativePhaseFourSyntheticVetoTest(unittest.TestCase):
    def test_original_m419_veto_occurs_before_any_real_aura_synthetic_dispatch(self):
        src=(Path(__file__).parent.parent /
             "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform"
             "/v1_8_9/Minecraft189RuntimeBridge.java").read_text()
        start=src.index("vetoTransientUnownedPhaseFourSyntheticClick(")
        schedule=src.index("if (host.shouldAutoClick(",start)
        owned=src.index("Minecraft189RejoinTargetAcceptance.verifyOwnedClick(",schedule)
        self.assertTrue(start < schedule < owned)
        self.assertIn("host, playerHit, playerIndex)) return;",src[start:schedule],
                      "no wrong source UUID may issue synthetic packet/controller")
        self.assertIn("verifyRealControllerB(",(Path(__file__).parent.parent /
            "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform"
            "/v1_8_9/Minecraft189RejoinTargetAcceptance.java").read_text())

    def test_only_original_opt_in_phase4_and_strict_positive_original_controller(self):
        src=(Path(__file__).parent.parent /
             "platform-1.8.9/src/main/java/dev/trexzo/custommc/platform"
             "/v1_8_9/Minecraft189RejoinTargetAcceptance.java").read_text()
        begin=src.index("static boolean vetoTransientUnownedPhaseFourSyntheticClick(")
        end=src.index("static void verifyRealControllerB(",begin)
        sub=src[begin:end]
        self.assertIn('Boolean.getBoolean("custommc.acceptance.reportKillAuraRejoin")',sub)
        self.assertIn('Boolean.getBoolean("custommc.acceptance.requireRejoinedControllerB")',sub)
        self.assertIn("phase != 4",sub)
        self.assertIn("m419SourceRejection(e, aura, true)",sub)
        self.assertIn("originalPlayerHit && originalHitIndex == e.b",sub)
        self.assertIn("return true;",sub)
        self.assertNotIn("CUSTOMMC_189_M419_NATIVE_PHASE4_SOURCE_STABLE=20",sub)
        self.assertNotIn("CUSTOMMC_189_REJOIN_REAL_B_NATIVE_CONTROLLER_UUID_PASS=YES",sub)
        self.assertIn("if (!(da + 0.12D < db))",src)
        self.assertIn("db - da >= 0.30D",src)


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
        import math
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
        self.assertIn("send_server_command(server,server_log,a_rejoined,", script)
        self.assertIn('expected=b"Teleported CITarget to"', script)
        # An original idempotent no-change receipt is never enough: the
        # four physically present vanilla stone cells must each be confirmed.
        self.assertIn('phase4_floor_reply, expected_count=4', script)
        self.assertIn("for dx in (0, 1):", script)
        self.assertIn("for dz in (0, 1):", script)
        self.assertIn('"testforblock {} {} {} stone"', script)

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
        self.assertIn("send_server_command(server,server_log,a_rejoined,", script)
        self.assertIn('expected=b"Teleported CITarget to"', script)
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


class OriginalLateFixtureGroundingAndHostLeaseTest(unittest.TestCase):
    def test_real_fov_support_positive_and_reverse_chunk(self):
        for login in (
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n"):
            with tempfile.TemporaryDirectory() as root:
                path = Path(root) / "server.log"
                path.write_bytes(login)
                host, target = actual_server_spawn_fov_fixture(path)
                air, stone = actual_server_fov_ground_support_fixture(host, target)
                self.assertEqual(air[:-4], stone[:-6])
                p = air.split()
                self.assertEqual(p[0], "fill")
                x0,y0,z0,x1,y1,z1 = map(int, p[1:7])
                self.assertEqual((x1-x0+1)*(y1-y0+1)*(z1-z0+1),15)
                self.assertEqual(y0, int(host.split()[3])-1)
                self.assertEqual(z1-z0,4)
                self.assertEqual(x1-x0,2)

    def test_original_fov_support_rejects_malformed_or_divergent_world(self):
        host = "tp CIHost -36 5 -36 0 0"
        target = "tp CITarget -36 5 -34 180 0"
        self.assertEqual(actual_server_fov_ground_support_fixture(
            host, target)[1], "fill -37 4 -37 -35 4 -33 stone")
        for a,b in (
            (host,"tp CITarget -36 5 -32 180 0"),
            (host,"tp CITarget -35 5 -34 180 0"),
            (host,"tp CITarget -36 6 -34 180 0"),
            ("tp CIHost -36 0 -36 0 0",target),
            (host,"tp BOGUS -36 5 -34 180 0")
        ):
            with self.assertRaises(AssertionError):
                actual_server_fov_ground_support_fixture(a,b)

    def test_v175_actual_fov_vertical_separation_is_not_valid_reach(self):
        # Exact V175 late native source: horizontal 2.0 but real 3D 5.85235.
        import math
        observed=5.852349955359813
        self.assertGreater(observed,3.5)
        self.assertAlmostEqual(math.sqrt(observed*observed-4),5.5,delta=.15)
        # Pure source fixture does NOT authorize a client attack.
        script=(Path(__file__).parent /
                "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        self.assertIn('wait_for(host, host_log, FOV_DISABLED, deadline,',script)
        self.assertIn('wait_for(host, host_log, FOV_VETO, deadline,',script)
        self.assertIn('expected=b"15 blocks filled"',script)
        self.assertIn('expected=b"blocks filled"',script)

    def test_v176_original_host_drift_fails_uncorrected_closer_bound(self):
        import math
        host=(-1.20,-.09)
        a=(-.06,-2.15)
        b=(-1.5,-2.5)
        actual_a=math.hypot(a[0]-host[0],a[1]-host[1])
        actual_b=math.hypot(b[0]-host[0],b[1]-host[1])
        self.assertLess(actual_b-actual_a,.12)
        # This is only a counterfactual ideal static fixture, NOT a
        # replacement for genuine World-source M419 acceptance.
        ideal_host=(-.5,-.5)
        ideal_a=math.hypot(a[0]-ideal_host[0],a[1]-ideal_host[1])
        ideal_b=math.hypot(b[0]-ideal_host[0],b[1]-ideal_host[1])
        self.assertGreater(ideal_b-ideal_a,.12)

    def test_m419_recenter_checks_real_fractional_a_opposite_b(self):
        for login in (
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n"):
            with tempfile.TemporaryDirectory() as root:
                p=Path(root)/"server.log"
                p.write_bytes(login)
                host,a,b=actual_server_three_client_rejoin_fixture(p)
                relocated=actual_server_rejoined_a_controller_fixture(host,a,b)
                self.assertEqual(actual_server_rejoined_host_recenter_fixture(
                    host,relocated,b),host)
                a_bad=relocated.replace("CITarget","CITargetB")
                with self.assertRaises(AssertionError):
                    actual_server_rejoined_host_recenter_fixture(host,a_bad,b)
                with self.assertRaises(AssertionError):
                    actual_server_rejoined_host_recenter_fixture(
                        host,relocated.split()[0] + " " + relocated.split()[1]
                        + " " + relocated.split()[2] + " " + relocated.split()[3]
                        + " 999.99 " + relocated.split()[5] + " " + relocated.split()[6],b)

    def test_original_host_recenter_precedes_fresh_damage_and_keeps_native_gates(self):
        script=(Path(__file__).parent /
                "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        recenter=script.find('expected=b"Teleported CIHost to"')
        reset=script.find('"scoreboard players set CITargetB taken 0"',recenter)
        native=script.find('wait_for(host,host_log,REJOIN_REAL_CONTROLLER_B',reset)
        self.assertGreaterEqual(recenter,0)
        self.assertTrue(recenter < reset < native)
        self.assertIn('wait_for_real_score(server,server_log,"CITargetB"',script)
        self.assertIn('REJOIN_CONTROLLER_ARGUMENT_UUID_B',script)


class OriginalM419PostTeleportWorldConvergenceTest(unittest.TestCase):
    def test_only_fresh_original_stdout_after_teleport_offset_counts(self):
        import time
        class Living:
            def poll(self):
                return None
        marker=b"CUSTOMMC_189_M419_NATIVE_PHASE4_SOURCE_STABLE=20"
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"real-original-mojang-client.log"
            p.write_bytes(marker+b"\n")
            old_end=p.stat().st_size
            with self.assertRaisesRegex(AssertionError,"missing fresh original"):
                wait_for_new_original_source_marker(
                    Living(),p,old_end,marker,time.monotonic()+.025,"M419")
            with p.open("ab") as out:
                out.write(b"actual client observed "+marker+b"\n")
            wait_for_new_original_source_marker(
                Living(),p,old_end,marker,time.monotonic()+.5,"M419")

    def test_fail_closed_on_missing_or_invalid_original_evidence(self):
        import time
        class Living:
            def poll(self):
                return None
        with tempfile.TemporaryDirectory() as root:
            p=Path(root)/"original.log"
            p.write_bytes(b"")
            for offset,marker in ((-1,b"sample"),(0,b""),(0,"string")):
                with self.assertRaises(AssertionError):
                    wait_for_new_original_source_marker(
                        Living(),p,offset,marker,time.monotonic()+.5,"M419")
            class Exited:
                def poll(self):
                    return 255
            with self.assertRaisesRegex(AssertionError,"process exited early"):
                wait_for_new_original_source_marker(
                    Exited(),p,0,b"marker",time.monotonic()+.5,"M419")

    def test_actual_native_fence_precedes_real_pvp_and_server_damage(self):
        text=(Path(__file__).parent /
              "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        start=text.find("original_source_start = host_log.stat().st_size")
        source=text.find("wait_for_new_original_source_marker(",start)
        ready=text.find("CUSTOMMC_189_M419_LIVE_SOURCE_STABLE_BEFORE_B_DAMAGE",source)
        reset=text.find('"scoreboard players set CITargetB taken 0"',ready)
        survivor=text.find('send_server_command(server,server_log,"gamemode 0 CITargetB")',reset)
        self.assertTrue(0 <= start < source < ready < reset < survivor)
        self.assertIn("REJOIN_REAL_CONTROLLER_B",text[survivor:])
        self.assertIn('wait_for_real_score(server,server_log,"CITargetB"',text[survivor:])

    def test_v176_v178_original_crashes_still_fail_original_controller_gate(self):
        for da,db in ((2.3491730716924386,2.4281797437686015),
                      (2.3400644431964785,2.4281797437686015)):
            self.assertLess(db-da,.12)
        source=(Path(__file__).parents[1] /
                "platform-1.8.9/src/main/java/dev/trexzo/custommc/"
                "platform/v1_8_9/Minecraft189RejoinTargetAcceptance.java").read_text()
        self.assertIn("if (!(da + 0.12D < db))",source)
        self.assertIn("&& db - da >= 0.30D;",source)


class OriginalM419InitialPrelockRealSourceMarginTest(unittest.TestCase):
    def test_real_source_prelock_moves_only_original_a_for_two_chunk_signs(self):
        import math
        for login in (
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (243.5, 4.0, 444.5)\n",
            b"CIHost[/127.0.0.1:42110] logged in with entity id 5 at (-33.5, 4.0, -33.5)\n"):
            with tempfile.TemporaryDirectory() as root:
                p=Path(root)/"original-server.log"
                p.write_bytes(login)
                host,a,b=actual_server_three_client_rejoin_fixture(p)
                moved=actual_server_original_rejoin_prelock_a_fixture(host,a,b)
                self.assertEqual(moved.split()[0:2],["tp","CITarget"])
                self.assertEqual(moved.split()[3],a.split()[3])
                self.assertEqual(moved.split()[5:],a.split()[5:])
                hx,hy,hz=(int(v) for v in host.split()[2:5])
                ax,ay,az=(float(v) for v in moved.split()[2:5])
                bx,by,bz=(int(v) for v in b.split()[2:5])
                self.assertAlmostEqual(ax,hx+.5)
                self.assertAlmostEqual(abs(az-(hz+.5)),1.55)
                self.assertEqual(math.floor(ax/16),hx//16)
                self.assertEqual(math.floor(az/16),hz//16)
                self.assertAlmostEqual(math.hypot(bx-hx,bz-hz),math.sqrt(5))
                self.assertGreater(math.sqrt(5)-(1.55+.30),.12)
                self.assertGreater(1.55-.30,1.0)

    def test_true_v177_initial_prelock_observation_lacked_source_margin(self):
        da=2.2222683371951284
        db=2.23606797749979
        self.assertLess(db-da,.12)
        self.assertGreater(da,1)
        self.assertLess(db,3.5)
        # Selection alone does not satisfy 20 original A-preferred reads.
        source=(Path(__file__).parents[1] /
                "platform-1.8.9/src/main/java/dev/trexzo/custommc/"
                "platform/v1_8_9/Minecraft189RejoinTargetAcceptance.java").read_text()
        self.assertIn("da+0.12D<db && selected(a)",source)

    def test_original_a_prelock_rejects_wrong_uuid_or_chunk(self):
        host="tp CIHost 15 5 3 0 0"
        a="tp CITarget 15 5 5 180 0"
        b="tp CITargetB 16 5 5 180 0"
        with self.assertRaisesRegex(AssertionError,"chunk"):
            actual_server_original_rejoin_prelock_a_fixture(host,a,b)
        for broken in ("tp NOT_A 15 5 5 180 0",
                       "tp CITarget 13 5 5 180 0",
                       "tp CITarget 15 5 9 180 0"):
            with self.assertRaises(AssertionError):
                actual_server_original_rejoin_prelock_a_fixture(
                    "tp CIHost 14 5 3 0 0",broken,"tp CITargetB 15 5 5 180 0")

    def test_original_server_rejoin_stage_keeps_b_last_and_native_gates(self):
        source=(Path(__file__).parent /
                "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        start=source.find("prelock_a=actual_server_original_rejoin_prelock_a_fixture(")
        a_tp=source.find("send_server_command(server, server_log, prelock_a,",start)
        b_tp=source.find("send_server_command(server, server_log, b_outer,",a_tp)
        b_ack=source.find("expected=original_locked_tp_server_reply(b_outer)",b_tp)
        ready=source.find("wait_for(host, host_log, REJOIN_READY,",b_ack)
        self.assertTrue(0<=start<a_tp<b_tp<b_ack<ready)
        self.assertIn("actual_server_rejoined_a_controller_fixture(",source[ready:])

if __name__ == "__main__":
    unittest.main()
