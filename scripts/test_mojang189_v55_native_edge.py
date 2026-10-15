"""Strict native-source edge grounding regressions for the V26 fixture."""
import unittest

from RunMojang189SingleplayerSmoke import original_edge_grounding_stable


class OriginalNativeEdgeGroundingTest(unittest.TestCase):
    def test_original_commanded_center_is_stable(self):
        self.assertTrue(original_edge_grounding_stable(
            [(1.05, 100.0, .5)] * 3))

    def test_v54_real_logged_vanilla_collision_correction(self):
        self.assertTrue(original_edge_grounding_stable(
            [(1.05, 100.0, .30000001192092896)] * 3))

    def test_missing_source_and_moving_samples_fail_closed(self):
        self.assertFalse(original_edge_grounding_stable([]))
        self.assertFalse(original_edge_grounding_stable([(1.05, 100, .3)] * 2))
        self.assertFalse(original_edge_grounding_stable([
            (1.05, 100, .3), (1.05, 100, .34), (1.05, 100, .38)]))

    def test_off_support_and_bad_height_are_vetoes(self):
        for sample in [(1.16, 100, .3), (1.05, 99.7, .3),
                       (1.05, 100, .10), (1.05, 100, .90)]:
            with self.subTest(sample=sample):
                self.assertFalse(original_edge_grounding_stable([sample] * 3))

    def test_stable_but_lateral_motion_is_not_grounding(self):
        self.assertFalse(original_edge_grounding_stable([
            (1.03, 100, .3), (1.05, 100, .3), (1.07, 100, .3)]))


if __name__ == "__main__":
    unittest.main()
