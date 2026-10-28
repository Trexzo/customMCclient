"""Offline regressions for opt-in real-game X11 screenshot provenance."""
import unittest

from Mojang189VisualEvidence import (
    graphite_sample_count, parse_x11_geometry, require_visible_graphite,
)


class NativeVisualEvidenceTests(unittest.TestCase):
    def test_geometry_requires_all_real_window_fields(self):
        raw = "WINDOW=123\nX=83\nY=42\nWIDTH=854\nHEIGHT=480\nSCREEN=0\n"
        self.assertEqual(
            parse_x11_geometry(raw),
            {"left": 83, "top": 42, "width": 854, "height": 480},
        )
        for invalid in (
                "X=0\nY=0\nWIDTH=854\n",
                "X=0\nY=0\nWIDTH=0\nHEIGHT=480\n",
                "X=0\nY=0\nWIDTH=5000\nHEIGHT=480\n",
                "X=0\nY=0\nWIDTH=854\nHEIGHT=-1\n",
                "X=nan\nY=0\nWIDTH=854\nHEIGHT=480\n"):
            with self.subTest(invalid=invalid):
                with self.assertRaises(ValueError):
                    parse_x11_geometry(invalid)

    def test_graphite_requires_real_rgb_pixels_and_detects_known_palette(self):
        width, height = 320, 200
        palette = bytes((25, 27, 31)) * (width * height)
        hits, sampled = graphite_sample_count(palette, width, height)
        self.assertGreater(sampled, 100)
        self.assertEqual(hits, sampled)
        world = bytes((111, 112, 113)) * (width * height)
        hits, sampled = graphite_sample_count(world, width, height)
        self.assertEqual(hits, 0)
        with self.assertRaises(ValueError):
            graphite_sample_count(palette[:55], width, height)

    def test_failure_without_new_graphite_witness_preserves_red(self):
        blank = {"dimensions": [854, 480],
                 "graphite_hits": 0, "graphite_samples": 1200}
        valid = {"dimensions": [854, 480],
                 "graphite_hits": 550, "graphite_samples": 1200}
        require_visible_graphite(blank, valid)
        with self.assertRaises(AssertionError):
            require_visible_graphite(blank, blank)
        with self.assertRaises(AssertionError):
            require_visible_graphite(valid, valid)
        with self.assertRaises(AssertionError):
            require_visible_graphite(
                blank, {**valid, "dimensions": [600, 480]})

    def test_world_frame_with_dark_non_graphite_does_not_pass(self):
        before = {"dimensions": [854, 480],
                  "graphite_hits": 50, "graphite_samples": 1200}
        after = {"dimensions": [854, 480],
                 "graphite_hits": 100, "graphite_samples": 1200}
        with self.assertRaises(AssertionError):
            require_visible_graphite(before, after)


if __name__ == "__main__":
    unittest.main()
