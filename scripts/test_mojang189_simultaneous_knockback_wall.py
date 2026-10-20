"""V265 actual server stone wall behind real A cannot spoof native PvP."""
import unittest
from RunMojang189LocalhostTwoPlayerSmoke import actual_server_simultaneous_a_knockback_wall_fixture as fixture

H="tp CIHost 243 5 444 0 0"
A="tp CITarget 243 5 446 180 0"
B="tp CITargetB 245 5 446 180 0"
N="tp CITargetB 244 5 445 180 0"

class OriginalSimultaneousKnockbackWallTest(unittest.TestCase):
    def test_positive_target_native_wall_exact_nine_blocks(self):
        wall, checks=fixture(H,A,B,N)
        self.assertEqual(wall,"fill 242 5 447 244 7 447 stone")
        self.assertEqual(checks,((242,5,447),(243,6,447),(244,7,447)))

    def test_negative_z_and_negative_x_wall(self):
        wall, checks=fixture("tp CIHost -34 5 -34 180 0",
            "tp CITarget -34 5 -36 0 0",
            "tp CITargetB -36 5 -36 180 0",
            "tp CITargetB -35 5 -35 180 0")
        self.assertEqual(wall,"fill -35 5 -37 -33 7 -37 stone")
        self.assertEqual(checks[1],(-34,6,-37))

    def test_exact_failed_original_source_preserves_b_tile(self):
        wall, checks=fixture("tp CIHost -440 5 -288 180 0",
            "tp CITarget -440 5 -290 0 0",
            "tp CITargetB -438 5 -290 180 0",
            "tp CITargetB -439 5 -289 180 0")
        self.assertEqual(wall,"fill -441 5 -291 -439 7 -291 stone")
        self.assertNotIn("-438 ",wall)
        self.assertEqual(checks[1],(-440,6,-291))

    def test_wall_is_behind_real_a_not_in_host_ray(self):
        _wall,checks=fixture(H,A,B,N)
        self.assertTrue(all(z>446 for x,y,z in checks))
        self.assertEqual([v.split()[1] for v in (H,A,B,N)],
            ["CIHost","CITarget","CITargetB","CITargetB"])

    def test_invalid_player_owner_rejected(self):
        with self.assertRaisesRegex(AssertionError,"ownership"):
            fixture(H,A.replace("CITarget","Impostor"),B,N)

    def test_missing_teleport_parts_rejected(self):
        with self.assertRaisesRegex(AssertionError,"four genuine"):
            fixture(H,A,B,N.rsplit(" ",1)[0])

    def test_non_integer_original_source_rejected(self):
        with self.assertRaisesRegex(AssertionError,"nonintegral"):
            fixture(H.replace("243 ","243.25 "),A,B,N)

    def test_wrong_host_a_distance_rejected(self):
        with self.assertRaisesRegex(AssertionError,"geometry"):
            fixture(H,A.replace("446","445"),B,N)

    def test_b_outer_must_be_two_live_diagonal_blocks(self):
        with self.assertRaisesRegex(AssertionError,"geometry"):
            fixture(H,A,B.replace("245","247"),N)

    def test_near_b_must_have_real_distinct_position(self):
        with self.assertRaisesRegex(AssertionError,"geometry"):
            fixture(H,A,B,N.replace("244","243"))

    def test_world_height_guard(self):
        with self.assertRaisesRegex(AssertionError,"height"):
            fixture(H.replace(" 5 "," 254 "),A.replace(" 5 "," 254 "),
                B.replace(" 5 "," 254 "),N.replace(" 5 "," 254 "))

    def test_world_border_guard(self):
        with self.assertRaisesRegex(AssertionError,"out of world"):
            fixture("tp CIHost 29999989 5 444 0 0",
                "tp CITarget 29999989 5 446 180 0",
                "tp CITargetB 29999991 5 446 180 0",
                "tp CITargetB 29999990 5 445 180 0")
