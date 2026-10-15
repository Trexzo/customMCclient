"""Unit regressions for genuine vanilla 1.8.9 Scaffold edge grounding.

These tests never fabricate World, collision, controller or source-native
candidate evidence: they only verify the pure interpretation of observed
original player position samples before the strict original native gate.
"""
import unittest

from RunMojang189SingleplayerSmoke import stable_original_scaffold_edge_support


class NativeScaffoldEdgeGroundingTest(unittest.TestCase):
    def test_nominal_teleport_center_is_stable(self):
        self.assertTrue(stable_original_scaffold_edge_support(
            [(1.05, 100.0, 0.5)] * 3))

    def test_actual_v54_same_sha_retry_collision_z_drift_remains_supported(self):
        # Original unchanged-SHA rerun job 116512106948: native player settled
        # x=1.05, feet y=100.0, z=0.3000000119, but old test demanded 0.50.
        self.assertTrue(stable_original_scaffold_edge_support(
            [(1.05, 100.0, 0.30000001192092896)] * 5))

    def test_requires_three_actual_source_samples(self):
        self.assertFalse(stable_original_scaffold_edge_support([]))
        self.assertFalse(stable_original_scaffold_edge_support(
            [(1.05, 100.0, 0.5)] * 2))

    def test_wrong_foot_height_or_x_cannot_prove_edge_grounding(self):
        self.assertFalse(stable_original_scaffold_edge_support(
            [(1.05, 99.0, 0.5)] * 3))
        self.assertFalse(stable_original_scaffold_edge_support(
            [(0.5, 100.0, 0.5)] * 3))
        self.assertFalse(stable_original_scaffold_edge_support(
            [(1.50, 100.0, 0.5)] * 3))

    def test_z_beyond_original_full_stone_edge_vetoes(self):
        for z in (0.0, 0.20, 0.80, 1.0):
            with self.subTest(z=z):
                self.assertFalse(stable_original_scaffold_edge_support(
                    [(1.05, 100.0, z)] * 3))

    def test_unstable_native_motion_does_not_pass(self):
        self.assertFalse(stable_original_scaffold_edge_support(
            [(1.05, 100.0, 0.30), (1.05, 100.0, 0.42),
             (1.05, 100.0, 0.50)]))
        self.assertFalse(stable_original_scaffold_edge_support(
            [(1.05, 100.0, 0.50), (1.05, 100.08, 0.50),
             (1.05, 100.0, 0.50)]))

    def test_only_last_three_stable_samples_are_authoritative(self):
        self.assertTrue(stable_original_scaffold_edge_support(
            [(0.5, 115.0, 0.5), (0.5, 110.0, 0.5)]
            + [(1.05, 100.0, 0.3)] * 3))


if __name__ == "__main__":
    unittest.main()
