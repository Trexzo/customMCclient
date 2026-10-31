"""Offline regressions for opt-in real-game X11 screenshot provenance."""
import unittest

from Mojang189VisualEvidence import (
    graphite_sample_count, parse_x11_geometry, require_visible_graphite,
    original_target_hud_native_text_contrast,
    original_arraylist_compact_row_pitch,
    original_arraylist_native_shadow_pixels,
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



    def test_original_arraylist_shadow_requires_dark_pixels_adjacent_to_glyphs(self):
        # Unit classifier fixture only: never an original-game screenshot.
        width, height = 854, 480
        sky = bytes((173, 203, 255)) * (width * height)
        frame = bytearray(sky)

        def paint(x0, y0, x1, y1, color):
            for y in range(y0, y1):
                for x in range(x0, x1):
                    at = (y * width + x) * 3
                    frame[at:at + 3] = bytes(color)

        for y in (30, 52, 74):
            # Dark adjacent glyph outlines; bright interior pixels.
            paint(678, y - 2, 762, y + 12, (42, 42, 42))
            paint(680, y, 760, y + 10, (242, 242, 242))
        result = original_arraylist_native_shadow_pixels(frame, width, height)
        self.assertGreaterEqual(result["bright_glyph_pixels"], 600)
        self.assertGreaterEqual(result["adjacent_dark_shadow_pixels"], 400)
        with self.assertRaisesRegex(AssertionError, "exact CI window"):
            original_arraylist_native_shadow_pixels(frame, 640, 480)

        # Removing shadow keeps the exact same bright glyph geometry,
        # but must fail the original RGB witness.
        for y in (30, 52, 74):
            paint(678, y - 2, 762, y + 12, (173, 203, 255))
            paint(680, y, 760, y + 10, (242, 242, 242))
        with self.assertRaisesRegex(AssertionError, "shadow pixels absent"):
            original_arraylist_native_shadow_pixels(frame, width, height)
        with self.assertRaisesRegex(AssertionError, "shadow pixels absent"):
            original_arraylist_native_shadow_pixels(sky, width, height)

    def test_compact_arraylist_glyph_pitch_accepts_real_size_only(self):
        # Synthetic RGB classifier-only unit input, never a game image.
        # The actual gate receives original Mojang X11 framebuffer bytes.
        width, height = 854, 480
        sky = bytes((170, 198, 247)) * (width * height)
        frame = bytearray(sky)

        def row(top):
            for y in range(top, top + 16):
                for x in range(685, 699):
                    at = (y * width + x) * 3
                    frame[at:at + 3] = bytes((242, 242, 242))

        for top in (28, 50, 72):
            row(top)
        result = original_arraylist_compact_row_pitch(frame, width, height)
        self.assertEqual([22, 22], result["physical_row_pitches"])
        self.assertEqual([[28, 43], [50, 65], [72, 87]],
                         result["glyph_bands"])
        with self.assertRaisesRegex(AssertionError, "exact CI window"):
            original_arraylist_compact_row_pitch(frame, 640, 480)

        with self.assertRaisesRegex(AssertionError, "glyph row count"):
            original_arraylist_compact_row_pitch(sky, width, height)

        # The previous verified S36 screenshot had 24-pixel spacing.
        # Keep it a hard negative control (not an allowed alternate).
        previous = bytearray(sky)
        for top in (28, 52, 76):
            for y in range(top, top + 16):
                for x in range(685, 699):
                    at = (y * width + x) * 3
                    previous[at:at + 3] = bytes((242, 242, 242))
        with self.assertRaisesRegex(AssertionError, "row pitch"):
            original_arraylist_compact_row_pitch(previous, width, height)

    def test_original_hud_font_pixel_probe_rejects_wrong_atlas(self):
        # Pure synthetic RGB unit fixture, NEVER a gameplay screenshot.
        # The real acceptance step passes X11-captured original framebuffer.
        width, height = 854, 480
        frame = bytearray(bytes((39, 43, 51)) * (width * height))

        def fill(x0, y0, x1, y1, rgb):
            for y in range(y0, y1):
                for x in range(x0, x1):
                    at = (y * width + x) * 3
                    frame[at:at + 3] = bytes(rgb)

        fill(35, 148, 39, 223, (238, 135, 85))
        # Old actual corrupt native font had virtually no neutral text.
        with self.assertRaisesRegex(AssertionError, "glyph/texture"):
            original_target_hud_native_text_contrast(frame, width, height)
        # Each original glyph line needs a meaningful visible neutral
        # pixel signal; saturated magenta texture fragments do not count.
        fill(60, 161, 72, 171, (190, 190, 190))
        fill(60, 193, 72, 203, (245, 245, 245))
        result = original_target_hud_native_text_contrast(frame, width, height)
        self.assertEqual([35, 148], result["rail"])
        self.assertGreaterEqual(result["nearby_neutral_pixels"], 80)
        self.assertGreaterEqual(result["distance_neutral_pixels"], 80)
        fill(60, 193, 72, 203, (200, 30, 220))
        with self.assertRaisesRegex(AssertionError, "glyph/texture"):
            original_target_hud_native_text_contrast(frame, width, height)

    def test_original_hud_pixel_probe_refuses_other_resolution_or_no_rail(self):
        frame = bytes((39, 43, 51)) * (854 * 480)
        with self.assertRaisesRegex(AssertionError, "accent rail"):
            original_target_hud_native_text_contrast(frame, 854, 480)
        with self.assertRaisesRegex(AssertionError, "exact CI window"):
            original_target_hud_native_text_contrast(frame, 640, 480)

if __name__ == "__main__":
    unittest.main()
