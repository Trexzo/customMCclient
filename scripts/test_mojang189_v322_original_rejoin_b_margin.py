"""V322: genuine server B buffer after the immutable V320 M419 red run.

Original #40196757559 attempt 2: native A=1.335000585, B=1.481988213
gave B-A=0.146987627 despite correct native UUID and crosshair. A server
tp is not native evidence and MUST NOT replace stable original World ticks.
"""
import math
from pathlib import Path
import unittest

from RunMojang189LocalhostTwoPlayerSmoke import (
    actual_server_rejoined_b_margin_anchor_fixture as anchor,
    actual_server_rejoined_phase4_grounded_floor_fixture as floor,
)

ROOT = Path(__file__).parents[1]
HARNESS = ROOT / "scripts" / "RunMojang189LocalhostTwoPlayerSmoke.py"
NATIVE = (ROOT / "platform-1.8.9" / "src" / "main" / "java"
          / "dev" / "trexzo" / "custommc" / "platform" / "v1_8_9"
          / "Minecraft189RejoinTargetAcceptance.java")

CASES = (
    ("tp CIHost 911 5 862 180 0",
     "tp CITarget 911.95 5 861.15 0 0",
     "tp CITargetB 910 5 860 0 0"),
    ("tp CIHost 0 5 -2 180 0",
     "tp CITarget 0.05 5 -2.85 0 0",
     "tp CITargetB 1 5 -4 0 0"),
)


class V322GenuineOriginalBMarginBuffer(unittest.TestCase):
    def test_exact_native_failed_0_147_gap_cannot_satisfy_original_0_30(self):
        a = 1.3350005852058642
        b = 1.481988212672422
        self.assertLess(b - a, 0.30)
        self.assertGreater(b - a, 0.12)

    def test_only_real_survivor_b_target_moves(self):
        for host, a, b in CASES:
            with self.subTest(host=host):
                result = anchor(host, a, b).split()
                self.assertEqual(result[0:2], ["tp", "CITargetB"])
                self.assertEqual(result[3], b.split()[3])
                self.assertEqual(result[5:], b.split()[5:])

    def test_b_increases_3d_margin_without_exiting_native_reach(self):
        for host, a, b in CASES:
            h, v, t = host.split(), a.split(), anchor(host, a, b).split()
            hx, hz = int(h[2]) + .5, int(h[4]) + .5
            da = math.hypot(float(v[2]) - hx, float(v[4]) - hz)
            db = math.hypot(float(t[2]) - hx, float(t[4]) - hz)
            with self.subTest(host=host):
                self.assertGreater(db, 2.7)
                self.assertLess(db, 3.5)
                self.assertGreater(db - da, .75)

    def test_b_native_offset_bounded_to_half_a_block(self):
        for host, a, b in CASES:
            original, target = b.split(), anchor(host, a, b).split()
            bx, bz = int(original[2]) + .5, int(original[4]) + .5
            self.assertAlmostEqual(
                math.hypot(float(target[2])-bx,float(target[4])-bz),
                .5, delta=.015)

    def test_anchor_position_stays_on_real_server_stone(self):
        for host, a, b in CASES:
            extents = [int(n) for n in floor(host, a, b).split()[1:7]]
            t = anchor(host, a, b).split()
            x, z = math.floor(float(t[2])), math.floor(float(t[4]))
            self.assertLessEqual(extents[0],x)
            self.assertLessEqual(x,extents[3])
            self.assertLessEqual(extents[2],z)
            self.assertLessEqual(z,extents[5])
            self.assertEqual(extents[1],int(t[3])-1)

    def test_reject_unreviewed_original_uuid_commands(self):
        host, a, b = CASES[0]
        bad = (b.replace("CITargetB","CITarget"),
               b.replace("tp ","give ",1),
               "tp CITargetB 900 5 860 0 0",
               "tp CITargetB 910 5 900 0 0")
        for wrong in bad:
            with self.subTest(wrong=wrong):
                with self.assertRaises(AssertionError):
                    anchor(host, a, wrong)

    def test_reject_nonfinite_original_a_source(self):
        host, a, b = CASES[0]
        with self.assertRaises(AssertionError):
            anchor(host, a.replace("911.95","nan"), b)

    def test_runner_uses_genuine_server_tp_and_real_stone_witness(self):
        src=HARNESS.read_text()
        start=src.index("b_anchor = actual_server_rejoined_b_margin_anchor_fixture(")
        end=src.index('send_server_command(server,server_log,"gamemode 0 CITarget")',start)
        segment=src[start:end]
        self.assertIn("send_server_command(server,server_log,b_anchor,",segment)
        self.assertIn('expected=b"Teleported CITargetB to"',segment)
        self.assertIn('"testforblock {} {} {} stone"',segment)
        self.assertIn('expected=b"successfully found the block"',segment)
        self.assertNotIn("PASS=YES",segment)
        self.assertNotIn("Minecraft189RejoinTargetAcceptance",segment)

    def test_existing_original_rejoin_gates_stay_in_order(self):
        src=HARNESS.read_text()
        offset=src.index("grounded_floor = actual_server_rejoined_phase4_grounded_floor_fixture")
        order=('send_server_command(server,server_log,grounded_floor,',
               'send_server_command(server,server_log,b_outer,',
               'send_server_command(server,server_log,b_anchor,',
               'send_server_command(server,server_log,a_rejoined,',
               'send_server_command(server,server_log,phase4_host,',
               'wait_for_new_original_source_marker(',
               '"scoreboard players set CITargetB taken 0"',
               '"scoreboard players test CITargetB taken 0 0"')
        positions=[src.index(term,offset) for term in order]
        self.assertEqual(positions, sorted(positions))
        self.assertEqual(src.count('send_server_command(server,server_log,b_outer,'),1)

    def test_original_native_20_sample_and_strict_3d_limits_unchanged(self):
        native=NATIVE.read_text()
        script=HARNESS.read_text()
        for phrase in ("m419NativeStableSourceTicks >= 20",
                       "db - da >= 0.30D",
                       "if (!(da + 0.12D < db))",
                       "verifyRealControllerB("):
            self.assertIn(phrase,native)
        self.assertIn('b"CUSTOMMC_189_M419_NATIVE_PHASE4_SOURCE_STABLE=20"',script)

    def test_reject_real_world_edge_out_of_bounds(self):
        h,a,b=CASES[0]
        for shift in (3000,-3000):
            # A/B/host translated identically: source geometry remains
            # valid but the new B anchor must be rejected at the border.
            def move(command):
                t=command.split()
                for col in (2,4):
                    value = float(t[col]) + shift
                    t[col] = str(int(value)) if "." not in t[col] else str(value)
                return " ".join(t)
            with self.assertRaises(AssertionError):
                anchor(move(h),move(a),move(b))


if __name__ == "__main__":
    unittest.main()
