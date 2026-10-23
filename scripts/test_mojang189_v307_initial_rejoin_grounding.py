"""Source-only fixture checks. Never count these as real Mojang gameplay."""
import importlib
import pathlib
import sys
import unittest

SCRIPTS = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(SCRIPTS))
smoke = importlib.import_module("RunMojang189LocalhostTwoPlayerSmoke")


class OriginalV307InitialGrounding(unittest.TestCase):
    HOST = "tp CIHost 100 64 100 0 0"
    A = "tp CITarget 100 64 102 180 0"
    B = "tp CITargetB 101 64 102 180 0"

    def test_genuine_three_player_columns_and_real_stone_plane(self):
        fill, columns = smoke.actual_server_initial_rejoin_grounded_floor_fixture(
            self.HOST, self.A, self.B)
        self.assertEqual("fill 97 63 97 104 63 105 stone", fill)
        self.assertEqual(((100, 63, 100), (100, 63, 102), (101, 63, 102)),
                         columns)
        self.assertTrue(all(y == 63 for _, y, _ in columns))

    def test_both_axis_rejoin_geometry_and_intended_ranges(self):
        self.assertEqual(
            3,
            len(smoke.actual_server_initial_rejoin_grounded_floor_fixture(
                self.HOST, self.A, self.B)[1]))

    def test_rejects_mismatched_original_player_identity(self):
        for bad in ("tp Other 101 64 102 180 0",
                    "tp CITargetB 101 64 102",
                    "tp CITargetB 101.5 64 102 180 0"):
            with self.subTest(bad=bad):
                with self.assertRaises(AssertionError):
                    smoke.actual_server_initial_rejoin_grounded_floor_fixture(
                        self.HOST, self.A, bad)

    def test_rejects_unstable_y_or_unreachable_b(self):
        for bad in ("tp CITargetB 101 63 102 180 0",
                    "tp CITargetB 101 64 112 180 0",
                    "tp CITargetB 100 64 102 180 0",
                    "tp CITargetB 120 64 102 180 0"):
            with self.subTest(bad=bad):
                with self.assertRaises(AssertionError):
                    smoke.actual_server_initial_rejoin_grounded_floor_fixture(
                        self.HOST, self.A, bad)

    def test_rejects_cross_chunk_and_world_height(self):
        with self.assertRaises(AssertionError):
            smoke.actual_server_initial_rejoin_grounded_floor_fixture(
                "tp CIHost 15 64 4 0 0",
                "tp CITarget 15 64 6 180 0",
                "tp CITargetB 16 64 6 180 0")
        with self.assertRaises(AssertionError):
            smoke.actual_server_initial_rejoin_grounded_floor_fixture(
                "tp CIHost 100 1 100 0 0",
                "tp CITarget 100 1 102 180 0",
                "tp CITargetB 101 1 102 180 0")

    def test_workflow_keeps_real_original_phase_and_timeout_requirements(self):
        source = (SCRIPTS / "RunMojang189LocalhostTwoPlayerSmoke.py").read_text()
        section = source.split("if args.rejoin_target or args.rejoin_release:", 2)[-1]
        self.assertIn("actual_server_initial_rejoin_grounded_floor_fixture(", section)
        self.assertIn("expected=b\"blocks filled\"", section)
        self.assertIn("expected=b\"successfully found the block\"", section)
        self.assertIn("expected=b\"Teleported CITargetB to\"", section)
        self.assertIn("wait_for(host, host_log, REJOIN_READY, deadline,", section)
        self.assertLess(
            section.find("CUSTOMMC_189_REJOIN_INITIAL_THREE_PLAYER_STONE_PASS"),
            section.find("send_server_command(server, server_log, host_tp)"))
        self.assertLess(
            section.find("expected=b\"Teleported CITargetB to\""),
            section.find("wait_for(host, host_log, REJOIN_READY, deadline,"))
        workflow = (SCRIPTS.parent / ".github/workflows/minecraft-189-real-client.yml").read_text()
        self.assertIn("--timeout 235 --rejoin-release --require-index-reorder", workflow)
        self.assertIn("python -m unittest discover -s scripts -p 'test_mojang189_*.py' -v", workflow)


if __name__ == "__main__":
    unittest.main()
