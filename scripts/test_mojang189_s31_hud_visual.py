#!/usr/bin/env python3
"""Offline negative/geometry tests for the strict genuine X11 S31 harness."""
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
import time

from Mojang189HudVisualCapture import (HUD_SEARCH_TERMS,
                                     hud_controls, require_fresh,
                                     original_pause_button_signature,
                                     original_native_ui_scale)


class HudVisualTest(unittest.TestCase):
    def test_clickgui_geometry_854x480_stays_on_real_controls(self):
        focus, clear, module = hud_controls(854, 480)
        self.assertTrue(80 <= focus[0] <= 120)
        self.assertTrue(175 <= clear[0] <= 204)
        self.assertTrue(95 <= focus[1] <= 125)
        self.assertTrue(250 <= module[0] <= 275)
        self.assertTrue(120 <= module[1] <= 140)
        self.assertGreater(clear[0], focus[0])
        self.assertNotEqual(module, focus)

    def test_real_hd_geometry_1280x720_still_has_bounded_controls(self):
        focus, clear, module = hud_controls(1280, 720)
        self.assertTrue(0 < focus[0] < clear[0] < module[0] < 1280)
        self.assertTrue(0 < focus[1] < module[1] < 720)

    def test_only_four_real_vanilla_pause_buttons_allow_physical_menu_recovery(self):
        # Exact source X11 failure at native #41057213142:
        # grey vanilla Game Menu buttons covered a valid Graphite GUI.
        source = [(112, 112, 112), (111, 111, 111),
                  (112, 112, 112), (111, 111, 111)]
        self.assertTrue(original_pause_button_signature(source))
        self.assertFalse(original_pause_button_signature(source[:3]))
        self.assertFalse(original_pause_button_signature(
            source[:3] + [(49, 60, 75)]))  # real sky/game world
        self.assertFalse(original_pause_button_signature(
            source[:3] + [(182, 155, 155)]))  # no grey button
        self.assertFalse(original_pause_button_signature(
            [(10, 10, 10)] * 4))

    def test_real_854x480_graphite_gui_scale_aligns_first_module_row(self):
        # Original run #41059457312: first visible row occupies
        # native physical Y~165..222. Old unscaled click landed at Y133
        # on the Combat section header; never treat that as toggling.
        source = (b"CUSTOMMC_189_NATIVE_VISUAL_PAGE=modules\n"
                  b"CUSTOMMC_189_S31_ORIGINAL_UI_VIEWPORT="
                  b"854x480,scale:1.3333334\n")
        scale = original_native_ui_scale(source, 854, 480)
        focus, clear, module = hud_controls(854, 480, scale)
        self.assertTrue(80 <= focus[0] <= 120)
        self.assertTrue(90 <= focus[1] <= 125)
        self.assertTrue(215 <= clear[0] <= 245)
        self.assertTrue(300 <= module[0] <= 345)
        self.assertTrue(155 <= module[1] <= 190)
        self.assertNotEqual(module, hud_controls(854, 480, 1.0)[2])

    def test_native_scale_cannot_be_inferred_from_stale_or_wrong_frame(self):
        source = b"CUSTOMMC_189_S31_ORIGINAL_UI_VIEWPORT=854x480,scale:1.3333334\n"
        with self.assertRaises(AssertionError):
            original_native_ui_scale(source, 1280, 720)
        with self.assertRaises(AssertionError):
            original_native_ui_scale(source + source, 854, 480)
        with self.assertRaises(AssertionError):
            original_native_ui_scale(b"", 854, 480)
        with self.assertRaises(AssertionError):
            original_native_ui_scale(
                b"CUSTOMMC_189_S31_ORIGINAL_UI_VIEWPORT=854x480,scale:0.25\n",
                854, 480)

    def test_physical_search_does_not_open_original_vanilla_gui_hotkeys(self):
        self.assertEqual(2, len(HUD_SEARCH_TERMS))
        self.assertEqual(["array", "mapp"], [q for q, _ in HUD_SEARCH_TERMS])
        # The original implementation descriptor, NOT inferred screenshot
        # text, must actually contain the no-vanilla-hotkey fragment.
        source = (Path(__file__).resolve().parent.parent
                  / "platform-1.8.9" / "src" / "main" / "java"
                  / "dev" / "trexzo" / "custommc" / "platform"
                  / "v1_8_9" / "Minecraft189TargetHudFeature.java").read_text()
        self.assertIn("mapped remote-player distance and direction", source)
        self.assertIn(HUD_SEARCH_TERMS[1][0], source.lower())
        for query, witness in HUD_SEARCH_TERMS:
            self.assertFalse(any(c in "te" for c in query.lower()))
            self.assertTrue(witness.startswith(b"CUSTOMMC_189_REAL_"))
            self.assertTrue(witness.endswith(b"=YES"))

    def test_invalid_windows_refused(self):
        for dims in ((0, 480), (854, 0), (199, 480), (854, 159)):
            with self.assertRaises(ValueError):
                hud_controls(*dims)

    def test_old_witness_is_not_accepted_as_fresh(self):
        with tempfile.TemporaryDirectory() as path:
            log = Path(path) / "original-host.log"
            marker = b"CUSTOMMC_189_REAL_TARGET_HUD_RENDERED=YES"
            log.write_bytes(marker + b"\n")
            cursor = log.stat().st_size
            with self.assertRaises(AssertionError):
                require_fresh(SimpleNamespace(), log, marker, cursor,
                              time.monotonic() + .05,
                              lambda p, phase: None, "new target")
            with log.open("ab") as out:
                out.write(marker + b"\n")
            require_fresh(SimpleNamespace(), log, marker, cursor,
                          time.monotonic() + 1,
                          lambda p, phase: None, "new target")


if __name__ == "__main__":
    unittest.main()
